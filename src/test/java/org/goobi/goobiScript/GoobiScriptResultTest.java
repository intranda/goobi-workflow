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
package org.goobi.goobiScript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.production.enums.GoobiScriptResultType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptResultTest extends AbstractTest {

    @Test
    public void testConstructorWithStarttimeSetsFields() {
        Map<String, String> params = new HashMap<>();
        GoobiScriptResult result = new GoobiScriptResult(42, "action:test", params, "testUser", 12345L);
        assertEquals(Integer.valueOf(42), result.getProcessId());
        assertEquals("action:test", result.getCommand());
        assertEquals("testUser", result.getUsername());
        assertEquals(GoobiScriptResultType.WAITING, result.getResultType());
        assertEquals("", result.getResultMessage());
        assertEquals("", result.getProcessTitle());
        assertEquals(12345L, result.getStarttime());
    }

    @Test
    public void testConstructorWithoutStarttimeSetsFields() {
        Map<String, String> params = new HashMap<>();
        GoobiScriptResult result = new GoobiScriptResult(7, "action:other", params, "anotherUser");
        assertEquals(Integer.valueOf(7), result.getProcessId());
        assertEquals("action:other", result.getCommand());
        assertEquals("anotherUser", result.getUsername());
        assertEquals(GoobiScriptResultType.WAITING, result.getResultType());
    }

    @Test
    public void testDefaultTimestampIsNotNull() {
        Map<String, String> params = new HashMap<>();
        GoobiScriptResult result = new GoobiScriptResult(1, "cmd", params, "user", 0L);
        assertNotNull(result.getTimestamp());
    }

    @Test
    public void testUpdateTimestampDoesNotThrow() {
        Map<String, String> params = new HashMap<>();
        GoobiScriptResult result = new GoobiScriptResult(1, "cmd", params, "user", 0L);
        result.updateTimestamp();
        assertNotNull(result.getTimestamp());
    }

    @Test
    public void testGetFormattedTimestamp() {
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getDateAsFormattedString(EasyMock.anyObject())).andReturn("2024-01-01 00:00:00").anyTimes();
        PowerMock.replayAll();

        Map<String, String> params = new HashMap<>();
        GoobiScriptResult result = new GoobiScriptResult(1, "cmd", params, "user", 0L);
        assertEquals("2024-01-01 00:00:00", result.getFormattedTimestamp());
    }
}
