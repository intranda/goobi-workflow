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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptDeleteProcess extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "deleteProcess";
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows you to delete existing processes.");
        addParameterToSampleCall(sb, "contentOnly", "false",
                "Define here if just the content shall be deleted (true) or the the entire process from the database as well (false).");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("contentOnly") == null || parameters.get("contentOnly").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "contentOnly");
            return new ArrayList<>();
        }

        if (!parameters.get("contentOnly").equals("true") && !parameters.get("contentOnly").equals("false")) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "wrong parameter 'contentOnly'; possible values: true, false");
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

        boolean contentOnly = Boolean.parseBoolean(parameters.get("contentOnly"));
        boolean removeUnknownFiles =
                StringUtils.isNotBlank(parameters.get("removeUnknownFiles")) && Boolean.parseBoolean(parameters.get("removeUnknownFiles"));

        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        try {
            if (this.checkDeletePermission(p, contentOnly)) {
                if (contentOnly && !removeUnknownFiles) {
                    try {
                        Path ocr = Paths.get(p.getOcrDirectory());
                        if (StorageProvider.getInstance().isFileExists(ocr)) {
                            StorageProvider.getInstance().deleteDir(ocr);
                        }
                        Path images = Paths.get(p.getImagesDirectory());
                        if (StorageProvider.getInstance().isFileExists(images)) {
                            StorageProvider.getInstance().deleteDir(images);
                        }
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Content deleted using GoobiScript.", username);
                        log.info("Content deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process deleted successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e) {
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Problem occured while trying to delete content using GoobiScript.",
                                username);
                        log.error("Content for process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process cannot be deleted: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                } else if (contentOnly && removeUnknownFiles) {
                    try {
                        List<Path> dataInProcessFolder = StorageProvider.getInstance().listFiles(p.getProcessDataDirectory());
                        for (Path path : dataInProcessFolder) {
                            // keep the mets file, but delete everything else
                            if (!path.getFileName().toString().matches("meta.*xml.*")) { //NOSONAR, regex is not vulnerable to backtracking
                                StorageProvider.getInstance().deleteDir(path);
                            }
                        }
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Content deleted using GoobiScript.", username);
                        log.info("Content deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process deleted successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e) {
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Problem occured while trying to delete content using GoobiScript.",
                                username);
                        log.error("Content for process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Content for process cannot be deleted: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                } else {
                    try {
                        StorageProvider.getInstance().deleteDir(Paths.get(p.getProcessDataDirectory()));
                        ProcessManager.deleteProcess(p);
                        log.info("Process deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Process deleted successfully.");
                        gsr.setResultType(GoobiScriptResultType.OK);
                    } catch (Exception e) {
                        Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Problem occured while trying to delete process using GoobiScript.",
                                username);
                        log.error("Process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
                        gsr.setResultMessage("Process cannot be deleted: " + e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                    }
                }
            } else {
                Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Problem occured while trying to delete process using GoobiScript.",
                        username);
                log.error("Process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
                String message = "Process cannot be deleted: missing permissions";
                gsr.setResultMessage(message);
                gsr.setResultType(GoobiScriptResultType.ERROR);
                gsr.setErrorText(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (DAOException | SwapException | IOException e) {
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, "Problem occured while trying to delete process using GoobiScript.", username);
            log.error("Process cannot be deleted using GoobiScript for process with ID " + gsr.getProcessId());
            gsr.setResultMessage("Process cannot be deleted: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        gsr.updateTimestamp();
    }

    private boolean checkDeletePermission(Process p, boolean contentOnly) throws DAOException, SwapException, InterruptedException, IOException {
        Path path = Paths.get(p.getProcessDataDirectory());
        boolean permission;
        if (contentOnly) {
            permission = true;
            List<Path> paths = StorageProvider.getInstance().listFiles(path.toAbsolutePath().toString());
            for (Path contentPath : paths) {
                permission = permission && StorageProvider.getInstance().isDeletable(contentPath);
            }
        } else {
            permission = StorageProvider.getInstance().isDeletable(path);
        }
        return permission;
    }
}
