package de.sub.goobi.export.dms;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.PluginType;
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
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.mock.MockProcess;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MetadatenHelper.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class ExportDmsTest extends AbstractTest {

    private Process testProcess = null;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        ConfigurationHelper.getInstance().setParameter("goobiUrl", "http://127.0.0.1/goobi");
        testProcess = MockProcess.createProcess();

        PowerMock.mockStatic(MetadatenHelper.class);
        EasyMock.expect(MetadatenHelper.getMetaFileType(EasyMock.anyString())).andReturn("metsmods").anyTimes();
        EasyMock.expect(MetadatenHelper.getFileformatByName(EasyMock.anyString(), EasyMock.anyObject(Ruleset.class)))
                .andReturn(new MetsMods(testProcess.getRegelsatz().getPreferences()))
                .anyTimes();
        EasyMock.expect(MetadatenHelper.getExportFileformatByName(EasyMock.anyString(), EasyMock.anyObject(Ruleset.class)))
                .andReturn(new MetsModsImportExport(testProcess.getRegelsatz().getPreferences()))
                .anyTimes();
        PowerMock.replay(MetadatenHelper.class);

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString())).andReturn("").anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());

        PowerMock.replay(Helper.class);

    }

    @Test
    public void testExportDms() {
        ExportDms dms = new ExportDms();
        assertNotNull(dms);
    }

    @Test
    public void testExportDmsBoolean() {
        ExportDms dms = new ExportDms(false);
        assertNotNull(dms);
    }

    @Test
    public void testSetExportFulltext() {
        ExportDms dms = new ExportDms(false);
        dms.setExportFulltext(false);
        assertNotNull(dms);
    }

    @Test
    public void testSetExportImages() {
        ExportDms dms = new ExportDms(false);
        dms.setExportImages(false);
        assertNotNull(dms);
    }

    @Test
    public void testFulltextDownload() throws SwapException, DAOException, IOException, InterruptedException {
        ExportDms dms = new ExportDms(false);
        dms.setExportFulltext(true);
        Path dest = folder.newFolder("text").toPath();
        Files.createDirectories(dest);
        dms.fulltextDownload(testProcess, dest, testProcess.getTitel(), "qwertzu");
        assertNotNull(StorageProvider.getInstance().list(dest.toString()));
    }

    @Test
    public void testImageDownload() throws SwapException, DAOException, IOException, InterruptedException {
        ExportDms dms = new ExportDms(true);
        dms.setExportFulltext(true);
        Path dest = folder.newFolder("images").toPath();
        Files.createDirectories(dest);
        dms.imageDownload(testProcess, dest, testProcess.getTitel(), "qwertzu");
        assertNotNull(StorageProvider.getInstance().list(dest.toString()));
    }

    @Test
    public void testStartExportProcessString()
            throws DocStructHasNoTypeException, PreferencesException, WriteException, MetadataTypeNotAllowedException, ExportFileException,
            UghHelperException, ReadException, SwapException, DAOException, TypeNotAllowedForParentException, IOException, InterruptedException {
        ExportDms dms = new ExportDms();
        dms.setExportFulltext(true);
        assertTrue(dms.startExport(testProcess));
    }

    @Test
    public void testGetType() {
        ExportDms dms = new ExportDms(true);
        assertEquals(PluginType.Export, dms.getType());
    }

    @Test
    public void testGetTitle() {
        ExportDms dms = new ExportDms(true);
        assertEquals("ExportDms", dms.getTitle());
    }

    @Test
    public void testGetDescription() {
        ExportDms dms = new ExportDms(true);
        assertEquals("ExportDms", dms.getDescription());
    }
}
