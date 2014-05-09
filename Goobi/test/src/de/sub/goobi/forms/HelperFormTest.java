package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.exceptions.DAOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, UIViewRoot.class })
public class HelperFormTest {

    @Before
    public void setUp() {
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(UIViewRoot.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);

        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();

        EasyMock.expect(externalContext.getRequestContextPath()).andReturn("junit").anyTimes();

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMANY).anyTimes();

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);
        locale.add(Locale.ENGLISH);

        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();

        EasyMock.replay(application);
        EasyMock.replay(root);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
    }

    @After
    public void tearDown() {
        FacesContextHelper.reset();
    }

    @Test
    public void testConstructor() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
    }

    @Test
    public void testGetBuildversion() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getBuildVersion();
        assertNotNull(fixture);
    }

    @Test
    public void testGetVersion() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getVersion();
        assertEquals("2.0", fixture);
    }

    @Test
    public void testGetApplicationHeaderTitle() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationHeaderTitle();
        assertEquals("Goobi", fixture);
    }

    @Test
    public void testGetApplicationTitle() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationTitle();
        assertEquals("http://goobi.intranda.com", fixture);
    }

    @Test
    public void testGetApplicationTitleStyle() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationTitlexe();
        assertEquals("font-size:17px; font-family:verdana; color: white;", fixture);
    }

    @Test
    public void testGetApplicationWebsiteUrl() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationWebsiteUrl();
        assertEquals("junit/", fixture);
    }

    @Test
    public void testGetApplicationWebsiteMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationWebsiteMsg();
        assertEquals("http://www.goobi.org/", fixture);
    }

    @Test
    public void testGetApplicationHomepageMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationHomepageMsg();
        assertNotNull(fixture);
    }

    @Test
    public void testGetApplicationTechnicalBackgroundMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationTechnicalBackgroundMsg();
        assertNotNull(fixture);
    }

    @Test
    public void testGetApplicationImpressumMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationImpressumMsg();
        assertNotNull(fixture);
    }

    @Test
    public void testGetApplicationIndividualHeader() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationIndividualHeader();
        assertNotNull(fixture);
    }

    @Test
    public void testGetAnonymized() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertFalse(helperForm.getAnonymized());
    }

    @Test
    public void testGetRegelsaetze() throws DAOException {

        // TODO

    }

    @Test
    public void testGetDockets() throws DAOException {

        // TODO
    }

    @Test
    public void testGetFileFormats() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getFileFormats();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());
    }

    @Test
    public void testGetFileFormatsInternalOnly() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getFileFormatsInternalOnly();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());
    }

    @Test
    public void testGetStepStatusList() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getStepStatusList();
        assertNotNull(fixture);
        assertEquals(4, fixture.size());
    }

    @Test
    public void testGetTimeZone() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        TimeZone tz = helperForm.getTimeZone();
        assertNotNull(tz);
    }

    @Test
    public void testGetMassImportAllowed() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertTrue(helperForm.getMassImportAllowed());
    }

    @Test
    public void testIsLdapIsWritable() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertFalse(helperForm.isLdapIsWritable());
    }
    
    @Test
    public void testIsPasswordIsChangable() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertFalse(helperForm.isPasswordIsChangable());
    }
    
    @Test
    public void testIsShowError() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertFalse(helperForm.isShowError());
        helperForm.setShowError(true);
        assertTrue(helperForm.isShowError());

        
    }
    
   
    
    
}
