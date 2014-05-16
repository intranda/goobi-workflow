package de.sub.goobi.forms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.TemplateManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TemplateManager.class, MasterpieceManager.class, PropertyManager.class, ProcessManager.class })
public class ProzesskopieFormTest {

    private Process template;
    private static final String RULESET_NAME = "ruleset.xml";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Step secondStep;
    private List<User> userList;
    private String datafolder;

    @Before
    public void setUp() throws Exception {
        datafolder = System.getenv("junitdata");
        if (datafolder == null) {
            datafolder = "/opt/digiverso/junit/data/";
        }

        setUpTemplate();
        setUpRuleset();
        setUpConfig();
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
        form.setOpacKatalog("GBV");
        form.setOpacSuchbegriff("517154005");
        assertEquals("", form.OpacAuswerten());
    }

    @Test
    public void testTemplateAuswahlAuswerten() {
        // TODO
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

        form.setOpacKatalog("GBV");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");
        assertEquals("", form.OpacAuswerten());
        assertEquals("process_new2", form.GoToSeite2());

    }

    @Test
    public void testNeuenProzessAnlegen() throws Exception {
        ProzesskopieForm form = new ProzesskopieForm();
        assertNotNull(form);

        form.setProzessVorlage(template);
        secondStep.setBenutzer(userList);
        assertEquals("process_new1", form.Prepare());
        form.setOpacKatalog("GBV");
        form.setOpacSuchbegriff("517154005");
        form.getProzessKopie().setTitel("test");

        assertEquals("", form.OpacAuswerten());

           // TODO fix empty id
//        String fixture = form.NeuenProzessAnlegen();
//        assertEquals("process_new3", fixture);

    }

    private void setUpTemplate() {
        template = new Process();
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

    private void setUpConfig() {

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);

    }

    private void setUpRuleset() throws IOException, URISyntaxException {
        File rulesetFolder = folder.newFolder("rulesets");
        rulesetFolder.mkdir();

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

    private void prepareMocking() throws DAOException {
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
        EasyMock.expect(ProcessManager.countProcessTitle(EasyMock.anyString())).andReturn(0).anyTimes();

        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(ProcessManager.class);
    }

}
