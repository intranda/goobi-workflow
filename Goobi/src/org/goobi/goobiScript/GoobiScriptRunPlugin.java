package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptRunPlugin extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "runPlugin";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to run a plugin that is assigned to a workflow step.");
        addParameterToSampleCall(sb, "steptitle", "Catalogue update",
                "Title of the workflow step which has a plugin assigned that shall be executed");
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

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        for (Step step : p.getSchritteList()) {
            if (step.getTitel().equalsIgnoreCase(steptitle)) {
                Step so = StepManager.getStepById(step.getId());

                if (so.getStepPlugin() != null && !so.getStepPlugin().isEmpty()) {
                    IStepPlugin myPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, so.getStepPlugin());

                    if (myPlugin != null) {
                        myPlugin.initialize(so, "");

                        if (myPlugin.getPluginGuiType() == PluginGuiType.NONE) {
                            myPlugin.execute();
                            myPlugin.finish();

                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                    "Plugin for step '" + steptitle + "' executed using GoobiScript.", username);
                            log.info("Plugin for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("Plugin for step '" + steptitle + "' executed successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } else {
                            gsr.setResultMessage("Plugin for step '" + steptitle + "' cannot be executed as it has a GUI.");
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                    }
                } else {
                    gsr.setResultMessage("Plugin for step '" + steptitle + "' cannot be executed as it is not Plugin workflow step.");
                    gsr.setResultType(GoobiScriptResultType.ERROR);
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
