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

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String FIELD_PROJECT = "project";

    private Project project;

    @Override
    public String getAction() {
        return "setProject";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to assign Goobi processes to another project.");
        addParameterToSampleCall(sb, FIELD_PROJECT, "Newspaper_2021",
                "Define the project where the processes shall belong to. Use the name of the project here.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String projectName = parameters.get(FIELD_PROJECT);
        if (projectName == null || "".equals(projectName)) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, missingParameter, FIELD_PROJECT);
            return new ArrayList<>();
        }

        String couldNotFindProject = "Could not find project: ";
        try {
            List<Project> projects = ProjectManager.getProjects(null, "titel='" + projectName + "'", null, null, null);
            if (projects == null || projects.isEmpty()) {
                Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, couldNotFindProject, projectName);
                return new ArrayList<>();
            }
            project = projects.get(0);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(GOOBI_SCRIPTFIELD, couldNotFindProject, projectName + " - " + e.getMessage());
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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        p.setProjekt(project);
        p.setProjectId(project.getId());
        try {
            ProcessManager.saveProcess(p);
            String message = "Project '" + project.getTitel() + "' assigned";
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
            log.info(message + " using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage(message + " successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (DAOException e) {
            gsr.setResultMessage("Problem assigning new project: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
