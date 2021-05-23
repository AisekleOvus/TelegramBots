package finca;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogManager;

public class GetCalStarter {
    public static void main(String[] args) {
    	
    	try {
    	    LogManager.getLogManager().readConfiguration(GetCalStarter.class.getResourceAsStream("/logging.properties"));
    	   
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	Logger log = Logger.getLogger("CalLog");
    	log.logp(Level.WARNING, "GetCalStarter", "Main", "OK, Let's do it");
    	
        new GetCal(/*args[0]*/ log);
    	//new Calendaring(LocalDate.parse(args[0]), LocalTime.parse(args[1]), "https://sslecal2.forexprostools.com/?columns=exc_flags,exc_currency,exc_importance,exc_actual,exc_forecast,exc_previous&category=_employment,_economicActivity,_inflation,_credit,_centralBanks,_confidenceIndex,_balance,_Bonds&importance=1,2,3&features=datepicker,timezone&countries=25,32,4,17,72,6,37,7,43,56,36,5,63,61,22,12,11,35&calType=day&timeZone=18&lang=7");
    }
}
