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
public class GoobiScriptMetadataReplaceAdvancedTest extends AbstractTest {

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

            GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();
            assertNotNull(fixture);
            assertEquals("metadataReplaceAdvanced", fixture.getAction());

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

            GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();
            assertNotNull(fixture);
            assertEquals(
                    "---\\n# This GoobiScript allows to replace an existing metadata within the METS file.\\naction: metadataReplaceAdvanced"
                            + "\\n\\n# Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not "
                            + "the translated display name (e.g. `Main title`).\\nfield: Description\\n\\n# Regular expression to replace old "
                            + "term with the new term\\nvalue: s/old value/new value/g\\n\\n# Name of the authority file, e.g. viaf or gnd.\\n"
                            + "authorityName: \\n\\n# Regular expression to replace old authority data value with new authority data value. "
                            + "e.g. `s/http:(.+)/https:$1/g` to replace http with https in all uris\\nauthorityValue: \\n\\n# Define where in "
                            + "the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` "
                            + "`any` `physical`\\nposition: work\\n\\n# If the metadata to change is in a group, set the internal name of the "
                            + "metadata group name here.\\ngroup: ",
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
            assertEquals(4, monograph.getAllMetadata().size());
            title = null;
            for (Metadata md : monograph.getAllMetadata()) {
                if ("TitleDocMain".equals(md.getType().getName())) {
                    title = md;
                }
            }
            assertNotNull(title);
            assertEquals("new title", title.getValue());

        }
    }

    @Test
    public void testMetadataWithAuthorityData() throws Exception {
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
            Metadata title = null;
            for (Metadata md : monograph.getAllMetadata()) {
                if ("TitleDocMain".equals(md.getType().getName())) {
                    title = md;
                    title.setAuthorityFile("gnd", "https://d-nb.info/gnd/", "https://d-nb.info/gnd/12345");
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
            parameters.put("authorityValue",
                    "s/https:\\/\\/d-nb.info\\/gnd\\/(.+)/https:\\/\\/viaf.org\\/processed\\/WKP|$1?httpAccept=text\\/xml/g");
            parameters.put("position", "work");
            parameters.put("group", "");
            GoobiScriptMetadataReplaceAdvanced fixture = new GoobiScriptMetadataReplaceAdvanced();

            List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
            fixture.execute(results.get(0));
            // now we have a new value
            ff = process.readMetadataFile();
            monograph = ff.getDigitalDocument().getLogicalDocStruct();
            assertEquals(4, monograph.getAllMetadata().size());
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

}
