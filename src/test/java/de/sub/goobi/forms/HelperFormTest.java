package de.sub.goobi.forms;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.Application;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Ruleset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.RulesetManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, RulesetManager.class, DocketManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class HelperFormTest extends AbstractTest {

    @Before
    public void setUp() {

        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        PowerMock.mockStatic(Helper.class);

        //        PowerMock.mockStatic(UIViewRoot.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);

        //        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();

        EasyMock.expect(externalContext.getRequestContextPath()).andReturn("junit").anyTimes();

        //        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        //        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        EasyMock.replay(application);
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
        Pattern versionRegex = Pattern.compile("^\\d{2}\\.\\d{2}(\\.\\d+)?(-dev|-SNAPSHOT)?$");
        Matcher versionMatcher = versionRegex.matcher(fixture);
        assertTrue(versionMatcher.find());
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
    public void testGetApplicationWebsiteUrl() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationWebsiteUrl();
        assertEquals("junit/", fixture);
    }

    //    @Test
    public void testGetApplicationWebsiteMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationWebsiteMsg();
        assertEquals("goobiWebseite", fixture);
    }

    //    @Test
    public void testGetApplicationHomepageMsg() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        String fixture = helperForm.getApplicationHomepageMsg();
        assertNotNull(fixture);
    }

    @Test
    public void testGetAnonymized() {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        assertFalse(helperForm.getAnonymized());
    }

    //    @Test
    public void testGetRegelsaetze() throws DAOException {
        Ruleset r = new Ruleset();
        r.setTitel("title");
        r.setDatei("file");
        r.setOrderMetadataByRuleset(false);
        List<Ruleset> rulesetList = new ArrayList<>();
        rulesetList.add(r);
        PowerMock.mockStatic(RulesetManager.class);
        EasyMock.expect(RulesetManager.getRulesets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject(Institution.class))).andReturn(rulesetList).anyTimes();

        PowerMock.replay(RulesetManager.class);
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getRegelsaetze();
        assertNotNull(fixture);
        assertEquals(1, fixture.size());
    }

    //    @Test
    public void testGetDockets() throws DAOException {
        Docket d = new Docket();
        d.setFile("file");
        d.setId(1);
        d.setName("name");
        List<Docket> docketList = new ArrayList<>();
        docketList.add(d);
        PowerMock.mockStatic(DocketManager.class);
        EasyMock.expect(DocketManager.getDockets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject(Institution.class))).andReturn(docketList).anyTimes();
        PowerMock.replay(DocketManager.class);
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getDockets();
        assertNotNull(fixture);
        assertEquals(1, fixture.size());
    }

    //    @Test
    public void testGetFileFormats() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getFileFormats();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());
    }

    //    @Test
    public void testGetFileFormatsInternalOnly() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getFileFormatsInternalOnly();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());
    }

    //    @Test
    public void testGetStepStatusList() throws DAOException {
        HelperForm helperForm = new HelperForm();
        assertNotNull(helperForm);
        List<SelectItem> fixture = helperForm.getStepStatusList();
        assertNotNull(fixture);
        assertEquals(5, fixture.size());
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

    //    @Test
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
