package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Ruleset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.RulesetManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, UIViewRoot.class, RulesetManager.class, DocketManager.class })
public class HelperFormTest {

    @Before
    public void setUp() {
        String datafolder = System.getenv("junitdata");
        if (datafolder == null) {
            datafolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = datafolder + "goobi_config.properties";

        ConfigurationHelper.getInstance().setParameter("localMessages", datafolder);
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);

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
        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

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
        assertNotNull(fixture);
    }

    @Test
    public void testGetApplicationTitle() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationTitle();
        assertNotNull(fixture);
    }

    @Test
    public void testGetApplicationTitleStyle() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationTitlexe();
        assertNotNull(fixture);
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
        Ruleset r = new Ruleset();
        r.setTitel("title");
        r.setDatei("file");
        r.setOrderMetadataByRuleset(false);
        List<Ruleset> rulesetList = new ArrayList<>();
        rulesetList.add(r);
        PowerMock.mockStatic(RulesetManager.class);
        EasyMock.expect(RulesetManager.getRulesets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt())).andReturn(
                rulesetList).anyTimes();

        PowerMock.replay(RulesetManager.class);
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getRegelsaetze();
        assertNotNull(fixture);
        assertEquals(1, fixture.size());
    }

    @Test
    public void testGetDockets() throws DAOException {
        Docket d = new Docket();
        d.setFile("file");
        d.setId(1);
        d.setName("name");
        List<Docket> docketList = new ArrayList<>();
        docketList.add(d);
        PowerMock.mockStatic(DocketManager.class);
        EasyMock.expect(DocketManager.getDockets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt())).andReturn(
                docketList).anyTimes();
        PowerMock.replay(DocketManager.class);
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getDockets();
        assertNotNull(fixture);
        assertEquals(1, fixture.size());
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
