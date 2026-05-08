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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.ImportFormat;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ImportProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.mock.MockUploadedFile;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.servlet.http.Part;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@ExtendWith(MockitoExtension.class)
public class MassImportFormTest extends AbstractTest {

    private Process template;

    private Step secondStep;
    private List<User> userList = new ArrayList<>();

    private MockedStatic<FacesContext> mockedFacesContext;
    private MockedStatic<ExternalContext> mockedExternalContext;
    private MockedStatic<Helper> mockedHelper;

    @AfterEach
    public void tearDown() throws Exception {
        // clean up uploaded test file so it doesn't pollute src/test/resources/tmp/
        Path uploadedFile = Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder(), "junit.xml");
        Files.deleteIfExists(uploadedFile);
        if (mockedHelper != null) mockedHelper.close();
        if (mockedExternalContext != null) mockedExternalContext.close();
        if (mockedFacesContext != null) mockedFacesContext.close();
    }

    @BeforeEach
    public void setUp() throws Exception {
        Path configBase = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(configBase.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        this.template = MockProcess.createProcess();
        this.template.setDocket(new Docket());
        this.template.setDocketId(0);
        this.template.setId(1);
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

    @SuppressWarnings("deprecation")
    private void prepareMocking() {
        mockedFacesContext = Mockito.mockStatic(FacesContext.class);
        mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
        mockedHelper = Mockito.mockStatic(Helper.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        UIViewRoot root = EasyMock.createMock(UIViewRoot.class);
        Application application = EasyMock.createMock(Application.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        PartialViewContext pvc = EasyMock.createMock(PartialViewContext.class);

        EasyMock.expect(facesContext.getApplication()).andReturn(application).anyTimes();
        List<Locale> locale = new ArrayList<>();
        locale.add(Locale.GERMAN);

        EasyMock.expect(facesContext.getViewRoot()).andReturn(root).anyTimes();

        FacesContextHelper.setFacesContext(facesContext);
        mockedFacesContext.when(() -> FacesContext.getCurrentInstance()).thenReturn(facesContext);
        Map<String, Object> requestMap = new HashMap<>();
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getSessionMap()).andReturn(requestMap).anyTimes();

        EasyMock.expect(root.getLocale()).andReturn(Locale.GERMAN).anyTimes();
        EasyMock.expect(application.getSupportedLocales()).andReturn(locale.iterator()).anyTimes();
        facesContext.addMessage(EasyMock.anyString(), EasyMock.anyObject(FacesMessage.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(facesContext.getPartialViewContext()).andReturn(pvc).anyTimes();
        EasyMock.expect(pvc.getRenderIds()).andReturn(new ArrayList<>()).anyTimes();

        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("");
        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);

        EasyMock.replay(root);
        EasyMock.replay(application);
        EasyMock.replay(externalContext);
        EasyMock.replay(pvc);
        EasyMock.replay(facesContext);
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
        assertEquals("", massImportForm.prepare());
        secondStep.setBenutzer(userList);
        assertEquals("process_import_1", massImportForm.prepare());
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
    public void testUploadedFile() throws Exception {
        InputStream stream = new FileInputStream(template.getProcessDataDirectory() + "/meta.xml");
        Part file = new MockUploadedFile(stream, "junit");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);

        massImportForm.setUploadedFile(file);
        assertEquals(file, massImportForm.getUploadedFile());

    }

    @Test
    public void testUploadFile() throws Exception {
        InputStream stream = new FileInputStream(template.getProcessDataDirectory() + "/meta.xml");
        Part file = new MockUploadedFile(stream, "./some/path\\junit.xml");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);

        massImportForm.setUploadedFile(file);
        massImportForm.uploadFile();

        Path dest = Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder(), "junit.xml");
        assertTrue(Files.exists(dest) && Files.isRegularFile(dest));
    }

    // @Test
    public void testConvertWithFileUpload() throws Exception {
        InputStream stream = new FileInputStream(template.getProcessDataDirectory() + "/meta.xml");
        Part file = new MockUploadedFile(stream, "./some/path\\junit.xml");
        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setUploadedFile(file);
        massImportForm.uploadFile();
        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }

    // @Test
    public void testConvertWithFileId() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setIdList("junit");

        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }

    // @Test
    public void testConvertWithFileRecord() throws FileNotFoundException {

        MassImportForm massImportForm = new MassImportForm();
        assertNotNull(massImportForm);
        massImportForm.setTemplate(template);
        massImportForm.setRecords("junit");

        massImportForm.setCurrentPlugin("JunitImportPlugin");
        String fixture = massImportForm.convertData();
        assertEquals("process_import_3", fixture);
    }

    // @Test
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

    @Test
    public void testPrepareResetsState() {

        // MassImportForm massImportForm = new MassImportForm();
        // assertNotNull(massImportForm);

        // massImportForm.setTemplate(template);
        // secondStep.setBenutzer(userList);

        // massImportForm.setCurrentPlugin("JUnitImportPlugin");
        // massImportForm.setRecords("SomeRecords");
        // massImportForm.setIdList("SomeIDs");
        // List<String> filenames = new ArrayList<>();
        // filenames.add("test");
        // massImportForm.setSelectedFilenames(filenames);

        // massImportForm.prepare();

        // assertEquals("", massImportForm.getCurrentPlugin());
        // assertEquals("", massImportForm.getRecords());
        // assertEquals("", massImportForm.getIdList());
        // assertTrue(massImportForm.getSelectedFilenames().isEmpty());
    }
}
