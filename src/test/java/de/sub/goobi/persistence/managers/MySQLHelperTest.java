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
package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class MySQLHelperTest extends AbstractTest {

    // --- prepareSortField: injection protection ---

    @Test
    public void testPrepareSortFieldRejectsSqlInjectionPayload() {
        StringBuilder sql = new StringBuilder();
        String result = MySQLHelper.prepareSortField("'; DROP TABLE benutzer;--", sql);
        assertEquals("", result);
        assertFalse(sql.toString().contains("DROP"));
    }

    @Test
    public void testPrepareSortFieldRejectsUnknownField() {
        String result = MySQLHelper.prepareSortField("unknownColumn", new StringBuilder());
        assertEquals("", result);
    }

    // --- prepareSortField: user sort fields from user_all.xhtml ---

    @Test
    public void testPrepareSortFieldAcceptsUserLoginField() {
        String result = MySQLHelper.prepareSortField("benutzer.login", new StringBuilder());
        assertEquals("benutzer.login", result);
    }

    @Test
    public void testPrepareSortFieldAcceptsUserStandortField() {
        String result = MySQLHelper.prepareSortField("benutzer.standort", new StringBuilder());
        assertEquals("benutzer.standort", result);
    }

    @Test
    public void testPrepareSortFieldAcceptsUserNameField() {
        String result = MySQLHelper.prepareSortField("benutzer.nachname, benutzer.vorname", new StringBuilder());
        assertEquals("benutzer.nachname, benutzer.vorname", result);
    }

    @Test
    public void testPrepareSortFieldAcceptsUserNameFieldDescending() {
        String result = MySQLHelper.prepareSortField("benutzer.nachname, benutzer.vorname desc", new StringBuilder());
        assertEquals("benutzer.nachname, benutzer.vorname desc", result);
    }

    // --- prepareSortField: compound fields with "desc" in prefix ---
    @Test
    void testTaskSortDescending() {
        String result = MySQLHelper.prepareSortField("prioritaet desc, prozesse.ProzesseID desc", new StringBuilder());
        assertEquals("prioritaet desc, prozesse.ProzesseID desc", result);
    }

    @Test
    void testTaskSortAscending() {
        String result = MySQLHelper.prepareSortField("prioritaet desc, prozesse.ProzesseID asc", new StringBuilder());
        assertEquals("prioritaet desc, prozesse.ProzesseID asc", result);
    }

    // --- prepareSortField: already-whitelisted fields still work ---

    @Test
    public void testPrepareSortFieldAcceptsInstitutionShortName() {
        String result = MySQLHelper.prepareSortField("institution.shortName", new StringBuilder());
        assertEquals("institution.shortName", result);
    }

    @Test
    public void testCheckMariadbVersion() {
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.3.8-MariaDB-1:10.3.8+maria~xenial"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.2.13-MariaDB-10.2.13+maria~stretch"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.2.3-MariaDB-10.2.3+maria~stretch"));
        assertFalse(MySQLHelper.checkMariadbVersion("5.5.5-10.2.2-MariaDB-10.2.2+maria~stretch"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.3.14-MariaDB-1"));
        assertFalse(MySQLHelper.checkMariadbVersion("5.7.21-0ubuntu0.16.04.1"));
        assertFalse(MySQLHelper.checkMariadbVersion("10.0.34-MariaDB-0ubuntu0.16.04.1"));
        assertFalse(MySQLHelper.checkMariadbVersion("10.2.2-MariaDB-0ubuntu0.16.04.1"));
        assertTrue(MySQLHelper.checkMariadbVersion("10.3.12-MariaDB-0ubuntu0.20.04.1"));
    }

    // --- convertStringToMap: secure XML deserialization (OWASP A08 fix) ---

    @Test
    public void testConvertStringToMapParsesValidXml() {
        String xml = "<root><key1>val1</key1><key2>val2</key2></root>";
        Map<String, String> result = MySQLHelper.convertStringToMap(xml);
        assertEquals("val1", result.get("key1"));
        assertEquals("val2", result.get("key2"));
        assertEquals(2, result.size());
    }

    @Test
    public void testConvertStringToMapReturnsEmptyMapForNull() {
        Map<String, String> result = MySQLHelper.convertStringToMap(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testConvertStringToMapReturnsEmptyMapForBlank() {
        Map<String, String> result = MySQLHelper.convertStringToMap("  ");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testConvertStringToMapBlocksXxeDoctype() {
        String xxePayload = "<?xml version=\"1.0\"?><!DOCTYPE root [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><root><key>&xxe;</key></root>";
        // Should not throw, and should return empty map or map without external content
        Map<String, String> result = MySQLHelper.convertStringToMap(xxePayload);
        // Either blocked (empty/exception caught) or returned without external content
        assertNotNull(result);
        // The key value must NOT contain file system content
        if (!result.isEmpty()) {
            for (String v : result.values()) {
                assertFalse(v.contains("root:"), "XXE must be blocked — file content must not appear in result");
            }
        }
    }

    @Test
    public void testConvertRoundTrip() {
        Map<String, String> original = new java.util.LinkedHashMap<>();
        original.put("title", "Test Process");
        original.put("project", "My Project");
        String xml = MySQLHelper.convertDataToString(original);
        Map<String, String> roundTripped = MySQLHelper.convertStringToMap(xml);
        assertEquals(original, roundTripped);
    }
}
