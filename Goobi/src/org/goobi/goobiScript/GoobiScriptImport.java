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

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptImport extends AbstractIGoobiScript implements IGoobiScript {
    private MassImportForm mi;
    private Batch batch = null;
    private List<Record> records;

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

        //		batchId = 1;
        //        try {
        //            batchId += ProcessManager.getMaxBatchNumber();
        //        } catch (Exception e1) {
        //        }

        String[] identifiers = parameters.get("identifiers").split(",");
        for (String id : identifiers) {
            GoobiScriptResult gsr = new GoobiScriptResult(Integer.parseInt(parameters.get("template")), command, username);
            gsr.setProcessTitle(id);
            resultList.add(gsr);
        }

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

        public void run() {
            String pluginName = parameters.get("plugin");
            Process template = ProcessManager.getProcessById(Integer.parseInt(parameters.get("template")));

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : resultList) {
                if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    gsr.updateTimestamp();
                    gsr.setResultType(GoobiScriptResultType.RUNNING);
                    IImportPlugin plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, pluginName);
                    Process proc = ProcessManager.getProcessById(gsr.getProcessId());
                    plugin.setPrefs(proc.getRegelsatz().getPreferences());
                    plugin.setForm(mi);

                    List<ImportObject> answer = new ArrayList<ImportObject>();

                    String tempfolder = ConfigurationHelper.getInstance().getTemporaryFolder();
                    plugin.setImportFolder(tempfolder);

                    List<Record> recordList = new ArrayList<Record>();
                    Record r = null;

                    // there are records already to lets find the right one
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
                                gsr.setResultMessage("Import successfully finished for id '" + gsr.getProcessTitle() + "'. Processname is " + io
                                        .getProcessTitle() + ".");
                                gsr.setResultType(GoobiScriptResultType.OK);
                            }
                        } else {
                            String[] parameter = { io.getProcessTitle(), io.getErrorMessage() };
                            gsr.setResultMessage("Import failed for id '" + gsr.getProcessTitle() + "'. " + Helper.getTranslation("importFailedError",
                                    parameter));
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
