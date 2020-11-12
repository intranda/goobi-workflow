package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddStepAfterOtherStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }
        
        if (parameters.get("insertiontitle") == null || parameters.get("insertiontitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "insertiontitle");
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
        AddStepThread et = new AddStepThread();
        et.start();
    }

    class AddStepThread extends Thread {
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
                    List<Step> steps = p.getSchritte();
                    int order = -1;
                    for (int i = 0; i < steps.size(); i++) {
                        // Look for the given title
                        if (steps.get(i).getTitel().equals(parameters.get("insertiontitle"))) {
                            if (order == -1) {
                                // Index was found, but still look whole list for that case when there are multiple indices
                                order = steps.get(i).getReihenfolge() + 1;// order + 1 because new step should be inserted after found step
                            } else {
                                // When there are multiple steps with same name -> error
                                order = -2;// Multiple steps
                                break;
                            }
                        }
                    }
                    // In case of success insert the new step and move later steps
                    if (order >= 0) {
                        // Move all later steps when the current position is already in use
                        // This is false, when there was a gap directly after the searched task
                        if (p.containsStepOfOrder(order)) {
                            Step s;
                            for (int i = 0; i < steps.size(); i++) {
                                s = steps.get(i);
                                if (s.getReihenfolge() >= order) {
                                    s.setReihenfolge(s.getReihenfolge() + 1);
                                }
                            }
                        }
                        // Create new step on free position/order
                        gsr.setResultType(GoobiScriptResultType.RUNNING);
                        gsr.updateTimestamp();
                        Step s = new Step();
                        s.setTitel(parameters.get("steptitle"));
                        s.setReihenfolge(order);
                        s.setProzess(p);
                        if (p.getSchritte() == null) {
                            p.setSchritte(new ArrayList<Step>());
                        }
                        p.getSchritte().add(s);
                        try {
                            ProcessManager.saveProcess(p);
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                    "Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript.",
                                    username);
                            log.info("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge()
                                    + "' to process using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "'.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } catch (DAOException e) {
                            log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                            gsr.setResultMessage("A problem occurred while adding a workflow step '" + s.getTitel() + "' at position '"
                                    + s.getReihenfolge() + "': " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(e.getMessage());
                        }
                        gsr.updateTimestamp();
                    } else {
                        // Case of error
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        if (order == -2) {
                            gsr.setErrorText("Cannot insert step. There are multiple steps with fitting name.");
                        } else if (order == -1) {
                            gsr.setErrorText("Cannot insert step. There are no steps with fitting name.");
                        }
                    }
                }
            }
        }
    }
}
