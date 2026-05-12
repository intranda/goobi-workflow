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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.goobi.beans.User;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, PropertyManager.class, MetadataManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
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

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(5)).andReturn(process);
        PowerMock.mockStatic(ProjectManager.class);
        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.eq(PropertyOwnerType.PROCESS)))
                .andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(MetadataManager.class);
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(Helper.class);

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(null);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

        ProcessService service = new ProcessService();
        service.request = mockReq;

        Response response = service.getProcessData("5");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetProcessDataWithMemberTokenAllowsAccess() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(5)).andReturn(process);
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 42)).andReturn(true);
        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.eq(PropertyOwnerType.PROCESS)))
                .andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(MetadataManager.class);
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(Helper.class);

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(token);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

        ProcessService service = new ProcessService();
        service.request = mockReq;

        Response response = service.getProcessData("5");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetProcessDataWithNonMemberTokenReturns403() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(5)).andReturn(process);
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 42)).andReturn(false);
        PowerMock.mockStatic(Helper.class);

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(token);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

        ProcessService service = new ProcessService();
        service.request = mockReq;

        Response response = service.getProcessData("5");
        assertEquals(403, response.getStatus());
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

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessByExactTitle("template")).andReturn(template);
        EasyMock.expect(ProcessManager.countProcessTitle(EasyMock.anyString(), EasyMock.anyObject())).andReturn(0);
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 42)).andReturn(false);
        PowerMock.mockStatic(Helper.class);

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(token);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

        ProcessService service = new ProcessService();
        service.request = mockReq;

        org.goobi.api.rest.model.RestProcessResource res = new org.goobi.api.rest.model.RestProcessResource();
        res.setProcessTemplateName("template");
        res.setTitle("newProcess");
        Response response = service.createProcess(res);
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testDeleteProcessWithNonMemberTokenReturns403() throws Exception {
        Process process = buildProcess(42);
        AuthenticationToken token = buildToken(7);

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(5)).andReturn(process);
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 42)).andReturn(false);
        PowerMock.mockStatic(Helper.class);

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(token);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

        ProcessService service = new ProcessService();
        service.request = mockReq;

        org.goobi.api.rest.model.RestProcessResource res = new org.goobi.api.rest.model.RestProcessResource();
        res.setId(5);
        Response response = service.deleteProcess(res);
        assertEquals(403, response.getStatus());
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

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcesses(EasyMock.anyString(), EasyMock.anyObject(),
                EasyMock.anyObject())).andReturn(allProcesses);
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 42)).andReturn(true);
        EasyMock.expect(ProjectManager.isUserMemberOfProject(7, 99)).andReturn(false);
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();

        HttpServletRequest mockReq = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(mockReq.getAttribute("authToken")).andReturn(token);
        EasyMock.replay(mockReq);

        PowerMock.replayAll();

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
