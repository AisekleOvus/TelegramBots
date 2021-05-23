package finca;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import java.nio.file.Paths;

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

class Calendaring implements Callable<Boolean> {
	private WebDriver driver;
	private LocalDateTime dateTime;
	private LocalTime time;
	private String calendarUrl;
	private String calendarName;
	private String installDir;
	private Logger log;
	private HashMap<WebElement, Boolean> accumulator;
	private String soughtDateTimeMain;
	private String soughtDateTimeFallback;
	private String trAttributeMain;
	private String trAttributeFallback;
	private Dictionary dict;

	
    Calendaring(LocalDate date, LocalTime time, String[] calendar, Logger log, Dictionary dict) {
    	this.log = log;
    	this.dateTime = LocalDateTime.of(date, time).minusHours(Utils.getMoscowOffset());
    	this.time = time;
    	this.calendarUrl = calendar[1];
    	this.calendarName = calendar[0];
    	this.dict = dict;
    	accumulator = new HashMap<WebElement, Boolean>();
    	installDir = Utils.getInstallDir();
    	
    	log.logp(Level.WARNING, "Calendaring", "Constructor", "BEGIN TO HANDLE EVENT AT " + time.toString() + calendarName + " calendar");
    	System.out.println("BEGIN TO HANDLE EVENT AT " + time.toString() + " " + calendarName + " calendar");
    }	
    Calendaring(LocalTime time, String[] calendar, Logger log, Dictionary dict) {
    	this(LocalDate.now(), time, calendar, log, dict);
    }
    @Override
    public Boolean call() {
    	try { 
    		getEvent();
    	} catch (TimeoutException toe) {
    		driver.quit();
    		return new Boolean(false);
    	}
    	return new Boolean(true);
    }
    
    private boolean getEvent() throws TimeoutException{
    	
    	String trAttribute = "";
    	soughtDateTimeMain = dateTime.plusHours(Utils.getMoscowOffset()).truncatedTo(ChronoUnit.MINUTES).toString().replace("T", " ").replace("-", "/") + ":00";
    	soughtDateTimeFallback = dateTime.truncatedTo(ChronoUnit.MINUTES).toString().replace("T", " ") + ":00";
        trAttributeMain = "data-event-datetime";
        trAttributeFallback = "event_timestamp";
    	
    	
    	driver = HeadlessChromeInit.initChromeDriver("1920,1200", installDir);
    	driver.get(calendarUrl);
    	
    	//Wait until all cell of waitingList will be with text
    	log.logp(Level.WARNING, "Calendaring", "getEvent at" + time.toString(), "Wait for 60 min until all cell of waitingList will be with text");
    	System.out.println("Event at " + time.toString() + " : Wait for 60 min until all cell of waitingList will be with text");
        // wait for publishing for 90 min with 2 minute polling
        try {
           	if(calendarName.equals("main")) {
            	if(driver.findElements(By.cssSelector("tr[" + trAttributeMain + "=\"" + soughtDateTimeMain + "\"]")).size() == 0) return true;
            	new WebDriverWait(driver, 5400, 120000).until((wd) -> {   
            		List<WebElement> trl = wd.findElements(By.cssSelector("tr[" + trAttributeMain + "=\"" + soughtDateTimeMain + "\"]"));
            		int trlSize = trl.size();
            		System.out.println("WAIT FOR ALL " + "tr[" + trAttributeMain + "=\"" + soughtDateTimeMain + "\"] TO BE FULL OF TEXT..");    			
        			for(WebElement we : trl) {
        				List<WebElement> tdl = we.findElements(By.cssSelector("td"));                
        				if(dict.isContainingWarningWords(tdl.get(3).getText())) {
        					trlSize--;
        					continue;
        				}
        				if(!tdl.get(4).getText().equals(" ") && !tdl.get(4).getText().equals("&nbsp;") && !tdl.get(4).getText().equals(""))
        					accumulate(we);
        				//else return false;
        			}
        			log.logp(Level.WARNING, "Calendaring", "EVENT AT " + time.toString(),"Try to send something to Telegram");
        			log.logp(Level.WARNING, "Calendaring", "EVENT AT " + time.toString(), LocalTime.now() + " there are(is) " + trl.size() + " event(s) accumulated..");
        			eventSender(trl.size());
        			if(accumulator.size() >= trlSize) {
        				System.out.println("accumulator.size() >= trl.size() waiting should be finished!");
        				return true;
        			}
        			
        			return false;
        		});
            	Utils.screenShoot(driver, "Now_" + LocalTime.now() + "_But_Event_at_" + time.toString(), "table[id=\"economicCalendarData\"]");
        	}
        	if(calendarName.equals("fallback")) {
        	    if(driver.findElements(By.cssSelector("tr[" + trAttributeFallback + "=\"" + soughtDateTimeFallback + "\"]")).size() == 0) return true;
            	new WebDriverWait(driver, 5400, 120000).until((wd) -> {   
            		List<WebElement> trl = wd.findElements(By.cssSelector("tr[" + trAttributeFallback + "=\"" + soughtDateTimeFallback + "\"]"));
            		int trlSize = trl.size();
            		System.out.println("WAIT FOR ALL " + "tr[" + trAttributeFallback + "=\"" + soughtDateTimeFallback + "\"] TO BE FULL OF TEXT..");    			
        			for(WebElement we : trl) {
        				List<WebElement> tdl = we.findElements(By.cssSelector("td"));                
        				if(dict.isContainingWarningWords(tdl.get(3).getText())) {
        					trlSize--;
        					continue;
        				}
        				if(!tdl.get(4).getText().equals(" ") && !tdl.get(4).getText().equals("&nbsp;") && !tdl.get(4).getText().equals(""))
        					accumulate(we);
        				//else return false;
        			}
        			log.logp(Level.WARNING, "Calendaring", "EVENT AT " + time.toString(),"Try to send something to Telegram");
        			log.logp(Level.WARNING, "Calendaring", "EVENT AT " + time.toString(), LocalTime.now() + " there are(is) " + trl.size() + " event(s) accumulated..");
        			eventSender(trl.size());
        			if(accumulator.size() >= trl.size()) {
        				System.out.println("accumulator.size() >= trl.size() waiting should be finished!");
        				return true;
        			}
        			
        			return false;
        		});
            	Utils.screenShoot(driver, "Now_" + LocalTime.now() + "_But_Event_at_" + time.toString(), "table[id=\"ecEventsTable\"]");
        	}
        } catch(TimeoutException toe) {
        	eventSender(0);
        	log.logp(Level.WARNING, "Calendaring", "getEvent at " + time.toString(), "Wait time out.. Event have not done!");
        	System.out.println("Event at " + time.toString() + " : Wait time out.. Event have not done!");
        	toe.printStackTrace();
        	Utils.screenShoot(driver, "TOE for " + time.toString(), calendarName.equals("fallback") ? "table[id=\"ecEventsTable\"]" : "table[id=\"economicCalendarData\"]");
        } finally {
        	driver.quit();
        }
    	return true;
    }
    private void accumulate(WebElement element) {
    	accumulator.putIfAbsent(element, new Boolean(false));  // value 'true' means is event was sent? TRUE is for sent events.
    }
    private void eventSender(int totalEvents) {
    	boolean isSendable = true;
    	if(accumulator.size() < totalEvents) {
    	isSendable = accumulator.entrySet().stream()
    			                        .filter(el -> !el.getValue())
    			                        .anyMatch(el -> el.getKey().findElements(By.cssSelector("td")).stream()
    			                        		        .anyMatch(val -> {
    			                        		        	String volatility = val.getAttribute("title") != null ? val.getAttribute("title") : "";
    			             								if(volatility.toLowerCase().contains("умеренная волатильность") 
    			             										|| volatility.toLowerCase().contains("высокая волатильность"))
    			             									return true;
    			             								else return false;
    			                        		        }));
    	} 
    	if(isSendable) {

    		List<List<String>> eventsList = new ArrayList<>();
    		for(Map.Entry<WebElement, Boolean> entryWE : accumulator.entrySet()) {
    			if(!entryWE.getValue() && !dict.isContainingWarningWords(entryWE.getKey().getText()) && entryWE.getKey().getText().contains(time.toString())) {
    				eventsList.add(entryWE.getKey().findElements(By.cssSelector("td"))
    						           .stream() 
    				        		   .collect(ArrayList::new, (sup, val) -> { 
   								            String volatility = val.getAttribute("title") != null ? val.getAttribute("title") : "";
     								        sup.add(volatility.contains("волатильность") ? volatility
     										                                             :val.getText().trim());}
     								, ArrayList::addAll));
    				accumulator.compute(entryWE.getKey(), (k, v) -> true);
    			}
    		}
        	
    		if(eventsList.size() > 0) {
                BulletinSender bs = new BulletinSender(installDir, eventsList, log);
                log.logp(Level.WARNING, "Calendaring", "getEvent at " + time.toString(), "All good, try to send " + eventsList.size() + " events to Telegram");
                System.out.println("Event at " + time.toString() + " : All good, try to send " + eventsList.size() + " events to Telegram");
                
                log.logp(Level.WARNING, "Calendaring", "getEvent at " + time.toString(), "Shutdown Chrome");
                System.out.println("Event at " + time.toString() + " : Shutdown Chrome");

                bs.sendBulletin2Telegram();
    		} else {
    			System.out.println("Event at " + time.toString() + " gone somewhere, eventsList size is 0");
    			log.logp(Level.WARNING, "Calendaring", "getEvent", "Event at " + time.toString() + " gone somewhere, eventsList size is 0");
    		}
    	}
    }
}
