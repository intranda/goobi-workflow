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

package org.goobi.goobiScript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.User;
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
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, ConfigurationHelper.class, MetadataManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptMetadataAddTest extends AbstractTest {

    private Process process;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private static String resourcesFolder;
    private File processDirectory;
    private File metadataDirectory;

    @Before
    public void setUp() throws Exception {
        // detect root folder
        resourcesFolder = "src/test/resources/"; // for junit tests in eclipse
        if (!Files.exists(Paths.get(resourcesFolder))) {
            resourcesFolder = "target/test-classes/"; // to run mvn test from cli or in jenkins
        }

        // create temp folder
        metadataDirectory = folder.newFolder("metadata");
        processDirectory = new File(metadataDirectory + File.separator + "1");
        processDirectory.mkdirs();

        File images = new File(metadataDirectory + "/1/images/master_testprocess_media");
        images.mkdirs();
        File images2 = new File(metadataDirectory + "/1/images/testprocess_media");
        images2.mkdirs();

        // copy test files into temp folder
        Path metaSource = Paths.get(resourcesFolder + "metadata/1/meta.xml");
        Path metaTarget = Paths.get(processDirectory.getAbsolutePath(), "meta.xml");
        Files.copy(metaSource, metaTarget);

        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);

        PowerMock.mockStatic(ConfigurationHelper.class);
        ConfigurationHelper configurationHelper = EasyMock.createMock(ConfigurationHelper.class);
        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(configurationHelper).anyTimes();
        EasyMock.expect(configurationHelper.getMetsEditorLockingTime()).andReturn(1800000l).anyTimes();
        EasyMock.expect(configurationHelper.isAllowWhitespacesInFolder()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.useS3()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.isUseProxy()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.getGoobiContentServerTimeOut()).andReturn(60000).anyTimes();

        EasyMock.expect(configurationHelper.getConfigurationFolder()).andReturn(resourcesFolder + "config/").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesMasterDirectoryName()).andReturn("master_testprocess_media").anyTimes();
        EasyMock.expect(configurationHelper.isCreateMasterDirectory()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.getProcessOcrTxtDirectoryName()).andReturn("testprocess_txt").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesSourceDirectoryName()).andReturn("src").anyTimes();
        EasyMock.expect(configurationHelper.isCreateSourceFolder()).andReturn(false).anyTimes();
        EasyMock.expect(configurationHelper.getProcessImportDirectoryName()).andReturn("import").anyTimes();
        EasyMock.expect(configurationHelper.getGoobiUrl()).andReturn("http://example.com").anyTimes();
        EasyMock.expect(configurationHelper.getGoobiFolder()).andReturn(resourcesFolder).anyTimes();
        EasyMock.expect(configurationHelper.getScriptsFolder()).andReturn(resourcesFolder + "scripts/").anyTimes();
        EasyMock.expect(configurationHelper.getNumberOfMetaBackups()).andReturn(0).anyTimes();
        EasyMock.expect(configurationHelper.getMetadataFolder()).andReturn(metadataDirectory.toString() + "/").anyTimes();
        EasyMock.expect(configurationHelper.getRulesetFolder()).andReturn(resourcesFolder + "rulesets/").anyTimes();
        EasyMock.expect(configurationHelper.getProcessImagesMainDirectoryName()).andReturn("sample_media").anyTimes();
        EasyMock.expect(configurationHelper.isUseMasterDirectory()).andReturn(true).anyTimes();

        PowerMock.mockStatic(MetadataManager.class);
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));
        MetadataManager.updateJSONMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));
        PowerMock.replay(MetadataManager.class);

        EasyMock.replay(configurationHelper);
        PowerMock.replay(ConfigurationHelper.class);

        User u = new User();
        u.setVorname("firstname");
        u.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(u).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setTitel("testprocess");
        process.setId(1);
        MockProcess.setUpRuleset(process);
        MockProcess.setUpProject(process);

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();
        assertNotNull(fixture);
        assertEquals("metadataAdd", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows to add a new metadata to a METS file.\\naction: metadataAdd\\n\\n# Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the translated display name (e.g. `Main title`).\\nfield: Description\\n\\n# This is used to define the value that shall be stored inside of the newly created metadata field.\\nvalue: This is my content.\\n\\n# Name of the normdatabase, e.g. viaf or gnd.\\nauthorityName: \\n\\n# Define the normdata value for this metadata field.\\nauthorityValue: \\n\\n# Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any` `physical`\\nposition: work\\n\\n# Define if the further processing shall be cancelled for a Goobi process if an error occures (`false`) or if the processing should skip errors and move on (`true`).\\n# This is especially useful if the the value `any` was selected for the position.\\nignoreErrors: true\\n\\n# Define what type of metadata you would like to add. Possible values are `metadata` and `group`. Default is metadata.\\ntype: metadata\\n\\n# Internal name of the group. Use it when the metadata to add is located within a group or if a new group should be added.\\ngroup: ",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataAdd";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "PlaceOfPublication");
        parameters.put("value", "Place");
        parameters.put("position", "work");
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("metadataAdd", results.get(0).getCommand());
    }

    @Test
    public void testExecute() throws Exception {
        // test process has 3 metadata fields
        Fileformat ff = process.readMetadataFile();
        DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());

        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataAdd";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "PlaceOfPublication");
        parameters.put("value", "Place");
        parameters.put("position", "work");
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();

        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        // now we have 4 fields
        ff = process.readMetadataFile();
        monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(4, monograph.getAllMetadata().size());
    }

    @Test
    public void testAddMetadataToGroup() throws Exception {
        // test process has no metadata group
        Fileformat ff = process.readMetadataFile();
        DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertNull(monograph.getAllMetadataGroups());

        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataAdd";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "junitMetadata");
        parameters.put("value", "value");
        parameters.put("position", "work");
        parameters.put("type", "group");
        parameters.put("group", "junitgrp");
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();

        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        // now we have a group with a new metadata field
        ff = process.readMetadataFile();
        monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(1, monograph.getAllMetadataGroups().size());
    }

    @Test
    public void testAddMetadataWithAuthorityData() throws Exception {
        // test process has 3 metadata fields
        Fileformat ff = process.readMetadataFile();
        DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());

        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataAdd";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "junitMetadata");
        parameters.put("value", "value");
        parameters.put("position", "work");
        parameters.put("type", "metadata");

        parameters.put("authorityName", "gnd");
        parameters.put("authorityValue", "https://d-nb.info/gnd/12345");

        parameters.put("group", "");
        GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();

        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        // now we have 4 fields
        ff = process.readMetadataFile();
        monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(4, monograph.getAllMetadata().size());
        Metadata meta = null;
        for (Metadata md : monograph.getAllMetadata()) {
            if ("junitMetadata".equals(md.getType().getName())) {
                meta = md;
            }
        }
        assertNotNull(meta);
        assertEquals("value", meta.getValue());
        assertEquals("gnd", meta.getAuthorityID());
        assertEquals("http://d-nb.info/gnd/", meta.getAuthorityURI());
        assertEquals("https://d-nb.info/gnd/12345", meta.getAuthorityValue());
    }
}
