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
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class GoobiScriptSetStepStatus extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptSetStepStatus.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		 if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
	            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
	            return false;
	        }

	        if (parameters.get("status") == null || parameters.get("status").equals("")) {
	            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "status");
	            return false;
	        }

	        if (!parameters.get("status").equals("0") && !parameters.get("status").equals("1") && !parameters.get("status").equals(
	                "2") && !parameters.get("status").equals("3") && !parameters.get("status").equals("4") && !parameters.get("status").equals("5")) {
	            Helper.setFehlerMeldung("goobiScriptfield", "Wrong status parameter: status ", "(possible: 0=closed, 1=open, 2=in work, 3=finished, 4=error, 5=deactivated");
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
		SetStepStatusThread et = new SetStepStatusThread();
		et.start();
	}

	class SetStepStatusThread extends Thread {
			public void run() {
			// execute all jobs that are still in waiting state
				ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
	            for (GoobiScriptResult gsr : templist) {
				if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.setResultType(GoobiScriptResultType.RUNNING);
					gsr.updateTimestamp();
					
					for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
		                Step s = iterator.next();
		                if (s.getTitel().equals(parameters.get("steptitle"))) {
		                    s.setBearbeitungsstatusAsString(parameters.get("status"));
		                    try {
		                        StepManager.saveStep(s);
		                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Changed status of step '" + s.getTitel() + "' to '" + s.getBearbeitungsstatusEnum().getUntranslatedTitle() + "' using GoobiScript.", username);
		                        logger.info("Changed status of step '" + s.getTitel() + "' to '" + s.getBearbeitungsstatusEnum().getUntranslatedTitle() + "' using GoobiScript for process with ID " + p.getId());
		                        gsr.setResultMessage("Status of the step is set successfully.");
								gsr.setResultType(GoobiScriptResultType.OK);
		                    } catch (DAOException e) {
		                        logger.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
		                        gsr.setResultMessage("Error while changing the step status: " + e.getMessage());
								gsr.setResultType(GoobiScriptResultType.ERROR);
		                    }
		                    break;
		                }
		            }
					gsr.updateTimestamp();
				}
			}
		}
	}

}
