package de.sub.goobi.export.download;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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

import de.sub.goobi.config.ConfigurationHelper;

public class ExportMetsTest {
 private static final String RULESET_NAME = "ruleset.xml";
    
    private Process testProcess = null;
    private File processFolder = null;
    
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, URISyntaxException {
        String configFolder = System.getenv("junitdata");;
        if (configFolder == null) {
            configFolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME =configFolder + "goobi_config.properties";
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
        File digitalCollection = new File (configFolder, "goobi_digitalCollections.xml");
        FileUtils.copyFile(digitalCollectionTemplate, digitalCollection);
        
        
        File projectsTemplate = new File(folder + "goobi_projects.xml");
        File projects = new File (configFolder, "goobi_projects.xml");
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
        File images = new File (processFolder, "images");
        
       
        File masterfolder = new File (images, "master_testprocess_media");
        File mediafolder = new File (images, "testprocess_media");
        File sourceFolder = new File(images, "testprocess_source");
        masterfolder.mkdirs();
        mediafolder.mkdirs();
        sourceFolder.mkdirs();
        File ocr = new File (processFolder, "ocr");
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
        
        File export = new File(processFolder, "export");
        export.mkdir();
        File exportFile = new File(export, "junit.txt");
        exportFile.createNewFile();
       
        File subfolder = new File (export, "testprocess_overview");
        subfolder.mkdir();
        File fileInSubfolder = new File (subfolder, "testprocess.xml");
        fileInSubfolder.createNewFile();
       
        
    }
    
    
    @Test
    public void testStartExport() throws Exception {
        File destination = folder.newFolder("export");
        destination.mkdirs();
        ExportMets exportMets = new ExportMets();
        assertNotNull(exportMets);
        exportMets.startExport(testProcess, destination.getAbsolutePath());
    }

}
