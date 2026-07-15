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
package org.goobi.api.rest.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("deprecation")
public class ProcessesAliasResourceTest extends AbstractTest {

    @Test
    public void testAdvancedSearchWithEmptyResultReturnsEmptyList() throws Exception {
        try (MockedStatic<RestDbHelper> db = Mockito.mockStatic(RestDbHelper.class)) {
            db.when(() -> RestDbHelper.searchProcesses(Mockito.any(SearchRequest.class))).thenReturn(new ArrayList<>());

            ProcessesAliasResource resource = new ProcessesAliasResource();
            List<RestProcess> result = resource.advancedSearch(new SearchRequest());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testSimpleSearchWithEmptyResultReturnsEmptyList() throws Exception {
        try (MockedStatic<RestDbHelper> db = Mockito.mockStatic(RestDbHelper.class)) {
            db.when(() -> RestDbHelper.searchProcesses(Mockito.any(SearchRequest.class))).thenReturn(new ArrayList<>());

            ProcessesAliasResource resource = new ProcessesAliasResource();
            List<RestProcess> result =
                    resource.simpleSearch("CatalogIDDigital", "12345", 0, 0, null, false, null, null, null, null, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Test
    public void testAddLogentryCallsHelperAddMessageToProcessJournalWithMappedLogType() {
        int processId = 42;

        Process process = new Process();
        process.setId(processId);
        process.setTitel("testprocess");

        JournalEntry entry = new JournalEntry(processId, new Date(), "someUser", LogType.INFO, "test log message", EntryType.PROCESS);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);
            mockedHelper.when(() -> Helper.addMessageToProcessJournal(Mockito.anyInt(), Mockito.any(LogType.class), Mockito.anyString(),
                    Mockito.anyString())).thenAnswer(invocation -> null);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.addLogentry(processId, entry);

            assertEquals(200, response.getStatus());
            mockedHelper.verify(() -> Helper.addMessageToProcessJournal(processId, LogType.INFO, "test log message", "webapi"));
        }
    }

    @Test
    public void testAddLogentryProcessNotFoundReturnsError() {
        int processId = 999;

        JournalEntry entry = new JournalEntry(processId, new Date(), "someUser", LogType.INFO, "test log message", EntryType.PROCESS);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(null);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.addLogentry(processId, entry);

            assertEquals(500, response.getStatus());
            mockedHelper.verifyNoInteractions();
        }
    }
}
