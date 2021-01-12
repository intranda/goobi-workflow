package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
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
        addNewAction(sb, "This GoobiScript allow to adapt the workflow for a process by switching to another process template.");
        addParameter(sb, "templateName", "Manuscript_workflow", "Use the name of the process template to use for the Goobi processes.");
        return sb.toString();
    }
    
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
        ChangeProcessTemplateThread thread = new ChangeProcessTemplateThread();
        thread.run();

    }

    class ChangeProcessTemplateThread extends Thread {
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

            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
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
