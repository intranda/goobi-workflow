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
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptDeleteUserGroup extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String GROUP = "group";

    @Override
    public String getAction() {
        return "deleteUserGroup";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to remove a user group from a workflow step where it was assigned to.");
        addParameterToSampleCall(sb, STEPTITLE, "Scanning", "Title of the workflow step to adapt");
        addParameterToSampleCall(sb, GROUP, "Photographers", "Name of the user group that shall be removed from the workflow step");
        return sb.toString();

        // return "---\\naction: deleteUserGroup\\nsteptitle: TITLE_STEP\\ngroup:
        // GROUP_NAME";  (NOSONAR)
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, STEPTITLE);
            return new ArrayList<>();
        }

        String group = parameters.get(GROUP);
        if (group == null || "".equals(group)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, GROUP);
            return new ArrayList<>();
        }

        /* check if user group exists */
        if (this.getUserGroup(group) == null) {
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

        String stepTitle = parameters.get(STEPTITLE);
        String group = parameters.get(GROUP);

        Usergroup userGroup = getUserGroup(group);
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean found = false;
        if (userGroup != null) {
            for (Step step : p.getSchritteList()) {
                if (step.getTitel().equals(stepTitle)) {
                    List<Usergroup> userGroups = step.getBenutzergruppen();
                    if (userGroups == null) {
                        userGroups = new ArrayList<>();
                        step.setBenutzergruppen(userGroups);
                    }
                    found = true;
                    if (userGroups.contains(userGroup)) {
                        userGroups.remove(userGroup);

                        StepManager.removeUsergroupFromStep(step, userGroup);
                        String info = "'" + userGroup.getTitel() + "' from step '" + step.getTitel() + "'";
                        String message = "Deleted usergroup " + info + " using GoobiScript";
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + ".", username);
                        log.info(message + " for process with ID " + p.getId());
                        gsr.setResultMessage(message + " successfully.");

                    }
                }
            }
        }
        if (!found) {
            gsr.setResultMessage("No step '" + stepTitle + "' found.");
            gsr.setResultType(GoobiScriptResultType.ERROR);
        } else {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }

    private Usergroup getUserGroup(String groupName) {
        try {
            List<Usergroup> userGroups = UsergroupManager.getUsergroups(null, "titel='" + groupName + "'", null, null, null);
            if (userGroups != null && !userGroups.isEmpty()) {
                return userGroups.get(0);
            } else {
                Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, "Unknown group: ", groupName);
                return null;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, "Error in GoobiScript addusergroup", e);
            return null;
        }
    }
}
