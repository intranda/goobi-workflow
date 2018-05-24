package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class GoobiScriptSetStepNumber extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptSetStepNumber.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("number") == null || parameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return false;
        }

        if (!StringUtils.isNumeric(parameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
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
		TEMPLATEThread et = new TEMPLATEThread();
		et.start();
	}

	class TEMPLATEThread extends Thread {
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
		                    s.setReihenfolge(Integer.parseInt(parameters.get("number")));
		                    try {
		                        StepManager.saveStep(s);
		                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' using GoobiScript.", username);
		                        logger.info("Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' using GoobiScript for process with ID " + p.getId());
								gsr.setResultMessage("Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' successfully.");
								gsr.setResultType(GoobiScriptResultType.OK);
		                    } catch (DAOException e) {
		                        logger.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
		                        gsr.setResultMessage("Error while changing the order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "'.");
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
