package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, Application.class, UIViewRoot.class })
public class MetadatenTest {

    private Process process;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        ConfigurationHelper.setImagesPath("/some/path/");
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        Application application = EasyMock.createMock(Application.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);

        HttpSession session = EasyMock.createMock(HttpSession.class);

        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("Ansicht", "test");
        requestMap.put("BenutzerID", "1");
        FacesContextHelper.setFacesContext(facesContext);
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getRequestParameterMap()).andReturn(requestMap).anyTimes();
        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        EasyMock.expect(externalContext.getSession(false)).andReturn(session).anyTimes();
        EasyMock.expect(session.getId()).andReturn("123").anyTimes();

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        EasyMock.expect(application.createValueBinding(EasyMock.anyString())).andReturn(null).anyTimes();
//        EasyMock.expect(facesContext.getResponseComplete()).andReturn(true).anyTimes();

        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        EasyMock.replay(root);
        EasyMock.replay(application);
        process = MockProcess.createProcess(folder);
    }

    @Test
    public void testMetadaten() {
        Metadaten fixture = new Metadaten();
        assertNotNull(fixture);
    }

    @Test
    public void testAnsichtAendern() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.AnsichtAendern();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        assertEquals("", value);

    }
}
