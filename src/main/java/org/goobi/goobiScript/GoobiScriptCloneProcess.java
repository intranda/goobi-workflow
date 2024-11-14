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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;

@Log4j2
public class GoobiScriptCloneProcess extends AbstractIGoobiScript implements IGoobiScript {

    private static final String GOOBI_SCRIPTFIELD = "goobiScriptfield";
    private static final String TITLE = "title";
    private static final String CONTENT = "content";
    private static final String META_ANCHOR_FILE = "meta_anchor.xml";

    private static final String CONTENT_ALL = "all"; // all data completely
    private static final String CONTENT_EMPTY = "empty"; // just the metadata

    @Override
    public String getAction() {
        return "cloneProcess";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to clone an existing process and store it under a new name.");
        addParameterToSampleCall(sb, TITLE, "\"{processtitle}_copy\"",
                "Title for the new process that is created. Please notice that typically no special characters and no blanks are allowed.");
        addParameterToSampleCall(sb, CONTENT, CONTENT_ALL,
                "You can define here now much of the content shall be cloned. Possible values are `all` (all data, the database entry and the METS file), `empty` (no data, just the database entry and the corresponding METS file)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String wrongParameter = "Wrong content parameter";
        String title = parameters.get(TITLE);
        if (title == null || "".equals(title)) {
            Helper.setFehlerMeldung(missingParameter, TITLE);
            return new ArrayList<>();
        }
        String content = parameters.get(CONTENT);
        if (content == null || "".equals(content)) {
            Helper.setFehlerMeldung(missingParameter, CONTENT);
            return new ArrayList<>();
        }

        if (!CONTENT_ALL.equals(content) && !CONTENT_EMPTY.equals(content)) {
            Helper.setFehlerMeldung(wrongParameter, "(only limited values are allowed)");
            return new ArrayList<>();
        }

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
        Process process = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(process.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        String title = parameters.get(TITLE);
        try {

            // first duplicate the process inside of the database
            Process newProcess = new Process(process);

            Path existingDirectory = Paths.get(process.getProcessDataDirectory());
            Path newDirectory = Paths.get(newProcess.getProcessDataDirectory());
            Path existingMetaAnchorFile = Paths.get(process.getProcessDataDirectory(), META_ANCHOR_FILE);
            Path newMetaAnchorFile = Paths.get(newProcess.getProcessDataDirectory(), META_ANCHOR_FILE);

            // copy the files and other content over to the new process
            if (CONTENT_ALL.equals(parameters.get(CONTENT))) {
                // all content
                StorageProvider.getInstance().copyDirectory(existingDirectory, newDirectory);
            } else {
                // just the metadata files
                StorageProvider.getInstance().copyFile(Paths.get(process.getMetadataFilePath()), Paths.get(newProcess.getMetadataFilePath()));
                if (StorageProvider.getInstance().isFileExists(existingMetaAnchorFile)) {
                    StorageProvider.getInstance().copyFile(existingMetaAnchorFile, newMetaAnchorFile);
                }
            }

            // get the title to be set and pipe it through the variable replacer
            Fileformat ff = process.readMetadataFile();
            VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), process.getRegelsatz().getPreferences(), process, null);
            title = replacer.replace(title);

            // assign the old process title to be able to change that and all folders correctly
            newProcess.setTitel(process.getTitel());
            newProcess.changeProcessTitle(title);
            ProcessManager.saveProcess(newProcess);

            // all successfull, yeah
            gsr.setResultMessage(
                    "Process '" + process.getTitel() + "' successfully cloned under the new name '" + title + "' with id: " + newProcess.getId());
            gsr.setResultType(GoobiScriptResultType.OK);

        } catch (Exception e) {
            String message = "Error while cloning the process '" + process.getTitel() + "' under the new name '" + title + "'.";
            Helper.addMessageToProcessJournal(process.getId(), LogType.ERROR, message, username);
            log.error(message, e);
            gsr.setResultMessage("Error while cloning a process: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }

        gsr.updateTimestamp();
    }

}
