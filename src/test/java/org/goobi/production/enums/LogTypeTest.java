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
package org.goobi.production.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class LogTypeTest {

    @Test
    public void testValuesCount() {
        assertEquals(8, LogType.values().length);
    }

    @Test
    public void testErrorTitle() {
        assertEquals("error", LogType.ERROR.getTitle());
    }

    @Test
    public void testWarnTitle() {
        assertEquals("warn", LogType.WARN.getTitle());
    }

    @Test
    public void testInfoTitle() {
        assertEquals("info", LogType.INFO.getTitle());
    }

    @Test
    public void testDebugTitle() {
        assertEquals("debug", LogType.DEBUG.getTitle());
    }

    @Test
    public void testUserTitle() {
        assertEquals("user", LogType.USER.getTitle());
    }

    @Test
    public void testStatusTitle() {
        assertEquals("status", LogType.STATUS.getTitle());
    }

    @Test
    public void testFileTitle() {
        assertEquals("file", LogType.FILE.getTitle());
    }

    @Test
    public void testImportantUserTitle() {
        assertEquals("important", LogType.IMPORTANT_USER.getTitle());
    }

    @Test
    public void testGetByTitleError() {
        assertEquals(LogType.ERROR, LogType.getByTitle("error"));
    }

    @Test
    public void testGetByTitleInfo() {
        assertEquals(LogType.INFO, LogType.getByTitle("info"));
    }

    @Test
    public void testGetByTitleUser() {
        assertEquals(LogType.USER, LogType.getByTitle("user"));
    }

    @Test
    public void testGetByTitleImportantUser() {
        assertEquals(LogType.IMPORTANT_USER, LogType.getByTitle("important"));
    }

    @Test
    public void testGetByTitleUnknownReturnsNull() {
        assertNull(LogType.getByTitle("unknown"));
    }
}
