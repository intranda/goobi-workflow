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
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
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
import de.sub.goobi.forms.NavigationForm.Theme;
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
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
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
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, Theme.ui, null);

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
        MetaPerson md = new MetaPerson(p, 0, prefs, null, process, Theme.ui, null);

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

    @Test
    public void testSpeichern() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempTyp("junitMetadata");

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, Theme.ui, null);
        md.setValue("test");

        fixture.setSelectedMetadatum(md);
        assertEquals("metseditor_timeout", fixture.Speichern());

        fixture.setTempTyp("TitleDocMain");
        md.setValue("title");
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");

        assertEquals("", fixture.Speichern());
    }

    @Test
    public void testSaveGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempMetadataGroupType("junitgrp");

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        fixture.setSelectedGroup(mdg);

        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");

        assertEquals("", fixture.saveGroup());
    }

    
    public void testLoadRightFrame() throws Exception {
        Metadaten fixture = new Metadaten();
        assertEquals("metseditor", fixture.loadRightFrame());
    }

    @Test
    public void testSpeichernPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempPersonRolle("junitPerson");
        fixture.setTempPersonVorname("firstname");
        fixture.setTempPersonNachname("lastname");

        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");

        assertEquals("", fixture.SpeichernPerson());
    }

    @Test
    public void testdeleteGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempMetadataGroupType("junitgrp");

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, Theme.ui, null);
        fixture.setSelectedGroup(mdg);
        fixture.saveGroup();

        fixture.setCurrentGroup(mdg);

        assertEquals("metseditor_timeout", fixture.deleteGroup());

        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        assertEquals("", fixture.deleteGroup());
    }

    @Test
    public void testLoeschen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempTyp("junitMetadata");

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, Theme.ui, null);
        md.setValue("test");

        fixture.setSelectedMetadatum(md);
        assertEquals("metseditor_timeout", fixture.Speichern());

        fixture.setCurMetadatum(md);

        assertEquals("metseditor_timeout", fixture.Loeschen());
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        assertEquals("", fixture.Loeschen());
    }
    
    @Test
    public void testGetAddableRollen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        List<SelectItem> list = fixture.getAddableRollen();
        assertEquals(6, list.size());
        assertEquals("Author", list.get(0).getLabel());
    }

    @Test
    public void testSizeOfRoles() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.setSizeOfRoles(1);
        assertEquals(6, fixture.getSizeOfRoles());
    }

    @Test
    public void testSizeOfMetadata() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadata(1);
        assertEquals(13, fixture.getSizeOfMetadata());
    }

    @Test
    public void testSizeOfMetadataGroups() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadataGroups(1);
        assertEquals(1, fixture.getSizeOfMetadataGroups());
    }

    @Test
    public void testTempMetadatumList() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        List<SelectItem> list = fixture.getAddableMetadataTypes();
        assertEquals(list.size(), fixture.getTempMetadatumList().size());

        fixture.setTempMetadatumList(new ArrayList<MetadatumImpl>());
        assertEquals(0, fixture.getTempMetadatumList().size());
    }

    @Test
    public void testTempMetadataGroupList() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.getAddableMetadataGroupTypes();
        assertEquals(1, fixture.getTempMetadataGroupList().size());

        fixture.setTempMetadataGroupList(new ArrayList<MetadataGroupImpl>());
        assertTrue(fixture.getTempMetadataGroupList().isEmpty());
    }

    @Test
    public void testMetadatenTypen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        SelectItem[] data = fixture.getMetadatenTypen();
        assertEquals(19, data.length);
    }

    @Test
    public void testMetadataGroupTypes() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        SelectItem[] data = fixture.getMetadataGroupTypes();
        assertEquals(1, data.length);
    }

    @Test
    public void testXMLlesen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        String data = fixture.XMLlesen();
        assertEquals("", data);
    }

    @Test
    public void testCheckForRepresentative() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertTrue(fixture.isCheckForRepresentative());
    }

    @Test
    public void testKnotenUp() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(1);
        fixture.setMyStrukturelement(ds);
        fixture.KnotenUp();
    }

    @Test
    public void testKnotenDown() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(ds);
        fixture.KnotenDown();
    }

    @Test
    public void testKnotenVerschieben() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        DocStruct other = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setTempStrukturelement(other);
        fixture.setMyStrukturelement(ds);
        fixture.KnotenVerschieben();
    }

    @Test
    public void testKnotenDelete() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(ds);
        fixture.KnotenDelete();
        assertEquals(2, fixture.getDocument().getLogicalDocStruct().getAllChildren().size());
    }

    @Test
    public void testDuplicateNode() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(ds);
        fixture.duplicateNode();
        assertEquals(4, fixture.getDocument().getLogicalDocStruct().getAllChildren().size());
    }

    @Test
    public void testKnotenAdd() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setPagesStart("1: uncounted");
        fixture.setPagesEnd("1: uncounted");

        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");

        // 1
        fixture.setNeuesElementWohin("1");
        assertEquals("metseditor", fixture.KnotenAdd());

        fixture.setAddDocStructType1("Chapter");
        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(1);
        fixture.setMyStrukturelement(ds);
        assertEquals("metseditor", fixture.KnotenAdd());

        // 2
        fixture.setNeuesElementWohin("2");
        assertEquals("metseditor", fixture.KnotenAdd());

        // 3
        fixture.setNeuesElementWohin("3");
        fixture.setAddDocStructType2("Chapter");
        ds = fixture.getDocument().getLogicalDocStruct();
        fixture.setMyStrukturelement(ds);
        assertEquals("metseditor", fixture.KnotenAdd());
        // 4
        fixture.setNeuesElementWohin("4");
        assertEquals("metseditor", fixture.KnotenAdd());
    }

    @Test
    public void testetAddableDocStructTypenAlsKind() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setMyStrukturelement(ds);
        assertEquals(31, fixture.getAddableDocStructTypenAlsKind().length);
    }

    @Test
    public void testetAddableDocStructTypenAlsNachbar() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setMyStrukturelement(ds);
        assertEquals(46, fixture.getAddableDocStructTypenAlsNachbar().length);
    }

    @Test
    public void testCreatePagination() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertTrue(StringUtils.isBlank(fixture.createPagination()));
    }

    @Test
    public void testPaginierung() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String[] pages = { "0" };
        fixture.setAlleSeitenAuswahl(pages);
        fixture.setPaginierungSeitenProImage(1);
        fixture.setPaginierungArt("3");
        fixture.setPaginierungAbSeiteOderMarkierung(2);
       
       assertEquals("metseditor_timeout",fixture.Paginierung());      
    }

    @Test
    public void testTreeExpand() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertEquals("metseditor", fixture.TreeExpand());
    }
    
    @Test
    public void testXMLschreiben() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.setCurrentRepresentativePage("1");
        assertEquals("Metadaten", fixture.XMLschreiben());
    }
    
    
    @Test
    public void testGoMain()  throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        
        assertEquals("index", fixture.goMain());
    }
    
    @Test
    public void testGoZurueck()  throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        
        MetadatenSperrung locking = new MetadatenSperrung();
        locking.setLocked(1, "1");
        fixture.setMyBenutzerID("1");
        
        assertEquals("Main", fixture.goZurueck());
    }
    

}
