package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public class GoobiScriptMetadataTypeChange extends AbstractIGoobiScript implements IGoobiScript {
    // action:metadataTypeChange position:work oldMetadata:singleDigCollection newMetadata:DDC

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("oldType"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "oldType");
            return false;
        }

        if (StringUtils.isBlank(parameters.get("newType"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "newType");
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
                        // first get the top element
                        DocStruct ds = ff.getDigitalDocument().getLogicalDocStruct();

                        // if child element shall be updated get this
                        String position = parameters.get("position");
                        if (position.equals("child")) {
                            if (ds.getType().isAnchor()) {
                                ds = ds.getAllChildren().get(0);
                            } else {
                                gsr.setResultMessage("Error while adding metadata to child, as topstruct is no anchor");
                                gsr.setResultType(GoobiScriptResultType.ERROR);
                                continue;
                            }
                        } else if (position.equals("work")) {
                            if (ds.getType().isAnchor()) {
                                ds = ds.getAllChildren().get(0);
                            }
                        }

                        String oldMetadataType = parameters.get("oldType");
                        String newMetadataType = parameters.get("newType");

                        boolean changed = changeMetadataType(ds, oldMetadataType, newMetadataType, p.getRegelsatz().getPreferences());
                        if (changed) {
                            p.writeMetadataFile(ff);
                            Thread.sleep(2000);
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                    "Metadata type changed using GoobiScript: from " + oldMetadataType + " to " + newMetadataType, username);
                        }
                        log.info("Metadata type changed using GoobiScript for process with ID " + p.getId());

                        gsr.setResultMessage("Metadata changed successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e1) {
                        log.error("Problem while changing the metadata using GoobiScript for process with id: " + p.getId(), e1);
                        gsr.setResultMessage("Error while changing metadata: " + e1.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e1.getMessage());
                    }

                    gsr.updateTimestamp();
                }
            }
        }

        /**
         * Change the type of all occurrences of a metadata
         * @param ds docstruct
         * @param oldMetadataType old type
         * @param newMetadataType new type
         * @param prefs ruleset
         * @return true, if the type was changed, false otherwise
         * @throws MetadataTypeNotAllowedException if the new type does not exist or is not allowed
         */

        private boolean changeMetadataType(DocStruct ds, String oldMetadataType, String newMetadataType, Prefs prefs)
                throws MetadataTypeNotAllowedException {

            // search for all metadata with type of oldMetadataType

            List<? extends Metadata> mdList = ds.getAllMetadataByType(prefs.getMetadataTypeByName(oldMetadataType));

            if (mdList == null || mdList.isEmpty()) {
                return false;
            }
            MetadataType type = prefs.getMetadataTypeByName(newMetadataType);

            // for each metadata create new metadata with new type
            for (Metadata oldMd : mdList) {
                Metadata newMd = new Metadata(type);
                // copy value from existing metadata
                newMd.setValue(oldMd.getValue());
                // add all new metadata
                ds.addMetadata(newMd);
                // delete oldMetadata from ds
                ds.removeMetadata(oldMd);
            }
            return true;
        }
    }

}
