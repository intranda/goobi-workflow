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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.AddProcessMetadataReq;
import org.goobi.api.rest.request.DeleteProcessMetadataReq;
import org.goobi.api.rest.request.ProcessCreationRequest;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.WebApplicationException;
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

    @Test
    public void testCreateProcessEmptyIdentifierReturnsBadRequest() {
        ProcessCreationRequest req = new ProcessCreationRequest();
        // identifier stays blank/null, no static mocking needed as the guard fires first

        ProcessesAliasResource resource = new ProcessesAliasResource();
        Response response = resource.createProcess(req);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCreateProcessMissingTemplateReturnsBadRequest() {
        ProcessCreationRequest req = new ProcessCreationRequest();
        req.setIdentifier("identifier-1");
        // neither templateName nor templateId set, no static mocking needed as the guard fires first

        ProcessesAliasResource resource = new ProcessesAliasResource();
        Response response = resource.createProcess(req);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCreateProcessTitleAlreadyExistsReturnsConflict() {
        ProcessCreationRequest req = new ProcessCreationRequest();
        req.setIdentifier("identifier-1");
        req.setTemplateName("someTemplate");
        req.setLogicalDSType("Monograph");

        Process existingProcess = new Process();
        existingProcess.setTitel("identifier-1");

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessByTitle("identifier-1")).thenReturn(existingProcess);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.createProcess(req);

            assertEquals(409, response.getStatus());
        }
    }

    @Test
    public void testCreateProcessTemplateNotFoundReturnsNotFound() {
        ProcessCreationRequest req = new ProcessCreationRequest();
        req.setIdentifier("identifier-1");
        req.setTemplateName("someTemplate");
        req.setLogicalDSType("Monograph");

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessByTitle("identifier-1")).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("someTemplate")).thenReturn(null);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.createProcess(req);

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testUploadFileInvalidFolderNameReturnsBadRequest() {
        ProcessesAliasResource resource = new ProcessesAliasResource();
        // the folder-name regex guard fires before any process lookup or IO, so processId/streams can stay unused
        Response response = resource.uploadFile(1, "invalid folder!", null, null, null);

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testUploadFileUserNotInProjectReturnsUnauthorized() throws Exception {
        int processId = 12;

        Process process = new Process();
        process.setId(processId);
        process.setProjectId(5);

        Project otherProject = new Project();
        otherProject.setId(6);
        User user = new User();
        user.setProjekte(new ArrayList<>(List.of(otherProject)));

        LoginBean loginBean = Mockito.mock(LoginBean.class);
        Mockito.when(loginBean.getMyBenutzer()).thenReturn(user);

        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute("LoginForm")).thenReturn(loginBean);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(session);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            injectRequest(resource, request);

            Response response = resource.uploadFile(processId, "validfolder", null, null, "image.tif");

            assertEquals(401, response.getStatus());
        }
    }

    @Test
    public void testUploadFilePathTraversalFilenameReturnsBadRequest() throws Exception {
        int processId = 13;

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.getConfiguredImageFolder("validfolder")).thenReturn("/tmp/goobi-upload-test/");

        // no LoginForm in the session -> user stays null and the project-membership guard is skipped,
        // so the request reaches the filename handling below
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(session.getAttribute("LoginForm")).thenReturn(null);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getSession()).thenReturn(session);

        StorageProviderInterface storageProvider = Mockito.mock(StorageProviderInterface.class);
        Mockito.when(storageProvider.isFileExists(Mockito.any())).thenReturn(true);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StorageProvider> mockedStorageProvider = Mockito.mockStatic(StorageProvider.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);
            mockedStorageProvider.when(StorageProvider::getInstance).thenReturn(storageProvider);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            injectRequest(resource, request);

            // ".." is not stripped by Paths.get(rawName).getFileName() (it is itself the single name
            // element), so resolving it against the destination folder escapes that folder and trips
            // the "!dest.startsWith(path)" guard.
            Response response = resource.uploadFile(processId, "validfolder", null, null, "..");

            assertEquals(400, response.getStatus());
        }
    }

    private static void injectRequest(ProcessesAliasResource resource, HttpServletRequest request) throws Exception {
        Field requestField = ProcessesAliasResource.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(resource, request);
    }

    @Test
    public void testUpdateProcessPropertyExistingPropertyReturnsAccepted() {
        int processId = 7;
        String propertyName = "ExistingProp";

        Processproperty existing = new Processproperty();
        existing.setProcessId(processId);
        existing.setTitel(propertyName);
        existing.setWert("oldValue");

        List<Processproperty> properties = new ArrayList<>();
        properties.add(existing);

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(processId)).thenReturn(properties);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.updateProcessProperty(processId, propertyName, "newValue");

            assertEquals(202, response.getStatus());
            assertEquals("newValue", existing.getWert());
            mockedPropertyManager.verify(() -> PropertyManager.saveProcessProperty(existing));
        }
    }

    @Test
    public void testUpdateProcessPropertyNewPropertyReturnsAccepted() {
        int processId = 8;
        String propertyName = "NewProp";

        Process process = new Process();
        process.setId(processId);

        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedPropertyManager.when(() -> PropertyManager.getProcessPropertiesForProcess(processId)).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);

            ProcessesAliasResource resource = new ProcessesAliasResource();
            Response response = resource.updateProcessProperty(processId, propertyName, "newValue");

            assertEquals(202, response.getStatus());
            mockedPropertyManager.verify(() -> PropertyManager.saveProcessProperty(Mockito.argThat(
                    pp -> propertyName.equals(pp.getTitel()) && "newValue".equals(pp.getWert()) && pp.getProzess() == process)));
        }
    }

    @Test
    public void testDeleteMetadataUnknownProcessReturnsNotFound() {
        int unknownId = 999999999;
        DeleteProcessMetadataReq req = new DeleteProcessMetadataReq();

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(unknownId)).thenReturn(null);

            ProcessesAliasResource resource = new ProcessesAliasResource();

            WebApplicationException exception = assertThrows(WebApplicationException.class, () -> resource.deleteMetadata(unknownId, req));

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), exception.getResponse().getStatus());
        }
    }

    @Test
    public void testAddMetadataUnknownProcessReturnsNotFound() {
        int unknownId = 999999999;
        AddProcessMetadataReq req = new AddProcessMetadataReq();

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(unknownId)).thenReturn(null);

            ProcessesAliasResource resource = new ProcessesAliasResource();

            WebApplicationException exception = assertThrows(WebApplicationException.class, () -> resource.addMetadata(unknownId, req));

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), exception.getResponse().getStatus());
        }
    }
}
