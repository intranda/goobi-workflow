package org.goobi.goobiScript;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.io.BackupFileManager;
import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptMetadataBackup extends AbstractIGoobiScript implements IGoobiScript {

    private static final String ACTION = "metadataBackup";

    @Override
    public String getAction() {
        return ACTION;
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows create a backup of meta.xml and meta_anchor.xml.");
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
    public void execute(GoobiScriptResult result) {

        Process process = ProcessManager.getProcessById(result.getProcessId());
        result.setProcessTitle(process.getTitel());
        result.setResultType(GoobiScriptResultType.RUNNING);
        result.updateTimestamp();

        ConfigurationHelper config = ConfigurationHelper.getInstance();
        String path = config.getMetadataFolder() + process.getId() + "/";
        int maximumNumberOfBackupFiles = config.getNumberOfMetaBackups();
        String warning = " does not exist! No backup created.";

        String metaFileName = "meta.xml";
        boolean existsMetaFile = StorageProvider.getInstance().isFileExists(Paths.get(path + metaFileName));
        GoobiScriptResult metaBackupResult;

        if (existsMetaFile) {
            metaBackupResult = GoobiScriptMetadataBackup.createBackup(path, metaFileName, maximumNumberOfBackupFiles);
        } else {
            metaBackupResult = new GoobiScriptResult(null, null, null, null, 0);
            metaBackupResult.setResultMessage(metaFileName + warning);
            metaBackupResult.setResultType(GoobiScriptResultType.ERROR);
        }

        String metaAnchorFileName = "meta_anchor.xml";
        boolean existsMetaAnchorFile = StorageProvider.getInstance().isFileExists(Paths.get(path + metaAnchorFileName));
        GoobiScriptResult metaAnchorBackupResult;

        if (existsMetaAnchorFile) {
            metaAnchorBackupResult = GoobiScriptMetadataBackup.createBackup(path, metaAnchorFileName, maximumNumberOfBackupFiles);
        } else {
            metaAnchorBackupResult = new GoobiScriptResult(null, null, null, null, 0);
            metaAnchorBackupResult.setResultMessage(metaAnchorFileName + warning);
            metaAnchorBackupResult.setResultType(GoobiScriptResultType.OK);
        }

        result.setResultMessage(metaBackupResult.getResultMessage() + " " + metaAnchorBackupResult.getResultMessage());
        if (metaBackupResult.getResultType() == GoobiScriptResultType.OK && metaAnchorBackupResult.getResultType() == GoobiScriptResultType.OK) {
            result.setResultType(GoobiScriptResultType.OK);
        } else {
            result.setResultType(GoobiScriptResultType.ERROR);
        }
        result.updateTimestamp();
    }

    private static GoobiScriptResult createBackup(String path, String file, int maximumNumberOfBackupFiles) {
        GoobiScriptResult result = new GoobiScriptResult(null, null, null, null, 0);
        try {
            String backupFileName = BackupFileManager.createBackup(path, file, maximumNumberOfBackupFiles, false);
            result.setResultType(GoobiScriptResultType.OK);
            result.setResultMessage(Helper.getTranslation("backupCreated") + " " + path + backupFileName + ".");
        } catch (IOException ioException) {
            result.setResultType(GoobiScriptResultType.ERROR);
            result.setResultMessage(ioException.getMessage());
        }
        return result;
    }
}