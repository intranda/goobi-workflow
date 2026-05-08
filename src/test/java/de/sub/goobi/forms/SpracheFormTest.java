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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class SpracheFormTest extends AbstractTest {

    @BeforeEach
    public void setUp() throws Exception {

        FacesContext facesContext = Mockito.mock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);
        UIViewRoot root = Mockito.mock(UIViewRoot.class);
        Application application = Mockito.mock(Application.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(facesContext.getApplication()).thenReturn(application);

        Mockito.when(externalContext.getRequestContextPath()).thenReturn("junit");

        Mockito.when(facesContext.getViewRoot()).thenReturn(root);
        root.setLocale(Mockito.any());
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);
        Mockito.when(application.getSupportedLocales()).thenReturn(locale.iterator());
        root.setLocale(Mockito.any());
        root.setLocale(Mockito.any());

        Map<String, Object> sessionMap = new HashMap<>();
        Mockito.when(externalContext.getSessionMap()).thenReturn(sessionMap);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("locale", "de");
        requestMap.put("ziel", "test");
        Mockito.when(externalContext.getRequestParameterMap()).thenReturn(requestMap);

        Mockito.when(root.getLocale()).thenReturn(Locale.GERMAN);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            SpracheForm form = new SpracheForm();
            assertNotNull(form);

        }
    }

    @Test
    public void testConstructorDefault() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
            SpracheForm form = new SpracheForm();
            assertNotNull(form);

        }
    }

    @Test
    public void testSupportedLocales() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
            SpracheForm form = new SpracheForm();
            assertNotNull(form);

            List<Map<String, Object>> fixture = form.getSupportedLocales();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testSwitchLanguage() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
            SpracheForm form = new SpracheForm();
            assertNotNull(form);
            form.switchLanguage("de");

        }
    }

    @Test
    public void testSpracheUmschalten() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
            SpracheForm form = new SpracheForm();
            assertNotNull(form);
            String fixture = form.changeLanguage();
            assertEquals("test", fixture);

        }
    }

    @Test
    public void testGetLocale() {
        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
                MockedStatic<UIViewRoot> mockedUIViewRoot = Mockito.mockStatic(UIViewRoot.class)) {

            ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
            SpracheForm form = new SpracheForm();
            assertNotNull(form);
            form.switchLanguage("de");
            Locale fixture = form.getLocale();
            assertEquals(Locale.GERMAN, fixture);

        }
    }

}
