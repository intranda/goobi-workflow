package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Deprecated
@Log4j
public class GoobiScriptCountImages extends AbstractIGoobiScript implements IGoobiScript {

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
        CountImagesThread et = new CountImagesThread();
        et.start();
    }

    class CountImagesThread extends Thread {

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

                    if (p.getSortHelperImages() == 0) {
                        int value = HistoryManager.getNumberOfImages(p.getId());
                        if (value > 0) {
                            ProcessManager.updateImages(value, p.getId());
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Image numbers counted using GoobiScript.", username);
                            log.info("Image numbers counted using GoobiScript for process with ID " + p.getId());
                        }
                    }
                    gsr.setResultMessage("Images counted successfully.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
