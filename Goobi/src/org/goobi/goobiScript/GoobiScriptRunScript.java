package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ShellScriptReturnValue;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptRunScript extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewAction(sb, "runScript", "This GoobiScript allows to execute on specific or all scripts that are assiged to an existing workflow step.");
        addParameter(sb, "steptitle", "Update XMP Headers", "Title of the workflow step which has scripts that shall be executed");
        addParameter(sb, "script", "Write headers", "Name of a specific scripts to be executed. If this parameter is missing then all scripts of the defined task are executed.");
        return sb.toString();
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
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
        RunScriptThread et = new RunScriptThread();
        et.start();
    }

    class RunScriptThread extends Thread {

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
            HelperSchritte hs = new HelperSchritte();
            String steptitle = parameters.get("steptitle");
            String scriptname = parameters.get("script");

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    // TODO change this
                    for (Step step : p.getSchritteList()) {
                        if (step.getTitel().equalsIgnoreCase(steptitle)) {
                            Step so = StepManager.getStepById(step.getId());
                            if (scriptname != null) {
                                if (step.getAllScripts().containsKey(scriptname)) {
                                    String path = step.getAllScripts().get(scriptname);
                                    ShellScriptReturnValue returncode = hs.executeScriptForStepObject(so, path, false);
                                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                            "Script '" + scriptname + "' for step '" + steptitle + "' executed using GoobiScript.", username);
                                    log.info("Script '" + scriptname + "' for step '" + steptitle
                                            + "' executed using GoobiScript for process with ID " + p.getId());
                                    if (returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99) {
                                        gsr.setResultMessage("Script '" + scriptname + "' for step '" + steptitle + "' executed successfully.");
                                        gsr.setResultType(GoobiScriptResultType.OK);
                                    } else {
                                        gsr.setResultMessage("A problem occured while executing script '" + scriptname + "' for step '" + steptitle
                                                + "': " + returncode.getReturnCode());
                                        gsr.setResultType(GoobiScriptResultType.ERROR);
                                        gsr.setErrorText(returncode.getErrorText());
                                    }
                                } else {
                                    gsr.setResultMessage("Cant find script '" + scriptname + "' for step '" + steptitle + "'.");
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                }
                            } else {
                                ShellScriptReturnValue returncode = hs.executeAllScriptsForStep(so, false);
                                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                        "All scripts for step '" + steptitle + "' executed using GoobiScript.", username);
                                log.info("All scripts for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
                                if (returncode.getReturnCode() == 0 || returncode.getReturnCode() == 98 || returncode.getReturnCode() == 99) {
                                    gsr.setResultMessage("All scripts for step '" + steptitle + "' executed successfully.");
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                } else {
                                    gsr.setResultMessage("A problem occured while executing all scripts for step '" + steptitle + "': "
                                            + returncode.getReturnCode());
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                    gsr.setErrorText(returncode.getErrorText());
                                }
                            }
                        }
                    }
                    if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
                        gsr.setResultType(GoobiScriptResultType.OK);
                        gsr.setResultMessage("Step not found: " + parameters.get("steptitle"));
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
