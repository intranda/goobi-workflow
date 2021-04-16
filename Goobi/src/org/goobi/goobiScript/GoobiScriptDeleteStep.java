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

        // execute all jobs that are still in waiting state
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        if (p.getSchritte() != null) {
            for (Iterator<Step> iterator = p.getSchritte().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(parameters.get("steptitle"))) {
                    p.getSchritte().remove(s);

                    StepManager.deleteStep(s);
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                            "Deleted step '" + parameters.get("steptitle") + "' from process using GoobiScript.", username);
                    log.info("Deleted step '" + parameters.get("steptitle") + "' from process using GoobiScript for process with ID "
                            + p.getId());
                    gsr.setResultMessage("Deleted step '" + parameters.get("steptitle") + "' from process.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                    break;
                }
            }
        }
        if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + parameters.get("steptitle"));
        }
        gsr.updateTimestamp();
    }
}
