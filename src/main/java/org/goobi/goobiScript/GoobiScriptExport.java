/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.goobiScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;

import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

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
                "This GoobiScript allows to export Goobi processes using the default export mechanism. It either uses"
                        + " the default export or alternativly an export plugin that was configured in one of the workflow steps.");
        addParameterToSampleCall(sb, "exportImages", "false", "Decide if the images shall get exported additionally to the metdata (`true`).");
        addParameterToSampleCall(sb, "exportOcr", "false", "Decide if the OCR results shall get exported additionally as well (`true`).");
        // allow choice among multiple export plugins
        addParameterToSampleCall(sb, "pluginName", "",
                "Decide which export plugin shall be used by giving the name of this export plugin. It has the highest priority if"
                        + " it is configured. [OPTIONAL]");
        addParameterToSampleCall(sb, "stepName", "",
                "Decide which export plugin shall be used by giving the name of the export step. If it is configured then it will"
                        + " try to find this export step first, and only when there is no such step, will "
                        + "the standard choice be used instead. [OPTIONAL]");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        String exportFullTextParameter = parameters.get("exportOcr");
        String exportImagesParameter = parameters.get("exportImages");
        boolean exportFullText = "true".equalsIgnoreCase(exportFullTextParameter);
        boolean exportImages = "true".equalsIgnoreCase(exportImagesParameter);

        String pluginNameConfigured = parameters.get("pluginName");
        String stepNameConfigured = parameters.get("stepName");

        // execute all jobs that are still in waiting state
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        try {
            gsr.setProcessTitle(p.getTitel());
            gsr.setResultType(GoobiScriptResultType.RUNNING);
            gsr.updateTimestamp();

            if (!p.getContainsExportStep()) {
                log.error("No task with type Export found in process " + p.getId());
                gsr.setResultMessage(Helper.getString(Helper.getSessionLocale(), "noExportTaskError"));
                gsr.setResultType(GoobiScriptResultType.ERROR);
                return;
            }

            // process has step marked as export
            // 1. get a proper export plugin
            String pluginName = getExportPluginName(pluginNameConfigured, stepNameConfigured, p);
            log.info("The export plugin '" + pluginName + "' will be used.");
            IExportPlugin export = getExportPlugin(pluginName);

            // 2. set up this export plugin
            export.setExportImages(exportImages);
            export.setExportFulltext(exportFullText);

            String logExtension = exportImages ? "including images and " : "without images and ";
            logExtension += exportFullText ? "including ocr results" : "without ocr results";

            // 3. kick this export plugin to run
            boolean success = export.startExport(p);

            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Export " + logExtension + " using GoobiScript.", username);
            log.info("Export " + logExtension + " using GoobiScript for process with ID " + p.getId());

            // 4. set status to result of command
            if (!success) {
                gsr.setResultMessage("Errors occurred: " + export.getProblems().toString());
                gsr.setResultType(GoobiScriptResultType.ERROR);
            } else {
                gsr.setResultMessage("Export done successfully");
                gsr.setResultType(GoobiScriptResultType.OK);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

        } catch (NoSuchMethodError | DocStructHasNoTypeException | PreferencesException | WriteException | MetadataTypeNotAllowedException
                | ReadException
                | TypeNotAllowedForParentException | IOException | ExportFileException | UghHelperException | SwapException | DAOException e) {
            gsr.setResultMessage(e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
            log.error("Exception during the export of process " + p.getId(), e);
        }
        gsr.updateTimestamp();

    }

    private String getExportPluginName(String pluginNameConfigured, String stepNameConfigured, Process process) {
        if (StringUtils.isNotBlank(pluginNameConfigured)) {
            return pluginNameConfigured;
        }

        if (StringUtils.isNotBlank(stepNameConfigured)) {
            List<Step> steps = process.getSchritte();
            for (Step step : steps) {
                boolean isExportStep = step.isTypExportDMS() || step.isTypExportRus();
                if (isExportStep && stepNameConfigured.equals(step.getTitel())) {
                    return step.getStepPlugin();
                }
            }
        }

        // otherwise use the default value
        return ProcessManager.getExportPluginName(process.getId());
    }

    private IExportPlugin getExportPlugin(String pluginName) {
        IExportPlugin export = null;
        if (StringUtils.isNotEmpty(pluginName)) {
            export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
        }

        if (export == null) {
            export = new ExportDms();
        }

        return export;
    }
}
