package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddPluginToStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "addPluginToStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a plugin to a defined workflow step");
        addParameterToSampleCall(sb, "steptitle", "TITLE_STEP", "Title of the step to adapt");
        addParameterToSampleCall(sb, "plugin", "PLUGIN_NAME", "Name of the plugin to be assigned to the workflow step");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }

        if (parameters.get("plugin") == null || parameters.get("plugin").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "plugin");
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
