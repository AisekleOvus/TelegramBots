package merch;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class LongPolling extends TelegramLongPollingBot {

	
    @Override
    public void onUpdateReceived(Update update) {
    	
        // We check if the update has a message and the message has text and user is Toro d'Oro
        if (update.hasMessage() && update.getMessage().hasText()) {
                SendMessage message = new SendMessage(); // Create a SendMessage object with mandatory fields
                message.setChatId(update.getMessage().getChatId().toString());
                message.setParseMode("MarkdownV2");
                message.setText("Hello");
                try {
                    execute(message); // Call method to send the message
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
        	}
     }

    @Override
    public String getBotUsername() {
        return "Экономические Рассылки";
    }

    @Override
    public String getBotToken() {
        return "1482280045:AAFHPLI6zGBY2xoZkm8N1QF16AaiaywC_v0";
    }
}