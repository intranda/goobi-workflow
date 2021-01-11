package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptSetStepNumber extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewAction(sb, "setStepNumber", "comment");
        addParameter(sb, "steptitle", "def", "ghi");
        addParameter(sb, "number", "def", "ghi");
        return sb.toString();

//        return "---\\naction: setStepNumber\\nsteptitle: TITLE_STEP\\nnumber: NUMBER_1_TO_?";
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("number") == null || parameters.get("number").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "number");
            return false;
        }

        if (!StringUtils.isNumeric(parameters.get("number"))) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong number parameter", "(only numbers allowed)");
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
            } // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();

                    for (Iterator<Step> iterator = p.getSchritteList().iterator(); iterator.hasNext();) {
                        Step s = iterator.next();
                        if (s.getTitel().equals(parameters.get("steptitle"))) {
                            s.setReihenfolge(Integer.parseInt(parameters.get("number")));
                            try {
                                StepManager.saveStep(s);
                                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                        "Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' using GoobiScript.",
                                        username);
                                log.info("Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge()
                                + "' using GoobiScript for process with ID " + p.getId());
                                gsr.setResultMessage(
                                        "Changed order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "' successfully.");
                                gsr.setResultType(GoobiScriptResultType.OK);
                            } catch (DAOException e) {
                                log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                                gsr.setResultMessage(
                                        "Error while changing the order number of step '" + s.getTitel() + "' to '" + s.getReihenfolge() + "'.");
                                gsr.setResultType(GoobiScriptResultType.ERROR);
                                gsr.setErrorText(e.getMessage());
                            }
                            break;
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
}
