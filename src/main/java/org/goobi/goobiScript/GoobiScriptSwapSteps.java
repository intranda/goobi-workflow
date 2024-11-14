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
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSwapSteps extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String SWAP_1_NUMBER = "swap1nr";
    private static final String SWAP_1_TITLE = "swap1title";
    private static final String SWAP_2_NUMBER = "swap2nr";
    private static final String SWAP_2_TITLE = "swap2title";

    @Override
    public String getAction() {
        return "swapSteps";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to swap the order of two steps within a workflow.");
        addParameterToSampleCall(sb, SWAP_1_NUMBER, "4", "Order number of the first workflow step");
        addParameterToSampleCall(sb, SWAP_1_TITLE, "Quality assurance", "Title of the first workflow step");
        addParameterToSampleCall(sb, SWAP_2_NUMBER, "5", "Order number of the second workflow step");
        addParameterToSampleCall(sb, SWAP_2_TITLE, "Metadata enrichment", "Title of the second workflow step");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {

        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String swap1Number = parameters.get(SWAP_1_NUMBER);
        if (swap1Number == null || "".equals(swap1Number)) {
            Helper.setFehlerMeldung(missingParameter, SWAP_1_NUMBER);
            return new ArrayList<>();
        }

        String swap1Title = parameters.get(SWAP_1_TITLE);
        if (swap1Title == null || "".equals(swap1Title)) {
            Helper.setFehlerMeldung(missingParameter, SWAP_1_TITLE);
            return new ArrayList<>();
        }

        String swap2Number = parameters.get(SWAP_2_NUMBER);
        if (swap2Number == null || "".equals(swap2Number)) {
            Helper.setFehlerMeldung(missingParameter, SWAP_2_NUMBER);
            return new ArrayList<>();
        }

        String swap2Title = parameters.get(SWAP_2_TITLE);
        if (swap2Title == null || "".equals(swap2Title)) {
            Helper.setFehlerMeldung(missingParameter, SWAP_2_TITLE);
            return new ArrayList<>();
        }

        try {
            Integer.parseInt(swap1Number);
            Integer.parseInt(swap2Number);
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("Invalid order number used: ", swap1Number + " - " + swap2Number);
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
        int swap1nr = Integer.parseInt(parameters.get(SWAP_1_NUMBER));
        int swap2nr = Integer.parseInt(parameters.get(SWAP_2_NUMBER));
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        String swap1Title = parameters.get(SWAP_1_TITLE);
        String swap2Title = parameters.get(SWAP_2_TITLE);

        Step s1 = null;
        Step s2 = null;
        for (Step step : process.getSchritteList()) {
            if (step.getTitel().equals(swap1Title) && step.getReihenfolge().intValue() == swap1nr) {
                s1 = step;
            }
            if (step.getTitel().equals(swap2Title) && step.getReihenfolge().intValue() == swap2nr) {
                s2 = step;
            }
        }
        if (s1 != null && s2 != null) {
            StepStatus statustemp = s1.getBearbeitungsstatusEnum();
            s1.setBearbeitungsstatusEnum(s2.getBearbeitungsstatusEnum());
            s2.setBearbeitungsstatusEnum(statustemp);
            s1.setReihenfolge(Integer.valueOf(swap2nr));
            s2.setReihenfolge(Integer.valueOf(swap1nr));
            try {
                StepManager.saveStep(s1);
                StepManager.saveStep(s2);
                String message = "Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel() + "'";
                Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                log.info(message + " using GoobiScript for process with ID " + process.getId());
                gsr.setResultMessage(message + ".");
                gsr.setResultType(GoobiScriptResultType.OK);
            } catch (DAOException e) {
                String message = "Error on save while swapping steps: " + process.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel();
                log.error(message, e);
                gsr.setResultMessage(message + " error: " + e.getMessage());
                gsr.setResultType(GoobiScriptResultType.ERROR);
                gsr.setErrorText(e.getMessage());
            }
        }
        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
