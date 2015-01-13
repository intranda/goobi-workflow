package de.sub.goobi.export.dms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import static org.junit.Assert.*;

import java.io.IOException;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.production.enums.PluginType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.MetsMods;
import ugh.fileformats.mets.MetsModsImportExport;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.mock.MockProcess;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MetadatenHelper.class)
public class AutomaticDmsExportTest {

    //    private static final String RULESET_NAME = "ruleset.xml";

    private Process testProcess = null;
    //    private File processFolder = null;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        testProcess = MockProcess.createProcess(folder);      
    }
    
    @Test
    public void testConstructor() {
        AutomaticDmsExport exp = new AutomaticDmsExport();
        assertNotNull(exp);
    }

    @Test
    public void testConstructor2() {
        AutomaticDmsExport exp = new AutomaticDmsExport(false);
        assertNotNull(exp);
    }

    @Test
    public void testExportFulltext() {
        AutomaticDmsExport exp = new AutomaticDmsExport(false);
        exp.setExportFulltext(false);

    }

    @Test
    public void testExport() throws WriteException, PreferencesException, DocStructHasNoTypeException, MetadataTypeNotAllowedException,
            ExportFileException, UghHelperException, SwapException, DAOException, TypeNotAllowedForParentException, IOException, InterruptedException {

        //        prepareMocking();

        AutomaticDmsExport exp = new AutomaticDmsExport(true);
        exp.setExportFulltext(true);
        exp.startExport(testProcess);
    }

    public void prepareMocking() throws IOException, PreferencesException {
        PowerMock.mockStatic(MetadatenHelper.class);
        EasyMock.expect(MetadatenHelper.getMetaFileType(EasyMock.anyString())).andReturn("metsmods");
        //        EasyMock.expect(MetadatenHelper.getMetaFileType(processFolder.getAbsolutePath() + File.separator + "meta.xml")).andReturn("metsmods");
        EasyMock.expect(MetadatenHelper.getFileformatByName("metsmods", testProcess.getRegelsatz())).andReturn(
                new MetsMods(testProcess.getRegelsatz().getPreferences()));
        EasyMock.expect(MetadatenHelper.getExportFileformatByName("Mets", testProcess.getRegelsatz())).andReturn(
                new MetsModsImportExport(testProcess.getRegelsatz().getPreferences()));
        EasyMock.expect(MetadatenHelper.getExportFileformatByName("Mets", testProcess.getRegelsatz())).andReturn(
                new MetsModsImportExport(testProcess.getRegelsatz().getPreferences()));
        EasyMock.expect(MetadatenHelper.getExportFileformatByName("Mets", testProcess.getRegelsatz())).andReturn(
                new MetsModsImportExport(testProcess.getRegelsatz().getPreferences()));
        EasyMock.expectLastCall();

        PowerMock.replayAll();
    }

    @Test
    public void testGetType() {
        AutomaticDmsExport dms = new AutomaticDmsExport();
        assertEquals(PluginType.Export, dms.getType());
    }

    @Test
    public void testGetTitle() {
        AutomaticDmsExport dms = new AutomaticDmsExport();
        assertEquals("AutomaticDmsExport", dms.getTitle());
    }

    @Test
    public void testGetDescription() {
        AutomaticDmsExport dms = new AutomaticDmsExport();
        assertEquals("AutomaticDmsExport", dms.getDescription());
    }
}
