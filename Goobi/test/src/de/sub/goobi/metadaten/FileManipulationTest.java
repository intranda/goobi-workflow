package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.mock.MockUploadedFile;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, HttpSession.class, UIViewRoot.class })
public class FileManipulationTest {

    private Metadaten metadataBean;

    private static int counter = 1;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        File file = new File("/tmp/" + counter++);
        file.createNewFile();
        
        prepareMocking();

        ConfigurationHelper.setImagesPath("/tmp/");
        metadataBean = new Metadaten();
        Process testProcess = MockProcess.createProcess(folder);
        metadataBean.setMyProzess(testProcess);
        metadataBean.XMLlesenStart();
        
        
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
    public void testGetUploadedFile() throws FileNotFoundException {
        FileManipulation fixture = new FileManipulation(metadataBean);
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/00000001.tif");
        UploadedFile uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");
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
    }

    @Test
    public void testUploadFile() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/00000001.tif");
        UploadedFile uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("lastPage");
        fixture.uploadFile();

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture2");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("1");
        fixture.uploadFile();
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

    @Test
    public void testDownloadFile() throws FileNotFoundException {
        FileManipulation fixture = new FileManipulation(metadataBean);
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/00000001.tif");
        UploadedFile uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

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
    }

    @Test
    public void testSelectedFiles() {
        FileManipulation fixture = new FileManipulation(metadataBean);

        List<String> filesToDownload = new ArrayList<String>();
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
    public void testExportFiles() throws FileNotFoundException {
        FileManipulation fixture = new FileManipulation(metadataBean);

        // first upload file
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/00000001.tif");
        UploadedFile uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture.tif");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("lastPage");
        fixture.uploadFile();

        List<String> filesToDownload = new ArrayList<String>();
        filesToDownload.add("1");

        fixture.setDeleteFilesAfterMove(true);
        fixture.setSelectedFiles(filesToDownload);
        fixture.exportFiles();
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
    public void testGetAllImportFolder() throws FileNotFoundException {
        FileManipulation fixture = new FileManipulation(metadataBean);

        // first upload file
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/00000001.tif");
        UploadedFile uploadedFile = new MockUploadedFile(stream, ".fi/xt\\ure.tif");

        fixture.setUploadedFile(uploadedFile);
        fixture.setUploadedFileName("fixture.tif");
        fixture.setCurrentFolder("testprocess_media");
        fixture.setInsertPage("lastPage");
        fixture.uploadFile();

        List<String> filesToDownload = new ArrayList<String>();
        filesToDownload.add("1");

        fixture.setDeleteFilesAfterMove(true);
        fixture.setSelectedFiles(filesToDownload);
        fixture.exportFiles();
        
        List<String> list = fixture.getAllImportFolder();

        assertNotNull(list);
        assertFalse(list.isEmpty());
        
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

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);

        FacesContextHelper.setFacesContext(facesContext);

        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getSession(EasyMock.anyBoolean())).andReturn(session).anyTimes();
        EasyMock.expect(session.getId()).andReturn("fixture").anyTimes();

        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        EasyMock.expect(application.createValueBinding(EasyMock.anyString())).andReturn(null).anyTimes();
        EasyMock.expect(facesContext.getResponseComplete()).andReturn(true);

        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));

        EasyMock.replay(session);
        EasyMock.replay(application);
        EasyMock.replay(root);

        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
    }
}
