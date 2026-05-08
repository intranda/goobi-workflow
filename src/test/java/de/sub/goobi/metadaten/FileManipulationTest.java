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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.goobi.beans.Process;
import org.junit.jupiter.api.AfterEach;
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
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.mock.MockUploadedFile;
import de.sub.goobi.persistence.managers.MetadataManager;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class FileManipulationTest extends AbstractTest {

    private Metadaten metadataBean;
    private Process testProcess;

    private MockedStatic<ExternalContext> mockedExternalContext;
    private MockedStatic<FacesContext> mockedFacesContext;
    private MockedStatic<HttpSession> mockedHttpSession;
    private MockedStatic<Helper> mockedHelper;
    private MockedStatic<MetadataManager> mockedMetadataManager;

    @TempDir
    private Path tempDir;

    @BeforeEach
    public void setUp() throws Exception {

        prepareMocking();

        ConfigurationHelper.setImagesPath("/tmp/");
        testProcess = MockProcess.createProcess();
        testProcess.setId(1);

        // redirect metadata writes to a temp directory so tests don't pollute src/test/resources
        String resourcesFolder = "src/test/resources/";
        if (!Files.exists(Paths.get(resourcesFolder))) {
            resourcesFolder = "target/test-classes/";
        }
        File metadataDir = tempDir.toFile();
        Path sourceProcessDir = Paths.get(resourcesFolder + "metadata/1");
        Path targetProcessDir = metadataDir.toPath().resolve("1");
        StorageProvider.getInstance().copyDirectory(sourceProcessDir, targetProcessDir);
        ConfigurationHelper.getInstance().setParameter("dataFolder", metadataDir.getAbsolutePath() + "/");

        metadataBean = new Metadaten();
        metadataBean.setMyBenutzerID("1");
        metadataBean.setMyProzess(testProcess);
        metadataBean.XMLlesenStart();
    }

    @AfterEach
    public void tearDown() throws Exception {
        // delete uploaded files, if exists
        Path file1 = Paths.get(testProcess.getImagesTifDirectory(false), "fixture.tif");
        Path file2 = Paths.get(testProcess.getImagesTifDirectory(false), "fixture2.tif");
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        if (mockedMetadataManager != null) {
            mockedMetadataManager.close();
        }
        if (mockedHelper != null) {
            mockedHelper.close();
        }
        if (mockedHttpSession != null) {
            mockedHttpSession.close();
        }
        if (mockedFacesContext != null) {
            mockedFacesContext.close();
        }
        if (mockedExternalContext != null) {
            mockedExternalContext.close();
        }
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

    private void prepareMocking() {
        mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
        mockedFacesContext = Mockito.mockStatic(FacesContext.class);
        mockedHttpSession = Mockito.mockStatic(HttpSession.class);
        mockedHelper = Mockito.mockStatic(Helper.class);
        mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);

        FacesContext facesContext = Mockito.mock(FacesContext.class);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        UIViewRoot root = Mockito.mock(UIViewRoot.class);
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        PartialViewContext pvc = Mockito.mock(PartialViewContext.class);

        FacesContextHelper.setFacesContext(facesContext);
        mockedFacesContext.when(() -> FacesContext.getCurrentInstance()).thenReturn(facesContext);
        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(externalContext.getSession(Mockito.anyBoolean())).thenReturn(session);
        Mockito.when(externalContext.getRequest()).thenReturn(request);

        Mockito.when(request.getScheme()).thenReturn("http://");
        Mockito.when(request.getServerName()).thenReturn("example.com");
        Mockito.when(request.getServerPort()).thenReturn(80);
        Mockito.when(request.getContextPath()).thenReturn("goobi");

        Mockito.when(session.getId()).thenReturn("fixture");

        ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(session.getServletContext()).thenReturn(context);
        Mockito.when(context.getContextPath()).thenReturn("fixture");

        Application application = Mockito.mock(Application.class);
        Mockito.when(facesContext.getApplication()).thenReturn(application);

        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        Mockito.when(facesContext.getViewRoot()).thenReturn(root);
        Mockito.when(root.getLocale()).thenReturn(Locale.GERMAN);
        Mockito.when(application.getSupportedLocales()).thenReturn(locale.iterator());

        facesContext.addMessage(Mockito.any(), Mockito.any());
        Mockito.when(facesContext.getPartialViewContext()).thenReturn(pvc);
        Mockito.when(pvc.getRenderIds()).thenReturn(new ArrayList<>());

        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
        mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
        mockedHelper.when(() -> Helper.getRequestParameter(Mockito.anyString())).thenReturn("1");
        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);

    }
}
