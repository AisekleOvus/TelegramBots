package finca;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;


public class HeadlessChromeInit {
	private static String windowXSize;
	private static String downloadXFilepath;
	public static WebDriver initChromeDriver(String windowSize, String downloadFilepath) {
		windowXSize = windowSize;
		downloadXFilepath = downloadFilepath;
		String chromeProfilePath = "/home/torodioro/.config/google-chrome/";
		WebDriver driver = null;
		try {
			System.out.println("ChromeDriever download path = "+downloadFilepath);
			System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");  
	    	DesiredCapabilities dcap = new DesiredCapabilities();
	    	dcap.setCapability("pageLoadStrategy", "none");
			ChromeOptions options = new ChromeOptions();
	    	HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
	    	chromePrefs.put("profile.default_content_settings.popups", 0);    	
	        chromePrefs.put("download.default_directory", downloadFilepath);
	        chromePrefs.put("excludeSwitches", "enable-automation");
	        options.setExperimentalOption("prefs", chromePrefs);
			options.addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox", "--disable-gpu", "--window-size="+windowSize,"--ignore-certificate-errors","--disable-infobars");
			options.addArguments("user-data-dir="+chromeProfilePath);
			options.merge(dcap);
			driver = new ChromeDriver(options);
//			driver.manage().timeouts().implicitlyWait(60L,  TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(15L,  TimeUnit.SECONDS);
//			driver.manage().timeouts().setScriptTimeout(60L,  TimeUnit.SECONDS);

		}catch (org.openqa.selenium.SessionNotCreatedException snce){
			System.out.println(LocalDateTime.now() + "   -- Session Not Created.. trying again..");
			initChromeDriver(windowXSize, downloadXFilepath);
		}catch (org.openqa.selenium.WebDriverException wde) {
			System.out.println(LocalDateTime.now() + "   -- WebDriver Exception.. trying again..");
//			initChromeDriver(windowXSize, downloadXFilepath);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return driver;
	}
}
