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
package de.sub.goobi.helper.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ReportLevelTest {

    @Test
    public void testValuesCount() {
        assertEquals(8, ReportLevel.values().length);
    }

    @Test
    public void testValueOfFatal() {
        assertNotNull(ReportLevel.valueOf("FATAL"));
    }

    @Test
    public void testValueOfError() {
        assertNotNull(ReportLevel.valueOf("ERROR"));
    }

    @Test
    public void testValueOfWarn() {
        assertNotNull(ReportLevel.valueOf("WARN"));
    }

    @Test
    public void testValueOfInfo() {
        assertNotNull(ReportLevel.valueOf("INFO"));
    }

    @Test
    public void testValueOfSuccess() {
        assertNotNull(ReportLevel.valueOf("SUCCESS"));
    }

    @Test
    public void testValueOfDebug() {
        assertNotNull(ReportLevel.valueOf("DEBUG"));
    }

    @Test
    public void testValueOfVerbose() {
        assertNotNull(ReportLevel.valueOf("VERBOSE"));
    }

    @Test
    public void testValueOfLudicrous() {
        assertNotNull(ReportLevel.valueOf("LUDICROUS"));
    }

    @Test
    public void testToLowerCaseFatal() {
        assertEquals("fatal", ReportLevel.FATAL.toLowerCase());
    }

    @Test
    public void testToLowerCaseError() {
        assertEquals("error", ReportLevel.ERROR.toLowerCase());
    }

    @Test
    public void testToLowerCaseWarn() {
        assertEquals("warn", ReportLevel.WARN.toLowerCase());
    }

    @Test
    public void testToLowerCaseInfo() {
        assertEquals("info", ReportLevel.INFO.toLowerCase());
    }

    @Test
    public void testToLowerCaseSuccess() {
        assertEquals("success", ReportLevel.SUCCESS.toLowerCase());
    }

    @Test
    public void testToLowerCaseDebug() {
        assertEquals("debug", ReportLevel.DEBUG.toLowerCase());
    }

    @Test
    public void testToLowerCaseVerbose() {
        assertEquals("verbose", ReportLevel.VERBOSE.toLowerCase());
    }

    @Test
    public void testToLowerCaseLudicrous() {
        assertEquals("ludicrous", ReportLevel.LUDICROUS.toLowerCase());
    }

    @Test
    public void testNameMatchesEnumConstant() {
        assertEquals("FATAL", ReportLevel.FATAL.name());
        assertEquals("SUCCESS", ReportLevel.SUCCESS.name());
        assertEquals("LUDICROUS", ReportLevel.LUDICROUS.name());
    }
}
