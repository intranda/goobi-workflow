package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptRenameStep extends AbstractIGoobiScript implements IGoobiScript {
    // action:renameProcess search:415121809 replace:1659235871 type:contains|full

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewAction(sb, "renameStep", "comment");
        addParameter(sb, "oldStepName", "def", "ghi");
        addParameter(sb, "newStepName", "def", "ghi");
        return sb.toString();

//        return "---\\naction: renameStep\\noldStepName: OLDNAME\\nnewStepName: NEWNAME";
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("oldStepName"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "oldStepName");
            return false;
        }
        if (StringUtils.isBlank(parameters.get("newStepName"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "newStepName");
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
        RenameStepThread et = new RenameStepThread();
        et.start();
    }

    class RenameStepThread extends Thread {
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

                    String oldStepName = parameters.get("oldStepName");
                    String newStepName = parameters.get("newStepName");
                    for (Step step : p.getSchritte()) {
                        if (step.getTitel().equalsIgnoreCase(oldStepName)) {
                            step.setTitel(newStepName);
                            try {
                                StepManager.saveStep(step);
                            } catch (DAOException e) {
                                log.error(e);
                            }

                            log.info("Step title changed using GoobiScript for process with ID " + p.getId());
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                    "Step title changed '" + oldStepName + " to  '" + newStepName + "' using GoobiScript.", username);
                            gsr.setResultMessage("Step title changed successfully.");
                        }
                    }
                    gsr.setResultType(GoobiScriptResultType.OK);

                    gsr.updateTimestamp();
                }
            }
        }

    }

}
