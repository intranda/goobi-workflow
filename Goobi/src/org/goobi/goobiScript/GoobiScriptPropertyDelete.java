package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.enums.GoobiScriptResultType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptPropertyDelete extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("name"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "name");
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
        TEMPLATEThread et = new TEMPLATEThread();
        et.start();
    }

    class TEMPLATEThread extends Thread {
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
            String propertyName = parameters.get("name");

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();

                    for (Processproperty pp : p.getEigenschaften()) {
                        if (pp.getTitel().equals(propertyName)) {
                            PropertyManager.deleteProcessProperty(pp);
                            gsr.setResultMessage("Property deleted.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                            break;
                        }
                    }
                    if (gsr.getResultType().equals(GoobiScriptResultType.RUNNING)) {
                        gsr.setResultType(GoobiScriptResultType.OK);
                        gsr.setResultMessage("Property not found: " + propertyName);
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }
}
