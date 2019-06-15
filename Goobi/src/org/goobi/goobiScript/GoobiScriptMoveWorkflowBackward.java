package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptMoveWorkflowBackward extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        MoveWorkflowBackwardThread et = new MoveWorkflowBackwardThread();
        et.start();
    }

    class MoveWorkflowBackwardThread extends Thread {
        @Override
        public void run() {

            // execute all jobs that are still in waiting state
            ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    List<Step> tempList = new ArrayList<>(p.getSchritteList());
                    Collections.reverse(tempList);
                    for (Step step : tempList) {
                        if (step.getBearbeitungsstatusEnum() != StepStatus.LOCKED) {
                            step.setEditTypeEnum(StepEditType.ADMIN);
                            step.setBearbeitungszeitpunkt(new Date());
                            step.setBearbeitungsstatusDown();
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Status changed using GoobiScript mass manipulation for step " + step
                                    .getTitel());
                            break;
                        }
                    }
                    try {
                        ProcessManager.saveProcess(p);
                        gsr.setResultMessage("Workflow was moved forward successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (DAOException e) {
                        e.printStackTrace();
                        gsr.setResultMessage("Errow while moving the workflow forward: " +  e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
