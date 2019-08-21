package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;

import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiScriptExportDMS extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        ExportThread et = new ExportThread();
        et.start();
    }

    class ExportThread extends Thread {

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

            String exportFulltextParameter = parameters.get("exportOcr");
            String exportImagesParameter = parameters.get("exportImages");
            boolean exportFulltext = exportFulltextParameter.toLowerCase().equals("true");
            boolean exportImages = exportImagesParameter.toLowerCase().equals("true");

            // execute all jobs that are still in waiting state
            synchronized (resultList) {
                for (GoobiScriptResult gsr : resultList) {
                    if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING
                            && gsr.getCommand().equals(command)) {
                        Process p = ProcessManager.getProcessById(gsr.getProcessId());
                        try {
                            gsr.setProcessTitle(p.getTitel());
                            gsr.setResultType(GoobiScriptResultType.RUNNING);
                            gsr.updateTimestamp();

                            IExportPlugin export = null;
                            String pluginName = ProcessManager.getExportPluginName(p.getId());
                            if (StringUtils.isNotEmpty(pluginName)) {
                                try {
                                    export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
                                } catch (Exception e) {
                                    log.error("Can't load export plugin, use default plugin", e);
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

                            boolean success = export.startExport(p);
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Export " + logextension + " using GoobiScript.", username);
                            log.info("Export " + logextension + " using GoobiScript for process with ID " + p.getId());

                            // set status to result of command
                            if (!success) {
                                gsr.setResultMessage("Errors occurred: " + export.getProblems().toString());
                                gsr.setResultType(GoobiScriptResultType.ERROR);
                            } else {
                                gsr.setResultMessage("Export done successfully");
                                gsr.setResultType(GoobiScriptResultType.OK);
                            }
                        } catch (NoSuchMethodError | Exception e) {
                            gsr.setResultMessage(e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(e.getMessage());
                            log.error("Exception during the export of process " + p.getId(), e);
                        }
                        gsr.updateTimestamp();
                    }
                }
            }
        }
    }
}
