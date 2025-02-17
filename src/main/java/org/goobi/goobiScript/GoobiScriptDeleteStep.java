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
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptDeleteStep extends AbstractIGoobiScript implements IGoobiScript {

    private static final String STEPTITLE = "steptitle";

    @Override
    public String getAction() {
        return "deleteStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to delete an existing workflow step");
        addParameterToSampleCall(sb, STEPTITLE, "Image upload", "Define the name of the step that shall get deleted.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(missingParameter, STEPTITLE);
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
        // execute all jobs that are still in waiting state
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        if (process.getSchritte() == null) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("No steps available for process: " + process.getTitel());
            return;
        }

        List<Step> steps = process.getSchritte();
        String stepTitle = parameters.get(STEPTITLE);
        int removedSteps = 0;

        for (Step step : steps) {
            if (step.getTitel().equals(stepTitle)) {
                StepManager.deleteStep(step);
                removedSteps++;
            }
        }

        if (removedSteps > 0) {
            String howOften = "";
            if (removedSteps > 1) {
                howOften = " (" + removedSteps + "x)";
            }
            String message = "Deleted step '" + stepTitle + "'" + howOften + " from process";
            Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, message + "using GoobiScript.", username);
            log.info(message + "using GoobiScript for process with ID " + process.getId());
            gsr.setResultMessage(message + ".");
        } else {
            gsr.setResultMessage("Step not found: " + stepTitle);
        }
        gsr.setResultType(GoobiScriptResultType.OK);
        gsr.updateTimestamp();
    }
}
