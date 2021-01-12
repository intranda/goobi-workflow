package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Batch;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.ImportReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.helper.JobCreation;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptImport extends AbstractIGoobiScript implements IGoobiScript {
    private MassImportForm mi;
    private Batch batch = null;
    private List<Record> records;

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewAction(sb, "import", "This GoobiScript is used to execute mass imports of data using an existing mass import plugin.");
        addParameter(sb, "plugin", "plugin_intranda_import_myplugin", "Define the plugin identifier to use here.");
        addParameter(sb, "identifiers", "1,2,3,4,5", "Define the identifiers to use for the import. These are comma separated.");
        addParameter(sb, "template", "13", "Define here the identifier of the process template to use for the mass import.");
        addParameter(sb, "projectId", "2", "In case another project shall be used define the project identifier here. This parameter is optional.");
        return sb.toString();
    }
    
    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);
        mi = (MassImportForm) Helper.getManagedBeanValue("#{MassImportForm}");

        if (parameters.get("plugin") == null || parameters.get("plugin").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "plugin");
            return false;
        }

        if (parameters.get("identifiers") == null || parameters.get("identifiers").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "identifiers");
            return false;
        }

        if (parameters.get("template") == null || parameters.get("template").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "template");
            return false;
        }

        batch = mi.getBatch();

        //		batchId = 1;
        //        try {
        //            batchId += ProcessManager.getMaxBatchNumber();
        //        } catch (Exception e1) {
        //        }

        String[] identifiers = parameters.get("identifiers").split(",");
        ImmutableList.Builder<GoobiScriptResult> newList = ImmutableList.<GoobiScriptResult> builder().addAll(gsm.getGoobiScriptResults());
        for (String id : identifiers) {
            GoobiScriptResult gsr = new GoobiScriptResult(Integer.parseInt(parameters.get("template")), command, username, starttime);
            gsr.setProcessTitle(id);
            newList.add(gsr);
        }
        gsm.setGoobiScriptResults(newList.build());

        return true;
    }

    public void setRecords(List<Record> records) {
        this.records = records;
    }

    @Override
    public void execute() {
        ImportThread et = new ImportThread();
        et.start();
    }

    class ImportThread extends Thread {

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

            String pluginName = parameters.get("plugin");
            Process template = ProcessManager.getProcessById(Integer.parseInt(parameters.get("template")));

            // set the overridden project if present
            if (parameters.get("projectId") != null && !parameters.get("projectId").equals("")) {
                int projectid = Integer.parseInt(parameters.get("projectId"));
                template.setProjectId(projectid);
            }

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    gsr.updateTimestamp();
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    IImportPlugin plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, pluginName);
                    Process proc = ProcessManager.getProcessById(gsr.getProcessId());
                    plugin.setPrefs(proc.getRegelsatz().getPreferences());
                    plugin.setForm(mi);

                    List<ImportObject> answer = new ArrayList<>();

                    String tempfolder = ConfigurationHelper.getInstance().getTemporaryFolder();
                    plugin.setImportFolder(tempfolder);

                    List<Record> recordList = new ArrayList<>();
                    Record r = null;

                    // there are records already so lets find the right one
                    if (records != null) {
                        for (Record record : records) {
                            if (record.getId().equals(gsr.getProcessTitle())) {
                                r = record;
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

                    answer = plugin.generateFiles(recordList);

                    for (ImportObject io : answer) {
                        if (batch != null) {
                            io.setBatch(batch);
                        }
                        if (io.getImportReturnValue().equals(ImportReturnValue.ExportFinished)) {
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
        }
    }
}
