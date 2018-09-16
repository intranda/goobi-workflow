package org.goobi.goobiScript;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.DocStruct;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class GoobiScriptUpdateDatabaseCache extends AbstractIGoobiScript implements IGoobiScript {
    private static final Logger logger = Logger.getLogger(GoobiScriptUpdateDatabaseCache.class);

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        ReplaceSearchThread et = new ReplaceSearchThread();
        et.start();
    }

    class ReplaceSearchThread extends Thread {

        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        @Override
        public void run() {
            // execute all jobs that are still in waiting state
            ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();


                    try {
                        // write metadata into database
                        String metdatdaPath = p.getMetadataFilePath();
                        String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
                        Path metadataFile = Paths.get(metdatdaPath);
                        Path anchorFile = Paths.get(anchorPath);
                        Map<String, List<String>> pairs = new HashMap<>();

                        HelperSchritte.extractMetadata(metadataFile, pairs);

                        if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                            HelperSchritte.extractMetadata(anchorFile, pairs);
                        }

                        MetadataManager.updateMetadata(p.getId(), pairs);

                        // now add all authority fields to the metadata pairs
                        HelperSchritte.extractAuthorityMetadata(metadataFile, pairs);
                        if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                            HelperSchritte.extractAuthorityMetadata(anchorFile, pairs);
                        }
                        MetadataManager.updateJSONMetadata(p.getId(), pairs);

                        // calculate history entries
                        boolean result = HistoryAnalyserJob.updateHistoryForProzess(p);
                        if (!result) {
                            Helper.addMessageToProcessLog(p.getId(), LogType.ERROR, "History not successfully updated using GoobiScript.", username);
                            logger.info("History could not be updated using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("History update was not successful.");
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            continue;
                        }

                        // calculate number and size of images and metadata

                        if (p.getSortHelperImages() == 0) {
                            int value = HistoryManager.getNumberOfImages(p.getId());
                            if (value > 0) {
                                ProcessManager.updateImages(value, p.getId());
                            }
                        }

                        try {
                            DocStruct logical = p.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
                            p.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(logical, CountType.DOCSTRUCT));
                            p.setSortHelperMetadata(zaehlen.getNumberOfUghElements(logical, CountType.METADATA));
                        } catch (PreferencesException | ReadException | WriteException | IOException e) {
                            // metadata not readable or not found
                        }

                        if ( StorageProvider.getInstance().isFileExists(Paths.get(p.getImagesDirectory()))) {
                            p.setMediaFolderExists(true);
                        }

                        ProcessManager.saveProcess(p);

                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Database updated using GoobiScript.", username);
                        logger.info("Database updated using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage("Updated database cache successfully.");


                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (SwapException | DAOException | IOException | InterruptedException e1) {
                        logger.error("Problem while updating database using GoobiScript for process with id: " + p.getId(), e1);
                        gsr.setResultMessage("Error while updating database cache: " + e1.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                    }
                    gsr.updateTimestamp();
                }
            }
        }

    }

}
