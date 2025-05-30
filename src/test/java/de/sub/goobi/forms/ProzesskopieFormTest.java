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

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)

@PrepareForTest({ PropertyManager.class, ProcessManager.class, MetadataManager.class,
        HistoryAnalyserJob.class, StepManager.class, Helper.class })
public class ProzesskopieFormTest extends AbstractTest {

    private Process template;
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Step secondStep;
    private List<User> userList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        ConfigurationHelper.getInstance().setParameter("script_createDirMeta", "");

        this.template = MockProcess.createProcess();
        this.template.setDocket(new Docket());
        this.template.setDocketId(0);
        this.template.setId(666);
        this.template.setMetadatenKonfigurationID(1);
        this.template.setIstTemplate(true);

        User user = new User();
        user.setLogin("login");
        user.setEncryptedPassword("password");
        userList.add(user);

        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        step.setTitel("titel");
        step.setBenutzer(userList);
        step.setReihenfolge(1);
        step.setBearbeitungsstatusEnum(StepStatus.DONE);
        stepList.add(step);

        secondStep = new Step();
        secondStep.setTitel("titel");
        secondStep.setReihenfolge(2);
        secondStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
        secondStep.setBenutzer(new ArrayList<>());
        stepList.add(secondStep);
        this.template.setSchritte(stepList);

        prepareMocking();

    }

    @Test
    public void testConstructor() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
    }

    @Test
    public void testprepare() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        assertEquals("", form.prepare());

        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
    }

    @Test
    public void testopacAuswerten() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        form.setOpacKatalog("KXP");
        assertEquals("KXP", form.getOpacKatalog());
        form.setOpacSuchbegriff("517154005");
        assertEquals("", form.opacAuswerten());
    }

    @Test
    public void testOpenFirstPage() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        assertEquals("process_new1", form.openFirstPage());
    }

    @Test
    public void testOpenPage2() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        assertEquals("process_new1", form.openPage2());

        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        assertEquals("", form.opacAuswerten());
        assertEquals("process_new2", form.openPage2());

    }

    @Test
    public void testCreateNewProcess() throws Exception {
        Path meta = folder.newFolder("metadata").toPath();
        Files.createDirectories(meta);
        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", meta.toString() + FileSystems.getDefault().getSeparator());
        Path processFolder = Paths.get(meta.toString(), "0");
        Files.createDirectories(processFolder);

        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        form.getProzessKopie().setId(0);
        assertEquals("", form.opacAuswerten());
        String fixture = form.createNewProcess();
        assertEquals("process_new3", fixture);

    }

    @Test
    public void testGetProzessVorlage() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        assertEquals(template, form.getProzessVorlage());
    }

    @Test
    public void testGetAuswahl() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setAuswahl(1);
        assertEquals(1, form.getAuswahl().intValue());
    }

    @Test
    public void testGetAllOpacCatalogues() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        form.prepare();
        List<String> fixture = form.getAllOpacCatalogues();

        assertEquals(1, fixture.size());
    }

    @Test
    public void testGetAllDoctypes() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        form.prepare();
        List<ConfigOpacDoctype> fixture = form.getAllDoctypes();

        assertEquals(2, fixture.size());
    }

    @Test
    public void testSetDocType() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        form.getProzessKopie().setId(0);
        assertEquals("", form.opacAuswerten());
        form.setDocType("monograph");
        form.setDocType("periodical");
        //        form.setDocType("multivolume");

    }

    @Test
    public void testAdditionalFields() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());

        List<AdditionalField> fixture = form.getAdditionalFields();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());

    }

    @Test
    public void testDigitalCollections() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        List<String> fixture = new ArrayList<>();
        fixture.add("1");
        form.setDigitalCollections(fixture);
        assertEquals(fixture, form.getDigitalCollections());
    }

    @Test
    public void testCalculateProcessTitle() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);

        AdditionalField field1 = new AdditionalField();
        field1.setTitel("TitleDocMain");
        field1.setWert("Test title");

        AdditionalField field2 = new AdditionalField();
        field2.setTitel("ListOfCreators");
        field2.setWert("first author");

        AdditionalField field3 = new AdditionalField();
        field3.setTitel("Identifier");
        field3.setWert("123");

        List<AdditionalField> fixture = new ArrayList<>();
        fixture.add(field1);
        fixture.add(field2);
        fixture.add(field3);
        form.setAdditionalFields(fixture);
        form.calculateProcessTitle();

    }

    @Test
    public void testUseOpac() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        assertFalse(form.isUseOpac());
    }

    @Test
    public void testUseTemplates() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        assertFalse(form.isUseTemplates());
    }

    @Test
    public void testTifHeader_documentname() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setTifHeaderDocumentname(fixture);
        assertEquals(fixture, form.getTifHeaderDocumentname());
    }

    @Test
    public void testTifHeader_imagedescription() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setTifHeaderImagedescription(fixture);
        assertEquals(fixture, form.getTifHeaderImagedescription());
    }

    @Test
    public void testProzessKopie() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        Process p = new Process();
        form.setProzessKopie(p);
        assertEquals(p, form.getProzessKopie());
    }

    @Test
    public void testOpacSuchfeld() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setOpacSuchfeld(fixture);
        assertEquals(fixture, form.getOpacSuchfeld());
    }

    @Test
    public void testOpacSuchbegriff() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setOpacSuchbegriff(fixture);
        assertEquals(fixture, form.getOpacSuchbegriff());
    }

    @Test
    public void testStandardFields() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        assertEquals(6, form.getStandardFields().size());
        assertTrue(form.getStandardFields().get("doctype").booleanValue());
    }

    @Test
    public void testImagesGuessed() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertEquals(0, form.getImagesGuessed().intValue());
        form.setImagesGuessed(1);
        assertEquals(1, form.getImagesGuessed().intValue());
    }

    @Test
    public void testRulesetSelection() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertEquals(0, form.getRulesetSelection().intValue());
        form.setProzessKopie(template);
        assertEquals(11111, form.getRulesetSelection().intValue());
    }

    @Test
    public void testCreateAtstsl() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertEquals("abcfix", form.createAtstsl("fix fixture fixture fixture fixture fixture", "abc def"));
        assertEquals("abcdatit", form.createAtstsl("ätitle with umlauts", "abcdef"));
        assertEquals("worwww", form.createAtstsl("wor w w w w w", ""));
        assertEquals("longlolol", form.createAtstsl("longword longword longword longword longword", ""));
        assertEquals("longwo", form.createAtstsl("longword word", ""));
    }

    @SuppressWarnings("unchecked")
    private void prepareMocking() throws Exception {

        GoobiProperty prop = new GoobiProperty(PropertyOwnerType.PROCESS);
        List<GoobiProperty> propList = new ArrayList<>();
        propList.add(prop);

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(propList).anyTimes();
        PowerMock.replay(PropertyManager.class);

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.countProcessTitle(EasyMock.anyString(), EasyMock.anyObject(Institution.class))).andReturn(0).anyTimes();

        PowerMock.mockStatic(MetadataManager.class);
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));
        MetadataManager.updateJSONMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));

        PowerMock.mockStatic(HistoryAnalyserJob.class);
        EasyMock.expect(HistoryAnalyserJob.updateHistoryForProzess(EasyMock.anyObject(Process.class))).andReturn(true);
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        //
        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(this.template.getSchritte());
        //
        //        PowerMock.mockStatic(FilesystemHelper.class);
        //        FilesystemHelper.createDirectory(EasyMock.anyString());
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(null);
        //        EasyMock.expectLastCall().anyTimes();

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyObject(Exception.class));
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString())).andReturn("").anyTimes();

        PowerMock.replay(Helper.class);

        PowerMock.replay(ProcessManager.class);
        PowerMock.replay(MetadataManager.class);
        PowerMock.replay(HistoryAnalyserJob.class);
        PowerMock.replay(StepManager.class);
        //        PowerMock.replay(FilesystemHelper.class);

    }

}
