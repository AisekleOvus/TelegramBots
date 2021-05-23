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

import java.util.concurrent.*;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.util.Elements;

import java.util.logging.LogManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GatherMarketData {
    private static final String BASE_FNM_URL = "https://www.finam.ru/";
    private static final String BASE_MW_URL = "https://www.marketwatch.com/";
    private static final String BASE_FX_URL = "https://www.fx.co/";
    private static final String BASE_SNP_URL = "https://www.spglobal.com/spdji/en/indices/";
    private static final String BASE_ENX_URL = "https://live.euronext.com/en/";
    private static ExecutorService executor;
    private static Logger log;
    
    
    private static HashMap<String, String> quiproquoColor = new HashMap<>();
    private static HashMap<String, String> quiproquoIValues = new HashMap<>();
    private static HashMap<String, String> quiproquoIChanges = new HashMap<>();
    


    public static void main(String[] args) {
    	try {
    	    LogManager.getLogManager().readConfiguration(GatherMarketData.class.getResourceAsStream("/logging.properties"));
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	log = Logger.getLogger("GMD");
    	log.logp(Level.WARNING, "GatherMarketData", "Main", "Logger is ready");
    	executor = Executors.newSingleThreadExecutor();
    	String installDir = Setup.getInstallDir();
    	LocalTime timerStart = LocalTime.now();
    	WebDriver driver = HeadlessChromeInit.initChromeDriver("1920,1200", installDir);
    	String partOTWorld = args.length !=0 ? args[0] : "Europe";
    	try {
        	gatherAll(driver,partOTWorld, installDir);
        	LocalTime timerFinish = LocalTime.now();
        	System.out.println( timerStart.until(timerFinish, ChronoUnit.SECONDS)+ " сек."); 
    	}catch(Exception e) { 
    		driver.quit();
    		e.printStackTrace(); 
    	}
    }
    private static void gatherAll(WebDriver driver, String partOTWorld, String dir) throws Exception{

    	switch(partOTWorld) {
    	case("Asia") :
        	if(gatherAsia(driver)) {
        		executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
        		gatherCrude(driver);
        		gatherMetalls(driver);
        		gatherBonds(driver);
        		gatherCurrencies(driver);
        	}
    	    break;
    	case("Europe") :
    	    if(gatherEurope(driver)) {
    	    	executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
        		gatherCrude(driver);
        		gatherMetalls(driver);
        		gatherBonds(driver);
        		gatherCurrencies(driver);
    	    }
    	    break;
    	case("Americas") :
	        if(gatherAmericas(driver)) {
	        	executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
        		gatherCrude(driver);
        		gatherMetalls(driver);
        		gatherBonds(driver);
        		gatherCurrencies(driver);
	        }
    	    break;
    	
	    case("Asia-only") :
    	    if(gatherAsia(driver)) {
    		    executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
    	    }
	        break;
	    case("Europe-only") :
	        if(gatherEurope(driver)) {
	    	    executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
	        }
	        break;
	    case("Americas-only") :
            if(gatherAmericas(driver)) {
        	    executor.execute(new MapModifier(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges));
            }
	        break;
	    case("Crude-only") :
    		gatherCrude(driver);
	        break;
	    case("Metalls-only") :
       		gatherMetalls(driver);
	        break;
	    case("Bonds-only") :
    		gatherBonds(driver);
    	    break;
	    case("Currencies-only") :
    		gatherCurrencies(driver);
    	    break;	   
    	}
    	driver.quit();
    }
    
    public static boolean gatherAsia(WebDriver driver) {
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";

    	// SHANGHAI COMPISITE
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/shanghai-composite/");
        String shaValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
        shaValue = shaValue.format("%.2f", Double.parseDouble(shaValue));
    	String shaChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.SHANGHAI, shaValue);
    	quiproquoIChanges.put(IndiciesChages.SHANGHAI, shaChange);
    	if(shaChange.contains("-"))
    		quiproquoColor.put(CountryColors.CHINA, "red");
    	else
    		quiproquoColor.put(CountryColors.CHINA, "green");

    	// HANG-SENG
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/hang-seng/");
    	String hasValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String hasChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.HANGSENG, hasValue);
    	quiproquoIChanges.put(IndiciesChages.HANGSENG, hasChange);

    	
    	// NIKKEI 225
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/n225jap/");
    	String nikValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String nikChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.NIKKEI, nikValue);
    	quiproquoIChanges.put(IndiciesChages.NIKKEI, nikChange);
    	if(nikChange.contains("-"))
    		quiproquoColor.put(CountryColors.JAPAN, "red");
    	else
    		quiproquoColor.put(CountryColors.JAPAN, "green");

    	// BSE Sensex
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/bse-sensex/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/bse-sensex/");
    	String bseValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String bseChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.BSESEN, bseValue);
    	quiproquoIChanges.put(IndiciesChages.BSESEN, bseChange);
    	if(bseChange.contains("-"))
    		quiproquoColor.put(CountryColors.INDIA, "red");
    	else
    		quiproquoColor.put(CountryColors.INDIA, "green");

    	// KOSPI
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/kospi/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/kospi/");
    	String kosValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String kosChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.KOSPI, kosValue);
    	quiproquoIChanges.put(IndiciesChages.KOSPI, kosChange);
    	if(kosChange.contains("-"))
    		quiproquoColor.put(CountryColors.KOREA, "red");
    	else
    		quiproquoColor.put(CountryColors.KOREA, "green");
    	
    	// ASX 200
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/asx200-australia/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/asx200-australia/");
    	String asxValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String asxChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.ASX, asxValue);
    	quiproquoIChanges.put(IndiciesChages.ASX, asxChange);
    	if(asxChange.contains("-"))
    		quiproquoColor.put(CountryColors.AUSTRALIA, "red");
    	else
    		quiproquoColor.put(CountryColors.AUSTRALIA, "green");
    	
    	return true;
    }
    public static boolean gatherEurope(WebDriver driver) {
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";  

    	String valueXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[1]/div[3]/div[2]";
    	String changeXPathFX = "/html/body/main/div[1]/div[2]/div/div[3]/div[2]/div[1]/div[1]/div[2]/span[2]";
    	
//      DAX GERMANY
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quote/dax");
    	driver.get(BASE_FX_URL + "ru/quote/dax");
    	try {
    		Thread.sleep(2000L);
    	} catch(Exception e) {}
    	String daxValue =  driver.findElement(By.xpath(valueXPathFX)).getText();
    	String daxChange = driver.findElement(By.xpath(changeXPathFX)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.DAX, daxValue);
    	quiproquoIChanges.put(IndiciesChages.DAX, daxChange);
    	if(daxChange.contains("-"))
    		quiproquoColor.put(CountryColors.GERMANY, "red");
    	else
    		quiproquoColor.put(CountryColors.GERMANY, "green");
    	
//      SMI SWITZERLAND
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quote/smi");
    	driver.get(BASE_FX_URL + "ru/quote/smi");
    	try {
    		Thread.sleep(2000L);
    	} catch(Exception e) {}
    	String smiValue =  driver.findElement(By.xpath(valueXPathFX)).getText();
    	String smiChange = driver.findElement(By.xpath(changeXPathFX)).getText().replaceAll("[\\(\\)]","");
/*    	quiproquoIValues.put(IndiciesValues.SMI, smiValue);
    	quiproquoIChanges.put(IndiciesChages.DAX, smiChange);*/
    	if(smiChange.contains("-"))
    		quiproquoColor.put(CountryColors.SWITZERLAND, "red");
    	else
    		quiproquoColor.put(CountryColors.SWITZERLAND, "green");
    	
/*
//      CAC40 FRANCE
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/cac-40/");
    	String cacValue =  driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","");
    	String cacChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	cacValue = cacValue.format("%.2f", Double.parseDouble(cacValue.replace(" ", "").replace(",", ".")));
    	quiproquoIValues.put(IndiciesValues.CAC, cacValue);
    	quiproquoIChanges.put(IndiciesChages.CAC, cacChange);
    	if(cacChange.contains("-"))
    		quiproquoColor.put(CountryColors.FRANCE, "red");
    	else
    		quiproquoColor.put(CountryColors.FRANCE, "green");*/
    	
//      FTSE100 GREAT BRITAIN
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/futsee-100/");
    	String ftsValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String ftsChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.FTSE, ftsValue);
    	quiproquoIChanges.put(IndiciesChages.FTSE, ftsChange);
    	if(ftsChange.contains("-"))
    		quiproquoColor.put(CountryColors.BRITAIN, "red");
    	else
    		quiproquoColor.put(CountryColors.BRITAIN, "green");
    	
//      IMOEX RUSSIA
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/micex/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/micex/");
    	String imoValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("RUB","").replace(" ","").replace(",",".");
    	String imoChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.IMOEX, imoValue);
    	quiproquoIChanges.put(IndiciesChages.IMOEX, imoChange);
    	if(imoChange.contains("-"))
    		quiproquoColor.put(CountryColors.RUSSIA, "red");
    	else
    		quiproquoColor.put(CountryColors.RUSSIA, "green");
    	
//      IBEX 35  && FTSE MIB
       	System.out.println("Try to get " + BASE_MW_URL + "and trying click da link");
    	driver.get(BASE_MW_URL);
    	List<WebElement> paywallCloser = driver.findElements(By.cssSelector(".close-btn"));                                              // PayWall Close
    	List<WebElement> cookieCloser = driver.findElements(By.cssSelector(".gdpr-close"));                                             // Cookie warning Close
    	if(paywallCloser.size() != 0) paywallCloser.get(0).click();
    	if(cookieCloser.size() != 0) cookieCloser.get(0).click();
    	try {
    	driver.findElement(By.cssSelector("a[href='https://www.marketwatch.com/markets/europe-middle-east']")).click();
    	} catch (org.openqa.selenium.ElementClickInterceptedException ecie) {
        	driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[id^='sp_message_iframe']")));
        	driver.findElement(By.cssSelector("button[title='ACCEPT COOKIES']")).click();
        	driver.switchTo().defaultContent();
    		driver.findElement(By.cssSelector("a[href='https://www.marketwatch.com/markets/europe-middle-east']")).click();
    		
    	}
    	String ibeMWCSSselectorValue = "tr[data-charting-symbol='INDEX/XX/XMAD/IBEX'] td.price";
    	String ibeMWCSSselectorChange = "tr[data-charting-symbol='INDEX/XX/XMAD/IBEX'] td.change";
    	String mibMWCSSselectorValue = "tr[data-charting-symbol='INDEX/IT/MTAA/I945'] td.price";
    	String mibMWCSSselectorChange = "tr[data-charting-symbol='INDEX/IT/MTAA/I945'] td.change";
       	String ibeValue = driver.findElement(By.cssSelector(ibeMWCSSselectorValue)).getText(); 
    	String ibeChange = driver.findElement(By.cssSelector(ibeMWCSSselectorChange)).getText();
       	String mibValue = driver.findElement(By.cssSelector(mibMWCSSselectorValue)).getText(); 
    	String mibChange = driver.findElement(By.cssSelector(mibMWCSSselectorChange)).getText();
/*    	quiproquoIValues.put(IndiciesValues.IBEX, ibeValue);
    	quiproquoIChanges.put(IndiciesChages.IBEX, ibeChange);
    	quiproquoIValues.put(IndiciesValues.FTSEMIB, mibValue);
    	quiproquoIChanges.put(IndiciesChages.FTSEMIB, mibChange);*/
    	
    	if(ibeChange.contains("-"))
    		quiproquoColor.put(CountryColors.SPAIN, "red");
    	else
    		quiproquoColor.put(CountryColors.SPAIN, "green");
    	
    	if(mibChange.contains("-"))
    		quiproquoColor.put(CountryColors.ITALY, "red");
    	else
    		quiproquoColor.put(CountryColors.ITALY, "green");
    	
//      NASDAQ OMX NORDIC 40
    	System.out.println("Try to get " + BASE_MW_URL + "investing/index/omxn40?countrycode=xx");
    	driver.get(BASE_MW_URL + "investing/index/omxn40?countrycode=xx");
    	
    	/* SCREENSHOOTING 
    	 File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    	 try{ 
    		 Files.copy(screenshot.toPath(), new File("/home/torodioro/AutoUpdater/nasdaq.png").toPath(), StandardCopyOption.REPLACE_EXISTING);
    	 } catch (Exception e) { e.printStackTrace();}*/
    	
    	String omxValue = driver.findElement(By.cssSelector("h3[class*='intraday__price']")).getText().replace(",","");
    	String omxChange = driver.findElement(By.cssSelector("span[class*='change--percent--q']")).getText();
/*    	quiproquoIValues.put(IndiciesValues.OMX, omxValue);
    	quiproquoIChanges.put(IndiciesChages.OMX, omxChange);*/
    	if(omxChange.contains("-")) {
        	quiproquoColor.put(CountryColors.FINLAND, "red");
        	quiproquoColor.put(CountryColors.SWEDEN, "red");
        	quiproquoColor.put(CountryColors.DENMARK, "red");
        	quiproquoColor.put(CountryColors.ICELAND, "red");
    	} else {
        	quiproquoColor.put(CountryColors.FINLAND, "green");
        	quiproquoColor.put(CountryColors.SWEDEN, "green");
        	quiproquoColor.put(CountryColors.DENMARK, "green");
        	quiproquoColor.put(CountryColors.ICELAND, "green");
    	}
//      OMX OSLO 20
    	System.out.println("Try to get " + BASE_MW_URL + "investing/index/omxo20pi?countrycode=xx");
    	driver.get(BASE_MW_URL + "investing/index/omxo20pi?countrycode=xx");
    	    	
    	String omoValue = driver.findElement(By.cssSelector("h3[class*='intraday__price']")).getText().replace(",","");
    	String omoChange = driver.findElement(By.cssSelector("span[class*='change--percent--q']")).getText();
/*    	quiproquoIValues.put(IndiciesValues.OMXOSLO, omoValue);
    	quiproquoIChanges.put(IndiciesChages.OMOSLO, omoChange);*/
    	if(omxChange.contains("-")) {
        	quiproquoColor.put(CountryColors.NORWAY, "red");
    	} else {
        	quiproquoColor.put(CountryColors.NORWAY, "green");
    	}
    	
//      AEX BEL20 CAC40 ISEQ20 PSI 20 
    	System.out.println("Try to get https://live.euronext.com/en/");
    	driver.get(BASE_ENX_URL);
    	String aexValue = driver.findElement(By.cssSelector("tr[id='NL0000000107.XAMS'] td[name='lastPrice']")).getText().replace(",","");
    	String aexChange = driver.findElement(By.cssSelector("tr[id='NL0000000107.XAMS'] td[name='dayChangeRelative']")).getText();    	   
    	String belValue = driver.findElement(By.cssSelector("tr[id='BE0389555039.XBRU'] td[name='lastPrice']")).getText().replace(",","");
    	String belChange = driver.findElement(By.cssSelector("tr[id='BE0389555039.XBRU'] td[name='dayChangeRelative']")).getText();
    	String cacValue = driver.findElement(By.cssSelector("tr[id='FR0003500008.XPAR'] td[name='lastPrice']")).getText().replace(",","");
    	String cacChange = driver.findElement(By.cssSelector("tr[id='FR0003500008.XPAR'] td[name='dayChangeRelative']")).getText(); 
    	String iseValue = driver.findElement(By.cssSelector("tr[id='IE00B0500264.XDUB'] td[name='lastPrice']")).getText().replace(",","");
    	String iseChange = driver.findElement(By.cssSelector("tr[id='IE00B0500264.XDUB'] td[name='dayChangeRelative']")).getText(); 
    	String psiValue = driver.findElement(By.cssSelector("tr[id='PTING0200002.XLIS'] td[name='lastPrice']")).getText().replace(",","");
    	String psiChange = driver.findElement(By.cssSelector("tr[id='PTING0200002.XLIS'] td[name='dayChangeRelative']")).getText();
/*
    	quiproquoIValues.put(IndiciesValues.AEX, aexValue);
    	quiproquoIChanges.put(IndiciesChages.AEX, aexChange);

    	quiproquoIValues.put(IndiciesValues.BEL20, belValue);
    	quiproquoIChanges.put(IndiciesChages.BEL20, belChange);
*/
     	quiproquoIValues.put(IndiciesValues.CAC, cacValue);
    	quiproquoIChanges.put(IndiciesChages.CAC, cacChange);
/*
     	quiproquoIValues.put(IndiciesValues.ISEQ20, iseValue);
    	quiproquoIChanges.put(IndiciesChages.ISEQ20, iseChange);
    	
    	quiproquoIValues.put(IndiciesValues.PSI20, psiValue);
    	quiproquoIChanges.put(IndiciesChages.PSI20, psiChange);

*/
    	if(aexChange.contains("-")) 
        	quiproquoColor.put(CountryColors.HOLLAND, "red");
    	else 
        	quiproquoColor.put(CountryColors.HOLLAND, "green");
    	
    	if(belChange.contains("-"))
        	quiproquoColor.put(CountryColors.BELGIUM, "red");
    	else
        	quiproquoColor.put(CountryColors.BELGIUM, "green");
    
    	if(cacChange.contains("-"))
    		quiproquoColor.put(CountryColors.FRANCE, "red");
    	else
    		quiproquoColor.put(CountryColors.FRANCE, "green");

    	if(iseChange.contains("-"))
        	quiproquoColor.put(CountryColors.IRLAND, "red");
    	else
        	quiproquoColor.put(CountryColors.IRLAND, "green");
    	
    	if(psiChange.contains("-"))
        	quiproquoColor.put(CountryColors.PORTUGAL, "red");
    	else
        	quiproquoColor.put(CountryColors.PORTUGAL, "green");

//      ATX
    	System.out.println("Try to get https://www.wienerborse.at/en/indices/index-values/overview/?ISIN=AT0000999982&ID_NOTATION=92866");
    	driver.get("https://www.wienerborse.at/en/indices/index-values/overview/?ISIN=AT0000999982&ID_NOTATION=92866");
    	    	
    	String atxValue = driver.findElement(By.cssSelector("#c6919-module-container > table > tbody > tr > td.multi-cell.large-font > span:nth-child(2)")).getText().replace(",","");
    	String atxChange = driver.findElement(By.cssSelector("#c6919-module-container > table > tbody > tr > td:nth-child(2) > span")).getText();
/*    	quiproquoIValues.put(IndiciesValues.ATX, atxValue);
    	quiproquoIChanges.put(IndiciesChages.ATX, atxChange);*/
    	if(atxChange.contains("-")) {
        	quiproquoColor.put(CountryColors.AUSTRIA, "red");
    	} else {
        	quiproquoColor.put(CountryColors.AUSTRIA, "green");
    	}
//      PX
    	System.out.println("Try to get https://www.pse.cz/en/indices/index-values/detail/XC0009698371");
    	driver.get("https://www.pse.cz/en/indices/index-values/detail/XC0009698371");
    	    	
    	String pxValue = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/div[2]/div[2]/div[2]")).getText().replace(",","");
    	String pxChange = driver.findElement(By.xpath("/html/body/div[7]/div/div[2]/div/div[3]/div[2]/div[2]")).getText();
/*    	quiproquoIValues.put(IndiciesValues.PX, pxValue);
    	quiproquoIChanges.put(IndiciesChages.PX, pxChange);*/
    	if(pxChange.contains("-")) {
        	quiproquoColor.put(CountryColors.CZECHIA, "red");
    	} else {
        	quiproquoColor.put(CountryColors.CZECHIA, "green");
    	}
    	
//      WIG30
    	System.out.println("Try to get https://stooq.com/q/?s=wig30");
    	driver.get("https://stooq.com/q/?s=wig30");
    	    	
    	String wigValue = driver.findElement(By.cssSelector("#aq_wig30_c2")).getText().replace(",","");
    	String wigChange = driver.findElement(By.cssSelector("#aq_wig30_m3")).getText().replaceAll("[\\(\\)]","");
/*    	quiproquoIValues.put(IndiciesValues.WIG, wigValue);
    	quiproquoIChanges.put(IndiciesChages.WIG, wigChange);*/
    	if(wigChange.contains("-")) {
        	quiproquoColor.put(CountryColors.POLAND, "red");
    	} else {
        	quiproquoColor.put(CountryColors.POLAND, "green");
    	}
    	return true;
    }
    public static boolean gatherAmericas(WebDriver driver) {
    	String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	String valueXPathFX = "div[class^='_bid']";
    	String changeXPathFX = "span[class^='_change-percent']";
    	
    	String valueXPathSP = "span[class='published-value']";
    	String changeXPathSP = "label[class*='daily-change']";

    	// DOW JONES INDASTRIAL AVERAGE
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/d-j-ind/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/d-j-ind/");
    	String djiValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String djiChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.DJIA, djiValue);
    	quiproquoIChanges.put(IndiciesChages.DJIA, djiChange);
    	
    	// S&P500
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
    	driver.get(BASE_FNM_URL + "quote/mirovye-indeksy/sandp-500/");
    	String snpValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String snpChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.SNP, snpValue);
    	quiproquoIChanges.put(IndiciesChages.SNP, snpChange);
    	if(snpChange.contains("-"))
    		quiproquoColor.put(CountryColors.USA, "red");
    	else
    		quiproquoColor.put(CountryColors.USA, "green");

    	// NASDAQ
    	System.out.println("Try to get " + BASE_FX_URL + "ru/quote/ndx");
    	driver.get(BASE_FX_URL + "ru/quote/ndx");
    	try {
    		Thread.sleep(2000L);
    	}catch(Exception e) {}
    	String nsdValue = driver.findElement(By.cssSelector(valueXPathFX)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String nsdChange = driver.findElement(By.cssSelector(changeXPathFX)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.NASDAQ, nsdValue);
    	quiproquoIChanges.put(IndiciesChages.NASDAQ, nsdChange);

    	// S&P / BMV
    	System.out.println("Try to get " + BASE_SNP_URL + "equity/sp-bmv-ipc/#overview");
    	driver.get(BASE_SNP_URL + "equity/sp-bmv-ipc/#overview");
    	try {
    		Thread.sleep(2000L);
    	}catch(Exception e) {}
    	String bmvValue = driver.findElement(By.cssSelector(valueXPathSP)).getText().replace("USD","").replace(" ","").replace(",",".");
    	String bmvChange = driver.findElement(By.cssSelector(changeXPathSP)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.SNPBMV, bmvValue);
    	quiproquoIChanges.put(IndiciesChages.SNPBMV, bmvChange);
    	if(bmvChange.contains("-"))
    		quiproquoColor.put(CountryColors.MEXICO, "red");
    	else
    		quiproquoColor.put(CountryColors.MEXICO, "green");
    	
    	// S&P / TSX
    	System.out.println("Try to get " + BASE_SNP_URL + "equity/sp-tsx-60-index/#overview");
    	driver.get(BASE_SNP_URL + "equity/sp-tsx-60-index/#overview");
    	try {
    		Thread.sleep(2000L);
    	}catch(Exception e) {}
    	String tsxValue = driver.findElement(By.cssSelector(valueXPathSP)).getText().replace("USD","").replace(",","");
    	String tsxChange = driver.findElement(By.cssSelector(changeXPathSP)).getText().replaceAll("[\\(\\)]","");
    	quiproquoIValues.put(IndiciesValues.SNPTSX, tsxValue);
    	quiproquoIChanges.put(IndiciesChages.SNPTSX, tsxChange);
    	if(tsxChange.contains("-"))
    		quiproquoColor.put(CountryColors.CANADA, "red");
    	else
    		quiproquoColor.put(CountryColors.CANADA, "green");
    	
    	return true;
    }
    public static boolean gatherCrude(WebDriver driver) {
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, String> changes = new HashMap<>();
    	
        // Finam.ru
        String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
/*
    	// USD INDEX
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/usd-ind/");
    	driver.get(BASE_FNM_URL + "quote/forex/usd-ind/");
    	String usdValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("USD","$")
    	String usdChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\(\\)]","");
    	values.put("usdValue", usdValue);
    	changes.put("usdChang", usdChange);*/
    	
    	// CRUDE BRENT
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/brent/");
        driver.get(BASE_FNM_URL + "quote/tovary/brent/");
    	String breValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $"; 
    	String breChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	values.put("breValue", breValue);
    	changes.put("breChang", breChange);
    	
    	// CRUDE WTI
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/light/");
    	driver.get(BASE_FNM_URL + "quote/tovary/light/");
       	String wtiValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $"; 
    	String wtiChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	values.put("wtiValue", wtiValue);
    	changes.put("wtiChang", wtiChange);
    	System.out.println("Execute \"MapModifier\" for \"Crude");
    	executor.execute(new MapModifier("Crude", quiproquoColor, values, changes));
    	return true;
    }
    public static boolean gatherMetalls(WebDriver driver) {
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, String> changes = new HashMap<>();  
        
        // Finam.ru
        String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	
    	// GOLD
       	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/gold/");
    	driver.get(BASE_FNM_URL + "quote/tovary/gold/");
       	String golValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $"; 
    	String golChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	values.put("golValue", golValue);
    	changes.put("golChang", golChange);
    	
    	// COPPER
       	System.out.println("Try to get " + BASE_FNM_URL + "quote/tovary/copper/");
    	driver.get(BASE_FNM_URL + "quote/tovary/copper/");
       	String copValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText() + " $"; 
    	String copChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	values.put("copValue", copValue);
    	changes.put("copChang", copChange);
    	System.out.println("Execute \"MapModifier\" for \"Metalls");
    	executor.execute(new MapModifier("Metalls", quiproquoColor, values, changes));
    return true;
    }
    
    public static boolean gatherBonds(WebDriver driver) {
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, String> changes = new HashMap<>();
        // Finam.ru
        String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	// BONDS
       	System.out.println("Try to get " + BASE_MW_URL + "and trying click da link");
    	driver.get(BASE_MW_URL);
    	List<WebElement> paywallCloser = driver.findElements(By.cssSelector(".close-btn"));                              // PayWall Close
    	List<WebElement> cookieCloser = driver.findElements(By.cssSelector(".gdpr-close"));                              // Cookie warning Close
    	if(paywallCloser.size() != 0) paywallCloser.get(0).click();
    	if(cookieCloser.size() != 0) cookieCloser.get(0).click();
    	try {
        	driver.findElement(By.cssSelector("a[href='https://www.marketwatch.com/investing/bonds']")).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException ecie) {
        	driver.switchTo().frame(driver.findElement(By.cssSelector("iframe[id^='sp_message_iframe']")));
        	driver.findElement(By.cssSelector("button[title='ACCEPT COOKIES']")).click();
        	driver.switchTo().defaultContent();
        	driver.findElement(By.cssSelector("a[href='https://www.marketwatch.com/investing/bonds']")).click();
    	}
    	String tsyMWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMUBMUSD10Y'] td.price";
    	String tsyMWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMUBMUSD10Y'] td.change";
    	String bundMWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKDE-10Y'] td.price";
    	String bundMWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKDE-10Y'] td.change";
    	String jp10MWCSSselectorValue = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKJP-10Y'] td.price";
    	String jp10MWCSSselectorChange = "tr[data-charting-symbol='BOND/BX/XTUP/TMBMKJP-10Y'] td.change";
    	
       	String tryValue = driver.findElement(By.cssSelector(tsyMWCSSselectorValue)).getText(); 
    	String tryChange = driver.findElement(By.cssSelector(tsyMWCSSselectorChange)).getText();
       	String bunValue = driver.findElement(By.cssSelector(bundMWCSSselectorValue)).getText(); 
    	String bunChange = driver.findElement(By.cssSelector(bundMWCSSselectorChange)).getText();
       	String japValue = driver.findElement(By.cssSelector(jp10MWCSSselectorValue)).getText(); 
    	String japChange = driver.findElement(By.cssSelector(jp10MWCSSselectorChange)).getText();
    	values.put("tryValue", tryValue);
    	values.put("bunValue", bunValue);
    	values.put("japValue", japValue);
    	changes.put("tryChang", tryChange);
    	changes.put("bunChang", bunChange);
    	changes.put("japChang", japChange);
    	System.out.println("Execute \"MapModifier\" for \"Bonds");
    	executor.execute(new MapModifier("Bonds", quiproquoColor, values, changes));
    	return true;
    }
    public static boolean gatherCurrencies(WebDriver driver) {
        HashMap<String, String> values = new HashMap<>();
        HashMap<String, String> changes = new HashMap<>();
        // Finam.ru
        String valueXPathFnm = "span[class^='PriceInformation__price']";
    	String changeXPathFnm = "sub[class^='PriceInformation__subContainer'] span:last-child";
    	// CURRENCIES
    	
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/usd-rub/");
    	driver.get(BASE_FNM_URL + "quote/forex/usd-rub/");
    	String rudValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("RUB","").replace(" ","") +  " ₽";
    	String rudChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	
    	System.out.println("Try to get " + BASE_FNM_URL + "quote/forex/eur-rub/");
    	driver.get(BASE_FNM_URL + "quote/forex/eur-rub/");
    	String rueValue = driver.findElement(By.cssSelector(valueXPathFnm)).getText().replace("RUB","").replace(" ","") + " ₽";
    	String rueChange = driver.findElement(By.cssSelector(changeXPathFnm)).getText().replaceAll("[\\)\\(]","");
    	values.put("rudValue", rudValue);
    	values.put("rueValue", rueValue);
    	changes.put("rudChang", rudChange);
    	changes.put("rueChang", rueChange);
    	System.out.println("Execute \"MapModifier\" for \"Currencies");
    	executor.execute(new MapModifier("Currencies", quiproquoColor, values, changes));
    	executor.shutdown();
    	return true;
    }

}
