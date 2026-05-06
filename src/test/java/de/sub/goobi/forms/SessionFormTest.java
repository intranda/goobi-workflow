package de.sub.goobi.forms;

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
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class SessionFormTest extends AbstractTest {

    @Test
    public void testConstructor() {
        SessionForm form = new SessionForm();
        assertNotNull(form);
    }

    @Test
    public void testGetNumberOfSessions() {
        SessionForm form = new SessionForm();
        assertEquals(0, form.getNumberOfSessions());
    }

    @Test
    public void testGetNumberOfRealUserSessions() {
        SessionForm form = new SessionForm();
        assertEquals(0, form.getNumberOfRealUserSessions());
    }

    @Test
    public void testGetCurrentTime() {
        SessionForm form = new SessionForm();
        String time = form.getCurrentTime();
        assertNotNull(time);
        assertTrue(Pattern.matches("\\d{2}:\\d{2}:\\d{2}", time));
    }

    @Test
    public void testGetDate() {
        SessionForm form = new SessionForm();
        String date = form.getDate();
        assertNotNull(date);
        assertTrue(date.length() > 0);
    }

    @Test
    public void testGetLogoutMessageDefault() {
        SessionForm form = new SessionForm();
        assertEquals("", form.getLogoutMessage());
    }

    @Test
    public void testSendLogoutMessage() {
        SessionForm form = new SessionForm();
        assertEquals("admin", form.sendLogoutMessage());
    }

    @Test
    public void testLoggedOutConstant() {
        assertEquals(" - ausgeloggt - ", SessionForm.LOGGED_OUT);
    }

    @Test
    public void testNotLoggedInConstant() {
        assertEquals(" - ", SessionForm.NOT_LOGGED_IN);
    }

    @Test
    public void testGetSessionInfoByIdReturnsNullWhenEmpty() {
        SessionForm form = new SessionForm();
        assertNotNull(form);
        // no sessions added, lookup by any id returns null
        assertEquals(null, form.getSessionInfoById("nonexistent-id"));
    }

}
