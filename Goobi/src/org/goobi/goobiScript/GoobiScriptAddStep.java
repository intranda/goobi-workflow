package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
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

public class GoobiScriptAddStep extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptAddStep.class);

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
			GoobiScriptResult gsr = new GoobiScriptResult(i, command);
			resultList.add(gsr);
		}
		
		return true;
	}

	@Override
	public void execute() {
		AddStepThread et = new AddStepThread();
		et.start();
	}

	class AddStepThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
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
		                Helper.addMessageToProcessLog(p.getId(),LogType.DEBUG,"Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript.", username);
		                logger.info("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "' to process using GoobiScript for process with ID " + p.getId());
		                gsr.setResultMessage("Added workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "'.");
						gsr.setResultType(GoobiScriptResultType.OK);
		            } catch (DAOException e) {
		                logger.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
		                gsr.setResultMessage("A problem occurred while adding a workflow step '" + s.getTitel() + "' at position '" + s.getReihenfolge() + "': " + e.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
		            }
				}
			}
		}
	}

}
