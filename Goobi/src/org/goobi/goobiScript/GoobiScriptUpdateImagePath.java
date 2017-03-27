package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;

public class GoobiScriptUpdateImagePath extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptUpdateImagePath.class);
	public final static String DIRECTORY_SUFFIX = "_tif";
	
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
		UpdateImagePathThread et = new UpdateImagePathThread();
		et.start();
	}

	class UpdateImagePathThread extends Thread {
		public void run() {
			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());

					try {
						Fileformat myRdf = p.readMetadataFile();
						UghHelper ughhelp = new UghHelper();
						MetadataType mdt = ughhelp.getMetadataType(p, "pathimagefiles");
						List<? extends ugh.dl.Metadata> alleImagepfade = myRdf.getDigitalDocument()
								.getPhysicalDocStruct().getAllMetadataByType(mdt);
						if (alleImagepfade.size() > 0) {
							for (Metadata md : alleImagepfade) {
								myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
							}
						}
						Metadata newmd = new Metadata(mdt);
						if (SystemUtils.IS_OS_WINDOWS) {
							newmd.setValue("file:/" + p.getImagesDirectory() + p.getTitel() + DIRECTORY_SUFFIX);
						} else {
							newmd.setValue("file://" + p.getImagesDirectory() + p.getTitel() + DIRECTORY_SUFFIX);
						}
						myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
						p.writeMetadataFile(myRdf);
						Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "ImagePath updated using GoobiScript.",username);
						logger.info("ImagePath updated using GoobiScript for process with ID " + p.getId());
						gsr.setResultMessage("ImagePath updated successfully.");
						gsr.setResultType(GoobiScriptResultType.OK);
					} catch (Exception e) {
						gsr.setResultMessage("ImagePath cannot be updated: " + e.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
					}
				}
			}
		}
	}

}
