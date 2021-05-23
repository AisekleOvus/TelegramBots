package newsbot;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.LinkedHashMap;

import java.net.Authenticator;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpClient.Version;
import java.net.URI;

import java.time.Duration;
import java.time.LocalDateTime;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NewsBot {
	private final static String NEWS_URL = "https://finviz.com/news.ashx";
	private TitlesStore titles;
	public NewsBot() {
		titles = TitlesStore.getInstance();
		readNews();
	}
	private boolean readNews() {
	
		Elements titlesElements = getDocument(NEWS_URL).select("tr.nn");
		LinkedHashMap<String, String> resultmap = titlesElements.stream()
				                                  .filter(element -> element.child(0).className().matches(".*is-[(3)(10)(7)(2)]$"))
				                                  .filter(element -> !titles.contains(element.child(2).text()))
				                                  .collect(LinkedHashMap::new, (ll, el) -> ll.put(el.child(2).text(), el.child(2).child(0).attr("href")), LinkedHashMap::putAll);
		titles.addAll(resultmap.keySet());
//		titles.printAll();
		try {
		LinkedHashMap<String, String> translatedMap = new Translater(resultmap).translate();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	private Document getDocument(String url) {
		try {
		    Document d = Jsoup.connect(url).get();
		    return d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Document("");
	}
}

class Translater {
	private static final String OAU = "";
	private LinkedHashMap<String, String> mapToTranslate;
    private static final String INSTALL_DIR = getInstallDir();
    private  String folderId;
    private  String translaterCommand;
	
	public Translater(LinkedHashMap<String, String> mapToTranslate) {
		try(Scanner scanFolderId = new Scanner(new File(INSTALL_DIR + "Settings/folder_id")).useDelimiter("\\R")) {
			folderId = scanFolderId.next();
		}
		catch(Exception e) {}
		this.mapToTranslate = mapToTranslate;
	}
	public LinkedHashMap<String, String> translate() throws Exception{
		String iam_token = "";
		StringBuilder toTranslate = new StringBuilder();
		StringBuilder trLines = new StringBuilder();

		LinkedHashMap<String, String> translated = new LinkedHashMap<>();

            iam_token = getIamToken();
		toTranslate.append("curl -X POST -H \"Content-Type: application/json\" -H \"Authorization: Bearer ");
		toTranslate.append(iam_token + "\" ");
		toTranslate.append("-d \"{\\\"folder_id\\\":\\\"\\\",\\\"texts\\\":[");
		int i = 0;
		Iterator<String> setIterator = mapToTranslate.keySet().iterator();
		while(setIterator.hasNext()) {
			toTranslate.append((i > 0 ? "," : "" ) + "\\\"" + setIterator.next() + "\\\"");
			i++;
		}
		toTranslate.append("],\\\"targetLanguageCode\\\":\\\"ru\\\",\\\"sourceLanguageCode\\\":\\\"en\\\"}\" https://translate.api.cloud.yandex.net/translate/v2/translate");
		
//		System.out.println(toTranslate.toString());
		
		String trLine = "";
		String translaterCommand = toTranslate.toString() ;
		
//		Process process = Runtime.getRuntime().exec("java -version");
		Process process = Runtime.getRuntime().exec(translaterCommand);
		Scanner scanner = new Scanner(process.getErrorStream());
		while(scanner.hasNext())
			System.out.println(scanner.next());
		
		
//		Process proc = Runtime.getRuntime().exec("java -version");
		
//		Process process = Runtime.getRuntime().exec(translaterCommand);
//		Process process = new ProcessBuilder(translaterCommand.split(" ")).start();
		
		
//		Scanner erScanner = new Scanner(process.getErrorStream());
//		Scanner trScanner = new Scanner(proc.getInputStream());
//		System.out.println("erScanner.hasNext() = " + erScanner.hasNext());
//		System.out.println("trScanner.hasNext() = " + trScanner.hasNext());
/*		while(erScanner.hasNext()) {
			System.out.println(erScanner.next());
		}*/
/*		while(trScanner.hasNext()) {
			System.out.println(trScanner.next());
		}*/

		return null;
	}
	public String getIamToken() throws Exception{
		String iamTokenDataString = "";
		LocalDateTime iamTokenExpirationDateTime = LocalDateTime.of(1, 1, 1, 1, 1);
		StringBuilder sb = new StringBuilder();

		Scanner scanIamToken = new Scanner(new File(INSTALL_DIR + "Settings/iam_token")).useDelimiter("\\R");
		if(scanIamToken.hasNext()) {
			iamTokenDataString = scanIamToken.next();
			iamTokenExpirationDateTime = LocalDateTime.parse(scanIamToken.next());
			scanIamToken.close();
		}
		if(iamTokenDataString.isEmpty() || iamTokenExpirationDateTime.isBefore(LocalDateTime.now())) {
			String command = "curl -d {\"yandexPassportOauthToken\":\"\"} https://iam.api.cloud.yandex.net/iam/v1/tokens";
			Process process = Runtime.getRuntime().exec(command);
			Scanner scanner = new Scanner(process.getInputStream());
			while(scanner.hasNext())
				sb.append(scanner.next().replaceAll("[{}\"]", "").replace("\\R", ""));
				
			iamTokenDataString = sb.toString();
			iamTokenDataString = iamTokenDataString.substring(0, iamTokenDataString.indexOf(",")).split(":")[1];
			iamTokenExpirationDateTime = LocalDateTime.now().plusHours(12L);
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(INSTALL_DIR + "Settings/iam_token", false));
			bwr.write(iamTokenDataString);
			bwr.write("\n" + iamTokenExpirationDateTime.toString());
			bwr.flush();
			bwr.close();
		}
		System.out.println("IAM_TOKEN : " +iamTokenDataString);
		return iamTokenDataString;
	}
	private static String getInstallDir() {
    	try {
    		String installDir = Paths.get(Translater.class        // These several strings of code help to understand where we are
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
}

class TitlesStore {
	private static TitlesStore instance;
	private LinkedList<String> wholeTitles;
	
	private TitlesStore() {
		wholeTitles = new LinkedList<>();
	}
	
	public static TitlesStore getInstance() {
		if(instance == null)
			instance = new TitlesStore();
		return instance;
	}
	public boolean addAll(Collection<String> c) {
		restrictor(c.size());
		return wholeTitles.addAll(c);
	}
	public void printAll() {
		wholeTitles.forEach(System.out::println);
	}
	public boolean contains(String str) {
		return wholeTitles.contains(str);
	}
	private void restrictor(int amount) {
		if(wholeTitles.size() + amount >= 1000)
			for(int i = 0; i < 1000 - wholeTitles.size() + amount; i++)
				wholeTitles.removeFirst();
	}
}
