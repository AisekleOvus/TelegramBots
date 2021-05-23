package gmd;

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
import java.time.LocalDateTime;
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

import com.ovus.aisekle.telegrambotwork.TBWork;

class SliceSender {

	private static final String TOKEN = "1401189439:AAEIkru6VxBrOQpuQn1I-uTgmHh7MTXAnHw"; // @ToroDelOroMarketSlicerBot 
	private static final String TORODORO = "@torodioro";
	private Map<String, String[]> settsMap;
	private LinkedHashMap<Integer, String> timers;
	private List<String> clients;
	private String parentDir;
	private String[] telegram; // telegram[0] = telegram directory , telegram[1] = telegram URL

	public SliceSender(String parentDir) {
        this.parentDir = parentDir;
        timers = new LinkedHashMap<Integer, String>();
    	settsMap = lastInit(Setup.getMcdSettings(parentDir));
    	clients = getClients(parentDir);
    	
	}
	private List<String> getClients(String dir) {
/*		LocalDate currentD = LocalDate.now();
		LinkedList<String> clientsList = new LinkedList<>();
		try(Scanner sc = new Scanner(new File(dir + "Settings/" + "slicer.clients")).useDelimiter("\\R")) {
			while(sc.hasNext()) {
				String clientLine = sc.next();
				if(!clientLine.startsWith("//")) {
					String[] clientInfo = clientLine.split(" ");
					if(currentD.compareTo(LocalDate.parse(clientInfo[2]).plusDays(2)) < 0)
					    clientsList.add(clientInfo[0]);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return clientsList;*/
		return DBWork.getClients("Обзор ситуации на рынках");
	}
    private String translater(String w) {
    	HashMap<String, String> translate = new HashMap<>();
    	translate.put("Asia", "Азия");
    	translate.put("Americas", "Америка");
    	translate.put("Europe", "Европа");
    	translate.put("Bonds", "Десятилетние\\_Государственные\\_Облигации");
    	translate.put("Crude", "Нефть");
    	translate.put("Currencies", "Рубль");
    	translate.put("Metalls", "Металлы");
    	
    	return translate.getOrDefault(w, "");
    }
    
    private Map<String, String[]> lastInit(Map<String, String[]> params) {
    	for(Map.Entry<String, String[]> entry : params.entrySet()) {
    		if(entry.getKey().contains("timer"))
    			timers.put(Integer.parseInt(entry.getValue()[0]), entry.getValue()[1]);
    	}
    	telegram = params.get("telegram");
    	return params;
    }
    
    public boolean sendSlice2Telegram(String fileName){
    	
        String result = null;
        String caption = "#" + translater(fileName.split("_")[2]);
        String stopWords = "#Азия #Америка #Европа";
        
    	try {
    		// COPY MAPFILE to dirT    		
            Files.copy(Paths.get(parentDir + "maps/" + fileName), Paths.get(telegram[0] + fileName), StandardCopyOption.REPLACE_EXISTING);
    		
    		for(String chatid : clients) {
    			System.out.println("Send slice to " + chatid);
    			if(chatid.equals(TORODORO) && stopWords.contains(caption)){
            		Integer hourlyCaption = LocalTime.now(ZoneId.of("Europe/Moscow")).getHour();
            		String htagDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "");        		
               		caption = htagDate + " " + caption + " " + ( timers.containsKey(hourlyCaption) ? timers.get(hourlyCaption) : "");
    			}
                String photoUrl = telegram[1] + fileName;
                System.out.println(photoUrl);
 
                TBWork tbw = new TBWork(TOKEN, chatid); 
                if((result = tbw.sendPhoto("&photo=" + photoUrl + "&parse_mode=MarkdownV2&caption=" + caption + "&disable_notification=false")) != null)
                        if(chatid.equals(TORODORO)) Glossary.Update(TOKEN, TORODORO, parentDir, 176, 3, 2);
    		}
    		// DELETE MAPFILE FROM T-DIR
    		Files.deleteIfExists(Paths.get(telegram[0] + fileName));
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		return false;
    	}
    return true;
    }
}
