package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ImportProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockUploadedFile;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JobCreation.class)
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
        prepareMocking();
    }

    private void prepareMocking() {
        PowerMock.mockStatic(JobCreation.class);
        EasyMock.expect(JobCreation.generateProcess(EasyMock.isA(ImportObject.class), EasyMock.isA(Process.class))).andReturn(new Process());
        EasyMock.expect(JobCreation.generateProcess(EasyMock.isA(ImportObject.class), EasyMock.isA(Process.class))).andReturn(new Process());
        EasyMock.expect(JobCreation.generateProcess(EasyMock.isA(ImportObject.class), EasyMock.isA(Process.class))).andReturn(new Process());
        EasyMock.expect(JobCreation.generateProcess(EasyMock.isA(ImportObject.class), EasyMock.isA(Process.class))).andReturn(new Process());
        EasyMock.expect(JobCreation.generateProcess(EasyMock.isA(ImportObject.class), EasyMock.isA(Process.class))).andReturn(null);

        
        PowerMock.replayAll();
    }
    
    private void setUpConfig() {

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("tempfolder", folder.getRoot().getAbsolutePath() + File.separator);

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
    public void testTemplate() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        assertEquals(template, massImportForm.getTemplate());
    }

    @Test
    public void testDigitalCollections() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        List<String> fixture = new ArrayList<>();
        fixture.add("collection");
        massImportForm.setDigitalCollections(fixture);
        assertEquals(fixture, massImportForm.getDigitalCollections());
    }

    @Test
    public void testPossibleDigitalCollections() {
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

    @Test
    public void testGetCurrentPlugin() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        assertEquals("JunitImportPlugin", massImportForm.getCurrentPlugin());
    }

    @Test
    public void testGetImportPlugin() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        IImportPlugin plugin = massImportForm.getPlugin();
        assertNotNull(plugin);
        assertEquals("JunitImportPlugin", plugin.getTitle());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetInclude() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        assertEquals("plugins/JunitImportPlugin.jsp", massImportForm.getInclude());
    }

    @Test
    public void testGetHasNextPage() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        boolean fixture = massImportForm.getHasNextPage();
        assertFalse(fixture);

        massImportForm.setCurrentPlugin("JunitImportPluginWithSecondPage");
        fixture = massImportForm.getHasNextPage();
        assertTrue(fixture);

        massImportForm.setCurrentPlugin("JunitImportPluginWithProperties");
        fixture = massImportForm.getHasNextPage();
        assertTrue(fixture);
    }

    @Test
    public void testGetNextPage() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.nextPage();
        assertEquals("", fixture);

        massImportForm.setIdList("junit");
        fixture = massImportForm.nextPage();
        assertEquals("process_import_2", fixture);

        massImportForm.setCurrentPlugin("JunitImportPluginWithSecondPage");
        fixture = massImportForm.nextPage();
        assertEquals("process_import_2_mass", fixture);

    }

    @Test
    public void testGetProperties() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPluginWithProperties");
        List<ImportProperty> fixture = massImportForm.getProperties();
        assertEquals(1, fixture.size());
    }

    @Test
    public void testProcessList() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);

        List<Process> processList = new ArrayList<>();
        processList.add(template);
        massImportForm.setProcessList(processList);
        assertEquals(processList, massImportForm.getProcessList());
    }

    @Test
    public void testGetDocstructs() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        List<? extends DocstructElement> fixture = massImportForm.getDocstructs();
        assertEquals(0, fixture.size());

        massImportForm.setCurrentPlugin("JunitImportPluginWithSecondPage");
        fixture = massImportForm.getDocstructs();
        assertEquals(1, fixture.size());
    }

    @Test
    public void testGetDocstructssize() {
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setCurrentPlugin("JunitImportPluginWithSecondPage");
        assertEquals(1, massImportForm.getDocstructssize());
    }

    @Test
    public void testUploadedFile() throws FileNotFoundException {
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/metadata.xml");
        UploadedFile file = new MockUploadedFile(stream, "junit");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);

        massImportForm.setUploadedFile(file);
        assertEquals(file, massImportForm.getUploadedFile());
        
    }
    
    @Test
    public void testUploadFile() throws FileNotFoundException {
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/metadata.xml");
        UploadedFile file = new MockUploadedFile(stream, "./some/path\\junit.xml");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);

        massImportForm.setUploadedFile(file);
        massImportForm.uploadFile();
        
        File dest = new File(ConfigurationHelper.getInstance().getTemporaryFolder(), "junit.xml");
        assertTrue(dest.exists() && dest.isFile());
    }
    
    
    @Test
    public void testConvertWithFileUpload() throws FileNotFoundException {
        InputStream stream = new FileInputStream("/opt/digiverso/junit/data/metadata.xml");
        UploadedFile file = new MockUploadedFile(stream, "./some/path\\junit.xml");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setUploadedFile(file);
        massImportForm.uploadFile();
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }
    
    @Test
    public void testConvertWithFileId() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setIdList("junit");
        
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }
    
    @Test
    public void testConvertWithFileRecord() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setRecords("junit");
        
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }
    
    @Test
    public void testConvertWithFileFileSelection() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        List<String> list = new ArrayList<>();
        list.add("junit");
        massImportForm.setSelectedFilenames(list);
        
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }
    
    @Test
    public void testConvertFail() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        List<String> list = new ArrayList<>();
        list.add("junit");
        massImportForm.setSelectedFilenames(list);
        
        massImportForm.setCurrentPlugin("JunitImportPluginError");
        String fixture = massImportForm.convertData();
        assertEquals("", fixture);
    }
}
