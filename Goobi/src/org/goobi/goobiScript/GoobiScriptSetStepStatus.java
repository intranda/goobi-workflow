package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptSetStepStatus extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("status") == null || parameters.get("status").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
            return false;
        }

        if (!parameters.get("status").equals("0") && !parameters.get("status").equals("1") && !parameters.get("status").equals("2")
                && !parameters.get("status").equals("3") && !parameters.get("status").equals("4") && !parameters.get("status").equals("5")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ",
                    "(possible: 0=closed, 1=open, 2=in work, 3=finished, 4=error, 5=deactivated");
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
        SetStepStatusThread et = new SetStepStatusThread();
        et.start();
    }

    class SetStepStatusThread extends Thread {
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

                    for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
                        Step s = iterator.next();
                        if (s.getTitel().equals(parameters.get("steptitle"))) {
                            s.setBearbeitungsstatusAsString(parameters.get("status"));
                            try {
                                StepManager.saveStep(s);
                                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Changed status of step '" + s.getTitel() + "' to '"
                                        + s.getBearbeitungsstatusEnum().getUntranslatedTitle() + "' using GoobiScript.", username);
                                log.info("Changed status of step '" + s.getTitel() + "' to '" + s.getBearbeitungsstatusEnum().getUntranslatedTitle()
                                        + "' using GoobiScript for process with ID " + p.getId());
                                gsr.setResultMessage("Status of the step is set successfully.");
                                gsr.setResultType(GoobiScriptResultType.OK);
                            } catch (DAOException e) {
                                log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                                gsr.setResultMessage("Error while changing the step status: " + e.getMessage());
                                gsr.setResultType(GoobiScriptResultType.ERROR);
                                gsr.setErrorText(e.getMessage());
                            }
                            break;
                        }
                    }
                    if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
                        gsr.setResultType(GoobiScriptResultType.OK);
                        gsr.setResultMessage("Step not found: " + parameters.get("steptitle"));
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
