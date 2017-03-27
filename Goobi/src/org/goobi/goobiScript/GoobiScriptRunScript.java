package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class GoobiScriptRunScript extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptRunScript.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command);
			resultList.add(gsr);
		}
		
		return true;
	}

	@Override
	public void execute() {
		RunScriptThread et = new RunScriptThread();
		et.start();
	}

	class RunScriptThread extends Thread {
		
		public void run() {
			HelperSchritte hs = new HelperSchritte();
			String stepname = parameters.get("stepname");
			String scriptname = parameters.get("script");
			
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());

					for (Step step : p.getSchritteList()) {
		                if (step.getTitel().equalsIgnoreCase(stepname)) {
		                    Step so = StepManager.getStepById(step.getId());
		                    if (scriptname != null) {
		                        if (step.getAllScripts().containsKey(scriptname)) {
		                            String path = step.getAllScripts().get(scriptname);
		                            hs.executeScriptForStepObject(so, path, false);
		                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Script '" + scriptname + "' for step '" + stepname + "' executed using GoobiScript.", username);
		                            logger.info("Script '" + scriptname + "' for step '" + stepname + "' executed using GoobiScript for process with ID " + p.getId());
		                            gsr.setResultMessage("Script '" + scriptname + "' for step '" + stepname + "' executed successfully.");
		                            gsr.setResultType(GoobiScriptResultType.OK);
		                        }
		                    } else {
		                        hs.executeAllScriptsForStep(so, false);
		                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "All scripts for step '" + stepname + "' executed using GoobiScript.", username);
		                        logger.info("All scripts for step '" + stepname + "' executed using GoobiScript for process with ID " + p.getId());
		                        gsr.setResultMessage("All scripts for step '" + stepname + "' executed successfully.");
		                        gsr.setResultType(GoobiScriptResultType.OK);
		                    }
		                }
		            }
				}
			}
		}
	}

}
