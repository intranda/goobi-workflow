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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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

    public static void setUpProject(Process testProcess) throws IOException {
        Project project = new Project();
        project.setTitel("project");
        project.setId(666);
        project.setProjectIdentifier("identifier");
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

    public static void setUpRuleset(Process testProcess) throws IOException, URISyntaxException {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(11111);
        ruleset.setOrderMetadataByRuleset(true);
        ruleset.setTitel(RULESET_NAME);
        ruleset.setDatei(RULESET_NAME);
        testProcess.setRegelsatz(ruleset);
    }

}
