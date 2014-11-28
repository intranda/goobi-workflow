package de.sub.goobi.metadaten;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
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

import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.mock.MockProcess;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, Application.class, UIViewRoot.class })
public class MetadatenTest {

    private Process process;
    private Prefs prefs;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @SuppressWarnings("deprecation")
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
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));

        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        EasyMock.replay(root);
        EasyMock.replay(application);
        process = MockProcess.createProcess(folder);
        prefs = process.getRegelsatz().getPreferences();
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
        fixture.setMyBenutzerID("1");
        value = fixture.AnsichtAendern();
        assertEquals("", value);
    }

    @Test
    public void testHinzufuegen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Hinzufuegen();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.Hinzufuegen();
        assertEquals("", value);
    }

    @Test
    public void testAddGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.AddGroup();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.AddGroup();
        assertEquals("", value);
    }

    @Test
    public void testHinzufuegenPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.HinzufuegenPerson();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.HinzufuegenPerson();
        assertEquals("", value);
    }

    @Test
    public void testAbbrechen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Abbrechen();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.Abbrechen();
        assertEquals("", value);
    }

    @Test
    public void testReload() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Reload();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.Reload();
        assertEquals("", value);
    }

    @Test
    public void testCopyGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md);
        fixture.setCurrentGroup(mdg);

        String value = fixture.CopyGroup();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.CopyGroup();
        assertEquals("", value);
    }

    @Test
    public void testKopieren() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setAutorityFile("id", "uri", "value");
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process);
       
        fixture.setCurMetadatum(md);

        String value = fixture.Kopieren();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.Kopieren();
        assertEquals("", value);
    }
    
    
    @Test
    public void testKopierenPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
       
        p.setAutorityFile("id", "uri", "value");
        MetaPerson md = new MetaPerson(p, 0, prefs, null);
       
        p.addNamePart(new NamePart("type", "value"));
        
        fixture.setCurPerson(md);

        String value = fixture.KopierenPerson();
        assertEquals("metseditor_timeout", value);
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        value = fixture.KopierenPerson();
        assertEquals("", value);
    }
    
    
    
    @Test
    public void testChangeCurrentDocstructType() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        
        DocStruct dsToChange = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(dsToChange);
        fixture.setTempWert("Chapter");
        
        assertEquals("metseditor", fixture.ChangeCurrentDocstructType());
    }
}
