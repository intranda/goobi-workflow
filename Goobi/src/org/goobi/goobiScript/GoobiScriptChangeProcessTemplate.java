package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);
        helper = new BeanHelper();
        if (StringUtils.isBlank(parameters.get("templateName"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "templateName");
            return false;
        }

        // check if template exists
        Process template = ProcessManager.getProcessByExactTitle(parameters.get("templateName"));
        if (template == null) {
            Helper.setFehlerMeldung("goobiScriptfield", "Unknown process template: ", parameters.get("templateName"));
            return false;
        }
        processTemplate = template;

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
            resultList.add(gsr);
        }

        return true;

    }

    @Override
    public void execute() {
        ChangeProcessTemplateThread thread = new ChangeProcessTemplateThread();
        thread.run();

    }

    class ChangeProcessTemplateThread extends Thread {
        @Override
        public void run() {
            ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
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

                }
                gsr.updateTimestamp();

            }
        }

    }
}
