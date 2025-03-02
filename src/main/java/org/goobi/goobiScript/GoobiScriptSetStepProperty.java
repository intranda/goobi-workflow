/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.goobiScript;

import java.util.ArrayList;
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
public class GoobiScriptSetStepProperty extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String STEPTITLE = "steptitle";
    private static final String PROPERTY = "property";
    private static final String VALUE = "value";

    private static final String PROPERTY_METADATA = "metadata";
    private static final String PROPERTY_READ_IMAGES = "readimages";
    private static final String PROPERTY_WRITE_IMAGES = "writeimages";
    private static final String PROPERTY_VALIDATE = "validate";
    private static final String PROPERTY_EXPORT_DMS = "exportdms";
    private static final String PROPERTY_BATCH = "batch";
    private static final String PROPERTY_AUTOMATIC = "automatic";
    private static final String PROPERTY_IMPORT_FILE_UPLOAD = "importfileupload";
    private static final String PROPERTY_ACCEPT_AND_CLOSE = "acceptandclose";
    private static final String PROPERTY_ACCEPT_MODULE_AND_CLOSE = "acceptmoduleandclose";
    private static final String PROPERTY_SCRIPT = "script";
    private static final String PROPERTY_DELAY = "delay";
    private static final String PROPERTY_UPDATE_METADATA_INDEX = "updatemetadataindex";
    private static final String PROPERTY_GENERATE_DOCKET = "generatedocket";

    @Override
    public String getAction() {
        return "setStepProperty";
    }

    @Override
    public String getSampleCall() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("`" + PROPERTY_METADATA + "` ");
        buffer.append("`" + PROPERTY_READ_IMAGES + "` ");
        buffer.append("`" + PROPERTY_WRITE_IMAGES + "` ");
        buffer.append("`" + PROPERTY_VALIDATE + "` ");
        buffer.append("`" + PROPERTY_EXPORT_DMS + "` ");
        buffer.append("`" + PROPERTY_BATCH + "` ");
        buffer.append("`" + PROPERTY_AUTOMATIC + "` ");
        buffer.append("`" + PROPERTY_IMPORT_FILE_UPLOAD + "` \\n#");// newline because YAML line gets too long otherwise, '#' is the comment char
        buffer.append("`" + PROPERTY_ACCEPT_AND_CLOSE + "` ");
        buffer.append("`" + PROPERTY_ACCEPT_MODULE_AND_CLOSE + "` ");
        buffer.append("`" + PROPERTY_SCRIPT + "` ");
        buffer.append("`" + PROPERTY_DELAY + "` ");
        buffer.append("`" + PROPERTY_UPDATE_METADATA_INDEX + "` ");
        buffer.append("`" + PROPERTY_GENERATE_DOCKET + "`");

        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allow to configure a specific workflow step (e.g. to work as metadata edition step, to automatically run a plugin).");
        addParameterToSampleCall(sb, STEPTITLE, "Metadata edition", "Title of the workflow step to configure");
        addParameterToSampleCall(sb, PROPERTY, PROPERTY_METADATA,
                "Name of the property to be changed. Possible values are: \\n# " + buffer.toString());
        addParameterToSampleCall(sb, VALUE, "true", "Value that the property shall have (e.g. `true` or `false`)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        /*
         * -------------------------------- Validierung der Actionparameter --------------------------------
         */

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(missingParameter, STEPTITLE);
            return new ArrayList<>();
        }

        String property = parameters.get(PROPERTY);
        if (property == null || "".equals(property)) {
            Helper.setFehlerMeldung(missingParameter, PROPERTY);
            return new ArrayList<>();
        }

        String value = parameters.get(VALUE);
        if (value == null || "".equals(value)) {
            Helper.setFehlerMeldung(missingParameter, VALUE);
            return new ArrayList<>();
        }

        switch (property) {
            case PROPERTY_METADATA:
            case PROPERTY_READ_IMAGES:
            case PROPERTY_WRITE_IMAGES:
            case PROPERTY_VALIDATE:
            case PROPERTY_EXPORT_DMS:
            case PROPERTY_BATCH:
            case PROPERTY_AUTOMATIC:
            case PROPERTY_IMPORT_FILE_UPLOAD:
            case PROPERTY_ACCEPT_AND_CLOSE:
            case PROPERTY_ACCEPT_MODULE_AND_CLOSE:
            case PROPERTY_SCRIPT:
            case PROPERTY_DELAY:
            case PROPERTY_UPDATE_METADATA_INDEX:
            case PROPERTY_GENERATE_DOCKET:
                break;
            default:
                StringBuilder buffer = new StringBuilder();
                buffer.append(PROPERTY_METADATA + ", ");
                buffer.append(PROPERTY_READ_IMAGES + ", ");
                buffer.append(PROPERTY_WRITE_IMAGES + ", ");
                buffer.append(PROPERTY_VALIDATE + ", ");
                buffer.append(PROPERTY_EXPORT_DMS + ", ");
                buffer.append(PROPERTY_BATCH + ", ");
                buffer.append(PROPERTY_AUTOMATIC + ", ");
                buffer.append(PROPERTY_IMPORT_FILE_UPLOAD + ", ");
                buffer.append(PROPERTY_ACCEPT_AND_CLOSE + ", ");
                buffer.append(PROPERTY_ACCEPT_MODULE_AND_CLOSE + ", ");
                buffer.append(PROPERTY_SCRIPT + ", ");
                buffer.append(PROPERTY_DELAY + ", ");
                buffer.append(PROPERTY_UPDATE_METADATA_INDEX + ", ");
                buffer.append(PROPERTY_GENERATE_DOCKET);
                Helper.setFehlerMeldung("", "wrong parameter 'property'; possible values: " + buffer.toString());
                return new ArrayList<>();
        }

        if (!"true".equals(value) && !"false".equals(value)) {
            Helper.setFehlerMeldung("", "wrong parameter 'value'; possible values: true, false");
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
        String steptitle = parameters.get(STEPTITLE);
        String property = parameters.get(PROPERTY);
        String value = parameters.get(VALUE);

        if (p.getSchritte() != null) {
            for (Step s : p.getSchritte()) {
                if (s.getTitel().equals(steptitle)) {
                    if (PROPERTY_METADATA.equals(property)) {
                        s.setTypMetadaten(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_READ_IMAGES.equals(property)) {
                        s.setTypImagesLesen(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_WRITE_IMAGES.equals(property)) {
                        s.setTypImagesSchreiben(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_VALIDATE.equals(property)) {
                        s.setTypBeimAbschliessenVerifizieren(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_EXPORT_DMS.equals(property)) {
                        s.setTypExportDMS(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_BATCH.equals(property)) {
                        s.setBatchStep(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_AUTOMATIC.equals(property)) {
                        s.setTypAutomatisch(Boolean.parseBoolean(value));
                    }

                    if (PROPERTY_IMPORT_FILE_UPLOAD.equalsIgnoreCase(property)) {
                        s.setTypImportFileUpload(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_ACCEPT_AND_CLOSE.equalsIgnoreCase(property)) {
                        s.setTypBeimAnnehmenAbschliessen(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_ACCEPT_MODULE_AND_CLOSE.equalsIgnoreCase(property)) {
                        s.setTypBeimAnnehmenModulUndAbschliessen(Boolean.parseBoolean(value));
                    }

                    if (PROPERTY_SCRIPT.equalsIgnoreCase(property)) {
                        s.setTypScriptStep(Boolean.parseBoolean(value));
                    }
                    if (PROPERTY_DELAY.equalsIgnoreCase(property)) {
                        s.setDelayStep(Boolean.parseBoolean(value));
                    }

                    if (PROPERTY_UPDATE_METADATA_INDEX.equalsIgnoreCase(property)) {
                        s.setUpdateMetadataIndex(Boolean.parseBoolean(value));
                    }

                    if (PROPERTY_GENERATE_DOCKET.equalsIgnoreCase(property)) {
                        s.setGenerateDocket(Boolean.parseBoolean(value));
                    }

                    String info = "'" + property + "' to '" + value + "' for step '" + s.getTitel() + "'";
                    try {
                        ProcessManager.saveProcess(p);
                        String message = "Changed property " + info;
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, message + " using GoobiScript.", username);
                        log.info(message + " using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage(message + " successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (DAOException e) {
                        log.error(GOOBI_SCRIPTFIELD + " Error while saving process: " + p.getTitel(), e);
                        gsr.setResultMessage("Error while chaning property " + info + ".");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                    break;
                }
            }
        }
        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + steptitle);
        }
        gsr.updateTimestamp();
    }
}
