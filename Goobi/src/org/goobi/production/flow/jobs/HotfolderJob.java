package org.goobi.production.flow.jobs;

/**
 * This File is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.goobi.org
 *          - http://launchpad.net/goobi-production
 *          - http://gdz.sub.uni-goettingen.de
 *          - http://www.intranda.com
 *          - http://digiverso.com 
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.CopyProcess;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.GoobiHotfolder;
import org.goobi.production.importer.ImportObject;

import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

/**
 * 
 * @author Robert Sehr
 * 
 */

@Deprecated
public class HotfolderJob extends AbstractGoobiJob {
    private static final Logger logger = Logger.getLogger(HotfolderJob.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
     */
    @Override
    public String getJobName() {
        return "HotfolderJob";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#execute()
     */
    @Override
    public void execute() {
        // logger.error("TEST123");
        if (ConfigurationHelper.getInstance().isRunHotfolder()) {
            //            logger.trace("1");
            List<GoobiHotfolder> hotlist = GoobiHotfolder.getInstances();
            //            logger.trace("2");
            for (GoobiHotfolder hotfolder : hotlist) {
                //                logger.trace("3");
                List<Path> list = hotfolder.getCurrentFiles();
                //                logger.trace("4");
                long size = 0;
                try {
                    size = getSize(list);
                } catch (IOException e1) {
                    logger.error(e1);
                }
                //                logger.trace("5");
                try {
                    if (size > 0) {
                        if (!hotfolder.isLocked()) {

                            //                            logger.trace("6");
                            Thread.sleep(10000);
                            //                            logger.trace("7");
                            list = hotfolder.getCurrentFiles();
                            //                            logger.trace("8");
                            if (size == getSize(list)) {
                                hotfolder.lock();
                                //                                logger.trace("9");
                                Process template = ProcessManager.getProcessById(hotfolder.getTemplate());
                                //                              dao.refresh(template);
                                //                                logger.trace("10");
                                List<String> metsfiles = hotfolder.getFileNamesByFilter(GoobiHotfolder.filter);
                                //                                logger.trace("11");
                                HashMap<String, Integer> failedData = new HashMap<String, Integer>();
                                //                                logger.trace("12");

                                for (String filename : metsfiles) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("found file: " + filename);
                                    }
                                    //                                    logger.trace("13");

                                    int returnValue =
                                            generateProcess(filename, template, hotfolder.getFolderAsFile(), hotfolder.getCollection(), hotfolder
                                                    .getUpdateStrategy());
                                    //                                    logger.trace("14");
                                    if (returnValue != 0) {
                                        //                                        logger.trace("15");
                                        failedData.put(filename, returnValue);
                                        //                                        logger.trace("16");
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug("finished file: " + filename);
                                        }
                                    }
                                }
                                if (!failedData.isEmpty()) {
                                    // // TODO Errorhandling
                                    //                                    logger.trace("17");
                                    for (String filename : failedData.keySet()) {
                                        Path oldFile = Paths.get(hotfolder.getFolderAsFile().toString(), filename);
                                        if (Files.exists(oldFile)) {
                                            Path newFile = Paths.get(oldFile.toString() + "_");
                                           Files.move(oldFile, newFile);
                                        }
                                        logger.error("error while importing file: " + filename + " with error code " + failedData.get(filename));
                                    }
                                }
                                hotfolder.unlock();
                            }
                        } else {
                            //                            logger.trace("18");
                            return;
                        }
                        //                        logger.trace("19");
                    }

                } catch (InterruptedException e) {
                    logger.error(e);
                    //                    logger.trace("20");
                    //              } catch (DAOException e) {
                    //                  logger.error(e);
                    //                  logger.trace("21");
                } catch (Exception e) {
                    logger.error(e);
                }
            }

        }
    }

    private long getSize(List<Path> list) throws IOException {
        long size = 0;
        for (Path f : list) {
            if (Files.isDirectory(f)) {
                List<Path> subdir = NIOFileUtils.listFiles(f.toString());
                for (Path sub : subdir) {
                    size += Files.size(sub);
                }
            } else {
                size += Files.size(f);
            }
        }
        return size;
    }

    public static int generateProcess(String processTitle, Process vorlage, Path dir, String digitalCollection, String updateStrategy) {
        // wenn keine anchor Datei, dann Vorgang anlegen
        if (!processTitle.contains("anchor") && processTitle.endsWith("xml")) {
            if (!updateStrategy.equals("ignore")) {
                boolean test = testTitle(processTitle.substring(0, processTitle.length() - 4));
                if (!test && updateStrategy.equals("error")) {
                    Path images =
                            Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + FileSystems.getDefault().getSeparator());
                    if (Files.isDirectory(images)) {
                        NIOFileUtils.deleteDir(images);
                    }
                    try {
                        NIOFileUtils.deleteDir(Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle));
                    } catch (Exception e) {
                        logger.error("Can not delete Path " + processTitle, e);
                        return 30;
                    }
                    Path anchor =
                            Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (Files.exists(anchor)) {
                        NIOFileUtils.deleteDir(anchor);
                    }
                    return 27;
                } else if (!test && updateStrategy.equals("update")) {
                    // TODO UPDATE mets data
                    Path images =
                            Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + FileSystems.getDefault().getSeparator());
                    if (Files.isDirectory(images)) {
                        NIOFileUtils.deleteDir(images);
                    }
                    try {
                        NIOFileUtils.deleteDir(Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle));
                    } catch (Exception e) {
                        logger.error("Can not delete Path " + processTitle, e);
                        return 30;
                    }
                    Path anchor =
                            Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + "_anchor.xml");
                    if (Files.exists(anchor)) {
                        try {
                            Files.delete(anchor);
                        } catch (IOException e) {
                            logger.error(e);
                        }
                    }
                    return 28;
                }
            }
            CopyProcess form = new CopyProcess();
            form.setProzessVorlage(vorlage);
            form.metadataFile = dir.toString() + FileSystems.getDefault().getSeparator() + processTitle;
            form.Prepare();
            form.getProzessKopie().setTitel(processTitle.substring(0, processTitle.length() - 4));
            if (form.testTitle()) {
                if (digitalCollection == null) {
                    List<String> collections = new ArrayList<String>();
                    form.setDigitalCollections(collections);
                } else {
                    List<String> col = new ArrayList<String>();
                    col.add(digitalCollection);
                    form.setDigitalCollections(col);
                }
                form.OpacAuswerten();

                try {
                    Process p = form.NeuenProzessAnlegen2();
                    if (p.getId() != null) {

                        // copy image files to new directory
                        Path images =
                                Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4)
                                        + FileSystems.getDefault().getSeparator());
                        if (Files.isDirectory(images)) {
                            List<String> imageDir = NIOFileUtils.list(images.toString());

                            for (String file : imageDir) {
                                Path image = Paths.get(images.toString(), file);
                                Path dest = Paths.get(p.getImagesOrigDirectory(false) + image.getFileName());
                                Files.move(image, dest);
                            }
                            NIOFileUtils.deleteDir(images);
                        }

                        // copy fulltext files

                        Path fulltext =
                                Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + "_txt"
                                        + FileSystems.getDefault().getSeparator());
                        if (Files.isDirectory(fulltext)) {

                            Files.move(fulltext, Paths.get(p.getTxtDirectory()), NIOFileUtils.STANDARD_COPY_OPTIONS);
                        }

                        // copy source files

                        Path sourceDir =
                                Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4) + "_src"
                                        + FileSystems.getDefault().getSeparator());
                        if (Files.isDirectory(sourceDir)) {
                            Files.move(sourceDir, Paths.get(p.getImportDirectory()), NIOFileUtils.STANDARD_COPY_OPTIONS);
                        }

                        try {
                            NIOFileUtils.deleteDir(Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle));
                        } catch (Exception e) {
                            logger.error("Can not delete Path " + processTitle + " after importing " + p.getTitel() + " into goobi", e);
                            return 30;
                        }
                        Path anchor =
                                Paths.get(dir.toString() + FileSystems.getDefault().getSeparator() + processTitle.substring(0, processTitle.length() - 4)
                                        + "_anchor.xml");
                        if (Files.exists(anchor)) {
                            Files.delete(anchor);
                        }
                        List<Step> steps = StepManager.getStepsForProcess(p.getId());
                        for (Step s : steps) {
                            if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN) && s.isTypAutomatisch()) {
                                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                                myThread.start();
                            }
                        }
                    }
                } catch (ReadException e) {
                    logger.error(e);
                    return 20;
                } catch (PreferencesException e) {
                    logger.error(e);
                    return 21;
                } catch (SwapException e) {
                    logger.error(e);
                    return 22;
                } catch (DAOException e) {
                    logger.error(e);
                    return 22;
                } catch (WriteException e) {
                    logger.error(e);
                    return 23;
                } catch (IOException e) {
                    logger.error(e);
                    return 24;
                } catch (InterruptedException e) {
                    logger.error(e);
                    return 25;
                }
            }
            // TODO updateImagePath aufrufen

            return 0;
        } else {
            return 26;
        }
    }

    public static boolean testTitle(String titel) {
        if (titel != null) {
            long anzahl = 0;
            //          try {
            anzahl = ProcessManager.countProcessTitle(titel);
            //          } catch (DAOException e) {
            //              return false;
            //          }
            if (anzahl > 0) {
                Helper.setFehlerMeldung("processTitleAllreadyInUse");
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @SuppressWarnings("static-access")
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
            logger.trace("wrong title");
            // removing all data
            Path imagesFolder = Paths.get(basepath);
            if (Files.exists(imagesFolder) && Files.isDirectory(imagesFolder)) {
                NIOFileUtils.deleteDir(imagesFolder);
            } else {
                imagesFolder = Paths.get(basepath + "_" + vorlage.DIRECTORY_SUFFIX);
                if (Files.exists(imagesFolder) && Files.isDirectory(imagesFolder)) {
                    NIOFileUtils.deleteDir(imagesFolder);
                }
            }
            try {
                Files.delete(metsfile);
            } catch (Exception e) {
                logger.error("Can not delete Path " + processTitle, e);
                return null;
            }
            Path anchor = Paths.get(basepath + "_anchor.xml");
            if (Files.exists(anchor)) {
                try {
                    Files.delete(anchor);
                } catch (IOException e) {
                    logger.error(e);
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
                JobCreation.moveFiles(metsfile, basepath, p);

            } catch (ReadException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (PreferencesException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (SwapException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (DAOException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (WriteException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (IOException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            } catch (InterruptedException e) {
                Helper.setFehlerMeldung(e);
                logger.error(e);
            }
        } else {
            logger.trace("title is invalid");
        }
        return p;

    }

}
