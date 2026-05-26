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
 */

package org.goobi.production.messages;

import java.net.URI;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

public class MessagesWsConfigurator extends ServerEndpointConfig.Configurator {

    @Override
    public boolean checkOrigin(String originHeaderValue) {
        if (originHeaderValue == null) {
            return false; // browsers always send Origin
        }
        try {
            String host = URI.create(originHeaderValue).getHost();
            return host != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession == null || httpSession.getAttribute("LoginBean") == null) {
            // Reject upgrade by emitting a 401 in modifyHandshake-equivalent
            // path; concretely, store a marker and reject in @OnOpen.
            config.getUserProperties().put("authenticated", Boolean.FALSE);
        } else {
            config.getUserProperties().put("authenticated", Boolean.TRUE);
        }
    }

}
