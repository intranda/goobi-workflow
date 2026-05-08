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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;

@ExtendWith(MockitoExtension.class)
public class JournalManagerTest extends AbstractTest {

    private JournalEntry sampleEntry;
    private List<JournalEntry> sampleEntries;

    @BeforeEach
    public void setUp() throws Exception {
        sampleEntry = new JournalEntry(1, new Date(), "testUser", LogType.INFO, "Test content", EntryType.PROCESS);
        sampleEntry.setId(1);
        sampleEntries = new ArrayList<>();
        sampleEntries.add(sampleEntry);

    }

    @Test
    public void testSaveJournalEntry() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            JournalManager.saveJournalEntry(sampleEntry);

        }
    }

    @Test
    public void testDeleteJournalEntry() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            JournalManager.deleteJournalEntry(sampleEntry);

        }
    }

    @Test
    public void testDeleteAllJournalEntries() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            JournalManager.deleteAllJournalEntries(1, EntryType.PROCESS);

        }
    }

    @Test
    public void testGetLogEntriesForProcess() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            List<JournalEntry> result = JournalManager.getLogEntriesForProcess(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetLogEntriesForInstitution() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            List<JournalEntry> result = JournalManager.getLogEntriesForInstitution(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetLogEntriesForUser() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            List<JournalEntry> result = JournalManager.getLogEntriesForUser(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetLogEntriesForProject() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            List<JournalEntry> result = JournalManager.getLogEntriesForProject(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetJournalEntryById() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            JournalEntry result = JournalManager.getJournalEntryById(1);
            assertNotNull(result);
            assertEquals(Integer.valueOf(1), result.getId());

        }
    }

    @Test
    public void testGetHitSizeThrowsUnsupported() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> {

            try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

                new JournalManager().getHitSize("", "", null);

            }
        });
    }

    @Test
    public void testGetListThrowsUnsupported() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> {

            try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

                new JournalManager().getList("", "", 0, 10, null);

            }
        });
    }

    @Test
    public void testGetIdListThrowsUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> {

            try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
                mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

                new JournalManager().getIdList("", "", null);

            }
        });
    }

    @Test
    public void testResultSetToLogEntryListHandlerNotNull() {
        try (MockedStatic<JournalMysqlHelper> mockedJournalMysqlHelper = Mockito.mockStatic(JournalMysqlHelper.class)) {
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.saveLogEntry(Mockito.any())).thenReturn(sampleEntry);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getLogEntries(Mockito.anyInt(), Mockito.any())).thenReturn(sampleEntries);
            mockedJournalMysqlHelper.when(() -> JournalMysqlHelper.getJournalEntryById(Mockito.anyInt())).thenReturn(sampleEntry);

            assertNotNull(JournalManager.resultSetToLogEntryListHandler);

        }
    }
}
