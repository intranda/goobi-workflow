package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.prefs.Preferences;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;

@Log4j2
public class GoobiScriptMetadataDelete extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "metadataDelete";
    }
    
    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to delete an existing metadata from the METS file.");
        addParameterToSampleCall(sb, "field", "Classification", "Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the translated display name (e.g. `Main title`) here.");
        addParameterToSampleCall(sb, "value", "Animals", "Define the value that the metadata shall have. Only if the value is the same the metadata will be deleted.");
        addParameterToSampleCall(sb, "position", "work", "Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any`");
        addParameterToSampleCall(sb, "ignoreValue", "false", "Set this parameter to `true` if the deletion of the metadata shall take place independent of the current metadata value. In this case all metadata that match the defined `field` will be deleted.");
        return sb.toString();
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // action:metadataDelete field:DocLanguage value:deutschTop position:top ignoreValue:true
        // action:metadataDelete field:DocLanguage value:deutschChild position:child

        if (parameters.get("field") == null || parameters.get("field").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "field");
            return false;
        }

        if (parameters.get("value") == null || parameters.get("value").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "value");
            return false;
        }

        if (parameters.get("position") == null || parameters.get("position").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "position");
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
        UpdateMetadataThread et = new UpdateMetadataThread();
        et.start();
    }

    class UpdateMetadataThread extends Thread {
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
            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    gsr.updateTimestamp();
                    try {
                        Fileformat ff = p.readMetadataFile();
                        DocStruct ds = ff.getDigitalDocument().getLogicalDocStruct();

                        // find the right elements to adapt
                        List<DocStruct> dsList = new ArrayList<DocStruct>();
                        switch (parameters.get("position")) {

                            // just the anchor element
                            case "top":
                                if (ds.getType().isAnchor()) {
                                    dsList.add(ds);
                                } else {
                                    gsr.setResultMessage("Error while adapting metadata for top element, as top element is no anchor");
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                    continue;
                                }
                                break;

                            // fist the first child element
                            case "child":
                                if (ds.getType().isAnchor()) {
                                    dsList.add(ds.getAllChildren().get(0));
                                } else {
                                    gsr.setResultMessage("Error while adapting metadata for child, as top element is no anchor");
                                    gsr.setResultType(GoobiScriptResultType.ERROR);
                                    continue;
                                }
                                break;

                            // any element in the hierarchy
                            case "any":
                                dsList.add(ds);
                                dsList.addAll(ds.getAllChildrenAsFlatList());
                                break;

                            // default "work", which is the first child or the main top element if it is not an anchor
                            default:
                                if (ds.getType().isAnchor()) {
                                    dsList.add(ds.getAllChildren().get(0));
                                } else {
                                    dsList.add(ds);
                                }
                                break;
                        }

                        // check if values shall be ignored
                        boolean ignoreValue = getParameterAsBoolean("ignoreValue");

                        // now find the metadata field to delete
                        deleteMetadata(dsList, parameters.get("field"), parameters.get("value"), ignoreValue, p.getRegelsatz().getPreferences());
                        p.writeMetadataFile(ff);
                        Thread.sleep(2000);
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                "Metadata deleted using GoobiScript: " + parameters.get("field") + " - " + parameters.get("value"), username);
                        log.info("Metadata deleted using GoobiScript for process with ID " + p.getId());
                        gsr.setResultMessage("Metadata deleted successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e1) {
                        log.error("Problem while deleting the metadata using GoobiScript for process with id: " + p.getId(), e1);
                        gsr.setResultMessage("Error while deleting metadata: " + e1.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e1.getMessage());
                    }
                    gsr.updateTimestamp();
                }
            }
        }

        /**
         * Method to delete a given metadata from a {@link DocStruct}
         * 
         * @param dsList as List of structural elements to use
         * @param field the metadata field that is used
         * @param value the metadata value to be deleted
         * @param ignoreValue a boolean that defines if the value of the metadata shall not be checked before deletion
         * @param prefs the {@link Preferences} to use
         */
        private void deleteMetadata(List<DocStruct> dsList, String field, String value, boolean ignoreValue, Prefs prefs) {
            for (DocStruct ds : dsList) {
                List<? extends Metadata> mdlist = ds.getAllMetadataByType(prefs.getMetadataTypeByName(field));
                if (mdlist != null && mdlist.size() > 0) {
                    for (Metadata md : mdlist) {
                        if (ignoreValue || md.getValue().equals(value)) {
                            ds.getAllMetadata().remove(md);
                        }
                    }
                }                
            }
        }

    }

}
