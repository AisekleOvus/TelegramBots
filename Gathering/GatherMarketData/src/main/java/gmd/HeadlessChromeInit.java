package gmd;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;


public class HeadlessChromeInit {
	public static WebDriver initChromeDriver(String windowSize, String downloadFilepath) {
		String chromeProfilePath = "/home/torodioro/.config/google-chrome/";
		WebDriver driver = null;
		try {
			System.out.println("ChromeDriver download path = "+downloadFilepath);
			System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");    	
			ChromeOptions options = new ChromeOptions();
	    	HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
	    	chromePrefs.put("profile.default_content_settings.popups", 0);    	
	        chromePrefs.put("download.default_directory", downloadFilepath);
	        chromePrefs.put("excludeSwitches", "enable-automation");
	        options.setExperimentalOption("prefs", chromePrefs);
			options.addArguments("--headless", "--disable-gpu", "--window-size=" + windowSize,"--ignore-certificate-errors","--disable-infobars");
			options.addArguments("user-data-dir="+chromeProfilePath);
			driver = new ChromeDriver(options);
			driver.manage().timeouts().implicitlyWait(120L,  TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(120L,  TimeUnit.SECONDS);

		}catch(Exception e) {
			e.printStackTrace();
		}
		return driver;
	}
}
