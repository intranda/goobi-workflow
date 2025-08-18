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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.exceptions.UGHException;

@Log4j2
public class GoobiScriptUpdateDatabaseCache extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "updateDatabaseCache";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript ensures that the internal database table of the Goobi database is updated "
                        + "with the status of the workflows and the associated media files as well as metadata from the METS file.");
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
        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        try {
            // write metadata into database
            String metdatdaPath = p.getMetadataFilePath();
            String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
            Path metadataFile = Paths.get(metdatdaPath);
            Path anchorFile = Paths.get(anchorPath);
            Map<String, List<String>> pairs = new HashMap<>();

            HelperSchritte.extractMetadata(metadataFile, pairs);

            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                HelperSchritte.extractMetadata(anchorFile, pairs);
            }

            MetadataManager.updateMetadata(p.getId(), pairs);

            // now add all authority fields to the metadata pairs
            HelperSchritte.extractAuthorityMetadata(metadataFile, pairs);
            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                HelperSchritte.extractAuthorityMetadata(anchorFile, pairs);
            }
            MetadataManager.updateJSONMetadata(p.getId(), pairs);

            // calculate history entries
            boolean result = HistoryAnalyserJob.updateHistoryForProzess(p);
            if (!result) {
                Helper.addMessageToProcessJournal(p.getId(), LogType.ERROR, "History not successfully updated using GoobiScript.", username);
                log.info("History could not be updated using GoobiScript for process with ID " + p.getId());
                gsr.setResultMessage("History update was not successful.");
                gsr.setResultType(GoobiScriptResultType.ERROR);
                return;
            }

            // calculate number and size of images and metadata

            if (p.getSortHelperImages() == 0) {
                int value = HistoryManager.getNumberOfImages(p.getId());

                if (value == 0) {
                    value = StorageProvider.getInstance().getNumberOfFiles(Paths.get(p.getImagesOrigDirectory(true)));
                }
                if (value == 0) {
                    value = StorageProvider.getInstance().getNumberOfFiles(Paths.get(p.getImagesTifDirectory(true)));
                }
                if (value > 0) {
                    p.setSortHelperImages(value);
                }
            }

            try {
                DocStruct logical = p.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
                p.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(logical, CountType.DOCSTRUCT));
                p.setSortHelperMetadata(zaehlen.getNumberOfUghElements(logical, CountType.METADATA));
            } catch (UGHException | SwapException e) {
                // metadata not readable or not found
                log.error(e);
            }

            if (StorageProvider.getInstance().isFileExists(Paths.get(p.getImagesDirectory()))) {
                p.setMediaFolderExists(true);
            }

            ProcessManager.saveProcess(p);

            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Database updated using GoobiScript.", username);
            log.info("Database updated using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Updated database cache successfully.");

            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (IOException | SwapException | DAOException e1) {
            log.error("Problem while updating database using GoobiScript for process with id: " + p.getId(), e1);
            gsr.setResultMessage("Error while updating database cache: " + e1.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e1.getMessage());
        }
        gsr.updateTimestamp();
    }
}
