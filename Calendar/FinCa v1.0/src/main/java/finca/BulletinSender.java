package finca;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

//import com.ovus.aisekle.telegrambotwork.TBWork;

class BulletinSender {
	private Logger log;
	private static final String TOKEN = ""; // @ 
	private static final String TORODORO = "@";
	private Map<String, String[]> settsMap;
	private List<String> clients;
	private String parentDir;
	private List<List<String>> eventsList;
	private static final String SEPARATOR = "\n";
	private String[] telegram; // telegram[0] = telegram directory , telegram[1] = telegram URL

	public BulletinSender(String parentDir, List<List<String>> eventsList, Logger log) {  
		this.log = log;
        this.parentDir = parentDir;
        this.eventsList = eventsList;
    	clients = Utils.getClients("calendar.clients");
    	
	}
	
    private String translater(String w) {
    	HashMap<String, String> translate = new HashMap<>();
    	translate.put("JPY", "Япония");
    	translate.put("GBP", "Великобритания");
    	translate.put("CNY", "Китай");
    	translate.put("KRW", "ЮжнаяКорея");
    	translate.put("TRY", "Турция");
    	translate.put("AUD", "Австралия");
    	translate.put("EUR", "Еврозона");
    	translate.put("BRL", "Бразилия");
    	translate.put("USD", "США");
    	translate.put("CAD", "Канада");
    	translate.put("MXN", "Мексика");
    	translate.put("CHF", "Швейцария");
    	translate.put("NZD", "НоваяЗеландия");
    	translate.put("RUB", "Россия");
    	translate.put("SGD", "Сингапур");
    	translate.put("UAH", "Украина");
    	translate.put("HKD", "Гонконг");
    	
    	return translate.getOrDefault(w, "");
    }
    
    private String getEventStatus(String volatility) {
    	String result = "";
    	switch(volatility) {
    	case("Низкая волатильность"):
// !!!		result = "%E2%9D%97%E2%9D%95%E2%9D%95";
    		result = "%F0%9F%94%B5%E2%9A%AA%E2%9A%AA";
    		break;
    	case("Умеренная волатильность"):
// !!! 		result = "%E2%9D%97%E2%9D%97%E2%9D%95";
    		result = "%F0%9F%94%B5%F0%9F%94%B5%E2%9A%AA";
    		break;
    	case("Высокая волатильность"):
// !!! 		result = "%E2%9D%97%E2%9D%97%E2%9D%97";
    		result = "%F0%9F%94%B5%F0%9F%94%B5%F0%9F%94%B5";
    		break;
    	}
    	System.out.println("smile = " + result);
    	return result;
    }
    
    public boolean sendBulletin2Telegram(){
    	String result = "";
        String country = "";     // list.get(1)
        String eventStatus = ""; //  list.get(2)
        String currency = "";    // list.get(1)
        String event = "";       // list.get(3)
        String fact = "";        // list.get(4)
        String predict = "";     // list.get(5)
        String past = "";        // list.get(6)
        String text = "";
    	try {  
        		for(List<String> eventSplited : eventsList) {
        			
        			country = translater(eventSplited.get(1));
        			eventStatus = getEventStatus(eventSplited.get(2));
        			currency = eventSplited.get(1);
        			event = eventSplited.get(3);
        			fact = eventSplited.get(4);
        			predict = eventSplited.get(5);
        			past = eventSplited.get(6);
        			
        			text += "#" + country + " $" + currency + " " + eventStatus + SEPARATOR + event + SEPARATOR + "*" + (!past.equals("") ? "было: " + past + SEPARATOR : "")
        					+ (!predict.equals("") ? "ожидалось: " + predict  + SEPARATOR : "")
        					+ (!fact.equals("") ? "стало: " + fact  + SEPARATOR : "") + "*" + SEPARATOR;
        			log.logp(Level.WARNING, "BulletinSender", "sendBulletin2Telegram", "text to send: " + text);
        			System.out.println("text to send to Telegram: " + text);
        		}

        		for(String chatid : clients) {
        			if(chatid.equals(TORODORO)){
//                		Integer hourlyCaption = LocalTime.now(ZoneId.of("Europe/Moscow")).getHour();
                		String htagDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "");        		
                   		text = htagDate + " " + text;
        			}
                    TBWork tbw = new TBWork(TOKEN, chatid); 
                    if((result = tbw.sendMessage("&text=" + text + "&parse_mode=MarkdownV2&disable_notification=false")) != null)
                            if(chatid.equals(TORODORO)) Glossary.Update(TOKEN, TORODORO, parentDir, 176, 3, 2);
        		}
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		return false;
    	}
    return true;
    }
}
