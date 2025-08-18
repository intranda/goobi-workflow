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

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddStep extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String NUMBER = "order";

    @Override
    public String getAction() {
        return "addStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a new workflow step into the workflow.");
        addParameterToSampleCall(sb, STEPTITLE, "Scanning", "Title of the workflow step to add");
        addParameterToSampleCall(sb, NUMBER, "5", "This number defines where in the workflow this new step is ordered into. "
                + "Numerical values or the keyword \"end\" are permitted in order to insert the step at a specific position or at the end");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String wrongParameter = "Wrong number parameter";
        String steptitle = parameters.get(STEPTITLE);
        if (StringUtils.isBlank(steptitle)) {
            Helper.setFehlerMeldung(missingParameter, STEPTITLE);
            return new ArrayList<>();
        }

        String number = parameters.get(NUMBER);
        if (StringUtils.isBlank(number)) {
            Helper.setFehlerMeldung(missingParameter, NUMBER);
            return new ArrayList<>();
        }

        if (!StringUtils.isNumeric(number) && !"end".equals(number)) {
            Helper.setFehlerMeldung(wrongParameter, "(only numbers allowed)");
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
        Step s = new Step();
        s.setTitel(parameters.get(STEPTITLE));
        String number = parameters.get(NUMBER);
        int position = 0;
        if ("end".equals(number)) {
            position = p.getSchritte().get(p.getSchritteSize() - 1).getReihenfolge() + 1;
        } else {
            position = Integer.parseInt(number);
        }
        s.setReihenfolge(position);
        s.setProzess(p);
        if (p.getSchritte() == null) {
            p.setSchritte(new ArrayList<>());
        }
        p.getSchritte().add(s);
        String info = "'" + s.getTitel() + "' at position '" + s.getReihenfolge() + "'";
        try {
            ProcessManager.saveProcess(p);
            String message = "Added workflow step " + info + " to process";
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
            log.info(message + " using GoobiScript for process with ID " + p.getId());
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
