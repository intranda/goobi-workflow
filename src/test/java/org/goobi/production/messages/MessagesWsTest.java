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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

@ExtendWith(MockitoExtension.class)
public class MessagesWsTest {

    @Test
    public void testOnMessageKnownKeyReturnsTranslation() throws IOException {
        RemoteEndpoint.Basic remote = Mockito.mock(RemoteEndpoint.Basic.class);
        Session session = Mockito.mock(Session.class);

        ArgumentCaptor<String> responseCapture = ArgumentCaptor.forClass(String.class);
        Mockito.when(session.getBasicRemote()).thenReturn(remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);

        Mockito.verify(remote).sendText(responseCapture.capture());
        String response = responseCapture.getValue();
        assertTrue(response.contains("\"key\":\"Content\""));
        assertTrue(response.contains("\"value\":"));
    }

    @Test
    public void testOnMessageUnknownKeyReturnsFallback() throws IOException {
        RemoteEndpoint.Basic remote = Mockito.mock(RemoteEndpoint.Basic.class);
        Session session = Mockito.mock(Session.class);

        ArgumentCaptor<String> responseCapture = ArgumentCaptor.forClass(String.class);
        Mockito.when(session.getBasicRemote()).thenReturn(remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"unknownKeyThatDoesNotExist99999\"}", session);

        Mockito.verify(remote).sendText(responseCapture.capture());
        assertTrue(responseCapture.getValue().contains("???unknownKeyThatDoesNotExist99999???"));
    }

    @Test
    public void testOnMessageCachesLocale() throws IOException {
        RemoteEndpoint.Basic remote = Mockito.mock(RemoteEndpoint.Basic.class);
        Session session = Mockito.mock(Session.class);

        Mockito.when(session.getBasicRemote()).thenReturn(remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);

    }
}
