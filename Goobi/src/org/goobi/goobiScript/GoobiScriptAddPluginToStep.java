package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddPluginToStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("plugin") == null || parameters.get("plugin").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "plugin");
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
        AddPluginThread et = new AddPluginThread();
        et.start();
    }

    class AddPluginThread extends Thread {
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
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();

                    if (p.getSchritte() != null) {
                        for (Iterator<Step> iterator = p.getSchritte().iterator(); iterator.hasNext();) {
                            Step s = iterator.next();
                            if (s.getTitel().equals(parameters.get("steptitle"))) {
                                s.setStepPlugin(parameters.get("plugin"));
                                try {
                                    ProcessManager.saveProcess(p);
                                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                            "Added plugin '" + s.getStepPlugin() + " to step '" + s.getTitel() + "' using GoobiScript.", username);
                                    log.info("Added plugin '" + s.getStepPlugin() + " to step '" + s.getTitel()
                                    + "' using GoobiScript for process with ID " + p.getId());
                                    gsr.setResultMessage("Added plugin '" + s.getStepPlugin() + " to step '" + s.getTitel() + "'.");
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                } catch (DAOException e) {
                                    log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                                    gsr.setResultMessage("An error occurred while adding the plugin '" + s.getStepPlugin() + " to step '"
                                            + s.getTitel() + "': " + e.getMessage());
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                    gsr.setErrorText(e.getMessage());
                                }
                                break;
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

    @Override
    public String getSampleCall() {
        return "---\\naction: addPluginToStep\\nsteptitle: TITLE_STEP\\nplugin: PLUGIN_NAME";
    }
}
