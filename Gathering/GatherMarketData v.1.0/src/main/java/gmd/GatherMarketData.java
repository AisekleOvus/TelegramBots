package gmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.BufferedWriter;
import java.io.FileWriter;

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

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import com.ovus.aisekle.telegrambotwork.TBWork;

public class GatherMarketData {
    private static final String BASE_FNM_URL = "https://www.finam.ru/";
    private static final String BASE_MW_URL = "https://www.marketwatch.com/";
    private static final String BASE_FX_URL = "https://www.fx.co/";
    
    private static String getInstallDir() {
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
    private static Map<String, String[]> getMcdSettings(String installDir) throws Exception{
    	String[] strArr = new String[2];
    	String line = null;
    	Scanner settsScanner = new Scanner(new File(installDir + "mcd.set")).useDelimiter(System.lineSeparator());
    	LinkedHashMap<String, String[]> settsMap = new LinkedHashMap<>();
    	while(settsScanner.hasNext()) {
    		line = settsScanner.next();
    		strArr = line.split("  ");
   		    settsMap.put(strArr[0], strArr[1].split("=="));
    		
    	}
    	settsMap.forEach((k,v) -> System.out.println(k + " " + v[0] + "==" + v[1]));
        return settsMap;
    }
    public static void main(String[] args) {
    	String installDir = getInstallDir();
    	LocalTime timerStart = LocalTime.now();
    	WebDriver driver = HeadlessChromeInit.initChromeDriver("1920,1200", installDir);
    	String partOTWorld = args.length !=0 ? args[0] : "Europe";
    	try {
        	gatherAll(driver,
        			"https://markets.money.cnn.com/worldmarkets/map.asp?region=" + partOTWorld,
        			partOTWorld, installDir);
        	LocalTime timerFinish = LocalTime.now();
        	System.out.println( timerStart.until(timerFinish, ChronoUnit.SECONDS)+ " сек."); 
    	}catch(Exception e) { 
    		driver.quit();
    		e.printStackTrace(); 
    	}
    }
    private static void gatherAll(WebDriver driver, String mapUrl, String partOTWorld, String dir) throws Exception{
        String startHtml = "<style>#wrapper { display:block; width:615px; } table.wsod_dataTable td { padding: 5px 0px 5px 0px; } .wsod_dataTable td { border-bottom: 1px solid #e7e7e7; color: #333; } .wsodContent td { vertical-align: middle; } td { display: table-cell; vertical-align: inherit; text-align: right; } table { border-collapse: collapse; border-spacing: 0; border-collapse: separate; white-space: normal; line-height: normal; font-family: Arial, Helvetica, sans-serif; font-weight: bold; font-size: 20px; font-style: normal; color: -internal-quirk-inherit; text-align: start; border-spacing: 2px; font-variant: normal; } </style> <div id=\"wrapper\"> <div id=\"forImage\"> <img src = \"WorldMap.png\" /> </div> <div id=\"infoBlock\"> <table id=\"wsod_indexDataTableGrid\" class=\"wsod_dataTable wsod_dataTableBig\" cellspacing=\"0\"> <tbody> <tr height=\"5\"> <th width=\"115\"></th> <th width=\"250\"></th> <th width=\"150\"></th> <th width=\"100\"></th></tr>";
    	String happyEnd = "</tr></tbody></table></div></div>";
    	Map<String, String> dataByCountriesMap = new LinkedHashMap<>();
    	Map<String, String[]> mapIndicies = gatherMapIndicies(driver, mapUrl, dir);
    	StringBuilder output = new StringBuilder();

    	switch(partOTWorld) {
    	case("Asia") :
        	dataByCountriesMap = gatherAsia(driver, mapIndicies);
    	    break;
    	case("Europe") :
        	dataByCountriesMap = gatherEurope(driver, mapIndicies);
    	    break;
    	case("Americas") :
    		dataByCountriesMap = gatherAmericas(driver, mapIndicies);
    		break;
    	}
    	//                                                      GATHERING
    	
    	try(BufferedWriter bwr = new BufferedWriter(new FileWriter(dir + "blueprint.html"))) {
    		bwr.write(startHtml);

    		
        	for(Map.Entry<String, String> entry : dataByCountriesMap.entrySet())
        		bwr.write(entry.getValue());
    	
        	for(Map.Entry<String, String> entry : gatherCommon(driver).entrySet())
        		bwr.write(entry.getValue());
    		
    		bwr.write(happyEnd);
    		bwr.flush();
    		
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	String sliceFileName = LocalDate.now().toString() + "_" + LocalTime.now().toString() + "_" + partOTWorld + ".png";
    	driver.get(Paths.get(dir + "blueprint.html").toUri().toString());
    	
    	WebElement img = new WebDriverWait(driver, 3000L)
  	          .until(wdw -> wdw.findElement(By.tagName("img")));
    	
    	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.xpath("/html/body/div")));
    	ImageIO.write(screenshot.getImage(), "PNG", new File(dir + sliceFileName));
    	
    	Thread.sleep(3000L);
    	try {
    		Map<String, String[]> settings = getMcdSettings(dir);
    		String[] telegramDir = settings.get("telegram"); // indx 0 corresponds to telegram dir URI, and indx corresponds to telegram URL
    		LinkedHashMap<Integer, String> timers = new LinkedHashMap<>();
            for(Map.Entry<String, String[]> entry : settings.entrySet()) {
            	if(entry.getKey().startsWith("timer")) {
            	    timers.put(Integer.parseInt(entry.getValue()[0]), entry.getValue()[1]);
            	}
            }
        	Files.copy(Paths.get(dir + sliceFileName), Paths.get(telegramDir[0] + sliceFileName), StandardCopyOption.REPLACE_EXISTING);
        	if(sendSlice2Telegram(telegramDir[1], sliceFileName, dir, timers))
        		deleter(dir, telegramDir[0], sliceFileName);
        	else {
        		System.out.println("Something wrong with sending to Telegram, trying again ..");
        		if(sendSlice2Telegram(telegramDir[1], sliceFileName, dir, timers))
        			deleter(dir, telegramDir[0], sliceFileName);
        	}
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	driver.quit();
    }
    private static void deleter(String dir, String tDir, String sFname) throws Exception {
		Files.deleteIfExists(Paths.get(tDir + sFname));
		Files.deleteIfExists(Paths.get(dir + sFname));
	    Files.deleteIfExists(Paths.get(dir + "WorldMap.png"));
	    Files.deleteIfExists(Paths.get(dir + "blueprint.html"));
    }
    private static String paintIt(String what2paint) {
    	if(what2paint.contains("-")) what2paint=" style=\"color:red\">" + what2paint;
    	else 
    		if(!what2paint.contains("+"))
    		    what2paint=" style=\"color:green\"> +" + what2paint;
    		else
    			what2paint=" style=\"color:green\">" + what2paint;
    	return what2paint;
    }
    
    private static Map<String, String> gatherAsia(WebDriver driver, Map<String, String[]> mapIndicies) {
    	LinkedHashMap<String, String> gcMap = new LinkedHashMap<>();
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	if(mapIndicies.containsKey("wsod_index_CNG")) {
    		System.out.println("Get Shanghai index from mapIndicies");
    		gcMap.put("SHANGHAI",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("wsod_index_CNG")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_CNG")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
        	gcMap.put("SHANGHAI",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("wsod_index_HKH")) {
    		System.out.println("Get Hang Seng index from mapIndicies");
    		gcMap.put("HANGSENG",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Hang Seng</td><td>"
                     + mapIndicies.get("wsod_index_HKH")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_HKH")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
        	gcMap.put("HANGSENG",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Hang Seng</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("wsod_index_JPT")) {
    		System.out.println("Get Nikkei index from mapIndicies");
    		gcMap.put("NIKKEI",
        			"<tr><td style=\"text-align: left\">Япония</td><td>NIKKEI 225</td><td>"
                     + mapIndicies.get("wsod_index_JPT")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_JPT")[1]) + "</td></tr>");
    	}else {
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	gcMap.put("NIKKEI",
    			"<tr><td style=\"text-align: left\">Япония</td><td>NIKKEI 225</td><td>"
                + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
    			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	return gcMap;
    }
    private static Map<String, String> gatherEurope(WebDriver driver, Map<String, String[]> mapIndicies) {
    	LinkedHashMap<String, String> gcMap = new LinkedHashMap<>();
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";  

    	String valueXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[1]/div[3]/div[2]";
    	String changeXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[2]/div[1]/div[1]/div[2]/span[2]";
    	
    	if(mapIndicies.containsKey("wsod_index_DEX")) {
    		System.out.println("Get DAX index from mapIndicies");
    		gcMap.put("DAX",
        			"<tr><td style=\"text-align: left\">Германия</td><td>DAX</td><td>"
                     + mapIndicies.get("wsod_index_DEX")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_DEX")[1]) + "</td></tr>");
    	}else {
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quotes/indices");
    	driver.get(BASE_FX_URL + "ru/quote/dax");
    	try {
    		Thread.sleep(2000L);
    	} catch(Exception e) {}
    	gcMap.put("DAX",
    			"<tr><td style=\"text-align: left\">Германия</td><td>DAX</td><td>"
                + driver.findElement(By.xpath(valueXPathFX)).getText() + ""
    			+ "</td><td" + paintIt(driver.findElement(By.xpath(changeXPathFX)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("wsod_index_FRCF")) {
    		System.out.println("Get CAC 40 index from mapIndicies");
    		gcMap.put("CAC 40",
        			"<tr><td style=\"text-align: left\">Франция</td><td>CAC 40</td><td>"
                     + mapIndicies.get("wsod_index_FRCF")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_FRCF")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
        	gcMap.put("CAC 40",
        			"<tr><td style=\"text-align: left\">Франция</td><td>CAC 40</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("wsod_index_GBE")) {
    		System.out.println("Get FTSE 100 index from mapIndicies");
    		gcMap.put("FTSE 100",
        			"<tr><td style=\"text-align: left\">Великобритания</td><td>FTSE 100</td><td>"
                     + mapIndicies.get("wsod_index_GBE")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_GBE")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
        	gcMap.put("FTSE 100",
        			"<tr><td style=\"text-align: left\">Великобритания</td><td>FTSE 100</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}

    	return gcMap;
    }
    private static Map<String, String> gatherAmericas(WebDriver driver, Map<String, String[]> mapIndicies) {
    	LinkedHashMap<String, String> gcMap = new LinkedHashMap<>();
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	String valueXPathFX = "div[class^='_bid']";
    	String changeXPathFX = "span[class^='_change-percent']";
    	
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/d-j-ind/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/d-j-ind/");
    	gcMap.put("DJIA",
    			"<tr><td style=\"text-align: left\">США</td><td>DJIA</td><td>"
                + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
    			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	if(mapIndicies.containsKey("wsod_index_USN")) {
    		System.out.println("Get S&P 500 index from mapIndicies");
    		gcMap.put("S&P 500",
        			"<tr><td style=\"text-align: left\">США</td><td>S&P 500</td><td>"
                     + mapIndicies.get("wsod_index_USN")[0]	+ "</td><td" + paintIt(mapIndicies.get("wsod_index_USN")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
        	gcMap.put("S&P 500",
        			"<tr><td style=\"text-align: left\">США</td><td>S&P 500</td><td>"
        			+ driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>"); 
    	}
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quote/ndx");
    	driver.get(BASE_FX_URL + "ru/quote/ndx");
    	try {
    		Thread.sleep(2000L);
    	}catch(Exception e) {}
    	gcMap.put("NASDAQ",
    			"<tr><td style=\"text-align: left\">США</td><td>NASDAQ</td><td>"
    			+ driver.findElement(By.cssSelector(valueXPathFX)).getText().replace("USD","") + ""
    			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFX)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	return gcMap;
    }
    private static Map<String, String> gatherCommon(WebDriver driver) {
        LinkedHashMap<String, String> gcMap = new LinkedHashMap<>();
    	
        // Finam.ru
        String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/usd-ind/");
    	driver.get(BASE_FNM_URL + "quote/forex/usd-ind/");
    	gcMap.put("USDX",
    			"<tr><td style=\"text-align: left\">США</td><td>Индекс доллара</td><td>"
    			+ driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","$")
    			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/brent/");
        driver.get(BASE_FNM_URL + "quote/tovary/brent/");
    	gcMap.put("BRENT",
    			"<tr><td style=\"text-align: left\">Товар</td><td>Нефть Brent</td><td>"+ 
    			driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $" + 
    	    	"</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","")) + "</td></tr>");
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/light/");
    	driver.get(BASE_FNM_URL + "quote/tovary/light/");
       	gcMap.put("WTI",
       			"<tr><td style=\"text-align: left\">Товар</td><td>Нефть WTI</td><td>"+ 
       			driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $" + 
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","")) + "</td></tr>");
       	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/gold/");
    	driver.get(BASE_FNM_URL + "quote/tovary/gold/");
       	gcMap.put("GOLD",
       			"<tr><td style=\"text-align: left\">Товар</td><td>Золото</td><td>"+ 
       			driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $" + 
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","")) + "</td></tr>");
       	
       	System.out.println("Try to get " + BASE_MW_URL + "and trying click da link");
    	driver.get(BASE_MW_URL);
    	driver.findElement(By.cssSelector(".close-btn")).click();                                              // PayWall Close
    	driver.findElement(By.cssSelector(".gdpr-close")).click();                                              // Cookie warning Close
    	driver.findElement(By.cssSelector("a[href='https://www.marketwatch.com/investing/bonds']")).click();
    	String tsyMWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMUBMUSD10Y'] td.price";
    	String tsyMWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMUBMUSD10Y'] td.change";
    	String bundMWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKDE-10Y'] td.price";
    	String bundMWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKDE-10Y'] td.change";
    	String jp10MWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKJP-10Y'] td.price";
    	String jp10MWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKJP-10Y'] td.change";
       	gcMap.put("TSY",
       			"<tr><td style=\"text-align: left\">Облигации</td><td>США 10 лет</td><td>"+ 
       			driver.findElement(By.cssSelector(tsyMWCSSselectorValue)).getText() + 
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(tsyMWCSSselectorChange)).getText()) + "</td></tr>");
       	gcMap.put("BUNDES",
       			"<tr><td style=\"text-align: left\">Облигации</td><td>Германия 10 лет</td><td>"+ 
       			driver.findElement(By.cssSelector(bundMWCSSselectorValue)).getText() + 
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(bundMWCSSselectorChange)).getText()) + "</td></tr>");
       	gcMap.put("JP10",
       			"<tr><td style=\"text-align: left\">Облигации</td><td>Япония 10 лет</td><td>"+ 
       			driver.findElement(By.cssSelector(jp10MWCSSselectorValue)).getText() + 
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(jp10MWCSSselectorChange)).getText()) + "</td></tr>");
       	
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/usd-rub/");
    	driver.get(BASE_FNM_URL + "quote/forex/usd-rub/");
    	gcMap.put("USDRUB",
    			"<tr><td style=\"text-align: left\">Россия</td><td>USD/RUB</td><td>"+ 
    			driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("RUB","").replace(" ","") +  " ₽" +
    			"</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","")) + "</td></tr>");
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/eur-rub/");
    	driver.get(BASE_FNM_URL + "quote/forex/eur-rub/");
    	gcMap.put("EURRUB",
    			"<tr><td style=\"text-align: left\">Россия</td><td>EUR/RUB</td><td>"+ 
    			driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("RUB","").replace(" ","") + " ₽" +
    	    	"</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","")) + "</td></tr>");
    	return gcMap;
    }
    
    private static Map<String, String[]> gatherMapIndicies(WebDriver driver, String mapUrl, String dir) {
        LinkedHashMap<String, String[]> mapIndicies = new LinkedHashMap<>();
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	String valueXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[1]/div[3]/div[2]";
    	String changeXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[2]/div[1]/div[1]/div[2]/span[2]";
        
        // S&P 500 INDEX        
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
    	mapIndicies.put("wsod_index_USN", new String[]{driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "", driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
    	
    	// FTSE 100 INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
    	mapIndicies.put("wsod_index_GBE", new String[] {driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "", driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
        
    	// DAX INDEX
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quotes/indices");
    	driver.get(BASE_FX_URL + "ru/quote/dax");
    	try {
    		Thread.sleep(2000L);
    	} catch(Exception e) {}
    	mapIndicies.put("wsod_index_DEX", new String[] {driver.findElement(By.xpath(valueXPathFX)).getText() + "",driver.findElement(By.xpath(changeXPathFX)).getText().replaceAll("[\\(\\)]","")});
    	
    	// CAC 40 INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
    	mapIndicies.put("wsod_index_FRCF", new String[] {driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "", driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
    	
    	// Shanghai Composite INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
    	mapIndicies.put("wsod_index_CNG", new String[] {driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "", driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
    	
    	// Hang Seng INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
    	mapIndicies.put("wsod_index_HKH", new String[] {driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "",  driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
    	
    	// Nikkei INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	mapIndicies.put("wsod_index_JPT", new String[] { driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + "", driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")});
    	
    	
    	
    	try {
            //Make map market screenshot
        	String mapXpath = "//*[@id=\"wsod_canvas\"]";
            driver.get(mapUrl);
            
            
            JavascriptExecutor execJavaScript = (JavascriptExecutor) driver;
            String javaScript = "";
            for(Map.Entry<String, String[]> entry : mapIndicies.entrySet())
            	javaScript += "document.querySelector(\"#" + entry.getKey() + " > div > span > span > span[class$='Data']\").outerHTML=\"<span class="
                        + ( entry.getValue()[1].contains("-") ? "'negData'>" + entry.getValue()[1] + "</span>" 
                        		: "'posData'>" + entry.getValue()[1] ) + "</span>\";";
            

            execJavaScript.executeScript(javaScript);

            Thread.sleep(3000L);
            
        	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.xpath(mapXpath)));
        	ImageIO.write(screenshot.getImage(), "PNG", new File(dir + "WorldMap.png"));
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return mapIndicies;
    }
    
    private static boolean sendSlice2Telegram(String urlT, String fileName, String parentDir, Map<Integer, String> timers){ 
    	final String TOKEN = "1163692560:AAE3MtmyMzsaJRPFKbXJdJhnqcBlC5k_1GQ";
    	final String CHATID = "@Toro_dOro";
    	try {
    		Integer hourlyCaption = LocalTime.now(ZoneId.of("Europe/Moscow")).getHour();
    		String htagDate = "#"+LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(new Locale("ru"))).replace(" ", "");
    		
            TBWork tbw = new TBWork(TOKEN, CHATID);    	    
       		String caption = htagDate + " #Рынки " + ( timers.containsKey(hourlyCaption) ? timers.get(hourlyCaption) : "");
            String photoUrl = urlT + fileName;
            System.out.println(photoUrl);
            String result = null;
            if((result = tbw.sendPhoto("&photo=" + photoUrl + "&parse_mode=MarkdownV2&caption=" + caption + "&disable_notification=false")) != null)
                    Glossary.Update(TOKEN, CHATID, parentDir, 176, 3, 2);
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		return false;
    	}

    return true;
    }

}
