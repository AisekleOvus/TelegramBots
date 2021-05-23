package finca;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Set;
import java.util.Scanner;
import java.util.TimeZone;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZoneId;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.net.SocketTimeoutException;

import org.jsoup.Jsoup;
import org.jsoup.HttpStatusException;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

public class Utils {
	// Screen shot from Ashot
	public static void screenShoot(WebDriver driver, String name, String selector) {
    	try {
        	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.cssSelector(selector)));
        	ImageIO.write(screenshot.getImage(), "PNG", new File(name + ".png"));
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	// Try to choose working calendar url;
	public static String[] getCalendarURL() {
		int tryout = 0;
		String[] calendar = { 
/* main */				"https://ru.investing.com/economic-calendar/",
/* fallback */			"https://sslecal2.forexprostools.com/?columns=exc_flags,exc_currency,exc_importance,exc_actual,exc_forecast,exc_previous&category=_employment,_economicActivity,_inflation,_credit,_centralBanks,_confidenceIndex,_balance,_Bonds&importance=1,2,3&features=datepicker,timezone&countries=25,32,4,17,72,6,37,7,43,56,36,5,63,61,22,12,11,35&calType=day&timeZone=18&lang=7"
		         };
		for(int i = 0 ; i < 2; i = i == 1 ? 0 : 1) {
			tryout++;
			try { if(tryout % 3 == 0) Thread.sleep(300000); }
			catch (Exception e) { e.printStackTrace(); }
			
			try {
				Jsoup.connect(calendar[i]).get();
				return new String[] { i==1 ? "fallback" : "main", calendar[i]};
			}catch(HttpStatusException hse) {
				if(tryout == 2) System.out.println("Both Calendars URL do not responce ! Try till 23:00");
				if(LocalTime.now(ZoneId.of("GMT")).plusHours(getMoscowOffset()).isBefore(LocalTime.parse("23:00"))) continue;
				System.out.println("Both sites have not been reponding till 23:00 !");
				return new String[]{"abort","abort"};
			}catch(SocketTimeoutException ste) {
				if(tryout == 2) System.out.println("Both Calendars URL do not responce ! Try till 23:00");
				if(LocalTime.now(ZoneId.of("GMT")).plusHours(getMoscowOffset()).isBefore(LocalTime.parse("23:00"))) continue;
				System.out.println("Both sites have not been reponding till 23:00 !");
				return new String[]{"abort","abort"};
			}catch(IOException ioe) {
				ioe.printStackTrace();
			}
	    }
		return new String[]{"abort","abort"};
	}
	
	public static List<String> getClients(String fname) {
		LinkedList<String> clientsList = new LinkedList<>();
		try(Scanner sc = new Scanner(new File(getInstallDir() + "Settings/" + fname)).useDelimiter("\\R")) {
			while(sc.hasNext()) {
				String client = sc.next();
				if(!client.startsWith("//")) clientsList.add(client);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return clientsList;
	}
	
	public static String getInstallDir() {
    	try {
    		String installDir = Paths.get(Calendaring.class        // These several strings of code help to understand where we are
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
    
    public static boolean isAppropriateDateTime(String soughtDateTime) {
//    	System.out.println("is it appropriate : " + soughtDateTime + " ?");
        int timeOffset = getMoscowOffset();
        LocalDate mosDate = LocalDate.now(ZoneId.of("Europe/Moscow"));
        LocalDateTime dtGMT = LocalDateTime.parse(soughtDateTime.replace(" ","T").replace("/", "-"));
        dtGMT = dtGMT.plusHours(timeOffset);
        if(dtGMT.toLocalDate().compareTo(mosDate) == 0) {
//        	System.out.println("YES");
        	return true;
        }
//        System.out.println("NO");
        return false;
     }
        
    public static int getMoscowOffset() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Moscow");
        return tz.getOffset(new Date().getTime()) / 1000 / 60 / 60;
     }
    
    public static String[] getTelegramDirAndUrl() {  // [0] - local telegram dir; [1] - url e.g www.site.ru/telegram/
    	HashMap<String, String[] > settsMap = new HashMap<>();
    	String[] strArr = new String[2];
    	String line = null;
    	try(Scanner settsScanner = new Scanner(new File(getInstallDir() + "Settings/" + "mcd.set")).useDelimiter("\\R")) {
        	while(settsScanner.hasNext()) {
        		line = settsScanner.next();
        		strArr = line.split("  ");
       		    settsMap.put(strArr[0], strArr[1].split("=="));
        		
        	}
    	} catch(Exception e) {
    	    e.printStackTrace();
    	}
        return settsMap.get("telegram");
    }
}
