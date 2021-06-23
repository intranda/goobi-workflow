package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSetProject extends AbstractIGoobiScript implements IGoobiScript {
    private Project project;

    @Override
    public String getAction() {
        return "setProject";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to assign Goobi processes to another project.");
        addParameterToSampleCall(sb, "project", "Newspaper_2021",
                "Define the project where the processes shall belong to. Use the name of the project here.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("project") == null || parameters.get("project").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "project");
            return new ArrayList<>();
        }

        try {
            List<Project> projects = ProjectManager.getProjects(null, "titel='" + parameters.get("project") + "'", null, null, null);
            if (projects == null || projects.size() == 0) {
                Helper.setFehlerMeldung("goobiScriptfield", "Could not find project: ", parameters.get("project"));
                return new ArrayList<>();
            }
            project = projects.get(0);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("goobiScriptfield", "Could not find project: ", parameters.get("project") + " - " + e.getMessage());
            log.error("Exception during assignement of project using GoobiScript", e);
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
