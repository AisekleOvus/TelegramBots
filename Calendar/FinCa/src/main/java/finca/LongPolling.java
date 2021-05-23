package finca;
 
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

import java.io.File;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
    
    public Map<SingleEvent, Boolean> sendIt(Map<SingleEvent, Boolean> eventsList) {
    	StringBuilder eventText = new StringBuilder();
    	List<SendMessage> sendMessageList = new ArrayList<>();
    	List<String> clients = Utils.getClients();
    	System.out.println("Active Customers:");
    	clients.forEach(System.out::println);
    	int eventCounter = 0;

        	for(Map.Entry<SingleEvent, Boolean> entry : eventsList.entrySet()) {

        		if(!entry.getValue()) {
        			System.out.println("Формируем пост для " + entry.getKey().getCaption());
            	    eventText.append(getCountryFlag(entry.getKey().getCountry()) + "" + "#" + entry.getKey().getCountry() + " " + getEventStatus(entry.getKey().getLevel()) + "\n");
            	    eventText.append(entry.getKey().getCaption() + "\n");
            	    eventText.append("*было : " + entry.getKey().getPreviousValue() + "\n");
            	    eventText.append("прогноз : " + entry.getKey().getOutlookValue() + "\n");
            	    eventText.append("стало : " + entry.getKey().getCurrentValue() + "*\n");
//            	    eventText.append("важность " + getEventStatus(entry.getKey().getLevel()) + "\n");
            	    eventsList.replace(entry.getKey(), true);
            	    System.out.println("Поменялся ли статус события ? " + eventsList.get(entry.getKey()));
            	    eventCounter++;
        		}
        		
        	}

    	if(eventText.length() == 0) return eventsList;
    		
    	System.out.println("Отправлено " + eventCounter);
    	eventCounter = 0;
    	for(String chatId : clients) {
    	    SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setParseMode("MarkdownV2");

            String text = Utils.escapes(eventText.toString())
                    .replace("\\*","*")
                    .replace("\\~","~")
                    .replace("\\`","`")
                    .replace("\\_","_");
            if(chatId.equals("@torodioro")) {
        		String htagDate = "\\#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "").replace(".", "\\.");        		
       		    text = htagDate + "\n" + text;
    	    }
            System.out.println("Will try to send " + text.length() + " symbols");
            message.setText(text);
            try {
            	System.out.println(execute(message).getText());
            	
            } catch (Exception e) {
            	e.printStackTrace();
            }
    	}
    	return eventsList;        
    }
    private String getEventStatus(String volatility) {
    	String result = "";
    	if(volatility.toLowerCase().contains("низкая волатильность")) {
// !!!		result = "%E2%9D%97%E2%9D%95%E2%9D%95";
//    		result = "%F0%9F%94%B5%E2%9A%AA%E2%9A%AA";
//    		result = "\u26aa\ufe0f\u26aa\ufe0f\ud83d\udd35";
    		result = "\ud83d\udd25"; 
    	}
    	if(volatility.toLowerCase().contains("умеренная волатильность")) {	
// !!! 		result = "%E2%9D%97%E2%9D%97%E2%9D%95";
//    		result = "%F0%9F%94%B5%F0%9F%94%B5%E2%9A%AA";
//    		result = "\u26aa\ufe0f\ud83d\udd35\ud83d\udd35";
    		result = "\ud83d\udd25\ud83d\udd25";
    	}
    	if(volatility.toLowerCase().contains("высокая волатильность")) {
// !!! 		result = "%E2%9D%97%E2%9D%97%E2%9D%97";
//    		result = "%F0%9F%94%B5%F0%9F%94%B5%F0%9F%94%B5";
//    		result = "\ud83d\udd35\ud83d\udd35\ud83d\udd35";
    		result = "\ud83d\udd25\ud83d\udd25\ud83d\udd25";
    	}
    	System.out.println("volatility = " + volatility + " smile = " + result);
    	return result;
    }
    private String getCountryFlag(String country) {
    	String result = "";
    	switch(country) {
    	case("США"):	
    		result = "\ud83c\uddfa\ud83c\uddf8";
    		break;
    	case("Австралия"):	
    		result = "\ud83c\udde6\ud83c\uddfa";
    		break;
    	case("Великобритания"):	
    		result = "\ud83c\uddec\ud83c\udde7";
    		break;
    	case("НоваяЗеландия"):	
    		result = "\ud83c\uddf3\ud83c\uddff";
    		break;
    	case("Япония"):	
    		result = "\ud83c\uddef\ud83c\uddf5";
    		break;
    	case("Гонконг"):	
    		result = "\ud83c\udded\ud83c\uddf0";
    		break;
    	case("Китай"):	
    		result = "\ud83c\udde8\ud83c\uddf3";
    		break;
    	case("Россия"):	
    		result = "\ud83c\uddf7\ud83c\uddfa";
    		break;
    	case("Швейцария"):	
    		result = "\ud83c\udde8\ud83c\udded";
    		break;
    	case("Франция"):	
    		result = "\ud83c\uddeb\ud83c\uddf7";
    		break;
    	case("Испания"):	
    		result = "\ud83c\uddea\ud83c\uddf8";
    		break;
    	case("Италия"):	
    		result = "\ud83c\uddee\ud83c\uddf9";
    		break;
    	case("Германия"):	
    		result = "\ud83c\udde9\ud83c\uddea";
    		break;
    	case("Еврозона"):	
    		result = "\ud83c\uddea\ud83c\uddfa";
    		break;
    	case("Канада"):	
    		result = "\ud83c\udde8\ud83c\udde6";
    		break;
    	case("Сингапур"):	
    		result = "\ud83c\uddf8\ud83c\uddec";
    		break;
       }
    	return result;
    }
    
}