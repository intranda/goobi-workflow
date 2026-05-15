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

import java.util.ArrayList;

import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessServiceAccessControlTest extends AbstractTest {

    private Process buildProcess(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setTitel("testProject");
        Institution inst = new Institution();
        inst.setShortName("inst");
        project.setInstitution(inst);

        Ruleset ruleset = new Ruleset();
        ruleset.setTitel("testRuleset");

        Docket docket = new Docket();
        docket.setName("testDocket");

        Process process = new Process();
        process.setId(5);
        process.setTitel("testProcess");
        process.setProjekt(project);
        process.setRegelsatz(ruleset);
        process.setDocket(docket);
        return process;
    }

    private AuthenticationToken buildToken(int userId) {
        AuthenticationToken token = new AuthenticationToken();
        token.setUserId(userId);
        return token;
    }

    @Test
    public void testGetProcessDataWithoutTokenAllowsAccess() throws Exception {
        Process process = buildProcess(42);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(
                    Mockito.anyInt(), Mockito.eq(PropertyOwnerType.PROCESS))).thenReturn(new ArrayList<>());
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(new ArrayList<>());

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(null);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            Response response = service.getProcessData("5");
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void testGetProcessDataWithMemberTokenAllowsAccess() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(true);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(
                    Mockito.anyInt(), Mockito.eq(PropertyOwnerType.PROCESS))).thenReturn(new ArrayList<>());
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(new ArrayList<>());

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(token);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            Response response = service.getProcessData("5");
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    public void testGetProcessDataWithNonMemberTokenReturns403() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(token);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            Response response = service.getProcessData("5");
            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void testCreateProcessWithNonMemberTokenReturns403() throws Exception {
        Project project = new Project();
        project.setId(42);
        project.setTitel("testProject");
        Institution inst = new Institution();
        inst.setShortName("inst");
        project.setInstitution(inst);

        Process template = new Process();
        template.setId(1);
        template.setTitel("template");
        template.setProjekt(project);

        AuthenticationToken token = buildToken(7);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("template")).thenReturn(template);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(token);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            org.goobi.api.rest.model.RestProcessResource res = new org.goobi.api.rest.model.RestProcessResource();
            res.setProcessTemplateName("template");
            res.setTitle("newProcess");
            Response response = service.createProcess(res);
            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void testDeleteProcessWithNonMemberTokenReturns403() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(false);

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(token);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            org.goobi.api.rest.model.RestProcessResource res = new org.goobi.api.rest.model.RestProcessResource();
            res.setId(5);
            Response response = service.deleteProcess(res);
            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void testQueryWithNonMemberTokenFiltersResults() throws Exception {
        Process process1 = buildProcess(42);
        process1.setId(1);
        Process process2 = buildProcess(99);
        process2.setId(2);
        java.util.List<Process> allProcesses = new java.util.ArrayList<>();
        allProcesses.add(process1);
        allProcesses.add(process2);

        AuthenticationToken token = buildToken(7);
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcesses(
                    Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(allProcesses);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 42)).thenReturn(true);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(7, 99)).thenReturn(false);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);

            HttpServletRequest mockReq = Mockito.mock(HttpServletRequest.class);
            Mockito.when(mockReq.getAttribute("authToken")).thenReturn(token);

            ProcessService service = new ProcessService();
            service.request = mockReq;

            org.goobi.api.rest.model.RestProcessQueryResource queryRes = new org.goobi.api.rest.model.RestProcessQueryResource();
            Response response = service.retrieveProcessesSatisfyingCondition(queryRes);
            assertEquals(200, response.getStatus());
            org.goobi.api.rest.model.RestProcessQueryResult result =
                    (org.goobi.api.rest.model.RestProcessQueryResult) response.getEntity();
            assertEquals(1, result.getResults());
            assertEquals(Integer.valueOf(1), result.getIds()[0]);
        }
    }
}
