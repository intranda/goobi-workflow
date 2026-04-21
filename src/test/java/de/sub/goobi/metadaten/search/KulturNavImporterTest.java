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
package de.sub.goobi.metadaten.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.intranda.digiverso.normdataimporter.model.NormDataRecord;

public class KulturNavImporterTest {

    @Test
    public void testGetUuidFromUrl() {
        String url = "https://kulturnav.org/some/path/abc-uuid-123";
        String uuid = KulturNavImporter.getUuidFromUrl(url);
        assertEquals("abc-uuid-123", uuid);
    }

    @Test
    public void testGetUuidFromUrlWithTrailingSlash() {
        // url.lastIndexOf('/') is at end, substring is empty
        String url = "https://kulturnav.org/";
        String uuid = KulturNavImporter.getUuidFromUrl(url);
        assertEquals("", uuid);
    }

    @Test
    public void testGetUuidFromUrlNull() {
        String result = KulturNavImporter.getUuidFromUrl(null);
        assertEquals(null, result);
    }

    @Test
    public void testGetUuidFromUrlBlank() {
        String result = KulturNavImporter.getUuidFromUrl("");
        assertEquals("", result);
    }

    @Test
    public void testParseSearchStringSimple() {
        String result = KulturNavImporter.parseSearchString("Mozart");
        assertEquals("Mozart", result);
    }

    @Test
    public void testParseSearchStringWithSpaces() {
        String result = KulturNavImporter.parseSearchString("Wolfgang Amadeus Mozart");
        assertEquals("Wolfgang%20Amadeus%20Mozart", result);
    }

    @Test
    public void testParseSearchStringNull() {
        String result = KulturNavImporter.parseSearchString(null);
        assertEquals(null, result);
    }

    @Test
    public void testParseSearchStringBlank() {
        String result = KulturNavImporter.parseSearchString("");
        assertEquals("", result);
    }

    @Test
    public void testParseSourceWithoutComma() {
        String result = KulturNavImporter.parseSource("entityType:Person");
        assertEquals("entityType:Person,", result);
    }

    @Test
    public void testParseSourceAlreadyEndsWithComma() {
        String result = KulturNavImporter.parseSource("entityType:Person,");
        assertEquals("entityType:Person,", result);
    }

    @Test
    public void testParseSourceBlank() {
        String result = KulturNavImporter.parseSource("");
        assertEquals("", result);
    }

    @Test
    public void testParseSourceNull() {
        String result = KulturNavImporter.parseSource(null);
        assertEquals("", result);
    }

    @Test
    public void testConstructSearchUrl() {
        String url = KulturNavImporter.constructSearchUrl("Mozart", "entityType:Person");
        assertNotNull(url);
        assertTrue(url.startsWith(KulturNavImporter.SUMMARY_URL));
        assertTrue(url.contains("entityType:Person,"));
        assertTrue(url.contains("compoundName:Mozart"));
    }

    @Test
    public void testCreateNormDataRecord() {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("name", "Test Name");
        jsonMap.put("caption", "Test Caption");
        jsonMap.put("uuid", "uuid-12345");

        NormDataRecord record = KulturNavImporter.createNormDataRecord(jsonMap);
        assertNotNull(record);
        assertNotNull(record.getNormdataList());
        // should have entries for NORM_LABEL, NORM_ALTLABEL, URI
        assertTrue(record.getNormdataList().stream().anyMatch(nd -> "NORM_LABEL".equals(nd.getKey())));
        assertTrue(record.getNormdataList().stream().anyMatch(nd -> "URI".equals(nd.getKey())));
        assertNotNull(record.getValueList());
    }

    @Test
    public void testImportNormDataWithBadUrlReturnsEmpty() {
        // file:// URL to nonexistent file → IOException caught → returns empty list
        List<NormDataRecord> result = KulturNavImporter.importNormData("file:///this/path/does/not/exist/kulturnav.json");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
