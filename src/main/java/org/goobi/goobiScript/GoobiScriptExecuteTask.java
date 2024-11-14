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

import org.goobi.api.mq.QueueType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.ShellScriptReturnValue;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptExecuteTask extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String STEP_SCRIPT = "script";
    private static final String STEP_EXPORT = "export";
    private static final String STEP_DELAY = "delay plugin";
    private static final String STEP_PLUGIN = "plugin";
    private static final String STEP_HTTP = "HTTP";

    @Override
    public String getAction() {
        return "executeStepAndUpdateStatus";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allows to execute a specific workflow step and to move on the workflow afterwards automatically. This is mostly useful to trigger automatic workflows steps.");
        addParameterToSampleCall(sb, STEPTITLE, "OCR", "Title of the workflow step to be triggered.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(missingParameter, STEPTITLE);
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
        HelperSchritte hs = new HelperSchritte();
        // execute all jobs that are still in waiting state
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean foundExecutableStep = false;
        for (Step step : process.getSchritteList()) {
            if (!step.getTitel().equalsIgnoreCase(stepTitle)) {
                continue;
            }
            foundExecutableStep = true;
            List<String> scriptPaths = step.getAllScriptPaths();

            if (step.isTypScriptStep() && !scriptPaths.isEmpty()) {
                // This step is a script step
                if (step.getMessageQueue() == QueueType.NONE) {
                    ShellScriptReturnValue returncode = hs.executeAllScriptsForStep(step, step.isTypAutomatisch());
                    boolean success = returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99;
                    GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_SCRIPT, stepTitle, success);
                    if (!success) {
                        gsr.setErrorText(returncode.getErrorText() + " The return code was : " + returncode.getReturnCode());
                    }
                } else {
                    ScriptThreadWithoutHibernate thread = new ScriptThreadWithoutHibernate(step);
                    thread.startOrPutToQueue();
                    GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_SCRIPT, stepTitle, true);
                }

            } else if (step.isTypExportDMS()) {
                // This step is an export step
                boolean success = hs.executeDmsExport(step, step.isTypAutomatisch());
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_EXPORT, stepTitle, success);

            } else if (step.isDelayStep() && step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                // This step is a delay plugin step
                IDelayPlugin idp = (IDelayPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                idp.initialize(step, "");
                boolean success = idp.execute();
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_DELAY, stepTitle, success);
                if (success) {
                    hs.CloseStepObjectAutomatic(step);
                }

            } else if (step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                // This step is a plugin step
                IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                isp.initialize(step, "");

                boolean success;

                if (isp instanceof IStepPluginVersion2) {
                    IStepPluginVersion2 plugin = (IStepPluginVersion2) isp;
                    PluginReturnValue status = plugin.run();
                    success = (status == PluginReturnValue.FINISH) || (status == PluginReturnValue.WAIT);
                } else {
                    success = isp.execute();
                }

                GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_PLUGIN, stepTitle, success);
                if (success) {
                    GoobiScriptExecuteTask.printSuccessMessages(stepTitle, process.getId(), username);
                    hs.CloseStepObjectAutomatic(step);
                } else {
                    hs.errorStep(step);
                }

            } else if (step.isHttpStep()) {
                // This step is an HTTP step
                hs.runHttpStep(step);
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, STEP_HTTP, stepTitle, true);
            } else {
                gsr.setResultType(GoobiScriptResultType.OK);
                gsr.setResultMessage("Ignored the step \"" + stepTitle + "\" because there is no execution command specified.");
            }
        }
        if (!foundExecutableStep) {//gsr.getResultType().equals(GoobiScriptResultType.RUNNING)
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setResultMessage("Step not found: " + stepTitle);
        }

        gsr.updateTimestamp();
    }

    /**
     * Handles the success/fail state of the executed GoobiScript. When it was successful (parameter is true) the state is set to OK and a info
     * message is set. When it failed (parameter is false) the state is set to ERROR and a warning message is set.
     *
     * @param result The GoobiScriptResult object to set the message and the state
     * @param type The name of the type of the step
     * @param title The name of the currently executed step
     * @param successful The result of the execution of the script
     */
    private static void setStatusAndMessage(GoobiScriptResult result, String type, String title, boolean successful) {
        if (successful) {
            result.setResultType(GoobiScriptResultType.OK);
            result.setResultMessage("Executed the " + type + " step \"" + title + "\" successfully.");
        } else {
            result.setResultType(GoobiScriptResultType.ERROR);
            result.setResultMessage("There was an error while executing the " + type + " step \"" + title + "\".");
        }
    }

    /**
     * Prints success messages to the log output
     *
     * @param title The title of the step
     * @param id The id of the step
     * @param name The user name
     */
    private static void printSuccessMessages(String title, int id, String name) {
        String messagePrefix = "Plugin for step '" + title + "' executed using GoobiScript";
        Helper.addMessageToProcessJournal(id, LogType.DEBUG, messagePrefix + ".", name);
        log.info(messagePrefix + " for process with ID " + id);
    }
}
