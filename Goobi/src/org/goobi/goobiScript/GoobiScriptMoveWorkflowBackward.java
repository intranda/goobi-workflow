package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptMoveWorkflowBackward extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "moveWorkflowBackward";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript moves the progress of the workflow backward.");
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
        List<Step> tempList = new ArrayList<>(p.getSchritteList());
        Collections.reverse(tempList);
        for (Step step : tempList) {
            if (step.getBearbeitungsstatusEnum() != StepStatus.LOCKED) {
                step.setEditTypeEnum(StepEditType.ADMIN);
                step.setBearbeitungszeitpunkt(new Date());
                step.setBearbeitungsstatusDown();
                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                        "Status changed using GoobiScript mass manipulation for step " + step.getTitel());
                break;
            }
        }
        try {
            ProcessManager.saveProcess(p);
            gsr.setResultMessage("Workflow was moved backward successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            e.printStackTrace();
            gsr.setResultMessage("Errow while moving the workflow backward: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();
    }
}
