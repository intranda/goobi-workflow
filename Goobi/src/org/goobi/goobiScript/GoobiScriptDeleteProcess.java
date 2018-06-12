package org.goobi.goobiScript;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptDeleteProcess extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptDeleteProcess.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("contentOnly") == null || parameters.get("contentOnly").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "contentOnly");
			return false;
		}

		if (!parameters.get("contentOnly").equals("true") && !parameters.get("contentOnly").equals("false")) {
			Helper.setFehlerMeldung("goobiScriptfield", "",
					"wrong parameter 'contentOnly'; possible values: true, false");
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
		TEMPLATEThread et = new TEMPLATEThread();
		et.start();
	}

	class TEMPLATEThread extends Thread {
		public void run() {

			boolean contentOnly = Boolean.parseBoolean(parameters.get("contentOnly"));

			// execute all jobs that are still in waiting state
			ArrayList<GoobiScriptResult> templist = new ArrayList<>(resultList);
            for (GoobiScriptResult gsr : templist) {
            		if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());
					gsr.setResultType(GoobiScriptResultType.RUNNING);
					gsr.updateTimestamp();
					if (contentOnly) {
						try {
							Path ocr = Paths.get(p.getOcrDirectory());
							if (Files.exists(ocr)) {
								NIOFileUtils.deleteDir(ocr);
							}
							Path images = Paths.get(p.getImagesDirectory());
							if (Files.exists(images)) {
								NIOFileUtils.deleteDir(images);
							}
							Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
									"Content deleted using GoobiScript.", username);
							logger.info("Content deleted using GoobiScript for process with ID " + gsr.getProcessId());
							gsr.setResultMessage("Content for process deleted successfully.");
							gsr.setResultType(GoobiScriptResultType.OK);
						} catch (Exception e) {
							Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
									"Problem occured while trying to delete content using GoobiScript.", username);
							logger.error("Content for process cannot be deleted using GoobiScript for process with ID "
									+ gsr.getProcessId());
							gsr.setResultMessage("Content for process cannot be deleted: " + e.getMessage());
							gsr.setResultType(GoobiScriptResultType.ERROR);
						}
					} else {
						try {
							NIOFileUtils.deleteDir(Paths.get(p.getProcessDataDirectory()));
							Path ocr = Paths.get(p.getOcrDirectory());
							if (Files.exists(ocr)) {
								NIOFileUtils.deleteDir(ocr);
							}
							ProcessManager.deleteProcess(p);
							logger.info("Process deleted using GoobiScript for process with ID " + gsr.getProcessId());
							gsr.setResultMessage("Process deleted successfully.");
							gsr.setResultType(GoobiScriptResultType.OK);
						} catch (Exception e) {
							Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Problem occured while trying to delete process using GoobiScript.", username);
							logger.error("Process cannot be deleted using GoobiScript for process with ID "+ gsr.getProcessId());
							gsr.setResultMessage("Process cannot be deleted: " + e.getMessage());
							gsr.setResultType(GoobiScriptResultType.ERROR);
						}
					}
					gsr.updateTimestamp();
				}
			}
		}
	}

}
