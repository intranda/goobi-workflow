package org.goobi.goobiScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class GoobiScriptMetadataAdd extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptMetadataAdd.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);
		
		// action:metadataAdd field:DocLanguage value:deutschTop position:top
		// action:metadataAdd field:DocLanguage value:deutschChild position:child
		
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
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
			resultList.add(gsr);
		}
		return true;
	}

	@Override
	public void execute() {
		UpdateMetadataThread et = new UpdateMetadataThread();
		et.start();
	}

	class UpdateMetadataThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
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
						if (position.equals("child")){
							if (ds.getType().isAnchor()) {
								ds = ff.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
							} else{
								gsr.setResultMessage("Error while adding metadata to child, as topstruct is no anchor");
								gsr.setResultType(GoobiScriptResultType.ERROR);
								continue;
							}
						}
						
						// now add the new metadata and save the file
						addMetadata(ds, parameters.get("field"), parameters.get("value"), p.getRegelsatz().getPreferences());
						p.writeMetadataFile(ff);
						Thread.sleep(2000);
						Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Metadata added using GoobiScript: " + parameters.get("field") + " - " + parameters.get("value"), username);
		                logger.info("Metadata added using GoobiScript for process with ID " + p.getId());
						gsr.setResultMessage("Metadata added successfully.");
						gsr.setResultType(GoobiScriptResultType.OK);
		            } catch (SwapException | DAOException | IOException | InterruptedException | MetadataTypeNotAllowedException | ReadException | PreferencesException | WriteException e1) {
		                logger.error("Problem while adding the metadata using GoobiScript for process with id: " + p.getId(), e1);
		                gsr.setResultMessage("Error while adding metadata: " + e1.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
		            }
					gsr.updateTimestamp();
				}
			}
		}
		
		/**
		 * Method to add a specific metadata to a given structural element
		 * 
		 * @param ds structural element to use
		 * @param field the metadata field to create
		 * @param prefs the {@link Preferences} to use
		 * @param value the information the shall be stored as metadata in the given field
		 * 
		 * @throws MetadataTypeNotAllowedException
		 */
		private void addMetadata(DocStruct ds, String field, String value, Prefs prefs) throws MetadataTypeNotAllowedException{
			Metadata mdColl = new Metadata(prefs.getMetadataTypeByName(field));
			mdColl.setValue(value);
			ds.addMetadata(mdColl);
		}
		
	}

}
