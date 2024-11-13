/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.goobiScript;

import java.util.ArrayList;
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

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String FIELD_STEPTITLE = "steptitle";
    private static final String FIELD_USERNAME = "username";

    @Override
    public String getAction() {
        return "addUser";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to assign a user to an existing workflow step.");
        addParameterToSampleCall(sb, FIELD_STEPTITLE, "Scanning", "Title of the workflow step to be edited");
        addParameterToSampleCall(sb, FIELD_USERNAME, "steffen", "Login name of the user to assign to the workflow step.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
        User myUser = null;
        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(FIELD_STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, FIELD_STEPTITLE);
            return new ArrayList<>();
        }
        String username = parameters.get(FIELD_USERNAME);
        if (username == null || "".equals(username)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, FIELD_USERNAME);
            return new ArrayList<>();
        }
        /* prüfen, ob ein solcher Benutzer existiert */

        myUser = getUser(username);
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
        User myUser = getUser(parameters.get(FIELD_USERNAME));
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        if (myUser != null) {
            for (Step s : p.getSchritteList()) {
                if (s.getTitel().equals(parameters.get(FIELD_STEPTITLE))) {
                    List<User> myBenutzer = s.getBenutzer();
                    if (myBenutzer == null) {
                        myBenutzer = new ArrayList<>();
                        s.setBenutzer(myBenutzer);
                    }
                    if (!myBenutzer.contains(myUser)) {
                        myBenutzer.add(myUser);
                        String info = "'" + myUser.getNachVorname() + "' to step '" + s.getTitel() + "'";
                        try {
                            StepManager.saveStep(s);
                            String message = "Added user " + info;
                            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                            log.info(message + " using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage(message + " successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } catch (DAOException e) {
                            log.error(GOOBI_SCRIPTFIELD + "Error while saving - " + p.getTitel(), e);
                            gsr.setResultMessage("Problem while adding user " + info + ": " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(e.getMessage());
                        }
                    }
                }
            }
        }

        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + parameters.get(FIELD_STEPTITLE));
        }
        gsr.updateTimestamp();
    }

    private User getUser(String userName) {
        try {
            List<User> treffer = UserManager.getUsers(null, "login='" + userName + "'", null, null, null);
            if (treffer != null && !treffer.isEmpty()) {
                return treffer.get(0);
            } else {
                Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, "Unknown user: ", userName);
                return null;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, "Error in GoobiScript.adduser", e);
            log.error(GOOBI_SCRIPTFIELD + "Error in GoobiScript.adduser: ", e);
            return null;
        }
    }
}
