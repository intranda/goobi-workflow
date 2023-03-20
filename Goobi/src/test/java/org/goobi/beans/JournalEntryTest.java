/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * 
 */

package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.enums.LogType;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class JournalEntryTest extends AbstractTest {

    @Test
    public void testDefaultConstructor() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertNotNull(fixture);
        assertEquals(1, fixture.getObjectId().intValue());
        assertEquals(d, fixture.getCreationDate());
        assertEquals("user", fixture.getUserName());
        assertEquals(LogType.INFO, fixture.getType());
        assertEquals("content", fixture.getContent());
        assertEquals(EntryType.PROCESS, fixture.getEntryType());
    }

    @Test
    public void testAllArgsConstructor() {
        Date d = new Date();
        Path path = Paths.get("/some/filename");
        JournalEntry fixture = new JournalEntry(1, 1, d, "user", LogType.FILE, "content", "filename", EntryType.PROCESS, path);
        assertNotNull(fixture);
        assertEquals(1, fixture.getObjectId().intValue());
        assertEquals(d, fixture.getCreationDate());
        assertEquals("user", fixture.getUserName());
        assertEquals(LogType.FILE, fixture.getType());
        assertEquals("content", fixture.getContent());
        assertEquals("filename", fixture.getFilename());
        assertEquals(EntryType.PROCESS, fixture.getEntryType());
        assertEquals(path.toString(), fixture.getFile().toString());
    }

    @Test
    public void testId() {
        JournalEntry fixture = new JournalEntry(1, new Date(), "user", LogType.INFO, "content", EntryType.PROCESS);
        assertNull(fixture.getId());
        fixture.setId(1);
        assertEquals(1, fixture.getId().intValue());
    }

    @Test
    public void testObjectId() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals(1, fixture.getObjectId().intValue());
        fixture.setObjectId(2);
        assertEquals(2, fixture.getObjectId().intValue());
    }

    @Test
    public void testCreationDate() {
        Date d = new Date();
        Date d2 = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals(d, fixture.getCreationDate());
        fixture.setCreationDate(d2);
        assertEquals(d2, fixture.getCreationDate());
    }

    @Test
    public void testUsername() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals("user", fixture.getUserName());
        fixture.setUserName("other");
        assertEquals("other", fixture.getUserName());
    }

    @Test
    public void testType() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals(LogType.INFO, fixture.getType());
        fixture.setType(LogType.ERROR);
        assertEquals(LogType.ERROR, fixture.getType());
    }

    @Test
    public void testContent() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals("content", fixture.getContent());
        fixture.setContent("other");
        assertEquals("other", fixture.getContent());
    }

    @Test
    public void testFilename() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertNull(fixture.getFilename());
        fixture.setFilename("other");
        assertEquals("other", fixture.getFilename());
    }

    @Test
    public void testEntryType() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertEquals(EntryType.PROCESS, fixture.getEntryType());
        fixture.setEntryType(EntryType.USER);
        assertEquals(EntryType.USER, fixture.getEntryType());
    }

    @Test
    public void testFile() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertNull(fixture.getFile());
        Path path = Paths.get("/some/file");
        fixture.setFile(path);
        assertEquals(path.toString(), fixture.getFile().toString());
    }

    @Test
    public void testFormattedCreationDate() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        assertNotNull(fixture.getFormattedCreationDate());
    }

    @Test
    public void testBasename() {
        Date d = new Date();
        Path path = Paths.get("/some/filename");
        JournalEntry fixture = new JournalEntry(1, 1, d, "user", LogType.FILE, "content", "filename", EntryType.PROCESS, path);
        assertEquals("filename", fixture.getBasename());
    }

    @Test
    public void testExternalFile() {
        Date d = new Date();
        Path path = Paths.get("/some/filename");
        JournalEntry fixture = new JournalEntry(1, 1, d, "user", LogType.FILE, "content", "filename", EntryType.PROCESS, path);
        assertEquals("filename", fixture.getBasename());
        assertTrue(fixture.isExternalFile());
        path = Paths.get("/some/intern/filename");
        fixture.setFile(path);
        fixture.setFilename(path.toString());
        assertFalse(fixture.isExternalFile());
    }

    @Test
    public void testFormattedContent() {
        Date d = new Date();
        JournalEntry fixture = new JournalEntry(1, d, "user", LogType.INFO, "content", EntryType.PROCESS);
        fixture.setContent("content \n more");
        assertEquals("content \n more", fixture.getContent());
        assertEquals("content <br/> more", fixture.getFormattedContent());

    }

    @Test
    public void testEntryTypes() {
        assertEquals(EntryType.PROCESS, EntryType.getByTitle("process"));
        assertEquals(EntryType.USER, EntryType.getByTitle("user"));
        assertEquals(EntryType.INSTITUTION, EntryType.getByTitle("institution"));
        assertEquals(EntryType.PROCESS, EntryType.getByTitle("something"));
    }

}
