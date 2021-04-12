package org.goobi.goobiScript;

import java.util.Date;
import java.util.Map;
import java.util.List;

import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddToProcessLog extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "addToProcessLog";
    }
    
    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add messages to the Goobi process log.");
        addParameterToSampleCall(sb, "type", "info", "Define the type for the message here. Possible values are: `debug` `info` `warn` `error` `user`");
        addParameterToSampleCall(sb, "message", "This is my message", "This parameter allows to define the message itself that shall be added to the process log.");
        return sb.toString();
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("message") == null || parameters.get("message").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "message");
            return false;
        }

        if (parameters.get("type") == null || parameters.get("type").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "type");
            return false;
        }
        if (!parameters.get("type").equals("debug") && !parameters.get("type").equals("info") && !parameters.get("type").equals("error")
                && !parameters.get("type").equals("warn") && !parameters.get("type").equals("user")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong parameter for type. Allowed values are: ", "error, warn, info, debug, user");
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
        AddToProcessLogThread et = new AddToProcessLogThread();
        et.start();
    }

    class AddToProcessLogThread extends Thread {
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

                    LogEntry logEntry = new LogEntry();
                    logEntry.setContent(parameters.get("message"));
                    logEntry.setCreationDate(new Date());
                    logEntry.setProcessId(p.getId());
                    logEntry.setType(LogType.getByTitle(parameters.get("type")));
                    logEntry.setUserName(username);

                    ProcessManager.saveLogEntry(logEntry);
                    log.info("Process log updated for process with ID " + p.getId());

                    gsr.setResultMessage("Process log updated.");
                    gsr.setResultType(GoobiScriptResultType.OK);
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
