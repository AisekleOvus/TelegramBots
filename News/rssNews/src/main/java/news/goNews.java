package news;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.nio.file.Path;
import java.nio.file.Paths;

public class goNews {
    public static void main(String[] args) {
    	ExecutorService executor = Executors.newFixedThreadPool(1);
    	executor.execute(new RussianNews("https://www.interfax.ru/rss.asp", getInstallDir()));
    	executor.shutdown();
    }
	private static String getInstallDir() {
    	try {
    		String installDir = Paths.get(goNews.class        // These several strings of code help to understand where we are
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
