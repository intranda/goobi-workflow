package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, UIViewRoot.class, Application.class })
public class SpracheFormTest {

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(UIViewRoot.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        //        EasyMock.expect(externalContext.getRequestContextPath()).andReturn("junit").anyTimes();

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        root.setLocale(EasyMock.anyObject(Locale.class));
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);
        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        root.setLocale(EasyMock.anyObject(Locale.class));
        root.setLocale(EasyMock.anyObject(Locale.class));

        Map<String, Object> sessionMap = new HashMap<>();
        EasyMock.expect(externalContext.getSessionMap()).andReturn(sessionMap).anyTimes();

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("locale", "de");
        requestMap.put("ziel", "test");
        EasyMock.expect(externalContext.getRequestParameterMap()).andReturn(requestMap).anyTimes();

        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        EasyMock.replay(application);
        EasyMock.replay(root);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);

    }

    @Test
    public void testConstructor() {
        SpracheForm form = new SpracheForm();
        assertNotNull(form);
    }

    @Test
    public void testConstructorDefault() {
        ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
        SpracheForm form = new SpracheForm();
        assertNotNull(form);
    }

    @Test
    public void testSupportedLocales() {
        ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
        SpracheForm form = new SpracheForm();
        assertNotNull(form);

        List<Map<String, Object>> fixture = form.getSupportedLocales();
        assertNotNull(fixture);

    }

    @Test
    public void testSwitchLanguage() {
        ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
        SpracheForm form = new SpracheForm();
        assertNotNull(form);
        form.switchLanguage("de");
    }

    @Test
    public void testSpracheUmschalten() {
        ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
        SpracheForm form = new SpracheForm();
        assertNotNull(form);
        String fixture = form.SpracheUmschalten();
        assertEquals("test", fixture);
    }

    @Test
    public void testGetLocale() {
        ConfigurationHelper.getInstance().setParameter("language.force-default", "de");
        SpracheForm form = new SpracheForm();
        assertNotNull(form);
        form.switchLanguage("de");
        Locale fixture = form.getLocale();
        assertEquals(Locale.GERMAN, fixture);
    }

}
