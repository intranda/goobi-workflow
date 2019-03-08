package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptExecuteTask extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
            resultList.add(gsr);
        }
        return true;
    }

    @Override
    public void execute() {
        ExecuteTaskThread et = new ExecuteTaskThread();
        et.start();
    }

    class ExecuteTaskThread extends Thread {

        @Override
        public void run() {
            String steptitle = parameters.get("steptitle");
            HelperSchritte hs = new HelperSchritte();
            // execute all jobs that are still in waiting state
            List<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    for (Step step : p.getSchritteList()) {
                        if (step.getTitel().equalsIgnoreCase(steptitle)) {
                            List<String> scriptPaths = step.getAllScriptPaths();
                            if (step.getTypScriptStep() && !scriptPaths.isEmpty()) {
                                // found script(s) to execute
                                ShellScriptReturnValue returncode  =  hs.executeAllScriptsForStep(step, step.isTypAutomatisch());
                                if (returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99) {
                                    gsr.setResultMessage("Script for step '" + steptitle + "' executed successfully.");
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                } else {
                                    gsr.setResultMessage("A problem occured while executing script for step '" + steptitle
                                            + "': " + returncode.getReturnCode());
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                    gsr.setErrorText(returncode.getErrorText());
                                }
                            } else if (step.isTypExportDMS()) {
                                // step is an export task
                                boolean exportSuccessful =  hs.executeDmsExport(step, step.isTypAutomatisch());
                                if (exportSuccessful) {
                                    gsr.setResultMessage("Export done successfully");
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                } else {
                                    gsr.setResultMessage("Errors occurred during export. See process log for details.");
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                }
                            } else if (step.isDelayStep() && step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                                // check if the delay is exhausted
                                IDelayPlugin idp = (IDelayPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                                idp.initialize(step, "");
                                if (idp.execute()) {
                                    gsr.setResultMessage("Finished delay plugin "  + step.getStepPlugin());
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                    hs.CloseStepObjectAutomatic(step);
                                }

                            } else if (step.getStepPlugin() != null && !step.getStepPlugin().isEmpty()) {
                                // run plugin
                                IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                                isp.initialize(step, "");

                                if (isp instanceof IStepPluginVersion2) {
                                    IStepPluginVersion2 plugin = (IStepPluginVersion2) isp;
                                    PluginReturnValue val = plugin.run();
                                    if (val == PluginReturnValue.FINISH) {
                                        hs.CloseStepObjectAutomatic(step);
                                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Plugin for step '" + steptitle + "' executed using GoobiScript.", username);
                                        log.info("Plugin for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
                                        gsr.setResultMessage("Plugin for step '" + steptitle + "' executed successfully.");
                                        gsr.setResultType(GoobiScriptResultType.OK);
                                    } else if (val == PluginReturnValue.ERROR) {
                                        hs.errorStep(step);
                                        gsr.setResultMessage("Plugin for step " + steptitle + " failed.");
                                        gsr.setResultType(GoobiScriptResultType.ERROR);
                                    } else if (val == PluginReturnValue.WAIT) {
                                        // stay in status inwork
                                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Plugin for step '" + steptitle + "' executed using GoobiScript.", username);
                                        log.info("Plugin for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
                                        gsr.setResultMessage("Plugin for step '" + steptitle + "' executed successfully.");
                                        gsr.setResultType(GoobiScriptResultType.OK);
                                    }

                                } else {
                                    if (isp.execute()) {
                                        hs.CloseStepObjectAutomatic(step);
                                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Plugin for step '" + steptitle + "' executed using GoobiScript.", username);
                                        log.info("Plugin for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
                                        gsr.setResultMessage("Plugin for step '" + steptitle + "' executed successfully.");
                                        gsr.setResultType(GoobiScriptResultType.OK);
                                    } else {
                                        hs.errorStep(step);
                                        gsr.setResultMessage("Plugin for step " + steptitle + " failed.");
                                        gsr.setResultType(GoobiScriptResultType.ERROR);
                                    }
                                }
                            } else if (step.isHttpStep()) {
                                hs.runHttpStep(step);
                                gsr.setResultMessage("Executed http step");
                                gsr.setResultType(GoobiScriptResultType.OK);
                            }
                        }
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
