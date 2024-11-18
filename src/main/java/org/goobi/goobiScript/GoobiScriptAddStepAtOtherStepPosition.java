/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddStepAtOtherStepPosition extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STRATEGY = "insertionstrategy";
    private static final String EXISTING_STEP_TITLE = "existingsteptitle";
    private static final String NEW_STEP_TITLE = "newsteptitle";
    private static final String STATE_BEFORE = "before";
    private static final String STATE_AFTER = "after";

    private static final int ERROR_NO_STEPS = -1;
    private static final int ERROR_MULTIPLE_STEPS = -2;

    @Override
    public String getAction() {
        return "addStepAtOtherStepPosition";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allows to add a new workflow step into the workflow before or after an other step (defined by name).");
        addParameterToSampleCall(sb, STRATEGY, STATE_AFTER, "The new step can be executed \"" + STATE_BEFORE + "\" or \"" + STATE_AFTER
                + "\" the existing step.");
        addParameterToSampleCall(sb, EXISTING_STEP_TITLE, "Scanning", "Title of the existing workflow step");
        addParameterToSampleCall(sb, NEW_STEP_TITLE, "Analyzing", "Title of the new workflow step");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // Check strategy
        String missingParameter = "Missing parameter: ";
        String wrongParameter = "Wrong parameter: ";
        String strategy = parameters.get(STRATEGY);
        if (strategy == null || "".equals(strategy)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, STRATEGY);
            return new ArrayList<>();
        }
        if (!STATE_BEFORE.equals(strategy) && !STATE_AFTER.equals(strategy)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, wrongParameter, STRATEGY);
            return new ArrayList<>();
        }

        // Check existing step title
        String existingStepTitle = parameters.get(EXISTING_STEP_TITLE);
        if (existingStepTitle == null || "".equals(existingStepTitle)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, EXISTING_STEP_TITLE);
            return new ArrayList<>();
        }

        // Check new step title
        String newStepTitle = parameters.get(NEW_STEP_TITLE);
        if (newStepTitle == null || "".equals(newStepTitle)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, NEW_STEP_TITLE);
            return new ArrayList<>();
        }

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
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

    /**
     * Returns an order for the new step next to the order of a given step. Depending on "before" or "after" the order can be increased by 1.<br />
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
        for (Step step : steps) {
            // Look for the given title
            if (step.getTitel().equals(parameters.get(EXISTING_STEP_TITLE))) {
                if (order == -1) {
                    // Index was found, but still look whole list for that case when there are multiple indices
                    order = step.getReihenfolge();
                    if (STATE_AFTER.equals(parameters.get(STRATEGY))) {
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
            for (Step step : steps) {
                s = step;
                if (s.getReihenfolge() >= order) {
                    s.setReihenfolge(s.getReihenfolge() + 1);
                }
            }
        }
        Step s = new Step();
        s.setTitel(parameters.get(NEW_STEP_TITLE));
        s.setReihenfolge(order);
        s.setProzess(p);
        if (p.getSchritte() == null) {
            p.setSchritte(new ArrayList<>());
        }
        p.getSchritte().add(s);
        String info = "'" + s.getTitel() + "' at position '" + s.getReihenfolge() + "'";
        try {
            ProcessManager.saveProcess(p);
            String message = "Added workflow step " + info;
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " to process using GoobiScript.", username);
            log.info(message + " to process using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage(message + ".");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            log.error(GOOBI_SCRIPTFIELD + "Error while saving process: " + p.getTitel(), e);
            gsr.setResultMessage("A problem occurred while adding a workflow step " + info + ": " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();
    }
}
