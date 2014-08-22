package de.sub.goobi.mock;

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
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;

public class MockProcess {

    private static final String RULESET_NAME = "ruleset.xml";

    public static Process createProcess(TemporaryFolder folder) throws Exception {

        String configFolder = System.getenv("junitdata");
        ;
        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = configFolder + "goobi_config.properties";
        Process testProcess = new Process();
        testProcess.setTitel("testprocess");
        testProcess.setId(1);

        // set temporary ruleset
        setUpRuleset(folder, testProcess);

        // set temporary process infrastructure
        setUpProcessFolder(folder, testProcess);

        setUpProject(folder, testProcess);

        setUpConfig(folder);

        return testProcess;
    }

    private static void setUpConfig(TemporaryFolder folder) {

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("ExportFilesFromOptionalMetsFileGroups", "true");
    }

    private static void setUpProject(TemporaryFolder folder, Process testProcess) throws IOException {
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
        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        File digitalCollectionTemplate = new File(tempfolder + "goobi_digitalCollections.xml");
        File digitalCollection = new File(configFolder, "goobi_digitalCollections.xml");
        FileUtils.copyFile(digitalCollectionTemplate, digitalCollection);

        File projectsTemplate = new File(tempfolder + "goobi_projects.xml");
        File projects = new File(configFolder, "goobi_projects.xml");
        FileUtils.copyFile(projectsTemplate, projects);

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", configFolder.getAbsolutePath() + File.separator);
        ConfigurationHelper.getInstance().setParameter("localMessages", "/opt/digiverso/junit/data/");
    }

    private static void setUpRuleset(TemporaryFolder folder, Process testProcess) throws IOException, URISyntaxException {
        File rulesetFolder = folder.newFolder("rulesets");
        rulesetFolder.mkdir();
        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        File rulesetTemplate = new File(tempfolder + RULESET_NAME);
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

    private static void setUpProcessFolder(TemporaryFolder folder, Process testProcess) throws IOException, URISyntaxException {
        File processFolder = folder.newFolder("1");
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

        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        File metsTemplate = new File(tempfolder + "metadata.xml");

        File metsFile = new File(processFolder, "meta.xml");
        FileUtils.copyFile(metsTemplate, metsFile);

        File imageTemplate = new File(tempfolder, "00000001.tif");
        
        File masterfile = new File(masterfolder, "00000001.tif");
        
        FileUtils.copyFile(imageTemplate, masterfile);
        File mediafile = new File(mediafolder, "00000001.tif");
        FileUtils.copyFile(imageTemplate, mediafile);
        File altofile = new File(altofolder, "00000001.xml");
        altofile.createNewFile();
        File sourcefile = new File(sourceFolder, "source");
        sourcefile.createNewFile();

        File export = new File(processFolder, "export");
        export.mkdir();
        File exportFile = new File(export, "junit.txt");
        exportFile.createNewFile();

        File subfolder = new File(export, "testprocess_overview");
        subfolder.mkdir();
        File fileInSubfolder = new File(subfolder, "testprocess.xml");
        fileInSubfolder.createNewFile();

    }
}
