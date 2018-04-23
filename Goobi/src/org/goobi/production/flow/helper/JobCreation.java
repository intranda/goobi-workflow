package org.goobi.production.flow.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
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
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class JobCreation {
    private static final Logger logger = Logger.getLogger(JobCreation.class);

    public static Process generateProcess(ImportObject io, Process vorlage) {
        String processTitle = io.getProcessTitle();
        logger.trace("processtitle is " + processTitle);
        String metsfilename = io.getMetsFilename();
        logger.trace("mets filename is " + metsfilename);
        String basepath = metsfilename.substring(0, metsfilename.length() - 4);
        logger.trace("basepath is " + basepath);
        Path metsfile = Paths.get(metsfilename);
        Process p = null;
        if (!testTitle(processTitle)) {
            logger.error("cannot create process, process title \"" + processTitle + "\" is already in use");
            // removing all data
            Path imagesFolder = Paths.get(basepath);
            if (Files.exists(imagesFolder) && Files.exists(imagesFolder)) {
                deleteDirectory(imagesFolder);
            } else {
                imagesFolder = Paths.get(basepath + "_" + ConfigurationHelper.getInstance().getMediaDirectorySuffix());
                if (Files.exists(imagesFolder) && Files.exists(imagesFolder)) {
                    deleteDirectory(imagesFolder);
                }
            }
            try {
                Files.delete(metsfile);
            } catch (Exception e) {
                logger.error("Can not delete file " + processTitle, e);
                return null;
            }
            Path anchor = Paths.get(basepath + "_anchor.xml");
            if (Files.exists(anchor)) {
                try {
                    Files.delete(anchor);
                } catch (IOException e) {
                    logger.error("Can not delete file " + processTitle, e);
                    return null;
                }
            }
            return null;
        }

        CopyProcess cp = new CopyProcess();
        cp.setProzessVorlage(vorlage);
        cp.metadataFile = metsfilename;
        cp.Prepare(io);
        cp.getProzessKopie().setTitel(processTitle);
        logger.trace("testing title");
        if (cp.testTitle()) {
            logger.trace("title is valid");
            cp.OpacAuswerten();
            try {
                p = cp.createProcess(io);
                if (p != null && p.getId() != null) {
                    moveFiles(metsfile, basepath, p);
                    List<Step> steps = StepManager.getStepsForProcess(p.getId());
                    for (Step s : steps) {
                        if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN) && s.isTypAutomatisch()) {
                            ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                            myThread.start();
                        }
                    }
                    Files.delete(Paths.get(io.getMetsFilename()));
                }
            } catch (ReadException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                logger.error(e);
            } catch (PreferencesException e) {
                Helper.setFehlerMeldung("Cannot read file " + processTitle, e);
                logger.error(e);
            } catch (SwapException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Cannot save process " + processTitle, e);
                logger.error(e);
            } catch (WriteException e) {
                Helper.setFehlerMeldung("Cannot write file " + processTitle, e);
                logger.error(e);
            } catch (IOException e) {
                Helper.setFehlerMeldung("Cannot write file " + processTitle, e);
                logger.error(e);
            } catch (InterruptedException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            }
        } else {
            logger.error("Title " + processTitle + " is invalid");
        }
        return p;
    }

    public static boolean testTitle(String titel) {
        if (titel != null) {
            int anzahl = 0;
            anzahl = ProcessManager.getNumberOfProcessesWithTitle(titel);
            if (anzahl > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static void moveFiles(Path metsfile, String basepath, Process p) throws SwapException, DAOException, IOException, InterruptedException {

        // new folder structure for process imports
        Path importFolder = Paths.get(basepath);
        if (Files.exists(importFolder) && Files.isDirectory(importFolder)) {
            List<Path> folderList = StorageProvider.getInstance().listFiles(basepath);
            for (Path directory : folderList) {
                Path destination = Paths.get(p.getProcessDataDirectory(), directory.getFileName().toString());
                if (Files.isDirectory(directory)) {
                    //                    if (!Files.exists(destination)) {
                    //                        Files.move(directory, destination);
                    //                    } else {
                    FileUtils.copyDirectory(directory.toFile(), destination.toFile());
                    //                        StorageProvider.getInstance().copyDirectory(directory, destination);
                    deleteDirectory(directory);
                    //                    }

                } else {
                    Files.move(directory, Paths.get(p.getProcessDataDirectory(), directory.getFileName().toString()));
                }
            }
            Files.deleteIfExists(importFolder);
        }

    }

    private static void deleteDirectory(Path directory) {
        StorageProvider.getInstance().deleteDir(directory);
    }
}
