package org.goobi.goobiScript;

import java.io.IOException;
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

public class GoobiScriptMetadataReplace extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptMetadataReplace.class);

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
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING) {
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
						
						String replace = parameters.get("replace");
						if (replace==null){
							replace = "";
						}
						
						// now change the searched metadata and save the file
						replaceMetadata(ds, parameters.get("field"), parameters.get("search"), replace, p.getRegelsatz().getPreferences());
						p.writeMetadataFile(ff);
						
		                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Metadata deleted using GoobiScript: " + parameters.get("field") + " - " + parameters.get("value"), username);
		                logger.info("Metadata changed using GoobiScript for process with ID " + p.getId());
						gsr.setResultMessage("Metadata changed successfully.");
						gsr.setResultType(GoobiScriptResultType.OK);
		            } catch (SwapException | DAOException | IOException | InterruptedException | ReadException | MetadataTypeNotAllowedException | PreferencesException | WriteException e1) {
		                logger.error("Problem while changing the metadata using GoobiScript for process with id: " + p.getId(), e1);
		                gsr.setResultMessage("Error while changing metadata: " + e1.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
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
		private void replaceMetadata(DocStruct ds, String field, String search, String replace, Prefs prefs) throws MetadataTypeNotAllowedException{
			List<? extends Metadata> mdlist = ds.getAllMetadataByType(prefs.getMetadataTypeByName(field));
			if (mdlist != null && mdlist.size()>0){
				for (Metadata md : mdlist) {
					if (md.getValue().contains(search)){
						md.setValue(md.getValue().replaceAll(search, replace));
					}
				}
			}
		}
		
		
		
	}

}
