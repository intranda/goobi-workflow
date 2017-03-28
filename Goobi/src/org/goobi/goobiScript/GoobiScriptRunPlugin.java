package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class GoobiScriptRunPlugin extends AbstractIGoobiScript implements IGoobiScript {
	private static final Logger logger = Logger.getLogger(GoobiScriptRunPlugin.class);

	@Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		super.prepare(processes, command, parameters);

		if (parameters.get("steptitle") == null || parameters.get("steptitle").equals("")) {
			Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "steptitle");
			return false;
		}

		// add all valid commands to list
		for (Integer i : processes) {
			GoobiScriptResult gsr = new GoobiScriptResult(i, command);
			resultList.add(gsr);
		}
		return true;
	}

	@Override
	public void execute() {
		SetStepStatusThread et = new SetStepStatusThread();
		et.start();
	}

	class SetStepStatusThread extends Thread {

		public void run() {
			String steptitle = parameters.get("steptitle");

			// execute all jobs that are still in waiting state
			for (GoobiScriptResult gsr : resultList) {
				if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
					Process p = ProcessManager.getProcessById(gsr.getProcessId());
					gsr.setProcessTitle(p.getTitel());

					for (Step step : p.getSchritteList()) {
						if (step.getTitel().equalsIgnoreCase(steptitle)) {
							Step so = StepManager.getStepById(step.getId());

							if (so.getStepPlugin() != null && !so.getStepPlugin().isEmpty()) {
								IStepPlugin myPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step,
										so.getStepPlugin());

								if (myPlugin != null) {
									myPlugin.initialize(so, "");

									if (myPlugin.getPluginGuiType() == PluginGuiType.NONE) {
										myPlugin.execute();
										myPlugin.finish();

										Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Plugin for step '" + steptitle + "' executed using GoobiScript.", username);
										logger.info("Plugin for step '" + steptitle + "' executed using GoobiScript for process with ID " + p.getId());
										gsr.setResultMessage("Plugin for step '" + steptitle + "' executed successfully.");
										gsr.setResultType(GoobiScriptResultType.OK);
									}else{
				                        gsr.setResultMessage("Plugin for step '" + steptitle + "' cannot be executed as it has a GUI.");
				                        gsr.setResultType(GoobiScriptResultType.ERROR);
									}
								}
							} else {
		                        gsr.setResultMessage("Plugin for step '" + steptitle + "' cannot be executed as it is not Plugin workflow step.");
		                        gsr.setResultType(GoobiScriptResultType.ERROR);
							}
						}
					}
				}
			}
		}
	}

}
