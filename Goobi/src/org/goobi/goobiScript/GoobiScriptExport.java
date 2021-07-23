package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptExport extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "export";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript allows to export Goobi processes using the default export mechanism. It either uses the default export or alternativly an export plugin that was configured in one of the workflow steps.");
        addParameterToSampleCall(sb, "exportImages", "false", "Decide if the images shall get exported additionally to the metdata (`true`).");
        addParameterToSampleCall(sb, "exportOcr", "false", "Decide if the OCR results shall get exported additionally as well (`true`).");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
        	if(ProcessManager.getProcessById(i).getContainsExportStep()) {
	            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
	            newList.add(gsr);
        	}else {
        		log.error("No task with type Export found in process " + i);
            	Helper.setFehlerMeldung("noExportTaskError");
        	}
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        String exportFulltextParameter = parameters.get("exportOcr");
        String exportImagesParameter = parameters.get("exportImages");
        boolean exportFulltext = exportFulltextParameter.toLowerCase().equals("true");
        boolean exportImages = exportImagesParameter.toLowerCase().equals("true");

        // execute all jobs that are still in waiting state
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        if(p.getContainsExportStep()) {      
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
        }else {
        	log.error("No task with type Export found in process " + p.getId());
        	Helper.setFehlerMeldung("noExportTaskError");
        }
    }
}
