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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.easymock.EasyMock;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
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
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.commons.contentlib.exceptions.ContentLibException;
import de.unigoettingen.sub.commons.contentlib.exceptions.ImageManipulatorException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
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
        ProcessManager.class, PropertyManager.class, StepManager.class })
@PowerMockIgnore({ "javax.net.ssl.*" })
public class MetadatenTest extends AbstractTest {

    private Process process;
    private Prefs prefs;

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
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getRequestParameter(EasyMock.anyString())).andReturn("1").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());

        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString());
        PowerMock.replay(Helper.class);
        PowerMock.replay(ProcessManager.class);
        EasyMock.replay(servletRequest);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        EasyMock.replay(root);
        EasyMock.replay(application);

        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(Collections.emptyList());
        PowerMock.replay(StepManager.class);

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
            if ("_directionRTL".equals(md.getType().getName())) {
                rtl = md;
            }
        }
        for (Metadata md : fixture.getDocument().getPhysicalDocStruct().getAllMetadata()) {
            if ("_representative".equals(md.getType().getName())) {
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
            if ("_representative".equals(md.getType().getName())) {
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
        m.setAuthorityFile("id", "uri", "value");
        md.addMetadata(m);
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.addNamePart(new NamePart("type", "value"));
        p.setAuthorityFile("id", "uri", "value");
        md.addPerson(p);

        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setAuthorityFile("id", "uri", "value");
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
        m.setAuthorityFile("id", "uri", "value");
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
        c.setAuthorityFile("id", "uri", "value");
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

        p.setAuthorityFile("id", "uri", "value");
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
        m.setAuthorityFile("id", "uri", "value");
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
            if ("TitleDocMain".equals(meta.getType().getName())) {
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
            if ("junitCorporate".equals(existing.getType().getName())) {
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
        m.setAuthorityFile("id", "uri", "value");
        md.addMetadata(m);
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        p.addNamePart(new NamePart("type", "value"));
        p.setAuthorityFile("id", "uri", "value");
        md.addPerson(p);
        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        c.setAuthorityFile("id", "uri", "value");
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

        fixture.setTempMetadatumList(new ArrayList<>());
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

        fixture.setTempMetadataGroupList(new ArrayList<>());
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
        assertEquals("[{\"highlight\":true,\"areaId\":\"1_1\",\"w\":\"50\",\"x\":\"10\",\"h\":\"50\",\"y\":\"10\",\"logId\":\"LOG_0000\"}]",
                fixture.getPageAreas());
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

    @Test
    public void testGetAllTifFolders() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(3, fixture.getAllTifFolders().size());
    }

    @Test
    public void testSwitchImage() throws Exception {
        MetadatenImagesHelper mih = mockImageHelper();
        Metadaten fixture = initMetadaten();

        fixture.setImagehelper(mih);
        fixture.setBildNummer(0);
        fixture.setImageIndex(0);
        // disable image display
        fixture.BildAnzeigen();
        fixture.BildErmitteln(2);
        // no new image was created, number is still pointing to the old value
        assertEquals(0, fixture.getBildNummer());

        // enable image display again
        fixture.BildAnzeigen();
        fixture.BildErmitteln(2);
        assertEquals(3, fixture.getBildNummer());
    }

    @Test
    public void testDiscard() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.automaticSave();
        assertEquals("Main", fixture.discard());
    }

    @Test
    public void testCheckForNewerTemporaryMetadataFiles() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.isCheckForNewerTemporaryMetadataFiles());
        fixture.automaticSave();
        assertTrue(fixture.isCheckForNewerTemporaryMetadataFiles());
    }

    @Test
    public void testImportSubElementsFromOpac() throws Exception {
        Metadaten fixture = initMetadaten();
        GoobiProperty pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setPropertyName("Template");
        pp.setPropertyValue("test");
        List<GoobiProperty> props = new ArrayList<>();
        props.add(pp);
        process.setEigenschaften(props);
        List<String> catalogues = fixture.getAllOpacCatalogues();
        assertEquals("KXP", catalogues.get(0));
        fixture.setAdditionalOpacPpns("1800490011");
        fixture.setOpacKatalog("KXP");
        assertEquals("Metadaten3links", fixture.AddAdditionalOpacPpns());
        DocStruct logical = fixture.getDocument().getLogicalDocStruct();
        DocStruct lastChild = logical.getAllChildren().get(logical.getAllChildren().size() - 1);
        assertEquals("Chapter", lastChild.getType().getName());
    }

    @Test
    public void testImportMetadataFromOpac() throws Exception {
        Metadaten fixture = initMetadaten();
        GoobiProperty pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setPropertyName("Template");
        pp.setPropertyValue("test");
        List<GoobiProperty> props = new ArrayList<>();
        props.add(pp);
        process.setEigenschaften(props);
        List<String> catalogues = fixture.getAllOpacCatalogues();
        assertEquals("KXP", catalogues.get(0));
        fixture.setAdditionalOpacPpns("1800490011");
        fixture.setOpacKatalog("KXP");
        assertEquals("", fixture.AddMetadaFromOpacPpn());
        DocStruct logical = fixture.getDocument().getLogicalDocStruct();

        Metadata md1 = null;
        Metadata md2 = null;
        for (Metadata md : logical.getAllMetadata()) {
            if ("TitleDocMainShort".equals(md.getType().getName())) {
                md1 = md;
            } else if ("TitleDocSub1".equals(md.getType().getName())) {
                md2 = md;
            }
        }
        assertEquals("Semi-Vektoren und Spinoren", md1.getValue());
        assertEquals("von A. Einstein und W. Mayer", md2.getValue());
    }

    @Test
    public void testValidate() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.Validate();
        assertEquals(6, fixture.getStructSeiten().length);
    }

    @Test
    public void testCurrentStartpage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageNumber(1);
        fixture.CurrentStartpage();
        assertEquals("1: uncounted", fixture.getPagesStart());
    }

    @Test
    public void testCurrentEndpage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageNumber(4);
        fixture.CurrentEndpage();
        assertEquals("4: uncounted", fixture.getPagesEnd());
    }

    @Test
    public void testStartpage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageNumber(1);
        fixture.startpage();
        assertEquals("1: uncounted", fixture.getPagesStartCurrentElement());
    }

    @Test
    public void testEndpage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageNumber(4);
        fixture.endpage();
        assertEquals("4: uncounted", fixture.getPagesEndCurrentElement());
    }

    @Test
    public void testSetPages() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPagesStartCurrentElement("1: uncounted");
        fixture.setPagesEndCurrentElement("3: uncounted");

        // currently 1 page is assigned to cover
        DocStruct ds = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        assertEquals(1, ds.getAllToReferences().size());
        fixture.setMyStrukturelement(ds);
        fixture.setPages();
        // now 1-3 are assigned
        assertEquals(3, ds.getAllToReferences().size());
    }

    @Test
    public void testPageNumber() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageNumber(2);
        assertEquals(1, fixture.getPageNumber());

    }

    @Test
    public void testAjaxPageAssignment() throws Exception {
        Metadaten fixture = initMetadaten();
        List<String> results = fixture.getAjaxAlleSeiten("1");
        assertEquals(1, results.size());
        assertEquals("1: uncounted", results.get(0));
    }

    @Test
    public void testGetPageAssignmentFromChildren() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct logical = fixture.getDocument().getLogicalDocStruct();
        DocStruct chapter = logical.getAllChildren().get(logical.getAllChildren().size() - 1);

        // assign all pages to chapter
        fixture.setMyStrukturelement(chapter);
        fixture.setPagesStartCurrentElement("1: uncounted");
        fixture.setPagesEndCurrentElement("6: uncounted");
        fixture.setPages();
        assertEquals(6, chapter.getAllToReferences().size());

        // remove all pages from monograph
        logical.getAllReferences("to").removeAll(logical.getAllReferences("to"));
        assertEquals(0, logical.getAllToReferences().size());
        fixture.setMyStrukturelement(logical);

        // assign pages from sub elements
        fixture.SeitenVonChildrenUebernehmen();
        assertEquals(6, logical.getAllToReferences().size());
    }

    @Test
    public void testShowFirstPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageSelectionFirstPage("1");
        fixture.setBildNummer(1);
        fixture.BildErsteSeiteAnzeigen();
        assertEquals(0, fixture.getImageIndex());
        assertEquals(0, fixture.getBildNummer());
    }

    @Test
    public void testShowLastPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setPageSelectionLastPage("6");
        fixture.setBildNummer(1);
        fixture.BildLetzteSeiteAnzeigen();
        assertEquals(5, fixture.getImageIndex());
        assertEquals(5, fixture.getBildNummer());
    }

    @Test
    public void testAddPagAssignment() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct logical = fixture.getDocument().getLogicalDocStruct();
        DocStruct chapter = logical.getAllChildren().get(logical.getAllChildren().size() - 1);
        fixture.setMyStrukturelement(chapter);

        String[] selection = { "1", "2", "3", "4", "5", "6" };
        fixture.setAlleSeitenAuswahl(selection);
        assertEquals(1, chapter.getAllToReferences().size());
        fixture.SeitenHinzu();

        assertEquals(6, chapter.getAllToReferences().size());
    }

    @Test
    public void testRemovePagAssignment() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct logical = fixture.getDocument().getLogicalDocStruct();
        DocStruct chapter = logical.getAllChildren().get(logical.getAllChildren().size() - 1);
        fixture.setMyStrukturelement(chapter);

        String[] selection = { "1" };
        fixture.setStructSeitenAuswahl(selection);
        assertEquals(1, chapter.getAllToReferences().size());
        fixture.SeitenWeg();

        assertEquals(0, chapter.getAllToReferences().size());
    }

    @Test
    public void testImageHasOcr() throws Exception {
        Metadaten fixture = initMetadaten();
        assertTrue(fixture.isImageHasOcr());
    }

    @Test
    public void testShowOcrButton() throws Exception {
        Metadaten fixture = initMetadaten();
        assertTrue(fixture.isShowOcrButton());
    }

    @Test
    public void testOcrResult() throws Exception {
        Metadaten fixture = initMetadaten();
        assertTrue(StringUtils.isNotBlank(fixture.getOcrResult()));
    }

    @Test
    public void testJsonAlto() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.loadJsonAlto();
        assertTrue(StringUtils.isNotBlank(fixture.getJsonAlto()));
    }

    @Test
    public void testGetTempTyp() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals("CreatorsAllOrigin", fixture.getTempTyp());
    }

    @Test
    public void tesMetadatum() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.getTempTyp();
        MetadatumImpl mi = fixture.getSelectedMetadatum();
        fixture.setMetadatum(mi);
        assertEquals(mi.getTyp(), fixture.getMetadatum().getTyp());
    }

    @Test
    public void testTempMetadataGroupType() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.getSelectedGroup();
        fixture.setTempMetadataGroupType("junitgrp");

        assertEquals("junitgrp", fixture.getTempMetadataGroupType());
        assertEquals("junitgrp", fixture.getSelectedGroup().getMetadataGroup().getType().getName());
    }

    @Test
    public void testOutputType() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.getTempTyp();
        MetadatumImpl mi = fixture.getSelectedMetadatum();
        fixture.setMetadatum(mi);
        assertEquals(mi.getOutputType(), fixture.getOutputType());
    }

    @Test
    public void testStructSeiten() throws Exception {
        Metadaten fixture = initMetadaten();
        SelectItem[] items = fixture.getStructSeiten();

        assertEquals(6, items.length);
    }

    @Test
    public void testBildNummerGeheZuCompleteString() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setBildNummerGeheZuCompleteString("2: uncounted");
        fixture.BildGeheZu();
        assertEquals(1, fixture.getImageIndex());
    }

    @Test
    public void testNeuesElementWohin() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals("4", fixture.getNeuesElementWohin());
        fixture.setNeuesElementWohin("1");
        assertEquals("1", fixture.getNeuesElementWohin());
    }

    @Test
    public void testStrukturBaum3() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(1, fixture.getStrukturBaum3().size());
    }

    @Test
    public void testStrukturBaum3Alle() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(4, fixture.getStrukturBaum3Alle().size());
    }

    @Test
    public void testModusStrukturelementVerschieben() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.isModusStrukturelementVerschieben());
        fixture.setModusStrukturelementVerschieben(true);
        assertTrue(fixture.isModusStrukturelementVerschieben());
    }

    @Test
    public void testGetMetadata() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals("CatalogIDDigital", fixture.getMetadata().getMd().getType().getName());
    }

    @Test
    public void testGetOpacKatalog() throws Exception {
        Metadaten fixture = initMetadaten();
        GoobiProperty pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setPropertyName("Template");
        pp.setPropertyValue("test");
        List<GoobiProperty> props = new ArrayList<>();
        props.add(pp);
        process.setEigenschaften(props);
        assertEquals("KXP", fixture.getOpacKatalog());
    }

    @Test
    public void testGetAllSearchFields() throws Exception {
        Metadaten fixture = initMetadaten();
        GoobiProperty pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setPropertyName("Template");
        pp.setPropertyValue("test");
        List<GoobiProperty> props = new ArrayList<>();
        props.add(pp);
        process.setEigenschaften(props);

        assertNull(fixture.getAllSearchFields());
        List<String> catalogues = fixture.getAllOpacCatalogues();
        fixture.setOpacKatalog(catalogues.get(0));
        assertEquals(5, fixture.getAllSearchFields().size());
    }

    @Test
    public void testCurrentTifFolder() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setCurrentTifFolder("test");
        assertEquals("test", fixture.getCurrentTifFolder());
    }

    @Test
    public void testAutocomplete() throws Exception {
        Metadaten fixture = initMetadaten();
        List<String> complete = fixture.autocomplete("2");
        assertEquals(1, complete.size());
        assertEquals("2: uncounted", complete.get(0));
    }

    @Test
    public void testIsNotRootElement() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.getIsNotRootElement());
        DocStruct dsToChange = fixture.getDocument().getLogicalDocStruct().getAllChildren().get(0);
        fixture.setMyStrukturelement(dsToChange);
        assertTrue(fixture.getIsNotRootElement());
    }

    @Test
    public void testUpdateRepresentativePage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setResetRepresentative(true);
        fixture.updateRepresentativePage();
        for (String pageObject : fixture.getPageMap().getKeyList()) {
            PhysicalObject po = fixture.getPageMap().get(pageObject);
            assertFalse(po.isRepresentative());
        }
        fixture.setResetRepresentative(false);

        fixture.setCurrentRepresentativePage("5");
        fixture.updateRepresentativePage();
        assertTrue(fixture.getPageMap().get("5").isRepresentative());
    }

    @Test
    public void testMoveSeltectedPagesUp() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(1);
        Metadata order = page.getAllMetadataByType(prefs.getMetadataTypeByName("physPageNumber")).get(0);
        assertEquals("2", order.getValue());

        fixture.getPageMap().get("2").setSelected(true);

        fixture.moveSeltectedPagesUp(1);
        assertEquals("1", order.getValue());
    }

    @Test
    public void testMoveSeltectedPages() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(1);
        Metadata order = page.getAllMetadataByType(prefs.getMetadataTypeByName("physPageNumber")).get(0);
        assertEquals("2", order.getValue());

        fixture.getPageMap().get("2").setSelected(true);

        fixture.moveSelectedPages("up", 1);
        assertEquals("1", order.getValue());
    }

    @Test
    public void testMoveSeltectedPagesDown() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(1);
        Metadata order = page.getAllMetadataByType(prefs.getMetadataTypeByName("physPageNumber")).get(0);
        assertEquals("2", order.getValue());

        fixture.getPageMap().get("2").setSelected(true);

        fixture.moveSeltectedPagesDown(1);
        assertEquals("3", order.getValue());
    }

    @Test
    public void testDeleteSelectedPages() throws Exception {
        Process secondProcess = MockProcess.createProcess();
        secondProcess.setId(2);

        // copy metadata + images from first to a second process
        StorageProvider.getInstance().copyDirectory(Paths.get(process.getProcessDataDirectory()), Paths.get(secondProcess.getProcessDataDirectory()));

        String imageFolder = secondProcess.getImagesTifDirectory(true);
        assertTrue(Files.exists(Paths.get(imageFolder)));

        // use cloned process to test data
        Metadaten fixture = new Metadaten();
        fixture.setMyBenutzerID("1");
        fixture.setMyProzess(secondProcess);
        fixture.XMLlesenStart();

        // mark pages 2 + 4
        fixture.getPageMap().get("2").setSelected(true);
        fixture.getPageMap().get("4").setSelected(true);
        // delete them
        List<String> existingFiles = StorageProvider.getInstance().list(imageFolder);
        assertEquals(6, existingFiles.size());
        fixture.deleteSeltectedPages();
        existingFiles = StorageProvider.getInstance().list(imageFolder);
        assertEquals(4, existingFiles.size());
        // cleanup copied data
        StorageProvider.getInstance().deleteDir(Paths.get(secondProcess.getProcessDataDirectory()));

    }

    @Test
    public void testReOrderPagination() throws Exception {
        Metadaten fixture = initMetadaten();

        DocStruct page = fixture.getDocument().getPhysicalDocStruct().getAllChildren().get(1);
        assertEquals("00000002.tif", page.getImageName());

        fixture.getPageMap().get("2").setSelected(true);
        fixture.moveSeltectedPagesDown(1);
        fixture.reOrderPagination();

        assertEquals("00000003.tif", page.getImageName());

    }

    @Test
    public void testFileManipulation() throws Exception {
        Metadaten fixture = initMetadaten();
        assertNotNull(fixture.getFileManipulation());
    }

    @Test
    public void testModusCopyDocstructFromOtherProcess() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setDisplayInsertion(true);

        fixture.setModusCopyDocstructFromOtherProcess(false);
        assertTrue(fixture.isDisplayInsertion());

        fixture.setModusCopyDocstructFromOtherProcess(true);
        assertFalse(fixture.isDisplayInsertion());
    }

    @Test
    public void testDisplayFileManipulation() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.getDisplayFileManipulation());
    }

    @Test
    public void testIsProcessLoaded() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.getIsProcessLoaded());
    }

    @Test
    public void testProgress() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(0, fixture.getProgress().intValue());
        fixture.onComplete();
        assertEquals(0, fixture.getProgress().intValue());

        assertFalse(fixture.isShowProgressBar());
    }

    @Test
    public void testToggleDocStruct() throws Exception {
        Metadaten fixture = initMetadaten();
        assertFalse(fixture.isPhysicalTopstruct());
        fixture.changeTopstruct();
        assertTrue(fixture.isPhysicalTopstruct());
        fixture.changeTopstruct();
        assertFalse(fixture.isPhysicalTopstruct());
    }

    @Test
    public void testCheckSelectedThumbnail() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.getPageMap().get("2").setSelected(true);
        fixture.setModusAnsicht("Paginierung");
        // check another image
        fixture.checkSelectedThumbnail(4);
        assertFalse(fixture.getPageMap().get("2").isSelected());
        assertTrue(fixture.getPageMap().get("4").isSelected());
    }

    @Test
    public void testGetImageWidth() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(640, fixture.getImageWidth());
    }

    @Test
    public void testGetImageHeight() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(480, fixture.getImageHeight());
    }

    @Test
    public void testCmdMoveFirst() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setPageNo(1);
        fixture.cmdMoveFirst();
        assertEquals(0, fixture.getPageNo());
        fixture.setPagesRTL(true);
        fixture.cmdMoveFirst();
        assertEquals(2, fixture.getPageNo());
    }

    @Test
    public void testCmdMovePrevious() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setPageNo(1);
        fixture.cmdMovePrevious();
        assertEquals(0, fixture.getPageNo());
        fixture.setPagesRTL(true);
        fixture.cmdMovePrevious();
        assertEquals(1, fixture.getPageNo());
    }

    @Test
    public void testCmdMoveNext() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setPageNo(1);
        fixture.cmdMoveNext();
        assertEquals(2, fixture.getPageNo());
        fixture.setPagesRTL(true);
        fixture.cmdMoveNext();
        assertEquals(1, fixture.getPageNo());
    }

    @Test
    public void testCmdMoveLast() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setPageNo(1);
        fixture.cmdMoveLast();
        assertEquals(2, fixture.getPageNo());
        fixture.setPagesRTL(true);
        fixture.cmdMoveLast();
        assertEquals(0, fixture.getPageNo());
    }

    @Test
    public void testTxtMoveTo() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setPageNo(1);
        fixture.setTxtMoveTo(3);
        assertEquals(2, fixture.getPageNo());
    }

    @Test
    public void testLastPageNumber() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        assertEquals(2, fixture.getLastPageNumber());
    }

    @Test
    public void testFirstPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        assertTrue(fixture.isFirstPage());
        fixture.setTxtMoveTo(3);
        assertFalse(fixture.isFirstPage());
    }

    @Test
    public void testLastPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        assertFalse(fixture.isLastPage());
        fixture.setTxtMoveTo(3);
        assertTrue(fixture.isLastPage());
    }

    @Test
    public void testHasNextPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setTxtMoveTo(2);
        assertTrue(fixture.hasNextPage());
    }

    @Test
    public void testHasPreviousPage() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setTxtMoveTo(2);
        assertTrue(fixture.hasPreviousPage());
    }

    @Test
    public void testPageNumberCurrent() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setTxtMoveTo(2);
        assertEquals(2L, fixture.getPageNumberCurrent().longValue());
    }

    @Test
    public void testPageNumberLast() throws Exception {
        Metadaten fixture = initMetadaten();
        fixture.setNumberOfImagesPerPage(2);
        fixture.setTxtMoveTo(2);
        assertEquals(3L, fixture.getPageNumberLast().longValue());
    }

    @Test
    public void testThumbnailSize() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(200, fixture.getThumbnailSize());
        fixture.setThumbnailSize(150);
        assertEquals(150, fixture.getThumbnailSize());
    }

    @Test
    public void testContainerWidth() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(600, fixture.getContainerWidth());
        fixture.setContainerWidth(300);
        assertEquals(300, fixture.getContainerWidth());
        fixture.reduceContainerWidth();
        assertEquals(200, fixture.getContainerWidth());
        fixture.reduceContainerWidth();
        assertEquals(200, fixture.getContainerWidth());
        fixture.increaseContainerWidth();
        assertEquals(300, fixture.getContainerWidth());
    }

    @Test
    public void testNumberOfImagesPerPage() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(96, fixture.getNumberOfImagesPerPage());
        fixture.setNumberOfImagesPerPage(4);
        assertEquals(4, fixture.getNumberOfImagesPerPage());
        // still old value
        fixture.setNumberOfImagesPerPage(0);
        assertEquals(4, fixture.getNumberOfImagesPerPage());
    }

    @Test
    public void testPossibleDatabases() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(1, fixture.getPossibleDatabases().size());
    }

    @Test
    public void testPossibleNamePartTypes() throws Exception {
        Metadaten fixture = initMetadaten();
        assertEquals(2, fixture.getPossibleNamePartTypes().size());
        assertEquals("date", fixture.getPossibleNamePartTypes().get(0));
        assertEquals("termsOfAddress", fixture.getPossibleNamePartTypes().get(1));
    }

    @Test
    public void testAddableMetadata() throws Exception {
        Metadaten fixture = initMetadaten();
        assertTrue(fixture.isAddableMetadata(prefs.getMetadataTypeByName("junitMetadata")));
    }

    @Test
    public void testAddableMetadata2() throws Exception {
        Metadaten fixture = initMetadaten();
        Metadata md = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        md.setValue("x");
        fixture.getDocument().getLogicalDocStruct().addMetadata(md);
        assertTrue(fixture.isAddableMetadata(md));
    }

    @Test
    public void testAddablePerson() throws Exception {
        Metadaten fixture = initMetadaten();
        assertTrue(fixture.isAddablePerson(prefs.getMetadataTypeByName("junitPerson")));
    }

    @Test
    public void testAllPages() throws Exception {
        Metadaten fixture = initMetadaten();
        List<PhysicalObject> pages = fixture.getAllPages();
        assertEquals(6, pages.size());
        assertEquals("1: uncounted", pages.get(0).getLabel());
    }

    @Test
    public void testCurrentMetadataToPerformSearch() throws Exception {
        Metadaten fixture = initMetadaten();
        Metadata m = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        m.setValue("x");
        MetadatumImpl md = new MetadatumImpl(m, 0, prefs, process, null);
        fixture.setCurrentMetadataToPerformSearch(md);
        assertEquals(DisplayType.select, fixture.getCurrentMetadataToPerformSearch().getMetadataDisplaytype());
    }

    @Test
    public void testVttGeneration() throws Exception {
        Metadaten fixture = initMetadaten();
        // no file set or file is no video
        assertTrue(fixture.getChapterInformationAsVTT().isEmpty());

        // prepare video data
        DigitalDocument dd = fixture.getDocument();
        DocStruct phys = dd.getPhysicalDocStruct();

        DocStruct video = dd.createDocStruct(prefs.getDocStrctTypeByName("video"));
        video.setImageName("sample.mp4");
        // logical
        Metadata md = new Metadata(prefs.getMetadataTypeByName("physPageNumber"));
        md.setValue("7");
        video.addMetadata(md);

        md = new Metadata(prefs.getMetadataTypeByName("logicalPageNumber"));
        md.setValue("7");
        video.addMetadata(md);
        Image currentImage = new Image(process, "", "sample.mp4", 7, 200);
        fixture.getAllImages().add(currentImage);

        phys.addChild(video);

        DocStruct area1 = dd.createDocStruct(prefs.getDocStrctTypeByName("area"));
        DocStruct area2 = dd.createDocStruct(prefs.getDocStrctTypeByName("area"));
        area1.setDocstructType("area");
        area2.setDocstructType("area");

        md = new Metadata(prefs.getMetadataTypeByName("_BEGIN"));
        md.setValue("00:00:00.000");
        area1.addMetadata(md);
        md = new Metadata(prefs.getMetadataTypeByName("_END"));
        md.setValue("00:01:00.000");
        area1.addMetadata(md);
        md = new Metadata(prefs.getMetadataTypeByName("_BEGIN"));
        md.setValue("00:01:00.000");
        area2.addMetadata(md);

        md = new Metadata(prefs.getMetadataTypeByName("_END"));
        md.setValue("00:02:00.000");
        area2.addMetadata(md);
        video.addChild(area1);
        video.addChild(area2);

        DocStruct section1 = dd.createDocStruct(prefs.getDocStrctTypeByName("Chapter"));
        DocStruct section2 = dd.createDocStruct(prefs.getDocStrctTypeByName("Chapter"));

        md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("section 1");
        section1.addMetadata(md);

        md = new Metadata(prefs.getMetadataTypeByName("TitleDocMain"));
        md.setValue("section 2");
        section2.addMetadata(md);

        dd.getLogicalDocStruct().addChild(section1);
        dd.getLogicalDocStruct().addChild(section2);

        section1.addReferenceTo(area1, "logical_physical");
        section2.addReferenceTo(area2, "logical_physical");

        fixture.setImageIndex(7);

        String expected = """
                WEBVTT

                1
                00:00:00.000 --> 00:01:00.000
                section 1

                2
                00:01:00.000 --> 00:02:00.000
                section 2

                                """;

        assertEquals(expected, fixture.getChapterInformationAsVTT());

    }

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
        EasyMock.expect(mih.getImageFiles(EasyMock.anyObject(), EasyMock.anyString(), EasyMock.anyBoolean())).andReturn(files).anyTimes();
        mih.scaleFile(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.replay(mih);
        return mih;
    }

}
