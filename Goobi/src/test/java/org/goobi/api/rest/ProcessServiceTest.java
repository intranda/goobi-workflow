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
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
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
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, RulesetManager.class, DocketManager.class, PropertyManager.class, TemplateManager.class,
        MasterpieceManager.class, StepManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class ProcessServiceTest extends AbstractTest {

    private ProcessService service;
    private RestProcessResource processResource;

    @Before
    public void setUp() throws Exception {
        service = new ProcessService();

        processResource = new RestProcessResource();

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

        Step step = new Step();
        step.setTitel("step");
        step.setReihenfolge(1);
        step.setPrioritaet(1);

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getProcessByExactTitle(EasyMock.anyString())).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getBatchById(EasyMock.anyInt())).andReturn(batch).anyTimes();
        EasyMock.expect(ProcessManager.countProcessTitle(EasyMock.anyString(), EasyMock.anyObject())).andReturn(0).anyTimes();
        ProcessManager.saveProcessInformation(EasyMock.anyObject());
        ProcessManager.saveProcessInformation(EasyMock.anyObject());
        ProcessManager.saveProcess(EasyMock.anyObject());
        ProcessManager.deleteProcess(EasyMock.anyObject());
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

        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(StepManager.getStepById(EasyMock.anyInt())).andReturn(step).anyTimes();

        EasyMock.expectLastCall();
        PowerMock.replayAll();

        ;
        process.getSchritte().add(step);

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

        processResource.setId(0);
        Response response = service.updateProcess(processResource);
        assertNotNull(response);
        assertEquals(400, response.getStatus());

        processResource.setId(5);
        response = service.updateProcess(processResource);
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

        processResource.setNumberOfDocstructs(10);
        processResource.setNumberOfImages(10);
        processResource.setNumberOfMetadata(10);
        processResource.setStatus("100000000");
        processResource.setProjectName("other");
        processResource.setRulesetName("otherRuleset");
        processResource.setDocketName("otherDocket");
        processResource.setBatchNumber(1);

        processResource.setTitle("newTitle");

        response = service.updateProcess(processResource);
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

        Response response = service.createProcess(processResource);
        // no process template configured
        assertEquals(400, response.getStatus());
        processResource.setProcessTemplateName("template");

        // no title configured
        processResource.setTitle(null);
        response = service.createProcess(processResource);
        assertEquals(400, response.getStatus());

        // invalid title configured
        processResource.setTitle("ÖÄÜ?\"§$%%&");
        response = service.createProcess(processResource);
        assertEquals(406, response.getStatus());

        processResource.setTitle("sample");
        response = service.createProcess(processResource);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testDeleteProcess() {
        service = new ProcessService();
        processResource.setId(0);
        Response response = service.deleteProcess(processResource);
        assertEquals(400, response.getStatus());

        processResource.setId(1);
        response = service.deleteProcess(processResource);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetStepList() {
        service = new ProcessService();
        Response response = service.getStepList("");
        assertEquals(400, response.getStatus());
        response = service.getStepList("abc");
        assertEquals(400, response.getStatus());

        response = service.getStepList("1");
        assertNotNull(response);

        @SuppressWarnings("unchecked")
        List<RestStepResource> data = (List<RestStepResource>) response.getEntity();

        assertEquals(1, data.size());
        assertEquals("step", data.get(0).getStepName());
    }

    @Test
    public void testGetStep() {
        service = new ProcessService();
        Response response = service.getStep("", "");
        assertEquals(400, response.getStatus());

        response = service.getStep("1", "1");
        assertNotNull(response);

        RestStepResource data = (RestStepResource) response.getEntity();

        assertEquals("step", data.getStepName());
    }

    @Test
    public void testUpdateStep() {
        RestStepResource stepResource = new RestStepResource();

        // no step id
        Response response = service.updateStep(stepResource);
        assertEquals(400, response.getStatus());

        stepResource.setStepId(1);
        stepResource.setProcessId(1);
        response = service.updateStep(stepResource);
        assertEquals(200, response.getStatus());

        stepResource.setStepName("new step");
        stepResource.setStatus("stepdone");

        stepResource.setPriority(1);
        stepResource.setOrder(10);
        stepResource.setStartDate(new Date());
        stepResource.setFinishDate(new Date());
        stepResource.setStepPlugin("step plugin");
        stepResource.setValidationPlugin("validation plugin");
        stepResource.setQueueType("goobi_fast");

        stepResource.getProperties().put("metadata", true);
        stepResource.getProperties().put("automatic", false);
        stepResource.getProperties().put("thumbnailGeneration", true);
        stepResource.getProperties().put("readAccess", false);
        stepResource.getProperties().put("writeAccess", true);
        stepResource.getProperties().put("export", false);
        stepResource.getProperties().put("script", true);
        stepResource.getProperties().put("validate", false);
        stepResource.getProperties().put("batch", true);
        stepResource.getProperties().put("delayStep", false);
        stepResource.getProperties().put("updateMetadataIndex", true);
        stepResource.getProperties().put("generateDocket", false);

        stepResource.getScripts().put("scriptmname1", "/bin/false");
        stepResource.getScripts().put("scriptmname2", "/bin/false");
        stepResource.getScripts().put("scriptmname3", "/bin/false");
        stepResource.getScripts().put("scriptmname4", "/bin/false");
        stepResource.getScripts().put("scriptmname5", "/bin/false");

        stepResource.getHttpStepConfiguration().put("url", "url");
        stepResource.getHttpStepConfiguration().put("method", "GET");
        stepResource.getHttpStepConfiguration().put("body", "body");
        stepResource.getHttpStepConfiguration().put("closeStep", "false");
        stepResource.getHttpStepConfiguration().put("escapeBody", "true");

        response = service.updateStep(stepResource);
        assertEquals(200, response.getStatus());
    }

}
