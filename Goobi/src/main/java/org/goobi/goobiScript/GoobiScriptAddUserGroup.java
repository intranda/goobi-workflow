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
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Log4j2
public class GoobiScriptAddUserGroup extends AbstractIGoobiScript implements IGoobiScript {

    /**
     * The cache only works if the status is Status.STATUS_ALIVE. The status is Status.STATUS_ALIVE after it was registered in the cache manager.
     */
    private CacheManager cacheManager = null;
    /**
     * The cache stores all user groups that should be added with one or multiple goobi script instances. If the same group should be added to
     * multiple steps, the group is loaded from the database into the cache once and reused each time. This saves lots of database requests.
     */
    private Cache userGroupCache = null;

    @Override
    public String getAction() {
        return "addUserGroup";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to assign a user group to an existing workflow step.");
        addParameterToSampleCall(sb, "steptitle", "Scanning", "Title of the workflow step to be edited");
        addParameterToSampleCall(sb, "group", "Photographers", "Use the name of the user group to be assigned to the selected workflow step.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
        this.initializeCache();

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }
        if (parameters.get("group") == null || parameters.get("group").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "group");
            return new ArrayList<>();
        }

        /* check if usergroup exists */
        Usergroup groupInDatabase = GoobiScriptAddUserGroup.getUsergroupFromDatabase(parameters);
        if (groupInDatabase == null) {
            Helper.setFehlerMeldung("goobiScriptfield", "Unknown group: ", parameters.get("group"));
        }

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    /**
     * The cache is initialized with some standard values. After creating the cache object the cache must be registered in the cache manager. The
     * cache manager "holds" the cache to keep it alive. If the cache is tried to use without the manager, it throws an IllegalStateException.
     */
    private void initializeCache() {
        if (this.userGroupCache != null) {
            return;
        }

        String name = "userGroupCache";
        int maxElementsInMemory = 10;
        boolean overflowToDisk = false;
        boolean eternal = false;
        long timeToLiveSeconds = 3600;
        long timeToIdleSeconds = 3600;

        this.userGroupCache = new Cache(name, maxElementsInMemory, overflowToDisk, eternal, timeToLiveSeconds, timeToIdleSeconds);
        this.cacheManager = new CacheManager();
        this.cacheManager.addCache(this.userGroupCache);
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();

        // Add the group to the cache if it does not already exist
        if (!this.userGroupCache.isElementInMemory(parameters.get("group"))) {
            Usergroup group = GoobiScriptAddUserGroup.getUsergroupFromDatabase(parameters);
            Element element = new Element(parameters.get("group"), group);
            this.userGroupCache.put(element);
        }

        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        boolean found = false;
        List<Step> allSteps = process.getSchritteList();
        for (int index = 0; index < allSteps.size(); index++) {
            Step step = allSteps.get(index);

            if (!step.getTitel().equals(parameters.get("steptitle"))) {
                continue;
            }
            found = true;

            List<Usergroup> groupsOfStep = step.getBenutzergruppen();
            if (groupsOfStep == null) {
                groupsOfStep = new ArrayList<>();
                step.setBenutzergruppen(groupsOfStep);
            }

            Element element = this.userGroupCache.get(parameters.get("group"));
            Usergroup group = (Usergroup) (element.getObjectValue());

            if (!groupsOfStep.contains(group)) {
                groupsOfStep.add(group);
                this.saveStep(gsr, process, step, group);
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

    private void saveStep(GoobiScriptResult gsr, Process process, Step step, Usergroup group) {
        try {
            StepManager.saveStep(step);
            String message = "Added usergroup '" + group.getTitel() + "' to step '" + step.getTitel() + "' using GoobiScript.";
            Helper.addMessageToProcessLog(process.getId(), LogType.DEBUG, message, username);
            log.info(message + " The process id is " + process.getId());
            gsr.setResultMessage(message);

        } catch (DAOException edaoException) {
            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + process.getTitel(), edaoException);
            gsr.setResultMessage(
                    "Problem while adding usergroup '" + group.getTitel() + "' to step '" + step.getTitel() + "': " + edaoException.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(edaoException.getMessage());
        }
    }

    private static Usergroup getUsergroupFromDatabase(Map<String, String> parameters) {
        try {
            List<Usergroup> groups = UsergroupManager.getUsergroups(null, "titel='" + parameters.get("group") + "'", null, null, null);
            if (groups != null && groups.size() > 0) {
                return groups.get(0);
            } else {
                return null;
            }
        } catch (DAOException e) {
            log.error(e);
            Helper.setFehlerMeldung("goobiScriptfield", "Error in GoobiScript addusergroup", e);
            return null;
        }
    }
}
