package de.sub.goobi.forms;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.junit.Test;

public class AdditionalFieldTest {

    @Test
    public void testConstructor() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        assertNotNull(af);
    }

    @Test
    public void testInitStart() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setInitStart(null);
        af.setInitStart("start");
        assertEquals("start", af.getInitStart());
    }

    @Test
    public void testInitEnd() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setInitEnd(null);
        af.setInitEnd("end");
        assertEquals("end", af.getInitEnd());
    }

    @Test
    public void testTitle() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setTitel("title");
        assertEquals("title", af.getTitel());
    }

    @Test
    public void testValue() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setWert("Value");
        assertEquals("Value", af.getWert());
    }

    @Test
    public void testFrom() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        assertNotNull(af.getFrom());
        assertEquals("prozess", af.getFrom());
        af.setFrom("from");
        assertEquals("from", af.getFrom());
    }

    @Test
    public void testSelectList() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        List<SelectItem> list = new ArrayList<SelectItem>();
        list.add(new SelectItem("value", "label"));
        af.setSelectList(list);
        assertEquals(list, af.getSelectList());
    }

    @Test
    public void testRequired() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        assertFalse(af.isRequired());
        af.setRequired(true);
        assertTrue(af.isRequired());
    }

    @Test
    public void testUghbinding() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        assertFalse(af.isUghbinding());
        af.setUghbinding(true);
        assertTrue(af.isUghbinding());
    }

    @Test
    public void testDocstruct() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setDocstruct(null);
        assertEquals("topstruct", af.getDocstruct());

        af.setDocstruct("different value");
        assertEquals("different value", af.getDocstruct());
        
    }

    @Test
    public void testMetadata() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        
        af.setMetadata("value");
        assertEquals("value", af.getMetadata());
    }

    @Test
    public void testIsdoctype() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        af.setIsdoctype(null);
        assertNotNull(af.getIsdoctype());
        
        af.setIsdoctype("value");
        assertEquals("value", af.getIsdoctype());
    }

    @Test
    public void testIsnotdoctype() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        
        af.setIsnotdoctype(null);
        assertNotNull(af.getIsnotdoctype());
        
        af.setIsnotdoctype("value");
        assertEquals("value", af.getIsnotdoctype());
    }

    @Test
    public void testShowDependingOnDoctype() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        
        af.setIsnotdoctype("");
        af.setIsdoctype("");
        assertTrue(af.getShowDependingOnDoctype());
        
        pfk.setDocType("type");
        af.setIsdoctype("other");
        assertFalse(af.getShowDependingOnDoctype());
        
        pfk.setDocType("not");
        af.setIsnotdoctype("not");
        assertFalse(af.getShowDependingOnDoctype());

        pfk.setDocType("type");
        af.setIsdoctype("type");
        assertTrue(af.getShowDependingOnDoctype());
        
    }

    @Test
    public void testAutogenerated() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        
        assertFalse(af.getAutogenerated());
        af.setAutogenerated(true);
        assertTrue(af.getAutogenerated());
    }

    @Test
    public void testMultiselect() {
        ProzesskopieForm pfk = new ProzesskopieForm();
        AdditionalField af = new AdditionalField(pfk);
        
        assertTrue(af.isMultiselect());
        af.setMultiselect(false);
        assertFalse(af.isMultiselect());
    }
    
}
