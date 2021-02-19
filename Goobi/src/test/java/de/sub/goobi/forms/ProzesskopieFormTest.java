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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
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

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;

@PowerMockIgnore({"javax.net.ssl.*", "javax.management.*"})
@RunWith(PowerMockRunner.class)

@PrepareForTest({ TemplateManager.class, MasterpieceManager.class, PropertyManager.class, ProcessManager.class, MetadataManager.class, HistoryAnalyserJob.class, StepManager.class })
public class ProzesskopieFormTest {

    private Process template;
    private static final String RULESET_NAME = "ruleset.xml";
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Step secondStep;
    private List<User> userList = new ArrayList<>();
    private String datafolder;

    @Before
    public void setUp() throws Exception {
        String resourcesFolder = "src/test/resources/"; // for junit tests in eclipse
        if (!Files.exists(Paths.get(resourcesFolder))) {
            resourcesFolder = "target/test-classes/"; // to run mvn test from cli or in jenkins
        }
        String goobiFolder = Paths.get(resourcesFolder).toAbsolutePath().toString() + "/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);
        ConfigurationHelper.getInstance().setParameter("script_createDirMeta", "");

        this.template = MockProcess.createProcess();
        this.template.setDocket(new Docket());
        this.template.setDocketId(0);
        this.template.setId(666);
        this.template.setMetadatenKonfigurationID(0);
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
        stepList.add(step);

        secondStep = new Step();
        secondStep.setTitel("titel");
        secondStep.setReihenfolge(2);
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
    public void testPrepare() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        assertEquals("", form.Prepare());

        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
    }

    @Test
    public void testOpacAuswerten() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        assertEquals("", form.OpacAuswerten());
    }

    @Test
    public void testGoToSeite1() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        assertEquals("process_new1", form.GoToSeite1());
    }

    @Test
    public void testGoToSeite2() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        assertEquals("process_new1", form.GoToSeite2());

        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        assertEquals("", form.OpacAuswerten());
        assertEquals("process_new2", form.GoToSeite2());

    }

    @Test
    public void testNeuenProzessAnlegen() throws Exception {
        Path meta = folder.newFolder("metadata").toPath();
        Files.createDirectories(meta);
        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", meta.toString() + FileSystems.getDefault().getSeparator());
        Path processFolder = Paths.get(meta.toString(), "0");
        Files.createDirectories(processFolder);

        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        form.getProzessKopie().setId(0);
        assertEquals("", form.OpacAuswerten());
        String fixture = form.NeuenProzessAnlegen();
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
        form.Prepare();
        List<String> fixture = form.getAllOpacCatalogues();

        assertEquals(1, fixture.size());
    }

    @Test
    public void testGetAllDoctypes() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        form.Prepare();
        List<ConfigOpacDoctype> fixture = form.getAllDoctypes();

        assertEquals(2, fixture.size());
    }

    @Test
    public void testSetDocType() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        form.setOpacKatalog("KXP");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        form.getProzessKopie().setId(0);
        assertEquals("", form.OpacAuswerten());
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
        assertEquals("process_new1", form.Prepare());

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
    public void testCalcProzesstitel() {
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
        form.CalcProzesstitel();

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
        form.setTifHeader_documentname(fixture);
        assertEquals(fixture, form.getTifHeader_documentname());
    }

    @Test
    public void testTifHeader_imagedescription() {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);
        String fixture = "test";
        form.setTifHeader_imagedescription(fixture);
        assertEquals(fixture, form.getTifHeader_imagedescription());
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

    private void setUpTemplate() {
        this.template = new Process();
        template.setTitel("template");
        template.setId(1);
        template.setDocket(new Docket());
        template.setDocketId(0);
        template.setId(666);
        template.setMetadatenKonfigurationID(0);
        template.setIstTemplate(true);

        Project project = new Project();
        project.setTitel("Project");
        template.setProjekt(project);
        project.setFileFormatInternal("Mets");
        project.setFileFormatDmsExport("Mets");
        userList = new ArrayList<>();
        User user = new User();
        user.setLogin("login");
        user.setEncryptedPassword("password");
        userList.add(user);

        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        step.setTitel("titel");
        step.setBenutzer(userList);
        step.setReihenfolge(1);
        stepList.add(step);

        secondStep = new Step();
        secondStep.setTitel("titel");
        secondStep.setReihenfolge(2);
        stepList.add(secondStep);
        template.setSchritte(stepList);

    }

    private void setUpConfig() {

        ConfigurationHelper.getInstance()
        .setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + FileSystems.getDefault().getSeparator());
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);
        ConfigurationHelper.getInstance().setParameter("TiffHeaderArtists", "1");
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);

    }

    private void setUpRuleset() throws IOException, URISyntaxException {
        Path rulesetFolder = folder.newFolder("rulesets").toPath();
        Files.createDirectories(rulesetFolder);
        Path rulesetTemplate = Paths.get(datafolder + RULESET_NAME);
        Path rulesetFile = Paths.get(rulesetFolder.toString(), RULESET_NAME);
        Files.copy(rulesetTemplate, rulesetFile, NIOFileUtils.STANDARD_COPY_OPTIONS);
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);
        ConfigurationHelper.getInstance().setParameter("RegelsaetzeVerzeichnis", rulesetFolder.toString() + FileSystems.getDefault().getSeparator());
        template.setRegelsatz(ruleset);
    }

    @SuppressWarnings("unchecked")
    private void prepareMocking() throws Exception {
        Template template = new Template();
        List<Template> templateList = new ArrayList<>();
        templateList.add(template);

        PowerMock.mockStatic(TemplateManager.class);
        EasyMock.expect(TemplateManager.getTemplatesForProcess(EasyMock.anyInt())).andReturn(templateList).anyTimes();

        PowerMock.replay(TemplateManager.class);

        Masterpiece master = new Masterpiece();
        List<Masterpiece> masterList = new ArrayList<>();
        masterList.add(master);

        PowerMock.mockStatic(MasterpieceManager.class);
        EasyMock.expect(MasterpieceManager.getMasterpiecesForProcess(EasyMock.anyInt())).andReturn(masterList).anyTimes();
        PowerMock.replay(MasterpieceManager.class);

        Processproperty prop = new Processproperty();
        List<Processproperty> propList = new ArrayList<>();
        propList.add(prop);

        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(propList).anyTimes();
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
        PowerMock.replay(ProcessManager.class);
        PowerMock.replay(MetadataManager.class);
        PowerMock.replay(HistoryAnalyserJob.class);
        PowerMock.replay(StepManager.class);
        //        PowerMock.replay(FilesystemHelper.class);

    }

}
