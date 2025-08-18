/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.production.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
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
    public void onMessage(String message, Session session) throws IOException {
        MessageRequest mr = gson.fromJson(message, MessageRequest.class);
        Locale locale = Locale.of(mr.lang);
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
        locale2BundleMap.put(locale, ResourceBundle.getBundle("messages", locale));
    }

    private final class MessageRequest {
        private String lang;
        private String key;
    }
}
