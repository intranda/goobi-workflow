package org.goobi.production.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

/**
 * This class serves as a websocket server to get messages for localization in JavaScript. <br>
 * The server expects a JSON object with a language and the key that shall be localized, eg: {"lang":"de","key":"Content"} <br>
 * The response is again a JSON object with the original key and the localized value as fields: {"key":"Content","value":"Inhalt"}
 * 
 * @author Oliver Paetzel
 *
 */
@Log4j2
@ServerEndpoint("/messagesws")
public class MessagesWs {
    private static Gson gson = new Gson();
    private static Map<Locale, ResourceBundle> locale2BundleMap = new HashMap<>();

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {
        MessageRequest mr = gson.fromJson(message, MessageRequest.class);
        Locale locale = new Locale(mr.lang);
        if (!locale2BundleMap.containsKey(locale)) {
            loadMessages(locale);
        }

        JsonObject jo = new JsonObject();
        jo.addProperty("key", mr.key);
        String translated = "???" + mr.key + "???";
        try {
            translated = locale2BundleMap.get(locale).getString(mr.key);
        } catch (MissingResourceException e) {
            //don't log
        }
        jo.addProperty("value", translated);
        session.getBasicRemote().sendText(gson.toJson(jo));
    }

    @OnOpen
    public void onOpen() {
        log.debug("Client connected");
    }

    @OnClose
    public void onClose() {
        log.debug("Connection closed");
    }

    private static void loadMessages(Locale locale) {
        locale2BundleMap.put(locale, ResourceBundle.getBundle("messages.messages", locale));
    }

    private class MessageRequest {
        String lang;
        String key;
    }
}
