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

package io.goobi.workflow.harvester.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class RecordTest extends AbstractTest {

    @Test
    public void testDefaultConstructor() {
        Record rec = new Record();
        assertNotNull(rec);
    }

    @Test
    public void testId() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getId());
        rec.setId(1);
        assertEquals(1, rec.getId().intValue());
    }

    @Test
    public void testTimestamp() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getTimestamp());
        rec.setTimestamp("fixture");
        assertEquals("fixture", rec.getTimestamp());
    }

    @Test
    public void testIdentifier() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getIdentifier());
        rec.setIdentifier("fixture");
        assertEquals("fixture", rec.getIdentifier());
    }

    @Test
    public void testRepositoryTimestamp() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getRepositoryTimestamp());
        rec.setRepositoryTimestamp("fixture");
        assertEquals("fixture", rec.getRepositoryTimestamp());
    }

    @Test
    public void testTitle() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getTitle());
        rec.setTitle("fixture");
        assertEquals("fixture", rec.getTitle());
    }

    @Test
    public void testCreator() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getCreator());
        rec.setCreator("fixture");
        assertEquals("fixture", rec.getCreator());
    }

    @Test
    public void testSpecList() {
        Record rec = new Record();
        assertNotNull(rec);
        assertTrue(rec.getSetSpecList().isEmpty());
        List<String> specs = new ArrayList<>();
        specs.add("fixture");
        rec.setSetSpecList(specs);
        assertEquals("fixture", rec.getSetSpecList().get(0));
    }

    @Test
    public void testSubquery() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getSubquery());
        rec.setSubquery("fixture");
        assertEquals("fixture", rec.getSubquery());
    }

    @Test
    public void testJobId() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getJobId());
        rec.setJobId(1);
        assertEquals(1, rec.getJobId().intValue());
    }

    @Test
    public void testSource() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getSource());
        rec.setSource("fixture");
        assertEquals("fixture", rec.getSource());
    }

    @Test
    public void testExported() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getExported());
        rec.setExported("fixture");
        assertEquals("fixture", rec.getExported());
    }

    @Test
    public void testExportedDatestamp() {
        Record rec = new Record();
        assertNotNull(rec);
        assertNull(rec.getExportedDatestamp());
        Date d = new Date();
        rec.setExportedDatestamp(d);
        assertSame(d, rec.getExportedDatestamp());
    }

    @Test
    public void testArgsConstructor() {
        Record rec = new Record(1, new Timestamp(1l), "identifier", new Date(), "title", "creator", 1, "SpecAsString", 1, "source", "exported",
                new Date(), "subquery");
        assertNotNull(rec);
        assertEquals(1, rec.getId().intValue());
    }

    @Test
    public void testGetSetSpec() {
        Record rec = new Record();
        List<String> specs = new ArrayList<>();
        specs.add("fix");
        specs.add("ture");
        rec.setSetSpecList(specs);
        assertEquals("fix, ture", rec.getSetSpec());
    }
}
