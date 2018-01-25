package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;

public class GoobiScriptUpdateHistory extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptUpdateHistory.class);
	
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
		UpdateHistoryThread et = new UpdateHistoryThread();
		et.start();
	}

	class UpdateHistoryThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.setResultType(GoobiScriptResultType.RUNNING);
					gsr.updateTimestamp();
					try {
						boolean result = HistoryAnalyserJob.updateHistoryForProzess(p);
						if (result) {
							Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "History updated using GoobiScript.", username);
							logger.info("History updated using GoobiScript for process with ID " + p.getId());
							gsr.setResultMessage("History updated successfully.");
							gsr.setResultType(GoobiScriptResultType.OK);
						} else {
							Helper.addMessageToProcessLog(p.getId(), LogType.ERROR, "History not successfully updated using GoobiScript.", username);
							logger.info("History could not be updated using GoobiScript for process with ID " + p.getId());
							gsr.setResultMessage("History update was not successful.");
							gsr.setResultType(GoobiScriptResultType.ERROR);
						}
					} catch (Exception e) {
						gsr.setResultMessage("History cannot be updated: " + e.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
					}
					gsr.updateTimestamp();
				}
			}
		}
	}

}
