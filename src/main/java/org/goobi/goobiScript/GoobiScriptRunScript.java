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
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ShellScriptReturnValue;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptRunScript extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String SCRIPT = "script";

    @Override
    public String getAction() {
        return "runScript";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to execute on specific or all scripts that are assiged to an existing workflow step.");
        addParameterToSampleCall(sb, STEPTITLE, "Update XMP Headers", "Title of the workflow step which has scripts that shall be executed");
        addParameterToSampleCall(sb, SCRIPT, "Write headers",
                "Name of a specific scripts to be executed. If this parameter is missing then all scripts of the defined task are executed.");
        return sb.toString();
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
        HelperSchritte hs = new HelperSchritte();
        String steptitle = parameters.get(STEPTITLE);
        String scriptname = parameters.get(SCRIPT);

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        String info = "'" + scriptname + "' for step '" + steptitle + "'";
        // TODO change this
        for (Step step : p.getSchritteList()) {
            if (step.getTitel().equalsIgnoreCase(steptitle)) {
                Step so = StepManager.getStepById(step.getId());
                if (scriptname != null) {
                    if (step.getAllScripts().containsKey(scriptname)) {
                        String path = step.getAllScripts().get(scriptname);
                        ShellScriptReturnValue returncode = hs.executeScriptForStepObject(so, path, false);
                        String message = "Script " + info + " executed successfully";
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                        log.info(message + " using GoobiScript for process with ID " + p.getId());
                        if (returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99) {
                            gsr.setResultMessage(message + ".");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } else {
                            gsr.setResultMessage("A problem occured while executing script " + info + ": " + returncode.getReturnCode());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(returncode.getErrorText());
                        }
                    } else {
                        gsr.setResultMessage("Cant find script " + info + ".");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                    }
                } else {
                    ShellScriptReturnValue returncode = hs.executeAllScriptsForStep(so, false);
                    String message = "All scripts for step '" + steptitle + "' executed";
                    Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                    log.info(message + " using GoobiScript for process with ID " + p.getId());
                    if (returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99) {
                        gsr.setResultMessage(message + " successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } else {
                        gsr.setResultMessage(
                                "A problem occured while executing all scripts for step '" + steptitle + "': " + returncode.getReturnCode());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(returncode.getErrorText());
                    }
                }
            }
        }
        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + steptitle);
        }
        gsr.updateTimestamp();
    }
}
