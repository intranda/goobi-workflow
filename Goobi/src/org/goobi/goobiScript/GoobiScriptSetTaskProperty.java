package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptSetTaskProperty extends AbstractIGoobiScript implements IGoobiScript {
    private static final Logger logger = Logger.getLogger(GoobiScriptSetTaskProperty.class);

    private String property;
    private String value;

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return false;
        }

        if (parameters.get("property") == null || parameters.get("property").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
            return false;
        }

        if (parameters.get("value") == null || parameters.get("value").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
            return false;
        }

        property = parameters.get("property");
        value = parameters.get("value");

        switch (property) {
            case "metadata":
            case "readimages":
            case "writeimages":
            case "validate":
            case "exportdms":
            case "batch":
            case "automatic":
            case "importfileupload":
            case "acceptandclose":
            case "acceptmoduleandclose":
            case "script":
            case "delay":
            case "updatemetadataindex":
            case "generatedocket":
                break;
            default:
                Helper.setFehlerMeldung("goobiScriptfield", "",
                        "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms, batch, importfileupload, "
                                + "acceptandclose, acceptmoduleandclose, script, delay, updatemetadataindex, generatedocket");
                return false;
        }
        //        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
        //                && !property.equals("exportdms") && !property.equals("batch") && !property.equals("automatic")) {
        //            Helper.setFehlerMeldung("goobiScriptfield", "",
        //                    "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
        //            return false;
        //        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "wrong parameter 'value'; possible values: true, false");
            return false;
        }

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        TEMPLATEThread et = new TEMPLATEThread();
        et.start();
    }

    class TEMPLATEThread extends Thread {
        public void run() {
            // execute all jobs that are still in waiting state
            ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();

                    if (p.getSchritte() != null) {
                        for (Iterator<Step> iterator = p.getSchritte().iterator(); iterator.hasNext();) {
                            Step s = iterator.next();
                            if (s.getTitel().equals(parameters.get("steptitle"))) {

                                if (property.equals("metadata")) {
                                    s.setTypMetadaten(Boolean.parseBoolean(value));
                                }
                                if (property.equals("automatic")) {
                                    s.setTypAutomatisch(Boolean.parseBoolean(value));
                                }
                                if (property.equals("batch")) {
                                    s.setBatchStep(Boolean.parseBoolean(value));
                                }
                                if (property.equals("readimages")) {
                                    s.setTypImagesLesen(Boolean.parseBoolean(value));
                                }
                                if (property.equals("writeimages")) {
                                    s.setTypImagesSchreiben(Boolean.parseBoolean(value));
                                }
                                if (property.equals("validate")) {
                                    s.setTypBeimAbschliessenVerifizieren(Boolean.parseBoolean(value));
                                }
                                if (property.equals("exportdms")) {
                                    s.setTypExportDMS(Boolean.parseBoolean(value));
                                }

                                if (property.equalsIgnoreCase("ImportFileUpload")) {
                                    s.setTypImportFileUpload(Boolean.parseBoolean(value));
                                }
                                if (property.equalsIgnoreCase("")) {
                                }
                                if (property.equalsIgnoreCase("acceptandclose")) {
                                    s.setTypBeimAnnehmenAbschliessen(Boolean.parseBoolean(value));
                                }
                                if (property.equalsIgnoreCase("acceptmoduleandclose")) {
                                    s.setTypBeimAnnehmenModulUndAbschliessen(Boolean.parseBoolean(value));
                                }

                                if (property.equalsIgnoreCase("script")) {
                                    s.setTypScriptStep(Boolean.parseBoolean(value));
                                }
                                if (property.equalsIgnoreCase("delay")) {
                                    s.setDelayStep(Boolean.parseBoolean(value));
                                }

                                if (property.equalsIgnoreCase("updatemetadataindex")) {
                                    s.setUpdateMetadataIndex(Boolean.parseBoolean(value));
                                }

                                if (property.equalsIgnoreCase("generatedocket")) {
                                    s.setGenerateDocket(Boolean.parseBoolean(value));
                                }

                                try {
                                    ProcessManager.saveProcess(p);
                                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Changed property '" + property + "' to '" + value
                                            + "' for step '" + s.getTitel() + "' using GoobiScript.", username);
                                    logger.info("Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel()
                                            + "' using GoobiScript for process with ID " + p.getId());
                                    gsr.setResultMessage("Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel()
                                            + "' successfully.");
                                    gsr.setResultType(GoobiScriptResultType.OK);
                                } catch (DAOException e) {
                                    logger.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                                    gsr.setResultMessage("Error while chaning property '" + property + "' to '" + value + "' for step '" + s
                                            .getTitel() + "'.");
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                }
                                break;
                            }
                        }
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
