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

package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class SessionInfoTest extends AbstractTest {

    @Test
    public void testConstructor() {
        SessionInfo fixture = new SessionInfo();
        assertNotNull(fixture);
    }

    @Test
    public void testSession() {
        HttpSession session = EasyMock.createMock(HttpSession.class);
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getSession());
        fixture.setSession(session);
        assertNotNull(fixture.getSession());
    }

    @Test
    public void testSessionId() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getSession());
        fixture.setSessionId("fixture");
        assertEquals("fixture", fixture.getSessionId());
    }

    @Test
    public void testSessionCreatedTimestamp() {
        SessionInfo fixture = new SessionInfo();
        assertEquals(0, fixture.getSessionCreatedTimestamp());
        fixture.setSessionCreatedTimestamp(1l);
        assertEquals(1l, fixture.getSessionCreatedTimestamp());
    }

    @Test
    public void testSessionCreatedFormatted() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getSessionCreatedFormatted());
        fixture.setSessionCreatedFormatted("fixture");
        assertEquals("fixture", fixture.getSessionCreatedFormatted());
    }

    @Test
    public void testLastAccessTimestamp() {
        SessionInfo fixture = new SessionInfo();
        assertEquals(0, fixture.getLastAccessTimestamp());
        fixture.setLastAccessTimestamp(1l);
        assertEquals(1l, fixture.getLastAccessTimestamp());
    }

    @Test
    public void testLastAccessFormatted() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getLastAccessFormatted());
        fixture.setLastAccessFormatted("fixture");
        assertEquals("fixture", fixture.getLastAccessFormatted());
    }

    @Test
    public void testUserId() {
        SessionInfo fixture = new SessionInfo();
        assertEquals(0, fixture.getUserId());
        fixture.setUserId(1);
        assertEquals(1, fixture.getUserId());
    }

    @Test
    public void testUserIpAddress() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getUserIpAddress());
        fixture.setUserIpAddress("fixture");
        assertEquals("fixture", fixture.getUserIpAddress());
    }

    @Test
    public void testUserName() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getUserName());
        fixture.setUserName("fixture");
        assertEquals("fixture", fixture.getUserName());
    }

    @Test
    public void testUserTimeout() {
        SessionInfo fixture = new SessionInfo();
        assertEquals(0, fixture.getUserTimeout());
        fixture.setUserTimeout(1);
        assertEquals(1, fixture.getUserTimeout());
    }

    @Test
    public void testBrowserName() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getBrowserName());
        fixture.setBrowserName("fixture");
        assertEquals("fixture", fixture.getBrowserName());
    }

    @Test
    public void testBrowserIconFileName() {
        SessionInfo fixture = new SessionInfo();
        assertNull(fixture.getBrowserIconFileName());
        fixture.setBrowserIconFileName("fixture");
        assertEquals("fixture", fixture.getBrowserIconFileName());
    }

}
