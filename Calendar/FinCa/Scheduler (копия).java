package finca;
 
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Scanner;
import java.util.stream.Collectors;

import java.io.File;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.time.temporal.ChronoUnit;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SchedulerOld {
	private ScheduledExecutorService scheduler;
	private ArrayList<LocalTime> timeList;
	private ConcurrentHashMap<LocalTime, ScheduledFuture<Set<SingleEvent>>> eventsResults = new ConcurrentHashMap<>();
	private final LocalDate today = LocalDate.now();
	private final String installDir =  Utils.getInstallDir();
	
	public void startCalendar(String url, String reserveCalendar, BotSession ruleIt) throws Exception{
		timeList = new ArrayList<>();
		scheduler = Executors.newScheduledThreadPool(2);
		
		Document doc = Jsoup.connect(url).get();
		Elements eventElementRows = doc.select("tr[data-event-datetime]");
		List<Element> holidaysElementRows = (List<Element>) doc.select("tr[id^='eventRowId']").stream()
				.filter(el -> "Выходной".equals(el.child(2).text()))
				.collect(Collectors.toList());
		System.out.println("\nWe have " + holidaysElementRows.size() + " holidays\n");
		
		for(Element wElement : eventElementRows) {
			String dt = wElement.attr("data-event-datetime").replace("/","-").replace(" ","T");
        	LocalDateTime dateTime = LocalDateTime.parse(dt);
            LocalTime eventTime = dateTime.toLocalTime().minusSeconds(120);
        	long delay = LocalTime.now().until(eventTime, ChronoUnit.SECONDS);
        	delay = delay < 0 ? 120L*timeList.size() : delay;
        	
        	if(!timeList.contains(eventTime)) {
        		System.out.println("Event at " + eventTime + " was planned on " + LocalTime.now().plusSeconds(delay));
        	    eventsResults.put(eventTime, scheduler.schedule(new GetEventResult(dateTime, url), delay, TimeUnit.SECONDS));
        	    timeList.add(eventTime);
        	}
		}
		
        // is scheduler was shutted down before 23:59 if yes - start calendar again

        long tillDawn = LocalTime.now().until(LocalTime.of(23, 56), ChronoUnit.MINUTES);
        System.out.println("The Calendar will stop work after " + tillDawn + " minutes at 23:59");
        scheduler.awaitTermination(tillDawn, TimeUnit.MINUTES);
        System.out.println("All scheduled is scheduled.. renew schedule every 10 minutes till 23:45");
        while(LocalTime.now().isBefore(LocalTime.of(23, 45))) {
    		Document docReNew = Jsoup.connect(url).get();
    		List<Element> foundNewEvents = (List<Element>) doc.select("tr[data-event-datetime]").stream()
    				.filter(el -> !timeList.contains(LocalDateTime.parse(el.attr("data-event-datetime").replace("/","-").replace(" ","T"))
    						.toLocalTime().minusSeconds(120)))
    				.collect(Collectors.toList());
            for(Element el : foundNewEvents) {
            	Boolean isHappend = false;
            	LocalDateTime dTime = LocalDateTime.parse(el.attr("data-event-datetime").replace("/","-").replace(" ","T"));
                LocalTime eTime = dTime.toLocalTime().minusSeconds(120);
            	long dlay = LocalTime.now().until(eTime, ChronoUnit.SECONDS);
            	dlay = dlay < 0 ? 120L*timeList.size() : dlay;
        		System.out.println("SOME CHANGES IN SCHEDULE: Event at " + eTime + " was planned on " + LocalTime.now().plusSeconds(dlay));
        	    eventsResults.put(eTime, scheduler.schedule(new GetEventResult(dTime, url), dlay, TimeUnit.SECONDS));
        	    timeList.add(eTime);            	
            }
    		
        	try {
        		Thread.sleep(600000);
        	} catch(Exception e) {}
        }
        System.out.println("It's " + LocalDateTime.now() + " and we go to shotdown scheduler and stop bot session..");
        ruleIt.stop();
        
	}
}


class GetEventResult implements Callable {
	private Map<SingleEvent, Boolean> eventAccum = new HashMap<>(); 
	private Dictionary dictionary;
	private LocalDateTime eventDateTime;
	private String eventDateTimeString;
	private String url;
	private LongPolling lp;
	public GetEventResult(LocalDateTime eventDateTime, String url) {
		this.eventDateTime = eventDateTime;
		this.eventDateTimeString = eventDateTime.toString().replace("-","/").replace("T", " ") + ":00";
		this.url = url;
		this.dictionary = new Dictionary();
		lp = new LongPolling();
	}
	@Override
	public Set<SingleEvent> call() {
		System.out.println("Ok, it's a " + LocalTime.now().toString() + " and we gonna know what with event at " + eventDateTimeString);
		return eventHandler();
	}
	public Set<SingleEvent> eventHandler() {
		List<SingleEvent> outputList = new ArrayList<>();
		List<SingleEvent> noDataEvents = new ArrayList<>();
		ChromeDriver driver = (ChromeDriver) Utils.getChromeWD("1920,1200");
		driver.get(url);
		noDataEvents =  driver.findElements(By.cssSelector("tr[data-event-datetime='" + eventDateTimeString + "']")).stream()
				        .filter(we -> dictionary.isContainingWarningWords(we.findElement(By.cssSelector("td:nth-of-type(4)")).getText()))
				        .collect(ArrayList::new,
				        		(se, we) -> {
				        			String country = we.findElement(By.cssSelector("td:nth-of-type(2) span")).getAttribute("title").replace(" ","");
				        			String caption = we.findElement(By.cssSelector("td:nth-of-type(4)")).getText();
				        			String currentValue = we.findElement(By.cssSelector("td:nth-of-type(5)")).getText();
				        			String outlookValue = we.findElement(By.cssSelector("td:nth-of-type(6)")).getText();
				        			String previousValue = we.findElement(By.cssSelector("td:nth-of-type(7)")).getText();
				        			String level = we.findElement(By.cssSelector("td:nth-of-type(3)")).getAttribute("title");
				        			se.add(new SingleEvent(eventDateTime, country, caption, currentValue, previousValue, outlookValue, level, false));},
				        		ArrayList::addAll);
		System.out.println(noDataEvents.size() + " no data events");
		
		// Check if event persist in time, 2 minutes waiting, if no - go away! START
		WebElement chekElementPresence = new WebDriverWait(driver, 120, 1000).until(wd -> {
			return wd.findElement​(By.cssSelector("tr[data-event-datetime='" + eventDateTimeString + "']"));
		});
		if(chekElementPresence == null)
			return eventAccum.keySet();  
		// Check if event persist in time, 2 minutes waiting, if no - go away! END
		
		new WebDriverWait(driver, 5400, 120000).until((wd) -> {
			List<WebElement> allEventsInTime =  driver.findElements(By.cssSelector("tr[data-event-datetime='" + eventDateTimeString + "']")).stream()
					                            .filter(we -> !dictionary.isContainingWarningWords(we.findElement(By.cssSelector("td:nth-of-type(4)")).getText()))
					                            .collect(Collectors.toList());
            List<SingleEvent> allHappendInTime = allEventsInTime.stream()
            		                                .filter(we -> !"&nbsp".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
            		                                .filter(we -> !" ".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
            		                                .filter(we -> !"".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
            		                                .collect(ArrayList::new,
            		                            		(seb, we) -> {
            		    				        			String country = we.findElement(By.cssSelector("td:nth-of-type(2) span")).getAttribute("title").replace(" ","");
            		    				        			String caption = we.findElement(By.cssSelector("td:nth-of-type(4)")).getText();
            		    				        			String currentValue = we.findElement(By.cssSelector("td:nth-of-type(5)")).getText();
            		    				        			String outlookValue = we.findElement(By.cssSelector("td:nth-of-type(6)")).getText();
            		    				        			String previousValue = we.findElement(By.cssSelector("td:nth-of-type(7)")).getText();
            		    				        			String level = we.findElement(By.cssSelector("td:nth-of-type(3)")).getAttribute("title");
            		    				        			seb.add(new SingleEvent(eventDateTime, country, caption, currentValue, previousValue, outlookValue, level, false));
            		                            		},
            		                            		ArrayList::addAll);
            
            if(!allHappendInTime.isEmpty()) {
                for(SingleEvent se : allHappendInTime)
                	eventAccum.putIfAbsent(se, false);
                System.out.println("Okay.. trying to send..");
                boolean isMajor = allHappendInTime.stream().anyMatch(se -> se.getLevel().contains("Высокая") || se.getLevel().contains("Умеренная"));
                if(isMajor)
                    eventAccum = lp.sendIt(eventAccum);
                // 
            }
            
            if(eventAccum.size() == allEventsInTime.size()) {
                System.out.println("Waiting is over..");
            	return true;
            }
           
            System.out.println("Waiting further more..");
            return false;
			
		});
		if(!eventAccum.isEmpty())
		    lp.sendIt(eventAccum);
		driver.quit();
		System.out.println("All right it is a EventHandler");
		return eventAccum.keySet();
	}
}

class SingleEvent {
	private Boolean isHappend;
	private LocalDateTime dateTime;
	private String country;
	private String caption;
	private String currentValue;
	private String previousValue;
	private String outlookValue;
	private String level;
	
    public SingleEvent(LocalDateTime dateTime, String country, String caption, String currentValue, String previousValue, String outlookValue, String level, Boolean isHappend) {
    	this.dateTime = dateTime;
    	this.country = country;
    	this.caption = caption;
    	this.currentValue = currentValue;
    	this.previousValue = previousValue;
    	this.outlookValue = outlookValue;
    	this.level = level;
    	this.isHappend = isHappend;
    }
    public void setHappening(boolean happening) {
    	isHappend = happening;
    }
	public void setCurrentValue(String curVal) {
		currentValue = curVal;
	}
	public void setPreviousValue(String prevVal) {
		previousValue = prevVal;
	}
	public void setOutlookValue(String outVal) {
		outlookValue = outVal;
	}
	public void setLevel(String lev) {
		level = lev;
	}
    public LocalDateTime getDateTime() {
    	return dateTime;
    }
    public String getCountry() {
    	return country;
    }
    public String getCaption() {
    	return caption;
    }
    public String getCurrentValue() {
    	return currentValue;
    }
    public String getPreviousValue() {
    	return previousValue;
    }
    public String getOutlookValue() {
    	return outlookValue;
    }
    public Boolean getIsHappend() {
    	return isHappend;
    }
    public String getLevel() {
    	return level;
    }
}
