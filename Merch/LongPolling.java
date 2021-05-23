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

public class LongPolling extends TelegramLongPollingBot {
	private ScheduledExecutorService scheduler;
	private Dictionary dictionary;
	private ArrayList<LocalTime> timeList;
	private HashMap<String, String> countries = new HashMap<>(); 
	private ConcurrentHashMap<LocalTime, ScheduledFuture<List<SingleEvent>>> eventsResults = new ConcurrentHashMap<>();
	private final LocalDate today = LocalDate.now();
	private final String installDir =  Utils.getInstallDir();
	
	public void startCalendar(String calType, String url) throws Exception{
		timeList = new ArrayList<>();
		scheduler = Executors.newScheduledThreadPool(2);
		List<WebElement> wholeRows = new ArrayList<>();
		dictionary = new Dictionary();
		countries = readDict();
		ChromeDriver driver = (ChromeDriver) Utils.getChromeWD("1920,1200");
		driver.get(url);
		new WebDriverWait(driver,30).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[class='block-forex-calendar__row']")));
		Utils.screenShoot(driver, "calendar", "html");
		if("daily".equals(calType))
			wholeRows = driver.findElements(By.cssSelector("div[class='block-forex-calendar__row']"));
		
        long eventCounter = 0;
        for(WebElement wElement : wholeRows) {
        	Boolean isHappend = false;
        	LocalTime eventTime = LocalTime.parse(wElement.findElement(By.cssSelector("div:nth-of-type(1)")).getText());
        	LocalDateTime dateTime = LocalDateTime.of(today, eventTime);
/*        	String countryFlagUrl = wElement.findElement(By.cssSelector("div[class='block-forex-calendar__title'] div:nth-of-type(1) img")).getAttribute("src");
        	String country = countries.get(countryFlagUrl.substring(countryFlagUrl.indexOf("/circle/")+8));
        	String caption = wElement.findElement(By.cssSelector("div[class='block-forex-calendar__title'] div:nth-of-type(2)")).getText();
        	String currentValue = "";
        	String previousValue = "";
        	String outlookValue = "";
        	String level = wElement.findElement(By.cssSelector("div:nth-of-type(6)")).getAttribute("class").replace("block-forex-calendar__item_","");
        	SingleEvent singleEvent = new SingleEvent(dateTime, country, caption, currentValue, previousValue, outlookValue, level, isHappend);
*/
        	long delay = LocalTime.now().until(eventTime.minusSeconds(120), ChronoUnit.SECONDS);
        	delay = delay < 0 ? 120L*eventCounter : delay;
        	
        	//System.out.println(eventCounter + " Waiting for " + delay + " seconds to know about \"" + eventTime + "\" event");
        	if(!timeList.contains(eventTime)) {
        	    eventsResults.put(eventTime, scheduler.schedule(new GetEventResult(eventTime, url, dictionary), delay, TimeUnit.SECONDS));
        	    timeList.add(eventTime);
        	}
        	eventCounter++;
        	
        }
        scheduler.shutdown();
        driver.quit();
	}
	
    @Override
    public void onUpdateReceived(Update update) {
    }

    @Override
    public String getBotUsername() {
        return "Events";
    }

    @Override
    public String getBotToken() {
        return "1473657414:AAGtn6-sO0pRxUMaK5iF_0-zs8gP3T3W6RE";
    }
    
	private HashMap<String, String> readDict() {
		HashMap<String, String> transformers = new HashMap<>(); 
		String curLine = "";
		try(Scanner scanner = new Scanner(new File(installDir + "Settings/calendar.dic")).useDelimiter("\\R")) {
			while(scanner.hasNext()) {
				curLine = scanner.next();
				if(curLine.contains("="))
				    transformers.put(curLine.split("=")[0],curLine.split("=")[1]);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return transformers;
	}
}

class GetEventResult implements Callable {
	private Dictionary dictionary;
	private LocalTime eventT;
	private String url;
	public GetEventResult(LocalTime eventTime, String url, Dictionary dictionary) {
		this.eventT = eventTime;
		this.url = url;
		this.dictionary = dictionary;
	}
	@Override
	public List<SingleEvent> call() {
		System.out.println("Ok, it's a " + LocalTime.now().toString() + " and we gonna know what with event at " + eventT);
		return eventHandler();
	}
	public List<SingleEvent> eventHandler() {
		List<SingleEvent> outputList = new ArrayList<>();
		ChromeDriver driver = (ChromeDriver) Utils.getChromeWD("1920,1200");
		driver.get(url);
		new WebDriverWait(driver,600, 5000).until(wd -> {
            List<WebElement> allEventsInTime = wd.findElements(By.cssSelector("div[class='block-forex-calendar__row']"));
            if(allEventsInTime.isEmpty()) return false; // No element - go wait on..
            return true;
		});
		new WebDriverWait(driver,3600, 120000).until(wd -> {
            List<WebElement> allEventsInTime = wd.findElements(By.cssSelector("div[class='block-forex-calendar__row']"));
            List<WebElement> filteredEventsInTime = allEventsInTime.stream()
				.filter(we -> eventT.toString().equals(we.findElement(By.cssSelector("div:nth-of-type(1)")).getText().trim()))
				.filter(we -> !dictionary.isContainingWarningWords(we.findElement(By.cssSelector("div[class='block-forex-calendar__title'] div:nth-of-type(2)")).getText()))
				.collect(Collectors.toList());
		    return filteredEventsInTime.stream()
		    		.allMatch(
		    		we -> !"-".equals(we.findElement(By.cssSelector("div[class='block-forex-calendar__row'] div:nth-of-type(3)")).getText())
		    		);
		
		});
		List<WebElement> allEventsInTime = driver.findElements(By.cssSelector("div[class='block-forex-calendar__row']"));
        List<WebElement> filteredEventsInTime = allEventsInTime.stream()
			.filter(we -> eventT.toString().equals(we.findElement(By.cssSelector("div:nth-of-type(1)")).getText().trim()))
			.filter(we -> !dictionary.isContainingWarningWords(we.findElement(By.cssSelector("div[class='block-forex-calendar__title'] div:nth-of-type(2)")).getText()))
			.collect(Collectors.toList());
        filteredEventsInTime.forEach(wElement -> {System.out.println(
        		wElement.findElement(By.cssSelector("div[class='block-forex-calendar__title'] div:nth-of-type(2)")).getText() 
        	+ " " + wElement.findElement(By.cssSelector("div[class='block-forex-calendar__row'] div:nth-of-type(3)")).getText());});
		driver.quit();
		return outputList;
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
    public String getLevel() {
    	return level;
    }
}
