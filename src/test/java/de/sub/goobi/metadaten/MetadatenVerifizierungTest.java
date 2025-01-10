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
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.easymock.EasyMock;
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
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.fileformats.mets.MetsMods;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class, HttpSession.class, Helper.class, MetadataManager.class, ProcessManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class MetadatenVerifizierungTest extends AbstractTest {

    private Process process;
    private Prefs prefs;
    private Fileformat fileformat;

    @Before
    public void setUp() throws Exception {
        // mock jsf context and http session
        prepareMocking();

        ConfigurationHelper.setImagesPath("/some/path/");

        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        fileformat = process.readMetadataFile();
    }

    @Test
    public void testConstructor() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        assertNotNull(mv);
    }

    @Test
    public void testValidateProcess() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        assertTrue(mv.validate(process));
    }

    @Test
    public void testValidateFileformat() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        assertTrue(mv.validate(fileformat, prefs, process));
    }

    @Test
    public void testVerificationErrorsInvalidIdentifier() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        Metadata catalogIDDigital = log.getAllMetadataByType(prefs.getMetadataTypeByName("CatalogIDDigital")).get(0);
        catalogIDDigital.setValue("PPN!\"§$%&/()=");

        Metadata childId = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
        childId.setValue("PPN!\"§$%&/()=");
        log.getAllChildren().get(0).addMetadata(childId);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("error", mv.getProblems().get(0)); // invalid characters on main identifier
        assertEquals("error", mv.getProblems().get(1)); // main and sub id are the same
        assertEquals("error", mv.getProblems().get(2)); // invalid characters on sub identifier
    }

    @Test
    public void testVerificationErrorsNoIdentifier() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        Metadata catalogIDDigital = log.getAllMetadataByType(prefs.getMetadataTypeByName("CatalogIDDigital")).get(0);
        log.removeMetadata(catalogIDDigital, true);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): error: PPN in Monograph error 0 error", mv.getProblems().get(0));
    }

    @Test
    public void testVerificationErrorsDocstructWithoutPage() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        DocStruct chapter = fileformat.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("Chapter"));
        log.addChild(chapter);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): errorChapter", mv.getProblems().get(0));
    }

    @Test
    public void testVerificationErrorsPageWithoutDocstruct() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct phys = fileformat.getDigitalDocument().getPhysicalDocStruct();
        DocStruct page = fileformat.getDigitalDocument().createDocStruct(prefs.getDocStrctTypeByName("page"));
        Metadata md = new Metadata(prefs.getMetadataTypeByName("logicalPageNumber"));
        md.setValue("7");
        page.addMetadata(md);
        md = new Metadata(prefs.getMetadataTypeByName("physPageNumber"));
        md.setValue("1");
        page.addMetadata(md);
        phys.addChild(page);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): error: 1 (7)", mv.getProblems().get(0));
    }

    @Test
    public void testVerificationErrorsInvalidValueInSelectList() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        Metadata md = log.getAllMetadataByType(prefs.getMetadataTypeByName("TitleDocMain")).get(0);
        md.setValue("xyz");
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): error: Main title in Monograph error", mv.getProblems().get(0));
    }

    @Test
    public void testVerificationErrorsRequiredMetadataMissing() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        Metadata md = log.getAllMetadataByType(prefs.getMetadataTypeByName("TitleDocMain")).get(0);
        md.setValue("");
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): error: Main title in Monograph error", mv.getProblems().get(0));
        log.removeMetadata(md, true);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("testprocess (1): error: Main title in Monograph error", mv.getProblems().get(0));
    }

    @Test
    public void testVerificationErrorsRequiredFieldsMissing() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();

        // load alternative ruleset
        Prefs prefsRequiredField = new Prefs();
        Path folder = Paths.get(process.getProcessDataDirectory()).getParent().getParent();
        prefsRequiredField.loadPrefs(Paths.get(folder.toString(), "rulesets", "ruleset_requiredFields.xml").toString());

        Fileformat ff = new MetsMods(prefsRequiredField);
        ff.read(process.getMetadataFilePath());
        DocStruct log = ff.getDigitalDocument().getLogicalDocStruct();

        // required fields are missing
        assertFalse(mv.validate(ff, prefsRequiredField, process));
        assertEquals("testprocess (1): error: junitMetadata in Monograph error", mv.getProblems().get(0));
        assertEquals("testprocess (1): error: junitPerson in Monograph error", mv.getProblems().get(1));
        assertEquals("testprocess (1): error: junitCorporate in Monograph error", mv.getProblems().get(2));
        assertEquals("testprocess (1): error: junitgrp in Monograph error", mv.getProblems().get(3));
        mv.getProblems().clear();
        // required fields exist, but are empty
        Metadata md = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        log.addMetadata(md);
        Person p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        log.addPerson(p);
        Corporate c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        log.addCorporate(c);
        MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName("junitgrp"));
        log.addMetadataGroup(mg);
        assertFalse(mv.validate(ff, prefsRequiredField, process));
        assertEquals("testprocess (1): error: junitMetadata in Monograph error", mv.getProblems().get(0));
        assertEquals("testprocess (1): error: junitPerson in Monograph error", mv.getProblems().get(1));
        assertEquals("testprocess (1): error: junitCorporate in Monograph error", mv.getProblems().get(2));
        assertEquals("testprocess (1): error: junitgrp in Monograph error", mv.getProblems().get(3));
        mv.getProblems().clear();

        // required fields in group exists, but are empty
        md.setValue("x");
        p.setLastname("x");
        c.setMainName("x");
        md = new Metadata(prefs.getMetadataTypeByName("junitMetadata"));
        p = new Person(prefs.getMetadataTypeByName("junitPerson"));
        c = new Corporate(prefs.getMetadataTypeByName("junitCorporate"));
        mg.addMetadata(md);
        mg.addPerson(p);
        mg.addCorporate(c);
        assertFalse(mv.validate(ff, prefsRequiredField, process));
        assertEquals("testprocess (1): error: junitgrp in Monograph error", mv.getProblems().get(0));
        assertEquals("testprocess (1): error: junitMetadata in Monograph error", mv.getProblems().get(1));
        assertEquals("testprocess (1): error: junitPerson in Monograph error", mv.getProblems().get(2));
        assertEquals("testprocess (1): error: junitCorporate in Monograph error", mv.getProblems().get(3));
        mv.getProblems().clear();

        // validation is successful, all required fields exist and are not empty
        md.setValue("x");
        p.setLastname("x");
        c.setMainName("x");
        assertTrue(mv.validate(ff, prefsRequiredField, process));
    }

    @Test
    public void testVerificationErrorsValidationExpression() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        Metadata md = new Metadata(prefs.getMetadataTypeByName("DocLanguage"));
        md.setValue("invalid");
        log.addMetadata(md);
        assertFalse(mv.validate(fileformat, prefs, process));
        assertEquals("The value 'invalid' does not correspond to a three-letter ISO 639 code.", mv.getProblems().get(0));
        md.setValue("ger");
        assertTrue(mv.validate(fileformat, prefs, process));
    }

    @Test
    public void testValidateIdentifier() throws Exception {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        // no anchor
        DocStruct log = fileformat.getDigitalDocument().getLogicalDocStruct();
        assertTrue(mv.validateIdentifier(log));

        // create anchor docstruct
        Fileformat ff = new MetsMods(prefs);
        DigitalDocument dd = new DigitalDocument();
        ff.setDigitalDocument(dd);
        DocStruct anchor = dd.createDocStruct(prefs.getDocStrctTypeByName("MultiVolumeWork"));
        DocStruct volume = dd.createDocStruct(prefs.getDocStrctTypeByName("Volume"));
        dd.setLogicalDocStruct(anchor);
        anchor.addChild(volume);

        // identical identifier exist
        Metadata md1 = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
        Metadata md2 = new Metadata(prefs.getMetadataTypeByName("CatalogIDDigital"));
        anchor.addMetadata(md1);
        volume.addMetadata(md2);
        md1.setValue("identical");
        md2.setValue("identical");
        assertFalse(mv.validateIdentifier(anchor));

        // identifier differ, but contain special characters
        md1.setValue("123!!!");
        md2.setValue("456§§§");
        assertFalse(mv.validateIdentifier(anchor));

        // identifier differ and contain valid characters
        md1.setValue("123");
        md2.setValue("456");
        assertTrue(mv.validateIdentifier(anchor));
    }

    @SuppressWarnings("deprecation")
    private void prepareMocking() {
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(HttpSession.class);
        PowerMock.mockStatic(Helper.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        FacesContextHelper.setFacesContext(facesContext);
        //        facesContext.responseComplete();
        EasyMock.expect(FacesContext.getCurrentInstance()).andReturn(facesContext).anyTimes();
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getSession(EasyMock.anyBoolean())).andReturn(session).anyTimes();
        EasyMock.expect(externalContext.getRequest()).andReturn(request).anyTimes();

        EasyMock.expect(request.getScheme()).andReturn("http://").anyTimes();

        EasyMock.expect(request.getServerName()).andReturn("example.com").anyTimes();
        EasyMock.expect(request.getServerPort()).andReturn(80).anyTimes();
        EasyMock.expect(request.getContextPath()).andReturn("goobi").anyTimes();

        EasyMock.expect(session.getId()).andReturn("fixture").anyTimes();

        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(session.getServletContext()).andReturn(context).anyTimes();
        EasyMock.expect(context.getContextPath()).andReturn("fixture").anyTimes();

        Application application = EasyMock.createMock(Application.class);

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();

        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();

        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();

        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("error").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString())).andReturn("error").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString())).andReturn("error").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString()))
                .andReturn("error")
                .anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getRequestParameter(EasyMock.anyString())).andReturn("1").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setMeldung(EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyObject(Exception.class));
        Helper.setFehlerMeldung(EasyMock.anyString());
        PowerMock.mockStatic(MetadataManager.class);
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject());

        PowerMock.replay(Helper.class);
        EasyMock.replay(request);
        EasyMock.replay(root);
        EasyMock.replay(session);
        EasyMock.replay(application);
        EasyMock.replay(externalContext);
        EasyMock.replay(context);
        EasyMock.replay(facesContext);
    }
}
