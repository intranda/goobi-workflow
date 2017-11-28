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

public class GoobiScriptAddShellScriptToStep extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptAddShellScriptToStep.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("label") == null || parameters.get("label").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "label");
            return false;
        }

        if (parameters.get("script") == null || parameters.get("script").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
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
		AddShellScriptThread et = new AddShellScriptThread();
		et.start();
	}

	class AddShellScriptThread extends Thread {
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
		                        s.setTypAutomatischScriptpfad(parameters.get("script"));
		                        s.setScriptname1(parameters.get("label"));
		                        s.setTypScriptStep(true);
		                        try {
		                            ProcessManager.saveProcess(p);
		                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "' using GoobiScript.", username);
		                            logger.info("Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "' using GoobiScript for process with ID " + p.getId());
		                            gsr.setResultMessage("Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "'.");
		        					gsr.setResultType(GoobiScriptResultType.OK);
		                        } catch (DAOException e) {
		                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + p.getTitel(), e);
		                            logger.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
		                            gsr.setResultMessage("Error while adding script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '" +  s.getTypAutomatischScriptpfad() + "': " + e.getMessage());
		        					gsr.setResultType(GoobiScriptResultType.ERROR);
		                        }
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
