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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EasydbSearchFieldTest {

    private EasydbSearchField field;

    @Before
    public void setUp() {
        field = new EasydbSearchField();
    }

    @Test
    public void testDefaultValues() {
        assertNotNull(field);
        assertNull(field.getType());
        assertNull(field.getBool());
        assertNull(field.getMode());
        assertNull(field.getString());
        assertNull(field.getFields());
        assertFalse(field.isPhrase());
        assertNull(field.getIn());
        assertNull(field.getField());
        assertNull(field.getFrom());
        assertNull(field.getTo());
        assertNotNull(field.getOverrideValueList());
        assertTrue(field.getOverrideValueList().isEmpty());
        assertEquals("numeric", field.getFieldType());
    }

    @Test
    public void testSetType() {
        field.setType("match");
        assertEquals("match", field.getType());
    }

    @Test
    public void testSetBool() {
        field.setBool("must");
        assertEquals("must", field.getBool());
    }

    @Test
    public void testSetMode() {
        field.setMode("fulltext");
        assertEquals("fulltext", field.getMode());
    }

    @Test
    public void testSetString() {
        field.setString("test value");
        assertEquals("test value", field.getString());
    }

    @Test
    public void testSetFields() {
        List<String> fields = Arrays.asList("artefact.title", "artefact.name");
        field.setFields(fields);
        assertEquals(2, field.getFields().size());
        assertTrue(field.getFields().contains("artefact.title"));
    }

    @Test
    public void testSetPhrase() {
        field.setPhrase(true);
        assertTrue(field.isPhrase());
    }

    @Test
    public void testSetIn() {
        List<Object> in = Arrays.asList("val1", "val2");
        field.setIn(in);
        assertEquals(2, field.getIn().size());
    }

    @Test
    public void testSetFromTo() {
        field.setFrom("2020-01-01");
        field.setTo("2024-12-31");
        assertEquals("2020-01-01", field.getFrom());
        assertEquals("2024-12-31", field.getTo());
    }

    @Test
    public void testSetOverrideValueList() {
        List<String> overrides = Arrays.asList("override1", "override2");
        field.setOverrideValueList(overrides);
        assertEquals(2, field.getOverrideValueList().size());
    }

    @Test
    public void testSetFieldType() {
        field.setFieldType("text");
        assertEquals("text", field.getFieldType());
    }
}
