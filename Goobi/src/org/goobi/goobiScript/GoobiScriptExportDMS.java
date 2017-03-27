package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;

import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptExportDMS extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptExportDMS.class);

	@Override
	public void prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command);
			resultList.add(gsr);
		}
	}

	@Override
	public void execute() {
		LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
		String user = login.getMyBenutzer().getNachVorname();
		ExportThread et = new ExportThread(user);
		et.start();
	}

	class ExportThread extends Thread {
		private String username = "";
		
		public ExportThread(String inName){
			username = inName;
		}
		
		public void run() {
			String exportFulltextParameter = parameters.get("exportOcr");
			String exportImagesParameter = parameters.get("exportImages");
			boolean exportFulltext = exportFulltextParameter.toLowerCase().equals("true");
			boolean exportImages = exportImagesParameter.toLowerCase().equals("true");

			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());

					

					IExportPlugin export = null;
					String pluginName = ProcessManager.getExportPluginName(p.getId());
					if (StringUtils.isNotEmpty(pluginName)) {
						try {
							export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
						} catch (Exception e) {
							logger.error("Can't load export plugin, use default plugin", e);
							export = new ExportDms();
						}
					}
					String logextension = "without ocr results";
					if (exportFulltext) {
						logextension = "including ocr results";
					}
					if (export == null) {
						export = new ExportDms();
					}
					export.setExportFulltext(exportFulltext);
					if (exportImages == false) {
						logextension = "without images and " + logextension;
						export.setExportImages(false);
					} else {
						logextension = "including images and " + logextension;
						export.setExportImages(true);
					}

					try {
						boolean success = export.startExport(p);
						Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG,
								"Export " + logextension + " using GoobiScript.", username);
						logger.info("Export " + logextension + " using GoobiScript for process with ID "
								+ p.getId());
						
						// set status to result of command
						if (!success){
							gsr.setResultMessage("Errors occurred: " + export.getProblems().toString());
							gsr.setResultType(GoobiScriptResultType.ERROR);
						}else{
							gsr.setResultMessage("Export done successfully");
							gsr.setResultType(GoobiScriptResultType.OK);
						}
					} catch (NoSuchMethodError | Exception e) {
						gsr.setResultMessage(e.getMessage());
						gsr.setResultType(GoobiScriptResultType.ERROR);
						logger.error("Exception during the export of process " + p.getId(), e);
					}
				}
			}
		}
	}

}
