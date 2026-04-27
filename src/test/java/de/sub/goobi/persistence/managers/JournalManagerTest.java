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
package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.enums.LogType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ JournalMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class JournalManagerTest extends AbstractTest {

    private JournalEntry sampleEntry;
    private List<JournalEntry> sampleEntries;

    @Before
    public void setUp() throws Exception {
        sampleEntry = new JournalEntry(1, new Date(), "testUser", LogType.INFO, "Test content", EntryType.PROCESS);
        sampleEntry.setId(1);
        sampleEntries = new ArrayList<>();
        sampleEntries.add(sampleEntry);

        PowerMock.mockStatic(JournalMysqlHelper.class);
        EasyMock.expect(JournalMysqlHelper.saveLogEntry(EasyMock.anyObject())).andReturn(sampleEntry).anyTimes();
        JournalMysqlHelper.deleteLogEntry(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        JournalMysqlHelper.deleteAllJournalEntries(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(JournalMysqlHelper.getLogEntries(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(sampleEntries).anyTimes();
        EasyMock.expect(JournalMysqlHelper.getJournalEntryById(EasyMock.anyInt())).andReturn(sampleEntry).anyTimes();
        PowerMock.replay(JournalMysqlHelper.class);
    }

    @Test
    public void testSaveJournalEntry() {
        JournalManager.saveJournalEntry(sampleEntry);
    }

    @Test
    public void testDeleteJournalEntry() {
        JournalManager.deleteJournalEntry(sampleEntry);
    }

    @Test
    public void testDeleteAllJournalEntries() {
        JournalManager.deleteAllJournalEntries(1, EntryType.PROCESS);
    }

    @Test
    public void testGetLogEntriesForProcess() {
        List<JournalEntry> result = JournalManager.getLogEntriesForProcess(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetLogEntriesForInstitution() {
        List<JournalEntry> result = JournalManager.getLogEntriesForInstitution(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetLogEntriesForUser() {
        List<JournalEntry> result = JournalManager.getLogEntriesForUser(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetLogEntriesForProject() {
        List<JournalEntry> result = JournalManager.getLogEntriesForProject(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetJournalEntryById() {
        JournalEntry result = JournalManager.getJournalEntryById(1);
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetHitSizeThrowsUnsupported() throws Exception {
        new JournalManager().getHitSize("", "", null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetListThrowsUnsupported() throws Exception {
        new JournalManager().getList("", "", 0, 10, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetIdListThrowsUnsupported() {
        new JournalManager().getIdList("", "", null);
    }

    @Test
    public void testResultSetToLogEntryListHandlerNotNull() {
        assertNotNull(JournalManager.resultSetToLogEntryListHandler);
    }
}
