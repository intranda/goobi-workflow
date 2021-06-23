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
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "addStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a new workflow step into the workflow.");
        addParameterToSampleCall(sb, "steptitle", "Scanning", "Title of the workflow step to add");
        addParameterToSampleCall(sb, "number", "5", "This number defines where in the workflow this new step is ordered into.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }
        if (parameters.get("number") == null || parameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return new ArrayList<>();
        }

        if (!StringUtils.isNumeric(parameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
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
        s.setTitel(parameters.get("steptitle"));
        s.setReihenfolge(Integer.parseInt(parameters.get("number")));
        s.setProzess(p);
        if (p.getSchritte() == null) {
            p.setSchritte(new ArrayList<Step>());
        }
        p.getSchritte().add(s);
        try {
            ProcessManager.saveProcess(p);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                    "Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript.",
                    username);
            log.info("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge()
                    + "' to process using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "'.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
            gsr.setResultMessage("A problem occurred while adding a workflow step '" + s.getTitel() + "' at position '"
                    + s.getReihenfolge() + "': " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();
    }

}
