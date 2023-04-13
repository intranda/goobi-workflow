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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.TemplateManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, RulesetManager.class, DocketManager.class, PropertyManager.class, TemplateManager.class,
    MasterpieceManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class ProcessServiceTest extends AbstractTest {

    private ProcessService service;
    private RestProcessResource resource;

    @Before
    public void setUp() throws Exception {
        service = new ProcessService();

        resource = new RestProcessResource();

        Process process = MockProcess.createProcess();
        process.setId(5);
        process.setIstTemplate(true);
        process.setSortHelperDocstructs(5);
        process.setSortHelperImages(5);
        process.setSortHelperMetadata(5);
        process.setSortHelperStatus("050050000");

        Project otherProject = new Project();
        otherProject.setTitel("other");
        Institution inst = new Institution();
        inst.setShortName("name");
        process.getProjekt().setInstitution(inst);
        otherProject.setInstitution(inst);
        Ruleset otherRuleset = new Ruleset();
        otherRuleset.setTitel("otherRuleset");
        Docket docket = new Docket();
        docket.setName("docket");
        process.setDocket(docket);
        Docket otherDocket = new Docket();
        otherDocket.setName("otherDocket");

        Batch batch = new Batch();
        batch.setBatchId(1);
        batch.setBatchLabel("label");

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getBatchById(EasyMock.anyInt())).andReturn(batch).anyTimes();
        EasyMock.expect(ProcessManager.countProcessTitle(EasyMock.anyString(), EasyMock.anyObject())).andReturn(0).anyTimes();
        ProcessManager.saveProcessInformation(EasyMock.anyObject());
        ProcessManager.saveProcessInformation(EasyMock.anyObject());

        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.getProjectByName(EasyMock.anyString())).andReturn(otherProject).anyTimes();

        PowerMock.mockStatic(RulesetManager.class);
        EasyMock.expect(RulesetManager.getRulesetByName(EasyMock.anyString())).andReturn(otherRuleset).anyTimes();

        PowerMock.mockStatic(DocketManager.class);
        EasyMock.expect(DocketManager.getDocketByName(EasyMock.anyString())).andReturn(otherDocket).anyTimes();

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(TemplateManager.class);
        EasyMock.expect(TemplateManager.getTemplatesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(MasterpieceManager.class);
        EasyMock.expect(MasterpieceManager.getMasterpiecesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();

        EasyMock.expectLastCall();
        PowerMock.replayAll();

    }

    @Test
    public void testGetProcessData() {
        service = new ProcessService();
        Response response = service.getProcessData("");
        assertEquals(400, response.getStatus());
        response = service.getProcessData("abc");
        assertEquals(400, response.getStatus());

        response = service.getProcessData("1");
        assertNotNull(response);

        RestProcessResource res = (RestProcessResource) response.getEntity();
        assertEquals(5, res.getId());
        assertEquals("testprocess", res.getTitle());
        assertEquals("project", res.getProjectName());
    }

    @Test
    public void testUpdateProcess() {
        service = new ProcessService();

        resource.setId(0);
        Response response = service.updateProcess(resource);
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        resource.setId(5);
        response = service.updateProcess(resource);
        RestProcessResource res = (RestProcessResource) response.getEntity();
        assertEquals(5, res.getId());
        assertEquals("testprocess", res.getTitle());
        assertEquals(5, res.getNumberOfDocstructs().intValue());
        assertEquals(5, res.getNumberOfImages().intValue());
        assertEquals(5, res.getNumberOfMetadata().intValue());
        assertEquals("050050000", res.getStatus());
        assertEquals("project", res.getProjectName());
        assertEquals("ruleset.xml", res.getRulesetName());
        assertEquals("docket", res.getDocketName());
        assertNull(res.getBatchNumber());

        resource.setNumberOfDocstructs(10);
        resource.setNumberOfImages(10);
        resource.setNumberOfMetadata(10);
        resource.setStatus("100000000");
        resource.setProjectName("other");
        resource.setRulesetName("otherRuleset");
        resource.setDocketName("otherDocket");
        resource.setBatchNumber(1);

        resource.setTitle("newTitle");

        response = service.updateProcess(resource);
        res = (RestProcessResource) response.getEntity();
        assertEquals(10, res.getNumberOfDocstructs().intValue());
        assertEquals(10, res.getNumberOfImages().intValue());
        assertEquals(10, res.getNumberOfMetadata().intValue());
        assertEquals("100000000", res.getStatus());
        assertEquals("other", res.getProjectName());
        assertEquals("otherRuleset", res.getRulesetName());
        assertEquals("otherDocket", res.getDocketName());
        assertEquals(1, res.getBatchNumber().intValue());
        assertEquals("newTitle", res.getTitle());
    }

    @Test
    public void testCreateProcess() {
        service = new ProcessService();
        Response response = service.createProcess(resource);
        assertNull(response);
    }

    @Test
    public void testDeleteProcess() {
        service = new ProcessService();
        Response response = service.deleteProcess(resource);
        assertNull(response);
    }
}
