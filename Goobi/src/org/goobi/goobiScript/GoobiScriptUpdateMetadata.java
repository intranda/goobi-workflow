package org.goobi.goobiScript;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Deprecated
@Log4j
public class GoobiScriptUpdateMetadata extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        UpdateMetadataThread et = new UpdateMetadataThread();
        et.start();
    }

    class UpdateMetadataThread extends Thread {
        @Override
        public void run() {
            // execute all jobs that are still in waiting state
            synchronized (resultList) {
                for (GoobiScriptResult gsr : resultList) {
                    if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING) {
                        Process p = ProcessManager.getProcessById(gsr.getProcessId());
                        gsr.setProcessTitle(p.getTitel());
                        gsr.setResultType(GoobiScriptResultType.RUNNING);
                        gsr.updateTimestamp();
                        try {
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

                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Metadata updated using GoobiScript.", username);
                            log.info("Metadata updated using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("Metadata updated successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } catch (SwapException | DAOException | IOException | InterruptedException e1) {
                            log.error("Problem while updating the metadata using GoobiScript for process with id: " + p.getId(), e1);
                            gsr.setResultMessage("Error while updating metadata: " + e1.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(e1.getMessage());
                        }
                        gsr.updateTimestamp();
                    }
                }
            }

        }
    }
}
