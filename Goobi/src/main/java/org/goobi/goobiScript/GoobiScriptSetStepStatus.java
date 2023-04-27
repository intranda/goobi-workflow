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
import java.util.Iterator;
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
public class GoobiScriptSetStepStatus extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptField";
    private static final String STEPTITLE = "steptitle";
    private static final String STATUS = "status";

    private static final String STATUS_NUMBER_LOCKED = "0";
    private static final String STATUS_NUMBER_OPEN = "1";
    private static final String STATUS_NUMBER_IN_WORK = "2";
    private static final String STATUS_NUMBER_DONE = "3";
    private static final String STATUS_NUMBER_ERROR = "4";
    private static final String STATUS_NUMBER_DEACTIVATED = "5";

    private static final String STATUS_TEXT_LOCKED = "locked";
    private static final String STATUS_TEXT_OPEN = "open";
    private static final String STATUS_TEXT_IN_WORK = "in work";
    private static final String STATUS_TEXT_DONE = "done";
    private static final String STATUS_TEXT_ERROR = "error";
    private static final String STATUS_TEXT_DEACTIVATED = "deactivated";

    @Override
    public String getAction() {
        return "setStepStatus";
    }

    @Override
    public String getSampleCall() {
        StringBuilder options = new StringBuilder();
        options.append("`" + STATUS_NUMBER_LOCKED + "` (" + STATUS_TEXT_LOCKED + "), ");
        options.append("`" + STATUS_NUMBER_OPEN + "` (" + STATUS_TEXT_OPEN + "), ");
        options.append("`" + STATUS_NUMBER_IN_WORK + "` (" + STATUS_TEXT_IN_WORK + "), ");
        options.append("`" + STATUS_NUMBER_DONE + "` (" + STATUS_TEXT_DONE + "), ");
        options.append("`" + STATUS_NUMBER_ERROR + "` (" + STATUS_TEXT_ERROR + "), ");
        options.append("`" + STATUS_NUMBER_DEACTIVATED + "` (" + STATUS_TEXT_DEACTIVATED + ")");

        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to change the current status of a specific step in the workflow.");
        addParameterToSampleCall(sb, STEPTITLE, "Metadata enrichment", "Title of the workflow step to be changed");
        addParameterToSampleCall(sb, STATUS, "1", "Value of the status. Possible values are " + options.toString());
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || steptitle.equals("")) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, STEPTITLE);
            return new ArrayList<>();
        }

        String status = parameters.get(STATUS);
        if (status == null || status.equals("")) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, STATUS);
            return new ArrayList<>();
        }

        if (!status.equals("0") && !status.equals("1") && !status.equals("2") && !status.equals("3") && !status.equals("4") && !status.equals("5")) {
            StringBuilder options = new StringBuilder();
            options.append(STATUS_NUMBER_LOCKED + "=" + STATUS_TEXT_LOCKED + ", ");
            options.append(STATUS_NUMBER_OPEN + "=" + STATUS_TEXT_OPEN + ", ");
            options.append(STATUS_NUMBER_IN_WORK + "=" + STATUS_TEXT_IN_WORK + ", ");
            options.append(STATUS_NUMBER_DONE + "=" + STATUS_TEXT_DONE + ", ");
            options.append(STATUS_NUMBER_ERROR + "=" + STATUS_TEXT_ERROR + ", ");
            options.append(STATUS_NUMBER_DEACTIVATED + "=" + STATUS_TEXT_DEACTIVATED);
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, "Wrong status parameter: status ", "(possible: " + options.toString() + ")");
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
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        for (Iterator<Step> iterator = process.getSchritteList().iterator(); iterator.hasNext();) {
            Step step = iterator.next();
            if (step.getTitel().equals(gsr.getParameters().get(STEPTITLE))) {
                String oldStatusTitle = step.getBearbeitungsstatusEnum().getUntranslatedTitle();
                step.setBearbeitungsstatusAsString(gsr.getParameters().get(STATUS));
                try {
                    StepManager.saveStep(step);
                    String newStatusTitle = step.getBearbeitungsstatusEnum().getUntranslatedTitle();
                    String message = "Changed status of step '" + step.getTitel() + "' from' " + oldStatusTitle + " ' to '" + newStatusTitle
                            + "' using GoobiScript";
                    Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, message + ".", username);
                    log.info(message + " for process with ID " + process.getId());
                    gsr.setResultMessage("Status of the step is set successfully.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                } catch (DAOException e) {
                    log.error(GOOBI_SCRIPTFIELD + " Error while saving process: " + process.getTitel(), e);
                    gsr.setResultMessage("Error while changing the step status: " + e.getMessage());
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                    gsr.setErrorText(e.getMessage());
                }
                break;
            }
        }
        if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + gsr.getParameters().get(STEPTITLE));
        }
        gsr.updateTimestamp();
    }
}
