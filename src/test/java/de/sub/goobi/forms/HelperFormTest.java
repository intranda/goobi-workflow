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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Ruleset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class HelperFormTest extends AbstractTest {

    @BeforeEach
    public void setUp() {

        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        FacesContext facesContext = Mockito.mock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);

        //        UIViewRoot root = Mockito.mock(UIViewRoot.class);
        Application application = Mockito.mock(Application.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);

        Mockito.when(externalContext.getRequestContextPath()).thenReturn("junit");

        //        Mockito.when(facesContext.getViewRoot()).thenReturn(root);
        //        Mockito.when(root.getLocale()).thenReturn(Locale.GERMAN);

        Mockito.when(facesContext.getApplication()).thenReturn(application);
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        Mockito.when(application.getSupportedLocales()).thenReturn(locale.iterator());
    }

    @AfterEach
    public void tearDown() {
        FacesContextHelper.reset();
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);

        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetBuildversion() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getBuildVersion();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testGetVersion() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getVersion();
            Pattern versionRegex = Pattern.compile("^\\d{2}\\.\\d{2}(\\.\\d+)?(-dev|-SNAPSHOT)?$");
            Matcher versionMatcher = versionRegex.matcher(fixture);
            assertTrue(versionMatcher.find());

        }
    }

    @Test
    public void testGetApplicationHeaderTitle() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getApplicationHeaderTitle();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testGetApplicationTitle() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getApplicationTitle();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testGetApplicationWebsiteUrl() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getApplicationWebsiteUrl();
            assertEquals("junit/", fixture);

        }
    }

    //    @Test
    public void testGetApplicationWebsiteMsg() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getApplicationWebsiteMsg();
            assertEquals("goobiWebseite", fixture);

        }
    }

    //    @Test
    public void testGetApplicationHomepageMsg() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            String fixture = helperForm.getApplicationHomepageMsg();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testGetAnonymized() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            assertFalse(helperForm.getAnonymized());

        }
    }

    //    @Test
    public void testGetRegelsaetze() throws DAOException {

        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            Ruleset r = new Ruleset();
            r.setTitel("title");
            r.setDatei("file");
            r.setOrderMetadataByRuleset(false);
            List<Ruleset> rulesetList = new ArrayList<>();
            rulesetList.add(r);

            try (MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class)) {
                mockedRulesetManager
                        .when(() -> RulesetManager.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                                Mockito.any(Institution.class)))
                        .thenReturn(rulesetList);

                HelperForm helperForm = new HelperForm();
                assertNotNull(helperForm);
                List<SelectItem> fixture = helperForm.getRegelsaetze();
                assertNotNull(fixture);
                assertEquals(1, fixture.size());

            }
        }
    }

    //    @Test
    public void testGetDockets() throws DAOException {

        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            Docket d = new Docket();
            d.setFile("file");
            d.setId(1);
            d.setName("name");
            List<Docket> docketList = new ArrayList<>();
            docketList.add(d);

            try (MockedStatic<DocketManager> mockedDocketManager = Mockito.mockStatic(DocketManager.class)) {
                mockedDocketManager.when(() -> DocketManager.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.any(Institution.class))).thenReturn(docketList);
                HelperForm helperForm = new HelperForm();
                assertNotNull(helperForm);
                List<SelectItem> fixture = helperForm.getDockets();
                assertNotNull(fixture);
                assertEquals(1, fixture.size());

            }
        }
    }

    //    @Test
    public void testGetFileFormats() throws DAOException {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            List<SelectItem> fixture = helperForm.getFileFormats();
            assertNotNull(fixture);
            assertNotEquals(0, fixture.size());

        }
    }

    //    @Test
    public void testGetFileFormatsInternalOnly() throws DAOException {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            List<SelectItem> fixture = helperForm.getFileFormatsInternalOnly();
            assertNotNull(fixture);
            assertNotEquals(0, fixture.size());

        }
    }

    //    @Test
    public void testGetStepStatusList() throws DAOException {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            List<SelectItem> fixture = helperForm.getStepStatusList();
            assertNotNull(fixture);
            assertEquals(5, fixture.size());

        }
    }

    @Test
    public void testGetTimeZone() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            TimeZone tz = helperForm.getTimeZone();
            assertNotNull(tz);

        }
    }

    @Test
    public void testGetMassImportAllowed() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            assertTrue(helperForm.getMassImportAllowed());

        }
    }

    //    @Test
    public void testIsPasswordIsChangable() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            assertFalse(helperForm.isPasswordIsChangable());

        }
    }

    @Test
    public void testIsShowError() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            HelperForm helperForm = new HelperForm();
            assertNotNull(helperForm);
            assertFalse(helperForm.isShowError());
            helperForm.setShowError(true);
            assertTrue(helperForm.isShowError());

        }
    }

}
