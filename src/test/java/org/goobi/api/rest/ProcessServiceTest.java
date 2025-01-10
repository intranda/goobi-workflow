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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.api.rest.model.RestMetadataResource;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import jakarta.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, RulesetManager.class, DocketManager.class, PropertyManager.class, TemplateManager.class,
        MasterpieceManager.class, StepManager.class, UsergroupManager.class, CloseStepHelper.class, JournalManager.class, Helper.class,
        MetadataManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class ProcessServiceTest extends AbstractTest {

    private ProcessService service;
    private RestProcessResource processResource;

    private Process process;
    private Step step;
    private JournalEntry entry;

    @After
    public void tearDown() throws Exception {
        // if process folder exists, delete it
        Path tempFolder = Paths.get(process.getProcessDataDirectory());
        if (StorageProvider.getInstance().isFileExists(tempFolder)) {
            StorageProvider.getInstance().deleteDir(tempFolder);
        }

    }

    @Before
    public void setUp() throws Exception {
        service = new ProcessService();

        processResource = new RestProcessResource();

        process = MockProcess.createProcess();
        String oldMetadata = process.getProcessDataDirectory() + "meta.xml";
        process.setId(5);
        process.setIstTemplate(true);
        process.setSortHelperDocstructs(5);
        process.setSortHelperImages(5);
        process.setSortHelperMetadata(5);
        process.setSortHelperStatus("050050000");

        // copy meta.xml from old folder to new folder
        Path newDirectory = Paths.get(process.getProcessDataDirectory());
        Path newMetadata = Paths.get(newDirectory.toString(), "meta.xml");
        if (!Files.exists(newDirectory)) {
            Files.createDirectories(newDirectory);
        }
        Files.deleteIfExists(newMetadata);

        Files.copy(Paths.get(oldMetadata), Paths.get(newDirectory.toString(), "meta.xml"));

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

        step = new Step();
        step.setTitel("step");
        step.setReihenfolge(1);
        step.setPrioritaet(1);
        step.setProcessId(1);

        Processproperty property = new Processproperty();
        property.setProzess(process);
        property.setProcessId(1);
        property.setCreationDate(new Date());
        property.setTitel("title");
        property.setWert("value");

        entry = new JournalEntry(1, 1, new Date(), "user", LogType.INFO, "content", "filename", EntryType.PROCESS, null);
        List<JournalEntry> journal = new ArrayList<>();
        journal.add(entry);

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

        List<Processproperty> props = new ArrayList<>();
        props.add(property);

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(props).anyTimes();
        EasyMock.expect(PropertyManager.getProcessPropertyById(EasyMock.anyInt())).andReturn(property).anyTimes();
        PowerMock.mockStatic(TemplateManager.class);
        EasyMock.expect(TemplateManager.getTemplatesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(MasterpieceManager.class);
        EasyMock.expect(MasterpieceManager.getMasterpiecesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();

        PropertyManager.saveProcessProperty(EasyMock.anyObject());
        PropertyManager.saveProcessProperty(EasyMock.anyObject());
        PropertyManager.deleteProcessProperty(EasyMock.anyObject());
        PropertyManager.deleteProcessProperty(EasyMock.anyObject());

        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(StepManager.getStepById(EasyMock.anyInt())).andReturn(step).anyTimes();
        StepManager.saveStep(EasyMock.anyObject());
        StepManager.saveStep(EasyMock.anyObject());
        StepManager.deleteStep(EasyMock.anyObject());

        PowerMock.mockStatic(UsergroupManager.class);
        Usergroup grp = new Usergroup();
        grp.setTitel("group");
        EasyMock.expect(UsergroupManager.getUsergroupByName(EasyMock.anyString())).andReturn(grp).anyTimes();

        PowerMock.mockStatic(CloseStepHelper.class);
        EasyMock.expect(CloseStepHelper.closeStep(EasyMock.anyObject(), EasyMock.anyObject())).andReturn(true).anyTimes();

        PowerMock.mockStatic(JournalManager.class);
        EasyMock.expect(JournalManager.getLogEntriesForProcess(EasyMock.anyInt())).andReturn(journal).anyTimes();
        EasyMock.expect(JournalManager.getJournalEntryById(EasyMock.anyInt())).andReturn(entry).anyTimes();
        JournalManager.saveJournalEntry(EasyMock.anyObject());
        JournalManager.deleteJournalEntry(EasyMock.anyObject());

        PowerMock.mockStatic(Helper.class);
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyString());
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyString());

        PowerMock.mockStatic(MetadataManager.class);

        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject());
        MetadataManager.updateJSONMetadata(EasyMock.anyInt(), EasyMock.anyObject());
        List<StringPair> metadataList = new ArrayList<>();
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(metadataList).anyTimes();

        EasyMock.expectLastCall();
        PowerMock.replayAll();

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
        assertEquals("step", data.get(0).getSteptitle());
    }

    @Test
    public void testGetStep() {
        service = new ProcessService();
        Response response = service.getStep("", "");
        assertEquals(400, response.getStatus());

        response = service.getStep("1", "1");
        assertNotNull(response);

        RestStepResource data = (RestStepResource) response.getEntity();

        assertEquals("step", data.getSteptitle());
    }

    @Test
    public void testUpdateStep() {
        RestStepResource stepResource = new RestStepResource();

        // no step id
        Response response = service.updateStep("1", stepResource);
        assertEquals(400, response.getStatus());

        stepResource.setStepId(1);
        stepResource.setProcessId(1);
        response = service.updateStep("1", stepResource);
        assertEquals(200, response.getStatus());

        prepareStepObject(stepResource);

        response = service.updateStep("1", stepResource);
        assertEquals(200, response.getStatus());
    }

    private void prepareStepObject(RestStepResource stepResource) {
        stepResource.setSteptitle("new step");
        stepResource.setStatus("stepdone");

        stepResource.setPriority(1);
        stepResource.setOrder("10");
        stepResource.setStartDate(new Date());
        stepResource.setFinishDate(new Date());
        stepResource.setSteptitle("step plugin");
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
    }

    @Test
    public void testCreateStep() {
        RestStepResource stepResource = new RestStepResource();

        // missing process id
        Response response = service.createStep("", stepResource);
        assertEquals(400, response.getStatus());
        response = service.createStep("abc", stepResource);
        assertEquals(400, response.getStatus());

        // missing step title
        response = service.createStep("1", stepResource);
        assertEquals(400, response.getStatus());
        stepResource.setSteptitle("new step");
        // missing step order
        response = service.createStep("1", stepResource);
        assertEquals(400, response.getStatus());

        // missing usergroups
        stepResource.setOrder("10");
        response = service.createStep("1", stepResource);
        assertEquals(400, response.getStatus());

        // minimum requirements fulfilled
        stepResource.getUsergroups().add("Administration");
        response = service.createStep("1", stepResource);
        assertEquals(200, response.getStatus());

        // update optional parameter
        prepareStepObject(stepResource);
        response = service.createStep("1", stepResource);
        assertEquals(200, response.getStatus());

    }

    @Test
    public void testDeleteStep() {
        RestStepResource stepResource = new RestStepResource();

        // no step id given
        Response response = service.deleteStep("0", stepResource);
        assertEquals(400, response.getStatus());

        // deletion successful
        stepResource.setStepId(1);
        response = service.deleteStep("0", stepResource);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCloseStep() {
        // missing/wrong parameter
        Response response = service.closeStep("", "1");
        assertEquals(400, response.getStatus());
        response = service.closeStep("abc", "1");
        assertEquals(400, response.getStatus());
        response = service.closeStep("1", "");
        assertEquals(400, response.getStatus());
        response = service.closeStep("1", "abc");
        assertEquals(400, response.getStatus());

        // step belongs to a different process
        response = service.closeStep("2", "1");
        assertEquals(409, response.getStatus());

        // step has the wrong status
        response = service.closeStep("1", "1");
        assertEquals(409, response.getStatus());

        // step has the correct status
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        response = service.closeStep("1", "1");
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetJournal() {
        // missing/wrong parameter
        Response response = service.getJournal("");
        assertEquals(400, response.getStatus());
        response = service.getJournal("abc");
        assertEquals(400, response.getStatus());

        response = service.getJournal("1");
        assertEquals(200, response.getStatus());
        @SuppressWarnings("unchecked")
        List<RestJournalResource> data = (List<RestJournalResource>) response.getEntity();
        assertEquals(1, data.size());
        assertEquals("content", data.get(0).getMessage());
    }

    @Test
    public void testUpdateJournalEntry() {
        RestJournalResource resource = new RestJournalResource();
        // missing/wrong parameter
        Response response = service.updateJournalEntry("", resource);
        assertEquals(400, response.getStatus());
        response = service.updateJournalEntry("abc", resource);
        assertEquals(400, response.getStatus());

        // no journal id
        response = service.updateJournalEntry("1", resource);
        assertEquals(400, response.getStatus());

        resource.setId(1);
        resource.setMessage("new content");

        response = service.updateJournalEntry("1", resource);
        assertEquals(200, response.getStatus());
        assertEquals("new content", ((RestJournalResource) response.getEntity()).getMessage());
    }

    @Test
    public void testCreateJournalEntry() {
        RestJournalResource resource = new RestJournalResource();

        resource.setMessage("content");
        resource.setType("warn");
        resource.setUserName("user");

        Response response = service.createJournalEntry("", resource);
        assertEquals(400, response.getStatus());
        response = service.createJournalEntry("abc", resource);
        assertEquals(400, response.getStatus());

        response = service.createJournalEntry("1", resource);
        assertEquals(200, response.getStatus());
        assertEquals(200, response.getStatus());
        assertEquals("content", ((RestJournalResource) response.getEntity()).getMessage());
        assertEquals("warn", ((RestJournalResource) response.getEntity()).getType());
        assertEquals("user", ((RestJournalResource) response.getEntity()).getUserName());
    }

    @Test
    public void testDeleteJournalEntry() {
        RestJournalResource resource = new RestJournalResource();
        resource.setId(1);
        resource.setProcessId(1);

        // missing/wrong parameter
        Response response = service.deleteJournalEntry("", resource);
        assertEquals(400, response.getStatus());
        response = service.deleteJournalEntry("abc", resource);
        assertEquals(400, response.getStatus());

        // entry belongs to a different process
        response = service.deleteJournalEntry("2", resource);
        assertEquals(409, response.getStatus());

        response = service.deleteJournalEntry("1", resource);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetProperties() {
        Response response = service.getProperties(null);
        assertEquals(400, response.getStatus());

        response = service.getProperties("1");
        @SuppressWarnings("unchecked")
        List<RestPropertyResource> data = (List<RestPropertyResource>) response.getEntity();
        assertEquals("title", data.get(0).getName());
        assertEquals("value", data.get(0).getValue());
    }

    @Test
    public void testGetProperty() {
        Response response = service.getProperty(null, "1");
        assertEquals(400, response.getStatus());
        response = service.getProperty("1", null);
        assertEquals(400, response.getStatus());
        response = service.getProperty("1", "1");
        assertEquals(200, response.getStatus());

        RestPropertyResource data = (RestPropertyResource) response.getEntity();
        assertEquals("title", data.getName());
        assertEquals("value", data.getValue());
    }

    @Test
    public void testUpdateProperty() {

        RestPropertyResource resource = new RestPropertyResource();

        Response response = service.updateProperty(null, resource);
        assertEquals(400, response.getStatus());
        response = service.updateProperty("1", resource);
        assertEquals(400, response.getStatus());
        resource.setId(1);
        response = service.updateProperty("2", resource);
        assertEquals(409, response.getStatus());
        resource.setId(1);
        response = service.updateProperty("1", resource);
        RestPropertyResource data = (RestPropertyResource) response.getEntity();
        assertEquals("title", data.getName());
        assertEquals("value", data.getValue());

        resource.setName("new name");
        resource.setValue("new value");

        response = service.updateProperty("1", resource);
        data = (RestPropertyResource) response.getEntity();
        assertEquals("new name", data.getName());
        assertEquals("new value", data.getValue());
    }

    @Test
    public void testCreateProperty() {

        RestPropertyResource resource = new RestPropertyResource();

        Response response = service.createProperty("", resource);
        assertEquals(400, response.getStatus());
        response = service.createProperty("1", resource);
        assertEquals(400, response.getStatus());

        resource.setName("name");
        response = service.createProperty("1", resource);
        assertEquals(400, response.getStatus());

        resource.setValue("value");
        response = service.createProperty("1", resource);
        RestPropertyResource data = (RestPropertyResource) response.getEntity();
        assertEquals("name", data.getName());
        assertEquals("value", data.getValue());
    }

    @Test
    public void testGetMetadata() {
        Response response = service.getMetadata(null);
        assertEquals(400, response.getStatus());

        response = service.getMetadata("1");
        assertEquals(200, response.getStatus());

        @SuppressWarnings("unchecked")
        List<RestMetadataResource> data = (List<RestMetadataResource>) response.getEntity();
        assertEquals(4, data.size());
    }

    @Test
    public void testUpdateMetadata() {
        RestMetadataResource resource = new RestMetadataResource();
        resource.setValue("value");

        Response response = service.updateMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setName("invalid");
        response = service.updateMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setMetadataLevel("topstruct");
        response = service.updateMetadata(null, resource);
        assertEquals(400, response.getStatus());

        response = service.updateMetadata("", resource);
        assertEquals(400, response.getStatus());
        response = service.updateMetadata("abc", resource);
        assertEquals(400, response.getStatus());

        response = service.updateMetadata("1", resource);
        assertEquals(500, response.getStatus());

        resource.setName("TitleDocMain");
        resource.setAuthorityValue("value");
        response = service.updateMetadata("1", resource);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCreateMetadata() {
        RestMetadataResource resource = new RestMetadataResource();
        resource.setValue("value");
        resource.setAuthorityValue("value");

        Response response = service.createMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setName("invalid");
        response = service.createMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setMetadataLevel("topstruct");
        response = service.createMetadata(null, resource);
        assertEquals(400, response.getStatus());

        response = service.createMetadata("", resource);
        assertEquals(400, response.getStatus());
        response = service.createMetadata("abc", resource);
        assertEquals(400, response.getStatus());

        response = service.createMetadata("1", resource);
        assertEquals(400, response.getStatus());

        resource.setName("TitleDocMain");
        response = service.createMetadata("1", resource);
        assertEquals(500, response.getStatus());

        resource.setName("PublicationYear");
        response = service.createMetadata("1", resource);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testDeleteMetadata() {
        RestMetadataResource resource = new RestMetadataResource();
        resource.setValue("value");
        resource.setAuthorityValue("value");

        Response response = service.deleteMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setName("invalid");
        response = service.deleteMetadata(null, resource);
        assertEquals(400, response.getStatus());

        resource.setMetadataLevel("topstruct");
        response = service.deleteMetadata(null, resource);
        assertEquals(400, response.getStatus());

        response = service.deleteMetadata("", resource);
        assertEquals(400, response.getStatus());
        response = service.deleteMetadata("abc", resource);
        assertEquals(400, response.getStatus());

        response = service.deleteMetadata("1", resource);
        assertEquals(400, response.getStatus());

        resource.setName("TitleDocMain");
        response = service.deleteMetadata("1", resource);
        assertEquals(200, response.getStatus());
    }
}
