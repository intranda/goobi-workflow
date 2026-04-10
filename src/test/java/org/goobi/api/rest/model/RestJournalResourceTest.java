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
package org.goobi.api.rest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.goobi.beans.JournalEntry;
import org.goobi.production.enums.LogType;
import org.junit.Test;

public class RestJournalResourceTest {

    @Test
    public void testNoArgConstructor() {
        RestJournalResource resource = new RestJournalResource();
        assertNotNull(resource);
        assertNull(resource.getId());
        assertNull(resource.getProcessId());
        assertNull(resource.getCreationDate());
        assertNull(resource.getUserName());
        assertNull(resource.getType());
        assertNull(resource.getMessage());
        assertNull(resource.getFilename());
    }

    @Test
    public void testConstructorFromJournalEntry() {
        Date now = new Date();
        JournalEntry entry = new JournalEntry(1, now, "tester", LogType.INFO, "test message", JournalEntry.EntryType.PROCESS);
        entry.setId(42);
        entry.setFilename("/some/path/file.txt");

        RestJournalResource resource = new RestJournalResource(entry);

        assertEquals(Integer.valueOf(42), resource.getId());
        assertEquals(Integer.valueOf(1), resource.getProcessId());
        assertEquals(now, resource.getCreationDate());
        assertEquals("tester", resource.getUserName());
        assertEquals("info", resource.getType());
        assertEquals("test message", resource.getMessage());
        assertEquals("/some/path/file.txt", resource.getFilename());
    }

    @Test
    public void testSettersAndGetters() {
        RestJournalResource resource = new RestJournalResource();
        Date now = new Date();
        resource.setId(5);
        resource.setProcessId(10);
        resource.setCreationDate(now);
        resource.setUserName("admin");
        resource.setType("error");
        resource.setMessage("something failed");
        resource.setFilename("report.pdf");

        assertEquals(Integer.valueOf(5), resource.getId());
        assertEquals(Integer.valueOf(10), resource.getProcessId());
        assertEquals(now, resource.getCreationDate());
        assertEquals("admin", resource.getUserName());
        assertEquals("error", resource.getType());
        assertEquals("something failed", resource.getMessage());
        assertEquals("report.pdf", resource.getFilename());
    }
}
