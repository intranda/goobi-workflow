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

package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessJournalResourceTest extends AbstractProcessResourceTest {

    @Test
    public void testUpdateJournalEntryMissingJournalIdReturnsBadRequest() {
        Process process = buildProcess(5);
        RestJournalResource body = new RestJournalResource();
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessJournalResource resource = new ProcessJournalResource();

            // journal id not set at all -> null
            assertEquals(400, resource.updateJournalEntry("5", body).getStatus());

            // journal id explicitly 0
            body.setId(0);
            assertEquals(400, resource.updateJournalEntry("5", body).getStatus());
        }
    }

    @Test
    public void testUpdateJournalEntryNotFoundReturnsNotFound() {
        Process process = buildProcess(5);
        RestJournalResource body = new RestJournalResource();
        body.setId(123);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> jm = Mockito.mockStatic(JournalManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            jm.when(() -> JournalManager.getJournalEntryById(123)).thenReturn(null);

            ProcessJournalResource resource = new ProcessJournalResource();

            assertEquals(404, resource.updateJournalEntry("5", body).getStatus());
        }
    }

    @Test
    public void testDeleteJournalEntryMissingJournalIdReturnsBadRequest() {
        Process process = buildProcess(5);
        RestJournalResource body = new RestJournalResource();
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessJournalResource resource = new ProcessJournalResource();

            // journal id not set at all -> null
            assertEquals(400, resource.deleteJournalEntry("5", body).getStatus());

            // journal id explicitly 0
            body.setId(0);
            assertEquals(400, resource.deleteJournalEntry("5", body).getStatus());
        }
    }

    @Test
    public void testDeleteJournalEntryNotFoundReturnsNotFound() {
        Process process = buildProcess(42);
        RestJournalResource body = new RestJournalResource();
        body.setId(123);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> jm = Mockito.mockStatic(JournalManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            jm.when(() -> JournalManager.getJournalEntryById(123)).thenReturn(null);

            ProcessJournalResource resource = new ProcessJournalResource();
            resource.setRequest(mockRequestWithoutToken());

            assertEquals(404, resource.deleteJournalEntry("5", body).getStatus());
        }
    }

    @Test
    public void testCreateJournalEntryDefaultsAppliedWhenUserNameAndTypeMissing() {
        Process process = buildProcess(5);
        RestJournalResource body = new RestJournalResource();
        body.setMessage("content");
        // userName and type intentionally left unset to trigger the default branches

        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> jm = Mockito.mockStatic(JournalManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);

            ProcessJournalResource resource = new ProcessJournalResource();

            Response response = resource.createJournalEntry("5", body);
            assertEquals(200, response.getStatus());

            ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
            jm.verify(() -> JournalManager.saveJournalEntry(captor.capture()));
            JournalEntry saved = captor.getValue();
            assertEquals("rest api", saved.getUserName());
            assertEquals(LogType.DEBUG, saved.getType());
            assertEquals("content", saved.getContent());
            assertEquals(EntryType.PROCESS, saved.getEntryType());
        }
    }

    @Test
    public void testUpdateJournalEntryNonMemberTokenReturnsForbidden() {
        Process process = buildProcess(42);
        RestJournalResource body = new RestJournalResource();
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> projm = Mockito.mockStatic(ProjectManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            projm.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            ProcessJournalResource resource = new ProcessJournalResource();
            resource.setRequest(mockRequestWithToken(buildToken(7)));

            assertEquals(403, resource.updateJournalEntry("5", body).getStatus());
        }
    }
}
