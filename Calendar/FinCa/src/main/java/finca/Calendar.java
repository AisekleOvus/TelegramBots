package finca;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;


public class Calendar {

    public static void main(String[] args) {
    	String mainCalendar = "https://ru.investing.com/economic-calendar/";
    	String reserveCalendar = "https://sslecal2.forexprostools.com?columns=exc_flags,exc_currency,exc_importance,exc_actual,exc_forecast,exc_previous&category=_employment,_economicActivity,_inflation,_credit,_centralBanks,_confidenceIndex,_balance,_Bonds&importance=2,3&features=datepicker,timezone&countries=25,4,17,39,72,26,10,6,37,43,56,36,5,61,22,12,35&calType=day&timeZone=18&lang=7";
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            BotSession ruleIt = botsApi.registerBot(new LongPolling());
            Scheduler scheduler = new Scheduler(mainCalendar, reserveCalendar, ruleIt);
       		scheduler.startCalendar(true); // false - yesterday true - today
        } catch (TelegramApiException e) {
            e.printStackTrace();
       	} catch(Exception e) {
        	e.printStackTrace();
        }
    }
}