package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.ErrorProperty;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.beans.Process;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.helper.BatchStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Getter;
import lombok.Setter;

@ManagedBean(name = "AktuelleSchritteForm")
@SessionScoped
public class StepBean extends BasicBean {
    private static final long serialVersionUID = 5841566727939692509L;
    private static final Logger logger = Logger.getLogger(StepBean.class);
    private Process myProzess = new Process();
    private Step mySchritt = new Step();
    private IStepPlugin myPlugin;
    private Integer myProblemID;
    private Integer mySolutionID;
    private String problemMessage;
    private String solutionMessage;

    private String modusBearbeiten = "";
    //    private Schritteigenschaft mySchrittEigenschaft;
    private WebDav myDav = new WebDav();
    private int gesamtAnzahlImages = 0;
    private int pageAnzahlImages = 0;
    private boolean nurOffeneSchritte = false;
    private boolean nurEigeneSchritte = false;
    private boolean showAutomaticTasks = false;
    private boolean hideCorrectionTasks = false;
    private boolean hideStepsFromOtherUsers = false;
    private HashMap<String, Boolean> anzeigeAnpassen;
    private String scriptPath;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String DONEDIRECTORYNAME = "fertig/";
    //	private Boolean flagWait = false;
    private BatchStepHelper batchHelper;
    private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
    private Integer container;
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;

    @Getter
    @Setter
    private String content = "";
    @Getter
    @Setter
    private String secondContent = "";
    @Getter
    @Setter
    private String thirdContent = "";

    public StepBean() {
        this.anzeigeAnpassen = new HashMap<String, Boolean>();

        /*
         * --------------------- Vorgangsdatum generell anzeigen? -------------------
         */
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        if (login != null && login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("lockings", login.getMyBenutzer().isDisplayLocksColumn());
            this.anzeigeAnpassen.put("selectionBoxes", login.getMyBenutzer().isDisplaySelectBoxes());
            this.anzeigeAnpassen.put("processId", login.getMyBenutzer().isDisplayIdColumn());
            this.anzeigeAnpassen.put("batchId", login.getMyBenutzer().isDisplayBatchColumn());
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isDisplayProcessDateColumn());
            this.anzeigeAnpassen.put("modules", login.getMyBenutzer().isDisplayModulesColumn());
            nurOffeneSchritte = login.getMyBenutzer().isDisplayOnlyOpenTasks();
            nurEigeneSchritte = login.getMyBenutzer().isDisplayOnlySelectedTasks();
            showAutomaticTasks = login.getMyBenutzer().isDisplayAutomaticTasks();
            hideCorrectionTasks = login.getMyBenutzer().isHideCorrectionTasks();
            hideStepsFromOtherUsers = !login.getMyBenutzer().isDisplayOtherTasks();

        } else {
            this.anzeigeAnpassen.put("lockings", false);
            this.anzeigeAnpassen.put("selectionBoxes", false);
            this.anzeigeAnpassen.put("processId", false);
            this.anzeigeAnpassen.put("modules", false);
            this.anzeigeAnpassen.put("batchId", false);
            this.anzeigeAnpassen.put("processDate", false);
        }
        DONEDIRECTORYNAME = ConfigurationHelper.getInstance().getDoneDirectoryName();
    }

    /*
     * Filter
     */

    /**
     * Anzeige der Schritte
     */
    public String FilterAlleStart() {

        StepManager m = new StepManager();
        String sql = FilterHelper.criteriaBuilder(filter, false, nurOffeneSchritte, nurEigeneSchritte, hideStepsFromOtherUsers, false, true);
        if (!showAutomaticTasks) {
            sql = "typAutomatisch = false AND " + sql;
        }
        if (hideCorrectionTasks) {
            sql = sql + " AND Prioritaet != 10 ";
        }
        if (!sql.isEmpty()) {
            sql = sql + " AND ";
        }
        sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        paginator = new DatabasePaginator(sortList(), sql, m, "task_all");

        return "task_all";
    }

    public DatabasePaginator getPaginator() {
        if (paginator == null) {
            FilterAlleStart();
        }
        return paginator;
    }

    private String sortList() {
        if (sortierung == null) {
            return "prioritaet desc";
        }

        String answer = "prioritaet desc ";

        if (this.sortierung.equals("schrittAsc")) {
            answer += ", schritte.titel";
        } else if (this.sortierung.equals("schrittDesc")) {
            answer += ", schritte.titel desc";
        }
        if (this.sortierung.equals("prozessAsc")) {
            answer += ", prozesse.Titel";
        }
        if (this.sortierung.equals("prozessDesc")) {
            answer += ", prozesse.Titel desc";
        }
        if (this.sortierung.equals("batchAsc")) {
            answer += ", prozesse.batchID";
        }
        if (this.sortierung.equals("batchDesc")) {
            answer += ", prozesse.batchID desc";
        }
        if (this.sortierung.equals("prozessdateAsc")) {

            answer += ", prozesse.erstellungsdatum";
        }
        if (this.sortierung.equals("prozessdateDesc")) {
            answer += ", prozesse.erstellungsdatum desc";
        }
        if (this.sortierung.equals("projektAsc")) {
            answer += " ,projekte.Titel";
        }
        if (this.sortierung.equals("projektDesc")) {
            answer += ", projekte.Titel desc";
        } else if (this.sortierung.equals("modulesAsc")) {
            answer += ", typModulName";
        } else if (this.sortierung.equals("modulesDesc")) {
            answer += ", typModulName desc";
        } else if (this.sortierung.equals("statusAsc")) {
            answer += ", bearbeitungsstatus";
        } else if (this.sortierung.equals("statusDesc")) {
            answer += ", bearbeitungsstatus desc";
        } else if (this.sortierung.equals("idAsc")) {
            answer = "prozesse.ProzesseID";
        } else if (this.sortierung.equals("idDesc")) {
            answer = "prozesse.ProzesseID desc";
        }

        return answer;
    }

    /*
     * Bearbeitung des Schritts übernehmen oder abschliessen
     */

    public String SchrittDurchBenutzerUebernehmen() {
        //		synchronized (this.flagWait) {
        //
        //			if (!this.flagWait) {
        //				this.flagWait = true;

        // Helper.getHibernateSession().clear();
        //				Helper.getHibernateSession().refresh(this.mySchritt);
        // reload step
        mySchritt = StepManager.getStepById(mySchritt.getId());
        mySchritt.lazyLoad();

        if (!(this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.OPEN || this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.ERROR)) {
            Helper.setFehlerMeldung("stepInWorkError");
            //					this.flagWait = false;
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
            HistoryManager.addHistory(this.mySchritt.getBearbeitungsbeginn(), this.mySchritt.getReihenfolge().doubleValue(), this.mySchritt
                    .getTitel(), HistoryEventType.stepInWork.getValue(), this.mySchritt.getProzess().getId());

            //            this.mySchritt.getProzess().getHistory().add(
            //                    new HistoryEvent(this.mySchritt.getBearbeitungsbeginn(), this.mySchritt.getReihenfolge().doubleValue(),
            //                            this.mySchritt.getTitel(), HistoryEventType.stepInWork, this.mySchritt.getProzess()));
            try {
                /*
                 * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
                 */
                //						ProcessManager.saveProcess(this.mySchritt.getProzess());
                StepManager.saveStep(mySchritt);
            } catch (DAOException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                logger.error("step couldn't get saved", e);
                //					} finally {
                //						this.flagWait = false;
            }
            /*
             * wenn es ein Image-Schritt ist, dann gleich die Images ins Home
             */

            if (this.mySchritt.isTypImagesLesen() || this.mySchritt.isTypImagesSchreiben()) {
                DownloadToHome();
            }
        }
        //			} else {
        //				Helper.setFehlerMeldung("stepInWorkError");
        //				return "";
        //			}
        //			this.flagWait = false;
        //		}
        return "task_edit";
    }

    public String EditStep() {
        mySchritt = StepManager.getStepById(mySchritt.getId());
        mySchritt.lazyLoad();

        return "task_edit";
    }

    public String TakeOverBatch() {
        // find all steps with same batch id and step status
        List<Step> currentStepsOfBatch = new ArrayList<Step>();

        String steptitle = this.mySchritt.getTitel();
        Integer batchNumber = this.mySchritt.getProzess().getBatchID();
        if (batchNumber != null) {
            // only steps with same title
            currentStepsOfBatch = StepManager.getSteps(null, "schritte.titel = \"" + steptitle + "\" and prozesse.batchID = " + batchNumber, 0,
                    Integer.MAX_VALUE);

        } else {
            return SchrittDurchBenutzerUebernehmen();
        }
        // if only one step is asigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements in batch");
        if (currentStepsOfBatch.size() == 0) {
            return "";
        }
        if (currentStepsOfBatch.size() == 1) {
            return SchrittDurchBenutzerUebernehmen();
        }

        for (Step s : currentStepsOfBatch) {
            if (s.getBearbeitungsstatusEnum().equals(StepStatus.OPEN)) {
                s.setBearbeitungsstatusEnum(StepStatus.INWORK);
                s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                s.setBearbeitungszeitpunkt(new Date());
                User ben = Helper.getCurrentUser();
                if (ben != null) {
                    s.setBearbeitungsbenutzer(ben);
                    s.setUserId(ben.getId());
                }
                if (s.getBearbeitungsbeginn() == null) {
                    Date myDate = new Date();
                    s.setBearbeitungsbeginn(myDate);
                }

                HistoryManager.addHistory(s.getBearbeitungsbeginn(), s.getReihenfolge().doubleValue(), s.getTitel(), HistoryEventType.stepInWork
                        .getValue(), s.getProzess().getId());

                if (s.isTypImagesLesen() || s.isTypImagesSchreiben()) {
                    try {
                        Paths.get(s.getProzess().getImagesOrigDirectory(false));
                    } catch (Exception e1) {

                    }
                    s.setBearbeitungszeitpunkt(new Date());

                    if (ben != null) {
                        s.setBearbeitungsbenutzer(ben);
                    }
                    this.myDav.DownloadToHome(s.getProzess(), s.getId().intValue(), !s.isTypImagesSchreiben());

                }
            }

            try {
                //                ProcessManager.saveProcess(s.getProzess());
                StepManager.saveStep(s);
            } catch (DAOException e) {
                Helper.setFehlerMeldung(Helper.getTranslation("stepSaveError"), e);
                logger.error("step couldn't get saved", e);
            }
        }

        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "task_edit_batch";
    }

    public String BatchesEdit() {
        // find all steps with same batch id and step status
        List<Step> currentStepsOfBatch = new ArrayList<Step>();

        String steptitle = this.mySchritt.getTitel();
        Integer batchNumber = this.mySchritt.getProzess().getBatchID();
        if (batchNumber != null) {
            // only steps with same title
            currentStepsOfBatch = StepManager.getSteps(null, "schritte.titel = \"" + steptitle
                    + "\"  AND batchStep = true AND schritte.prozesseID in (select prozesse.prozesseID from prozesse where batchID = " + batchNumber
                    + ")", 0, Integer.MAX_VALUE);

            //			Session session = Helper.getHibernateSession();
            //			Criteria crit = session.createCriteria(Step.class);
            //			crit.add(Restrictions.eq("titel", steptitle));
            //			// only steps with same batchid
            //			crit.createCriteria("prozess", "proc");
            //			crit.add(Restrictions.eq("proc.batchID", batchNumber));
            //			crit.add(Restrictions.eq("batchStep", true));
            //
            //			currentStepsOfBatch = crit.list();
        } else {
            return "task_edit";
        }
        // if only one step is asigned for this batch, use the single

        // Helper.setMeldung("found " + currentStepsOfBatch.size() + " elements in batch");

        if (currentStepsOfBatch.size() == 1) {
            return "task_edit";
        }
        this.setBatchHelper(new BatchStepHelper(currentStepsOfBatch));
        return "task_edit_batch";
    }

    @Deprecated
    public void saveProperties() {
    }

    public String SchrittDurchBenutzerZurueckgeben() {
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.OPEN);
        // mySchritt.setBearbeitungsbenutzer(null);
        // if we have a correction-step here then never remove startdate
        if (this.mySchritt.isCorrectionStep()) {
            this.mySchritt.setBearbeitungsbeginn(null);
        }
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }

        try {
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            //            ProcessManager.saveProcess(this.mySchritt.getProzess());
            StepManager.saveStep(mySchritt);
        } catch (DAOException e) {
        }
        // calcHomeImages();
        return "task_all";
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
         * -------------------------------- if step allows writing of images, then count all images here --------------------------------
         */
        if (this.mySchritt.isTypImagesSchreiben()) {
            try {
                // this.mySchritt.getProzess().setSortHelperImages(
                // FileUtils.getNumberOfFiles(Paths.get(this.mySchritt.getProzess().getImagesOrigDirectory())));
                HistoryAnalyserJob.updateHistory(this.mySchritt.getProzess());
            } catch (Exception e) {
                Helper.setFehlerMeldung("Error while calculation of storage and images", e);
            }
        }

        /*
         * -------------------------------- wenn das Resultat des Arbeitsschrittes zunÃ¤chst verifiziert werden soll, dann ggf. das Abschliessen
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
                if (prop.getCurrentStepAccessCondition().equals(AccessCondition.WRITEREQUIRED) && (prop.getValue() == null || prop.getValue().equals(
                        ""))) {
                    Helper.setFehlerMeldung(Helper.getTranslation("Eigenschaft") + " " + prop.getName() + " " + Helper.getTranslation(
                            "requiredValue"));
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
        //        Step so = StepObjectManager.getStepById(this.mySchritt.getId());
        new HelperSchritte().CloseStepObjectAutomatic(mySchritt, true);
        // new HelperSchritte().SchrittAbschliessen(this.mySchritt, true);
        return FilterAlleStart();
    }

    /*
     *  Eigenschaften bearbeiten
     */

    //    public String SchrittEigenschaftNeu() {
    //        this.mySchritt.setBearbeitungszeitpunkt(new Date());
    //        this.mySchrittEigenschaft = new Schritteigenschaft();
    //        return "";
    //    }

    public String SperrungAufheben() {
        MetadatenSperrung.UnlockProcess(this.mySchritt.getProzess().getId());
        return "";
    }

    /*
     * Korrekturmeldung an vorherige Schritte 
     */

    public List<Step> getPreviousStepsForProblemReporting() {
        List<Step> alleVorherigenSchritte = StepManager.getSteps("Reihenfolge desc", " schritte.prozesseID = " + this.mySchritt.getProzess().getId()
                + " AND Reihenfolge < " + this.mySchritt.getReihenfolge(), 0, Integer.MAX_VALUE);

        //		List<Step> alleVorherigenSchritte = Helper.getHibernateSession().createCriteria(Step.class)
        //				.add(Restrictions.lt("reihenfolge", this.mySchritt.getReihenfolge())).addOrder(Order.desc("reihenfolge")).createCriteria("prozess")
        //				.add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
        return alleVorherigenSchritte;
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    public String ReportProblem() {
        if (logger.isDebugEnabled()) {
            logger.debug("mySchritt.ID: " + this.mySchritt.getId().intValue());
            logger.debug("Korrekturschritt.ID: " + this.myProblemID.intValue());
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
        mySchritt.setPrioritaet(10);
        try {
            Step temp = StepManager.getStepById(myProblemID);
            temp.setBearbeitungsstatusEnum(StepStatus.OPEN);
            // if (temp.getPrioritaet().intValue() == 0)
            temp.setCorrectionStep();
            temp.setBearbeitungsende(null);
            ErrorProperty se = new ErrorProperty();

            se.setTitel(Helper.getTranslation("Korrektur notwendig"));
            se.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + this.problemMessage);
            se.setType(PropertyType.messageError);
            se.setCreationDate(myDate);
            se.setSchritt(temp);
            String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + this.problemMessage;
            LogEntry logEntry = new LogEntry();
            logEntry.setContent(message);
            logEntry.setCreationDate(new Date());
            logEntry.setProcessId(mySchritt.getProzess().getId());
            logEntry.setType(LogType.ERROR);
            if (ben != null) {
                logEntry.setUserName(ben.getNachVorname());
            }
            ProcessManager.saveLogEntry(logEntry);

            temp.getEigenschaften().add(se);
            StepManager.saveStep(temp);
            HistoryManager.addHistory(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError.getValue(), temp
                    .getProzess().getId());

            //            this.mySchritt.getProzess().getHistory().add(
            //                    new HistoryEvent(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError, temp.getProzess()));
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */

            List<Step> alleSchritteDazwischen = StepManager.getSteps("Reihenfolge desc", " schritte.prozesseID = " + this.mySchritt.getProzess()
                    .getId() + " AND Reihenfolge <= " + this.mySchritt.getReihenfolge() + "  AND Reihenfolge > " + temp.getReihenfolge(), 0,
                    Integer.MAX_VALUE);

            //			List<Step> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Step.class)
            //					.add(Restrictions.le("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.gt("reihenfolge", temp.getReihenfolge()))
            //					.addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
            for (Step step : alleSchritteDazwischen) {
                step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
                // if (step.getPrioritaet().intValue() == 0)
                step.setCorrectionStep();
                step.setBearbeitungsende(null);
                ErrorProperty seg = new ErrorProperty();
                seg.setTitel(Helper.getTranslation("Korrektur notwendig"));
                seg.setWert(Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + this.problemMessage);
                seg.setSchritt(step);
                seg.setType(PropertyType.messageImportant);
                seg.setCreationDate(new Date());
                step.getEigenschaften().add(seg);
                StepManager.saveStep(step);
            }
            if (temp.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(temp);
                myThread.start();
            }

            StepManager.saveStep(mySchritt);
            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            ProcessManager.saveProcessInformation(this.mySchritt.getProzess());
        } catch (DAOException e) {
        }

        this.problemMessage = "";
        this.myProblemID = 0;
        return FilterAlleStart();
    }

    /*
     *  Problem-behoben-Meldung an nachfolgende Schritte
     */

    public List<Step> getNextStepsForProblemSolution() {

        List<Step> alleNachfolgendenSchritte = StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.mySchritt.getProzess().getId()
                + " AND Reihenfolge > " + this.mySchritt.getReihenfolge() + " AND prioritaet = 10", 0, Integer.MAX_VALUE);

        //		List<Step> alleNachfolgendenSchritte = Helper.getHibernateSession().createCriteria(Step.class)
        //				.add(Restrictions.gt("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.eq("prioritaet", 10))
        //				.addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
        return alleNachfolgendenSchritte;
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    public String SolveProblem() {
        Date now = new Date();
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        this.mySchritt.setBearbeitungsstatusEnum(StepStatus.DONE);
        this.mySchritt.setBearbeitungsende(now);
        this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }

        try {

            Step temp = StepManager.getStepById(this.mySolutionID);
            /*
             * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
             */
            List<Step> alleSchritteDazwischen = StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.mySchritt.getProzess().getId()
                    + " AND Reihenfolge >= " + this.mySchritt.getReihenfolge() + "  AND Reihenfolge <= " + temp.getReihenfolge(), 0,
                    Integer.MAX_VALUE);

            //			List<Step> alleSchritteDazwischen = Helper.getHibernateSession().createCriteria(Step.class)
            //			       .add(Restrictions.ge("reihenfolge", this.mySchritt.getReihenfolge())).add(Restrictions.le("reihenfolge", temp.getReihenfolge()))
            //					.addOrder(Order.asc("reihenfolge")).createCriteria("prozess").add(Restrictions.idEq(this.mySchritt.getProzess().getId())).list();
            for (Iterator<Step> iter = alleSchritteDazwischen.iterator(); iter.hasNext();) {
                Step step = iter.next();
                step.setBearbeitungsstatusEnum(StepStatus.DONE);
                step.setBearbeitungsende(now);
                step.setPrioritaet(Integer.valueOf(0));
                if (step.getId().intValue() == temp.getId().intValue()) {
                    step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                    step.setCorrectionStep();
                    step.setBearbeitungsende(null);
                    // step.setBearbeitungsbeginn(null);
                    step.setBearbeitungszeitpunkt(now);
                }
                ErrorProperty seg = new ErrorProperty();
                seg.setTitel(Helper.getTranslation("Korrektur durchgefuehrt"));
                step.setBearbeitungszeitpunkt(new Date());
                if (ben != null) {
                    step.setBearbeitungsbenutzer(ben);
                }
                seg.setWert("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + Helper.getTranslation(
                        "KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage);
                seg.setSchritt(step);
                seg.setType(PropertyType.messageImportant);
                seg.setCreationDate(new Date());
                step.getEigenschaften().add(seg);
                StepManager.saveStep(step);
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + this.solutionMessage;

            LogEntry logEntry = new LogEntry();
            logEntry.setContent(message);
            logEntry.setCreationDate(new Date());
            logEntry.setProcessId(mySchritt.getProzess().getId());
            logEntry.setType(LogType.INFO);
            if (ben != null) {
                logEntry.setUserName(ben.getNachVorname());
            }
            ProcessManager.saveLogEntry(logEntry);

            ProcessManager.saveProcessInformation(this.mySchritt.getProzess());

            if (temp.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(temp);
                myThread.start();
            }
        } catch (DAOException e) {
        }

        this.solutionMessage = "";
        this.mySolutionID = 0;
        return FilterAlleStart();
    }

    /*
     * Upload und Download der Images
     */

    public String UploadFromHome() {
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.UploadFromHome(this.mySchritt.getProzess());
        Helper.setMeldung(null, "Removed directory from user home", this.mySchritt.getProzess().getTitel());
        return "";
    }

    public String DownloadToHome() {
        try {
            Paths.get(this.mySchritt.getProzess().getImagesOrigDirectory(true));
        } catch (Exception e1) {

        }
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.myDav.DownloadToHome(this.mySchritt.getProzess(), this.mySchritt.getId().intValue(), !this.mySchritt.isTypImagesSchreiben());

        return "";
    }

    public String UploadFromHomeAlle() throws NumberFormatException, DAOException {

        List<String> fertigListe = this.myDav.UploadFromHomeAlle(DONEDIRECTORYNAME);
        List<String> geprueft = new ArrayList<String>();
        /*
         * -------------------------------- die hochgeladenen Prozess-IDs durchlaufen und auf abgeschlossen setzen --------------------------------
         */
        if (fertigListe != null && fertigListe.size() > 0 && this.nurOffeneSchritte) {
            this.nurOffeneSchritte = false;
            FilterAlleStart();
        }
        for (String element : fertigListe) {
            String myID = element.substring(element.indexOf("[") + 1, element.indexOf("]")).trim();

            String sql = FilterHelper.criteriaBuilder("id:" + myID, false, false, false, false, false, true);
            List<Step> stepList = StepManager.getSteps(sortList(), sql);

            for (Step step : stepList) {
                if (step.getBearbeitungsstatusEnum().equals(StepStatus.INWORK)) {
                    this.mySchritt = step;
                    if (SchrittDurchBenutzerAbschliessen() != "") {
                        geprueft.add(element);
                    }
                    this.mySchritt.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                }
            }
        }

        this.myDav.removeFromHomeAlle(geprueft, DONEDIRECTORYNAME);
        Helper.setMeldung(null, "removed " + geprueft.size() + " directories from user home:", DONEDIRECTORYNAME);
        return FilterAlleStart();
    }

    @SuppressWarnings("unchecked")
    public String DownloadToHomePage() {
        User ben = Helper.getCurrentUser();

        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getList().iterator(); iter.hasNext();) {
            Step step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
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
        // calcHomeImages();
        Helper.setMeldung(null, "Created directies in user home", "");
        return FilterAlleStart();
    }

    @SuppressWarnings("unchecked")
    public String DownloadToHomeHits() {
        User ben = Helper.getCurrentUser();

        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getCompleteList().iterator(); iter.hasNext();) {
            Step step = iter.next();
            if (step.getBearbeitungsstatusEnum() == StepStatus.OPEN) {
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
        // calcHomeImages();
        Helper.setMeldung(null, "Created directories in user home", "");
        return FilterAlleStart();
    }

    public String getScriptPath() {

        return this.scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public void executeScript() {
        //        StepObject so = StepObjectManager.getStepById(this.mySchritt.getId());
        new HelperSchritte().executeScriptForStepObject(mySchritt, this.scriptPath, false);

    }

    /**
     * call module for this step ================================================================
     * 
     * @throws IOException
     */
    @Deprecated
    public void executeModule() {
    }

    @Deprecated
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
                        // gesamtAnzahlImages +=
                        // myDav.getAnzahlImages(step.getProzess().getImagesOrigDirectory());
                        this.gesamtAnzahlImages += NIOFileUtils.getNumberOfFiles(step.getProzess().getImagesOrigDirectory(false));
                    }
                } catch (Exception e) {
                    logger.error(e);
                }
            }
        }
    }

    /*
     *  Getter und Setter
     */

    public Process getMyProzess() {
        return this.myProzess;
    }

    public void setMyProzess(Process myProzess) {
        this.myProzess = myProzess;
    }

    public Step getMySchritt() {
        try {
            schrittPerParameterLaden();
        } catch (NumberFormatException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        }
        return this.mySchritt;
    }

    public void setMySchritt(Step mySchritt) {
        myPlugin = null;
        this.modusBearbeiten = "";
        this.mySchritt = mySchritt;
        loadProcessProperties();
        if (this.mySchritt.getStepPlugin() != null && !this.mySchritt.getStepPlugin().isEmpty()) {
            myPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, this.mySchritt.getStepPlugin());
            if (myPlugin == null) {
                Helper.setFehlerMeldung("Plugin could not be found:", this.mySchritt.getStepPlugin());
            } else {
                myPlugin.initialize(mySchritt, "/task_edit");
                //                if (myPlugin.getPluginGuiType() == PluginGuiType.FULL || myPlugin.getPluginGuiType() == PluginGuiType.PART) {
                //                    runPlugin();
                //                }
            }
        }
    }

    public String runPlugin() {
        //        Helper.setMeldung("Starte Plugin");
        //        Helper.setMeldung(mySchritt.getStepPlugin());

        if (myPlugin.getPluginGuiType() == PluginGuiType.FULL) {
            String mypath = myPlugin.getPagePath();
            if (logger.isDebugEnabled()) {
                logger.debug("Plugin is full GUI");
                //            String mypath = "/ui/plugins/step/" + myPlugin.getTitle() + "/plugin.xhtml";
                logger.debug("open plugin GUI: " + mypath);
            }
            myPlugin.execute();
            return mypath;
        } else {
            myPlugin.execute();
            myPlugin.finish();
            return "";
        }
    }

    public IStepPlugin getMyPlugin() {
        return myPlugin;
    }

    public void setMyPlugin(IStepPlugin myPlugin) {
        this.myPlugin = myPlugin;
    }

    public void setStep(Step step) {
        this.mySchritt = step;
        loadProcessProperties();
    }

    public Step getStep() {
        return this.mySchritt;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }

    public Integer getMyProblemID() {
        return this.myProblemID;
    }

    public void setMyProblemID(Integer myProblemID) {
        this.myProblemID = myProblemID;
    }

    public Integer getMySolutionID() {
        return this.mySolutionID;
    }

    public void setMySolutionID(Integer mySolutionID) {
        this.mySolutionID = mySolutionID;
    }

    public String getProblemMessage() {
        return this.problemMessage;
    }

    public void setProblemMessage(String problemMessage) {
        this.problemMessage = problemMessage;
    }

    public String getSolutionMessage() {
        return this.solutionMessage;
    }

    public void setSolutionMessage(String solutionMessage) {
        this.solutionMessage = solutionMessage;
    }

    //    public Schritteigenschaft getMySchrittEigenschaft() {
    //        return this.mySchrittEigenschaft;
    //    }
    //
    //    public void setMySchrittEigenschaft(Schritteigenschaft mySchrittEigenschaft) {
    //        this.mySchrittEigenschaft = mySchrittEigenschaft;
    //    }

    /*
     * Parameter per Get Ã¼bergeben bekommen und entsprechen den passenden Schritt laden 
     */

    /**
     * prüfen, ob per Parameter vielleicht zunÃ¤chst ein anderer geladen werden soll
     * 
     * @throws DAOException , NumberFormatException
     */
    private void schrittPerParameterLaden() throws DAOException, NumberFormatException {
        String param = Helper.getRequestParameter("myid");
        if (param != null && !param.equals("")) {
            /*
             * wenn bisher noch keine aktuellen Schritte ermittelt wurden, dann dies jetzt nachholen, damit die Liste vollstÃ¤ndig ist
             */
            if (this.paginator == null && (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}") != null) {
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
    public void SelectionNone() {
        for (Iterator<Step> iter = (Iterator<Step>) this.paginator.getList().iterator(); iter.hasNext();) {
            Step s = iter.next();
            s.setSelected(false);
        }
    }

    /*
     * Downloads
     */

    public void DownloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.mySchritt.getProzess());
        tiff.ExportStart();
    }

    public void ExportDMS() {
        IExportPlugin dms = null;
        if (StringUtils.isNotBlank(mySchritt.getStepPlugin())) {
            try {
                dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, mySchritt.getStepPlugin());
            } catch (Exception e) {
                logger.error("Can't load export plugin, use default plugin", e);
                dms = new ExportDms();
            }
        }
        if (dms == null) {
            dms = new ExportDms();
        }
        try {
            dms.startExport(this.mySchritt.getProzess());
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on export", e.getMessage());
            logger.error(e);
        }
    }

    public boolean isNurOffeneSchritte() {
        return this.nurOffeneSchritte;
    }

    public void setNurOffeneSchritte(boolean nurOffeneSchritte) {
        this.nurOffeneSchritte = nurOffeneSchritte;
    }

    public boolean isNurEigeneSchritte() {
        return this.nurEigeneSchritte;
    }

    public void setNurEigeneSchritte(boolean nurEigeneSchritte) {
        this.nurEigeneSchritte = nurEigeneSchritte;
    }

    public HashMap<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    public void addLogEntry() {
        if (StringUtils.isNotBlank(content)) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            LogEntry logEntry = new LogEntry();
            logEntry.setContent(content);
            logEntry.setSecondContent(secondContent);
            logEntry.setThirdContent(thirdContent);
            logEntry.setCreationDate(new Date());
            logEntry.setProcessId(mySchritt.getProzess().getId());
            logEntry.setType(LogType.USER);
            logEntry.setUserName(user.getNachVorname());
            ProcessManager.saveLogEntry(logEntry);
            mySchritt.getProzess().getProcessLog().add(logEntry);
            this.content = "";
            secondContent = "";
            thirdContent = "";
        }
    }

    public ProcessProperty getProcessProperty() {
        return this.processProperty;
    }

    public void setProcessProperty(ProcessProperty processProperty) {
        this.processProperty = processProperty;
    }

    public List<ProcessProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    private void loadProcessProperties() {
        this.containers = new TreeMap<Integer, PropertyListObject>();
        this.processPropertyList = PropertyParser.getPropertiesForStep(this.mySchritt);

        for (ProcessProperty pt : this.processPropertyList) {
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

            //            try {
            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
            Helper.setMeldung("propertiesSaved");
            //            } catch (DAOException e) {
            //                myLogger.error(e);
            //                Helper.setFehlerMeldung("propertiesNotSaved");
            //            }
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
            //            try {
            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
            //                ProcessManager.saveProcess(this.mySchritt.getProzess());
            Helper.setMeldung("propertySaved");
            //            } catch (DAOException e) {
            //                myLogger.error(e);
            //                Helper.setFehlerMeldung("propertyNotSaved");
            //            }
        }
        loadProcessProperties();
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    public List<Integer> getContainerList() {
        return new ArrayList<Integer>(this.containers.keySet());
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
        // if (this.processProperty.getProzesseigenschaft().getId() != null) {
        this.mySchritt.getProzess().getEigenschaften().remove(this.processProperty.getProzesseigenschaft());
        // this.mySchritt.getProzess().removeProperty(this.processProperty.getProzesseigenschaft());
        // }

        List<Processproperty> props = this.mySchritt.getProzess().getEigenschaftenList();
        for (Processproperty pe : props) {
            if (pe.getTitel() == null) {
                this.mySchritt.getProzess().getEigenschaften().remove(pe);
            }
        }
        //        try {
        //            ProcessManager.saveProcess(this.mySchritt.getProzess());
        PropertyManager.deleteProcessProperty(processProperty.getProzesseigenschaft());
        //        } catch (DAOException e) {
        //            myLogger.error(e);
        //            Helper.setFehlerMeldung("propertiesNotDeleted");
        //        }
        // saveWithoutValidation();
        loadProcessProperties();
    }

    public void duplicateProperty() {
        ProcessProperty pt = this.processProperty.getClone(0);
        this.processPropertyList.add(pt);
        this.processProperty = pt;
        saveCurrentProperty();
        loadProcessProperties();
    }

    public BatchStepHelper getBatchHelper() {
        return this.batchHelper;
    }

    public void setBatchHelper(BatchStepHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public Integer getContainer() {
        return this.container;
    }

    public void setContainer(Integer container) {
        this.container = container;
        if (container != null && container > 0) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    public List<ProcessProperty> getContainerProperties() {
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();
        // int currentContainer = this.processProperty.getContainer();

        if (this.container != null && this.container > 0) {
            for (ProcessProperty pp : this.processPropertyList) {
                if (pp.getContainer() == this.container) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    public String duplicateContainer() {
        Integer currentContainer = this.processProperty.getContainer();
        List<ProcessProperty> plist = new ArrayList<ProcessProperty>();
        // search for all properties in container
        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getContainer() == currentContainer) {
                plist.add(pt);
            }
        }
        int newContainerNumber = 0;
        if (currentContainer > 0) {
            newContainerNumber++;
            // find new unused container number
            boolean search = true;
            while (search) {
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    newContainerNumber++;
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

    public void setShowAutomaticTasks(boolean showAutomaticTasks) {
        this.showAutomaticTasks = showAutomaticTasks;
    }

    public boolean getHideCorrectionTasks() {
        return hideCorrectionTasks;
    }

    public void setHideCorrectionTasks(boolean hideCorrectionTasks) {
        this.hideCorrectionTasks = hideCorrectionTasks;
    }

    public boolean isHideStepsFromOtherUsers() {
        return hideStepsFromOtherUsers;
    }

    public void setHideStepsFromOtherUsers(boolean hideStepsFromOtherUsers) {
        this.hideStepsFromOtherUsers = hideStepsFromOtherUsers;
    }

    public String callStepPlugin() {
        if (mySchritt.getStepPlugin() != null && mySchritt.getStepPlugin().length() > 0) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
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
}
