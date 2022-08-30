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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.mock.MockUploadedFile;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, HttpSession.class, Helper.class, MetadataManager.class, ProcessManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })

public class FileManipulationTest extends AbstractTest {

    private Metadaten metadataBean;
    private Process testProcess;

    @Before
    public void setUp() throws Exception {

        prepareMocking();

        ConfigurationHelper.setImagesPath("/tmp/");
        testProcess = MockProcess.createProcess();
        testProcess.setId(1);

        metadataBean = new Metadaten();
        metadataBean.setMyBenutzerID("1");
        metadataBean.setMyProzess(testProcess);
        metadataBean.XMLlesenStart();
    }

    @After
    public void tearDown() throws Exception {
        // delete uploaded files, if exists
        Path file1 = Paths.get(testProcess.getImagesTifDirectory(false), "fixture.tif");
        Path file2 = Paths.get(testProcess.getImagesTifDirectory(false), "fixture2.tif");
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
    }

    @Test
    public void testFileManipulation() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        assertNotNull(fixture);
    }

    @Test
    public void testGetCurrentFolder() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        fixture.setCurrentFolder("testprocess_media");
        assertEquals("testprocess_media", fixture.getCurrentFolder());
    }

    @Test
    public void testGetUploadedFile() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);

        InputStream stream = new FileInputStream(metadataBean.getMyProzess().getProcessDataDirectory() + "/images/testprocess_media/00000001.tif");
        Part uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");
        fixture.setUploadedFile(uploadedFile);
        assertEquals(uploadedFile, fixture.getUploadedFile());
    }

    @Test
    public void testUploadedFileName() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        String filename = "test.tif";
        fixture.setUploadedFileName(filename);
        assertEquals(filename, fixture.getUploadedFileName());
    }

    @Test
    public void testInsertPage() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        String pageNo = "5";
        fixture.setInsertPage(pageNo);
        assertEquals(pageNo, fixture.getInsertPage());
    }

    @Test
    public void testUploadFileWithoutSelection() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);
        fixture.uploadFile();
        assertNull(fixture.getUploadedFile());
    }

    @Test
    public void testUploadFile() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);
        Path p = Paths.get("src/test/resources/file_example_TIFF_1MB.tif");
        if (!Files.exists(p)) {
            p = Paths.get("target/test-classes/file_example_TIFF_1MB.tif");
        }

        InputStream stream = Files.newInputStream(p);
        Part uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("lastPage");
        fixture.uploadFile();
        Path testfile = Paths.get(testProcess.getImagesTifDirectory(false), "fixture.tif");
        assertTrue(Files.exists(testfile));

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture2");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("1");
        fixture.uploadFile();
        testfile = Paths.get(testProcess.getImagesTifDirectory(false), "fixture2.tif");
        assertTrue(Files.exists(testfile));
    }

    @Test
    public void testInsertMode() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        String mode = "uncounted";
        fixture.setInsertMode(mode);
        assertEquals(mode, fixture.getInsertMode());
    }

    @Test
    public void testImageSelection() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        String selection = "1";
        fixture.setImageSelection(selection);
        assertEquals(selection, fixture.getImageSelection());
    }

    //    @Test
    public void testDownloadFile() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);
        Path p = Paths.get("src/test/resources/file_example_TIFF_1MB.tif");
        if (!Files.exists(p)) {
            p = Paths.get("target/test-classes/file_example_TIFF_1MB.tif");
        }
        InputStream stream = Files.newInputStream(p);

        Part uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("lastPage");
        fixture.uploadFile();

        fixture.setImageSelection("1");
        fixture.downloadFile();
    }

    @Test
    public void testExportFilesWithoutSelection() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        fixture.exportFiles();
        assertNotNull(fixture.getSelectedFiles());
    }

    @Test
    public void testSelectedFiles() {
        FileManipulation fixture = new FileManipulation(metadataBean);

        List<String> filesToDownload = new ArrayList<>();
        filesToDownload.add("1");
        fixture.setSelectedFiles(filesToDownload);

        assertEquals(filesToDownload, fixture.getSelectedFiles());

    }

    @Test
    public void testDeleteFilesAfterMove() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        fixture.setDeleteFilesAfterMove(false);
        assertFalse(fixture.isDeleteFilesAfterMove());
        fixture.setDeleteFilesAfterMove(true);
        assertTrue(fixture.isDeleteFilesAfterMove());
    }

    @Test
    public void testExportFiles() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);

        List<String> filesToDownload = new ArrayList<>();
        filesToDownload.add("1");

        fixture.setDeleteFilesAfterMove(false);
        fixture.setSelectedFiles(filesToDownload);
        fixture.exportFiles();

        Path exportFolder = Paths.get("src/test/resources/tmp/fileupload/testprocess");
        if (!Files.exists(exportFolder)) {
            exportFolder = Paths.get("target/test-classes/tmp/fileupload/testprocess");
        }
        assertTrue(Files.exists(exportFolder));

        // cleanup
        StorageProvider.getInstance().deleteDir(exportFolder);
    }

    @Test
    public void testIsMoveFilesInAllFolder() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        fixture.setMoveFilesInAllFolder(false);
        assertFalse(fixture.isMoveFilesInAllFolder());
        fixture.setMoveFilesInAllFolder(true);
        assertTrue(fixture.isMoveFilesInAllFolder());
    }

    @Test
    public void testGetAllImportFolder() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);

        List<String> filesToDownload = new ArrayList<>();
        filesToDownload.add("1");

        fixture.setDeleteFilesAfterMove(false);
        fixture.setSelectedFiles(filesToDownload);
        fixture.exportFiles();

        List<String> list = fixture.getAllImportFolder();

        assertNotNull(list);
        assertFalse(list.isEmpty());

        // cleanup
        Path exportFolder = Paths.get("src/test/resources/tmp/fileupload/testprocess");
        if (!Files.exists(exportFolder)) {
            exportFolder = Paths.get("target/test-classes/tmp/fileupload/testprocess");
        }
        assertTrue(Files.exists(exportFolder));

        StorageProvider.getInstance().deleteDir(exportFolder);
    }

    @Test
    public void testImportFiles() {
        FileManipulation fixture = new FileManipulation(metadataBean);
        List<String> list = fixture.getAllImportFolder();

        fixture.importFiles();

        fixture.setSelectedFiles(list);
        fixture.importFiles();

    }

    @SuppressWarnings("deprecation")
    private void prepareMocking() {
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(HttpSession.class);
        PowerMock.mockStatic(Helper.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        FacesContextHelper.setFacesContext(facesContext);
        //        facesContext.responseComplete();
        EasyMock.expect(FacesContext.getCurrentInstance()).andReturn(facesContext).anyTimes();
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getSession(EasyMock.anyBoolean())).andReturn(session).anyTimes();
        EasyMock.expect(externalContext.getRequest()).andReturn(request).anyTimes();

        EasyMock.expect(request.getScheme()).andReturn("http://").anyTimes();

        EasyMock.expect(request.getServerName()).andReturn("example.com").anyTimes();
        EasyMock.expect(request.getServerPort()).andReturn(80).anyTimes();
        EasyMock.expect(request.getContextPath()).andReturn("goobi").anyTimes();

        EasyMock.expect(session.getId()).andReturn("fixture").anyTimes();

        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(session.getServletContext()).andReturn(context).anyTimes();
        EasyMock.expect(context.getContextPath()).andReturn("fixture").anyTimes();

        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();

        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        EasyMock.expect(application.createValueBinding(EasyMock.anyString())).andReturn(null).anyTimes();

        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getRequestParameter(EasyMock.anyString())).andReturn("1").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyObject(Exception.class));
        Helper.setFehlerMeldung(EasyMock.anyString());
        PowerMock.mockStatic(MetadataManager.class);
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject());

        PowerMock.replay(Helper.class);
        EasyMock.replay(request);
        EasyMock.replay(root);
        EasyMock.replay(session);
        EasyMock.replay(application);
        EasyMock.replay(externalContext);
        EasyMock.replay(context);
        EasyMock.replay(facesContext);
    }
}
