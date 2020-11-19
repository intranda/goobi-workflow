package org.goobi.goobiScript;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptDeleteProcess extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("contentOnly") == null || parameters.get("contentOnly").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "contentOnly");
            return false;
        }

        if (!parameters.get("contentOnly").equals("true") && !parameters.get("contentOnly").equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "wrong parameter 'contentOnly'; possible values: true, false");
            return false;
        }

        // add all valid commands to list
        ImmutableList.Builder<GoobiScriptResult> newList = ImmutableList.<GoobiScriptResult> builder().addAll(gsm.getGoobiScriptResults());
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            newList.add(gsr);
        }
        gsm.setGoobiScriptResults(newList.build());

        return true;
    }

    @Override
    public void execute() {
        TEMPLATEThread et = new TEMPLATEThread();
        et.start();
    }

    class TEMPLATEThread extends Thread {
        @Override
        public void run() {

            // wait until there is no earlier script to be executed first
            while (gsm.getAreEarlierScriptsWaiting(starttime)) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Problem while waiting for running GoobiScripts", e);
                }
            }

            boolean contentOnly = Boolean.parseBoolean(parameters.get("contentOnly"));
            boolean removeUnknownFiles =
                    StringUtils.isBlank(parameters.get("removeUnknownFiles")) ? false : Boolean.parseBoolean(parameters.get("removeUnknownFiles"));

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    try {
                        if (this.checkDeletePermission(p, contentOnly)) {
                            if (contentOnly && removeUnknownFiles) {
                                List<Path> dataInProcessFolder = StorageProvider.getInstance().listFiles(p.getProcessDataDirectory());
                                for (Path path : dataInProcessFolder) {
                                    // keep the mets file, but delete everything else
                                    if (!path.getFileName().toString().matches("meta.*xml.*")) {
                                        StorageProvider.getInstance().deleteDir(path);
                                    }
                                }
                            } else if (contentOnly) {
                                Path ocr = Paths.get(p.getOcrDirectory());
                                if (StorageProvider.getInstance().isFileExists(ocr)) {
                                    StorageProvider.getInstance().deleteDir(ocr);
                                }
                                Path images = Paths.get(p.getImagesDirectory());
                                if (StorageProvider.getInstance().isFileExists(images)) {
                                    StorageProvider.getInstance().deleteDir(images);
                                }
                            } else {
                                StorageProvider.getInstance().deleteDir(Paths.get(p.getProcessDataDirectory()));
                                ProcessManager.deleteProcess(p);
                            }
                        } else {
                            throw new Exception("Missing delete permission. No files were deleted.");
                        }
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Content deleted using GoobiScript.", username);
                        log.info("Content deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process deleted successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e) {
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                "Problem occured while trying to delete content using GoobiScript.", username);
                        log.error("Content for process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process cannot be deleted: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                    gsr.updateTimestamp();
                }
            }
        }

        public boolean checkDeletePermission(Process p, boolean contentOnly) throws DAOException, SwapException, InterruptedException, IOException {
            Path path = Paths.get(p.getProcessDataDirectory());
            Path parent = path.getParent();
            boolean permission;
            if (contentOnly) {
                permission = StorageProvider.getInstance().isDeletable(path);
            } else {
                permission = StorageProvider.getInstance().isDeletable(parent);
            }
            return permission;
        }
    }
}
