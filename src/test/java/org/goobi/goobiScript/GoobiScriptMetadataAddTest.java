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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;

@ExtendWith(MockitoExtension.class)
public class GoobiScriptMetadataAddTest extends AbstractTest {

    private Process process;

    @TempDir
    private Path tempDir;
    private static String resourcesFolder;
    private File processDirectory;
    private File metadataDirectory;

    private ConfigurationHelper configurationHelper;
    private User u;

    @BeforeEach
    public void setUp() throws Exception {
        // detect root folder
        resourcesFolder = "src/test/resources/"; // for junit tests in eclipse
        if (!Files.exists(Paths.get(resourcesFolder))) {
            resourcesFolder = "target/test-classes/"; // to run mvn test from cli or in jenkins
        }

        // create temp folder
        metadataDirectory = tempDir.toFile();
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

        configurationHelper = Mockito.mock(ConfigurationHelper.class);

        u = new User();
        u.setVorname("firstname");
        u.setNachname("lastname");

        process = new Process();
        process.setTitel("testprocess");
        process.setId(1);
        MockProcess.setUpRuleset(process);
        MockProcess.setUpProject(process);

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();
            assertNotNull(fixture);
            assertEquals("metadataAdd", fixture.getAction());

        }
    }

    @Test
    public void testSampleCall() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();
            assertNotNull(fixture);
            assertEquals(
                    "---\\n# This GoobiScript allows to add a new metadata to a METS file.\\naction: metadataAdd\\n\\n# Internal "
                            + "name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the "
                            + "translated display name (e.g. `Main title`).\\nfield: Description\\n\\n# This is used to define the "
                            + "value that shall be stored inside of the newly created metadata field.\\nvalue: This is my content."
                            + "\\n\\n# Name of the authority file, e.g. viaf or gnd.\\nauthorityName: \\n\\n# Define the authority "
                            + "data value for this metadata field.\\nauthorityValue: \\n\\n# Define where in the hierarchy of the "
                            + "METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any` `physical`"
                            + "\\nposition: work\\n\\n# Define if the further processing shall be cancelled for a Goobi process if "
                            + "an error occures (`false`) or if the processing should skip errors and move on (`true`).\\n# This is "
                            + "especially useful if the the value `any` was selected for the position.\\nignoreErrors: true\\n\\n# "
                            + "Define what type of metadata you would like to add. Possible values are `metadata` and `group`. Default"
                            + " is metadata.\\ntype: metadata\\n\\n# Internal name of the group. Use it when the metadata to add is "
                            + "located within a group or if a new group should be added.\\ngroup: ",
                    fixture.getSampleCall());

        }
    }

    @Test
    public void testPrepare() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

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
    }

    @Test
    public void testExecute() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            // test process has 3 metadata fields
            Fileformat ff = process.readMetadataFile();
            DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
            assertEquals(4, monograph.getAllMetadata().size());

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            String command = "metadataAdd";
            Map<String, String> parameters = new HashMap<>();
            parameters.put("field", "PublicationYear");
            parameters.put("value", "1234");
            parameters.put("position", "work");
            GoobiScriptMetadataAdd fixture = new GoobiScriptMetadataAdd();

            List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
            fixture.execute(results.get(0));
            // now we have 5 fields
            ff = process.readMetadataFile();
            monograph = ff.getDigitalDocument().getLogicalDocStruct();
            assertEquals(5, monograph.getAllMetadata().size());

        }
    }

    @Test
    public void testAddMetadataToGroup() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

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
    }

    @Test
    public void testAddMetadataWithAuthorityData() throws Exception {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);
            Mockito.lenient().when(configurationHelper.getMetsEditorLockingTime()).thenReturn(1800000L);
            Mockito.lenient().when(configurationHelper.isAllowWhitespacesInFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.isUseProxy()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getGoobiContentServerTimeOut()).thenReturn(60000);
            Mockito.lenient().when(configurationHelper.getConfigurationFolder()).thenReturn(resourcesFolder + "config/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMasterDirectoryName()).thenReturn("master_testprocess_media");
            Mockito.lenient().when(configurationHelper.isCreateMasterDirectory()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessOcrTxtDirectoryName()).thenReturn("testprocess_txt");
            Mockito.lenient().when(configurationHelper.getProcessImagesSourceDirectoryName()).thenReturn("src");
            Mockito.lenient().when(configurationHelper.isCreateSourceFolder()).thenReturn(false);
            Mockito.lenient().when(configurationHelper.getProcessImportDirectoryName()).thenReturn("import");
            Mockito.lenient().when(configurationHelper.getGoobiUrl()).thenReturn("http://example.com");
            Mockito.lenient().when(configurationHelper.getGoobiFolder()).thenReturn(resourcesFolder);
            Mockito.lenient().when(configurationHelper.getScriptsFolder()).thenReturn(resourcesFolder + "scripts/");
            Mockito.lenient().when(configurationHelper.getNumberOfMetaBackups()).thenReturn(0);
            Mockito.lenient().when(configurationHelper.getMetadataFolder()).thenReturn(metadataDirectory.toString() + "/");
            Mockito.lenient().when(configurationHelper.getRulesetFolder()).thenReturn(resourcesFolder + "rulesets/");
            Mockito.lenient().when(configurationHelper.getProcessImagesMainDirectoryName()).thenReturn("sample_media");
            Mockito.lenient().when(configurationHelper.isUseMasterDirectory()).thenReturn(true);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            // test process has 3 metadata fields
            Fileformat ff = process.readMetadataFile();
            DocStruct monograph = ff.getDigitalDocument().getLogicalDocStruct();
            assertEquals(4, monograph.getAllMetadata().size());

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
            assertEquals(5, monograph.getAllMetadata().size());
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
}
