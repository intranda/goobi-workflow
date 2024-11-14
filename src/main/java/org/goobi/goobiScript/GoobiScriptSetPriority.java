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
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSetPriority extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String PRIORITY = "priority";

    private static final String PRIORITY_STANDARD = "standard";
    private static final String PRIORITY_HIGH = "high";
    private static final String PRIORITY_HIGHER = "higher";
    private static final String PRIORITY_HIGHEST = "highest";
    private static final String PRIORITY_CORRECTION = "correction";

    @Override
    public String getAction() {
        return "setPriority";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to define a priority to a specific workflow step.");
        addParameterToSampleCall(sb, STEPTITLE, "Scanning",
                "Title of the workflow step to be changed. Leave blank or remove this line to apply to all workflow steps.");
        addParameterToSampleCall(sb, PRIORITY, PRIORITY_HIGHER,
                "Priority to assign to the workflow step. Possible values are: `" + PRIORITY_STANDARD + "` `" + PRIORITY_HIGH + "` `"
                        + PRIORITY_HIGHER + "` `" + PRIORITY_HIGHEST + "` `" + PRIORITY_CORRECTION + "`");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String priority = parameters.get(PRIORITY);
        if (priority == null || "".equals(priority)) {
            Helper.setFehlerMeldung(missingParameter, PRIORITY);
            return new ArrayList<>();
        }

        String prio = priority.toLowerCase();
        if (!PRIORITY_STANDARD.equals(prio) && !PRIORITY_HIGH.equals(prio) && !PRIORITY_HIGHER.equals(prio) && !PRIORITY_HIGHEST.equals(prio)
                && !PRIORITY_CORRECTION.equals(prio)) {
            Helper.setFehlerMeldung("Wrong priority parameter",
                    "(only the following values are allowed: " + PRIORITY_STANDARD + ", " + PRIORITY_HIGH + ", " + PRIORITY_HIGHER + ", "
                            + PRIORITY_HIGHEST + ", " + PRIORITY_CORRECTION);
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

        String stepTitle = parameters.getOrDefault(STEPTITLE, null);
        if (stepTitle == null) {
            stepTitle = "";
        }

        int prio = 0;
        switch (parameters.get(PRIORITY).toLowerCase()) {
            case PRIORITY_HIGH:
                prio = 1;
                break;
            case PRIORITY_HIGHER:
                prio = 2;
                break;
            case PRIORITY_HIGHEST:
                prio = 3;
                break;
            case PRIORITY_CORRECTION:
                prio = 10;
                break;
            default:
                prio = 0;
        }

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        for (Step s : p.getSchritteList()) {
            if (stepTitle.length() == 0 || s.getTitel().equals(stepTitle)) {
                s.setPrioritaet(prio);
                try {
                    StepManager.saveStep(s);
                    String message = "Changed priority of step '" + s.getTitel() + "' to '" + s.getPrioritaet() + "'";
                    Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                    log.info(message + " using GoobiScript for process with ID " + p.getId());
                    gsr.setResultMessage(message + " successfully.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                } catch (DAOException e) {
                    log.error(GOOBI_SCRIPTFIELD + "Error while saving process: " + p.getTitel(), e);
                    gsr.setResultMessage("Error while changing the priority of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "'.");
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                    gsr.setErrorText(e.getMessage());
                }
            }
        }
        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + stepTitle);
        }
        gsr.updateTimestamp();
    }
}
