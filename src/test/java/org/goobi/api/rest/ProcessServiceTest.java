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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.api.rest.model.RestJournalResource;
import org.goobi.api.rest.model.RestMetadataResource;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.api.rest.model.RestPropertyResource;
import org.goobi.api.rest.model.RestStepResource;
import org.goobi.beans.Batch;
import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessServiceTest extends AbstractTest {

    private ProcessService service;
    private RestProcessResource processResource;

    private Process process;
    private Step step;
    private JournalEntry entry;

    @AfterEach
    public void tearDown() throws Exception {
        // if process folder exists, delete it
        Path tempFolder = Paths.get(process.getProcessDataDirectory());
        if (StorageProvider.getInstance().isFileExists(tempFolder)) {
            StorageProvider.getInstance().deleteDir(tempFolder);
        }
    }

    private Batch batch;
    private Docket docket;
    private Usergroup grp;
    private List<JournalEntry> journal;
    private List<StringPair> metadataList;
    private Docket otherDocket;
    private Project otherProject;
    private Ruleset otherRuleset;
    private GoobiProperty property;
    private List<GoobiProperty> props;

    @BeforeEach
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

        otherProject = new Project();
        otherProject.setTitel("other");
        Institution inst = new Institution();
        inst.setShortName("name");
        process.getProjekt().setInstitution(inst);
        otherProject.setInstitution(inst);
        otherRuleset = new Ruleset();
        otherRuleset.setTitel("otherRuleset");
        docket = new Docket();
        docket.setName("docket");
        process.setDocket(docket);
        otherDocket = new Docket();
        otherDocket.setName("otherDocket");

        batch = new Batch();
        batch.setBatchId(1);
        batch.setBatchLabel("label");

        step = new Step();
        step.setTitel("step");
        step.setReihenfolge(1);
        step.setPrioritaet(1);
        step.setProcessId(1);

        property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setOwner(process);
        property.setObjectId(1);
        property.setCreationDate(new Date());
        property.setPropertyName("title");
        property.setPropertyValue("value");

        entry = new JournalEntry(1, 1, new Date(), "user", LogType.INFO, "content", "filename", EntryType.PROCESS, null);
        journal = new ArrayList<>();
        journal.add(entry);

        props = new ArrayList<>();
        props.add(property);

        grp = new Usergroup();
        grp.setTitel("group");

        metadataList = new ArrayList<>();

        List<Step> stepList = new ArrayList<>();
        stepList.add(step);
        process.setSchritte(stepList);

    }

    @Test
    public void testGetProcessData() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testUpdateProcess() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testCreateProcess() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testDeleteProcess() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

            service = new ProcessService();
            processResource.setId(0);
            Response response = service.deleteProcess(processResource);
            assertEquals(400, response.getStatus());

            processResource.setId(1);
            response = service.deleteProcess(processResource);
            assertEquals(200, response.getStatus());

        }
    }

    @Test
    public void testGetStepList() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testGetStep() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

            service = new ProcessService();
            Response response = service.getStep("", "");
            assertEquals(400, response.getStatus());

            response = service.getStep("1", "1");
            assertNotNull(response);

            RestStepResource data = (RestStepResource) response.getEntity();

            assertEquals("step", data.getSteptitle());

        }
    }

    @Test
    public void testUpdateStep() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testDeleteStep() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

            RestStepResource stepResource = new RestStepResource();

            // no step id given
            Response response = service.deleteStep("0", stepResource);
            assertEquals(400, response.getStatus());

            // deletion successful
            stepResource.setStepId(1);
            response = service.deleteStep("0", stepResource);
            assertEquals(200, response.getStatus());

        }
    }

    @Test
    public void testCloseStep() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testGetJournal() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testUpdateJournalEntry() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testCreateJournalEntry() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testDeleteJournalEntry() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testGetProperties() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

            Response response = service.getProperties(null);
            assertEquals(400, response.getStatus());

            response = service.getProperties("1");
            @SuppressWarnings("unchecked")
            List<RestPropertyResource> data = (List<RestPropertyResource>) response.getEntity();
            assertEquals("title", data.get(0).getName());
            assertEquals("value", data.get(0).getValue());

        }
    }

    @Test
    public void testGetProperty() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testUpdateProperty() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testCreateProperty() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testGetMetadata() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

            Response response = service.getMetadata(null);
            assertEquals(400, response.getStatus());

            response = service.getMetadata("1");
            assertEquals(200, response.getStatus());

            @SuppressWarnings("unchecked")
            List<RestMetadataResource> data = (List<RestMetadataResource>) response.getEntity();
            assertEquals(4, data.size());

        }
    }

    @Test
    public void testUpdateMetadata() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testCreateMetadata() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
    }

    @Test
    public void testDeleteMetadata() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
             MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class);
             MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
             MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
             MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle(Mockito.anyString())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch);
            mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any())).thenReturn(0);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.anyString())).thenReturn(otherProject);
            mockedRulesetManager.when(() -> RulesetManager.getRulesetByName(Mockito.anyString())).thenReturn(otherRuleset);
            mockedDocketManager.when(() -> DocketManager.getDocketByName(Mockito.anyString())).thenReturn(otherDocket);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedPropertyManager.when(() -> PropertyManager.getPropertById(Mockito.anyInt())).thenReturn(property);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedStepManager.when(() -> StepManager.getStepById(Mockito.anyInt())).thenReturn(step);
            mockedUsergroupManager.when(() -> UsergroupManager.getUsergroupByName(Mockito.anyString())).thenReturn(grp);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(Mockito.any(), Mockito.any())).thenReturn(true);
            mockedJournalManager.when(() -> JournalManager.getLogEntriesForProcess(Mockito.anyInt())).thenReturn(journal);
            mockedJournalManager.when(() -> JournalManager.getJournalEntryById(Mockito.anyInt())).thenReturn(entry);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);

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
}
