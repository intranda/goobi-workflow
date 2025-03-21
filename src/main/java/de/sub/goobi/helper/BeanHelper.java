/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
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
package de.sub.goobi.helper;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.LogType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;

import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;
import ugh.exceptions.UGHException;

@Log4j2
public class BeanHelper implements Serializable {

    private static final long serialVersionUID = 8661143513583015230L;

    public void EigenschaftHinzufuegen(Process inProzess, String inTitel, String inWert) {
        Processproperty eig = new Processproperty();
        eig.setPropertyName(inTitel);
        eig.setPropertyValue(inWert);
        eig.setProzess(inProzess);
        List<GoobiProperty> eigenschaften = inProzess.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }

    public void EigenschaftHinzufuegen(Template inVorlage, String inTitel, String inWert) {
        Templateproperty eig = new Templateproperty();
        eig.setPropertyName(inTitel);
        eig.setPropertyValue(inWert);
        eig.setVorlage(inVorlage);
        List<GoobiProperty> eigenschaften = inVorlage.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }

    public void EigenschaftHinzufuegen(Masterpiece inWerkstueck, String inTitel, String inWert) {
        Masterpieceproperty eig = new Masterpieceproperty();
        eig.setPropertyName(inTitel);
        eig.setPropertyValue(inWert);
        eig.setWerkstueck(inWerkstueck);
        List<GoobiProperty> eigenschaften = inWerkstueck.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }

    public void SchritteKopieren(Process prozessVorlage, Process prozessKopie) {
        List<Step> mySchritte = new ArrayList<>();
        for (Step step : prozessVorlage.getSchritteList()) {

            /* --------------------------------
             * Details des Schritts
             * --------------------------------*/
            Step stepneu = new Step();
            stepneu.setTypAutomatisch(step.isTypAutomatisch());
            stepneu.setScriptname1(step.getScriptname1());
            stepneu.setScriptname2(step.getScriptname2());
            stepneu.setScriptname3(step.getScriptname3());
            stepneu.setScriptname4(step.getScriptname4());
            stepneu.setScriptname5(step.getScriptname5());

            stepneu.setTypAutomatischScriptpfad(step.getTypAutomatischScriptpfad());
            stepneu.setTypAutomatischScriptpfad2(step.getTypAutomatischScriptpfad2());
            stepneu.setTypAutomatischScriptpfad3(step.getTypAutomatischScriptpfad3());
            stepneu.setTypAutomatischScriptpfad4(step.getTypAutomatischScriptpfad4());
            stepneu.setTypAutomatischScriptpfad5(step.getTypAutomatischScriptpfad5());
            stepneu.setBatchStep(step.getBatchStep());
            stepneu.setTypScriptStep(step.isTypScriptStep());
            stepneu.setTypBeimAnnehmenAbschliessen(step.isTypBeimAnnehmenAbschliessen());
            stepneu.setTypBeimAnnehmenModul(step.isTypBeimAnnehmenModul());
            stepneu.setTypBeimAnnehmenModulUndAbschliessen(step.isTypBeimAnnehmenModulUndAbschliessen());
            stepneu.setTypModulName(step.getTypModulName());
            stepneu.setTypExportDMS(step.isTypExportDMS());
            stepneu.setTypExportRus(step.isTypExportRus());
            stepneu.setTypImagesLesen(step.isTypImagesLesen());
            stepneu.setTypImagesSchreiben(step.isTypImagesSchreiben());
            stepneu.setTypImportFileUpload(step.isTypImportFileUpload());
            stepneu.setTypMetadaten(step.isTypMetadaten());
            stepneu.setPrioritaet(step.getPrioritaet());
            stepneu.setBearbeitungsstatusEnum(step.getBearbeitungsstatusEnum());
            stepneu.setReihenfolge(step.getReihenfolge());
            stepneu.setTitel(step.getTitel());
            stepneu.setHomeverzeichnisNutzen(step.getHomeverzeichnisNutzen());
            stepneu.setProzess(prozessKopie);

            stepneu.setStepPlugin(step.getStepPlugin());
            stepneu.setValidationPlugin(step.getValidationPlugin());
            stepneu.setDelayStep(step.isDelayStep());
            stepneu.setUpdateMetadataIndex(step.isUpdateMetadataIndex());

            stepneu.setTypBeimAbschliessenVerifizieren(step.isTypBeimAbschliessenVerifizieren());

            stepneu.setGenerateDocket(step.isGenerateDocket());

            stepneu.setHttpStep(step.isHttpStep());
            stepneu.setHttpUrl(step.getHttpUrl());
            stepneu.setHttpMethod(step.getHttpMethod());
            stepneu.setHttpEscapeBodyJson(step.isHttpEscapeBodyJson());
            stepneu.setHttpJsonBody(step.getHttpJsonBody());
            stepneu.setHttpCloseStep(step.isHttpCloseStep());
            stepneu.setMessageQueue(step.getMessageQueue());

            stepneu.setTypAutomaticThumbnail(step.isTypAutomaticThumbnail());
            stepneu.setAutomaticThumbnailSettingsYaml(step.getAutomaticThumbnailSettingsYaml());

            /* --------------------------------
             * Benutzer übernehmen
             * --------------------------------*/
            List<User> myBenutzer = new ArrayList<>();
            for (User benneu : step.getBenutzer()) {
                myBenutzer.add(benneu);
            }
            stepneu.setBenutzer(myBenutzer);

            /* --------------------------------
             * Benutzergruppen übernehmen
             * --------------------------------*/
            List<Usergroup> myBenutzergruppen = new ArrayList<>();
            for (Usergroup grupneu : step.getBenutzergruppen()) {
                myBenutzergruppen.add(grupneu);
            }
            stepneu.setBenutzergruppen(myBenutzergruppen);

            /* Schritt speichern */
            mySchritte.add(stepneu);
        }
        prozessKopie.setSchritte(mySchritte);
    }

    public void WerkstueckeKopieren(Process prozessVorlage, Process prozessKopie) {
        List<Masterpiece> myWerkstuecke = new ArrayList<>();
        for (Masterpiece werk : prozessVorlage.getWerkstuecke()) {
            /* --------------------------------
             * Details des Werkstücks
             * --------------------------------*/
            Masterpiece werkneu = new Masterpiece();
            werkneu.setProzess(prozessKopie);

            /* --------------------------------
             * Eigenschaften des Schritts
             * --------------------------------*/
            List<GoobiProperty> myEigenschaften = new ArrayList<>();
            for (GoobiProperty eig : werk.getEigenschaften()) {
                GoobiProperty eigneu = new GoobiProperty(PropertyOwnerType.MASTERPIECE);
                eigneu.setRequired(eig.isRequired());
                eigneu.setType(eig.getType());
                eigneu.setPropertyName(eig.getPropertyName());
                eigneu.setPropertyValue(eig.getPropertyValue());
                eigneu.setOwner(werkneu);
                myEigenschaften.add(eigneu);
            }
            werkneu.setEigenschaften(myEigenschaften);

            /* Schritt speichern */
            myWerkstuecke.add(werkneu);
        }
        prozessKopie.setWerkstuecke(myWerkstuecke);
    }

    public void EigenschaftenKopieren(Process prozessVorlage, Process prozessKopie) {
        List<GoobiProperty> myEigenschaften = new ArrayList<>();
        for (GoobiProperty eig : prozessVorlage.getEigenschaftenList()) {
            GoobiProperty eigneu = new GoobiProperty(PropertyOwnerType.PROCESS);
            eigneu.setRequired(eig.isRequired());
            eigneu.setType(eig.getType());
            eigneu.setPropertyName(eig.getPropertyName());
            eigneu.setPropertyValue(eig.getPropertyValue());
            eigneu.setOwner(prozessKopie);
            myEigenschaften.add(eigneu);
        }
        prozessKopie.setEigenschaften(myEigenschaften);
    }

    public void ScanvorlagenKopieren(Process prozessVorlage, Process prozessKopie) {
        List<Template> myVorlagen = new ArrayList<>();
        for (Template vor : prozessVorlage.getVorlagen()) {
            /* --------------------------------
             * Details der Vorlage
             * --------------------------------*/
            Template vorneu = new Template();
            vorneu.setHerkunft(vor.getHerkunft());
            vorneu.setProzess(prozessKopie);

            /* --------------------------------
             * Eigenschaften des Schritts
             * --------------------------------*/
            List<GoobiProperty> myEigenschaften = new ArrayList<>();
            for (GoobiProperty eig : vor.getEigenschaften()) {
                GoobiProperty eigneu = new GoobiProperty(PropertyOwnerType.TEMPLATE);
                eigneu.setRequired(eig.isRequired());
                eigneu.setType(eig.getType());
                eigneu.setPropertyName(eig.getPropertyName());
                eigneu.setPropertyValue(eig.getPropertyValue());
                eigneu.setOwner(vorneu);
                myEigenschaften.add(eigneu);
            }
            vorneu.setEigenschaften(myEigenschaften);

            /* Schritt speichern */
            myVorlagen.add(vorneu);
        }
        prozessKopie.setVorlagen(myVorlagen);
    }

    public String WerkstueckEigenschaftErmitteln(Process myProzess, String inEigenschaft) {
        String werkstueckEigenschaft = "";
        for (Masterpiece myWerkstueck : myProzess.getWerkstueckeList()) {
            for (GoobiProperty eigenschaft : myWerkstueck.getEigenschaftenList()) {
                if (eigenschaft.getPropertyName().equals(inEigenschaft)) {
                    werkstueckEigenschaft = eigenschaft.getPropertyValue();
                }
            }
        }
        return werkstueckEigenschaft;
    }

    public String ScanvorlagenEigenschaftErmitteln(Process myProzess, String inEigenschaft) {
        String scanvorlagenEigenschaft = "";
        for (Template myVorlage : myProzess.getVorlagenList()) {
            for (GoobiProperty eigenschaft : myVorlage.getEigenschaftenList()) {
                if (eigenschaft.getPropertyName().equals(inEigenschaft)) {
                    scanvorlagenEigenschaft = eigenschaft.getPropertyValue();
                }
            }
        }
        return scanvorlagenEigenschaft;
    }

    /**
     * Allow to change the process template after process generation. <br />
     * If a task exists in both templates, the old status remains.
     * 
     * metadata, images and all other data are not touched
     * 
     * @param processToChange process to change
     * @param template new process template
     * @return
     */

    public boolean changeProcessTemplate(Process processToChange, Process template) {
        List<Step> oldTaskList = new ArrayList<>(processToChange.getSchritte());

        // remove tasks from process
        processToChange.setSchritte(new ArrayList<>());
        // copy tasks from template to process
        SchritteKopieren(template, processToChange);

        // set task progress
        for (Step newTask : processToChange.getSchritte()) {
            for (Step oldTask : oldTaskList) {
                if (oldTask.getTitel().equals(newTask.getTitel()) && StepStatus.DONE.equals(oldTask.getBearbeitungsstatusEnum())) {
                    // if oldTask is finished, keep status, date, user in new task
                    newTask.setBearbeitungsbeginn(oldTask.getBearbeitungsbeginn());
                    newTask.setBearbeitungsende(oldTask.getBearbeitungsende());
                    newTask.setBearbeitungsstatusEnum(oldTask.getBearbeitungsstatusEnum());
                    newTask.setBearbeitungsbenutzer(oldTask.getBearbeitungsbenutzer());
                    break;
                }
            }
        }

        // remove old tasks from database
        for (Step oldTask : oldTaskList) {
            StepManager.deleteStep(oldTask);
        }
        // update properties for template name + id
        for (GoobiProperty property : processToChange.getEigenschaften()) {
            if ("Template".equals(property.getPropertyName())) {
                property.setPropertyValue(template.getTitel());
            } else if ("TemplateID".equals(property.getPropertyName())) {
                property.setPropertyValue(String.valueOf(template.getId()));
            }
        }

        // add text to process log
        User user = Helper.getCurrentUser();
        JournalEntry logEntry =
                new JournalEntry(processToChange.getId(), new Date(), user != null ? user.getNachVorname() : "", LogType.DEBUG,
                        "Changed process template to " + template.getTitel(), EntryType.PROCESS);
        processToChange.getJournal().add(logEntry);

        try {
            // if no open task was found, open first locked task
            for (Step newTask : processToChange.getSchritte()) {

                StepStatus status = newTask.getBearbeitungsstatusEnum();
                if (status == StepStatus.OPEN) {
                    break;
                } else if (status == StepStatus.LOCKED) {
                    newTask.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    break;
                }
            }
            // TODO what happens if task is automatic?

            // save new tasks
            ProcessManager.saveProcess(processToChange);
        } catch (DAOException e) {
            log.error(e);
            return false;
        }
        return true;
    }

    public Process createAndSaveNewProcess(Process template, String processName, Fileformat fileformat) {

        Process newProcess = new Process();
        newProcess.setTitel(processName);
        newProcess.setIstTemplate(false);
        newProcess.setInAuswahllisteAnzeigen(false);
        newProcess.setProjekt(template.getProjekt());
        newProcess.setRegelsatz(template.getRegelsatz());
        newProcess.setDocket(template.getDocket());
        newProcess.setExportValidator(template.getExportValidator());
        SchritteKopieren(template, newProcess);
        ScanvorlagenKopieren(template, newProcess);
        WerkstueckeKopieren(template, newProcess);
        EigenschaftenKopieren(template, newProcess);

        // add template information
        EigenschaftHinzufuegen(newProcess, "Template", template.getTitel());
        EigenschaftHinzufuegen(newProcess, "TemplateID", String.valueOf(template.getId()));

        // update task edition dates
        for (Step step : newProcess.getSchritteList()) {

            step.setBearbeitungszeitpunkt(newProcess.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);

            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(newProcess.getErstellungsdatum());
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }
        }

        // save process
        try {
            ProcessManager.saveProcess(newProcess);
        } catch (DAOException e) {
            log.error("error on save: ", e);
            return newProcess;
        }

        // write metadata file
        try {
            Path f = Paths.get(newProcess.getProcessDataDirectoryIgnoreSwapping());
            if (!StorageProvider.getInstance().isFileExists(f)) {
                StorageProvider.getInstance().createDirectories(f);
            }
            newProcess.writeMetadataFile(fileformat);
        } catch (UGHException | IOException | SwapException e) {
            log.error(e);
        }

        // Create history events
        if (Boolean.FALSE.equals(HistoryAnalyserJob.updateHistoryForProzess(newProcess))) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                ProcessManager.saveProcess(newProcess);
            } catch (DAOException e) {
                log.error("error on save: ", e);
                return newProcess;
            }
        }

        return newProcess;

    }

}
