package de.sub.goobi.export.dms;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Ruleset;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MetadatenHelper.class)
public class ExportDmsTest {

    private static final String RULESET_NAME = "ruleset.xml";

    private Process testProcess = null;
    private File processFolder = null;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, URISyntaxException {
        testProcess = new Process();
        testProcess.setTitel("testprocess");
        testProcess.setId(1);

        // set temporary ruleset
        setUpRuleset();

        // set temporary process infrastructure
        setUpProcessFolder();

        setUpProject();

        setUpConfig();

    }

    private void setUpConfig() {
        String configFolder = System.getenv("junitdata");;
        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME =configFolder + "goobi_config.properties";

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("ExportFilesFromOptionalMetsFileGroups", "true");
    }

    private void setUpProject() throws IOException {
        Project project = new Project();
        project.setTitel("project");
        testProcess.setProjekt(project);
        project.setFileFormatInternal("Mets");
        project.setFileFormatDmsExport("Mets");
        File exportFolder = folder.newFolder("hotfolder");
        exportFolder.mkdir();
        project.setDmsImportImagesPath(exportFolder.getAbsolutePath() + File.separator);
        project.setDmsImportErrorPath(exportFolder.getAbsolutePath() + File.separator);
        project.setDmsImportSuccessPath(exportFolder.getAbsolutePath() + File.separator);
        project.setDmsImportRootPath(exportFolder.getAbsolutePath() + File.separator);
        project.setUseDmsImport(true);
        project.setDmsImportCreateProcessFolder(true);

        ProjectFileGroup presentation = new ProjectFileGroup();
        presentation.setMimetype("image/jp2");
        presentation.setName("PRESENTATION");
        presentation.setPath("/opt/digiverso/viewer/media/1/");
        presentation.setSuffix("jp2");
        presentation.setProject(project);

        ProjectFileGroup alto = new ProjectFileGroup();
        alto.setFolder("getAltoDirectory");
        alto.setMimetype("text/xml");
        alto.setName("ALTO");
        alto.setPath("/opt/digiverso/viewer/alto/1/");
        alto.setSuffix("xml");
        alto.setProject(project);

        List<ProjectFileGroup> list = new ArrayList<ProjectFileGroup>();
        list.add(presentation);
        list.add(alto);
        project.setFilegroups(list);

        File configFolder = folder.newFolder("config");
        configFolder.mkdir();
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        File digitalCollectionTemplate = new File(folder + "goobi_digitalCollections.xml");
        File digitalCollection = new File(configFolder, "goobi_digitalCollections.xml");
        FileUtils.copyFile(digitalCollectionTemplate, digitalCollection);

        File projectsTemplate = new File(folder + "goobi_projects.xml");
        File projects = new File(configFolder, "goobi_projects.xml");
        FileUtils.copyFile(projectsTemplate, projects);

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", configFolder.getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("localMessages", "/opt/digiverso/junit/data/");
    }

    private void setUpRuleset() throws IOException, URISyntaxException {
        File rulesetFolder = folder.newFolder("rulesets");
        rulesetFolder.mkdir();
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        File rulesetTemplate = new File(folder + RULESET_NAME);
        File rulesetFile = new File(rulesetFolder, RULESET_NAME);
        FileUtils.copyFile(rulesetTemplate, rulesetFile);
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);
        ConfigurationHelper.getInstance().setParameter("RegelsaetzeVerzeichnis", rulesetFolder.getAbsolutePath() + File.separator);
        testProcess.setRegelsatz(ruleset);
    }

    private void setUpProcessFolder() throws IOException, URISyntaxException {
        processFolder = folder.newFolder("1");
        processFolder.mkdir();
        File images = new File(processFolder, "images");
        File masterfolder = new File(images, "master_testprocess_media");
        File mediafolder = new File(images, "testprocess_media");
        File sourceFolder = new File(images, "testprocess_source");
        masterfolder.mkdirs();
        mediafolder.mkdirs();
        sourceFolder.mkdirs();
        File ocr = new File(processFolder, "ocr");
        File altofolder = new File(ocr, "testprocess_alto");
        altofolder.mkdirs();
        String folder = System.getenv("junitdata");
        if (folder == null) {
            folder = "/opt/digiverso/junit/data/";
        }
        File metsTemplate = new File(folder + "metadata.xml");
        File metsFile = new File(processFolder, "meta.xml");
        FileUtils.copyFile(metsTemplate, metsFile);

        File masterfile = new File(masterfolder, "00000001.jp2");
        masterfile.createNewFile();
        File mediafile = new File(mediafolder, "00000001.jp2");
        mediafile.createNewFile();
        File altofile = new File(altofolder, "00000001.jp2");
        altofile.createNewFile();
        File sourcefile = new File(sourceFolder, "source");
        sourcefile.createNewFile();

        File exportFolder = new File(processFolder, "export");
        File export1 = new File(exportFolder, "testprocess_extension");
        File export2 = new File(exportFolder, "testprocess_extension.1");

        export1.mkdirs();
        export2.mkdirs();
        File data = new File(export1, "file.ext");
        data.createNewFile();
        String metadataFolder = processFolder.getParent();
        if (!metadataFolder.endsWith("/")) {
            metadataFolder = metadataFolder + "/";
        }
        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", metadataFolder);
    }

    @Test
    public void testExportDms() {
        ExportDms dms = new ExportDms();
        assertNotNull(dms);
    }

    @Test
    public void testExportDmsBoolean() {
        ExportDms dms = new ExportDms(false);
        assertNotNull(dms);
    }

    @Test
    public void testSetExportFulltext() {
        ExportDms dms = new ExportDms(false);
        dms.setExportFulltext(false);
        assertNotNull(dms);
    }

    @Test
    public void testFulltextDownload() throws SwapException, DAOException, IOException, InterruptedException {
        ExportDms dms = new ExportDms(false);
        dms.setExportFulltext(true);
        File dest = folder.newFolder("text");
        dest.mkdir();
        dms.fulltextDownload(testProcess, dest, testProcess.getTitel(), "qwertzu");
        assertNotNull(dest.list());
    }

    @Test
    public void testImageDownload() throws SwapException, DAOException, IOException, InterruptedException {
        ExportDms dms = new ExportDms(true);
        dms.setExportFulltext(true);
        File dest = folder.newFolder("images");
        dest.mkdir();
        dms.imageDownload(testProcess, dest, testProcess.getTitel(), "qwertzu");
        assertNotNull(dest.list());
    }

    @Test
    public void testStartExportProcessString() throws DocStructHasNoTypeException, PreferencesException, WriteException,
            MetadataTypeNotAllowedException, ExportFileException, UghHelperException, ReadException, SwapException, DAOException,
            TypeNotAllowedForParentException, IOException, InterruptedException {
        ExportDms dms = new ExportDms();
        dms.setExportFulltext(true);
        dms.startExport(testProcess);

    }
}
