package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Map;
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
public class GoobiScriptAddStepAtOtherStepPosition extends AbstractIGoobiScript implements IGoobiScript {

    private static final String ACTION = "addStepAtOtherStepPosition";
    private static final String PARAMETER_INSERTION_STRATEGY = "insertionstrategy";
    private static final String PARAMETER_EXISTING_STEP_TITLE = "existingsteptitle";
    private static final String PARAMETER_NEW_STEP_TITLE = "newsteptitle";
    private static final String STATE_BEFORE = "before";
    private static final String STATE_AFTER = "after";
    private static final int ERROR_NO_STEPS = -1;
    private static final int ERROR_MULTIPLE_STEPS = -2;

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a new workflow step into the workflow before or after an other step (defined by name).");
        addParameterToSampleCall(sb, PARAMETER_INSERTION_STRATEGY, "after", "The new step can be executed \"before\" or \"after\" the existing step.");
        addParameterToSampleCall(sb, PARAMETER_EXISTING_STEP_TITLE, "Scanning", "Title of the existing workflow step");
        addParameterToSampleCall(sb, PARAMETER_NEW_STEP_TITLE, "Analyzing", "Title of the new workflow step");
        return sb.toString();
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // Check strategy
        String strategy = parameters.get(PARAMETER_INSERTION_STRATEGY);
        if (strategy == null || strategy.equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", PARAMETER_INSERTION_STRATEGY);
            return false;
        }
        if (!strategy.equals(STATE_BEFORE) && !strategy.equals(STATE_AFTER)) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong parameter: ", PARAMETER_INSERTION_STRATEGY);
            return false;
        }

        // Check existing step title
        if (parameters.get(PARAMETER_EXISTING_STEP_TITLE) == null || parameters.get(PARAMETER_EXISTING_STEP_TITLE).equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", PARAMETER_EXISTING_STEP_TITLE);
            return false;
        }

        // Check new step title
        if (parameters.get(PARAMETER_NEW_STEP_TITLE) == null || parameters.get(PARAMETER_NEW_STEP_TITLE).equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", PARAMETER_NEW_STEP_TITLE);
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
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();

                    // Get order for new step
                    int order = this.getOrderForNewStep(p, parameters);

                    // In case of success insert the new step and move later steps
                    if (order >= 0) {
                        // Create new step on free position/order
                        this.createAndInsertNewStep(gsr, order, parameters);
                    } else {
                        // Case of error
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        if (order == ERROR_MULTIPLE_STEPS) {
                            gsr.setErrorText("Cannot insert step. There are multiple steps with fitting name.");
                        } else if (order == ERROR_NO_STEPS) {
                            gsr.setErrorText("Cannot insert step. There are no steps with fitting name.");
                        }
                    }
                }
            }
        }

        /**
         * Returns an order for the new step next to the order of a given step. Depending on "before" or "after" the order can be increased by
         * 1.<br />
         * Returns:<br />
         * order >= 0 when existing step is available and unique<br />
         * order == -1 when existing step isn't available<br />
         * order == -2 when existing step isn't unique<br />
         * 
         * @param p The current process
         * @param parameters The given parameters from the user executing this script
         * @return The found order or -1 or -2
         */
        private int getOrderForNewStep(Process p, Map<String, String> parameters) {
            List<Step> steps = p.getSchritte();
            int order = ERROR_NO_STEPS;
            for (int i = 0; i < steps.size(); i++) {
                // Look for the given title
                if (steps.get(i).getTitel().equals(parameters.get(PARAMETER_EXISTING_STEP_TITLE))) {
                    if (order == -1) {
                        // Index was found, but still look whole list for that case when there are multiple indices
                        order = steps.get(i).getReihenfolge();
                        if (parameters.get(PARAMETER_INSERTION_STRATEGY).equals(STATE_AFTER)) {
                            // order + 1 because new step should be inserted after found step
                            order++;
                        }
                    } else {
                        // When there are multiple steps with same name -> error
                        order = ERROR_MULTIPLE_STEPS;// Multiple steps found
                        break;
                    }
                }
            }
            return order;
        }

        /**
         * Creates a new step and inserts it at the given order. This works independent from inserting "before" or "after" an existing step.
         * 
         * @param gsr The current GoobiScriptResult object
         * @param order The order where to insert the new step
         * @param parameters The parameters given by the user executing this script
         */
        private void createAndInsertNewStep(GoobiScriptResult gsr, int order, Map<String, String> parameters) {
            Process p = ProcessManager.getProcessById(gsr.getProcessId());
            List<Step> steps = p.getSchritte();
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
            Step s = new Step();
            s.setTitel(parameters.get(PARAMETER_NEW_STEP_TITLE));
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
        }
    }
}
