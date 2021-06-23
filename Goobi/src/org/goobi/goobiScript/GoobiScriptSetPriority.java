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
public class GoobiScriptSetPriority extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "setPriority";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to define a priority to a specific workflow step.");
        addParameterToSampleCall(sb, "steptitle", "Scanning", "Title of the workflow step to be changed");
        addParameterToSampleCall(sb, "priority", "higher",
                "Priority to assign to the workflow step. Possible values are: `standard` `high` `higher` `highest` `correction`");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("priority") == null || parameters.get("priority").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "priority");
            return new ArrayList<>();
        }

        String prio = parameters.get("priority").toLowerCase();
        if (!prio.equals("standard") && !prio.equals("high") && !prio.equals("higher") && !prio.equals("highest") && !prio.equals("correction")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong priority parameter",
                    "(only the following values are allowed: standard, high, higher, highest, correction)");
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

        String st = parameters.get("steptitle");
        if (st == null) {
            st = "";
        }

        int prio = 0;
        switch (parameters.get("priority").toLowerCase()) {
            case "high":
                prio = 1;
                break;
            case "higher":
                prio = 2;
                break;
            case "highest":
                prio = 3;
                break;
            case "correction":
                prio = 10;
                break;
            default:
                prio = 0;
        }

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
            Step s = iterator.next();
            if (st.length() == 0 || s.getTitel().equals(st)) {
                s.setPrioritaet(prio);
                try {
                    StepManager.saveStep(s);
                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                            "Changed priority of step '" + s.getTitel() + "' to '" + s.getPrioritaet() + "' using GoobiScript.",
                            username);
                    log.info("Changed priority of step '" + s.getTitel() + "' to '" + s.getPrioritaet()
                            + "' using GoobiScript for process with ID " + p.getId());
                    gsr.setResultMessage("Changed priority of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' successfully.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                } catch (DAOException e) {
                    log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                    gsr.setResultMessage(
                            "Error while changing the priority of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "'.");
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                    gsr.setErrorText(e.getMessage());
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
