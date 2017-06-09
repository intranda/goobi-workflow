package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptCountImages extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptCountImages.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command, username);
			resultList.add(gsr);
		}
		return true;
	}

	@Override
	public void execute() {
		CountImagesThread et = new CountImagesThread();
		et.start();
	}

	class CountImagesThread extends Thread {
		
		public void run() {
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.updateTimestamp();
					
					if (p.getSortHelperImages() == 0) {
		                int value = HistoryManager.getNumberOfImages(p.getId());
		                if (value > 0) {
		                    ProcessManager.updateImages(value, p.getId());
		                    Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Image numbers counted using GoobiScript.", username);
		                    logger.info("Image numbers counted using GoobiScript for process with ID " + p.getId());
		                 }
		            }
					gsr.setResultMessage("Images counted successfully.");
					gsr.setResultType(GoobiScriptResultType.OK);
					gsr.updateTimestamp();
				}
			}
		}
	}

}
