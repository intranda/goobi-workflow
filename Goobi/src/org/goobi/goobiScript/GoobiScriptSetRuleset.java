package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;

public class GoobiScriptSetRuleset extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptSetRuleset.class);
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
			logger.error("Exception during assignement of ruleset using GoobiScript", e);
			return false;			
		}
        
		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
			resultList.add(gsr);
		}
		
		return true;
	}

	@Override
	public void execute() {
		SetRulesetThread et = new SetRulesetThread();
		et.start();
	}

	class SetRulesetThread extends Thread {
		public void run() {
			
			// execute all jobs that are still in waiting state
			ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
				if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.setResultType(GoobiScriptResultType.RUNNING);
					gsr.updateTimestamp();
	                p.setRegelsatz(regelsatz);
	                try {
						ProcessManager.saveProcess(p);
						Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Ruleset '" + regelsatz + "' assigned using GoobiScript.", username);
	                    logger.info("Ruleset '" + regelsatz + "' assigned using GoobiScript for process with ID " + p.getId());
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
