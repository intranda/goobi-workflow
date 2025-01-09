package org.goobi.managedbeans;

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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.ErrorProperty;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IPushPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.ShowStepCondition;
import org.goobi.production.properties.Type;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.helper.BatchStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named("AktuelleSchritteForm")
@WindowScoped
@Log4j2
public class StepBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 5841566727939692509L;

    private static final String RETURN_PAGE_ALL = "task_all";
    private static final String RETURN_PAGE_EDIT = "task_edit";
    private static final String RETURN_PAGE_EDIT_BATCH = "task_edit_batch";

    @Getter
    @Setter
    private Process myProzess = new Process();
    private Step mySchritt = new Step();
    @Getter
    private IStepPlugin myPlugin;
    @Getter
    @Setter
    private Integer myProblemID;
    @Getter
    @Setter
    private Integer mySolutionID;
    @Getter
    @Setter
    private String problemMessage;
    @Getter
    @Setter
    private String solutionMessage;

    @Getter
    @Setter
    private String modusBearbeiten = "";
    private WebDav myDav = new WebDav();
    private int gesamtAnzahlImages = 0;
    private int pageAnzahlImages = 0;
    @Getter
    @Setter
    private boolean nurOffeneSchritte = false;
    @Getter
    @Setter
    private boolean nurEigeneSchritte = false;
    @Setter
    private boolean showAutomaticTasks = false;
    @Setter
    private boolean hideCorrectionTasks = false;
    @Getter
    @Setter
    private boolean hideStepsFromOtherUsers = false;
    @Getter
    @Setter
    private HashMap<String, Boolean> anzeigeAnpassen;
    @Getter
    @Setter
    private String scriptPath;
    private String doneDirectoryName = "fertig/";
    @Getter
    @Setter
    private BatchStepHelper batchHelper;
    @Getter
    private Map<String, PropertyListObject> containers = new TreeMap<>();
    @Getter
    private String container;
    private List<ProcessProperty> processPropertyList;
    @Getter
    @Setter
    private ProcessProperty processProperty;
    @Getter
    private HashMap<String, Boolean> containerAccess;

    @Getter
    @Setter
    private String content = "";

    @Getter
    @Setter
    protected boolean priorityComment = false;

    @Getter
    private Map<String, List<String>> displayableMetadataMap = new HashMap<>();

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    @Push
    PushContext stepPluginPush;

    @Getter
    @Setter
    private String selectedErrorPropertyType;

    @Getter
    private Map<String, String> errorPropertyTypes = null;

    @Getter
    @Setter
    private String selectedSolutionPropertyType;

    @Getter
    private Map<String, String> solutionPropertyTypes = null;

    public StepBean() {
        this.anzeigeAnpassen = new HashMap<>();
        anzeigeAnpassen.put("numberOfImages", false);

        /*
         * --------------------- Vorgangsdatum generell anzeigen? -------------------
         */
        LoginBean login = Helper.getLoginBean();
        if (login != null && login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("lockings", login.getMyBenutzer().isDisplayLocksColumn());
            this.anzeigeAnpassen.put("selectionBoxes", login.getMyBenutzer().isDisplaySelectBoxes());
            this.anzeigeAnpassen.put("processId", login.getMyBenutzer().isDisplayIdColumn());
            this.anzeigeAnpassen.put("batchId", login.getMyBenutzer().isDisplayBatchColumn());
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isDisplayProcessDateColumn());
            this.anzeigeAnpassen.put("modules", login.getMyBenutzer().isDisplayModulesColumn());
            this.anzeigeAnpassen.put("numberOfImages", login.getMyBenutzer().isDisplayNumberOfImages());
            nurOffeneSchritte = login.getMyBenutzer().isDisplayOnlyOpenTasks();
            nurEigeneSchritte = login.getMyBenutzer().isDisplayOnlySelectedTasks();
            showAutomaticTasks = login.getMyBenutzer().isDisplayAutomaticTasks();
            hideCorrectionTasks = login.getMyBenutzer().isHideCorrectionTasks();
            hideStepsFromOtherUsers = !login.getMyBenutzer().isDisplayOtherTasks();
            anzeigeAnpassen.put("institution", login.getMyBenutzer().isDisplayInstitutionColumn());

            if (StringUtils.isNotBlank(login.getMyBenutzer().getTaskListDefaultSortingField())) {
                sortField = login.getMyBenutzer().getTaskListDefaultSortingField() + login.getMyBenutzer().getTaskListDefaultSortOrder();
            }
        } else {
            this.anzeigeAnpassen.put("lockings", false);
            this.anzeigeAnpassen.put("selectionBoxes", false);
            this.anzeigeAnpassen.put("processId", false);
            this.anzeigeAnpassen.put("modules", false);
            this.anzeigeAnpassen.put("batchId", false);
            this.anzeigeAnpassen.put("processDate", false);
            this.anzeigeAnpassen.put("numberOfImages", false);
        }
        this.doneDirectoryName = ConfigurationHelper.getInstance().getDoneDirectoryName();
    }

    /*
     * Filter
     */

    /**
     * Anzeige der Schritte
     */
    public String FilterAlleStart() {

        StepManager m = new StepManager();
        String searchValue = filter;
        if (StringUtils.isNotBlank(additionalFilter) && StringUtils.isNotBlank(filter)) {
            searchValue = additionalFilter.replace("{}", filter);
        }

        String sql = FilterHelper.criteriaBuilder(searchValue, false, nurOffeneSchritte, nurEigeneSchritte, hideStepsFromOtherUsers, false, true);
        if (!showAutomaticTasks) {
            sql = "typAutomatisch = false AND " + sql;
        }
        if (hideCorrectionTasks) {
            sql = sql + " AND Prioritaet != 10 ";
        }
        if (!sql.isEmpty()) {
            sql = sql + " AND ";
        }
        sql = sql + " projekte.projectIsArchived = false ";
        paginator = new DatabasePaginator(sortField, sql, m, RETURN_PAGE_ALL);

        return RETURN_PAGE_ALL;
    }

    @Override
    public DatabasePaginator getPaginator() {
        if (paginator == null) {
            FilterAlleStart();
        }
        return paginator;
    }

    /*
     * Bearbeitung des Schritts übernehmen oder abschliessen
     */

    public String SchrittDurchBenutzerUebernehmen() {

        // reload step
        mySchritt = StepManager.getStepById(mySchritt.getId());
        mySchritt.lazyLoad();

        if (!(this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.OPEN || this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.ERROR)) {
            Helper.setFehlerMeldung("stepInWorkError");
            return "";
        }

        else {
            this.mySchritt.setBearbeitungsstatusEnum(StepStatus.INWORK);
            this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
            mySchritt.setBearbeitungszeitpunkt(new Date());
            User ben = Helper.getCurrentUser();
            if (ben != null) {
                mySchritt.setBearbeitungsbenutzer(ben);
            }
            if (this.mySchritt.getBearbeitungsbeginn() == null) {
                Date myDate = new Date();
                this.mySchritt.setBearbeitungsbeginn(myDate);
            }
            SendMail.getInstance().sendMailToAssignedUser(mySchritt, StepStatus.INWORK);
            HistoryManager.addHistory(this.mySchritt.getBearbeitungsbeginn(), this.mySchritt.getReihenfolge().doubleValue(),
                    this.mySchritt.getTitel(), HistoryEventType.stepInWork.getValue(), this.mySchritt.getProzess().getId());

            try {
                /*
                 * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
                 */
                StepManager.saveStep(mySchritt);
            } catch (DAOException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                log.error("step couldn't get saved", e);
            }
            /*
             * wenn es ein Image-Schritt ist, dann gleich die Images ins Home
             */

            if (this.mySchritt.isTypImagesLesen() || this.mySchritt.isTypImagesSchreiben()) {
                downloadToHome();
            }
        }
        return RETURN_PAGE_EDIT;
    }

    public String EditStep() throws SwapException, DAOException, IOException, InterruptedException {
        try {
            mySchritt = StepManager.getStepById(mySchritt.getId());
            mySchritt.lazyLoad();
        } catch (Exception exception) {
            log.error(exception);
        }

        return RETURN_PAGE_EDIT;
    }

    public String TakeOverBatch() {
        // find all steps with same batch id and step status
        Institution institution = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            institution = user.getInstitution();
        }
        String steptitle = this.mySchritt.getTitel();
        Integer batchNumber = null;
        if (mySchritt.getProzess().getBatch() != null) {
            batchNumber = this.mySchritt.getProzess().getBatch().getBatchId();
        }
        List<Step> currentStepsOfBatch;
        if (batchNumber != null) {
            // only steps with same title
            String sql = "schritte.titel = '" + steptitle + "' and prozesse.batchID = " + batchNumber;
            currentStepsOfBatch = StepManager.getSteps(null, sql, 0, Integer.MAX_VALUE, institution);

        } else {
            return SchrittDurchBenutzerUebernehmen();
        }
        // if only one step is assigned for this batch, use the single

        if (currentStepsOfBatch.isEmpty()) {
            return "";
        }
        if (currentStepsOfBatch.size() == 1) {
            return SchrittDurchBenutzerUebernehmen();
        }

        // set current user, update dates, set symlink for each step in batch
        for (Step s : currentStepsOfBatch) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum())) {

                s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                s.setBearbeitungszeitpunkt(new Date());
                if (user != null) {
                    s.setBearbeitungsbenutzer(user);
                    s.setUserId(user.getId());
                }
                if (s.getBearbeitungsbeginn() == null) {
                    Date myDate = new Date();
                    s.setBearbeitungsbeginn(myDate);
                }

                if (s.isTypImagesLesen() || s.isTypImagesSchreiben()) {
                    try {
                        Paths.get(s.getProzess().getImagesOrigDirectory(false));
                    } catch (Exception e1) {
                        log.warn("Cannot find imagesOrigDirectory for process '" + s.getProzess().getTitel() + "': ", e1);
                    }

                    this.myDav.DownloadToHome(s.getProzess(), s.getId().intValue(), !s.isTypImagesSchreiben());

                }
            }
        }
        // set status, set history, save changes
        for (Step s : currentStepsOfBatch) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum())) {
                s.setBearbeitungsstatusEnum(StepStatus.INWORK);
                // overwrite 'mySchritt' with new status
                if (mySchritt.getId().equals(s.getId())) {
                    mySchritt = s;
                }
                SendMail.getInstance().sendMailToAssignedUser(s, StepStatus.INWORK);
                HistoryManager.addHistory(s.getBearbeitungsbeginn(), s.getReihenfolge().doubleValue(), s.getTitel(),
                        HistoryEventType.stepInWork.getValue(), s.getProzess().getId());
                try {
                    StepManager.saveStep(s);
                } catch (DAOException e) {
                    Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                    log.error("step couldn't get saved", e);
                }

            }
        }
        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch, mySchritt));
        return RETURN_PAGE_EDIT_BATCH;
    }

    public String BatchesEdit() {
        // find all steps with same batch id and step status
        Institution institution = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            institution = user.getInstitution();
        }
        String steptitle = this.mySchritt.getTitel();
        Integer batchNumber = null;
        if (mySchritt.getProzess().getBatch() != null) {
            batchNumber = this.mySchritt.getProzess().getBatch().getBatchId();
        }
        List<Step> currentStepsOfBatch;
        if (batchNumber != null) {
            // only steps with same title
            currentStepsOfBatch = StepManager.getSteps(null,
                    "schritte.titel = '" + steptitle
                            + "'  AND batchStep = true AND schritte.prozesseID in (select prozesse.prozesseID from prozesse where batchID = "
                            + batchNumber + ")",
                    0, Integer.MAX_VALUE, institution);

        } else {
            return RETURN_PAGE_EDIT;
        }
        // if only one step is asigned for this batch, use the single

        if (currentStepsOfBatch.size() == 1) {
            return RETURN_PAGE_EDIT;
        }
        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch, mySchritt));
        return RETURN_PAGE_EDIT_BATCH;
    }

    /**
     * @deprecated This method is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public void saveProperties() {
    }

    public String SchrittDurchBenutzerZurueckgeben() {
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.OPEN);
        // if we have a correction-step here then never remove startdate
        if (Boolean.TRUE.equals(this.mySchritt.isCorrectionStep())) {
            this.mySchritt.setBearbeitungsbeginn(null);
        }
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        SendMail.getInstance().sendMailToAssignedUser(mySchritt, StepStatus.OPEN);
        try {
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            StepManager.saveStep(mySchritt);
        } catch (DAOException e) {
            // do nothing
        }
        return FilterAlleStart();
    }

    public String SchrittDurchBenutzerAbschliessen() {

        if (mySchritt.getValidationPlugin() != null && mySchritt.getValidationPlugin().length() > 0) {
            IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, mySchritt.getValidationPlugin());
            if (ivp != null) {
                ivp.setStep(mySchritt);
                if (!ivp.validate()) {
                    return "";
                }
            } else {
                Helper.setFehlerMeldung("ErrorLoadingValidationPlugin");
            }
        }

        /*
         * -------------------------------- wenn das Resultat des Arbeitsschrittes zunächst verifiziert werden soll, dann ggf. das Abschliessen
         * abbrechen --------------------------------
         */
        if (this.mySchritt.isTypBeimAbschliessenVerifizieren()) {
            /* Metadatenvalidierung */
            if (this.mySchritt.isTypMetadaten() && ConfigurationHelper.getInstance().isUseMetadataValidation()) {
                MetadatenVerifizierung mv = new MetadatenVerifizierung();
                mv.setAutoSave(true);
                if (!mv.validate(this.mySchritt.getProzess())) {
                    return "";
                }
            }

            /* Imagevalidierung */
            if (this.mySchritt.isTypImagesSchreiben()) {
                MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                try {
                    if (!mih.checkIfImagesValid(this.mySchritt.getProzess().getTitel(), this.mySchritt.getProzess().getImagesOrigDirectory(false))) {
                        return "";
                    }
                } catch (Exception e) {
                    Helper.setFehlerMeldung("Error on image validation: ", e);
                }
            }
        }
        if (processPropertyList != null) {
            for (ProcessProperty prop : processPropertyList) {
                if (AccessCondition.WRITEREQUIRED.equals(prop.getCurrentStepAccessCondition()) && StringUtils.isBlank(prop.getReadValue())) {
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("Eigenschaft") + " " + prop.getName() + " " + Helper.getTranslation("requiredValue"));
                    return "";
                } else if (!prop.isValid()) {
                    Helper.setFehlerMeldung(Helper.getTranslation("PropertyValidation", prop.getName()));
                    return "";
                }
            }
        }

        /*
         * wenn das Ergebnis der Verifizierung ok ist, dann weiter, ansonsten schon vorher draussen
         */
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        new HelperSchritte().CloseStepObjectAutomatic(mySchritt);
        return FilterAlleStart();
    }

    public String SperrungAufheben() {
        MetadatenSperrung.UnlockProcess(this.mySchritt.getProzess().getId());
        return "";
    }

    /*
     * Korrekturmeldung an vorherige Schritte
     */

    public List<Step> getPreviousStepsForProblemReporting() {
        return StepManager.getSteps("Reihenfolge desc",
                " schritte.prozesseID = " + this.mySchritt.getProzess().getId() + " AND Reihenfolge < " + this.mySchritt.getReihenfolge(), 0,
                Integer.MAX_VALUE, null);
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    public Set<String> getAllErrorPropertyTypes() {
        if (errorPropertyTypes == null) {
            errorPropertyTypes = ConfigurationHelper.getInstance().getErrorPropertyTypes();
        }
        return errorPropertyTypes.keySet();
    }

    public boolean isDisplayErrorPropertyTypes() {
        return !getAllErrorPropertyTypes().isEmpty();
    }

    public Set<String> getAllSolutionPropertyTypes() {
        if (solutionPropertyTypes == null) {
            solutionPropertyTypes = ConfigurationHelper.getInstance().getSolutionPropertyTypes();
        }
        return solutionPropertyTypes.keySet();
    }

    public boolean isDisplaySolutionPropertyTypes() {
        return !getAllSolutionPropertyTypes().isEmpty();
    }

    public String ReportProblem() {

        if (myProblemID == null) {
            Helper.setFehlerMeldung("task_cannotProceedWithoutTaskSelection");
            return "";
        }

        if (log.isDebugEnabled()) {
            log.debug("mySchritt.ID: " + this.mySchritt.getId().intValue());
            log.debug("Korrekturschritt.ID: " + this.myProblemID.intValue());
        }
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        Date myDate = new Date();
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.mySchritt.setBearbeitungsbeginn(null);
        mySchritt.setCorrectionStep();
        try {
            Step temp = StepManager.getStepById(myProblemID);
            temp.setBearbeitungsstatusEnum(StepStatus.ERROR);
            temp.setCorrectionStep();
            temp.setBearbeitungsende(new Date());
            ErrorProperty se = new ErrorProperty();

            se.setTitel(Helper.getTranslation("Korrektur notwendig"));
            String messageText;
            if (StringUtils.isNotBlank(selectedErrorPropertyType)) {
                messageText = errorPropertyTypes.get(selectedErrorPropertyType).replace("{}", problemMessage);
            } else {
                messageText = problemMessage;
            }

            String suffix = "";
            if (ben != null) {
                suffix = " (" + ben.getNachVorname() + ")";
            }
            se.setWert(messageText + suffix);
            se.setType(PropertyType.MESSAGE_ERROR);
            se.setCreationDate(myDate);
            se.setSchritt(temp);
            String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + messageText;

            JournalEntry logEntry = new JournalEntry(mySchritt.getProzess().getId(), new Date(), ben != null ? ben.getNachVorname() : "",
                    LogType.ERROR, message, EntryType.PROCESS);

            JournalManager.saveJournalEntry(logEntry);

            temp.getEigenschaften().add(se);
            StepManager.saveStep(temp);
            SendMail.getInstance().sendMailToAssignedUser(temp, StepStatus.ERROR);
            HistoryManager.addHistory(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError.getValue(),
                    temp.getProzess().getId());

            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */

            List<Step> alleSchritteDazwischen =
                    StepManager.getSteps("Reihenfolge desc", " schritte.prozesseID = " + this.mySchritt.getProzess().getId() + " AND Reihenfolge <= "
                            + this.mySchritt.getReihenfolge() + "  AND Reihenfolge > " + temp.getReihenfolge(), 0, Integer.MAX_VALUE, null);

            for (Step step : alleSchritteDazwischen) {

                if (!StepStatus.DEACTIVATED.equals(step.getBearbeitungsstatusEnum())) {
                    step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
                }
                step.setCorrectionStep();
                step.setBearbeitungsende(null);
                ErrorProperty seg = new ErrorProperty();
                seg.setTitel(Helper.getTranslation("Korrektur notwendig"));
                seg.setWert(Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + messageText + suffix);
                seg.setSchritt(step);
                seg.setType(PropertyType.MESSAGE_IMPORTANT);
                seg.setCreationDate(new Date());
                step.getEigenschaften().add(seg);
                StepManager.saveStep(step);
            }
            if (temp.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(temp);
                myThread.startOrPutToQueue();
            }

            StepManager.saveStep(mySchritt);
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            ProcessManager.saveProcessInformation(this.mySchritt.getProzess());
        } catch (DAOException exception) {
            log.error(exception);
        }

        this.problemMessage = "";
        this.myProblemID = 0;
        return FilterAlleStart();
    }

    /*
     *  Problem-behoben-Meldung an nachfolgende Schritte
     */

    public List<Step> getNextStepsForProblemSolution() {

        return StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.mySchritt.getProzess().getId()
                + " AND Reihenfolge > " + this.mySchritt.getReihenfolge() + " AND prioritaet = 10", 0, Integer.MAX_VALUE, null);
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    public String SolveProblem() {

        if (mySolutionID == null) {
            Helper.setFehlerMeldung("task_cannotProceedWithoutTaskSelection");
            return "";
        }

        Date now = new Date();
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        SendMail.getInstance().sendMailToAssignedUser(mySchritt, StepStatus.DONE);
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
        this.mySchritt.setBearbeitungsende(now);
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User user = Helper.getCurrentUser();
        if (user != null) {
            mySchritt.setBearbeitungsbenutzer(user);
        }

        try {

            Step temp = StepManager.getStepById(this.mySolutionID);

            String messageText;
            if (StringUtils.isNotBlank(selectedSolutionPropertyType)) {
                messageText = solutionPropertyTypes.get(selectedSolutionPropertyType).replace("{}", solutionMessage);
            } else {
                messageText = solutionMessage;
            }

            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + messageText;

            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */
            List<Step> alleSchritteDazwischen =
                    StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.mySchritt.getProzess().getId() + " AND Reihenfolge >= "
                            + this.mySchritt.getReihenfolge() + "  AND Reihenfolge <= " + temp.getReihenfolge(), 0, Integer.MAX_VALUE, null);

            for (Step step : alleSchritteDazwischen) {

                if (!StepStatus.DEACTIVATED.equals(step.getBearbeitungsstatusEnum())) {
                    step.setBearbeitungsstatusEnum(StepStatus.DONE);
                }
                step.setBearbeitungsende(now);
                step.setCorrectionStep();
                step.setBearbeitungszeitpunkt(new Date());
                if (step.getId().intValue() == temp.getId().intValue()) {
                    step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    step.setCorrectionStep();
                    step.setBearbeitungsende(null);
                    step.setBearbeitungszeitpunkt(now);
                    SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.OPEN);

                }
                ErrorProperty seg = new ErrorProperty();
                seg.setTitel(Helper.getTranslation("Korrektur durchgefuehrt"));
                String suffix = "";
                if (user != null) {
                    step.setBearbeitungsbenutzer(user);
                    suffix = (" (" + user.getNachVorname() + ")");
                }
                seg.setWert(message + suffix);
                seg.setSchritt(step);
                seg.setType(PropertyType.MESSAGE_IMPORTANT);
                seg.setCreationDate(new Date());
                step.getEigenschaften().add(seg);
                StepManager.saveStep(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            JournalEntry logEntry = new JournalEntry(mySchritt.getProzess().getId(), new Date(), user != null ? user.getNachVorname() : "",
                    LogType.INFO, message, EntryType.PROCESS);
            JournalManager.saveJournalEntry(logEntry);

            ProcessManager.saveProcessInformation(this.mySchritt.getProzess());

            if (temp.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(temp);
                myThread.startOrPutToQueue();
            }
        } catch (DAOException exception) {
            log.error(exception);
        }

        this.solutionMessage = "";
        this.mySolutionID = 0;
        return FilterAlleStart();
    }

    /*
     * Upload und Download der Images
     */

    public String uploadFromHome() {
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        Helper.setMeldung(null, "Removed directory from user home", this.mySchritt.getProzess().getTitel());
        return "";
    }

    public String downloadToHome() {
        try {
            Paths.get(this.mySchritt.getProzess().getImagesOrigDirectory(true));
        } catch (Exception exception) {
            log.error(exception);
        }
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.DownloadToHome(this.mySchritt.getProzess(), this.mySchritt.getId().intValue(), !this.mySchritt.isTypImagesSchreiben());

        return "";
    }

    public String uploadFromHomeAlle() throws NumberFormatException {

        List<String> fertigListe = this.myDav.UploadFromHomeAlle(this.doneDirectoryName);
        List<String> geprueft = new ArrayList<>();
        /*
         * -------------------------------- die hochgeladenen Prozess-IDs durchlaufen und auf abgeschlossen setzen --------------------------------
         */
        if (fertigListe != null && !fertigListe.isEmpty() && this.nurOffeneSchritte) {
            this.nurOffeneSchritte = false;
            FilterAlleStart();
        }
        for (String element : fertigListe) {
            String myID = element.substring(element.indexOf("[") + 1, element.indexOf("]")).trim();

            String sql = FilterHelper.criteriaBuilder("id:" + myID, false, false, false, false, false, true);
            List<Step> stepList = StepManager.getSteps(sortField, sql, null);

            for (Step step : stepList) {
                if (StepStatus.INWORK.equals(step.getBearbeitungsstatusEnum())) {
                    this.mySchritt = step;
                    if (!"".equals(SchrittDurchBenutzerAbschliessen())) {
                        geprueft.add(element);
                    }
                    this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                }
            }
        }

        this.myDav.removeFromHomeAlle(geprueft, this.doneDirectoryName);
        Helper.setMeldung(null, "removed " + geprueft.size() + " directories from user home:", this.doneDirectoryName);
        return FilterAlleStart();
    }

    @SuppressWarnings("unchecked")
    public String downloadToHomePage() {
        User ben = Helper.getCurrentUser();

        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getList().iterator(); iter.hasNext();) {
            Step step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.INWORK);
                step.setBearbeitungsstatusEnum(StepStatus.INWORK);
                step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                step.setBearbeitungszeitpunkt(new Date());
                if (ben != null) {
                    step.setBearbeitungsbenutzer(ben);
                } else {
                    step.setBearbeitungsbenutzer(null);
                }
                step.setBearbeitungsbeginn(new Date());
                Process proz = step.getProzess();
                try {
                    StepManager.saveStep(step);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
                }
                this.myDav.DownloadToHome(proz, step.getId().intValue(), false);
            }
        }
        Helper.setMeldung(null, "Created directies in user home", "");
        return FilterAlleStart();
    }

    @SuppressWarnings("unchecked")
    public String downloadToHomeHits() {
        User ben = Helper.getCurrentUser();

        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getCompleteList().iterator(); iter.hasNext();) {
            Step step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                SendMail.getInstance().sendMailToAssignedUser(step, StepStatus.INWORK);
                step.setBearbeitungsstatusEnum(StepStatus.INWORK);
                step.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                step.setBearbeitungszeitpunkt(new Date());
                if (ben != null) {
                    step.setBearbeitungsbenutzer(ben);
                } else {
                    step.setBearbeitungsbenutzer(null);
                }
                step.setBearbeitungsbeginn(new Date());
                Process proz = step.getProzess();
                try {
                    StepManager.saveStep(step);
                } catch (DAOException e) {
                    Helper.setMeldung("fehlerNichtSpeicherbar" + proz.getTitel());
                }
                this.myDav.DownloadToHome(proz, step.getId().intValue(), false);
            }
        }
        Helper.setMeldung(null, "Created directories in user home", "");
        return FilterAlleStart();
    }

    public void executeScript() {
        new HelperSchritte().executeScriptForStepObject(mySchritt, this.scriptPath, false);

    }

    /**
     * @deprecated This method is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public void executeModule() {
    }

    /**
     * @deprecated This method is not used anymore
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public int getHomeBaende() {
        return 0;
    }

    public int getAllImages() {
        return this.gesamtAnzahlImages;
    }

    public int getPageImages() {
        return this.pageAnzahlImages;
    }

    @SuppressWarnings("unchecked")
    public void calcHomeImages() {
        this.gesamtAnzahlImages = 0;
        this.pageAnzahlImages = 0;
        User aktuellerBenutzer = Helper.getCurrentUser();

        if (aktuellerBenutzer != null && aktuellerBenutzer.isMitMassendownload()) {
            for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getCompleteList().iterator(); iter.hasNext();) {
                Step step = iter.next();
                try {
                    if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
                        this.gesamtAnzahlImages += StorageProvider.getInstance().getNumberOfFiles(step.getProzess().getImagesOrigDirectory(false));
                    }
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
    }

    /*
     *  Getter und Setter
     */

    public Step getMySchritt() {
        try {
            schrittPerParameterLaden();
        } catch (NumberFormatException e) {
            log.error(e);
        }
        return this.mySchritt;
    }

    public void setMySchritt(Step mySchritt) {
        myPlugin = null;
        this.modusBearbeiten = "";
        this.mySchritt = mySchritt;
        loadProcessProperties();
        loadDisplayableMetadata();
        if (this.mySchritt.getStepPlugin() != null && !this.mySchritt.getStepPlugin().isEmpty()) {
            myPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, this.mySchritt.getStepPlugin());

            IExportPlugin exportPlugin = null;
            if (myPlugin == null) {
                exportPlugin = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, this.mySchritt.getStepPlugin());
            }
            if (myPlugin == null && exportPlugin == null) {
                Helper.setFehlerMeldung("Plugin could not be found:", this.mySchritt.getStepPlugin());
            } else if (myPlugin != null) {
                if (myPlugin instanceof IPushPlugin) {
                    ((IPushPlugin) myPlugin).setPushContext(stepPluginPush);
                }
                if (Boolean.TRUE.equals(mySchritt.isBatchStep()) && mySchritt.isBatchSize()) {
                    myPlugin.initialize(mySchritt, "/task_edit_batch");
                } else {
                    myPlugin.initialize(mySchritt, "/task_edit");
                }
            }
        }
    }

    public String runPlugin() {
        if (myPlugin.getPluginGuiType() == PluginGuiType.FULL || myPlugin.getPluginGuiType() == PluginGuiType.PART_AND_FULL) {
            String mypath = myPlugin.getPagePath();
            if (log.isDebugEnabled()) {
                log.debug("Plugin is full GUI");
                log.debug("open plugin GUI: " + mypath);
            }
            myPlugin.execute();
            return mypath;
        } else {
            myPlugin.execute();
            myPlugin.finish();
            return "";
        }
    }

    public void setMyPlugin(IStepPlugin myPlugin) {
        if (myPlugin instanceof IPushPlugin) {
            ((IPushPlugin) myPlugin).setPushContext(stepPluginPush);
        }
        this.myPlugin = myPlugin;
    }

    public void setStep(Step step) {
        this.mySchritt = step;
        loadProcessProperties();
    }

    public Step getStep() {
        return this.mySchritt;
    }

    /*
     * Parameter per Get übergeben bekommen und entsprechen den passenden Schritt laden
     */

    /**
     * prüfen, ob per Parameter vielleicht zunächst ein anderer geladen werden soll
     * 
     * @throws DAOException
     * @throws NumberFormatException
     */
    private void schrittPerParameterLaden() throws NumberFormatException {
        String param = Helper.getRequestParameter("myid");
        if (param != null && !"".equals(param)) {
            /*
             * wenn bisher noch keine aktuellen Schritte ermittelt wurden, dann dies jetzt nachholen, damit die Liste vollständig ist
             */
            if (this.paginator == null && Helper.getCurrentUser() != null) {
                FilterAlleStart();
            }
            Integer inParam = Integer.valueOf(param);
            if (this.mySchritt == null || this.mySchritt.getId() == null || !this.mySchritt.getId().equals(inParam)) {
                this.mySchritt = StepManager.getStepById(inParam);
            }
        }
    }

    /*
     * Auswahl mittels Selectboxen
     */

    @SuppressWarnings("unchecked")
    public void SelectionAll() {
        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getList().iterator(); iter.hasNext();) {
            Step s = iter.next();
            s.setSelected(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void selectionNone() {
        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getList().iterator(); iter.hasNext();) {
            Step s = iter.next();
            s.setSelected(false);
        }
    }

    /*
     * Downloads
     */

    public void downloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.mySchritt.getProzess());
        tiff.ExportStart();
    }

    public void exportDMS() {
        IExportPlugin dms = null;
        if (StringUtils.isNotBlank(mySchritt.getStepPlugin())) {
            try {
                dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, mySchritt.getStepPlugin());
            } catch (Exception e) {
                String message = "Can't load export plugin, use default export";
                log.error(message, e);
                Helper.setFehlerMeldung(message);
                dms = new ExportDms();
            }
        }
        if (dms == null) {
            dms = new ExportDms();
        }
        try {
            dms.startExport(this.mySchritt.getProzess());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on export", e.getMessage() == null ? "" : e.getMessage());
            log.error(e);
        }
    }

    public void addJournalEntry() {
        if (StringUtils.isNotBlank(content)) {
            User user = Helper.getCurrentUser();
            JournalEntry logEntry =
                    new JournalEntry(mySchritt.getProzess().getId(), new Date(), user.getNachVorname(),
                            priorityComment ? LogType.IMPORTANT_USER : LogType.USER, content, EntryType.PROCESS);
            JournalManager.saveJournalEntry(logEntry);
            mySchritt.getProzess().getJournal().add(logEntry);
            this.content = "";
        }
    }

    public List<ProcessProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    public int getSizeOfDisplayableMetadata() {
        return displayableMetadataMap.size();
    }

    private void loadDisplayableMetadata() {

        displayableMetadataMap = new LinkedHashMap<>();
        List<String> possibleMetadataNames = PropertyParser.getInstance().getDisplayableMetadataForStep(mySchritt);
        if (possibleMetadataNames.isEmpty()) {
            return;
        }

        for (String metadataName : possibleMetadataNames) {
            List<String> values = MetadataManager.getAllMetadataValues(mySchritt.getProzess().getId(), metadataName);
            if (!values.isEmpty()) {
                displayableMetadataMap.put(metadataName, values);
            }
        }
    }

    private void loadProcessProperties() {
        containerAccess = new HashMap<>();
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getInstance().getPropertiesForStep(this.mySchritt);

        for (ProcessProperty pt : this.processPropertyList) {

            boolean match = true;
            for (ShowStepCondition cond : pt.getShowStepConditions()) {
                if (cond.getName().equals(mySchritt.getTitel()) && !cond.getDisplayCondition().isEmpty()) {
                    // check if condition matches
                    match = false;
                    for (StringPair sp : cond.getDisplayCondition()) {
                        for (ProcessProperty other : processPropertyList) {
                            if (other.getName().equals(sp.getOne())) {
                                Optional<String> otherValue = Optional.empty();
                                if (Type.VOCABULARYREFERENCE.equals(other.getType())) {
                                    try {
                                        otherValue = Optional.of(VocabularyAPIManager.getInstance()
                                                .vocabularyRecords()
                                                .get(Long.parseLong(other.getValue()))
                                                .getMainValue());
                                    } catch (NumberFormatException e) {
                                        log.error("Unable to read ID \"{}\"", other.getValue());
                                    }
                                } else {
                                    otherValue = Optional.ofNullable(other.getValue());
                                }
                                if (otherValue.orElse("").equals(sp.getTwo())) {
                                    match = true;
                                }
                            }
                        }
                    }
                }
            }
            if (!match) {
                continue;
            }

            if (!"0".equals(pt.getContainer()) && pt.getCurrentStepAccessCondition() != AccessCondition.READ) {
                containerAccess.put(pt.getContainer(), true);
            }
            if (pt.getProzesseigenschaft() == null) {
                Processproperty pe = new Processproperty();
                pe.setProzess(this.mySchritt.getProzess());
                pt.setProzesseigenschaft(pe);
                this.mySchritt.getProzess().getEigenschaften().add(pe);
                pt.transfer();
            }
            if (!this.containers.keySet().contains(pt.getContainer())) {
                PropertyListObject plo = new PropertyListObject(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            } else {
                PropertyListObject plo = this.containers.get(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            }
        }
    }

    public void saveProcessProperties() {
        boolean valid = true;
        for (IProperty p : this.processPropertyList) {
            if (!p.isValid()) {
                String value = Helper.getTranslation("propertyNotValid", p.getName());
                Helper.setFehlerMeldung(value);
                valid = false;
            }
        }

        if (valid) {
            for (ProcessProperty p : this.processPropertyList) {
                if (p.getProzesseigenschaft() == null) {
                    Processproperty pe = new Processproperty();
                    pe.setProzess(this.mySchritt.getProzess());
                    p.setProzesseigenschaft(pe);
                    this.mySchritt.getProzess().getEigenschaften().add(pe);
                }
                p.transfer();
                if (!this.mySchritt.getProzess().getEigenschaften().contains(p.getProzesseigenschaft())) {
                    this.mySchritt.getProzess().getEigenschaften().add(p.getProzesseigenschaft());
                }
            }
            Process p = this.mySchritt.getProzess();
            List<Processproperty> props = p.getEigenschaftenList();
            for (Processproperty pe : props) {
                if (pe.getTitel() == null) {
                    p.getEigenschaften().remove(pe);
                }
            }

            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
            Helper.setMeldung("propertiesSaved");
        }
    }

    public void saveCurrentProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                String value = Helper.getTranslation("propertyNotValid", processProperty.getName());
                Helper.setFehlerMeldung(value);
                Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                Processproperty pe = new Processproperty();
                pe.setProzess(this.mySchritt.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            List<Processproperty> props = this.mySchritt.getProzess().getEigenschaftenList();
            for (Processproperty pe : props) {
                if (pe.getTitel() == null) {
                    this.mySchritt.getProzess().getEigenschaften().remove(pe);
                }
            }
            if (!this.mySchritt.getProzess().getEigenschaften().contains(this.processProperty.getProzesseigenschaft())) {
                this.mySchritt.getProzess().getEigenschaften().add(this.processProperty.getProzesseigenschaft());
                this.processProperty.getProzesseigenschaft().setProzess(this.mySchritt.getProzess());
            }
            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
        }
        Helper.setMeldung("propertiesSaved");
        loadProcessProperties();
    }

    public List<String> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
    }

    public int getPropertyListSize() {
        if (this.processPropertyList == null) {
            return 0;
        }
        return this.processPropertyList.size();
    }

    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    public void deleteProperty() {
        this.processPropertyList.remove(this.processProperty);
        this.mySchritt.getProzess().getEigenschaften().remove(this.processProperty.getProzesseigenschaft());
        List<Processproperty> props = this.mySchritt.getProzess().getEigenschaftenList();
        for (Processproperty pe : props) {
            if (pe.getTitel() == null) {
                this.mySchritt.getProzess().getEigenschaften().remove(pe);
            }
        }

        PropertyManager.deleteProcessProperty(processProperty.getProzesseigenschaft());
        loadProcessProperties();
    }

    public void duplicateProperty() {
        ProcessProperty pt = this.processProperty.getClone("0");
        this.processPropertyList.add(pt);
        this.processProperty = pt;
        saveCurrentProperty();
        loadProcessProperties();
    }

    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<>();
        for (ProcessProperty pp : this.processPropertyList) {
            if ("0".equals(pp.getContainer())) {
                boolean match = true;
                for (ShowStepCondition cond : pp.getShowStepConditions()) {
                    if (cond.getName().equals(mySchritt.getTitel()) && !cond.getDisplayCondition().isEmpty()) {
                        // check if condition matches
                        match = false;
                        for (StringPair sp : cond.getDisplayCondition()) {
                            for (ProcessProperty other : processPropertyList) {
                                if (other.getName().equals(sp.getOne())) {
                                    Optional<String> otherValue = Optional.empty();
                                    if (Type.VOCABULARYREFERENCE.equals(other.getType())) {
                                        try {
                                            otherValue = Optional.of(VocabularyAPIManager.getInstance()
                                                    .vocabularyRecords()
                                                    .get(Long.parseLong(other.getValue()))
                                                    .getMainValue());
                                        } catch (NumberFormatException e) {
                                            log.error("Unable to read ID \"{}\"", other.getValue());
                                        }
                                    } else {
                                        otherValue = Optional.ofNullable(other.getValue());
                                    }
                                    if (otherValue.orElse("").equals(sp.getTwo())) {
                                        match = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (match) {
                    answer.add(pp);
                }
            }
        }
        return answer;
    }

    public void setContainer(String container) {
        this.container = container;
        if (container != null && !"0".equals(container)) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<>();

        if (this.container != null && !"0".equals(container)) {
            for (ProcessProperty pp : this.processPropertyList) {
                if (this.container.equals(pp.getContainer())) {
                    boolean match = true;
                    for (ShowStepCondition cond : pp.getShowStepConditions()) {
                        if (cond.getName().equals(mySchritt.getTitel()) && !cond.getDisplayCondition().isEmpty()) {
                            // check if condition matches
                            match = false;
                            for (StringPair sp : cond.getDisplayCondition()) {
                                for (ProcessProperty other : processPropertyList) {
                                    if (other.getName().equals(sp.getOne())) {
                                        Optional<String> otherValue = Optional.empty();
                                        if (Type.VOCABULARYREFERENCE.equals(other.getType())) {
                                            try {
                                                otherValue = Optional.of(VocabularyAPIManager.getInstance()
                                                        .vocabularyRecords()
                                                        .get(Long.parseLong(other.getValue()))
                                                        .getMainValue());
                                            } catch (NumberFormatException e) {
                                                log.error("Unable to read ID \"{}\"", other.getValue());
                                            }
                                        } else {
                                            otherValue = Optional.ofNullable(other.getValue());
                                        }
                                        if (otherValue.orElse("").equals(sp.getTwo())) {
                                            match = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (match) {
                        answer.add(pp);
                    }
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    public String duplicateContainer() {
        String currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer().equals(currentContainer)) {
                plist.add(pt);
            }
        }
        int counter = 1;
        currentContainer = currentContainer.replaceAll(" - \\d+", "");
        String newContainerNumber = currentContainer;
        if (!"0".equals(currentContainer)) {
            // find new unused container number
            boolean search = true;
            while (search) {
                newContainerNumber = currentContainer + " - " + counter;
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    counter++;
                }
            }
        }
        // clone properties
        for (ProcessProperty pt : plist) {
            ProcessProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            if (this.processProperty.getProzesseigenschaft() == null) {
                Processproperty pe = new Processproperty();
                pe.setProzess(this.mySchritt.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.mySchritt.getProzess().getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
        }
        Helper.setMeldung("propertySaved");
        loadProcessProperties();
        return "";
    }

    public boolean getShowAutomaticTasks() {
        return this.showAutomaticTasks;
    }

    public boolean getHideCorrectionTasks() {
        return hideCorrectionTasks;
    }

    public String callStepPlugin() {
        if (mySchritt.getStepPlugin() != null && mySchritt.getStepPlugin().length() > 0) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
            if (isp instanceof IPushPlugin) {
                ((IPushPlugin) isp).setPushContext(stepPluginPush);
            }
            isp.initialize(mySchritt, "");
            if (isp instanceof IStepPluginVersion2) {
                IStepPluginVersion2 plugin = (IStepPluginVersion2) isp;
                plugin.run();
            } else {
                isp.execute();
            }
        }
        return "";
    }

    public boolean isEnableFinalizeButton() {
        return ConfigurationHelper.getInstance().isEnableFinalizeTaskButton();
    }
}
