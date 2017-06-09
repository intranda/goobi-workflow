package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

public class GoobiScriptAddUserGroup extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptAddUserGroup.class);
	private Usergroup myGroup = null;
	
	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

	 	if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }
        if (parameters.get("group") == null || parameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return false;
        }
        /* check if usergroup exists */
        try {
            List<Usergroup> treffer = UsergroupManager.getUsergroups(null, "titel='" + parameters.get("group") + "'", null, null);
            if (treffer != null && treffer.size() > 0) {
                myGroup = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", parameters.get("group"));
                return false;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript addusergroup", e);
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
		AddUserGroupThread et = new AddUserGroupThread();
		et.start();
	}

	class AddUserGroupThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.updateTimestamp();
					
					for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
		                Step s = iterator.next();
		                if (s.getTitel().equals(parameters.get("steptitle"))) {
		                    List<Usergroup> myBenutzergruppe = s.getBenutzergruppen();
		                    if (myBenutzergruppe == null) {
		                        myBenutzergruppe = new ArrayList<Usergroup>();
		                        s.setBenutzergruppen(myBenutzergruppe);
		                    }
		                    if (!myBenutzergruppe.contains(myGroup)) {
		                        myBenutzergruppe.add(myGroup);
		                        try {
		                            StepManager.saveStep(s);
		                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Added usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "' using GoobiScript.", username);
		                            logger.info("Added usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "' using GoobiScript for process with ID " + p.getId());
		                            gsr.setResultMessage("Added usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "' successfully.");
		        					gsr.setResultType(GoobiScriptResultType.OK);
		                        } catch (DAOException e) {
		                            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + p.getTitel(), e);
		                            gsr.setResultMessage("Problem while adding usergroup '" + myGroup.getTitel() + "' to step '" + s.getTitel() + "': " + e.getMessage());
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
