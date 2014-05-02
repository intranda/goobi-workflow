package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.ImportFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;

public class MassImportFormTest {

    private Process template;
    private static final String RULESET_NAME = "ruleset.xml";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Step secondStep;
    private List<User> userList;
    private String datafolder;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        setUpTemplate();
        setUpRuleset();
        setUpConfig();
    }

    private void setUpConfig() {

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");

    }

    private void setUpRuleset() throws IOException, URISyntaxException {
        File rulesetFolder = folder.newFolder("rulesets");
        rulesetFolder.mkdir();
        datafolder = System.getenv("junitdata");
        if (datafolder == null) {
            datafolder = "/opt/digiverso/junit/data/";
        }
        File rulesetTemplate = new File(datafolder + RULESET_NAME);
        File rulesetFile = new File(rulesetFolder, RULESET_NAME);
        FileUtils.copyFile(rulesetTemplate, rulesetFile);
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);
        ConfigurationHelper.CONFIG_FILE_NAME = datafolder + "goobi_config.properties";
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);
        ConfigurationHelper.getInstance().setParameter("RegelsaetzeVerzeichnis", rulesetFolder.getAbsolutePath() + File.separator);
        template.setRegelsatz(ruleset);
    }

    private void setUpTemplate() {
        template = new Process();
        template.setTitel("template");
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
        user.setPasswort("password");
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

    @Test
    public void testMassImportForm() throws Exception {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
    }

    @Test
    public void testPrepare() throws Exception {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        assertEquals("", massImportForm.Prepare());
        secondStep.setBenutzer(userList);
        assertEquals("process_import_1", massImportForm.Prepare());
    }

    @Test
    public void testGetUsablePluginsForFiles() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> plugins = new ArrayList<>();
        plugins.add("test");
        massImportForm.setUsablePluginsForFiles(plugins);
        assertSame(plugins, massImportForm.getUsablePluginsForFiles());
    }

    @Test
    public void testGetUsablePluginsForFolder() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> plugins = new ArrayList<>();
        plugins.add("test");
        massImportForm.setUsablePluginsForFolder(plugins);
        assertSame(plugins, massImportForm.getUsablePluginsForFolder());
    }

    @Test
    public void testGetUsablePluginsForIDs() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> plugins = new ArrayList<>();
        plugins.add("test");
        massImportForm.setUsablePluginsForIDs(plugins);
        assertSame(plugins, massImportForm.getUsablePluginsForIDs());
    }

    @Test
    public void testGetUsablePluginsForRecords() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> plugins = new ArrayList<>();
        plugins.add("test");
        massImportForm.setUsablePluginsForRecords(plugins);
        assertSame(plugins, massImportForm.getUsablePluginsForRecords());
    }

    @Test
    public void testAllFilenames() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> filenames = new ArrayList<>();
        filenames.add("test");
        massImportForm.setAllFilenames(filenames);
        assertSame(filenames, massImportForm.getAllFilenames());
    }

    @Test
    public void testSelectedFilenames() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> filenames = new ArrayList<>();
        filenames.add("test");
        massImportForm.setSelectedFilenames(filenames);
        assertSame(filenames, massImportForm.getSelectedFilenames());
    }

    @Test
    public void testGetFormats() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> fixture = massImportForm.getFormats();
        assertNotNull(fixture);
        assertNotEquals(0, fixture.size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testCurrentFormat() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        assertEquals("", massImportForm.getCurrentFormat());
        massImportForm.setCurrentFormat("marcxml");
        assertEquals(ImportFormat.MARCXML.getTitle(), massImportForm.getCurrentFormat());
    }

    @Test
    public void testIdList() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        String fixture = "test";
        massImportForm.setIdList(fixture);
        assertEquals("test", massImportForm.getIdList());
    }

    
    @Test
    public void testRecords() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        String fixture = "test";
        massImportForm.setRecords(fixture);
        assertEquals("test", massImportForm.getRecords());
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testProcess() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<Process> fixture = new ArrayList<>();
        fixture.add(template);
        massImportForm.setProcess(fixture);
        assertEquals(fixture, massImportForm.getProcess());
        massImportForm.setProcesses(fixture);
        assertEquals(fixture, massImportForm.getProcess());
    }
    
    @Test
    public void testTemplate(){
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        assertEquals(template, massImportForm.getTemplate());
    }
    
    @Test
    public void testDigitalCollections(){
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> fixture = new ArrayList<>();
        fixture.add("collection");
        massImportForm.setDigitalCollections(fixture);
        assertEquals(fixture, massImportForm.getDigitalCollections());
    }
    
    @Test
    public void testPossibleDigitalCollections(){
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> fixture = new ArrayList<>();
        fixture.add("collection");
        massImportForm.setPossibleDigitalCollection(fixture);
        assertEquals(fixture, massImportForm.getPossibleDigitalCollection());
    }
    
    @Test
    public void testIds() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> fixture = new ArrayList<>();
        fixture.add("ID");
        massImportForm.setIds(fixture);
        assertEquals(fixture, massImportForm.getIds());
    }
    
    @Test
    public void testFormat() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        assertEquals("", massImportForm.getFormat());
        massImportForm.setFormat("marcxml");
        assertEquals(ImportFormat.MARCXML.getTitle(), massImportForm.getFormat());
    }
    
 
}
