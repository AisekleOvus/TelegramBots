package merch;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Scanner;

import java.time.LocalDate;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

import java.nio.charset.StandardCharsets;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;


public class Abili extends AbilityBot {


	private final static String BOT_TOKEN = "1526996742:AAFbzgcYCndE179OMVi4Nei_5H5yYX-bG_w";
	private final static String BOT_USERNAME = "FIN.CONTENT";
	private final static long TRIAL_DAYS = 365;
	private static String BOT_PRICE = "250";
	
	
	private  Map<String, String> bots;
	private  Map<String, String> fullBotsNames;
	private  Map<Integer, String> buttonsUrls;
	
	private final static String subscriptions = "Управление подпиской";
	private final static String contact = "Обратая связь";
	
	private final static String botDetails = "Подробнее об этом  боте";
	private final static String buyBot = "Купить";
	private final static String tryBot = "Попробовать";
	private final static String testBot = "Участвовать в тестировании";
	private final static String unsubscribe = "Отписаться";
	
	private final static String godModeClients = "Клиенты";
	private final static String godModeVisiters = "Посетители";
	
	private final static String backToMain = "К началу \u21a9";

	private static String botSwitcher;
	
	private String chanelName;
	private String contactInfo;
	private int lastMessageId;
	
	private boolean godMode;
	private boolean tryMode;
	private boolean buyMode;
	

	
	private final static String FIRST_LEVEL_TEXT = "[*Бот \"Экономическая статистика\"*](https://telegra.ph/Bot-Publikacii-EHkonomicheskoj-Statistiki-dlya-Telegram-03-03) \\- актуальная экономическая статистика от авторитетных мировых "
            + "финансовых институтов из 17 стран\\.\n"
            + "[*Бот \"Обзор ситуации на рынках\"*](https://telegra.ph/Bot-obzora-situacii-na-rynkah-03-04) \\- 5 раз в день публикует \"тепловую карту\" биржевых индексов, стоимость нефти, меди, золота,"
            +" доходности по облигациям, курсы рубля\\.\n"
            + "\nПожалуйста, выберите бота, которого хотите подключить к вашему каналу\\. "
            + "если бот уже подключен, вы можете перейти в раздел* \"Управление подпиской\"*";

	
	
	
	private List botChoiceButtonsLayer;
	private List buyOrTryButtonsLayer;
	private List godModeButtonsLayer;
	private List<Integer> messages;
	
	public Abili() {
		super(BOT_TOKEN, BOT_USERNAME);
		initialize();
	}
	private void initialize() {
		tryMode = false;
		buyMode = false;
		godMode = false;
		
		bots = new HashMap<>();
		fullBotsNames = new HashMap<>();
		buttonsUrls = new HashMap<>();
		messages = new ArrayList<>();
		
		bots.put("Экономическая статистика", "https://telegra.ph/Bot-Publikacii-EHkonomicheskoj-Statistiki-dlya-Telegram-03-03");
		bots.put("Обзор ситуации на рынках", "https://telegra.ph/Bot-obzora-situacii-na-rynkah-03-04");
		fullBotsNames.put("Экономическая статистика", "@ToroDelOroEconomicBulletinBot");
		fullBotsNames.put("Обзор ситуации на рынках", "@ToroDelOroMarketSlicerBot");
		buttonsUrls.put(4, "t.me/@TdOro");
		
		botSwitcher = ""; // Here will be current botName to work with
		

		
		botChoiceButtonsLayer = new ArrayList<String>();
        for(String bot : bots.keySet())
        	botChoiceButtonsLayer.add(bot);
		botChoiceButtonsLayer.add(subscriptions);
		botChoiceButtonsLayer.add(contact);
		
		godModeButtonsLayer = new ArrayList<String>();
		godModeButtonsLayer.add(godModeClients);
		godModeButtonsLayer.add(godModeVisiters);
		
		buyOrTryButtonsLayer = new ArrayList<String>();
		buyOrTryButtonsLayer.add(botDetails);
//		buyOrTryButtonsLayer.add(buyBot);
//		buyOrTryButtonsLayer.add(tryBot);
		buyOrTryButtonsLayer.add(testBot);
		buyOrTryButtonsLayer.add(contact);
		buyOrTryButtonsLayer.add(backToMain);

	}
	
	private SendMessage getButtons(String button) {
		List<String> buttonsList = new ArrayList<String>();
		buttonsList.add(button);
		return getButtons(buttonsList);
	}
	private SendMessage getButtons(String buttonCaption, String buttonCallBack) {
		Map<String, String> buttonsMap = new LinkedHashMap<>();
		buttonsMap.put(buttonCaption, buttonCallBack);
		return getButtons(buttonsMap);
	}
	private SendMessage getButtons(Map<String, String> buttonsMap) {
		Map<Integer, String> urls = new LinkedHashMap<>();
		return getButtons(buttonsMap, urls);
	}
	private SendMessage getButtons(List<String> buttonsList) {
		Map<Integer, String> urls = new LinkedHashMap<>();
		Map<String, String> buttonsMap = new LinkedHashMap<>();
		for(String button : buttonsList)
			buttonsMap.put(button, button);
		return getButtons(buttonsMap, urls);
	}
	private SendMessage getButtons(List<String> buttonsList, Map<Integer, String> urls) {
		Map<String, String> buttonsMap = new LinkedHashMap<>();
		for(String button : buttonsList)
			buttonsMap.put(button, button);
		return getButtons(buttonsMap, urls);
	}
	
	private SendMessage getButtons(Map<String, String> buttonsListWithCallbacks, Map<Integer, String> urls) { 
                                                                                      
		SendMessage message = new SendMessage();
		
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        int buttonCounter = 1;
        for(Map.Entry<String, String> buttonEntry : buttonsListWithCallbacks.entrySet()) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
                                 button.setText(buttonEntry.getKey());
                                 button.setCallbackData(buttonEntry.getValue());
                                 if(urls.containsKey(Integer.valueOf(buttonCounter)))
                                	 button.setUrl(urls.get(buttonCounter));
                                 
            row.add(button);
            keyboard.add(row);
            buttonCounter++;
        }
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        return message;
	}

	private void keepDialog(SendMessage message, String chatId, String text, boolean disableWebPagePreview) {
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        message.setDisableWebPagePreview(disableWebPagePreview);
        message.setText(text);
        try {
        	if(lastMessageId !=0 ) {
        	    execute(new DeleteMessage(chatId, lastMessageId));
        	    if(messages.contains(lastMessageId))
        		    messages.remove(messages.indexOf(lastMessageId));
        	}
        	lastMessageId = execute(message).getMessageId();
        	messages.add(lastMessageId);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
    @Override
    public void onUpdateReceived(Update update) {
    	User user = null;
    	SendMessage message = new SendMessage();
    	String userSaid = "";
    	String chatId = "";
    	String second_level_text = "";

    	
    	if (update.hasMessage()) {
    		user = update.getMessage().getFrom();
    		userSaid = update.getMessage().getText();
    		chatId = update.getMessage().getChatId().toString();
        	if(tryMode)
                if(trialer(chatId, user.getUserName(), botSwitcher, userSaid))
                	tryMode = false;
    	}
    	if (update.hasCallbackQuery()) {
    		user = update.getCallbackQuery().getMessage().getFrom();
    		userSaid = update.getCallbackQuery().getData();
    		chatId = update.getCallbackQuery().getMessage().getChatId().toString();
    	}
    	System.out.println(userSaid);
    	
    	if (("/start" + " " + backToMain).contains(userSaid)) {    		
            if(!messages.isEmpty()) {
            	System.out.println(lastMessageId + "\n\n");
            	if(messages.contains(lastMessageId))
            	    messages.remove(messages.indexOf(Integer.valueOf(lastMessageId)));
            	messages.forEach(System.out::println);
        		for(Integer messageId : messages) {
        			DeleteMessage dm = new DeleteMessage(chatId, messageId);
        			try {
        				execute(dm);
        			} catch (Exception e) {
        				e.printStackTrace();
        			}
        		}
            }
    		initialize();
    		godMode = chatId.equals(String.valueOf(creatorId()));
    		if(godMode /*&& Collections.disjoint(botChoiceButtonsLayer, godModeButtonsLayer)*/)
    			botChoiceButtonsLayer.addAll(godModeButtonsLayer);
    		DBWork.setVisiters(chatId, user.getFirstName(), user.getLastName(), user.getUserName());
    		keepDialog(getButtons(botChoiceButtonsLayer, buttonsUrls), chatId, FIRST_LEVEL_TEXT, true);
    	}
    	
    	if (bots.containsKey(userSaid)) {
    		
    		botSwitcher = userSaid;
/*    		second_level_text = "И так, вы выбрали для подключения бота *\""+ botSwitcher +"\"*"
			+ ", можете бесплатно пробовать его в течение 10 дней, или сразу перейти к оплате *" + BOT_PRICE + "*\u20bd/месяц:";*/
    		second_level_text = "И так, вы выбрали для подключения бота __ _\""+ botSwitcher +"\"_ __"
			+ "\n* Продукт находится в стадии тестировния, пользоваться можно бесплатно\\.*\n Приблизительная дата окончания тестирования \\- май 2021г\\. О введении абонентской платы, подключенные пользователи будут извещены за неделю\\.";
    		buttonsUrls.put(1, bots.get(userSaid));
    		buttonsUrls.put(3, "t.me/@TdOro");
    		buttonsUrls.remove(4);
    		keepDialog(getButtons(buyOrTryButtonsLayer, buttonsUrls), chatId, second_level_text, true);
    	}
    	if(tryBot.equals(userSaid) || buyBot.equals(userSaid) || testBot.equals(userSaid)) {
//    		System.out.println("Customer " + update.getCallbackQuery().getFrom().getUserName() + " wanna try " + botSwitcher);
    		if(!tryMode) {
//    			String text = "Что бы активировать *10\\-дневный пробный период для \"" + botSwitcher + "\"*, пожалуйста, дайте ссылку на ваш канал\\.";
    			String text = "Что бы подключить бота __ _\"" + botSwitcher + "\"_ __, пожалуйста, дайте ссылку на ваш канал\\.";
    			keepDialog(message, chatId, text, true);
    		}
    		tryMode = true;
    	}
    	if(godModeButtonsLayer.contains(userSaid))
    		godModeHandler(userSaid);
    	if(subscriptions.equals(userSaid))
    		showSubscriptions(chatId);
    	if(userSaid.contains(unsubscribe))
    		unsubscribe(userSaid, update.getCallbackQuery().getMessage(), chatId);
    		
     }
    private void showSubscriptions(String chatId) {
    	String resultText = "Это все ваши подписки ";
    	Map<Integer, List<String>> subscriptions = DBWork.getCusromerSubscriptions(chatId);
//    	for(int i = 1; i <= subscriptions.size(); i++) {
    	for(Map.Entry<Integer, List<String>> subscription : subscriptions.entrySet()) {
    		StringBuilder text = new StringBuilder();
    		List<String> row = subscription.getValue();
    		Integer days = Integer.valueOf(row.get(4))%10;
        	String ending = days == 1 ? "день"
        			      : days == 2 || days == 3 || days == 4 ? "дня"
        			      : "дней";
    		text.append("Канал *" + row.get(0) + "*\n");
    		text.append("подписан на :\n*" + row.get(1) + "*\n");
    		text.append("c *" + escapes(row.get(2)) + "*\n");
    		text.append("по *" + escapes(row.get(3)) + "*\n");
    		text.append("__до конца подписки *" + row.get(4) + "* " + ending + "__");

    		
    		keepDialog(getButtons(unsubscribe, unsubscribe + "_" + subscription.getKey()), chatId, text.toString(), true);
    		lastMessageId = 0;
    	}
    	if(subscriptions.isEmpty()) 
    		resultText = "Подписки отсутствуют \ud83e\udd37\u200d\u2642\ufe0f";
    	
    	keepDialog(getButtons(backToMain), chatId, escapes(resultText), true);
    }
    private void unsubscribe(String buttonCallBack, Message messageToEdit, String chatId) {
    	Integer messageId = messageToEdit.getMessageId();
    	String messageText = messageToEdit.getText();
    	DBWork.unsubscribe(buttonCallBack.split("_")[1]);
    	EditMessageText emt = new EditMessageText();
    	            emt.setChatId(chatId);
    	            emt.setMessageId(messageId);
    	            emt.setText(messageText + "\n\n *ПОДПИСКА ОТМЕНЕНА*");
    	 try {
    		 execute(emt);
    	 } catch (Exception e) {
    		 e.printStackTrace();
    	 }
    	
    }
    private boolean trialer(String chatId, String username, String botSwitcher, String userSaid) {

		LocalDate currentDate = LocalDate.now();
//    	SendMessage message = new SendMessage();

    	String botName = botSwitcher;
    	String chanelName = userSaid.startsWith("@") ? userSaid 
                : userSaid.startsWith("t.me/") ? "@" + userSaid.substring(5)
                : userSaid.startsWith("https://t.me/") ? "@" + userSaid.substring(13)
                : "@" + userSaid;

        String fullBotName = fullBotsNames.get(botName);
	    String text = "Тестовый период бота __ _" + botName + "_ __ для канала *" + chanelName + "* будет активирован *с " + escapes(currentDate.toString()) + " до " + escapes(currentDate.plusDays(TRIAL_DAYS).toString()) + "*,";
		       text = text + "\n теперь нужно добавить " + fullBotName + " в список администраторов канала " + chanelName + ", *разрешив ему только публикацию сообщений*\\.";
                
         boolean correct = chanelName.matches("^@\\w+");
         
         System.out.println(username);
         if(correct) {
         	// Проверить, вдруг уже идет триальный период или он закончился
         	String clientLastChance = DBWork.getClientActivityPeriod(botName, chanelName);
        	
        	
			if(clientLastChance !=null) {
				LocalDate endDate = LocalDate.parse(clientLastChance); 
         		    if(currentDate.isBefore(endDate)) {
                        text = "Тестовый период бота __ _" + botName + "_ __ для канала *" + chanelName + "* *уже активирован и действует до " + escapes(clientLastChance.toString()) + "*";
         		        return true;
         		    } 
/*         		    else
         		        text = "Тестовый период бота __ _" + botName + "_ __ для канала *" + chanelName + "* *закончен, действовал до " + escapes(clientLastChance.toString()) + "*";
*/

     			String utf8BotName = botName;
     			System.out.println(DBWork.setClients(chatId, username, chanelName, utf8BotName, currentDate.toString(), currentDate.plusDays(TRIAL_DAYS).toString()));
			}
         } else {
         	text = "*Некорректное название канала !*";
         	return false;
         }
         
         keepDialog(getButtons(backToMain), chatId, text, true);
         return true;
    }
	public void godModeHandler(String command) {
		String text = "это все " + command.toLowerCase();
		int quantity = 0;
		String creatorChatId = String.valueOf(creatorId());
		if(command.equals(godModeClients)) {
			List<String> clients = 	DBWork.getClients();
			quantity = clients.size();
			clients.forEach(row -> {
				
				keepDialog(new SendMessage(), creatorChatId, row, true);
				lastMessageId = 0;
				
			});
	    	if(clients.isEmpty()) 
	    		text = "А нет у тебя клиентов \ud83e\udd37\u200d\u2642\ufe0f";
		}
		if(command.equals(godModeVisiters)) {
	    	Map<Integer, List<String>> visiters = DBWork.getVisiters("daily");
	    	quantity = visiters.size();
	    	for(int i = 1; i <= visiters.size(); i++) {
	    		List<String> row = visiters.get(i);
		    	StringBuilder textBuilder = new StringBuilder();
		    	textBuilder.append(row.get(0) + "\n");
		    	textBuilder.append(row.get(1) + "\n");
		    	textBuilder.append(row.get(2) + "\n");
		    	textBuilder.append("__" + row.get(3) + " " + row.get(4) + "__\n");
		    	textBuilder.append("\n");
	    		keepDialog(new SendMessage(), creatorChatId, escapes(textBuilder.toString()), true);
	    		lastMessageId = 0;
	    	}
	    	if(visiters.isEmpty()) 
	    		text = ("А не было посетителей \ud83e\udd37\u200d\u2642\ufe0f");
	    	
		}
		keepDialog(getButtons(backToMain), creatorChatId, escapes(text) + " " + quantity + " шт", true);
		
	}
	@Override
	public int creatorId() {
	    return 706141860;
	}
	private String escapes(String str) {
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
    	           .replace("_","\\_")
    	           .replace("\\_\\_","__");
	}
}