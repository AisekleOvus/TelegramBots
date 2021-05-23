package news;

import java.util.Scanner;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

import java.util.stream.Stream;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Optional;

import java.io.File;
import java.io.FileWriter;

import java.net.URL;

import com.apptastic.rssreader.DateTime;
import com.apptastic.rssreader.Item;
import com.apptastic.rssreader.RssReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import com.ovus.aisekle.telegrambotwork.TBWork;

class RussianNews implements Runnable {
	private static final String TOKEN = "1493941048:AAHa3_T37G7G33mXhYPNPW1yPu-Akfun2GA"; // @ToroDelOroMarketNewsBot 
	private static final String TORODORO = "@torodioro";
	private String parentDir;
	private List<String> clients;
	private List<LocalDateTime> pubDates;
	private String rssUrl;
	private LocalDate today;
	
	public RussianNews(String rssUrl, String parentDir) {
		pubDates = new ArrayList<LocalDateTime>();
        clients = getClients("russianNews.clients", parentDir);
        this.parentDir = parentDir;
        this.rssUrl = rssUrl;
        getPubDates(parentDir);
	}
	
	private void getPubDates(String pd) {
		File f = new File(pd + "Settings/news.pd");
		if(f.exists()) {
			try(Scanner scanner = new Scanner(f).useDelimiter("\\R")) {
				while(scanner.hasNext())
					pubDates.add(LocalDateTime.parse(scanner.next()));
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		System.out.println("News Go Go !!");
		LocalTime endOfTheDay  = LocalTime.parse("23:50:00");
		while(endOfTheDay.isAfter(LocalTime.now())) {
		    getNews();
		    try { Thread.sleep(60000); }
		    catch(Exception e) { e.printStackTrace(); }
		}
		savePubDates();
	}
	
	public void getNews() {
    	String text = "";
    	String result="";
    	try {
        	RssReader reader = new RssReader();
        	Stream<Item> rssFeed = reader.read(rssUrl);
        	List<Item> rssItems = rssFeed
        			.filter(item -> item.getCategory().orElse("").toLowerCase().equals("экономика"))
        			.filter(item -> !pubDates.contains(item.getPubDateZonedDateTime().orElse(ZonedDateTime.now()).toLocalDateTime()))
        			.collect(Collectors.toList());
        	for(String chatid : clients) {
        		TBWork tbw = new TBWork(TOKEN, chatid);
                for(Item item : rssItems) {
                	pubDates.add(item.getPubDateZonedDateTime().orElse(ZonedDateTime.now()).toLocalDateTime());
                	text = "#события \n*" + item.getTitle().orElse("") + "*\n" + item.getDescription().orElse("") + "\n" + new URL(rssUrl).getHost();
                	if(chatid.equals(TORODORO)){
                		String htagDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "");        		
                   		text = htagDate + " " + text;
                	}
                    if((result = tbw.sendMessage("&text=" + text + "&parse_mode=MarkdownV2")) != null)
                        if(chatid.equals(TORODORO) && today != LocalDate.now()) {
                        	Glossary.Update(TOKEN, TORODORO, parentDir, 176, 3, 2);
                        	today = LocalDate.now();
                        }
                    
                    System.out.println(result);
                    if(rssItems.size() > 1) {
                    	try { Thread.sleep(60000); }
                    	catch(Exception e) { e.printStackTrace(); }
                    }
                }
        	}
    	} catch(Exception e) {
    		savePubDates();
    		e.printStackTrace();
    	}
	}
	
	private void savePubDates() {
		try(FileWriter fwr = new FileWriter(parentDir + "Settings/news.pd")) {
			for(LocalDateTime pubDate : pubDates)
				fwr.write(pubDate.toString() + System.lineSeparator());
			fwr.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private  List<String> getClients(String fname, String dir) {
		LinkedList<String> clientsList = new LinkedList<>();
		try(Scanner sc = new Scanner(new File(dir + "Settings/" + fname)).useDelimiter("\\R")) {
			while(sc.hasNext()) {
				String client = sc.next();
				if(!client.startsWith("//")) clientsList.add(client);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return clientsList;
	}
	
}