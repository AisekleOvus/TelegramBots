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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Setup {
	
	public static String getInstallDir() {
    	try {
    		String installDir = Paths.get(GatherMarketData.class        // These several strings of code help to understand where we are
    				           .getProtectionDomain()                   //
                               .getCodeSource()                         //
                               .getLocation()                           //
                               .getPath()).getParent().toString() + "/";//
    		return installDir;
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	return "";
    }
	
    public static Map<String, String[]> getMcdSettings(String installDir) {
    	HashMap<String, String[] > settsMap = new HashMap<>();
    	String[] strArr = new String[2];
    	String line = null;
    	try(Scanner settsScanner = new Scanner(new File(installDir + "Settings/" + "mcd.set")).useDelimiter(System.lineSeparator())) {
        	while(settsScanner.hasNext()) {
        		line = settsScanner.next();
        		strArr = line.split("  ");
       		    settsMap.put(strArr[0], strArr[1].split("=="));
        		
        	}
        	settsMap.forEach((k,v) -> System.out.println(k + " " + v[0] + "==" + v[1]));
    	} catch(Exception e) {
    	    e.printStackTrace();
    	}
        return settsMap;
    }	
}