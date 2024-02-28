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

    private static final String META_FILE = "meta.xml";
    private static final String META_ANCHOR_FILE = "meta_anchor.xml";

    @Override
    public String getAction() {
        return "metadataBackup";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript creates a backup of meta.xml and meta_anchor.xml.");
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
        String warning1 = "Could not create backup file. ";
        String warning2 = " does not exist.";
        String success = "Created backup successfully.";

        boolean existsMetaFile = StorageProvider.getInstance().isFileExists(Paths.get(path + META_FILE));
        GoobiScriptResult metaBackupResult;

        if (existsMetaFile) {
            metaBackupResult = GoobiScriptMetadataBackup.createBackup(path, META_FILE, maximumNumberOfBackupFiles);
            if (metaBackupResult.getResultType() == GoobiScriptResultType.OK) {
                metaBackupResult.setResultMessage(success);
            }
        } else {
            metaBackupResult = new GoobiScriptResult(null, null, null, null, 0);
            metaBackupResult.setResultMessage(warning1 + META_FILE + warning2);
            metaBackupResult.setResultType(GoobiScriptResultType.ERROR);
        }

        boolean existsMetaAnchorFile = StorageProvider.getInstance().isFileExists(Paths.get(path + META_ANCHOR_FILE));
        GoobiScriptResult metaAnchorBackupResult;

        if (existsMetaAnchorFile) {
            metaAnchorBackupResult = GoobiScriptMetadataBackup.createBackup(path, META_ANCHOR_FILE, maximumNumberOfBackupFiles);
        } else {
            metaAnchorBackupResult = new GoobiScriptResult(null, null, null, null, 0);
            metaAnchorBackupResult.setResultMessage(warning1 + META_ANCHOR_FILE + warning2);
            metaAnchorBackupResult.setResultType(GoobiScriptResultType.OK);
        }

        result.setResultMessage(metaBackupResult.getResultMessage());
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
            String backupFileName = BackupFileManager.createBackup(path, path, file, maximumNumberOfBackupFiles, false);
            result.setResultType(GoobiScriptResultType.OK);
            result.setResultMessage(Helper.getTranslation("backupCreated") + " " + path + backupFileName + ".");
        } catch (IOException ioException) {
            result.setResultType(GoobiScriptResultType.ERROR);
            result.setResultMessage(ioException.getMessage());
        }
        return result;
    }
}