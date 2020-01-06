package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptSetProject extends AbstractIGoobiScript implements IGoobiScript {
    private Project project;

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("project") == null || parameters.get("project").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "project");
            return false;
        }

        try {
            List<Project> projects = ProjectManager.getProjects(null, "titel='" + parameters.get("project") + "'", null, null, null);
            if (projects == null || projects.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find project: ", parameters.get("project"));
                return false;
            }
            project = projects.get(0);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Could not find project: ", parameters.get("project") + " - " + e.getMessage());
            log.error("Exception during assignement of project using GoobiScript", e);
            return false;
        }

        // add all valid commands to list
        ImmutableList.Builder<GoobiScriptResult> newList = ImmutableList.<GoobiScriptResult> builder().addAll(gsm.getGoobiScriptResults());
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            newList.add(gsr);
        }
        gsm.setGoobiScriptResults(newList.build());

        return true;
    }

    @Override
    public void execute() {
        SetProjectThread et = new SetProjectThread();
        et.start();
    }

    class SetProjectThread extends Thread {
        @Override
        public void run() {
            // wait until there is no earlier script to be executed first
            while (gsm.getAreEarlierScriptsWaiting(starttime)) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Problem while waiting for running GoobiScripts", e);
                }
            }
            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    p.setProjekt(project);
                    p.setProjectId(project.getId());
                    try {
                        ProcessManager.saveProcess(p);
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Project '" + project + "' assigned using GoobiScript.", username);
                        log.info("Project '" + project + "' assigned using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage("Project  '" + project + "' assigned successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (DAOException e) {
                        gsr.setResultMessage("Problem assigning new project: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.OK);
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
