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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.jdom2.Document;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import io.goobi.workflow.harvester.repository.Repository;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, ProcessService.class, StepManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class MarcXmlParserTest extends AbstractTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path tempFolder;

    private Path testFile;

    private Path rulesetFile;

    private Path anchorFile;
    private Path volumeFile;

    private Repository repository;

    private Prefs prefs;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        tempFolder = folder.newFolder().toPath();
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

        repository = EasyMock.createMock(Repository.class);
        EasyMock.expect(repository.getName()).andReturn("test").anyTimes();
        EasyMock.expect(repository.checkAndCreateDownloadFolder(EasyMock.anyString())).andReturn(tempFolder).anyTimes();
        EasyMock.expect(repository.getParameter()).andReturn(parameterMap).anyTimes();

        EasyMock.expect(repository.downloadOaiRecord(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyObject()))
                .andReturn(anchorFile)
                .anyTimes();
        EasyMock.replay(repository);

        Process process = EasyMock.createMock(Process.class);
        Ruleset ruleset = EasyMock.createMock(Ruleset.class);
        Project project = EasyMock.createMock(Project.class);

        EasyMock.expect(process.writeMetadataFile(EasyMock.anyObject())).andReturn(true).anyTimes();
        EasyMock.expect(process.getRegelsatz()).andReturn(ruleset).anyTimes();
        EasyMock.expect(process.getProjekt()).andReturn(project).anyTimes();
        EasyMock.expect(process.getDocket()).andReturn(null).anyTimes();
        EasyMock.expect(process.getId()).andReturn(1).anyTimes();

        process.setProjekt(EasyMock.anyObject());
        EasyMock.expect(ruleset.getPreferences()).andReturn(prefs).anyTimes();
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getProcessByExactTitle("templateName")).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getProcessByExactTitle("processTitle")).andReturn(null).anyTimes();

        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.mockStatic(ProjectManager.class);
        EasyMock.expect(ProjectManager.getProjectByName(EasyMock.anyObject())).andReturn(project).anyTimes();

        PowerMock.mockStatic(ProcessService.class);
        EasyMock.expect(ProcessService.prepareProcess(EasyMock.anyString(), EasyMock.anyObject())).andReturn(process).anyTimes();

        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(Collections.emptyList()).anyTimes();

        EasyMock.expectLastCall();
        EasyMock.replay(ruleset);
        EasyMock.replay(process);
        PowerMock.replayAll();

    }

    @Test
    public void testAuthenticationMethods() {
        MarcXmlParser fixture = new MarcXmlParser();
        List<AuthenticationMethodDescription> desc = fixture.getAuthenticationMethods();
        assertEquals(1, desc.size());
        assertEquals("Upload a new marc record and create a new process", desc.get(0).getDescription());
        assertEquals("POST", desc.get(0).getMethodType());
        assertEquals("/metadata/marc/\\w+/\\w+/\\w+", desc.get(0).getUrl());
    }

    @Test
    public void testReadMetadataFile() throws Exception {
        MarcXmlParser fixture = new MarcXmlParser();

        try (InputStream in = Files.newInputStream(testFile)) {
            Fileformat ff = fixture.readMetadataFile(in, prefs);
            assertNotNull(ff);
            assertEquals("Monograph", ff.getDigitalDocument().getLogicalDocStruct().getType().getName());
        }
    }

    @Test
    public void testExtendMetadata() throws Exception {
        MarcXmlParser fixture = new MarcXmlParser();
        fixture.extendMetadata(repository, volumeFile);
        Document doc = XmlTools.readDocumentFromFile(volumeFile);
        assertEquals("collection", doc.getRootElement().getName());
    }

    @Test
    public void testReplaceMetadata() throws Exception {
        MarcXmlParser fixture = new MarcXmlParser();
        try (InputStream in = Files.newInputStream(testFile)) {
            Response resp = fixture.replaceMetadata(1, in);
            assertEquals(200, resp.getStatus());
        }
    }

    @Test
    public void testCreateNewProcess() throws Exception {
        MarcXmlParser fixture = new MarcXmlParser();
        try (InputStream in = Files.newInputStream(testFile)) {
            Response resp = fixture.createNewProcess("projectName", "templateName", "processTitle", in);
            assertEquals(204, resp.getStatus());
        }
    }
}
