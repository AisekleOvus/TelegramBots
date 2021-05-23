package finca;

import java.net.URI;
import java.net.URL;

import java.nio.charset.StandardCharsets;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.Connection;

/*
 *  Author - Aisekle Ovus
 *  Version 2.1 *  
 */

public class TBWork {
	private String token;
	private static final String TELEGA_URL = "https://api.telegram.org/bot%s/%s";
	private String chat_id;

		
	public TBWork(String token, String chat_id) {
		this.token = token;
		this.chat_id = chat_id;
	}
	

	//  sendMessage methods set
	
    public String sendMessage(String params) {
        return sendMessage(paramMapPacking(params));
    }
    public String sendMessage(Map<String, String> params) {
        return sendRequest("sendMessage", params);
    }

//                                                editMessageText methods set
	
	public String editMessageText(String params) {
		return editMessageText(paramMapPacking(params));
	}
	public String editMessageText(Map<String, String> params) {
		return sendRequest("editMessageText", params);
	}
//                                                sendPhoto methods set	

	public String sendPhoto(String params) { // params must be pair of '&key=value'
		return sendPhoto(paramMapPacking(params));
    }
	    
    public String sendPhoto(Map<String, String> params) {
    	return sendRequest("sendPhoto", params);
    	
    }

    private String percenting(String str) {
    	return 	str.replace(".", "\\%2E")
		           .replace("#", "\\%23")
                   .replace(" ","%20")
                   .replace("\n","%0D%0A")
		           .replace("-","\\%2D")
                   .replace("(","\\%28")
                   .replace(")","\\%29");
/*    	try {
            str = URLEncoder.encode(str, StandardCharsets.UTF_8.toString())
            		.replace("+","%20")
            		.replace(".", "\\%2E")
            		.replace("%23", "\\%23")
                    .replace("-","\\%2D")
                    .replace("%28","\\%28")
                    .replace("%29","\\%29");
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return 	str;*/
    }
    private Map<String, String> paramMapPacking(String str) {
    	String[] paramarray = str.split("(?<!S)&(?!P)"); // this array not empty values start with 1 index
    	HashMap<String, String> paramsHM = new HashMap<>();
    	for(int i = 1; i < paramarray.length; i++) {
    		String[] paramEntry = paramarray[i].split("=");
    	    paramsHM.put(paramEntry[0], paramEntry[1]);
    	}
    	
    	return paramsHM;
    }
    private String sendRequest(String method, Map<String, String> params) {
    	StringBuilder stringParams = new StringBuilder();
    	String result ="empty";

    	
    	if(params.containsKey("caption"))
    		params.computeIfPresent("caption", (k, v) -> percenting(v));
    	if(params.containsKey("text"))
    		params.computeIfPresent("text", (k, v) -> percenting(v));
    	
    	for(Map.Entry entry : params.entrySet())
    		stringParams.append("&" + entry.getKey() + "=" + entry.getValue());
    	
    	String wholeURL = String.format(TELEGA_URL, token, method + "?chat_id=" + chat_id + stringParams.toString());
    	System.out.println(wholeURL);
    	
    	try {
    	    Connection telegramBotConnection = Jsoup.connect(wholeURL).ignoreContentType(true);
    		result = telegramBotConnection.execute().body();
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	return wholeURL + System.lineSeparator() + result;    	
    }
}
