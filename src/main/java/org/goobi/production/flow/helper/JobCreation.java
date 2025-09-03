package org.goobi.production.flow.helper;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.importer.ImportObject;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@Log4j2
public final class JobCreation {

    private JobCreation() {
        // hide implicit public constructor
    }

    public static Process generateProcess(ImportObject io, Process vorlage) {
        String processTitle = io.getProcessTitle();
        log.trace("processtitle is " + processTitle);
        String metsfilename = io.getMetsFilename();
        String metsAnchorName = metsfilename.replace(".xml", "_anchor.xml");
        log.trace("mets filename is " + metsfilename);
        String basepath = metsfilename.substring(0, metsfilename.length() - 4);
        log.trace("basepath is " + basepath);
        Path metsfile = Paths.get(metsfilename);

        Path metsAnchorFile = Paths.get(metsAnchorName);

        Process p = null;
        if (!testTitle(processTitle)) {
            log.error("cannot create process, process title \"" + processTitle + "\" is already in use");
            // removing all data
            Path imagesFolder = Paths.get(basepath);
            if (StorageProvider.getInstance().isFileExists(imagesFolder)) {
                deleteDirectory(imagesFolder);
            } else {
                String folderRule = ConfigurationHelper.getInstance().getProcessImagesMainDirectoryName();
                folderRule = folderRule.replace("{processtitle}", basepath);
                imagesFolder = Paths.get(folderRule);
                if (StorageProvider.getInstance().isFileExists(imagesFolder)) {
                    deleteDirectory(imagesFolder);
                }
            }
            StorageProvider.getInstance().deleteDir(metsfile);
            if (StorageProvider.getInstance().isFileExists(metsAnchorFile)) {
                StorageProvider.getInstance().deleteDir(metsAnchorFile);
            }

            Path anchor = Paths.get(basepath + "_anchor.xml");
            if (StorageProvider.getInstance().isFileExists(anchor)) {
                StorageProvider.getInstance().deleteDir(anchor);
            }
            return null;
        }

        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(vorlage);
        cp.setMetadataFile(metsfilename);
        cp.prepare(io);
        cp.getProzessKopie().setTitel(processTitle);
        log.trace("testing title");
        if (cp.testTitle()) {
            log.trace("title is valid");
            cp.opacAuswerten();
            try {
                p = cp.createProcess(io);
                if (p != null && p.getId() != null) {
                    moveFiles(basepath, p);
                    List<Step> steps = StepManager.getStepsForProcess(p.getId());
                    for (Step s : steps) {
                        if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum()) && s.isTypAutomatisch()) {
                            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                            myThread.startOrPutToQueue();
                        }
                    }
                    if (StorageProvider.getInstance().isFileExists(metsAnchorFile)) {
                        StorageProvider.getInstance().deleteDir(metsAnchorFile);
                    }
                    StorageProvider.getInstance().deleteDir(metsfile);
                }
            } catch (ReadException | PreferencesException | SwapException | WriteException | IOException | DAOException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                log.error(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else {
            log.error("Title " + processTitle + " is invalid");
        }
        return p;
    }

    /**
     * This method checks if a given (new) process title already exists as process inside of the Goobi database. It return 'false' if the title is
     * already in use
     * 
     * @param title the new process title that shall be tested
     * @return true if the title is still not used; false if the title is already in use
     */
    public static boolean testTitle(String title) {
        if (title != null) {
            int anzahl = 0;
            anzahl = ProcessManager.getNumberOfProcessesWithTitle(title);
            if (anzahl > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static void moveFiles(String basepath, Process p) throws IOException, SwapException {

        // new folder structure for process imports
        Path importFolder = Paths.get(basepath);
        if (StorageProvider.getInstance().isFileExists(importFolder) && StorageProvider.getInstance().isDirectory(importFolder)) {
            if (ConfigurationHelper.getInstance().useS3()) {
                String rootFolderName = p.getProcessDataDirectory();
                List<Path> filesToUpload = new ArrayList<>();

                try (Stream<Path> input = Files.find(importFolder, 3, (path, file) -> file.isRegularFile())) {
                    input.forEach(filesToUpload::add);
                }

                for (Path file : filesToUpload) {
                    Path destination = Paths.get(file.toString().replace(importFolder.toString(), rootFolderName));
                    StorageProvider.getInstance().move(file, destination);
                }
            } else {
                List<Path> folderList = StorageProvider.getInstance().listFiles(basepath);
                for (Path directory : folderList) {
                    Path destination = Paths.get(p.getProcessDataDirectory(), directory.getFileName().toString());
                    if (StorageProvider.getInstance().isDirectory(directory)) {
                        FileUtils.copyDirectory(directory.toFile(), destination.toFile());
                        deleteDirectory(directory);

                    } else {
                        StorageProvider.getInstance().move(directory, Paths.get(p.getProcessDataDirectory(), directory.getFileName().toString()));
                    }
                }
            }
            StorageProvider.getInstance().deleteDir(importFolder);
        }
    }

    private static void deleteDirectory(Path directory) {
        StorageProvider.getInstance().deleteDir(directory);
    }
}
