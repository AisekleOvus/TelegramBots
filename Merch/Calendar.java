package finca;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class Calendar {

    public static void main(String[] args) {
    	String todaily = "https://www.fx.co/ru/forex-calendar/today";
    	String weekly = "https://www.fx.co/ru/forex-calendar/current_week?importance=medium,high";
    	LongPolling lpBot = new LongPolling();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new LongPolling());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        if(args.length > 0) {
        	try {
            	if("todaily".equals(args[0])) lpBot.startCalendar("daily", todaily);
            	if("weekly".equals(args[0])) lpBot.startCalendar("weekly", weekly);
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        }
    }
}