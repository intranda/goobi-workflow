package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class GoobiScriptDeleteStep extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptDeleteStep.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
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
		DeleteStepThread et = new DeleteStepThread();
		et.start();
	}

	class DeleteStepThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
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
		                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Deleted step '" + parameters.get("steptitle") + "' from process using GoobiScript.", username);
		                        logger.info("Deleted step '" + parameters.get("steptitle") + "' from process using GoobiScript for process with ID " + p.getId());
		                        gsr.setResultMessage("Deleted step '" + parameters.get("steptitle") + "' from process.");
		    					gsr.setResultType(GoobiScriptResultType.OK);
		    					break;
		                    }
		                }
		            }
					gsr.updateTimestamp();
				}
			}
		}
	}

}
