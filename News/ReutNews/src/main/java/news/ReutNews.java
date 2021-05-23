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
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Optional;

import java.io.File;
import java.io.FileWriter;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


class ReutNews {
	private static final String TOKEN = "1493941048:AAHa3_T37G7G33mXhYPNPW1yPu-Akfun2GA"; // @ToroDelOroMarketNewsBot 
	private static final String TORODORO = "@torodioro";
	private String parentDir;
	private Set<String> pubTitles;
	private String newsUrl;
	private LocalDate today;
	
	public ReutNews(String newsUrl, String parentDir) {
		pubTitles = new LinkedHashSet<String>();
        this.parentDir = parentDir;
        this.newsUrl = newsUrl;
        getpubTitles(parentDir);
	}
	
	private void getpubTitles(String pt) {
		File f = new File(pt + "Settings/news.ttls");
		if(f.exists()) {
			try(Scanner scanner = new Scanner(f).useDelimiter("\\R")) {
				while(scanner.hasNext())
					pubTitles.add(scanner.next());
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	
	public Map<String, String> getNews(Set<String> oldTitles) {
		pubTitles.addAll(oldTitles);
		Map<String, String> news = new HashMap<>();
		pubTitles.forEach(System.out::println);
		try {
			Document doc = Jsoup.connect(newsUrl).get();
			Elements articles = doc.getElementsByTag("article");
			String title = "";
	        for(Element article : articles) {
	        	title = article.getElementsByTag("h3").text();
	        	if(!pubTitles.contains(title)) {
	        	    news.put(title, article.getElementsByTag("p").text());
	        	}
	        }
	        return news;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return news;
	}
	
	private void savepubTitles() {
		try(FileWriter fwr = new FileWriter(parentDir + "Settings/news.pd")) {
			for(String pubTitle : pubTitles)
				fwr.write(pubTitle.toString() + System.lineSeparator());
			fwr.flush();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
}