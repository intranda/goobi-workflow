package de.sub.goobi.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Ruleset;

import de.sub.goobi.config.ConfigurationHelper;

public class MockProcess {

    private static final String RULESET_NAME = "ruleset.xml";

    public static Process createProcess() throws Exception {

        Process testProcess = new Process();
        testProcess.setTitel("testprocess");
        testProcess.setId(1);

        // set temporary ruleset
        setUpRuleset(testProcess);

        setUpProject(testProcess);

        setUpConfig();

        return testProcess;
    }

    private static void setUpConfig() {
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_SUFFIX", "media");
        ConfigurationHelper.getInstance().setParameter("DIRECTORY_PREFIX", "master");
        ConfigurationHelper.getInstance().setParameter("ExportFilesFromOptionalMetsFileGroups", "true");
    }

    private static void setUpProject(Process testProcess) throws IOException {
        Project project = new Project();
        project.setTitel("project");
        testProcess.setProjekt(project);
        project.setFileFormatInternal("Mets");
        project.setFileFormatDmsExport("Mets");

        Path exportFolder = Files.createTempDirectory("hotfolder");
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
        presentation.setFolder("");
        presentation.setId(1);
        presentation.setProject(project);

        ProjectFileGroup alto = new ProjectFileGroup();
        alto.setFolder("getOcrAltoDirectory");
        alto.setMimetype("text/xml");
        alto.setName("ALTO");
        alto.setPath("/opt/digiverso/viewer/alto/1/");
        alto.setSuffix("xml");
        alto.setProject(project);

        List<ProjectFileGroup> list = new ArrayList<>();
        list.add(presentation);
        list.add(alto);
        project.setFilegroups(list);

    }

    private static void setUpRuleset(Process testProcess) throws IOException, URISyntaxException {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);
        testProcess.setRegelsatz(ruleset);
    }




}
