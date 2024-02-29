/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptOpenNextStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "openNextStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript opens the first locked step, if no open/inwork/error/inflight step exists.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        Step firstLockedStep = null;
        for (Step step : p.getSchritteList()) {
            switch (step.getBearbeitungsstatusEnum()) {
                case OPEN:
                case INFLIGHT:
                case INWORK:
                case ERROR:
                    // nothing to do, finish/skip
                    gsr.setResultMessage("open step found: " + step.getTitel());
                    gsr.setResultType(GoobiScriptResultType.OK);
                    return;
                case LOCKED:
                    if (firstLockedStep == null) {
                        firstLockedStep = step;
                    }
                    break;
                default:
                    break;
            }

        }

        if (firstLockedStep == null) {
            // no locked step found, finish/skip
            gsr.setResultMessage("No locked step found");
            gsr.setResultType(GoobiScriptResultType.OK);
            return;
        }

        // open step and save
        try {
            firstLockedStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
            // start script/plugin if task is automatic
            if (firstLockedStep.isTypAutomatisch()) {
                firstLockedStep.setBearbeitungsstatusEnum(StepStatus.INWORK);
                firstLockedStep.setEditTypeEnum(StepEditType.AUTOMATIC);
                StepManager.saveStep(firstLockedStep);
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(firstLockedStep);
                myThread.startOrPutToQueue();

            } else {
                firstLockedStep.setEditTypeEnum(StepEditType.ADMIN);
            }
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                    "Step opened using GoobiScript mass manipulation " + firstLockedStep.getTitel());
            ProcessManager.saveProcess(p);
            gsr.setResultMessage("Step opened: " + firstLockedStep.getTitel());
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            log.error(e);
            gsr.setResultMessage("Errow while opening first locked step: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();

    }

}
