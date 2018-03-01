package org.goobi.production.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import lombok.extern.log4j.Log4j;

@Log4j
@ServerEndpoint("/messagesws")
public class MessagesWs {
    private static Gson gson = new Gson();
    private static Map<Locale, ResourceBundle> locale2BundleMap = new HashMap<>();

    @OnMessage
    public void onMessage(String message, Session session) throws IOException,
            InterruptedException {
        MessageRequest mr = gson.fromJson(message, MessageRequest.class);
        Locale locale = new Locale(mr.lang);
        if (!locale2BundleMap.containsKey(locale)) {
            loadMessages(locale);
        }

        JsonObject jo = new JsonObject();
        jo.addProperty("key", mr.key);
        jo.addProperty("value", locale2BundleMap.get(locale).getString(mr.key));
        log.debug("sending answer:\n" + gson.toJson(jo));
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
