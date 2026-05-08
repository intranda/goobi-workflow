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

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class MessagesWsTest {

    @Test
    public void testOnMessageKnownKeyReturnsTranslation() throws IOException {
        RemoteEndpoint.Basic remote = EasyMock.createMock(RemoteEndpoint.Basic.class);
        Session session = EasyMock.createMock(Session.class);

        Capture<String> responseCapture = EasyMock.newCapture();
        EasyMock.expect(session.getBasicRemote()).andReturn(remote);
        remote.sendText(EasyMock.capture(responseCapture));
        EasyMock.expectLastCall();
        EasyMock.replay(session, remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);

        EasyMock.verify(session, remote);
        String response = responseCapture.getValue();
        assertTrue(response.contains("\"key\":\"Content\""));
        assertTrue(response.contains("\"value\":"));
    }

    @Test
    public void testOnMessageUnknownKeyReturnsFallback() throws IOException {
        RemoteEndpoint.Basic remote = EasyMock.createMock(RemoteEndpoint.Basic.class);
        Session session = EasyMock.createMock(Session.class);

        Capture<String> responseCapture = EasyMock.newCapture();
        EasyMock.expect(session.getBasicRemote()).andReturn(remote);
        remote.sendText(EasyMock.capture(responseCapture));
        EasyMock.expectLastCall();
        EasyMock.replay(session, remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"unknownKeyThatDoesNotExist99999\"}", session);

        EasyMock.verify(session, remote);
        assertTrue(responseCapture.getValue().contains("???unknownKeyThatDoesNotExist99999???"));
    }

    @Test
    public void testOnMessageCachesLocale() throws IOException {
        RemoteEndpoint.Basic remote = EasyMock.createMock(RemoteEndpoint.Basic.class);
        Session session = EasyMock.createMock(Session.class);

        // two calls expected
        EasyMock.expect(session.getBasicRemote()).andReturn(remote).times(2);
        remote.sendText(EasyMock.anyString());
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(session, remote);

        MessagesWs ws = new MessagesWs();
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);
        ws.onMessage("{\"lang\":\"en\",\"key\":\"Content\"}", session);

        EasyMock.verify(session, remote);
    }
}
