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

    @Override
    public String getAction() {
        return "setStepStatus";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to change the current status of a specific step in the workflow.");
        addParameterToSampleCall(sb, "steptitle", "Metadata enrichment", "Title of the workflow step to be changed");
        addParameterToSampleCall(sb, "status", "1",
                "Value of the status. Possible values are `0` (locked), `1` (open), `2` (in work), `3` (done), `4` (error), `5` (deactivated)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }

        if (parameters.get("status") == null || parameters.get("status").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
            return new ArrayList<>();
        }

        if (!parameters.get("status").equals("0") && !parameters.get("status").equals("1") && !parameters.get("status").equals("2")
                && !parameters.get("status").equals("3") && !parameters.get("status").equals("4") && !parameters.get("status").equals("5")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ",
                    "(possible: 0=closed, 1=open, 2=in work, 3=finished, 4=error, 5=deactivated");
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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
            Step s = iterator.next();
            if (s.getTitel().equals(gsr.getParameters().get("steptitle"))) {
                String oldStatusTitle = s.getBearbeitungsstatusEnum().getUntranslatedTitle();
                s.setBearbeitungsstatusAsString(gsr.getParameters().get("status"));
                try {
                    StepManager.saveStep(s);
                    String newStatusTitle = s.getBearbeitungsstatusEnum().getUntranslatedTitle();
                    String message = "Changed status of step '" + s.getTitel() + "' from' " + oldStatusTitle + " ' to '" + newStatusTitle
                            + "' using GoobiScript";
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, message + ".", username);
                    log.info(message + " for process with ID " + p.getId());
                    gsr.setResultMessage("Status of the step is set successfully.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                } catch (DAOException e) {
                    log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                    gsr.setResultMessage("Error while changing the step status: " + e.getMessage());
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                    gsr.setErrorText(e.getMessage());
                }
                break;
            }
        }
        if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + gsr.getParameters().get("steptitle"));
        }
        gsr.updateTimestamp();
    }
}
