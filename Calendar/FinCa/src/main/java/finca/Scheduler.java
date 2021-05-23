package finca;
 
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import java.io.File;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

import org.openqa.selenium.interactions.Actions;
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

public class Scheduler {

	private  List<SingleEvent> events;

	private Map<SingleEvent, Boolean> accumulator;
	private String installDir =  Utils.getInstallDir();
	private final BotSession ruleIt;
	private final String url;
	private final String reserveCalendar;
	private final LongPolling lp;
	private long deltaTime;
//	private LocalDateTime filterDt;
	private Dictionary dictionary;
	
	public Scheduler(String url, String reserveCalendar, BotSession ruleIt) {
		this.url = url;
		this.reserveCalendar = reserveCalendar;
		this.dictionary = new Dictionary();
		this.ruleIt = ruleIt;
		this.accumulator = new HashMap<>();
		this.events = Collections.synchronizedList(new ArrayList<>());
		this.lp = new LongPolling();
		
/*//      Рассчитываем filterDt, начальное время, должно быть не раньше начала текущего дня, должно быть не больше 60 минут до настоящего момента 		
		long deltaTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)).until(LocalDateTime.now(), ChronoUnit.MINUTES);
		deltaTime = deltaTime > 360 ? 360 : deltaTime;
		filterDt = LocalDateTime.now().minusMinutes(deltaTime);*/
	}
	public void clearEnv() {
		accumulator = new HashMap<>();
		installDir =  Utils.getInstallDir();
		deltaTime = 0L;
//		filterDt = null;
		dictionary = new Dictionary();
		events = Collections.synchronizedList(new ArrayList<>());
	}
	public String availability(String url) {
		try {
			while(Jsoup.connect(url).get() == null) {
				Thread.sleep(120000);
			}
		}catch(Exception e) {}
		System.out.println("Okay url : " + url + " available..");
		return url;
	}
    public String adopter(String str) {
    	return str.replaceAll("\\R","").replaceAll("[\\h\\v]", " ").replaceAll("\\s{2,}", " ");
    }
	public void startCalendar(boolean today) {
		LocalDate nowaday = null;
		while(true) {
			nowaday = LocalDate.now();
			System.out.println("Okay starting Conductor thread..");
			Thread conductorThread = new Thread(new Conductor());
			conductorThread.start();
			while(LocalDate.now().isEqual(nowaday)) { // if today has been yesterday - it is time to start another iterration
				try { Thread.sleep(360000); }
				catch (Exception e) {}
			}
			if(!conductorThread.isAlive()) {
				System.out.println("Previous Conductor stream was terminated successfully");
			}
			
		}

	}
	
    class Conductor implements Runnable {
    	List<SingleEvent> tmpEvents;
    	ScheduledExecutorService sched;
    	String currrentD = LocalDate.now().toString().replace("-", "/");
    	LocalDateTime currentDT = LocalDateTime.now();
    	Document doc;
    	List<Element> holidays;
    	
    	
    	@Override
    	public void run() {
//    		if(getEventsSetSchedule()) 
                if(toConduct())
                	System.out.println("Last Orchestra is done, now Conductor should be terminated..");
    	}
    	/**
    	 *  toConduct -  метод, при вызывании которого в цикле происходит проверка и обновление рассписания событий
    	 *  
    	 */
   	  
    	public boolean toConduct() {
    		/*ScheduledExecutorService*/ sched = Executors.newScheduledThreadPool(1);
    			
    		sched = schedNewcomer(sched);
    		System.out.println("Okay 'scheduler' is filled..");
    		sched.shutdown();
    		while(!sched.isTerminated()) {
    			try {
    				Thread.sleep(360000);
    				sched = schedNewcomer(sched);
    				System.out.println(LocalTime.now() + " we have new sched now...");
    			} catch (Exception e) {}
    		}
        System.out.println("sched is terminated");
    	return sched.isTerminated();
    	}
    	private LocalDateTime getFilterDT() {
			long deltaTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)).until(LocalDateTime.now(), ChronoUnit.MINUTES);
			deltaTime = deltaTime > 60 ? 60 : deltaTime;
			return LocalDateTime.now().minusMinutes(deltaTime);
    	}
    	private ScheduledExecutorService schedNewcomer(ScheduledExecutorService sched) {
    		if(getEventsSetSchedule()) {

    			LocalDateTime filterDt = getFilterDT();
    			
        		Set<LocalDateTime> timeSet = new LinkedHashSet<>();
        		// Создаем расписание
        		System.out.println("Okay creating schedule..");
        		timeSet = events.stream()
        				.filter(se -> se.getDateTime().compareTo(filterDt) >= 0)
        				.collect(LinkedHashSet::new, (ts, se) -> ts.add(se.getDateTime()), LinkedHashSet::addAll);
        		timeSet.forEach(System.out::println);
        		int counter = 0;
        		for(LocalDateTime ldt : timeSet) {
        			++counter;
        			System.out.print(counter + " : ");
        			// Вычисляем разницу во времени между соседними событиями
        			
        			LocalDateTime nextEventDT = timeSet.stream()
        					.filter(dt -> ldt.compareTo(dt) < 0)
        					// если событие последнее в списке - интервал  сделать до 23:50
        					.min((dt1, dt2) -> dt1.compareTo(dt2)).orElse(LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 50)));
        			long delay = LocalDateTime.now().until(ldt, ChronoUnit.SECONDS);
        			long delayTime = ldt.until(nextEventDT, ChronoUnit.SECONDS);
        			
        			if(ldt.compareTo(LocalDateTime.now()) < 0 && nextEventDT.compareTo(LocalDateTime.now()) < 0) {
        				delay = 60 * counter;              // задержка выполнения
        				delayTime = 180;                   // сколько времени будет выполняться
        			}
        			if(ldt.compareTo(LocalDateTime.now()) < 0 && nextEventDT.compareTo(LocalDateTime.now()) > 0) {
        				delay = 60 * counter;   
        				delayTime = LocalDateTime.now().until(nextEventDT, ChronoUnit.SECONDS);
        			}
            		System.out.println("Событие в " + ldt + " будет запущено через " + delay + " и будет выполняться " + delayTime + " до " + nextEventDT);
            		sched.schedule(new Orchestra(ldt, delayTime), delay, TimeUnit.SECONDS);
        		}
    		}
    		return sched;
    	}
    	public boolean getEventsSetSchedule() {   	
    		try { 		
    			
    			LocalDateTime filterDt = getFilterDT();
    			
        		holidays = new ArrayList<>();
        		doc = Jsoup.connect(availability(url)).get();
        		holidays = (List<Element>) doc.select("tr[id^='eventRowId']").stream()                // праздники
        					.filter(el -> "Выходной".equals(el.child(2).text()))
        					.collect(Collectors.toList());
        		tmpEvents =  doc.select("tr[data-event-datetime^='" + currrentD + "']").stream()
        				    .filter(we -> LocalDateTime.parse(we.attr("data-event-datetime").replace("/","-").replace(" ","T")).compareTo(filterDt) >=0 )
        				    .filter(we -> {
        				    	String volatility = we.select("td:nth-of-type(3)").get(0).attr("title");
        				    	return volatility !=null && !volatility.toLowerCase().contains("низкая");
        				    	})
        				    .collect(ArrayList<SingleEvent>::new, (seb, we) -> {
	                    			LocalDateTime eventDateTime = LocalDateTime.parse(we.attr("data-event-datetime").replace("/","-").replace(" ","T"));
				        			String country = we.select("td:nth-of-type(2) span").get(0).attr("title").replace(" ","");
				        			String caption = adopter(we.select("td:nth-of-type(4)").get(0).wholeText());
				        			String currentValue = we.select("td:nth-of-type(5)").get(0).wholeText();
				        			String outlookValue = we.select("td:nth-of-type(6)").get(0).wholeText();
				        			String previousValue = we.select("td:nth-of-type(7)").get(0).wholeText();
				        			String level = we.select("td:nth-of-type(3)").get(0).attr("title");
	                                SingleEvent sine = new SingleEvent(eventDateTime, country, caption, currentValue, previousValue, outlookValue, level, false);
                                	// В случае повторного вызова метода, копируем значения полей isHappend из старого листа<SingleEvent> в новый
	                                if(!events.isEmpty() && events.contains(sine)) {
//	                                	System.out.println("setting up 'isHappend' was " + events.get(events.indexOf(sine)).getIsHappend() + " now - " + sine.getIsHappend());
                                		sine.setIsHappend(events.get(events.indexOf(sine)).getIsHappend());
	                                } //else {
                                	    seb.add(sine);
	                                //}
	                     		},
        				    		ArrayList<SingleEvent>::addAll);
        		// Сортируем события по времени, на всякий случай
        		 Collections.sort(tmpEvents, (se1,se2) -> se1.getDateTime().compareTo(se2.getDateTime()));
                 events = Collections.synchronizedList(tmpEvents);
                 System.out.println("Okay have gotten " + events.size() + " event(s)..");
                 return !events.isEmpty();
    		}catch (Exception e) {
    		    e.printStackTrace();
    		}
    		return false;
    	}
    }
    class Orchestra implements Runnable {
    	private LocalDateTime eventDT;
    	private long nextEventDelta; // in seconds
    	private long deltaTime;
		private WebDriver driver;
		private boolean isLast;

    	public Orchestra(LocalDateTime eventDT, long nextEventDelta) {
    		this.eventDT = eventDT;
    		this.nextEventDelta = nextEventDelta - 120;
    		this.deltaTime = deltaTime;
    		isLast = LocalTime.of(23,50).isBefore(LocalTime.now().plusSeconds(nextEventDelta + 5));

    	}
    	@Override
    	public void run() {
    		driver = Utils.getChromeWD("1920,1200");
//    		driver = Utils.getFireFoxWD("1920,1200");

    		System.out.println(LocalDateTime.now() + " Okay let's play the music.. ");
    		// Собрать это в список для поиска на вебстранице

    		List<SingleEvent> seToSearch = events.stream()
    				                       .filter(se -> !dictionary.isContainingWarningWords(se.getCaption()))
    		                               .filter(se -> !se.getIsHappend())
//    		                               .filter(se -> se.getDateTime().compareTo(eventDT.minusMinutes(deltaTimeForLambda)) >= 0)
    		                               .filter(se -> se.getDateTime().compareTo(eventDT) <= 0)
    		                               .collect(Collectors.toList());
    		if(seToSearch.size() > 0) {
        		System.out.println("Okay we will search for : ");
        		seToSearch.forEach(se -> System.out.println(se.getCaption() + " - " + se.getIsHappend()));
    		}
    		
    		
    		try {
    			driver.get(availability(url)); // Будет ждать URL и проверять доступность каждые 2 минуты
    			new WebDriverWait(driver, nextEventDelta, 60000).until(wd -> {
    				System.out.println("Ждем <= 2минуты");
    				try {
    				    new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.id("economicCalendarData")));
    				} catch (Exception e) {
    					System.out.println("Не дождались таблицы с календарем");
    				}
    				System.out.println("Дождались");
    				// Ишем все события из списка seToSearch по отдельности
    				System.out.println("Okay printing handled events : ");
    			    for(SingleEvent se : seToSearch) {
    			    	String seDT = se.getDateTime().toString().replace("-","/").replace("T"," ") + ":00";
//    			    	System.out.println("Ищу событие: \n" + se);
    			    	
    			    	if(se.getIsHappend()) continue;
    			    	List<WebElement> wElements = driver.findElements(By.cssSelector("tr[data-event-datetime='" + seDT + "']"));
    			    	if(wElements.size() == 0) return true; // Если wElements.size() == 0 значит событие из каленжаря исчезло нужно перейти к следующему
//                     Point to event on page
    			    	WebElement moveToElement = wElements.get(wElements.size() - 1);
    			    	Actions actions = new Actions(driver);
    			    	actions.moveToElement(moveToElement);
    			    	actions.perform();
    			    	
        				Optional<WebElement> neededE = wElements.stream()  
        						           .filter(we -> {
        						        	   
        			    			    		if(se.getCaption().trim().equals(adopter(we.findElement(By.cssSelector("td:nth-of-type(4)")).getText()).trim())) {
        			        			    		if(!dictionary.isContainingWarningWords(se.getCaption())) {
        			        			    			System.out.println("Значение поля события : " + we.findElement(By.cssSelector("td:nth-of-type(5)")).getText());
        			            			    		if(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText().matches("\\s*-?\\d+[.,]?(?=\\d)\\d*\\s*%?B?M?K?\\s*"))
        			    	        		    			System.out.println(LocalTime.now() + " " + se.getCaption() + " - Event contains value");
        			    			            		else 
        			    			    	        		System.out.println(LocalTime.now() + " " + se.getCaption() + " - Event doesn't contain value");
        			        			    		}
        			    			    		}
        						        	   
        						        	  return se.getCaption().trim().equals(adopter(we.findElement(By.cssSelector("td:nth-of-type(4)")).getText()).trim())
        						               && se.getCountry().equals(we.findElement(By.cssSelector("td:nth-of-type(2) span")).getAttribute("title").replace(" ",""))
        						               && !dictionary.isContainingWarningWords(se.getCaption()) 
        						               && we.findElement(By.cssSelector("td:nth-of-type(5)")).getText().matches("\\s*-?\\d+[.,']?\\d*[.,]?\\d*\\s*%?B?M?K?\\s*");
        						               })
        						           .findFirst(); 
        				System.out.println("Событие в " + seDT + "результат " + neededE.isPresent());
        				
        				if(neededE.isPresent()) {
        					int eIndx = events.indexOf(se);
        					se.setCurrentValue(neededE.get().findElement(By.cssSelector("td:nth-of-type(5)")).getText().replaceAll("\\R",""));
        					se.setPreviousValue(neededE.get().findElement(By.cssSelector("td:nth-of-type(7)")).getText().replaceAll("\\R",""));
        					se.setOutlookValue(neededE.get().findElement(By.cssSelector("td:nth-of-type(6)")).getText().replaceAll("\\R",""));
        					se.setIsHappend(true);
        					System.out.println("\n\n" + se.getCaption() + " IS " + se.getIsHappend());
        					seToSearch.set(seToSearch.indexOf(se), se);
        					System.out.println(seToSearch.get(seToSearch.indexOf(se)).getCaption() + " IS " + seToSearch.get(seToSearch.indexOf(se)).getIsHappend()+"\n\n");
        					accumulator.put(se, false);
        					
        				} else {
            					Utils.screenShoot(driver, se.getCaption().replaceAll("[():\\s]","_") + "_" + neededE.isPresent(), "html");

        					continue;
        				}
        				
        	            if(accumulator.entrySet().stream().anyMatch(entry -> 
        	                (entry.getKey().getLevel().contains("Высокая") || entry.getKey().getLevel().contains("Умеренная")) && !entry.getValue())) {
        	            	System.out.println("Okay there are VIP events in 'happend' map ");
        	            	accumulator = lp.sendIt(accumulator);
        	            	while(!accumulator.get(se)) {
        	            		try {
        	            			Thread.sleep(1000);
        	            		} catch (Exception e) {}
        	            	}
        	            	System.out.println(se + " - " + accumulator.get(se));
        	            	System.out.println("Содержимое аккумулятора:");
        	            	accumulator.forEach((k,v) -> System.out.println(k + " - " + v));
        	            	
        	            }
    			    }
    			    boolean wasItOk = seToSearch.stream().allMatch(se -> se.getIsHappend());
    			    if(!wasItOk) driver.navigate().refresh();
    			    return wasItOk;
    			});

    			
            }finally {
        	    driver.quit();
        	    if(accumulator.entrySet().stream().anyMatch(entry -> !entry.getValue())) {
    		    	System.out.println("Отправляем остатки:");
    			    accumulator = lp.sendIt(accumulator);
        	    }
        	    if(isLast) {
        	    	System.out.println("Last event finished!");
        	    	
        	    }
            }
        }
    }
}
