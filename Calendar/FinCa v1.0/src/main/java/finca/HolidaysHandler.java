package finca;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

class HolidaysHandler  implements Runnable {
	private static final String TOKEN = "1467541734:AAFzmytZ2PodzdDA33mzDJmI-C3hE3a7vtE"; // @ToroDelOroMarketHolidaysBot 
	private static final String TORODORO = "@torodioro";
	private Logger log;
	private String text;
	private List<String> holidaysList;
	private Map<String, String> namesCrossNames;
	private Map<String, String> namesCrossFiles;
	private List<String> clients;
	private String installDir;
	private String postcardsDir;
	private String[] telegram;
	
	public HolidaysHandler(List<String> holidaysList, Logger log) {
		this.telegram = Utils.getTelegramDirAndUrl();
		this.holidaysList = holidaysList;
		this.log = log;
		clients = Utils.getClients("holidays.clients");
		installDir = Utils.getInstallDir();
		postcardsDir = installDir + "Holidays/";
		namesCrossFiles = getNamesCrossFiles();
	}
	@Override
	public void run() {
		sendHoliPicture();
	}
	private Map<String, String> getNamesCrossFiles() {
		Map<String, String> mapNamesFiles = new HashMap<>(); 
		try(Scanner fileScanner = new Scanner(new File(postcardsDir + "holi.days")).useDelimiter("\\R")) {
			String[] splitedLine = new String[2]; 
			while(fileScanner.hasNext()) {
				splitedLine = fileScanner.next().split("=");
				mapNamesFiles.putIfAbsent(splitedLine[0].trim(), splitedLine[1].trim());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	    return mapNamesFiles;
	}
	private String getNamesCrossCaption(String caption) {
		HashMap<String, String> namesCrossCaptions = new HashMap<>();
		namesCrossCaptions.put("США - День благодарения","День Благодарения");
		namesCrossCaptions.put("США - День благодарения - Раннее Закрытие в 13:00","День Благодарения");
		namesCrossCaptions.put("Германия - Канун Рождества","Канун Рождества");
		namesCrossCaptions.put("США - Рождество","Рождество");
		
		
		return namesCrossCaptions.containsKey(caption) ? namesCrossCaptions.get(caption) : "";
	}
	
	private boolean sendHoliPicture() {
		String cardFileName = "";
		String htagDate = "";
		StringBuilder textBuilder = null;
	
		
		HashMap<String, String> postcards = new HashMap<>();
		List<String> removeFromHoliList = new LinkedList<>();
		for(String holidaysString : holidaysList) {
			if(!(cardFileName = getCardFileName(holidaysString)).equals("")) {
				try {
					Path sourceP = Paths.get(postcardsDir + cardFileName);
					Path destP = Paths.get(telegram[0] + cardFileName);
					Files.copy(sourceP, destP, StandardCopyOption.REPLACE_EXISTING);
					
					postcards.putIfAbsent(holidaysString, cardFileName);
					removeFromHoliList.add(holidaysString);  
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if(holidaysList.removeAll(removeFromHoliList) && holidaysList.size() > 0) {
		    textBuilder = holidaysList.stream().collect(StringBuilder::new, (sb, el) -> sb.append(el.split(" -.*")[0] + " "), StringBuilder::append);
		    text = "\n*Также выходной в:*\n" + textBuilder.toString();
		}
		for(String chatid : clients) {
			if(chatid.equals(TORODORO))
        		htagDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "");
			
			
            TBWork tbw = new TBWork(TOKEN, chatid); 
            
            postcards.forEach((caption, fn) -> {
                tbw.sendPhoto("&photo=" + telegram[1] + fn + "&parse_mode=MarkdownV2&caption=" + caption + "\n" + (text!=null ? text : "") + "&disable_notification=false");
            });

                if(chatid.equals(TORODORO)) 
                	Glossary.Update(TOKEN, TORODORO, installDir, 176, 3, 2);
		}
		return true;
	}
	private String getCardFileName(String holidayString) {
		String name = getNamesCrossCaption(holidayString);
		return namesCrossFiles.containsKey(name) ? namesCrossFiles.get(name) : "";
	}
	
}