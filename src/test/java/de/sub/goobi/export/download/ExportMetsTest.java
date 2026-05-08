package de.sub.goobi.export.download;

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
 * 
 */
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.Prefs;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class ExportMetsTest extends AbstractTest {

    private Process testProcess = null;
    private Prefs prefs;
    private MetsMods metsMods;
    private MetsModsImportExport metsModsImportExport;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.configFileName = goobiFolder.toString();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        testProcess = MockProcess.createProcess();
        prefs = testProcess.getRegelsatz().getPreferences();
        metsMods = new MetsMods(prefs);
        metsModsImportExport = new MetsModsImportExport(prefs);

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<MetadatenHelper> mockedMetadatenHelper = Mockito.mockStatic(MetadatenHelper.class);
             MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedMetadatenHelper.when(() -> MetadatenHelper.getMetaFileType(Mockito.anyString())).thenReturn("metsmods");
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsMods);
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getExportFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsModsImportExport);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");


            ExportMets exportMets = new ExportMets();
            assertNotNull(exportMets);
    
        }
}

    @Test
    public void testGetProblemsInitiallyEmpty() {
        try (MockedStatic<MetadatenHelper> mockedMetadatenHelper = Mockito.mockStatic(MetadatenHelper.class);
             MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedMetadatenHelper.when(() -> MetadatenHelper.getMetaFileType(Mockito.anyString())).thenReturn("metsmods");
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsMods);
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getExportFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsModsImportExport);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");


            ExportMets exportMets = new ExportMets();
            assertNotNull(exportMets.getProblems());
            assertTrue(exportMets.getProblems().isEmpty());
    
        }
}

    @Test
    public void testStartExport() throws Exception {
        try (MockedStatic<MetadatenHelper> mockedMetadatenHelper = Mockito.mockStatic(MetadatenHelper.class);
             MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedMetadatenHelper.when(() -> MetadatenHelper.getMetaFileType(Mockito.anyString())).thenReturn("metsmods");
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsMods);
                        mockedMetadatenHelper.when(() -> MetadatenHelper.getExportFileformatByName(Mockito.anyString(), Mockito.any(Ruleset.class))).thenReturn(metsModsImportExport);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");


            Path destination = tempDir.resolve("export");
            Files.createDirectories(destination);
            ExportMets exportMets = new ExportMets();
            assertNotNull(exportMets);
            exportMets.startExport(testProcess, destination.toString());
    
        }
}

}
