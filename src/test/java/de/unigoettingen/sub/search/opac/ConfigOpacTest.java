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
package de.unigoettingen.sub.search.opac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class ConfigOpacTest extends AbstractTest {

    @BeforeEach
    public void resetSingleton() throws Exception {
        Field instanceField = ConfigOpac.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    public void testGetInstance() {
        ConfigOpac instance = ConfigOpac.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void testGetInstanceReturnsSameObject() {
        ConfigOpac first = ConfigOpac.getInstance();
        ConfigOpac second = ConfigOpac.getInstance();
        assertEquals(first, second);
    }

    @Test
    public void testGetSearchFieldMap() {
        Map<String, String> fields = ConfigOpac.getInstance().getSearchFieldMap();
        assertNotNull(fields);
        assertFalse(fields.isEmpty());
        assertTrue(fields.containsKey("ISBN"));
        assertTrue(fields.containsKey("ISSN"));
        assertTrue(fields.containsKey("Identifier"));
    }

    @Test
    public void testGetAllDoctypeTitles() {
        List<String> titles = ConfigOpac.getInstance().getAllDoctypeTitles();
        assertNotNull(titles);
        assertFalse(titles.isEmpty());
        assertTrue(titles.contains("monograph"));
        assertTrue(titles.contains("periodical"));
    }

    @Test
    public void testGetDoctypeByNameMonograph() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByName("monograph");
        assertNotNull(doctype);
        assertEquals("monograph", doctype.getTitle());
        assertEquals("Monograph", doctype.getRulesetType());
        assertEquals("Monographie", doctype.getTifHeaderType());
        assertFalse(doctype.isPeriodical());
        assertFalse(doctype.isMultiVolume());
        assertFalse(doctype.isContainedWork());
        assertTrue(doctype.getMappings().contains("Aa"));
        assertTrue(doctype.getMappings().contains("Monograph"));
    }

    @Test
    public void testGetDoctypeByNamePeriodical() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByName("periodical");
        assertNotNull(doctype);
        assertEquals("periodical", doctype.getTitle());
        assertTrue(doctype.isPeriodical());
        assertFalse(doctype.isMultiVolume());
        assertEquals("PeriodicalVolume", doctype.getRulesetChildType());
    }

    @Test
    public void testGetDoctypeByNameUnknownReturnsNull() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByName("unknown_type");
        assertNull(doctype);
    }

    @Test
    public void testGetAllDoctypes() {
        List<ConfigOpacDoctype> doctypes = ConfigOpac.getInstance().getAllDoctypes();
        assertNotNull(doctypes);
        assertEquals(2, doctypes.size());
    }

    @Test
    public void testGetCatalogueByNameKxp() {
        ConfigOpacCatalogue cat = ConfigOpac.getInstance().getCatalogueByName("KXP");
        assertNotNull(cat);
        assertEquals("KXP", cat.getTitle());
        assertEquals("kxp.k10plus.de", cat.getAddress());
        assertEquals("2.1", cat.getDatabase());
        assertEquals(80, cat.getPort());
        assertEquals("utf-8", cat.getCharset());
        assertFalse(cat.getCbs().isEmpty());
        assertTrue(cat.getCbs().contains("UCNF"));
    }

    @Test
    public void testGetCatalogueByNameUnknownReturnsNull() {
        ConfigOpacCatalogue cat = ConfigOpac.getInstance().getCatalogueByName("NON_EXISTENT");
        assertNull(cat);
    }

    @Test
    public void testGetDoctypeByMappingAa() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByMapping("Aa", "KXP");
        assertNotNull(doctype);
        assertEquals("monograph", doctype.getTitle());
    }

    @Test
    public void testGetDoctypeByMappingPeriodical() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByMapping("Ab", "KXP");
        assertNotNull(doctype);
        assertEquals("periodical", doctype.getTitle());
    }

    @Test
    public void testGetDoctypeByMappingUnknownReturnsNull() {
        ConfigOpacDoctype doctype = ConfigOpac.getInstance().getDoctypeByMapping("ZZZZZ", "KXP");
        assertNull(doctype);
    }

}
