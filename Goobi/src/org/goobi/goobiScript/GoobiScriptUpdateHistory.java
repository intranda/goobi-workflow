package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

@Deprecated
@Log4j2
public class GoobiScriptUpdateHistory extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

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
        UpdateHistoryThread et = new UpdateHistoryThread();
        et.start();
    }

    class UpdateHistoryThread extends Thread {
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
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    try {
                        boolean result = HistoryAnalyserJob.updateHistoryForProzess(p);
                        if (result) {
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "History updated using GoobiScript.", username);
                            log.info("History updated using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("History updated successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } else {
                            Helper.addMessageToProcessLog(p.getId(), LogType.ERROR, "History not successfully updated using GoobiScript.", username);
                            log.info("History could not be updated using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("History update was not successful.");
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                    } catch (Exception e) {
                        gsr.setResultMessage("History cannot be updated: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
