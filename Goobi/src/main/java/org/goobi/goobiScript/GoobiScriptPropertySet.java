package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;

public class GoobiScriptPropertySet extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "propertySet";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to set a process property to a specific value.");
        addParameterToSampleCall(sb, "name", "Opening angle",
                "Name of the property to be changed. If the property does not exist already it is created here.");
        addParameterToSampleCall(sb, "value", "90°", "Value that the property shall be set to");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("name"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "name");
            return new ArrayList<>();
        }

        if (StringUtils.isBlank(parameters.get("value"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
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
        String propertyName = parameters.get("name");
        String value = parameters.get("value");

        // execute all jobs that are still in waiting state
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        boolean matched = false;
        for (Processproperty pp : p.getEigenschaften()) {
            if (pp.getTitel().equals(propertyName)) {
                pp.setWert(value);
                PropertyManager.saveProcessProperty(pp);
                gsr.setResultMessage("Property updated.");
                gsr.setResultType(GoobiScriptResultType.OK);
                matched = true;
                break;
            }
        }
        if (!matched) {
            Processproperty pp = new Processproperty();
            pp.setTitel(propertyName);
            pp.setWert(value);
            pp.setProzess(p);
            PropertyManager.saveProcessProperty(pp);
            gsr.setResultMessage("Property created.");
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
