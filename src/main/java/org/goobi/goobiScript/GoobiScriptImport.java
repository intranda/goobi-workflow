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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Batch;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.properties.ProcessProperty;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Setter;

public class GoobiScriptImport extends AbstractIGoobiScript implements IGoobiScript {

    private static final String PLUGIN = "plugin";
    private static final String IDENTIFIERS = "identifiers";
    private static final String TEMPLATE = "template";
    private static final String PROJECT_ID = "projectId";

    @Setter
    public List<ProcessProperty> additionalProperties;

    @Setter
    private MassImportForm mi;
    private Batch batch = null;
    private List<Record> records;

    @Override
    public String getAction() {
        return "import";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript is used to execute mass imports of data using an existing mass import plugin.");
        addParameterToSampleCall(sb, PLUGIN, "plugin_intranda_import_myplugin", "Define the plugin identifier to use here.");
        addParameterToSampleCall(sb, IDENTIFIERS, "1,2,3,4,5", "Define the identifiers to use for the import. These are comma separated.");
        addParameterToSampleCall(sb, TEMPLATE, "13", "Define here the identifier of the process template to use for the mass import.");
        addParameterToSampleCall(sb, PROJECT_ID, "2",
                "In case another project shall be used define the project identifier here. This parameter is optional.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String plugin = parameters.get(PLUGIN);
        if (plugin == null || "".equals(plugin)) {
            Helper.setFehlerMeldung(missingParameter, PLUGIN);
            return new ArrayList<>();
        }

        String identifiers = parameters.get(IDENTIFIERS);
        if (identifiers == null || "".equals(identifiers)) {
            Helper.setFehlerMeldung(missingParameter, IDENTIFIERS);
            return new ArrayList<>();
        }

        String template = parameters.get(TEMPLATE);
        if (template == null || "".equals(template)) {
            Helper.setFehlerMeldung(missingParameter, TEMPLATE);
            return new ArrayList<>();
        }

        batch = mi.getBatch();

        String[] identifierList = identifiers.split(",");
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (String id : identifierList) {
            GoobiScriptResult gsr = new GoobiScriptResult(Integer.parseInt(template), command, parameters, username, starttime);
            gsr.setProcessTitle(id);
            newList.add(gsr);
        }
        return newList;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        String pluginName = parameters.get(PLUGIN);
        Process template = ProcessManager.getProcessById(Integer.parseInt(parameters.get(TEMPLATE)));

        // set the overridden project if present
        String projectId = parameters.get(PROJECT_ID);
        if (projectId != null && !"".equals(projectId)) {
            template.setProjectId(Integer.parseInt(projectId));
        }

        // execute all jobs that are still in waiting state
        gsr.updateTimestamp();
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        IImportPlugin plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, pluginName);
        Process proc = ProcessManager.getProcessById(gsr.getProcessId());
        plugin.setPrefs(proc.getRegelsatz().getPreferences());
        plugin.setForm(mi);
        plugin.setWorkflowTitle(template.getTitel());

        String tempfolder = ConfigurationHelper.getInstance().getTemporaryFolder();
        plugin.setImportFolder(tempfolder);

        List<Record> recordList = new ArrayList<>();
        Record r = null;

        // there are records already so lets find the right one
        if (records != null) {
            for (Record rec : records) {
                if (rec.getId().equals(gsr.getProcessTitle())) {
                    r = rec;
                    break;
                }
            }
        }

        //Record not found so we create a new one here
        if (r == null) {
            r = new Record();
            r.setData(gsr.getProcessTitle());
            r.setId(gsr.getProcessTitle());
            r.setCollections(mi.getDigitalCollections());
        }

        recordList.add(r);

        List<ImportObject> answer = plugin.generateFiles(recordList);
        for (ImportObject io : answer) {
            if (batch != null) {
                io.setBatch(batch);
            }
            if (ImportReturnValue.ExportFinished.equals(io.getImportReturnValue())) {
                // add configured properties to all processes
                if (additionalProperties != null) {
                    for (ProcessProperty prop : additionalProperties) {
                        Processproperty pe = new Processproperty();
                        pe.setWert(prop.getValue());
                        pe.setTitel(prop.getName());
                        pe.setContainer(prop.getContainer());
                        io.getProcessProperties().add(pe);
                    }
                }
                Process p = JobCreation.generateProcess(io, template);
                if (p == null) {
                    gsr.setResultMessage("Import failed for id '" + gsr.getProcessTitle() + "'. Process cannot be created.");
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                } else {
                    gsr.setProcessId(p.getId());
                    gsr.setProcessTitle(p.getTitel());
                    gsr.setResultMessage("Import successfully finished for id '" + gsr.getProcessTitle() + "'. Processname is "
                            + io.getProcessTitle() + ".");
                    gsr.setResultType(GoobiScriptResultType.OK);
                }
            } else {
                String[] parameter = { gsr.getProcessTitle(), io.getErrorMessage() };
                gsr.setResultMessage(Helper.getTranslation("importFailedError", parameter));
                gsr.setResultType(GoobiScriptResultType.ERROR);
            }
        }
        // finally set result
        if (gsr.getResultType() == GoobiScriptResultType.RUNNING) {
            gsr.setResultType(GoobiScriptResultType.OK);
        }
        gsr.updateTimestamp();
    }
}
