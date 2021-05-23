package merch;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import java.time.LocalDate;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.ActionType;


public class AbiliOld extends AbilityBot {
	private LocalDate currentDate;
	private Scanner clientScanner;
	private String replayFor;
   	private String fullBotName;
	private final String botsForSale = "FINCA SLICER"; 
	
	private final static String FINCA_REAL_NAME = "@ToroDelOroEconomicBulletinBot";
	private final static String SLICER_REAL_NAME = "@ToroDelOroMarketSlicerBot"; 
	
	private final static String BOT_TOKEN = "1526996742:AAFbzgcYCndE179OMVi4Nei_5H5yYX-bG_w";
	private final static String BOT_USERNAME = "FIN.CONTENT";
	
	private final static String OFFER_FIRST_BUTTON_PRESSED = "FINCA BOT";
	private final static String OFFER_SECOND_BUTTON_PRESSED = "SLICER BOT";
	private final static String OFFER_THIRD_BUTTON_PRESSED = "DETAILING";
	private final static String OFFER_FOURTH_BUTTON_PRESSED = "SETTINGS";
	
	private final static String BUY_FINCA_BUTTON_PRESSED = "BFB";
	private final static String TRY_FINCA_BUTTON_PRESSED = "TFB";

	private final static String BUY_SLICER_BUTTON_PRESSED = "BSB";
	private final static String TRY_SLICER_BUTTON_PRESSED = "TSB";
	
	private final static String FINCA_DETAIL = "https://telegra.ph/Bot-Publikacii-EHkonomicheskoj-Statistiki-dlya-Telegram-03-03";
	private final static String SLICER_DETAIL = "https://telegra.ph/Bot-obzora-situacii-na-rynkah-03-04";
	
	private final static File SLICER_FILE = new File("./Settings/slicer.clients");
	private final static File FINCA_FILE = new File("./Settings/calendar.clients");
	
	private final static String FIRST_LEVEL_TEXT = "[*Бот \"Экономическая статистика\"*](https://telegra.ph/Bot-Publikacii-EHkonomicheskoj-Statistiki-dlya-Telegram-03-03) \\- актуальная экономическая статистика от авторитетных мировых "
			                                        + "финансовых институтов из 17 стран\\.\n"
			                                        + "[*Бот \"Обзор ситуации на рынках\"*](https://telegra.ph/Bot-obzora-situacii-na-rynkah-03-04) \\- 5 раз в день публикует \"тепловую карту\" биржевых индексов, стоимость нефти, меди, золота,"
			                                        +" доходности по облигациям, курсы рубля\\.\n"
			                                        + "\nПожалуйста, выберите бота, которого хотите подключить к вашему каналу\\. ";
//			                                        + "если бот уже подключен, вы можете перейти в раздел \"Управление подпиской\"";
	private final static String SECOND_LEVEL_FINCA = "И так, вы выбрали для подключения *\"Бот экономической статистики \"Events bot \"*"
			+ ", можете бесплатно пробовать его в течение 10 дней, или сразу перейти к оплате *250*\u20bd/месяц:";
	private final static String SECOND_LEVEL_SLICER = "И так, вы выбрали для подключения *\"Бот обзора ситуации на рынках \"Slicer bot\"*,"
			+ " можете бесплатно пробовать его в течение 10 дней, или сразу перейти к оплате *250*\u20bd/месяц:";
	
	private Set<File> clientsFilesSet;
	private Map<String, Set<String>> kycMap;
		
	private Set<String> offerLevel;
	private Set<String> sellLevel;
	private Set<String> tryLevel;
	
	private Map<String, String> dialog;
	
	public AbiliOld() {
		super(BOT_TOKEN, BOT_USERNAME);
        dialog = new HashMap<>();                            // RESERVED
        replayFor ="null";
		offerLevel = new HashSet<>();
		offerLevel.add(OFFER_FIRST_BUTTON_PRESSED);
		offerLevel.add(OFFER_SECOND_BUTTON_PRESSED);
		offerLevel.add(OFFER_THIRD_BUTTON_PRESSED);
		offerLevel.add(OFFER_FOURTH_BUTTON_PRESSED);
		
		sellLevel = new HashSet<>();
		sellLevel.add(BUY_FINCA_BUTTON_PRESSED);
		sellLevel.add(BUY_SLICER_BUTTON_PRESSED);
		tryLevel = new HashSet<>();
		tryLevel.add(TRY_FINCA_BUTTON_PRESSED);
		tryLevel.add(TRY_SLICER_BUTTON_PRESSED);
		currentDate = LocalDate.now();
		clientsFilesSet = new HashSet<>();
		clientsFilesSet.add(SLICER_FILE);
		clientsFilesSet.add(FINCA_FILE);
	}
	
	public void contentsCommand(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setParseMode("MarkdownV2");
        message.setDisableWebPagePreview(true);
        message.setText(FIRST_LEVEL_TEXT);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton botFirstButton = new InlineKeyboardButton();
                             botFirstButton.setText("Экономическая статистика");
                             botFirstButton.setCallbackData(OFFER_FIRST_BUTTON_PRESSED);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton botSecondButton = new InlineKeyboardButton();
                             botSecondButton.setText("Обзор ситуации на рынках");
                             botSecondButton.setCallbackData(OFFER_SECOND_BUTTON_PRESSED);
                             
                             
        List<InlineKeyboardButton> row3 = new ArrayList<>();       
        InlineKeyboardButton botFourthButton = new InlineKeyboardButton();
                             botFourthButton.setText("Управление подпиской");
                             botFourthButton.setCallbackData(OFFER_FOURTH_BUTTON_PRESSED);
                             
        row1.add(botFirstButton);
        row2.add(botSecondButton);
//      row3.add(botThirdButton);
//      row4.add(botFourthButton);
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
//      keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        
        try {
        	execute(message);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
	}
	public void payOrTry(String chatId, String botName) {
        String wtt = "";
        String wtb = "";
        String abo = "";
		SendMessage message = new SendMessage();
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId);
        String sayIt = botName.equals(OFFER_FIRST_BUTTON_PRESSED) ? SECOND_LEVEL_FINCA 
        		     : botName.equals(OFFER_SECOND_BUTTON_PRESSED) ? SECOND_LEVEL_SLICER
        		     : "";
        message.setText(sayIt);
        
        if(OFFER_FIRST_BUTTON_PRESSED.equals(botName)) {
            wtt = TRY_FINCA_BUTTON_PRESSED;
            wtb = BUY_FINCA_BUTTON_PRESSED;
            abo = FINCA_DETAIL;
        }else if(OFFER_SECOND_BUTTON_PRESSED.equals(botName)) {
            wtt = TRY_SLICER_BUTTON_PRESSED;
            wtb = BUY_SLICER_BUTTON_PRESSED;
            abo = SLICER_DETAIL;
        }
        
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton botFirstButton = new InlineKeyboardButton();
                             botFirstButton.setText("Об этом  боте");
                             botFirstButton.setUrl(abo);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton botSecondButton = new InlineKeyboardButton();
                             botSecondButton.setText("Купить");
                             botSecondButton.setCallbackData(wtb);
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton botThirdButton = new InlineKeyboardButton();
                             botThirdButton.setText("Попробовать");
                             botThirdButton.setCallbackData(wtt);

                             
        row1.add(botFirstButton);                             
        row2.add(botSecondButton);
        row3.add(botThirdButton);
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        
        try {
        	execute(message);
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
	}
    @Override
    public void onUpdateReceived(Update update) {
    	if(update.hasMessage()) {
    		String chatId = update.getMessage().getChatId().toString();
    		String messageText = update.getMessage().getText();
        	// ok we have chanel name let try
    		if (botsForSale.contains(replayFor))
    			try {
    			    trialer(Long.valueOf(chatId), "", messageText, true);
    			} catch(Exception e ) {
    				e.printStackTrace();
    			}
    	    // commands handler
        	if ("/start".equals(update.getMessage().getText()))
        		contentsCommand(update.getMessage().getChatId());
    	//callbacks handler
    	}else if (update.hasCallbackQuery()) {
            String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            String callbackQ = update.getCallbackQuery().getData();
            replayFor = "null";
    		if(offerLevel.contains(update.getCallbackQuery().getData())) {
                payOrTry(chatId, callbackQ); 
                
    		}else if (sellLevel.contains(callbackQ)) {
    			bargainHunter(Long.valueOf(chatId), callbackQ);
    			
    	    }else if (tryLevel.contains(callbackQ)) {
    	    	try {
    	    	    trialer(Long.valueOf(chatId), "", callbackQ, false);
    	    	} catch(Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    }
    	}
     }
    public void trialer(Long chatId, String username, String str, boolean hasChanelName) throws Exception{
    	String text = "";
 
		SendMessage message = new SendMessage();
		message.setParseMode("MarkdownV2");
        message.setChatId(chatId.toString());
        if(!hasChanelName) {
            if(TRY_SLICER_BUTTON_PRESSED.equals(str)) {
            	text = "Что бы активировать *10\\-дневный пробный период для \"Обзор ситуации на рынках \"Slicer bot\"*, пожалуйста, дайте ссылку на ваш канал\\. Или имя канала\\.";
            	replayFor = "SLICER";
            	fullBotName = SLICER_REAL_NAME;
            }
            if(TRY_FINCA_BUTTON_PRESSED.equals(str)) {
            	text = "Что бы активировать *10\\-дневный пробный период для \"Экономическая статистика \"Events bot\"*, пожалуйста, дайте ссылку на ваш канал\\. Или имя канала\\.";
            	replayFor="FINCA";
            	fullBotName = FINCA_REAL_NAME;
            }

        }else {
        	str = str.startsWith("@") ? str 
        		                      : str.startsWith("t.me/") ? "@" + str.substring(5)
        		                      : str.startsWith("https://t.me/") ? "@" + str.substring(13)
        		                      : "@" + str;
            boolean correct = str.matches("^@\\w+");
            if(correct) {
            	// Проверить, вдруг уже идет триальный период или он закончился
            	String clientLastChance = DBWork.getClientActivityPeriod(replayFor, str);
            	String botName = replayFor.equals("FINCA") ? "*\"Экономическая статистика \"Events bot\"*"
            			       : replayFor.equals("SLICER") ? "*\"Обзор ситуации на рынках \"Slicer bot\"*"
            			       : "";
           		if(clientLastChance !=null) {
           			LocalDate endDate = LocalDate.parse(clientLastChance);
            		if(currentDate.isBefore(endDate))
                        text = "Пробный период " + botName + " для канала " + str + " *уже активирован и действует до " + clientLastChance.toString().replace("-", "\\-") + "*";
            		else
            		    text = "Пробный период " + botName + " для канала " + str + " *закончен, действовал до " + clientLastChance.toString().replace("-", "\\-") + "*";

            	}else {
        			text = "Пробный период " + botName + " для канала " + str + " будет активирован *с " + currentDate.toString().replace("-", "\\-") + " до " + currentDate.plusDays(11).toString().replace("-", "\\-") + "*,";
        			text = text + "\n* теперь нужно добавить " + fullBotName + " в список администраторов канала " + str + ", разрешив только публикацию сообщений*\\.";
        			newCustomer(chatId.toString(), username, str, replayFor, LocalDate.now().toString(), currentDate.plusDays(11).toString(), null);
            	}
            } else {
            	text = "*Некорректное название канала !*";
            }
        }
        if(!text.equals("")) {
            message.setText(text);
            	execute(message);
           	if(hasChanelName) {
           		replayFor = "null";
                delaier(5, chatId);
           		contentsCommand(chatId);
           	}
        }
    }
    private void delaier(int sec, Long chatId) {
    	int delay = sec*1000;
    	try {
       		SendChatAction act = new SendChatAction();
       		act.setChatId(chatId.toString());
       		act.setAction(ActionType.get("typing"));
       		execute(act);
       		Thread.sleep(delay); 
    	} catch (Exception e) {}
    }
	public void bargainHunter(Long chatId, String botName) {
		String productTitle = "";
		String productDescr = "";
		String payLoad = "";
		String startParameter = "";
		
		if(BUY_FINCA_BUTTON_PRESSED.equals(botName)) {
			productTitle = "FINCA";
			productDescr = "FINCA";
			payLoad = "A";
			startParameter = "A1";	
		}
		if(BUY_SLICER_BUTTON_PRESSED.equals(botName)) {
			productTitle = "SLICER";
			productDescr = "SLICER";
			payLoad = "B";
			startParameter = "B1";	
		}
		SendInvoice slicerInvoice = new SendInvoice();
		slicerInvoice.setChatId(chatId.intValue());
		slicerInvoice.setTitle(productTitle);
		slicerInvoice.setDescription(productDescr);
		slicerInvoice.setPayload(payLoad);
		slicerInvoice.setProviderToken("381764678:TEST:22006");
		slicerInvoice.setStartParameter(startParameter);
		slicerInvoice.setCurrency("RUB");
		List<LabeledPrice> priceList = new ArrayList<>();
		priceList.add(new LabeledPrice("Цена", 25000));
		slicerInvoice.setPrices(priceList);
		slicerInvoice.setNeedName(true);
		
		try {
			execute(slicerInvoice);
		} catch (Exception e ) {
            e.printStackTrace();
		}
	}
	
	@Override
	public int creatorId() {
	    return 706141860;
	}
    private void newCustomer(String chatId, String username, String chanelName, String botName, String paidSince , String paidTill, String description ) {
        System.out.println(DBWork.setClients(chatId, username, chanelName, botName, paidSince, paidTill));
    }
}

class Customer {
	private HashMap<String, LocalDate> participation; // Название Бота = дата окончания периода пользования
	private String chanelName;
	
	public Customer() {
		
	}
}