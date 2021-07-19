package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import de.sub.goobi.helper.ShellScriptReturnValue;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptExecuteTask extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "executeStepAndUpdateStatus";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allows to execute a specific workflow step and to move on the workflow afterwards automatically. This is mostly usefull to trigger automatic workflows steps.");
        addParameterToSampleCall(sb, "steptitle", "OCR", "Title of the workflow step to be triggered.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
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
        String steptitle = parameters.get("steptitle");
        HelperSchritte hs = new HelperSchritte();
        // execute all jobs that are still in waiting state
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean foundExecutableStep = false;
        for (Step step : process.getSchritteList()) {
            if (!step.getTitel().equalsIgnoreCase(steptitle)) {
                continue;
            }
            foundExecutableStep = true;
            List<String> scriptPaths = step.getAllScriptPaths();

            if (step.isTypScriptStep() && !scriptPaths.isEmpty()) {
                // This step is a script step
                ShellScriptReturnValue returncode = hs.executeAllScriptsForStep(step, step.isTypAutomatisch());
                boolean success = returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99;
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, "script", steptitle, success);
                if (!success) {
                    gsr.setErrorText(returncode.getErrorText() + " The return code was : " + returncode.getReturnCode());
                }

            } else if (step.isTypExportDMS()) {
                // This step is an export step
                boolean success = hs.executeDmsExport(step, step.isTypAutomatisch());
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, "export", steptitle, success);

            } else if (step.isDelayStep() && step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                // This step is a delay plugin step
                IDelayPlugin idp = (IDelayPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                idp.initialize(step, "");
                boolean success = idp.execute();
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, "delay plugin", steptitle, success);
                if (success) {
                    hs.CloseStepObjectAutomatic(step);
                }

            } else if (step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                // This step is a plugin step
                IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                isp.initialize(step, "");

                if (isp instanceof IStepPluginVersion2) {
                    IStepPluginVersion2 plugin = (IStepPluginVersion2) isp;
                    PluginReturnValue status = plugin.run();

                    boolean success = (status == PluginReturnValue.FINISH) || (status == PluginReturnValue.WAIT);
                    GoobiScriptExecuteTask.setStatusAndMessage(gsr, "plugin", steptitle, success);
                    if (success) {
                        GoobiScriptExecuteTask.printSuccessMessages(steptitle, process.getId(), username);
                    } else {
                        // success == PluginReturnValue.ERROR
                        hs.errorStep(step);
                    }
                } else {
                    boolean success = isp.execute();
                    GoobiScriptExecuteTask.setStatusAndMessage(gsr, "plugin", steptitle, success);
                    if (success) {
                        GoobiScriptExecuteTask.printSuccessMessages(steptitle, process.getId(), username);
                        hs.CloseStepObjectAutomatic(step);
                    } else {
                        hs.errorStep(step);
                    }
                }
            } else if (step.isHttpStep()) {
                // This step is an HTTP step
                hs.runHttpStep(step);
                GoobiScriptExecuteTask.setStatusAndMessage(gsr, "HTTP", steptitle, true);
            } else {
                gsr.setResultType(GoobiScriptResultType.OK);
                gsr.setResultMessage("Ignored the step \"" + steptitle + "\" because there is no execution command specified.");
            }
        }
        if (!foundExecutableStep) {//gsr.getResultType().equals(GoobiScriptResultType.RUNNING)
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setResultMessage("Step not found: " + parameters.get("steptitle"));
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
        Helper.addMessageToProcessLog(id, LogType.DEBUG, messagePrefix + ".", name);
        log.info(messagePrefix + " for process with ID " + id);
    }
}
