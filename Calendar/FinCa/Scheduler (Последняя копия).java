package finca;
 
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

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

	private HashMap<LocalTime, Set<SingleEvent>> events;
	private Map<SingleEvent, Boolean> accumulator;
	private final String installDir =  Utils.getInstallDir();
	private final BotSession ruleIt;
	private final String url;
	private final String reserveCalendar;
	private final WebDriver driver;
	private final LongPolling lp;
	private Dictionary dictionary;
	
	public Scheduler(String url, String reserveCalendar, BotSession ruleIt) {
		this.url = url;
		this.reserveCalendar = reserveCalendar;
		this.dictionary = new Dictionary();
		this.ruleIt = ruleIt;
		this.driver = Utils.getChromeWD("1920,1200");
//		this.driver = Utils.getFireFoxWD("1920,1200");
		this.events = new HashMap<>();
		this.accumulator = new HashMap<>();
		this.lp = new LongPolling();
	}
	public String availability(String url) {
		try {
			while(Jsoup.connect(url).get() == null) {
				Thread.sleep(120000);
			}
		}finally {}
		return url;
	}
	public boolean isHolliday() {
		List<Element> holidaysElementRows = new ArrayList<>();
		try {
			Document doc = Jsoup.connect(url).get();
			Elements eventElementRows = doc.select("tr[data-event-datetime]");
			holidaysElementRows = (List<Element>) doc.select("tr[id^='eventRowId']").stream()
					.filter(el -> "Выходной".equals(el.child(2).text()))
					.collect(Collectors.toList());
			System.out.println("\nWe have " + holidaysElementRows.size() + " holidays\n");
		}catch (Exception e) {
		    e.printStackTrace();
		}
		return holidaysElementRows.isEmpty();
	}
	public void startCalendar(boolean today) {
		LocalDateTime current = LocalDateTime.now();
		long deltaMinutes = LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0)).until(current, ChronoUnit.MINUTES);
		deltaMinutes = deltaMinutes > 60 ? 60 : deltaMinutes;
		current = LocalDateTime.now().minusMinutes(deltaMinutes);
		try {
			driver.get(availability(url)); // Будет ждать URL ипроверять доступность каждые 2 минуты
			new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.id("economicCalendarData")));
			if(!today) {
				current = LocalDateTime.now().minusDays(1).minusMinutes(60);
				Utils.screenShoot(driver, "blabla", "html");
			    for(WebElement we :driver.findElements(By.xpath("//*[text()[contains(.,'I Accept')]]"))) {
	                    try {
			    		    we.click();
	                    }catch (Exception e) {
	                    }
			    }
				driver.findElement(By.id("timeFrame_yesterday")).click();
				new WebDriverWait(driver, 120).until(ExpectedConditions.visibilityOfElementLocated(By.id("economicCalendarData")));
			}
			LocalDateTime currentDT = current; // because of lambda !
			long tillEndOfTheDay = currentDT.toLocalTime().until(LocalTime.of(23, 55), ChronoUnit.SECONDS);
			System.out.println("To work further " + tillEndOfTheDay/60 + " minutes");

			new WebDriverWait(driver, tillEndOfTheDay, 300000).until(wd -> {
				System.out.println("Okay, ITTERATION: " + LocalDateTime.now());
				List<SingleEvent> happend = driver.findElements(By.cssSelector("tr[data-event-datetime]")).stream()
						.filter(we -> {
							LocalDateTime eventDT = LocalDateTime.parse(we.getAttribute("data-event-datetime").replace("/","-").replace(" ","T"));
//							System.out.println("eventDT.compareTo(currentDT) " + eventDT.compareTo(currentDT));
							return currentDT.until(eventDT, ChronoUnit.SECONDS) >= 0;
						})
						// Фильтруем все события без Данных т.к. Речи, Отчеты и пр.
						.filter(we -> !dictionary.isContainingWarningWords(we.findElement(By.cssSelector("td:nth-of-type(4)")).getText()))
						// Отфильтровываем еще не произошедшие события с пустыми ячейками текущих данных
	                    .filter(we -> !"&nbsp".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
	                    .filter(we -> !" ".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
	                    .filter(we -> !"".equals(we.findElement(By.cssSelector("td:nth-of-type(5)")).getText()))
	                    // Отфильтровать события уже отправленные в телегой для этого есть аккумулятор
	                    .collect(ArrayList::new,
	                    		(seb, we) -> {
	                    			LocalDateTime eventDateTime = LocalDateTime.parse(we.getAttribute("data-event-datetime").replace("/","-").replace(" ","T"));
				        			String country = we.findElement(By.cssSelector("td:nth-of-type(2) span")).getAttribute("title").replace(" ","");
				        			String caption = we.findElement(By.cssSelector("td:nth-of-type(4)")).getText();
				        			String currentValue = we.findElement(By.cssSelector("td:nth-of-type(5)")).getText();
				        			String outlookValue = we.findElement(By.cssSelector("td:nth-of-type(6)")).getText();
				        			String previousValue = we.findElement(By.cssSelector("td:nth-of-type(7)")).getText();
				        			String level = we.findElement(By.cssSelector("td:nth-of-type(3)")).getAttribute("title");
	                                SingleEvent sine = new SingleEvent(eventDateTime, country, caption, currentValue, previousValue, outlookValue, level, false);
	                                if(!accumulator.getOrDefault(sine, false)) {
	                                	seb.add(sine);
	                                }
	                    		},
	                    		ArrayList::addAll);
			System.out.println(happend.size());
				for(SingleEvent se : happend) {
	    			String country = se.getCountry();
	    			String caption = se.getCaption();
	    			accumulator.putIfAbsent(se, false);
				}
	            if(happend.stream().anyMatch(se -> se.getLevel().contains("Высокая") || se.getLevel().contains("Умеренная"))) {
	            	System.out.println("Okay there are VIP events in 'happend' map ");
	            	accumulator = lp.sendIt(accumulator);
	            }
				return false;
			});
        }finally {
        	driver.quit();
        }
	}
}
