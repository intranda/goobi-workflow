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
public class GoobiScriptMetadataReplaceAdvancedTest extends AbstractTest {

    private Process process;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private static String resourcesFolder;
    private File processDirectory;
    private File metadataDirectory;

    @SuppressWarnings("unchecked")
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
        GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();
        assertNotNull(fixture);
        assertEquals("metadataReplaceAdvanced", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows to replace an existing metadata within the METS file.\\naction: metadataReplaceAdvanced\\n\\n# Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the translated display name (e.g. `Main title`).\\nfield: Description\\n\\n# Regular expression to replace old term with the new term\\nvalue: s/old value/new value/g\\n\\n# Name of the normdatabase, e.g. viaf or gnd.\\nauthorityName: \\n\\n# Regular expression to replace old normdata value with new normdata value. e.g. `s/http:(.+)/https:$1/g` to replace http with https in all uris\\nauthorityValue: \\n\\n# Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any` `physical`\\nposition: work\\n\\n# If the metadata to change is in a group, set the internal name of the metadata group name here.\\ngroup: ",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataReplaceAdvanced";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "TitleDocMain");
        parameters.put("value", "s/main (title)/new \\/$1/g");
        parameters.put("position", "work");
        parameters.put("group", "");
        GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("metadataReplaceAdvanced", results.get(0).getCommand());
    }

    @Test
    public void testExecute() throws Exception {
        // test process has 3 metadata fields
        Fileformat ff = process.readMetadataFile();
        DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());
        Metadata title = null;
        for (Metadata md : monograph.getAllMetadata()) {
            if ("TitleDocMain".equals(md.getType().getName())) {
                title = md;
            }
        }
        assertNotNull(title);
        assertEquals("main title", title.getValue());

        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataReplaceAdvanced";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "TitleDocMain");
        parameters.put("value", "s/main (title)/new $1/g");
        parameters.put("position", "work");
        parameters.put("group", "");
        GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();

        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        // now we have a new value
        ff = process.readMetadataFile();
        monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());
        title = null;
        for (Metadata md : monograph.getAllMetadata()) {
            if ("TitleDocMain".equals(md.getType().getName())) {
                title = md;
            }
        }
        assertNotNull(title);
        assertEquals("new title", title.getValue());
    }

    @Test
    public void testMetadataWithAuthorityData() throws Exception {
        // test process has 3 metadata fields
        Fileformat ff = process.readMetadataFile();
        DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());
        Metadata title = null;
        for (Metadata md : monograph.getAllMetadata()) {
            if ("TitleDocMain".equals(md.getType().getName())) {
                title = md;
                title.setAutorityFile("gnd", "https://d-nb.info/gnd/", "https://d-nb.info/gnd/12345");
                process.writeMetadataFile(ff);
            }
        }
        assertNotNull(title);
        assertEquals("main title", title.getValue());

        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "metadataReplaceAdvanced";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("field", "TitleDocMain");
        parameters.put("value", "s/main (title)/new $1/g");
        parameters.put("authorityName", "gnd");
        parameters.put("authorityValue", "s/https:\\/\\/d-nb.info\\/gnd\\/(.+)/https:\\/\\/viaf.org\\/processed\\/WKP|$1?httpAccept=text\\/xml/g");
        parameters.put("position", "work");
        parameters.put("group", "");
        GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();

        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        // now we have a new value
        ff = process.readMetadataFile();
        monograph = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals(3, monograph.getAllMetadata().size());
        title = null;
        for (Metadata md : monograph.getAllMetadata()) {
            if ("TitleDocMain".equals(md.getType().getName())) {
                title = md;
            }
        }
        assertNotNull(title);
        assertEquals("new title", title.getValue());

        assertEquals("https://viaf.org/processed/WKP|12345?httpAccept=text/xml", title.getAuthorityValue());
    }

}
