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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
public class GoobiScriptRenameStep extends AbstractIGoobiScript implements IGoobiScript {
    // action:renameProcess search:415121809 replace:1659235871 type:contains|full

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptField";
    private static final String OLD_STEP_NAME = "oldStepName";
    private static final String NEW_STEP_NAME = "newStepName";

    @Override
    public String getAction() {
        return "renameStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to rename an existing workflow step.");
        addParameterToSampleCall(sb, OLD_STEP_NAME, "Scanning", "Define the current title of the workflow step here.");
        addParameterToSampleCall(sb, NEW_STEP_NAME, "Image upload", "Define how the title of the workflow step shall be from now on.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        if (StringUtils.isBlank(parameters.get(OLD_STEP_NAME))) {
            Helper.setFehlerMeldungUntranslated(GOOBI_SCRIPTFIELD, missingParameter, OLD_STEP_NAME);
            return Collections.emptyList();
        }

        if (StringUtils.isBlank(parameters.get(NEW_STEP_NAME))) {
            Helper.setFehlerMeldungUntranslated(GOOBI_SCRIPTFIELD, missingParameter, NEW_STEP_NAME);
            return Collections.emptyList();
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
        String processTitle = p.getTitel();
        gsr.setProcessTitle(processTitle);
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        String oldStepName = parameters.get(OLD_STEP_NAME);
        String newStepName = parameters.get(NEW_STEP_NAME);
        for (Step step : p.getSchritte()) {
            if (step.getTitel().equalsIgnoreCase(oldStepName)) {
                step.setTitel(newStepName);
                try {
                    StepManager.saveStep(step);
                } catch (DAOException e) {
                    log.error(e);
                }

                log.info("Step title changed using GoobiScript for process with ID " + p.getId());
                Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                        "Step title changed '" + oldStepName + " to  '" + newStepName + "' using GoobiScript.", username);
                gsr.setResultMessage("Step title changed successfully.");
            }
        }
        gsr.setResultType(GoobiScriptResultType.OK);

        gsr.updateTimestamp();
    }

}
