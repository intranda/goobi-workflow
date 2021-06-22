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
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptDeleteStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "deleteStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to delete an existing workflow step");
        addParameterToSampleCall(sb, "steptitle", "Image upload", "Define the name of the step that shall get deleted.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
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
        String stepTitle = parameters.get("steptitle");
        int removedSteps = 0;

        for (int index = 0; index < steps.size(); index++) {
            Step step = steps.get(index);

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
            String message = "Deleted step '" + stepTitle + "'" + howOften + " from process using GoobiScript";
            Helper.addMessageToProcessLog(process.getId(), LogType.DEBUG, message + ".", username);
            log.info(message + " for process with ID " + process.getId());
            gsr.setResultMessage("Deleted step '" + stepTitle + "'" + howOften + " from process.");
        } else {
            gsr.setResultMessage("Step not found: " + stepTitle);
        }
        gsr.setResultType(GoobiScriptResultType.OK);
        gsr.updateTimestamp();
    }
}
