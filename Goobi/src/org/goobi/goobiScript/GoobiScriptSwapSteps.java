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
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSwapSteps extends AbstractIGoobiScript implements IGoobiScript {
    private int reihenfolge1;
    private int reihenfolge2;

    @Override
    public String getAction() {
        return "swapSteps";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to swap the order of two steps within a workflow.");
        addParameterToSampleCall(sb, "swap1nr", "4", "Order number of the first workflow step");
        addParameterToSampleCall(sb, "swap1title", "Quality assurance", "Title of the first workflow step");
        addParameterToSampleCall(sb, "swap2nr", "5", "Order number of the second workflow step");
        addParameterToSampleCall(sb, "swap2title", "Metadata enrichment", "Title of the second workflow step");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("swap1nr") == null || parameters.get("swap1nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1nr");
            return new ArrayList<>();
        }
        if (parameters.get("swap2nr") == null || parameters.get("swap2nr").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2nr");
            return new ArrayList<>();
        }
        if (parameters.get("swap1title") == null || parameters.get("swap1title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap1title");
            return new ArrayList<>();
        }
        if (parameters.get("swap2title") == null || parameters.get("swap2title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "swap2title");
            return new ArrayList<>();
        }

        try {
            reihenfolge1 = Integer.parseInt(parameters.get("swap1nr"));
            reihenfolge2 = Integer.parseInt(parameters.get("swap2nr"));
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("goobiScriptfield", "Invalid order number used: ", parameters.get("swap1nr") + " - " + parameters.get("swap2nr"));
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

        Step s1 = null;
        Step s2 = null;
        for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
            Step s = iterator.next();
            if (s.getTitel().equals(parameters.get("swap1title")) && s.getReihenfolge().intValue() == reihenfolge1) {
                s1 = s;
            }
            if (s.getTitel().equals(parameters.get("swap2title")) && s.getReihenfolge().intValue() == reihenfolge2) {
                s2 = s;
            }
        }
        if (s1 != null && s2 != null) {
            StepStatus statustemp = s1.getBearbeitungsstatusEnum();
            s1.setBearbeitungsstatusEnum(s2.getBearbeitungsstatusEnum());
            s2.setBearbeitungsstatusEnum(statustemp);
            s1.setReihenfolge(Integer.valueOf(reihenfolge2));
            s2.setReihenfolge(Integer.valueOf(reihenfolge1));
            try {
                StepManager.saveStep(s1);
                StepManager.saveStep(s2);
                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                        "Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel() + "' using GoobiScript.", username);
                log.info("Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel()
                        + "' using GoobiScript for process with ID " + p.getId());
                gsr.setResultMessage("Switched order of steps '" + s1.getTitel() + "' and '" + s2.getTitel() + "'.");
                gsr.setResultType(GoobiScriptResultType.OK);
            } catch (DAOException e) {
                log.error("Error on save while swapping steps: " + p.getTitel() + " - " + s1.getTitel() + " : " + s2.getTitel(), e);
                gsr.setResultMessage("Error on save while swapping steps: " + p.getTitel() + " - " + s1.getTitel() + " vs. "
                        + s2.getTitel() + ": " + e.getMessage());
                gsr.setResultType(GoobiScriptResultType.ERROR);
                gsr.setErrorText(e.getMessage());
            }
        }
        if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
