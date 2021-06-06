package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSetRuleset extends AbstractIGoobiScript implements IGoobiScript {
    private Ruleset regelsatz;

    @Override
    public String getAction() {
        return "setRuleset";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to assign a specific ruleset to the processes.");
        addParameterToSampleCall(sb, "ruleset", "Newspapers",
                "Use the internal name of the ruleset here, not the name of the xml file where the ruleset is located.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("ruleset") == null || parameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
            return new ArrayList<>();
        }

        try {
            List<Ruleset> rulesets = RulesetManager.getRulesets(null, "titel='" + parameters.get("ruleset") + "'", null, null, null);
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", parameters.get("ruleset"));
                return new ArrayList<>();
            }
            regelsatz = rulesets.get(0);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", parameters.get("ruleset") + " - " + e.getMessage());
            log.error("Exception during assignement of ruleset using GoobiScript", e);
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
        p.setRegelsatz(regelsatz);
        try {
            ProcessManager.saveProcess(p);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Ruleset '" + regelsatz + "' assigned using GoobiScript.", username);
            log.info("Ruleset '" + regelsatz + "' assigned using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Ruleset  '" + regelsatz + "' assigned successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            gsr.setResultMessage("Problem assigning new ruleset: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
