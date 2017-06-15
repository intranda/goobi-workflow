package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;

public class GoobiScriptAddUser extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptAddUser.class);
	private User myUser = null;
	
	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }
        if (parameters.get("username") == null || this.parameters.get("username").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
            return false;
        }
        /* prüfen, ob ein solcher Benutzer existiert */
        
        try {
            List<User> treffer = UserManager.getUsers(null, "login='" + parameters.get("username") + "'", null, null);
            if (treffer != null && treffer.size() > 0) {
                myUser = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", parameters.get("username"));
                return false;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
            logger.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
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
		AddUserThread et = new AddUserThread();
		et.start();
	}

	class AddUserThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.setResultType(GoobiScriptResultType.RUNNING);
					gsr.updateTimestamp();
					
					for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
		                Step s = iterator.next();
		                if (s.getTitel().equals(parameters.get("steptitle"))) {
		                    List<User> myBenutzer = s.getBenutzer();
		                    if (myBenutzer == null) {
		                        myBenutzer = new ArrayList<User>();
		                        s.setBenutzer(myBenutzer);
		                    }
		                    if (!myBenutzer.contains(myUser)) {
		                        myBenutzer.add(myUser);
		                        try {
		                            StepManager.saveStep(s);
		                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "' using GoobiScript.", username);
		                            logger.info("Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "' using GoobiScript for process with ID " + p.getId());
		                            gsr.setResultMessage("Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "' successfully.");
		        					gsr.setResultType(GoobiScriptResultType.OK);
		                        } catch (DAOException e) {
		                        	logger.error("goobiScriptfield" + "Error while saving - " + p.getTitel(), e);
		                        	gsr.setResultMessage("Problem while adding user '" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "': " + e.getMessage());
		        					gsr.setResultType(GoobiScriptResultType.ERROR);
		                        }
		                    }
		                }
		            }
					gsr.updateTimestamp();
				}
			}
		}
	}

}
