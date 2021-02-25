package de.sub.goobi.helper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.api.mail.SendMail;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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

@Log4j2
public class CloseStepHelper {

    /**
     * This class is added to replace {@link HelperSchritte} class. The main goal is to use a singleton and/or static methods to close tasks
     */

    private static CloseStepHelper instance = null;

    /**
     * private constructor to prevent instantiation from outside
     */

    private CloseStepHelper() {
    }

    /**
     * get the only instance of this class
     * 
     * @return
     */

    public static synchronized CloseStepHelper getInstance() {
        if (CloseStepHelper.instance == null) {
            CloseStepHelper.instance = new CloseStepHelper();
        }
        return CloseStepHelper.instance;
    }

    /**
     * main method to close a task.
     * 
     * @param currentStep
     * @param user
     * @return
     */

    public static boolean closeStep(Step currentStep, User user) {
        saveStepStatus(currentStep, user);
        if (currentStep.isUpdateMetadataIndex()) {
            updateDatabaseIndex(currentStep);

        }
        SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.DONE);
        HistoryManager.addHistory(currentStep.getBearbeitungsende(), new Integer(currentStep.getReihenfolge()).doubleValue(), currentStep.getTitel(),
                HistoryEventType.stepDone.getValue(), currentStep.getProzess().getId());

        List<Step> automaticTasks = new ArrayList<>();
        List<Step> tasksToFinish = new ArrayList<>();
        /* prüfen, ob es Schritte gibt, die parallel stattfinden aber noch nicht abgeschlossen sind */
        List<Step> steps = StepManager.getStepsForProcess(currentStep.getProzess().getId());
        List<Step> followingSteps = new ArrayList<>();
        int openStepsInSameOrder = 0;
        for (Step so : steps) {
            if (so.getReihenfolge() == currentStep.getReihenfolge()
                    && !(so.getBearbeitungsstatusEnum().equals(StepStatus.DONE) || so.getBearbeitungsstatusEnum().equals(StepStatus.DEACTIVATED))
                    && so.getId() != currentStep.getId()) {
                openStepsInSameOrder++;
            } else if (so.getReihenfolge() > currentStep.getReihenfolge()) {
                followingSteps.add(so);
            }
        }

        /* wenn keine offenen parallelschritte vorhanden sind, die nächsten Schritte aktivieren */
        if (openStepsInSameOrder == 0) {
            int order = 0;
            boolean matched = false;
            for (Step myStep : followingSteps) {
                if (order < myStep.getReihenfolge() && !matched) {
                    order = myStep.getReihenfolge();
                }

                if (order == myStep.getReihenfolge() && !(myStep.getBearbeitungsstatusEnum().equals(StepStatus.DONE)
                        || myStep.getBearbeitungsstatusEnum().equals(StepStatus.DEACTIVATED))) {
                    /*
                     * open step, if it is locked, otherwise stop
                     */

                    if (myStep.getBearbeitungsstatusEnum().equals(StepStatus.LOCKED)) {

                        myStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
                        myStep.setBearbeitungszeitpunkt(currentStep.getBearbeitungsende());
                        myStep.setEditTypeEnum(StepEditType.AUTOMATIC);
                        HistoryManager.addHistory(currentStep.getBearbeitungsende(), new Integer(myStep.getReihenfolge()).doubleValue(),
                                myStep.getTitel(), HistoryEventType.stepOpen.getValue(), currentStep.getProzess().getId());
                        /* wenn es ein automatischer Schritt mit Script ist */
                        if (myStep.isTypAutomatisch()) {
                            automaticTasks.add(myStep);
                        } else if (myStep.isTypBeimAnnehmenAbschliessen()) {
                            tasksToFinish.add(myStep);
                        }
                        try {
                            SendMail.getInstance().sendMailToAssignedUser(myStep, StepStatus.OPEN);
                            StepManager.saveStep(myStep);
                            Helper.addMessageToProcessLog(currentStep.getProzess().getId(), LogType.DEBUG,
                                    "Step '" + myStep.getTitel() + "' opened.");
                        } catch (DAOException e) {
                            log.error("An exception occurred while saving a step for process with ID " + currentStep.getProzess().getId(), e);
                        }
                    }
                    matched = true;
                } else {
                    if (matched) {
                        break;
                    }
                }
            }
        }
        Process po = ProcessManager.getProcessById(currentStep.getProzess().getId());

        try {
            int numberOfFiles = StorageProvider.getInstance().getNumberOfFiles(Paths.get(po.getImagesOrigDirectory(true)));
            if (numberOfFiles == 0) {
                numberOfFiles = StorageProvider.getInstance().getNumberOfFiles(Paths.get(po.getImagesTifDirectory(true)));
            }
            if (numberOfFiles > 0 && po.getSortHelperImages() != numberOfFiles) {
                ProcessManager.updateImages(numberOfFiles, currentStep.getProzess().getId());
            }

        } catch (Exception e) {
            log.error("An exception occurred while closing a step for process with ID " + po.getId(), e);
        }

        updateProcessStatus(currentStep.getProzess().getId());
        // execute automatic tasks
        for (Step automaticStep : automaticTasks) {
            automaticStep.setBearbeitungsbeginn(new Date());
            automaticStep.setBearbeitungsbenutzer(null);
            automaticStep.setBearbeitungsstatusEnum(StepStatus.INWORK);
            automaticStep.setEditTypeEnum(StepEditType.AUTOMATIC);
            SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.INWORK);
            HistoryManager.addHistory(automaticStep.getBearbeitungsbeginn(), automaticStep.getReihenfolge().doubleValue(), automaticStep.getTitel(),
                    HistoryEventType.stepInWork.getValue(), automaticStep.getProzess().getId());
            try {
                StepManager.saveStep(automaticStep);
                Helper.addMessageToProcessLog(currentStep.getProzess().getId(), LogType.DEBUG,
                        "Step '" + automaticStep.getTitel() + "' started to work automatically.");
            } catch (DAOException e) {
                log.error("An exception occurred while saving an automatic step for process with ID " + automaticStep.getProzess().getId(), e);
            }
            // save
            if (log.isDebugEnabled()) {
                log.debug("Starting scripts for step with stepId " + automaticStep.getId() + " and process with ID "
                        + automaticStep.getProzess().getId());
            }
            if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(automaticStep);
                myThread.startOrPutToQueue();
            } else {
                automaticStep.setBearbeitungsstatusEnum(StepStatus.ERROR);
                try {
                    StepManager.saveStep(automaticStep);
                    Helper.addMessageToProcessLog(currentStep.getProzess().getId(), LogType.DEBUG,
                            "Step '" + automaticStep.getTitel() + "' was set to ERROR because it is an automatic step and message queues are switched off.");
                } catch (DAOException e) {
                    log.error("An exception occurred while saving the error status for an automatic step for process with ID " + automaticStep.getProzess().getId(), e);
                }
            }
        }
        for (Step finish : tasksToFinish) {
            closeStep(finish, user);
        }

        return true;
    }

    private static void updateDatabaseIndex(Step currentStep) {
        try {
            String metdatdaPath = currentStep.getProzess().getMetadataFilePath();
            String anchorPath = metdatdaPath.replace("meta.xml", "meta_anchor.xml");
            Path metadataFile = Paths.get(metdatdaPath);
            Path anchorFile = Paths.get(anchorPath);
            Map<String, List<String>> pairs = new HashMap<>();

            HelperSchritte.extractMetadata(metadataFile, pairs);

            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                HelperSchritte.extractMetadata(anchorFile, pairs);
            }

            MetadataManager.updateMetadata(currentStep.getProzess().getId(), pairs);

            // now add all authority fields to the metadata pairs
            HelperSchritte.extractAuthorityMetadata(metadataFile, pairs);
            if (StorageProvider.getInstance().isFileExists(anchorFile)) {
                HelperSchritte.extractAuthorityMetadata(anchorFile, pairs);
            }
            MetadataManager.updateJSONMetadata(currentStep.getProzess().getId(), pairs);

            HistoryAnalyserJob.updateHistory(currentStep.getProzess());

            if (!currentStep.getProzess().isMediaFolderExists()
                    && StorageProvider.getInstance().isFileExists(Paths.get(currentStep.getProzess().getImagesDirectory()))) {
                currentStep.getProzess().setMediaFolderExists(true);
                ProcessManager.saveProcessInformation(currentStep.getProzess());
            }

        } catch (SwapException | DAOException | IOException | InterruptedException e1) {
            log.error("An exception occurred while updating the metadata file process with ID " + currentStep.getProzess().getId(), e1);
        }

    }

    private static void saveStepStatus(Step currentStep, User user) {
        currentStep.setBearbeitungsstatusEnum(StepStatus.DONE);
        SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.DONE);
        Date myDate = new Date();

        currentStep.setBearbeitungszeitpunkt(myDate);
        if (user != null) {
            currentStep.setBearbeitungsbenutzer(user);
        }
        currentStep.setBearbeitungsende(myDate);
        try {
            StepManager.saveStep(currentStep);
            Helper.addMessageToProcessLog(currentStep.getProzess().getId(), LogType.DEBUG, "Step '" + currentStep.getTitel() + "' closed.");
        } catch (DAOException e) {
            log.error("An exception occurred while closing the step '" + currentStep.getTitel() + "' of process with ID "
                    + currentStep.getProzess().getId(), e);
        }

    }

    private static void updateProcessStatus(int processId) {

        int offen = 0;
        int inBearbeitung = 0;
        int abgeschlossen = 0;
        List<Step> stepsForProcess = StepManager.getStepsForProcess(processId);
        for (Step step : stepsForProcess) {
            if (step.getBearbeitungsstatusEnum().equals(StepStatus.DONE) || step.getBearbeitungsstatusEnum().equals(StepStatus.DEACTIVATED)) {
                abgeschlossen++;
            } else if (step.getBearbeitungsstatusEnum().equals(StepStatus.LOCKED)) {
                offen++;
            } else {
                inBearbeitung++;
            }
        }
        double offen2 = 0;
        double inBearbeitung2 = 0;
        double abgeschlossen2 = 0;

        if ((offen + inBearbeitung + abgeschlossen) == 0) {
            offen = 1;
        }

        offen2 = (offen * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        inBearbeitung2 = (inBearbeitung * 100) / (double) (offen + inBearbeitung + abgeschlossen);
        abgeschlossen2 = 100 - offen2 - inBearbeitung2;
        // (abgeschlossen * 100) / (offen + inBearbeitung + abgeschlossen);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#000");
        String value = df.format(abgeschlossen2) + df.format(inBearbeitung2) + df.format(offen2);

        ProcessManager.updateProcessStatus(value, processId);
    }
}
