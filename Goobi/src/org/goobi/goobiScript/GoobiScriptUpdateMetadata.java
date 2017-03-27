package org.goobi.goobiScript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.jdom2.JDOMException;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptUpdateMetadata extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptUpdateMetadata.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command);
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
					try {
		                String metdatdaPath = p.getMetadataFilePath();
		                String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
		                Path metadataFile = Paths.get(metdatdaPath);
		                Path anchorFile = Paths.get(anchorPath);
		                Map<String, List<String>> pairs = new HashMap<>();

		                pairs = HelperSchritte.extractMetadata(metadataFile, pairs);

		                if (Files.exists(anchorFile)) {
		                    pairs.putAll(HelperSchritte.extractMetadata(anchorFile, pairs));
		                }
		                MetadataManager.updateMetadata(p.getId(), pairs);
		                Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Metadata updated using GoobiScript.", username);
		                logger.info("Metadata updated using GoobiScript for process with ID " + p.getId());
						gsr.setResultMessage("Metadata updated successfully.");
						gsr.setResultType(GoobiScriptResultType.OK);
		            } catch (SwapException | DAOException | IOException | InterruptedException | JDOMException e1) {
		                logger.error("Problem while updating the metadata using GoobiScript for process with id: " + p.getId(), e1);
		                gsr.setResultMessage("Error while updating metadata: " + e1.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
		            }
				}
			}
		}
		
		
		
	}

}
