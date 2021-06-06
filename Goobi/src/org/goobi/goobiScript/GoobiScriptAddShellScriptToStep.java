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
public class GoobiScriptAddShellScriptToStep extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "addShellScriptToStep";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a shell script to an existing workflow step.");
        addParameterToSampleCall(sb, "steptitle", "Generate MD5 Hashes", "This is the title of the workflow step to be used.");
        addParameterToSampleCall(sb, "label", "Hash generation",
                "Define a label for the script that shall be visible for that script inside of an accepted task.");
        addParameterToSampleCall(sb, "script", "/bin/bash /path/to/script.sh \"parameter with blanks\"",
                "Define the script that shall be added here.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "steptitle");
            return new ArrayList<>();
        }

        if (parameters.get("label") == null || parameters.get("label").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "label");
            return new ArrayList<>();
        }

        if (parameters.get("script") == null || parameters.get("script").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Fehlender Parameter: ", "script");
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
                    s.setTypAutomatischScriptpfad(parameters.get("script"));
                    s.setScriptname1(parameters.get("label"));
                    s.setTypScriptStep(true);
                    try {
                        ProcessManager.saveProcess(p);
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Added script to step '" + s.getTitel() + "' with label '"
                                + s.getScriptname1() + "' and value '" + s.getTypAutomatischScriptpfad() + "' using GoobiScript.",
                                username);
                        log.info("Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1() + "' and value '"
                                + s.getTypAutomatischScriptpfad() + "' using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage("Added script to step '" + s.getTitel() + "' with label '" + s.getScriptname1()
                                + "' and value '" + s.getTypAutomatischScriptpfad() + "'.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (DAOException e) {
                        Helper.setFehlerMeldung("goobiScriptfield", "Error while saving process: " + p.getTitel(), e);
                        log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                        gsr.setResultMessage("Error while adding script to step '" + s.getTitel() + "' with label '" + s.getScriptname1()
                                + "' and value '" + s.getTypAutomatischScriptpfad() + "': " + e.getMessage());
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
