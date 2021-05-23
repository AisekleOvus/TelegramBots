package gmd;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class GatherMarketData {
    private static final String BASE_FNM_URL = "https://www.finam.ru/";
    private static final String BASE_MW_URL = "https://www.marketwatch.com/";
    private static final String BASE_FX_URL = "https://www.fx.co/";
    
    public static void main(String[] args) {
    	LocalTime timerStart = LocalTime.now();
    	WebDriver driver = HeadlessChromeInit.initChromeDriver("1920,1200", "./");
    	String partOTWorld = args.length !=0 ? args[0] : "Europe";
    	try {
        	gatherAll(driver,
        			"https://markets.money.cnn.com/worldmarkets/map.asp?region=" + partOTWorld,
        			partOTWorld);
        	LocalTime timerFinish = LocalTime.now();
        	System.out.println( timerStart.until(timerFinish, ChronoUnit.SECONDS)+ " сек."); 
    	}catch(Exception e) { 
    		driver.quit();
    		e.printStackTrace(); 
    	}
    }
    private static void gatherAll(WebDriver driver, String mapUrl, String partOTWorld) throws Exception{
        String startHtml = "<style>#wrapper { display:block; width:615px; } table.wsod_dataTable td { padding: 5px 0px 5px 0px; } .wsod_dataTable td { border-bottom: 1px solid #e7e7e7; color: #333; } .wsodContent td { vertical-align: middle; } td { display: table-cell; vertical-align: inherit; text-align: right; } table { border-collapse: collapse; border-spacing: 0; border-collapse: separate; white-space: normal; line-height: normal; font-family: Arial, Helvetica, sans-serif; font-weight: bold; font-size: 20px; font-style: normal; color: -internal-quirk-inherit; text-align: start; border-spacing: 2px; font-variant: normal; } </style> <div id=\"wrapper\"> <div id=\"forImage\"> <img src = \"WorldMap.png\" /> </div> <div id=\"infoBlock\"> <table id=\"wsod_indexDataTableGrid\" class=\"wsod_dataTable wsod_dataTableBig\" cellspacing=\"0\"> <tbody> <tr height=\"5\"> <th width=\"115\"></th> <th width=\"250\"></th> <th width=\"150\"></th> <th width=\"100\"></th></tr>";
    	String happyEnd = "</tr></tbody></table></div></div>";
    	Map<String, String> dataByCountriesMap = new LinkedHashMap<>();
    	Map<String, String[]> mapIndicies = gatherMapIndicies(driver, mapUrl);
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
    	
    	try(BufferedWriter bwr = new BufferedWriter(new FileWriter("blueprint.html"))) {
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
    	driver.get(Paths.get("./blueprint.html").toUri().toString());
    	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.xpath("/html/body/div")));
    	ImageIO.write(screenshot.getImage(), "PNG", new File("./" + partOTWorld + ".png"));
    	driver.quit();
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
    	
    	if(mapIndicies.containsKey("Shanghai Composite")) {
    		System.out.println("Get Shanghai index from mapIndicies");
    		gcMap.put("SHANGHAI",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("Shanghai Composite")[0]	+ "</td><td" + paintIt(mapIndicies.get("Shanghai Composite")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
        	gcMap.put("SHANGHAI",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("Hang Seng")) {
    		System.out.println("Get Hang Seng index from mapIndicies");
    		gcMap.put("HANGSENG",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("Hang Seng")[0]	+ "</td><td" + paintIt(mapIndicies.get("Hang Seng")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
        	gcMap.put("HANGSENG",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Hang Seng</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("Nikkei")) {
    		System.out.println("Get Nikkei index from mapIndicies");
    		gcMap.put("NIKKEI",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("Nikkei")[0]	+ "</td><td" + paintIt(mapIndicies.get("Nikkei")[1]) + "</td></tr>");
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
    	
    	if(mapIndicies.containsKey("DAX")) {
    		System.out.println("Get DAX index from mapIndicies");
    		gcMap.put("DAX",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("DAX")[0]	+ "</td><td" + paintIt(mapIndicies.get("DAX")[1]) + "</td></tr>");
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
    	if(mapIndicies.containsKey("CAC 40")) {
    		System.out.println("Get CAC 40 index from mapIndicies");
    		gcMap.put("CAC 40",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("CAC 40")[0]	+ "</td><td" + paintIt(mapIndicies.get("CAC 40")[1]) + "</td></tr>");
    	}else {
        	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
        	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
        	gcMap.put("CAC 40",
        			"<tr><td style=\"text-align: left\">Франция</td><td>CAC 40</td><td>"
                    + driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","") + ""
        			+ "</td><td" + paintIt(driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","")) + "</td></tr>");
    	}
    	if(mapIndicies.containsKey("FTSE 100")) {
    		System.out.println("Get FTSE 100 index from mapIndicies");
    		gcMap.put("FTSE 100",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("FTSE 100")[0]	+ "</td><td" + paintIt(mapIndicies.get("FTSE 100")[1]) + "</td></tr>");
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
    	if(mapIndicies.containsKey("S&P 500")) {
    		System.out.println("Get S&P 500 index from mapIndicies");
    		gcMap.put("S&P 500",
        			"<tr><td style=\"text-align: left\">Китай</td><td>Shanghai Composite</td><td>"
                     + mapIndicies.get("S&P 500")[0]	+ "</td><td" + paintIt(mapIndicies.get("S&P 500")[1]) + "</td></tr>");
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
    
    private static Map<String, String[]> gatherMapIndicies(WebDriver driver, String mapUrl) {
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
            
//            System.out.println(javaScript);
            execJavaScript.executeScript(javaScript);
            
//            execJavaScript.executeScript("document.querySelector(\"#wsod_index_CNG > div > span > span > span[class$='Data']\").outerHTML=\"<span class='negData'>Hello World</span>\";");

        	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.xpath(mapXpath)));
        	ImageIO.write(screenshot.getImage(), "PNG", new File("./WorldMap.png"));
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	return mapIndicies;
    }
}
