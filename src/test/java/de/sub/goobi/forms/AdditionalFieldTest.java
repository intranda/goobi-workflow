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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import jakarta.faces.model.SelectItem;

public class AdditionalFieldTest extends AbstractTest {

    @Test
    public void testConstructor() {
        AdditionalField af = new AdditionalField();
        assertNotNull(af);
    }

    @Test
    public void testInitStart() {
        AdditionalField af = new AdditionalField();
        af.setInitStart(null);
        af.setInitStart("start");
        assertEquals("start", af.getInitStart());
    }

    @Test
    public void testInitEnd() {
        AdditionalField af = new AdditionalField();
        af.setInitEnd(null);
        af.setInitEnd("end");
        assertEquals("end", af.getInitEnd());
    }

    @Test
    public void testTitle() {
        AdditionalField af = new AdditionalField();
        af.setTitel("title");
        assertEquals("title", af.getTitel());
    }

    @Test
    public void testValue() {
        AdditionalField af = new AdditionalField();
        af.setWert("Value");
        assertEquals("Value", af.getWert());
    }

    @Test
    public void testProperty() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isProperty());
        af.setProperty(true);
        assertTrue(af.isProperty());
    }

    @Test
    public void testSelectList() {
        AdditionalField af = new AdditionalField();
        List<SelectItem> list = new ArrayList<>();
        list.add(new SelectItem("value", "label"));
        af.setSelectList(list);
        assertEquals(list, af.getSelectList());
    }

    @Test
    public void testRequired() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isRequired());
        af.setRequired(true);
        assertTrue(af.isRequired());
    }

    @Test
    public void testUghbinding() {
        AdditionalField af = new AdditionalField();
        assertFalse(af.isUghbinding());
        af.setUghbinding(true);
        assertTrue(af.isUghbinding());
    }

    @Test
    public void testDocstruct() {
        AdditionalField af = new AdditionalField();
        af.setDocstruct(null);
        assertEquals("topstruct", af.getDocstruct());

        af.setDocstruct("different value");
        assertEquals("different value", af.getDocstruct());

    }

    @Test
    public void testMetadata() {
        AdditionalField af = new AdditionalField();

        af.setMetadata("value");
        assertEquals("value", af.getMetadata());
    }

    @Test
    public void testIsdoctype() {
        AdditionalField af = new AdditionalField();
        af.setIsdoctype(null);
        assertNotNull(af.getIsdoctype());

        af.setIsdoctype("value");
        assertEquals("value", af.getIsdoctype());
    }

    @Test
    public void testIsnotdoctype() {
        AdditionalField af = new AdditionalField();

        af.setIsnotdoctype(null);
        assertNotNull(af.getIsnotdoctype());

        af.setIsnotdoctype("value");
        assertEquals("value", af.getIsnotdoctype());
    }

    @Test
    public void testShowDependingOnDoctype() {
        AdditionalField af = new AdditionalField();

        af.setIsnotdoctype("");
        af.setIsdoctype("");
        assertTrue(af.getShowDependingOnDoctype(""));

        af.setIsdoctype("other");
        assertFalse(af.getShowDependingOnDoctype("type"));

        af.setIsnotdoctype("not");
        assertFalse(af.getShowDependingOnDoctype("not"));

        af.setIsdoctype("type");
        assertTrue(af.getShowDependingOnDoctype("type"));
    }

    @Test
    public void testAutogenerated() {
        AdditionalField af = new AdditionalField();

        assertFalse(af.getAutogenerated());
        af.setAutogenerated(true);
        assertTrue(af.getAutogenerated());
    }

    @Test
    public void testMultiselect() {
        AdditionalField af = new AdditionalField();

        assertFalse(af.isMultiselect());
        af.setMultiselect(true);
        assertTrue(af.isMultiselect());
    }

    @Test
    public void testFieldType() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getFieldType());
        af.setFieldType("type");
        assertEquals("type", af.getFieldType());
    }

    @Test
    public void testPattern() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getPattern());
        af.setPattern("pattern");
        assertEquals("pattern", af.getPattern());
    }

    @Test
    public void testValueAsDateTime() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getValueAsDateTime());
    }

    @Test
    public void testValueAsDate() {
        AdditionalField af = new AdditionalField();
        assertNull(af.getValueAsDate());
        af.setValueAsDate(LocalDate.of(2000, 1, 1));
        assertEquals(2000, af.getValueAsDate().getYear());
    }

}
