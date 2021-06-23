package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

    @Override
    public String getAction() {
        return "renameStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to rename an existing workflow step.");
        addParameterToSampleCall(sb, "oldStepName", "Scanning", "Define the current title of the workflow step here.");
        addParameterToSampleCall(sb, "newStepName", "Image upload", "Define how the title of the workflow step shall be from now on.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("oldStepName"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "oldStepName");
            return new ArrayList<>();
        }
        if (StringUtils.isBlank(parameters.get("newStepName"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "newStepName");
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
        String processTitle = p.getTitel();
        gsr.setProcessTitle(processTitle);
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        String oldStepName = parameters.get("oldStepName");
        String newStepName = parameters.get("newStepName");
        for (Step step : p.getSchritte()) {
            if (step.getTitel().equalsIgnoreCase(oldStepName)) {
                step.setTitel(newStepName);
                try {
                    StepManager.saveStep(step);
                } catch (DAOException e) {
                    log.error(e);
                }

                log.info("Step title changed using GoobiScript for process with ID " + p.getId());
                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                        "Step title changed '" + oldStepName + " to  '" + newStepName + "' using GoobiScript.", username);
                gsr.setResultMessage("Step title changed successfully.");
            }
        }
        gsr.setResultType(GoobiScriptResultType.OK);

        gsr.updateTimestamp();
    }

}
