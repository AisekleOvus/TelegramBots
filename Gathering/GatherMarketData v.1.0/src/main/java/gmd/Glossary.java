package gmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import com.ovus.aisekle.telegrambotwork.TBWork;

public class Glossary {
    public static boolean Update(String token, String chatid, String installDir, int message_id, int checkDateIndex, int insertIndex) { // 176 3 2
    	
	    String lastHashTaggDate = "#"+LocalDate.now().minusDays(1L).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ","");
	    String hashTaggDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ","");
	    StringBuilder result = new StringBuilder();
	    LinkedList<String> firstMessage = new LinkedList<>();
	    try(Scanner fmScanner = new Scanner(new File(installDir + "FMessage")).useDelimiter(System.lineSeparator())) {
	        while(fmScanner.hasNext())
	            firstMessage.add(fmScanner.next());
	        if(firstMessage.size() == 15)
	            firstMessage.remove(3);
	        if(firstMessage.get(firstMessage.size() - insertIndex).equals("")) {
	         	if(firstMessage.get(firstMessage.size() - checkDateIndex).equals(lastHashTaggDate)) {
		          	firstMessage.add(firstMessage.size() - insertIndex, hashTaggDate);
	         	}
	        }

	        for(String line : firstMessage)
	            result.append(line+System.lineSeparator());
	        TBWork tbw = new TBWork(token, chatid);
	        tbw.editMessageText("&message_id="+message_id+"&parse_mode=MarkdownV2"+"&text="+result.toString());
	        BufferedWriter bwr = new BufferedWriter(new FileWriter(installDir + "FMessage", false));
	        for(String line : firstMessage)
	            bwr.write(line+System.lineSeparator());
	        bwr.flush();
	        bwr.close();
	    }catch(Exception e) {
	        e.printStackTrace();
	    }              
	    return true;
	}
}