package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

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

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
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
    public void testUploadFile() throws Exception {
        FileManipulation fixture = new FileManipulation(metadataBean);
        // uploadedFile is null

        fixture.uploadFile();

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

    // @Test
    // public void testGetUploadedFileName() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetUploadedFileName() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetUploadedFile() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetUploadedFile() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetInsertPage() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetInsertPage() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetInsertMode() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetInsertMode() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetImageSelection() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetImageSelection() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testDownloadFile() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testExportFiles() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetSelectedFiles() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetSelectedFiles() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testIsDeleteFilesAfterMove() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetDeleteFilesAfterMove() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testIsMoveFilesInAllFolder() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetMoveFilesInAllFolder() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetAllImportFolder() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetAllImportFolder() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testImportFiles() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testGetCurrentFolder() {
    // fail("Not yet implemented");
    // }
    //
    // @Test
    // public void testSetCurrentFolder() {
    // fail("Not yet implemented");
    // }
    
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
