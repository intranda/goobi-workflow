package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptMoveWorkflowForward extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "moveWorkflowForward";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript moves the progress of the workflow forward.");
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

        try {
            List<Step> stepList = new ArrayList<>(p.getSchritteList());
            for (Step so : stepList) {
                if (!(so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)
                        || so.getBearbeitungsstatusEnum().equals(StepStatus.DEACTIVATED))) {
                    switch (so.getBearbeitungsstatusEnum()) {
                        case LOCKED:
                            so.setBearbeitungsstatusEnum(StepStatus.OPEN);
                            break;
                        case OPEN:
                            so.setBearbeitungsstatusEnum(StepStatus.INWORK);
                            break;
                        case INWORK:
                        case INFLIGHT:
                        case ERROR:
                            so.setBearbeitungsstatusEnum(StepStatus.DONE);
                            break;
                        default:
                            so.setBearbeitungsstatusEnum(StepStatus.DONE);
                            break;
                    }
                    so.setEditTypeEnum(StepEditType.ADMIN);
                    if (so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
                        new HelperSchritte().CloseStepObjectAutomatic(so);
                    } else {
                        ProcessManager.saveProcess(p);
                    }
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                            "Status changed using GoobiScript mass manipulation for step " + so.getTitel());
                    break;
                }
            }
            gsr.setResultMessage("Workflow was moved forward successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            log.error(e);
            gsr.setResultMessage("Errow while moving the workflow forward: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();
    }
}
