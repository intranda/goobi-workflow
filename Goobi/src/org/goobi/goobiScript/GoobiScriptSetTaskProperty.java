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
public class GoobiScriptSetTaskProperty extends AbstractIGoobiScript implements IGoobiScript {
    private String property;
    private String value;

    @Override
    public String getAction() {
        return "setTaskProperty";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allow to configure a specific workflow step (e.g. to work as metadata edition step, to automatically run a plugin).");
        addParameterToSampleCall(sb, "steptitle", "Metadata edition", "Title of the workflow step to configure");
        addParameterToSampleCall(sb, "property", "metadata",
                "Name of the property to be changed. Possible values are: \\n# `metadata` `readimages` `writeimages` `validate` `exportdms` `automatic` `batch` `importfileupload` \\n# `acceptandclose` `acceptmoduleandclose` `script` `delay` `updatemetadataindex` `generatedocket`");
        addParameterToSampleCall(sb, "value", "true", "Value that the property shall have (e.g. `true` or `false`)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */
        if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
            return new ArrayList<>();
        }

        if (parameters.get("property") == null || parameters.get("property").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "property");
            return new ArrayList<>();
        }

        if (parameters.get("value") == null || parameters.get("value").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "value");
            return new ArrayList<>();
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
                return new ArrayList<>();
        }
        //        if (!property.equals("metadata") && !property.equals("readimages") && !property.equals("writeimages") && !property.equals("validate")
        //                && !property.equals("exportdms") && !property.equals("batch") && !property.equals("automatic")) {
        //            Helper.setFehlerMeldung("goobiScriptfield", "",
        //                    "wrong parameter 'property'; possible values: metadata, readimages, writeimages, validate, exportdms");
        //            return new ArrayList<>();
        //        }

        if (!value.equals("true") && !value.equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "wrong parameter 'value'; possible values: true, false");
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
                        log.info("Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel()
                                + "' using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage(
                                "Changed property '" + property + "' to '" + value + "' for step '" + s.getTitel() + "' successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (DAOException e) {
                        log.error("goobiScriptfield" + "Error while saving process: " + p.getTitel(), e);
                        gsr.setResultMessage(
                                "Error while chaning property '" + property + "' to '" + value + "' for step '" + s.getTitel() + "'.");
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
