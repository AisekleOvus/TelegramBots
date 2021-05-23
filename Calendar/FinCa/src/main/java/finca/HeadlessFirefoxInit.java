package finca;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;


public class HeadlessFirefoxInit {
	private static String windowXSize;
	private static String downloadXFilepath;
	public static WebDriver initFireFoxDriver(String windowSize, String downloadFilepath) {
		windowXSize = windowSize;
		downloadXFilepath = downloadFilepath;
//		String chromeProfilePath = "/home/torodioro/.config/google-chrome/";
		WebDriver driver = null;
/*		try {
			System.out.println("Geckodriver download path = "+downloadFilepath);
			System.setProperty("webdriver.gecko.driver", "/usr/local/bin/geckodriver");    	

			driver = new FirefoxDriver();

		}catch (org.openqa.selenium.SessionNotCreatedException snce){
			System.out.println("Session Not Created.. ..");
//			initFireFoxDriver(windowXSize, downloadXFilepath);
		}catch (org.openqa.selenium.WebDriverException wde) {
			System.out.println("WebDriver Exception.. ..");
//			initFireFoxDriver(windowXSize, downloadXFilepath);
		}catch(Exception e) {
			e.printStackTrace();
		}*/
		return driver;
	}
}
