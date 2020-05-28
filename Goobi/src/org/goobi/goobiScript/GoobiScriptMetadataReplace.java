package org.goobi.goobiScript;

import java.util.HashMap;
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
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public class GoobiScriptMetadataReplace extends AbstractIGoobiScript implements IGoobiScript {

    // action:metadataReplace field:DocLanguage search:deutschTop replace:deutschNewTop position:top
    // action:metadataReplace field:DocLanguage search:deutschChild replace:deutschNewChild position:child

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("field") == null || parameters.get("field").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "field");
            return false;
        }

        if (parameters.get("search") == null || parameters.get("search").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "search");
            return false;
        }

        //		if (parameters.get("replace") == null || parameters.get("replace").equals("")) {
        //            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "replace");
        //            return false;
        //        }

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

                        String replace = parameters.get("replace");
                        if (replace == null) {
                            replace = "";
                        }

                        // now change the searched metadata and save the file
                        replaceMetadata(ds, parameters.get("field"), parameters.get("search"), replace, p.getRegelsatz().getPreferences());
                        p.writeMetadataFile(ff);
                        Thread.sleep(2000);
                        Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
                                "Metadata changed using GoobiScript: " + parameters.get("field") + " - " + parameters.get("value"), username);
                        log.info("Metadata changed using GoobiScript for process with ID " + p.getId());
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
         * Method to replace a string in a given metadata from a {@link DocStruct}
         * 
         * @param ds the structural element to use
         * @param field the metadata field that is used
         * @param search a partial string to be replaced
         * @param replace the replacement to be used
         * @param prefs the {@link Preferences} to use
         * 
         * @throws MetadataTypeNotAllowedException
         */
        private void replaceMetadata(DocStruct ds, String field, String search, String replace, Prefs prefs) throws MetadataTypeNotAllowedException {
            List<? extends Metadata> mdlist = ds.getAllMetadataByType(prefs.getMetadataTypeByName(field));
            if (mdlist != null && mdlist.size() > 0) {
                for (Metadata md : mdlist) {
                    if (md.getValue().contains(search)) {
                        md.setValue(md.getValue().replaceAll(search, replace));
                    }
                }
            }
        }

    }

}
