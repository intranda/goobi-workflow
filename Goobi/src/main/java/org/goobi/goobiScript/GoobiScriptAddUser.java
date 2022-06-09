package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddUser extends AbstractIGoobiScript implements IGoobiScript {

	@Override
	public String getAction() {
		return "addUser";
	}

	@Override
	public String getSampleCall() {
		StringBuilder sb = new StringBuilder();
		addNewActionToSampleCall(sb, "This GoobiScript allows to assign a user to an existing workflow step.");
		addParameterToSampleCall(sb, "steptitle", "Scanning", "Title of the workflow step to be edited");
		addParameterToSampleCall(sb, "username", "steffen", "Login name of the user to assign to the workflow step.");
		return sb.toString();
	}

	@Override
	public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
		super.prepare(processes, command, parameters);
		User myUser = null;
		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return new ArrayList<>();
		}
		if (parameters.get("username") == null || this.parameters.get("username").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "username");
			return new ArrayList<>();
		}
		/* prüfen, ob ein solcher Benutzer existiert */

		myUser = getUser(parameters.get("username"));
		if (myUser == null) {
			return new ArrayList<>();
		}

		// add all valid commands to list
		List<GoobiScriptResult> newList = new ArrayList<>();
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
			newList.add(gsr);
		}
		return newList;
	}

	@Override
	public void execute(GoobiScriptResult gsr) {
		Map<String, String> parameters = gsr.getParameters();
		User myUser = getUser(parameters.get("username"));
		Process p = ProcessManager.getProcessById(gsr.getProcessId());
		gsr.setProcessTitle(p.getTitel());
		gsr.setResultType(GoobiScriptResultType.RUNNING);
		gsr.updateTimestamp();

		for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
			Step s = iterator.next();
			if (s.getTitel().equals(parameters.get("steptitle"))) {
				List<User> myBenutzer = s.getBenutzer();
				if (myBenutzer == null) {
					myBenutzer = new ArrayList<>();
					s.setBenutzer(myBenutzer);
				}
				if (!myBenutzer.contains(myUser)) {
					myBenutzer.add(myUser);
					try {
						StepManager.saveStep(s);
						Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Added user '" + myUser.getNachVorname()
								+ "' to step '" + s.getTitel() + "' using GoobiScript.", username);
						log.info("Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel()
								+ "' using GoobiScript for process with ID " + p.getId());
						gsr.setResultMessage("Added user '" + myUser.getNachVorname() + "' to step '" + s.getTitel()
								+ "' successfully.");
						gsr.setResultType(GoobiScriptResultType.OK);
					} catch (DAOException e) {
						log.error("goobiScriptfield" + "Error while saving - " + p.getTitel(), e);
						gsr.setResultMessage("Problem while adding user '" + myUser.getNachVorname() + "' to step '"
								+ s.getTitel() + "': " + e.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
						gsr.setErrorText(e.getMessage());
					}
				}
			}
		}
		if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
			gsr.setResultType(GoobiScriptResultType.OK);
			gsr.setResultMessage("Step not found: " + parameters.get("steptitle"));
		}
		gsr.updateTimestamp();
	}

	private User getUser(String userTitle) {
		try {
			List<User> treffer = UserManager.getUsers(null, "login='" + parameters.get("username") + "'", null, null,
					null);
			if (treffer != null && treffer.size() > 0) {
				return treffer.get(0);
			} else {
				Helper.setFehlerMeldung("goobiScriptfield", "Unknown user: ", parameters.get("username"));
				return null;
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript.adduser", e);
			log.error("goobiScriptfield" + "Error in GoobiScript.adduser: ", e);
			return null;
		}
	}
}
