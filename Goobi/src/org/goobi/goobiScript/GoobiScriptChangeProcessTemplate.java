package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptChangeProcessTemplate extends AbstractIGoobiScript implements IGoobiScript {

    private Process processTemplate;

    private BeanHelper helper;

    @Override
    public String getAction() {
        return "changeProcessTemplate";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to adapt the workflow for a process by switching to another process template.");
        addParameterToSampleCall(sb, "templateName", "Manuscript_workflow", "Use the name of the process template to use for the Goobi processes.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
        helper = new BeanHelper();
        if (StringUtils.isBlank(parameters.get("templateName"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "templateName");
            return new ArrayList<>();
        }

        // check if template exists
        Process template = ProcessManager.getProcessByExactTitle(parameters.get("templateName"));
        if (template == null) {
            Helper.setFehlerMeldung("goobiScriptfield", "Unknown process template: ", parameters.get("templateName"));
            return new ArrayList<>();
        }
        processTemplate = template;

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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        if (helper.changeProcessTemplate(p, processTemplate)) {
            gsr.setResultMessage("Changed template for process " + p.getTitel());
            gsr.setResultType(GoobiScriptResultType.OK);
        } else {
            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + p.getTitel());
            gsr.setResultMessage("Problem while changing template for process " + p.getTitel());
            gsr.setResultType(GoobiScriptResultType.ERROR);
        }

        gsr.updateTimestamp();
    }
}
