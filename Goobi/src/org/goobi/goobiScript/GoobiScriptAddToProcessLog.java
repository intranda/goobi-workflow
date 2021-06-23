package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

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
        addParameterToSampleCall(sb, "type", "info",
                "Define the type for the message here. Possible values are: `debug` `info` `warn` `error` `user`");
        addParameterToSampleCall(sb, "message", "This is my message",
                "This parameter allows to define the message itself that shall be added to the process log.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("message") == null || parameters.get("message").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "message");
            return new ArrayList<>();
        }

        if (parameters.get("type") == null || parameters.get("type").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "type");
            return new ArrayList<>();
        }
        if (!parameters.get("type").equals("debug") && !parameters.get("type").equals("info") && !parameters.get("type").equals("error")
                && !parameters.get("type").equals("warn") && !parameters.get("type").equals("user")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong parameter for type. Allowed values are: ", "error, warn, info, debug, user");
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
