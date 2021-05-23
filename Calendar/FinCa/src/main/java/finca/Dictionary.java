package finca;

import java.util.Set;
import java.util.Scanner;
import java.util.HashSet;

import java.io.File;

public class Dictionary {
    private Set<String> calendarWarningWordsSet;
    
    public Dictionary() {
    	calendarWarningWordsSet = loadDictionary();  	
    }
    
    public boolean isContainingWarningWords(String string) {
		return calendarWarningWordsSet.stream().anyMatch(el -> string.toLowerCase().contains(el));
    }
    private Set<String> loadDictionary() {
    	Set<String> dict = new HashSet<>();
    	try(Scanner scanner = new Scanner(new File(Utils.getInstallDir() + "Settings/calendar.sw")).useDelimiter("\\R")) {
    		while(scanner.hasNext())
    			dict.add(scanner.next());
    			
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	return dict;
    }
}
