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

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.goobi.beans.Docket;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class ProzesskopieFormTest extends AbstractTest {

    private Process template;

    @TempDir
    private Path tempDir;

    private Step secondStep;
    private List<User> userList = new ArrayList<>();

    private MockedStatic<PropertyManager> mockedPropertyManager;
    private MockedStatic<ProcessManager> mockedProcessManager;
    private MockedStatic<MetadataManager> mockedMetadataManager;
    private MockedStatic<HistoryAnalyserJob> mockedHistoryAnalyserJob;
    private MockedStatic<StepManager> mockedStepManager;
    private MockedStatic<Helper> mockedHelper;

    @BeforeEach
    public void setUp() throws Exception {
        Path configBase = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path goobiFolder = Paths.get(configBase.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties");
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        ConfigurationHelper.getInstance().setParameter("script_createDirMeta", "");

        // redirect metadata writes to a temp directory so tests don't pollute src/test/resources
        File metadataDir = tempDir.toFile();
        ConfigurationHelper.getInstance().setParameter("dataFolder", metadataDir.getAbsolutePath() + FileSystems.getDefault().getSeparator());

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

    @AfterEach
    public void tearDown() {
        if (mockedHelper != null) {
            mockedHelper.close();
        }
        if (mockedStepManager != null) {
            mockedStepManager.close();
        }
        if (mockedHistoryAnalyserJob != null) {
            mockedHistoryAnalyserJob.close();
        }
        if (mockedMetadataManager != null) {
            mockedMetadataManager.close();
        }
        if (mockedProcessManager != null) {
            mockedProcessManager.close();
        }
        if (mockedPropertyManager != null) {
            mockedPropertyManager.close();
        }
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
        String metaPath = ConfigurationHelper.getInstance().getMetadataFolder();
        Path processFolder = Paths.get(metaPath, "0");
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
    public void testTifHeaderDocumentname() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setTifHeaderDocumentname(fixture);
        assertEquals(fixture, form.getTifHeaderDocumentname());
    }

    @Test
    public void testTifHeaderImagedescription() {
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

    @Test
    public void testShowDuplicateButtonDefaultTrue() {
        ProzesskopieForm form = new ProzesskopieForm();
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());
        assertTrue(form.isShowDuplicateButton());
    }

    @Test
    public void testPrepareDuplicate() {
        ProzesskopieForm form = new ProzesskopieForm();
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());

        // The test config defines duplicate-titled fields for different doc types
        // (e.g. "Title" for isnotdoctype=multivolume and "Title" for isdoctype=multivolume).
        // Default doctype is "monograph", so only the first "Title" field is active.
        List<AdditionalField> fields = form.getAdditionalFields();
        assertNotNull(fields);
        assertTrue(fields.size() >= 7, "Expected at least 7 additional fields from test config");

        // Set values on the fields visible for the active doctype (monograph)
        // Field index 1 = "Title" (isnotdoctype=multivolume) — active for monograph
        // Field index 2 = "Title" (isdoctype=multivolume) — NOT active for monograph
        fields.get(1).setWert("Das Kapital");
        fields.get(3).setWert("Kapital, Das");
        fields.get(5).setWert("1867");

        form.setTifHeaderDocumentname("my-document");
        form.setTifHeaderImagedescription("my-description");
        form.setAddToWikiField("some wiki text");

        // call prepareDuplicate
        String result = form.prepareDuplicate();
        assertEquals("process_new1", result);

        // verify field values are preserved — especially the active fields must not be
        // overwritten by the empty duplicate-titled fields for other doc types
        List<AdditionalField> restored = form.getAdditionalFields();
        assertEquals("Das Kapital", restored.get(1).getWert()); // Title (monograph)
        assertEquals("", restored.get(2).getWert()); // Title (multivolume) was empty
        assertEquals("Kapital, Das", restored.get(3).getWert()); // Sorting title (monograph)
        assertEquals("", restored.get(4).getWert()); // Sorting title (multivolume) was empty
        assertEquals("1867", restored.get(5).getWert()); // Publishing year (monograph)
        assertEquals("", restored.get(6).getWert()); // Publishing year (multivolume) was empty

        assertEquals("my-document", form.getTifHeaderDocumentname());
        assertEquals("my-description", form.getTifHeaderImagedescription());
        assertEquals("some wiki text", form.getAddToWikiField());

        // process title must be cleared
        assertEquals("", form.getProzessKopie().getTitel());
    }

    @Test
    public void testReadMetadataFromTemplateDoesNotAccumulateDigitalCollections() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.prepare());

        // a source process carrying two digital collection properties
        Process source = new Process();
        source.setId(4242);
        source.setTitel("source");
        source.setRegelsatz(template.getRegelsatz());

        List<GoobiProperty> props = new ArrayList<>();
        GoobiProperty c1 = new GoobiProperty(PropertyOwnerType.PROCESS);
        c1.setPropertyName("digitalCollection");
        c1.setPropertyValue("Collection A");
        GoobiProperty c2 = new GoobiProperty(PropertyOwnerType.PROCESS);
        c2.setPropertyName("digitalCollection");
        c2.setPropertyValue("Collection B");
        props.add(c1);
        props.add(c2);
        source.setEigenschaften(props);

        mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(source);
        form.setAuswahl(source.getId());

        // first read populates the collections from the template's properties
        form.readMetadataFromTemplate();
        int sizeAfterFirstRead = form.getDigitalCollections().size();

        // reading from the template a second time must not append the same values again
        form.readMetadataFromTemplate();
        List<String> collections = form.getDigitalCollections();

        assertEquals(sizeAfterFirstRead, collections.size(),
                "digitalCollections must not grow when reading from a template repeatedly");
        Set<String> unique = new HashSet<>(collections);
        assertEquals(unique.size(), collections.size(), "digitalCollections must not contain duplicate entries");
    }

    private void prepareMocking() throws Exception {

        GoobiProperty prop = new GoobiProperty(PropertyOwnerType.PROCESS);
        List<GoobiProperty> propList = new ArrayList<>();
        propList.add(prop);

        mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
        mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
        mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
        mockedHistoryAnalyserJob = Mockito.mockStatic(HistoryAnalyserJob.class);
        mockedStepManager = Mockito.mockStatic(StepManager.class);
        mockedHelper = Mockito.mockStatic(Helper.class);

        mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(propList);

        mockedProcessManager.when(() -> ProcessManager.countProcessTitle(Mockito.anyString(), Mockito.any(Institution.class))).thenReturn(0);

        mockedHistoryAnalyserJob.when(() -> HistoryAnalyserJob.updateHistoryForProzess(Mockito.any(Process.class))).thenReturn(true);

        mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(this.template.getSchritte());

        mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(null);

        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
        mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");
    }

}
