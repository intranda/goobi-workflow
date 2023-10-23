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
package io.goobi.workflow.harvester.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import io.goobi.workflow.harvester.beans.Record;

public class ExportHistoryEntryTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testRecordConstructor() {
        Record rec = new Record();
        rec.setId(1);
        rec.setIdentifier("1234");
        rec.setTitle("title");
        rec.setRepositoryId(1);

        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertEquals(1, fixture.getRecordId().intValue());
        assertEquals("1234", fixture.getRecordIdentifier());
        assertEquals("title", fixture.getRecordTitle());
        assertEquals(1, fixture.getRepositoryId().intValue());
    }

    @Test
    public void testArgsConstructor() {
        ExportHistoryEntry fixture = new ExportHistoryEntry(1, "1234", "title", 1, "DONE", "message");
        assertNotNull(fixture);
        assertEquals(1, fixture.getRecordId().intValue());
        assertEquals("1234", fixture.getRecordIdentifier());
        assertEquals("title", fixture.getRecordTitle());
        assertEquals(1, fixture.getRepositoryId().intValue());
        assertEquals("DONE", fixture.getStatus());
    }

    @Test
    public void testAllArgsConstructor() {
        ExportHistoryEntry fixture = new ExportHistoryEntry(1, new Timestamp(1l), 1, "1234", "title", 1, "DONE", "message");
        assertNotNull(fixture);
        assertEquals(1, fixture.getRecordId().intValue());
        assertEquals("1234", fixture.getRecordIdentifier());
        assertEquals("title", fixture.getRecordTitle());
        assertEquals(1, fixture.getRepositoryId().intValue());
        assertEquals("DONE", fixture.getStatus());
        assertEquals("message", fixture.getMessage());
        assertTrue(fixture.getTimestamp().endsWith("001"));

        fixture = new ExportHistoryEntry(null, null, null, null, null, null, null, null);
        assertEquals("", fixture.getTimestamp());
    }

    @Test
    public void testReExportRecord() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertEquals("", fixture.reExportRecord());
    }

    @Test
    public void testId() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getId());
        fixture.setId(1);
        assertEquals(1, fixture.getId().intValue());
    }

    @Test
    public void testTimestamp() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getTimestamp());
        fixture.setTimestamp("fixture");
        assertEquals("fixture", fixture.getTimestamp());
    }

    @Test
    public void testRecordId() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getRecordId());
        fixture.setRecordId(1);
        assertEquals(1, fixture.getRecordId().intValue());
    }

    @Test
    public void testRecordIdentifier() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getRecordIdentifier());
        fixture.setRecordIdentifier("fixture");
        assertEquals("fixture", fixture.getRecordIdentifier());
    }

    @Test
    public void testRecordTitle() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getRecordTitle());
        fixture.setRecordTitle("fixture");
        assertEquals("fixture", fixture.getRecordTitle());
    }

    @Test
    public void testRepositoryId() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getRepositoryId());
        fixture.setRepositoryId(1);
        assertEquals(1, fixture.getRepositoryId().intValue());
    }

    @Test
    public void testStatus() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getStatus());
        fixture.setStatus("fixture");
        assertEquals("fixture", fixture.getStatus());
    }

    @Test
    public void testMessage() {
        Record rec = new Record();
        ExportHistoryEntry fixture = new ExportHistoryEntry(rec);
        assertNotNull(fixture);
        assertNull(fixture.getMessage());
        fixture.setMessage("fixture");
        assertEquals("fixture", fixture.getMessage());
    }

}
