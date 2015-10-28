package de.sub.goobi.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Ruleset;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.NIOFileUtils;

public class MockProcess {

    private static final String RULESET_NAME = "ruleset.xml";

    public static Process createProcess(TemporaryFolder folder) throws Exception {

        String configFolder = System.getenv("junitdata");
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

        ConfigurationHelper.getInstance().setParameter("MetadatenVerzeichnis", folder.getRoot().toString() + FileSystems.getDefault().getSeparator());
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
        Path exportFolder = folder.newFolder("hotfolder").toPath();
        Files.createDirectories(exportFolder);
        project.setDmsImportImagesPath(exportFolder.toString() + FileSystems.getDefault().getSeparator());
        project.setDmsImportErrorPath(exportFolder.toString() + FileSystems.getDefault().getSeparator());
        project.setDmsImportSuccessPath(exportFolder.toString() + FileSystems.getDefault().getSeparator());
        project.setDmsImportRootPath(exportFolder.toString() + FileSystems.getDefault().getSeparator());
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

        Path configFolder = folder.newFolder("config").toPath();
        Files.createDirectories(configFolder);
        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        Path digitalCollectionTemplate = Paths.get(tempfolder + "goobi_digitalCollections.xml");
        Path digitalCollection = Paths.get(configFolder.toString(), "goobi_digitalCollections.xml");
        NIOFileUtils.copyFile(digitalCollectionTemplate, digitalCollection);

        Path projectsTemplate = Paths.get(tempfolder + "goobi_projects.xml");
        Path projects = Paths.get(configFolder.toString(), "goobi_projects.xml");
        NIOFileUtils.copyFile(projectsTemplate, projects);

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", configFolder.toString() + FileSystems.getDefault().getSeparator());
        ConfigurationHelper.getInstance().setParameter("localMessages", "/opt/digiverso/junit/data/");
    }

    private static void setUpRuleset(TemporaryFolder folder, Process testProcess) throws IOException, URISyntaxException {
        Path rulesetFolder = folder.newFolder("rulesets").toPath();
        Files.createDirectories(rulesetFolder);
        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        Path rulesetTemplate = Paths.get(tempfolder + RULESET_NAME);
        Path rulesetFile = Paths.get(rulesetFolder.toString(), RULESET_NAME);
        NIOFileUtils.copyFile(rulesetTemplate, rulesetFile);
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);
        ConfigurationHelper.getInstance().setParameter("RegelsaetzeVerzeichnis", rulesetFolder.toString() + FileSystems.getDefault().getSeparator());
        testProcess.setRegelsatz(ruleset);
    }

    private static void setUpProcessFolder(TemporaryFolder folder, Process testProcess) throws IOException, URISyntaxException {
        Path processFolder = folder.newFolder("1").toPath();
        Files.createDirectories(processFolder);
        Path images = Paths.get(processFolder.toString(), "images");

        Path masterfolder = Paths.get(images.toString(), "master_testprocess_media");
        Path mediafolder = Paths.get(images.toString(), "testprocess_media");
        Path sourceFolder = Paths.get(images.toString(), "testprocess_source");
        Files.createDirectories(masterfolder);
        Files.createDirectories(mediafolder);
        Files.createDirectories(sourceFolder);
        Path ocr = Paths.get(processFolder.toString(), "ocr");
        Path altofolder = Paths.get(ocr.toString(), "testprocess_alto");
        Files.createDirectories(altofolder);

        String tempfolder = System.getenv("junitdata");
        if (tempfolder == null) {
            tempfolder = "/opt/digiverso/junit/data/";
        }
        Path metsTemplate = Paths.get(tempfolder + "metadata.xml");

        Path metsFile = Paths.get(processFolder.toString(), "meta.xml");
        NIOFileUtils.copyFile(metsTemplate, metsFile);

        Path imageTemplate = Paths.get(tempfolder, "00000001.tif");

        Path masterfile = Paths.get(masterfolder.toString(), "00000001.tif");

        NIOFileUtils.copyFile(imageTemplate, masterfile);
        Path mediafile = Paths.get(mediafolder.toString(), "00000001.tif");
        NIOFileUtils.copyFile(imageTemplate, mediafile);
        Path altofile = Paths.get(altofolder.toString(), "00000001.xml");
        Files.createFile(altofile);
        Path sourcefile = Paths.get(sourceFolder.toString(), "source");
        Files.createFile(sourcefile);

        Path export = Paths.get(processFolder.toString(), "export");
        Files.createDirectories(export);
        Path exportFile = Paths.get(export.toString(), "junit.txt");
        Files.createFile(exportFile);

        Path subfolder = Paths.get(export.toString(), "testprocess_overview");
        Files.createDirectories(subfolder);
        Path fileInSubfolder = Paths.get(subfolder.toString(), "testprocess.xml");
        Files.createFile(fileInSubfolder);

    }
}
