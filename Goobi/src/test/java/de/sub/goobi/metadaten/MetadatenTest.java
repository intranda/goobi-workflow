package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, Application.class, UIViewRoot.class, Helper.class, MetadataManager.class,
    ProcessManager.class })
public class MetadatenTest extends AbstractTest {

    private Process process;
    private Prefs prefs;

    @SuppressWarnings("deprecation")
    @Before
    public void setUp() throws Exception {

        // manipulate configuration file

        ConfigurationHelper.setImagesPath("/some/path/");

        process = MockProcess.createProcess();

        // mock jsf context and http session
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        Application application = EasyMock.createMock(Application.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);

        HttpSession session = EasyMock.createMock(HttpSession.class);

        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("Ansicht", "test");
        requestMap.put("BenutzerID", "1");
        requestMap.put("addTo", "current");
        requestMap.put("x", "10");
        requestMap.put("y", "10");
        requestMap.put("w", "50");
        requestMap.put("h", "50");
        requestMap.put("areaId", "1_1");

        FacesContextHelper.setFacesContext(facesContext);
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getRequestParameterMap()).andReturn(requestMap).anyTimes();
        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        EasyMock.expect(externalContext.getSession(false)).andReturn(session).anyTimes();
        EasyMock.expect(session.getId()).andReturn("123").anyTimes();
        EasyMock.expect(externalContext.getRequest()).andReturn(servletRequest).anyTimes();

        EasyMock.expect(servletRequest.getScheme()).andReturn("https").anyTimes();
        EasyMock.expect(servletRequest.getServerName()).andReturn("localhost").anyTimes();
        EasyMock.expect(servletRequest.getServerPort()).andReturn(443).anyTimes();
        EasyMock.expect(servletRequest.getContextPath()).andReturn("goobi").anyTimes();

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();
        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        EasyMock.expect(application.createValueBinding(EasyMock.anyString())).andReturn(null).anyTimes();
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));

        // database connection
        PowerMock.mockStatic(MetadataManager.class);
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject());
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process);
        ProcessManager.saveProcess(process);
        // Mock ui error message handling
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getRequestParameter(EasyMock.anyString())).andReturn("1").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());

        PowerMock.replay(Helper.class);
        PowerMock.replay(ProcessManager.class);
        EasyMock.replay(servletRequest);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        EasyMock.replay(root);
        EasyMock.replay(application);

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
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.AnsichtAendern();
        assertEquals("", value);
    }

    @Test
    public void testHinzufuegen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Hinzufuegen();
        value = fixture.Hinzufuegen();
        assertEquals("", value);
    }

    @Test
    public void testAddGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.AddGroup();
        value = fixture.AddGroup();
        assertEquals("", value);
    }

    @Test
    public void testHinzufuegenPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.HinzufuegenPerson();
        assertEquals("", value);
    }

    @Test
    public void testAddCorporate() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.AddCorporate();
        assertEquals("", value);
    }

    @Test
    public void testAbbrechen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Abbrechen();
        assertEquals("", value);
    }

    @Test
    public void testRepresentativeMetadata() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        // update existing value
        Metadata rep = null;
        Metadata rtl = null;
        for (Metadata md : fixture.getDocument().getLogicalDocStruct().getAllMetadata()) {
            if (md.getType().getName().equals("_directionRTL")) {
                rtl = md;
            }
        }
        for (Metadata md : fixture.getDocument().getPhysicalDocStruct().getAllMetadata()) {
            if (md.getType().getName().equals("_representative")) {
                rep = md;
            }
        }
        assertEquals("1", rep.getValue());
        assertFalse(fixture.isPagesRTL());

        fixture.setPagesRTL(true);
        fixture.setCurrentRepresentativePage("2");
        fixture.setRepresentativeMetadata();

        assertEquals("2", rep.getValue());
        assertTrue(fixture.isPagesRTL());

        // set new metadata
        fixture.getDocument().getLogicalDocStruct().removeMetadata(rtl);
        fixture.getDocument().getPhysicalDocStruct().removeMetadata(rep);

        fixture.setPagesRTL(false);
        fixture.setCurrentRepresentativePage("1");
        fixture.setRepresentativeMetadata();
        for (Metadata md : fixture.getDocument().getPhysicalDocStruct().getAllMetadata()) {
            if (md.getType().getName().equals("_representative")) {
                rep = md;
            }
        }
        assertEquals("1", rep.getValue());
        assertFalse(fixture.isPagesRTL());
    }

    @Test
    public void testToggleImageView() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.toggleImageView();
        assertEquals("", value);
    }

    @Test
    public void testAutomaticSave() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.automaticSave();
        assertEquals("", value);
    }

    @Test
    public void testReload() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String value = fixture.Reload();
        assertEquals("", value);
    }

    @Test
    public void testCopyGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setAutorityFile("id", "uri", "value");
        md.addMetadata(m);
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.addNamePart(new NamePart("type", "value"));
        p.setAutorityFile("id", "uri", "value");
        md.addPerson(p);

        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setAutorityFile("id", "uri", "value");
        c.addSubName(new NamePart("type", "value"));
        md.addCorporate(c);

        fixture.getDocument().getLogicalDocStruct().addMetadataGroup(md);
        fixture.setCurrentGroup(mdg);

        String value = fixture.CopyGroup();
        assertEquals("", value);
    }

    @Test
    public void testKopieren() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setAutorityFile("id", "uri", "value");
        fixture.getDocument().getLogicalDocStruct().addMetadata(m);

        fixture.setCurrentMetadata(m);

        String value = fixture.Kopieren();
        assertEquals("", value);
    }

    @Test
    public void testCopyCorporate() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setAutorityFile("id", "uri", "value");
        c.addSubName(new NamePart("type", "value"));
        fixture.getDocument().getLogicalDocStruct().addCorporate(c);
        fixture.setCurrentCorporate(c);
        String value = fixture.copyCorporate();
        assertEquals("", value);
    }

    @Test
    public void testKopierenPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));

        p.setAutorityFile("id", "uri", "value");
        MetaPerson md = new MetaPerson(p, 0, prefs, null, process, null);
        p.addNamePart(new NamePart("type", "value"));
        fixture.getDocument().getLogicalDocStruct().addPerson(p);
        fixture.setCurPerson(md);
        fixture.setCurrentPerson(p);

        String value = fixture.KopierenPerson();
        assertEquals("", value);
    }

    @Test
    public void testChangeCurrentDocstructType() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct dsToChange = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(dsToChange);
        fixture.setTempWert("Chapter");

        assertEquals("metseditor", fixture.ChangeCurrentDocstructType());
    }

    @Test
    public void testAddNewMetadata() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempTyp("junitMetadata");
        // add new metadata
        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setAutorityFile("id", "uri", "value");
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setValue("test");
        fixture.setSelectedMetadatum(md);
        assertEquals("", fixture.addNewMetadata());

        fixture.setTempTyp("junitMetadata");
        md.setValue("junitMetadata");
        assertEquals("", fixture.addNewMetadata());

        // remove existing title
        Metadata title = null;
        for (Metadata meta : fixture.getDocument().getLogicalDocStruct().getAllMetadata()) {
            if (meta.getType().getName().equals("TitleDocMain")) {
                title = meta;
                break;
            }
        }
        fixture.getDocument().getLogicalDocStruct().removeMetadata(title, true);

        // add new title metadata
        title = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        title.setValue("main title");
        MetadatumImpl titleImpl = new MetadatumImpl(title, 0, prefs, process, null);
        fixture.setTempTyp("TitleDocMain");
        fixture.setSelectedMetadatum(titleImpl);
        assertEquals("", fixture.addNewMetadata());
    }

    @Test
    public void testAddNewCorporate() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        // add new corporate
        fixture.setTempPersonRolle("junitCorporate");
        fixture.setTempCorporateMainName("main name");
        fixture.setTempCorporateSubName("sub name");
        fixture.setTempCorporatePartName("part name");

        assertEquals("", fixture.addNewCorporate());

        // check result
        Corporate corp = null;
        for (Corporate existing : fixture.getDocument().getLogicalDocStruct().getAllCorporates()) {
            if (existing.getType().getName().equals("junitCorporate")) {
                corp = existing;
            }
        }
        assertEquals("main name", corp.getMainName());
        assertEquals("sub name", corp.getSubNames().get(0).getValue());
        assertEquals("part name", corp.getPartName());

    }

    @Test
    public void testSaveGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.getTempMetadataGroupType();
        fixture.setTempMetadataGroupType("junitgrp");

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setAutorityFile("id", "uri", "value");
        md.addMetadata(m);
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.addNamePart(new NamePart("type", "value"));
        p.setAutorityFile("id", "uri", "value");
        md.addPerson(p);
        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setAutorityFile("id", "uri", "value");
        c.addSubName(new NamePart("type", "value"));
        md.addCorporate(c);
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setSelectedGroup(mdg);

        assertEquals("", fixture.saveGroup());
    }

    public void testLoadRightFrame() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setNeuesElementWohin("1");
        assertEquals("metseditor", fixture.loadRightFrame());
        fixture.setNeuesElementWohin("3");
        assertEquals("metseditor", fixture.loadRightFrame());
    }

    @Test
    public void testSpeichernPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setTempPersonRolle("junitPerson");
        fixture.setTempPersonVorname("firstname");
        fixture.setTempPersonNachname("lastname");

        assertEquals("", fixture.addNewPerson());
    }

    @Test
    public void testDeleteGroup() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.getTempMetadataGroupType();
        fixture.setTempMetadataGroupType("junitgrp");

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.getDocument().getLogicalDocStruct().addMetadataGroup(md);
        fixture.setSelectedGroup(mdg);
        fixture.saveGroup();

        fixture.setCurrentGroup(mdg);

        assertEquals("", fixture.deleteGroup());
    }

    @Test
    public void testLoeschen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        fixture.setTempTyp("junitMetadata");

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        md.setValue("test");

        fixture.setSelectedMetadatum(md);
        fixture.setCurMetadatum(md);
        fixture.getDocument().getLogicalDocStruct().addMetadata(m);
        assertEquals("", fixture.Loeschen());

        Metadata m2 = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        MetadatumImpl md2 = new MetadatumImpl(m2, 0, prefs, process, null);
        md2.setValue("test");
        fixture.setCurMetadatum(md2);
        assertEquals("", fixture.Loeschen());
        assertEquals("", md2.getValue());
    }

    @Test
    public void testDelete() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        fixture.setTempTyp("junitMetadata");

        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setValue("test");
        fixture.getDocument().getLogicalDocStruct().addMetadata(m);

        fixture.setCurrentMetadata(m);

        assertEquals("", fixture.delete());

        Metadata m2 = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m2.setValue("test");
        fixture.setCurrentMetadata(m2);
        assertEquals("", fixture.delete());
        assertEquals("", m2.getValue());
    }

    @Test
    public void testLoeschenPerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.setFirstname("first");
        p.setLastname("last");
        p.addNamePart(new NamePart("type", "value"));
        MetaPerson md = new MetaPerson(p, 0, prefs, null, process, null);
        fixture.getDocument().getLogicalDocStruct().addPerson(p);
        fixture.setCurPerson(md);

        assertEquals("", fixture.LoeschenPerson());

        p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.setFirstname("first");
        p.setLastname("last");
        p.addNamePart(new NamePart("type", "value"));
        md = new MetaPerson(p, 0, prefs, null, process, null);
        fixture.setCurPerson(md);
        assertEquals("", fixture.LoeschenPerson());
        assertEquals("", md.getVorname());
        assertEquals("", md.getNachname());
    }

    //    deletePerson
    @Test
    public void testDeletePerson() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.setFirstname("first");
        p.setLastname("last");
        p.addNamePart(new NamePart("type", "value"));
        fixture.getDocument().getLogicalDocStruct().addPerson(p);

        fixture.setCurrentPerson(p);

        assertEquals("", fixture.deletePerson());

        p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.setFirstname("first");
        p.setLastname("last");
        p.addNamePart(new NamePart("type", "value"));
        fixture.setCurrentPerson(p);

        assertEquals("", fixture.deletePerson());
        assertEquals("", p.getFirstname());
        assertEquals("", p.getLastname());
    }

    @Test
    public void testDeleteCorporate() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setMainName("main");
        fixture.getDocument().getLogicalDocStruct().addCorporate(c);
        fixture.setCurrentCorporate(c);
        assertEquals("", fixture.deleteCorporate());

        c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setMainName("main");
        fixture.setCurrentCorporate(c);
        assertEquals("", fixture.deleteCorporate());

    }

    @Test
    public void testGetAddableRollen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        List<SelectItem> list = fixture.getAddableRollen();
        assertEquals(6, list.size());
        assertEquals("Author", list.get(0).getLabel());

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

        fixture.setCurrentGroup(mdg);
        list = fixture.getAddableRollen();
        assertEquals(1, list.size());
        assertEquals("junitPerson", list.get(0).getLabel());
    }

    @Test
    public void testGetAddableCorporateRoles() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();

        List<SelectItem> list = fixture.getAddableCorporateRoles();
        assertEquals(1, list.size());
        assertEquals("junitCorporate", list.get(0).getLabel());

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);

        fixture.setCurrentGroup(mdg);
        list = fixture.getAddableCorporateRoles();
        assertEquals(1, list.size());
        assertEquals("junitCorporate", list.get(0).getLabel());
    }

    @Test
    public void testSizeOfRoles() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfRoles(1);
        assertEquals(6, fixture.getSizeOfRoles());
    }

    @Test
    public void testSizeOfMetadata() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadata(1);
        assertEquals(15, fixture.getSizeOfMetadata());
    }

    @Test
    public void testSizeOfCorporates() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadata(1);
        assertEquals(1, fixture.getSizeOfCorporates());
    }

    @Test
    public void testSizeOfMetadataGroups() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadataGroups(1);
        assertEquals(1, fixture.getSizeOfMetadataGroups());
    }

    @Test
    public void testAddableMetadataTypes() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadataGroups(1);
        assertEquals(15, fixture.getAddableMetadataTypes().size());

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setCurrentGroup(mdg);
        assertEquals(1, fixture.getAddableMetadataTypes().size());
    }

    @Test
    public void testAddableMetadataGroupTypes() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        fixture.setSizeOfMetadataGroups(1);
        assertEquals(1, fixture.getAddableMetadataGroupTypes().size());

        MetadataGroup md = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        MetadataGroupImpl mdg = new MetadataGroupImpl(prefs, process, md, null, "", "", 0);
        fixture.setCurrentGroup(mdg);
        assertEquals(0, fixture.getAddableMetadataGroupTypes().size());
    }

    @Test
    public void testTempMetadatumList() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
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
        fixture.setMyBenutzerID("1");
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
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        SelectItem[] data = fixture.getMetadatenTypen();
        assertEquals(22, data.length);
    }

    @Test
    public void testMetadataGroupTypes() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyProzess(process);
        fixture.setMyBenutzerID("1");
        fixture.XMLlesenStart();
        SelectItem[] data = fixture.getMetadataGroupTypes();
        assertEquals(1, data.length);
    }

    @Test
    public void testXMLlesen() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        String data = fixture.XMLlesen();
        assertEquals("metseditor", data);
    }

    @Test
    public void testCheckForRepresentative() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertTrue(fixture.isCheckForRepresentative());
    }

    @Test
    public void testCheckForReadingDirection() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertTrue(fixture.isCheckForReadingDirection());
    }

    @Test
    public void testKnotenUp() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(1);
        fixture.setMyStrukturelement(ds);
        assertEquals("metseditor", fixture.KnotenUp());
    }

    @Test
    public void testKnotenDown() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(ds);
        assertEquals("metseditor", fixture.KnotenDown());
    }

    @Test
    public void testKnotenVerschieben() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        DocStruct other = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setTempStrukturelement(other);
        fixture.setMyStrukturelement(ds);
        assertEquals("metseditor", fixture.KnotenVerschieben());
    }

    @Test
    public void testKnotenDelete() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
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
        fixture.setMyBenutzerID("1");
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
        fixture.setMyBenutzerID("1");
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
        // TODO addablePersondata addableCorporates pageAreaManager

    }

    @Test
    public void testetAddableDocStructTypenAlsKind() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setMyStrukturelement(ds);
        assertEquals(31, fixture.getAddableDocStructTypenAlsKind().length);
    }

    @Test
    public void testetAddableDocStructTypenAlsNachbar() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(2);
        fixture.setMyStrukturelement(ds);
        assertEquals(46, fixture.getAddableDocStructTypenAlsNachbar().length);
    }

    @Test
    public void testCreatePagination() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertTrue(StringUtils.isBlank(fixture.createPagination()));
    }

    @Test
    public void testPaginierung() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        String[] pages = { "0" };
        fixture.setAlleSeitenAuswahl(pages);
        fixture.setPaginierungSeitenProImage(1);
        fixture.setPaginierungArt("3");
        fixture.setPaginierungAbSeiteOderMarkierung(2);

        assertNull(fixture.Paginierung());
    }

    @Test
    public void testTreeExpand() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertEquals("metseditor", fixture.TreeExpand());
    }

    @Test
    public void testXMLschreiben() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        fixture.setCurrentRepresentativePage("1");
        assertEquals("Main", fixture.XMLschreiben());
    }

    @Test
    public void testGoMain() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        assertEquals("index", fixture.goMain());
    }

    @Test
    public void testGoZurueck() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        assertEquals("Main", fixture.goZurueck());
    }

    @Test
    public void testImageRight() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(0);
        fixture.imageRight();
        assertEquals(1, fixture.getImageIndex());

        // right to left
        fixture.setPagesRTL(true);
        assertEquals(1, fixture.getImageIndex());
        fixture.imageRight();
        assertEquals(0, fixture.getImageIndex());
    }

    @Test
    public void testImageRight2() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(0);
        fixture.imageRight2();
        assertEquals(2, fixture.getImageIndex());
    }

    @Test
    public void testImageLeft() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(5);
        fixture.imageLeft();
        assertEquals(4, fixture.getImageIndex());

        // right to left
        fixture.setPagesRTL(true);
        assertEquals(4, fixture.getImageIndex());
        fixture.imageLeft();
        assertEquals(5, fixture.getImageIndex());
    }

    @Test
    public void testImageLeft2() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(5);
        fixture.imageLeft2();
        assertEquals(3, fixture.getImageIndex());
    }

    @Test
    public void testImageLeftMost() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(3);
        fixture.imageLeftmost();
        assertEquals(0, fixture.getImageIndex());

        // right to left
        fixture.setPagesRTL(true);
        fixture.imageLeftmost();
        assertEquals(5, fixture.getImageIndex());
    }

    @Test
    public void testImageRightMost() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        // left to right
        fixture.setImageIndex(3);
        fixture.imageRightmost();
        assertEquals(5, fixture.getImageIndex());

        // right to left
        fixture.setPagesRTL(true);
        fixture.imageRightmost();
        assertEquals(0, fixture.getImageIndex());
    }

    @Test
    public void testReloadPagination() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        assertEquals("", fixture.reloadPagination());

    }

    @Test
    public void testAddPageAreaCommand() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setImageIndex(0);
        fixture.addPageAreaCommand();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(1, page.getAllChildren().size());
    }

    @Test
    public void testSetPageAreaCommand() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setImageIndex(0);
        fixture.addPageAreaCommand();
        fixture.setPageAreaCommand();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(1, page.getAllChildren().size());
        DocStruct area = page.getAllChildren().get(0);
        Metadata coords = area.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0);
        assertEquals("10,10,50,50", coords.getValue());
    }
    @Test
    public void testDeletePageAreaCommand() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setImageIndex(0);
        fixture.addPageAreaCommand();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(1, page.getAllChildren().size());
        DocStruct area = page.getAllChildren().get(0);
        Metadata coords = area.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS")).get(0);
        assertEquals("10,10,50,50", coords.getValue());

        fixture.deletePageAreaCommand();
        assertNull(page.getAllChildren());

    }

    @Test
    public void testDeletePageArea() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setImageIndex(0);
        fixture.addPageAreaCommand();
        fixture.setPageAreaCommand();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(1, page.getAllChildren().size());
        DocStruct area = page.getAllChildren().get(0);
        fixture.deletePageArea(area);
        assertNull(page.getAllChildren());
    }


    @Test
    public void testGetPageAreas() throws Exception {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();

        fixture.setImageIndex(0);
        fixture.addPageAreaCommand();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(0);
        assertEquals(1, page.getAllChildren().size());
        assertEquals("[{\"highlight\":true,\"areaId\":\"1_1\",\"w\":\"50\",\"x\":\"10\",\"h\":\"50\",\"y\":\"10\",\"logId\":\"LOG_0000\"}]", fixture.getPageAreas());
    }


    @Test
    public void testScrollPage() throws Exception {
        MetadatenImagesHelper mih = mockImageHelper();
        Metadaten fixture = initMetadaten();

        fixture.setImagehelper(mih);
        fixture.setImageIndex(0);
        fixture.setNumberOfNavigation(2);
        fixture.BildBlaettern();
        assertEquals(3, fixture.getBildNummer());
    }

    @Test
    public void testMoveToPage() throws Exception {
        MetadatenImagesHelper mih = mockImageHelper();
        Metadaten fixture = initMetadaten();

        fixture.setImagehelper(mih);
        fixture.setImageIndex(0);
        fixture.setBildNummerGeheZu("3");
        fixture.BildGeheZu();
        assertEquals(2, fixture.getBildNummer());
    }

    @Test
    public void testLoadImageInThumbnailList() throws Exception {
        MetadatenImagesHelper mih = mockImageHelper();
        Metadaten fixture = initMetadaten();

        fixture.setImagehelper(mih);
        fixture.setImageIndex(0);
        fixture.setBildNummerGeheZu("3");
        fixture.loadImageInThumbnailList();
        assertEquals(2, fixture.getBildNummer());
    }

    //    getAllTifFolders
    //    BildErmitteln
    //    discard
    //    isCheckForNewerTemporaryMetadataFiles
    //    AddAdditionalOpacPpns
    //    AddMetadaFromOpacPpn
    //    Validate
    //    CurrentStartpage
    //    CurrentEndpage
    //    startpage
    //    endpage
    //    setPages
    //     setPageNumber
    //    getAjaxAlleSeiten
    //        SeitenVonChildrenUebernehmen
    //        BildErsteSeiteAnzeigen
    //        BildLetzteSeiteAnzeigen
    //        SeitenHinzu
    //        SeitenWeg
    //        isImageHasOcr
    //        isShowOcrButton
    //        getOcrResult
    //        getJsonAlto
    //        saveAlto
    //        getOcrAddress
    //        getTempTyp
    //        getSelectedCorporate
    //        setMetadatum
    //        setTempMetadataGroupType
    //        getOutputType
    //        getStructSeiten
    //        BildAnzeigen
    //        getBildNummerGeheZu
    //        setBildNummerGeheZu
    //        setBildNummerGeheZuCompleteString
    //        getBildNummerGeheZuCompleteString
    //        getNeuesElementWohin
    //        setNeuesElementWohin
    //        getStrukturBaum3
    //        getStrukturBaum3Alle
    //        isModusStrukturelementVerschieben
    //        setModusStrukturelementVerschieben
    //        getMetadata
    //        search
    //        resetSearchValues
    //        getOpacKatalog
    //        setOpacKatalog
    //        getAllSearchFields
    //        getAllOpacCatalogues
    //        setCurrentTifFolder
    //        autocomplete
    //        autocompleteJson
    //        getIsNotRootElement
    //        updateRepresentativePage
    //        moveSeltectedPagesUp
    //        moveSelectedPages
    //        moveSeltectedPagesDown
    //        deleteSeltectedPages
    //        reOrderPagination
    //        getFileManipulation
    //        setModusCopyDocstructFromOtherProcess
    //        getDisplayFileManipulation
    //        filterMyProcess
    //        getStruktureTreeAsTableForFilteredProcess
    //        getIsProcessLoaded
    //        rememberFilteredProcessStruct
    //        importFilteredProcessStruct
    //        updateAllSubNodes
    //        getProgress
    //        onComplete
    //        isShowProgressBar
    //        changeTopstruct
    //        isPhysicalTopstruct
    //        getPaginatorList
    //        checkSelectedThumbnail
    //        getImageUrl
    //        getImageWidth
    //        getImageHeight
    //        cmdMoveFirst
    //        cmdMovePrevious
    //        cmdMoveNext
    //        cmdMoveLast
    //        setTxtMoveTo
    //        getLastPageNumber
    //        isFirstPage
    //        isLastPage
    //        hasNextPage
    //        hasPreviousPage
    //        getPageNumberCurrent
    //        getPageNumberLast
    //        getThumbnailSize
    //        setThumbnailSize
    //        setContainerWidth
    //        increaseContainerWidth
    //        reduceContainerWidth
    //        changeFolder
    //        setNumberOfImagesPerPage
    //        getPossibleDatabases
    //        getPossibleNamePartTypes
    //        reloadMetadataList
    //        isAddableMetadata
    //        isAddableMetadata
    //        isAddablePerson
    //        getAllPages
    //        setCurrentMetadataToPerformSearch


    private Metadaten initMetadaten() throws ReadException, IOException, PreferencesException, SwapException, DAOException {
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(process);
        fixture.XMLlesenStart();
        return fixture;
    }

    private MetadatenImagesHelper mockImageHelper()
            throws IOException, SwapException, InvalidImagesException, ContentLibException, ImageManipulatorException {
        MetadatenImagesHelper mih = EasyMock.createMock(MetadatenImagesHelper.class);
        List<String> files = StorageProvider.getInstance().list(process.getImagesTifDirectory(true));
        EasyMock.expect(mih.getImageFiles(EasyMock.anyObject(), EasyMock.anyString())).andReturn(files).anyTimes();
        mih.scaleFile(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.replay(mih);
        return mih;
    }

}
