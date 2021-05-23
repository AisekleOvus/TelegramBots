package gmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.util.Map;
import java.util.HashMap;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

import java.time.LocalDate;
import java.time.LocalTime;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import com.ovus.aisekle.telegrambotwork.TBWork;


public class MapModifier implements Runnable{
	private Map<String, String> quiproquoColor;
	private Map<String, String> quiproquoIValues;
	private Map<String, String> quiproquoIChanges;
	private String partOTWorld;
	private String parentDir;
	private String mapPath;
	
    public  MapModifier(String partOTWorld, Map<String, String> quiproquoColor, Map<String, String> quiproquoIValues, Map<String, String> quiproquoIChanges) {
    	this.quiproquoColor = quiproquoColor;
    	this.quiproquoIValues = quiproquoIValues;
    	this.quiproquoIChanges = quiproquoIChanges;
    	this.partOTWorld = partOTWorld;
    	parentDir = Setup.getInstallDir();
    	mapPath = parentDir + "maps/";
    	Logger log = Logger.getLogger("GMD");
    	log.logp(Level.WARNING, "MapModifier", "Constructor", "MapModifier for " + partOTWorld);
    }
    @Override
    public void run() {
    	try {
        	String newFileName = maPainter(partOTWorld, quiproquoColor, quiproquoIValues, quiproquoIChanges);
        	if(new SliceSender(parentDir).sendSlice2Telegram(newFileName)) 
        		Files.deleteIfExists(Paths.get(mapPath + newFileName));
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    private String maPainter(String mapName, Map<String, String> quiproquoColor, Map<String, String> quiproquoIValues, Map<String, String> quiproquoIChanges) {
        String line = "";
        String quiColor = "#";
        String textIdent = " ";
        
        
        String colouredFileName = LocalDate.now() + "_" + LocalTime.now() + "_" + mapName + "_coloured.svg";
        try(BufferedReader br = new BufferedReader(new FileReader(mapPath + mapName + ".svg"))) {
            BufferedWriter bwr = new BufferedWriter(new FileWriter(mapPath + colouredFileName));
            while((line = br.readLine()) != null ) {
               if(line.contains("fill=")) {
                   int fillIndx = line.indexOf("fill=");
                   quiColor = line.substring(fillIndx + 6, fillIndx + 13);
               }
               if(quiproquoColor.containsKey(quiColor)) {
                   line = line.replaceAll("fill=\"" + quiColor + "\"", "fill=\"" + quiproquoColor.get(quiColor) + "\"");
               }
               if(line.contains("<text style=")) {
            	   textIdent = line.substring(line.indexOf(">") + 1, line.indexOf(">") + 9);
               }
               if(quiproquoIValues.containsKey(textIdent)) {
            	   String changedValue = quiproquoIValues.get(textIdent);
            	   if(changedValue.contains("-")) {
            		   line = line.replace("x=\"441.695\"", "x=\"431.695\"");
            		   line = line.replace(textIdent, changedValue);
            	   } else {
            	       line = line.replace(textIdent, changedValue);
            	   }
               }
               if(quiproquoIChanges.containsKey(textIdent)) {
            	   String changedValue = quiproquoIChanges.get(textIdent);
            	   if(!changedValue.contains("-")) {
            		   changedValue = changedValue.contains("+") ? changedValue : "+" + changedValue;
            	       line = line.replace(textIdent+"e", changedValue + (!changedValue.contains("%") ? "%" : ""));
            	   } else {
            		   line = line.replace("fill: green;", "fill: red;");
            		   line = line.replace(textIdent+"e", changedValue + (!changedValue.contains("%") ? "%" : ""));
            	   }   
               }
               bwr.write(line + System.lineSeparator());
            }
            bwr.flush();
            bwr.close();
            
         }catch(Exception e) {
             e.printStackTrace();
         }
        SvgJpg.SvgJpg(mapPath + colouredFileName);
        return colouredFileName + ".jpg";
    }
}

abstract class CountryColors {
	final static String USA = "#AAAAAA";
	final static String CANADA = "#CCCCCC";
	final static String MEXICO = "#CCCCC0";
	final static String CHINA = "#CCCCC1";
	final static String KOREA = "#CCCCC2";
	final static String JAPAN = "#CCCCC3";
	final static String AUSTRALIA = "#CCCCC4";
	final static String SPAIN = "#CCCCC5";
	final static String PORTUGAL = "#CCCCC6";
	final static String ITALY = "#CCCCC7";
	final static String FRANCE = "#CCCCC8";
	final static String BRITAIN = "#CCCCC9";
	final static String BELGIUM = "#DDDDD1";
	final static String HOLLAND = "#DDDDD2";
	final static String DENMARK = "#DDDDD3";
	final static String GERMANY = "#DDDDD4";
	final static String POLAND = "#DDDDD5";
	final static String SWITZERLAND = "#DDDDD6";
	final static String AUSTRIA = "#DDDDD7";
	final static String CZECHIA = "#DDDDD8";
	final static String RUSSIA = "#DDDDD9";
	final static String NORWAY = "#C9CCCC";
	final static String SWEDEN = "#C8CCCC";
	final static String FINLAND = "#C7CCCC";
	final static String INDIA = "#C6CCCC";
	final static String ICELAND = "#C5CCCC";
	final static String IRLAND = "#C4CCCC";

}

abstract class IndiciesValues {
	final static String SNP = "snpValue";
	final static String NASDAQ = "nsdValue";
	final static String DJIA = "djiValue";
	final static String HANGSENG = "hasValue";
	final static String SHANGHAI = "shaValue";
	final static String NIKKEI = "nikValue";
	final static String ASX = "asxValue";
	final static String DAX = "daxValue";
	final static String CAC = "cacValue";
	final static String FTSE = "ftsValue";
	final static String IMOEX = "imoValue";
	final static String BSESEN = "bseValue";
	final static String KOSPI = "kosValue";
	final static String SNPBMV = "bmvValue";
	final static String SNPTSX = "tsxValue";
	final static String IBEX = "ibeValue";
	final static String FTSEMIB = "mibValue";
	final static String OMX = "omxValue";
}
abstract class IndiciesChages {
	final static String SNP = "snpChang";
	final static String NASDAQ = "nsdChang";
	final static String DJIA = "djiChang";
	final static String HANGSENG = "hasChang";
	final static String SHANGHAI = "shaChang";
	final static String NIKKEI = "nikChang";
	final static String ASX = "asxChang";
	final static String DAX = "daxChang";
	final static String CAC = "cacChang";
	final static String FTSE = "ftsChang";
	final static String IMOEX = "imoChang";
	final static String BSESEN = "bseChang";
	final static String KOSPI = "kosChang";
	final static String SNPBMV = "bmvChang";
	final static String SNPTSX = "tsxChang";
	final static String IBEX = "ibeChang";
	final static String FTSEMIB = "mibChang";
	final static String OMX = "omxChang";
}
