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
	
    public static WebDriver getChromeWD(String windowSize) {
    	return HeadlessChromeInit.initChromeDriver(windowSize, getInstallDir());
    }
    public static WebDriver getFireFoxWD(String windowSize) {
    	return HeadlessFirefoxInit.initFireFoxDriver(windowSize, getInstallDir());
    }
	// Screen shot from Ashot
	
	public static void screenShoot(WebDriver driver, String name, String selector) {
    	try {
        	Screenshot screenshot = new AShot().coordsProvider(new WebDriverCoordsProvider()).takeScreenshot(driver,driver.findElement(By.cssSelector(selector)));
        	ImageIO.write(screenshot.getImage(), "PNG", new File(name + ".png"));
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
	}
	
	
/*	public static List<String> getClients() {
		LocalDate currentD = LocalDate.now();
		LinkedList<String> clientsList = new LinkedList<>();
		try(Scanner sc = new Scanner(new File(getInstallDir() + "Settings/" + "calendar.clients")).useDelimiter("\\R")) {
			while(sc.hasNext()) {
				String clientLine = sc.next();
				if(!clientLine.startsWith("//")) {
					String[] clientInfo = clientLine.split(" ");
					if(currentD.compareTo(LocalDate.parse(clientInfo[2]).plusDays(2)) < 0)
					    clientsList.add(clientInfo[0]);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return clientsList;
	}*/
	
	public static List<String> getClients() {
        return DBWork.getClients("Экономическая статистика");
	}
	public static HashMap<String, String> readDict() {
		HashMap<String, String> transformers = new HashMap<>(); 
		String curLine = "";
		try(Scanner scanner = new Scanner(new File(getInstallDir() + "Settings/calendar.dic")).useDelimiter("\\R")) {
			while(scanner.hasNext()) {
				curLine = scanner.next();
				if(curLine.contains("="))
				    transformers.put(curLine.split("=")[0],curLine.split("=")[1]);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return transformers;
	}
	
	public static String getInstallDir() {
    	try {
    		String installDir = Paths.get(Utils.class        // These several strings of code help to understand where we are
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
	public static String escapes(String str) {
    	return 	str.replace(".", "\\.")
		           .replace("#", "\\#")
		           .replace("!", "\\!")
		           .replace("{", "\\{")
		           .replace("}", "\\}")
		           .replace("=", "\\=")
		           .replace("|", "\\|")
		           .replace("-","\\-")
                   .replace("(","\\(")
                   .replace("[","\\[")
                   .replace(")","\\)")
                   .replace("]","\\]")
                   .replace("*","\\*")
                   .replace("~","\\~")
                   .replace("`","\\`")
                   .replace(">","\\>")
                   .replace("+","\\+")
    	           .replace("_","\\_");
	}
}
