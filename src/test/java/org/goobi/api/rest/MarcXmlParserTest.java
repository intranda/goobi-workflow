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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.jdom2.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import io.goobi.workflow.harvester.repository.Repository;
import jakarta.ws.rs.core.Response;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class MarcXmlParserTest extends AbstractTest {

    @TempDir
    private Path tempFolder;

    private Path testFile;

    private Path rulesetFile;

    private Path anchorFile;
    private Path volumeFile;

    private Repository repository;

    private Prefs prefs;

    private Process process;
    private Project project;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        Files.createDirectories(template);

        rulesetFile = Paths.get(samplefiles.toString(), "ruleset.xml");
        prefs = new Prefs();
        prefs.loadPrefs(rulesetFile.toString());

        Path marcFile = Paths.get(samplefiles.toString(), "marc_monograph.xml");
        testFile = Paths.get(tempFolder.toString(), "b20411595_marc.xml");
        anchorFile = Paths.get(tempFolder.toString(), "marc_anchor.xml");
        volumeFile = Paths.get(tempFolder.toString(), "marc_volume.xml");
        Files.copy(marcFile, testFile);
        Files.copy(Paths.get(samplefiles.toString(), "marc_anchor.xml"), anchorFile);
        Files.copy(Paths.get(samplefiles.toString(), "marc_volume.xml"), volumeFile);

        Map<String, String> parameterMap = new HashMap<>();
        parameterMap.put("url", "url");
        parameterMap.put("metadataPrefix", "metadataPrefix");

        repository = Mockito.mock(Repository.class);
        Mockito.when(repository.getName()).thenReturn("test");
        Mockito.when(repository.checkAndCreateDownloadFolder(Mockito.anyString())).thenReturn(tempFolder);
        Mockito.when(repository.getParameter()).thenReturn(parameterMap);

        Mockito.when(repository.downloadOaiRecord(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
                .thenReturn(anchorFile);
        process = Mockito.mock(Process.class);
        Ruleset ruleset = Mockito.mock(Ruleset.class);
        project = Mockito.mock(Project.class);

        Mockito.when(process.writeMetadataFile(Mockito.any())).thenReturn(true);
        Mockito.when(process.getRegelsatz()).thenReturn(ruleset);
        Mockito.when(process.getProjekt()).thenReturn(project);
        Mockito.when(process.getDocket()).thenReturn(null);
        Mockito.when(process.getId()).thenReturn(1);

        process.setProjekt(Mockito.any());
        Mockito.when(ruleset.getPreferences()).thenReturn(prefs);

    }

    @Test
    public void testAuthenticationMethods() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessService> mockedProcessService = Mockito.mockStatic(ProcessService.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("templateName")).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("processTitle")).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessService.when(() -> ProcessService.prepareProcess(Mockito.anyString(), Mockito.any())).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(Collections.emptyList());

            MarcXmlParser fixture = new MarcXmlParser();
            List<AuthenticationMethodDescription> desc = fixture.getAuthenticationMethods();
            assertEquals(1, desc.size());
            assertEquals("Upload a new marc record and create a new process", desc.get(0).getDescription());
            assertEquals("POST", desc.get(0).getMethodType());
            assertEquals("/metadata/marc/\\w+/\\w+/\\w+", desc.get(0).getUrl());

        }
    }

    @Test
    public void testReadMetadataFile() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessService> mockedProcessService = Mockito.mockStatic(ProcessService.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("templateName")).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("processTitle")).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessService.when(() -> ProcessService.prepareProcess(Mockito.anyString(), Mockito.any())).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(Collections.emptyList());

            MarcXmlParser fixture = new MarcXmlParser();

            try (InputStream in = Files.newInputStream(testFile)) {
                Fileformat ff = fixture.readMetadataFile(in, prefs);
                assertNotNull(ff);
                assertEquals("Monograph", ff.getDigitalDocument().getLogicalDocStruct().getType().getName());
            }

        }
    }

    @Test
    public void testExtendMetadata() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessService> mockedProcessService = Mockito.mockStatic(ProcessService.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("templateName")).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("processTitle")).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessService.when(() -> ProcessService.prepareProcess(Mockito.anyString(), Mockito.any())).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(Collections.emptyList());

            MarcXmlParser fixture = new MarcXmlParser();
            fixture.extendMetadata(repository, volumeFile);
            Document doc = XmlTools.readDocumentFromFile(volumeFile);
            assertEquals("collection", doc.getRootElement().getName());

        }
    }

    @Test
    public void testReplaceMetadata() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessService> mockedProcessService = Mockito.mockStatic(ProcessService.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("templateName")).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("processTitle")).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessService.when(() -> ProcessService.prepareProcess(Mockito.anyString(), Mockito.any())).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(Collections.emptyList());

            MarcXmlParser fixture = new MarcXmlParser();
            try (InputStream in = Files.newInputStream(testFile)) {
                Response resp = fixture.replaceMetadata(1, in);
                assertEquals(200, resp.getStatus());
            }

        }
    }

    @Test
    public void testCreateNewProcess() throws Exception {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class);
                MockedStatic<ProcessService> mockedProcessService = Mockito.mockStatic(ProcessService.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("templateName")).thenReturn(process);
            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("processTitle")).thenReturn(null);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessService.when(() -> ProcessService.prepareProcess(Mockito.anyString(), Mockito.any())).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(Collections.emptyList());

            MarcXmlParser fixture = new MarcXmlParser();
            try (InputStream in = Files.newInputStream(testFile)) {
                Response resp = fixture.createNewProcess("projectName", "templateName", "processTitle", in);
                assertEquals(204, resp.getStatus());
            }

        }
    }
}
