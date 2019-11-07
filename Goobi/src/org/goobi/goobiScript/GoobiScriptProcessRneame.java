package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptProcessRneame extends AbstractIGoobiScript implements IGoobiScript {
    // action:renameProcess search:415121809 replace:1659235871 type:contains|full

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("search"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "search");
            return false;
        }
        if (StringUtils.isBlank(parameters.get("replace"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "replace");
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
        RenameProcessThread et = new RenameProcessThread();
        et.start();
    }

    class RenameProcessThread extends Thread {
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
            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    String processTitle = p.getTitel();
                    gsr.setProcessTitle(processTitle);
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    String type = parameters.get("type");
                    if (StringUtils.isBlank(type)) {
                        type = "contains";
                    }
                    String search = parameters.get("search");
                    String replace = parameters.get("replace");
                    boolean replacedTitle = false;
                    if ("contains".equals(type)) {
                        if (processTitle.contains(search)) {
                            processTitle = processTitle.replace(search, replace);
                            replacedTitle = true;
                        }
                    } else {
                        if (processTitle.equalsIgnoreCase(search)) {
                            processTitle = replace;
                            replacedTitle = true;
                        }
                    }

                    if (replacedTitle) {
                        log.info("Proces title changed using GoobiScript for process with ID " + p.getId());
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                "Process title changed from '" + p.getTitel() + " to  '" + processTitle + "' using GoobiScript.", username);
                        p.changeProcessTitle(processTitle);
                        gsr.setProcessTitle(processTitle);
                        ProcessManager.saveProcessInformation(p);
                        gsr.setResultMessage("Process title changed successfully.");
                    } else {
                        gsr.setResultMessage("Process title did not match.");
                    }

                    gsr.setResultType(GoobiScriptResultType.OK);

                    gsr.updateTimestamp();
                }
            }
        }

    }

}
