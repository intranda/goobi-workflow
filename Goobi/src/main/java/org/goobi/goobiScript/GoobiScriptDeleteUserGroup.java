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
import java.util.Iterator;
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

    @Override
    public String getAction() {
        return "deleteUserGroup";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to remove a user group from a workflow step where it was assigned to.");
        addParameterToSampleCall(sb, "steptitle", "Scanning", "Title of the workflow step to adapt");
        addParameterToSampleCall(sb, "group", "Photographers", "Name of the user group that shall be removed from the workflow step");
        return sb.toString();

        // return "---\\naction: deleteUserGroup\\nsteptitle: TITLE_STEP\\ngroup:
        // GROUP_NAME";  (NOSONAR)
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
        Usergroup myGroup = null;
        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }
        if (parameters.get("group") == null || parameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return new ArrayList<>();
        }

        myGroup = getUserGroup(parameters.get("group"));
        /* check if usergroup exists */
        if (myGroup == null) {
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
        Usergroup myGroup = getUserGroup(parameters.get("group"));
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean found = false;
        if (myGroup != null) {
            for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
                Step s = iterator.next();
                if (s.getTitel().equals(parameters.get("steptitle"))) {
                    List<Usergroup> myBenutzergruppe = s.getBenutzergruppen();
                    if (myBenutzergruppe == null) {
                        myBenutzergruppe = new ArrayList<>();
                        s.setBenutzergruppen(myBenutzergruppe);
                    }
                    found = true;
                    if (myBenutzergruppe.contains(myGroup)) {
                        myBenutzergruppe.remove(myGroup);

                        StepManager.removeUsergroupFromStep(s, myGroup);
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                "Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel() + "' using GoobiScript.", username);
                        log.info("Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel()
                        + "' using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage("Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel() + "' successfully.");

                    }
                }
            }
        }
        if (!found) {
            gsr.setResultMessage("No step '" + parameters.get("steptitle") + "' found.");
            gsr.setResultType(GoobiScriptResultType.ERROR);
        } else {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }

    private Usergroup getUserGroup(String UserGroupTitle) {
        Usergroup myGroup;
        try {
            List<Usergroup> treffer = UsergroupManager.getUsergroups(null, "titel='" + UserGroupTitle + "'", null, null, null);
            if (treffer != null && ! treffer.isEmpty()) {
                myGroup = treffer.get(0);
                return myGroup;
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", parameters.get("group"));
                return null;
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript addusergroup", e);
            return null;
        }
    }
}
