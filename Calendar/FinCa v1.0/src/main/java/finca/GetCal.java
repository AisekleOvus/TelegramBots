package finca;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.HashSet;
import java.util.TreeSet;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import java.io.FileWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class  GetCal {
//	private LinkedHashMap<LocalTime, Boolean> eventsTime;
    private ConcurrentHashMap<LocalTime, Boolean> doneEvents;
	private SortedSet<LocalTime> eventsTime;
	private LinkedHashMap<LocalTime, Future<Boolean>> eventsInProgress;
	private String nowadays;
	private Logger log;
	private ExecutorService executor;
	private ExecutorService missedEventsExecutor;
	private Document doc;
	private Dictionary dict;
	
    public GetCal(String nowadays, Logger log) {
    	this.log = log;
    	this.nowadays = nowadays;
    	dict = new Dictionary();
    	ArrayList<String> holidaysList = new ArrayList<>();
    	eventsTime = Collections.synchronizedSortedSet(new TreeSet<>());
    	doneEvents = new ConcurrentHashMap<>();
    	eventsInProgress = new LinkedHashMap<>();
    	executor = Executors.newFixedThreadPool(2);
    	missedEventsExecutor = Executors.newFixedThreadPool(1);
//                             IS IT HOLIDAY TODAY ?
        try {
        	String[] calendar = Utils.getCalendarURL();               // [0] - calendar name, [1] - calendar url
            doc = Jsoup.connect(calendar[1]).get();                   // Check for wich calendar to use
            
			System.out.println("Looking for Holiday in " + calendar[0] + " calendar..");
			System.out.println("Calendar URL : " + calendar[1]);
			log.logp(Level.WARNING, "GetCal", "Constructor", "Looking for Holiday in " + calendar[0] + " calendar..");
            
        	Elements holiSearchList = doc.select("tr[id^=eventRowId]");
        	for(Element element : holiSearchList) {
        		Elements getAllCells = element.select("td");
    			if(getAllCells.get(2).text().toLowerCase().contains("праздник") || getAllCells.get(2).text().toLowerCase().contains("выходной")) {
        			System.out.println(getAllCells.get(3).text() + " - Holiday found !");
        			log.logp(Level.WARNING, "GetCal", "Constructor", getAllCells.get(3).text() +  " - Holiday found !");
    				holidaysList.add(getAllCells.get(3).text()); 
    			}
        	}
        	doc = null;  // Attempt to free memory
        	
        	if(holidaysList.size() > 0) {
    			System.out.println("We have a hollidays today :)");
    			log.logp(Level.WARNING, "GetCal", "Constructor", "We have a hollidays today :)");
    			executor.execute(new HolidaysHandler(holidaysList, log));                         // YES IT IS HOLIDAY TODAY - STRAT "HOLIDAY HANDLER"
        	}
        	
        	// ------------------------------------- END HOLLIDAYS BLOCK -----------------------------------------------------------------//
        	
        	// ------------------------------------- WORK TILL AND OF THE DAY ------------------------------------------------------------//
        	
            if(justInTime()) { // While - true, may be?
    			System.out.println("Ok, all events passed, let's wait till end of the day");
    			log.logp(Level.WARNING, "GetCal","Constructor", "Ok, all events passed, let's wait till end of the day");
            	LocalTime endOfTheDay = LocalTime.parse("23:55:00");
            	if(endOfTheDay.isAfter(LocalTime.now())) {
                	if(!eventsTime.isEmpty()) {
                		SortedSet<LocalTime> setOld = eventsTime;
                		SortedSet<LocalTime> setNew = Collections.synchronizedSortedSet(new TreeSet<>());
                		if(getEventsTime())                                // On every hot cases
                		    setNew = eventsTime;
                		
                		if(setOld.size() != setNew.size() || !setOld.containsAll(setNew)) {
                 			System.out.println("Ok, it seems we have NEW SCHEDULE");
                 			log.logp(Level.WARNING, "GetCal", "justInTime()","Ok, it seems we have NEW SCHEDULE");

                 			justInTime();                   
                		}
                	}
            	}
            	executor.shutdown();
            }
        } catch(Exception e) {
        	e.printStackTrace();
        	executor.shutdown();
        }
    }
    public GetCal(Logger log) {
    	this(LocalDate.now().toString(), log);
    }

    private boolean getEventsTime() { // Get schedule
        try {
        	String[] calendar = Utils.getCalendarURL();               // [0] - calendar name, [1] - calendar url
            doc = Jsoup.connect(calendar[1]).get();                   // Check for wich calendar to use
            String trAttribute = calendar[0].equals("main") ? "data-event-datetime" : "event_timestamp";
            
         	Elements getAllRows = doc.select("tr[" + trAttribute + "]");
         	
         	for(Element element : getAllRows) {
         		if(dict.isContainingWarningWords(element.text())) continue;
         		if("fallback".equals(calendar[0]) && !Utils.isAppropriateDateTime(element.attributes().get(trAttribute))) continue; 
         		Elements getAllCells = element.select("td");
         		try {     			
         			LocalTime eventAt = LocalTime.parse(getAllCells.get(0).ownText());
         			eventsTime.add(eventAt);
         			doneEvents.putIfAbsent(eventAt, false);
         		} catch(java.time.format.DateTimeParseException dtpe) {      			
         			continue;
         		}
         	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        doc = null;
        return true;
    }
    private boolean isMissedEvents() {
    	LocalTime currentTime = LocalTime.now();
    	for(Map.Entry<LocalTime, Future<Boolean>> eventry : eventsInProgress.entrySet()) 
    		if(eventry.getValue().isDone()) {
    			try {
    				
    			    if(!eventry.getValue().get()) 
    				    doneEvents.compute(eventry.getKey(), (k, v) -> false);
    			
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    			
    	// Here are may be another check of time presence in eventsTime
    	return doneEvents.entrySet().stream().anyMatch(entry -> entry.getKey().isBefore(currentTime) && !entry.getValue());
    }
    
    public boolean justInTime() throws Exception {
    	log.logp(Level.WARNING, "GetCal", "justInTime()", "Let's handle all events ..");
    	long waitingTime = 0L;
    	log.logp(Level.WARNING, "GetCal", "justInTime()", "Getting events..");
    	getEventsTime();                                                                      //                        Renewing events map
    	System.out.println("The schedule: ");
    	eventsTime.forEach(System.out::println);
//      for(Map.Entry<LocalTime, Boolean> eventTimeEntry : eventsTime.entrySet()) {
        for(LocalTime eventTimeEntry : eventsTime) {
            LocalTime timeNow = LocalTime.now();      	
        	
        	// I think this check is, probably, not needed
        	if(timeNow.isAfter(eventTimeEntry) && doneEvents.containsKey(eventTimeEntry) && doneEvents.get(eventTimeEntry)) {                            
        		//if event time is before current time and is marked as perfect - skip it.
            	log.logp(Level.WARNING, "GetCal", "justInTime()", "Event at " + eventTimeEntry.toString() + " is before and done");
        		continue;                                                                                          
            
        	} else if(timeNow.isAfter(eventTimeEntry) && doneEvents.containsKey(eventTimeEntry) && !doneEvents.get(eventTimeEntry)) { 
        		// event is before current time and  isn't done
            	        
        		calendarit(eventTimeEntry, missedEventsExecutor);
            	log.logp(Level.WARNING, "GetCal", "justInTime()", "Event at " + eventTimeEntry.toString() + " is before and not done. Done it!");
            	Thread.sleep(120000L); // wait-wait-wait... oh god!
            	continue;
        	} else if(timeNow.isBefore(eventTimeEntry) && doneEvents.containsKey(eventTimeEntry)  && !doneEvents.get(eventTimeEntry)){ 
            	log.logp(Level.WARNING, "GetCal", "justInTime()", "Event at " + eventTimeEntry.toString() + " is after and not done. Done it !");
        		waitingTime = timeNow.until(eventTimeEntry, ChronoUnit.MILLIS);        // event is after current time
        	}
        	
        	
        	log.logp(Level.WARNING, "GetCal", "justInTime()", "Ok I'm sleeping now, will be woken up in " + ((waitingTime-120000)/1000/60) +  " min");
        	
//        	------------------------------ NEW SCHEDULE CATCHER -------------------------------------------------------------------- //
        	//Try to spend time carefully
        	boolean isNewSchedule = false;
        	for(long i = 0L; i < (waitingTime - 120000L); waitingTime = waitingTime - 60000L) { // inspect schedule every 1 minutes
        		long start = System.currentTimeMillis();
            	if(!eventsTime.isEmpty()) {
            		SortedSet<LocalTime> setOld = eventsTime;
            		SortedSet<LocalTime> setNew = Collections.synchronizedSortedSet(new TreeSet<>());
            		if(getEventsTime())                                // On every hot cases
            		    setNew = eventsTime;
            		
            		if(setOld.size() != setNew.size() || !setOld.containsAll(setNew)) {
             			System.out.println("Ok, it seems there were some changes in schedule");
             			log.logp(Level.WARNING, "GetCal", "justInTime()", "Ok, it seems there were some changes in schedule");
             			isNewSchedule = true;    
             			System.out.println("The OLD schedule: ");
             			setOld.forEach(System.out::println);
            			break;
            		}
            		if(isMissedEvents()) {
            	    	System.out.println("Searching for missed events:");
             			System.out.println("Ok, it seems there were some missed events");
             			log.logp(Level.WARNING, "GetCal", "justInTime()", "Ok, it seems there were some missed events");
             			isNewSchedule = true;            
             			System.out.println("The OLD schedule: ");
             			setOld.forEach(System.out::println);
            			break;
            		}
            	}
            	long deltaTime = System.currentTimeMillis() - start;
            	if(deltaTime < 60000)
            	    Thread.sleep(60000L - deltaTime);  // precision ;)
        	}
        	if(isNewSchedule) {
     			System.out.println("Ok, going deeper");
     			log.logp(Level.WARNING, "GetCal", "justInTime()", "Ok, going deeper");
     			justInTime();                   
    			break;
        	}
        	
//             ------------------------------ NEW SCHEDULE CATCHER -------------------------------------------------------------------- //
        	
        	calendarit(eventTimeEntry, executor);
        	
        	log.logp(Level.WARNING, "GetCal", "justInTime()", "Well, event is processing, just wating for 2 min before next event check. New iterration.");
        	System.out.println("Well, event is processing, just wating for 2 min before next event check. New iterration.");
        	
        	Thread.sleep(120000L);
        }
    	
    	log.logp(Level.WARNING, "GetCal", "justInTime()", "All events started to handle.. Just shutdown the ExecutorService as soon as possible");
    	System.out.println("All events started to handle.. Just shutdown the ExecutorService as soon as possible");
    	
        return true;
    }
//	--------------------------------- CALENDARE IT ------------------------------------------------------------------------------//    
    private void calendarit(LocalTime eTime, ExecutorService executeEvent) {
    	log.logp(Level.WARNING, "GetCal", "justInTime()", "Now is " + LocalTime.now() + " and I gonna handle events at " + eTime.toString());
    	System.out.println("Now is " + LocalTime.now() + " and I gonna handle events at " + eTime.toString());
    	
    	doneEvents.compute(eTime, (k, v) -> true);
    	System.out.println("Before \"execute Calendaring Thread\" let's change event status - " + eTime.toString() + " = " + doneEvents.get(eTime));
    	eventsInProgress.put(eTime, executeEvent.submit(new Calendaring(eTime, Utils.getCalendarURL(), log, dict)));
    }

}
