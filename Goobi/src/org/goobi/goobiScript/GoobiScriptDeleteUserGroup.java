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
    private Usergroup myGroup = null;

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

        //return "---\\naction: deleteUserGroup\\nsteptitle: TITLE_STEP\\ngroup: GROUP_NAME";
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }
        if (parameters.get("group") == null || parameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return new ArrayList<>();
        }
        /* check if usergroup exists */
        try {
            List<Usergroup> treffer = UsergroupManager.getUsergroups(null, "titel='" + parameters.get("group") + "'", null, null, null);
            if (treffer != null && treffer.size() > 0) {
                myGroup = treffer.get(0);
            } else {
                Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", parameters.get("group"));
                return new ArrayList<>();
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript addusergroup", e);
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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean found = false;
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
                            "Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel() + "' using GoobiScript.",
                            username);
                    log.info("Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel()
                            + "' using GoobiScript for process with ID " + p.getId());
                    gsr.setResultMessage("Deleted usergroup '" + myGroup.getTitel() + "' from step '" + s.getTitel() + "' successfully.");

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
}
