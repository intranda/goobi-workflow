package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptSetRuleset extends AbstractIGoobiScript implements IGoobiScript {
    private Ruleset regelsatz;

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("ruleset") == null || parameters.get("ruleset").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "ruleset");
            return false;
        }

        try {
            List<Ruleset> rulesets = RulesetManager.getRulesets(null, "titel='" + parameters.get("ruleset") + "'", null, null);
            if (rulesets == null || rulesets.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", parameters.get("ruleset"));
                return false;
            }
            regelsatz = rulesets.get(0);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Could not find ruleset: ", parameters.get("ruleset") + " - " + e.getMessage());
            log.error("Exception during assignement of ruleset using GoobiScript", e);
            return false;
        }

        // add all valid commands to list
        ImmutableList.Builder<GoobiScriptResult> newList = ImmutableList.<GoobiScriptResult> builder().addAll(gsm.getGoobiScriptResults());
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            newList.add(gsr);
        }
        gsm.setGoobiScriptResults(newList.build());

        return true;
    }

    @Override
    public void execute() {
        SetRulesetThread et = new SetRulesetThread();
        et.start();
    }

    class SetRulesetThread extends Thread {
        @Override
        public void run() {
            // wait until there is no earlier script to be executed first
            while (gsm.getAreEarlierScriptsWaiting(starttime)) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Problem while waiting for running GoobiScripts", e);
                }
            }
            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
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
        }
    }
}
