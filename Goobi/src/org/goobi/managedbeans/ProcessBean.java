package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *          - http://digiverso.com
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.goobi.api.mq.QueueType;
import org.goobi.api.mq.TaskTicket;
import org.goobi.api.mq.TicketGenerator;
import org.goobi.beans.Docket;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.goobiScript.GoobiScriptResult;
import org.goobi.goobiScript.IGoobiScript;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.UserRole;
import org.goobi.production.flow.helper.SearchColumn;
import org.goobi.production.flow.helper.SearchResultHelper;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jfree.chart.plot.PlotOrientation;
import org.reflections.Reflections;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.intranda.commons.chart.renderer.CSVRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.export.download.ExportPdf;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import io.goobi.workflow.xslt.XsltPreparatorDocket;
import lombok.Getter;
import lombok.Setter;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@Named("ProzessverwaltungForm")
@SessionScoped
public class ProcessBean extends BasicBean implements Serializable {
    private static final long serialVersionUID = 2838270843176821134L;
    private static final Logger logger = LogManager.getLogger(ProcessBean.class);
    private Process myProzess = new Process();
    private Step mySchritt = new Step();
    private StatisticsManager statisticsManager;

    @Getter
    private List<ProcessCounterObject> myAnzahlList;
    private HashMap<String, Integer> myAnzahlSummary;
    private Processproperty myProzessEigenschaft;
    private User myBenutzer;
    private Template myVorlage;
    private Templateproperty myVorlageEigenschaft;
    private Masterpiece myWerkstueck;
    private Masterpieceproperty myWerkstueckEigenschaft;
    private Usergroup myBenutzergruppe;
    private String modusAnzeige = "aktuell";
    private String modusBearbeiten = "";
    private String goobiScript;
    private HashMap<String, Boolean> anzeigeAnpassen;
    private String myNewProcessTitle;
    private String selectedXslt = "";
    private StatisticsRenderingElement myCurrentTable;
    private boolean showClosedProcesses = false;
    private boolean showArchivedProjects = false;
    private List<ProcessProperty> processPropertyList;
    private ProcessProperty processProperty;
    private Map<Integer, PropertyListObject> containers = new TreeMap<>();
    private Integer container;
    private String userDisplayMode = "";

    private boolean dispaySearchResult = false;
    private List<SearchColumn> searchField = new ArrayList<>();
    private List<SelectItem> possibleItems = null;
    private SearchColumn currentField = null;
    private int order = 0;

    private boolean showStatistics = false;

    private static String DONEDIRECTORYNAME = "fertig/";

    private DatabasePaginator usergroupPaginator;
    private DatabasePaginator userPaginator;

    private List<String> stepPluginList = new ArrayList<>();
    private List<String> validationPluginList = new ArrayList<>();

    @Getter
    @Setter
    private int goobiScriptHitsCount = 0;
    @Getter
    @Setter
    private Integer goobiScriptHitsCountUser = null;
    @Getter
    private String goobiScriptMode;
    @Getter
    private String goobiScriptHitsImage;

    private List<Process> availableProcessTemplates = null;

    @Getter
    @Setter
    private Process processToChange;
    @Getter
    @Setter
    private Process template;

    private List<StringPair> allGoobiScripts;

    @Getter
    @Setter
    private boolean createNewStepAllowParallelTask;

    @Getter
    private Map<String, List<String>> displayableMetadataMap = new HashMap<>();

    private IStepPlugin currentPlugin;

    @Inject
    private StepBean bean;

    public ProcessBean() {
        this.anzeigeAnpassen = new HashMap<>();

        this.sortierung = "titel";
        /*
         * Vorgangsdatum generell anzeigen?
         */
        LoginBean login = Helper.getLoginBean();
        if (login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("lockings", login.getMyBenutzer().isDisplayLocksColumn());
            this.anzeigeAnpassen.put("swappedOut", login.getMyBenutzer().isDisplaySwappingColumn());
            this.anzeigeAnpassen.put("selectionBoxes", login.getMyBenutzer().isDisplaySelectBoxes());
            this.anzeigeAnpassen.put("processId", login.getMyBenutzer().isDisplayIdColumn());
            this.anzeigeAnpassen.put("batchId", login.getMyBenutzer().isDisplayBatchColumn());
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isDisplayProcessDateColumn());

            this.anzeigeAnpassen.put("thumbnail", login.getMyBenutzer().isDisplayThumbColumn());
            this.anzeigeAnpassen.put("metadatadetails", login.getMyBenutzer().isDisplayMetadataColumn());
            this.anzeigeAnpassen.put("gridview", login.getMyBenutzer().isDisplayGridView());

            showClosedProcesses = login.getMyBenutzer().isDisplayFinishedProcesses();
            showArchivedProjects = login.getMyBenutzer().isDisplayDeactivatedProjects();
            anzeigeAnpassen.put("institution", login.getMyBenutzer().isDisplayInstitutionColumn());
            anzeigeAnpassen.put("editionDate",
                    ConfigurationHelper.getInstance().isProcesslistShowEditionData() ? login.getMyBenutzer().isDisplayLastEditionDate() : false);
            anzeigeAnpassen.put("editionUser",
                    ConfigurationHelper.getInstance().isProcesslistShowEditionData() ? login.getMyBenutzer().isDisplayLastEditionUser() : false);
            anzeigeAnpassen.put("editionTask",
                    ConfigurationHelper.getInstance().isProcesslistShowEditionData() ? login.getMyBenutzer().isDisplayLastEditionTask() : false);
            if (StringUtils.isNotBlank(login.getMyBenutzer().getProcessListDefaultSortField())) {
                sortierung = login.getMyBenutzer().getProcessListDefaultSortField() + login.getMyBenutzer().getProcessListDefaultSortOrder();
            }

        } else {
            this.anzeigeAnpassen.put("lockings", false);
            this.anzeigeAnpassen.put("swappedOut", false);
            this.anzeigeAnpassen.put("selectionBoxes", false);
            this.anzeigeAnpassen.put("processId", false);
            this.anzeigeAnpassen.put("batchId", false);
            this.anzeigeAnpassen.put("processDate", false);
            anzeigeAnpassen.put("institution", false);
        }
        DONEDIRECTORYNAME = ConfigurationHelper.getInstance().getDoneDirectoryName();

        searchField.add(new SearchColumn(order++));

        stepPluginList = PluginLoader.getListOfPlugins(PluginType.Step);
        stepPluginList.addAll(PluginLoader.getListOfPlugins(PluginType.Export));
        Collections.sort(stepPluginList);

        validationPluginList = PluginLoader.getListOfPlugins(PluginType.Validation);
        Collections.sort(validationPluginList);

    }

    /**
     * needed for ExtendedSearch
     * 
     * @return
     */
    public boolean getInitialize() {
        return true;
    }

    public String Neu() {
        this.myProzess = new Process();
        this.myNewProcessTitle = "";
        this.modusBearbeiten = "prozess";
        return "process_edit";
    }

    public String NeuVorlage() {
        this.myProzess = new Process();
        this.myNewProcessTitle = "";
        this.myProzess.setIstTemplate(true);
        this.modusBearbeiten = "prozess";
        return "process_edit";
    }

    public String editProcess() {
        reload();

        return "process_edit";
    }

    public String Speichern() {
        /*
         * wenn der Vorgangstitel geändert wurde, wird dieser geprüft und bei erfolgreicher Prüfung an allen relevanten Stellen mitgeändert
         */
        if (this.myProzess != null && this.myProzess.getTitel() != null) {
            if (!this.myProzess.getTitel().equals(this.myNewProcessTitle)) {

                String validateRegEx = ConfigurationHelper.getInstance().getProcessTiteValidationlRegex();
                if (!this.myNewProcessTitle.matches(validateRegEx)) {
                    this.modusBearbeiten = "prozess";
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
                    return "";
                } else if (ProcessManager.countProcessTitle(myNewProcessTitle, myProzess.getProjekt().getInstitution()) != 0) {
                    this.modusBearbeiten = "prozess";
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("UngueltigeDaten:") + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
                    return "";

                } else {
                    myProzess.changeProcessTitle(myNewProcessTitle);
                }
            }
        } else {
            Helper.setFehlerMeldung("titleEmpty");
        }
        try {
            ProcessManager.saveProcess(this.myProzess);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
        }
        if (paginator != null) {
            paginator.load();
        }
        return "";
    }

    public String Loeschen() {
        deleteMetadataDirectory();
        //        try {
        ProcessManager.deleteProcess(this.myProzess);
        //        } catch (DAOException e) {
        //            Helper.setFehlerMeldung("could not delete ", e);
        //            return "";
        //        }
        Helper.setMeldung("Process deleted");
        if (this.modusAnzeige == "vorlagen") {
            return FilterVorlagen();
        } else {
            return FilterAlleStart();
        }

    }

    public boolean getRenderReimport() {
        return ConfigurationHelper.getInstance().isRenderReimport();
    }

    public void reImportProcess() throws IOException, InterruptedException, SwapException, DAOException {
        String processId = this.myProzess.getId().toString();

        Path processFolder = Paths.get(this.myProzess.getProcessDataDirectory());
        Path importFolder = processFolder.resolve("import");

        Path dbExportFile = importFolder.resolve(processId + "_db_export.xml");

        if (!StorageProvider.getInstance().isFileExists(dbExportFile)) {
            Helper.setFehlerMeldung("DB export file does not exist in " + dbExportFile);
            return;
        }

        StorageProvider.getInstance().copyFile(dbExportFile, processFolder.resolve(processId + "_db_export.xml"));

        TaskTicket importTicket = TicketGenerator.generateSimpleTicket("DatabaseInformationTicket");
        //filename of xml file is "<processId>_db_export.xml"
        importTicket.setProcessName(processId);

        importTicket.getProperties().put("processFolder", processFolder.toString());
        importTicket.getProperties().put("createNewProcessId", "false");
        importTicket.getProperties().put("tempFolder", null);
        importTicket.getProperties().put("rule", "Autodetect rule");
        importTicket.getProperties().put("deleteOldProcess", "true");
        try {
            TicketGenerator.submitInternalTicket(importTicket, QueueType.FAST_QUEUE , "DatabaseInformationTicket", 0);
        } catch (JMSException e) {
            logger.error("Error adding TaskTicket to queue", e);
            LogEntry errorEntry = LogEntry.build(this.myProzess.getId())
                    .withType(LogType.ERROR)
                    .withContent("Error reading metadata for process" + this.myProzess.getTitel())
                    .withCreationDate(new Date())
                    .withUsername("automatic");
            ProcessManager.saveLogEntry(errorEntry);
        }
    }

    public String ContentLoeschen() {
        // deleteMetadataDirectory();
        try {
            Path ocr = Paths.get(this.myProzess.getOcrDirectory());
            if (StorageProvider.getInstance().isFileExists(ocr)) {
                StorageProvider.getInstance().deleteDir(ocr);
            }
            Path images = Paths.get(this.myProzess.getImagesDirectory());
            if (StorageProvider.getInstance().isFileExists(images)) {
                StorageProvider.getInstance().deleteDir(images);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
        Helper.addMessageToProcessLog(myProzess.getId(), LogType.DEBUG, "Deleted content for this process in process details.");

        Helper.setMeldung("Content deleted");
        return "";
    }

    private void deleteMetadataDirectory() {
        for (Step step : this.myProzess.getSchritteList()) {
            this.mySchritt = step;
            deleteSymlinksFromUserHomes();
        }
        try {
            StorageProvider.getInstance().deleteDir(Paths.get(this.myProzess.getProcessDataDirectory()));
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
    }

    /*
     * Filter
     */

    public String FilterAktuelleProzesse() {
        this.statisticsManager = null;
        this.myAnzahlList = null;
        ProcessManager m = new ProcessManager();
        String sql = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        if (this.modusAnzeige.equals("vorlagen")) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.istTemplate = true ";
        } else {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.istTemplate = false ";
        }
        if (!this.showClosedProcesses && !this.modusAnzeige.equals("vorlagen")) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        }

        paginator = new DatabasePaginator(sortList(), sql, m, "process_all");

        this.modusAnzeige = "aktuell";
        return "process_all";
    }

    public String FilterAktuelleProzesseOfGoobiScript(String status) {

        List<GoobiScriptResult> resultList = Helper.getSessionBean().getGsm().getGoobiScriptResults();
        filter = "\"id:";
        for (GoobiScriptResult gsr : resultList) {
            if (gsr.getResultType().toString().equals(status)) {
                filter += gsr.getProcessId() + " ";
            }
        }
        filter += "\"";
        return FilterAktuelleProzesse();
    }

    public String FilterVorlagen() {
        this.statisticsManager = null;
        this.myAnzahlList = null;

        //        try {
        //            this.myFilteredDataSource = new UserTemplatesFilter(true);
        //            Criteria crit = this.myFilteredDataSource.getCriteria();
        //            if (!this.showArchivedProjects) {
        //                crit.add(Restrictions.not(Restrictions.eq("proj.projectIsArchived", true)));
        //            }
        ////            sortList(crit, false);
        //            this.page = new Page(crit, 0);
        //        } catch (HibernateException he) {
        //            Helper.setFehlerMeldung("ProzessverwaltungForm.FilterVorlagen", he);
        //            return "";
        //        }

        String sql = FilterHelper.criteriaBuilder(filter, true, null, null, null, true, false);

        if (!this.showClosedProcesses && !this.modusAnzeige.equals("vorlagen")) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        }
        ProcessManager m = new ProcessManager();
        paginator = new DatabasePaginator(sortList(), sql, m, "process_all");
        this.modusAnzeige = "vorlagen";
        return "process_all";
    }

    public String NeuenVorgangAnlegen() {
        FilterVorlagen();
        if (this.paginator.getTotalResults() == 1) {
            Process einziger = (Process) this.paginator.getList().get(0);
            ProzesskopieForm pkf = (ProzesskopieForm) Helper.getBeanByName("ProzesskopieForm", ProzesskopieForm.class);
            pkf.setProzessVorlage(einziger);
            return pkf.Prepare();
        } else {
            return "process_all";
        }
    }

    /**
     * Anzeige der Sammelbände filtern
     */
    public String FilterAlleStart() {
        this.statisticsManager = null;
        this.myAnzahlList = null;

        String sql = FilterHelper.criteriaBuilder(filter, null, null, null, null, true, false);
        if (this.modusAnzeige.equals("vorlagen")) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.istTemplate = true ";
        } else {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.istTemplate = false ";
        }
        if (!this.showClosedProcesses && !this.modusAnzeige.equals("vorlagen")) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        }

        ProcessManager m = new ProcessManager();
        paginator = new DatabasePaginator(sortList(), sql, m, "process_all");

        return "process_all";
    }

    private String sortList() {
        String answer = "prozesse.titel";
        if (this.sortierung.equals("titelAsc")) {
            answer = "prozesse.titel";
        } else if (this.sortierung.equals("titelDesc")) {
            answer = "prozesse.titel desc";
        } else if (this.sortierung.equals("batchAsc")) {
            answer = "batchID";
        } else if (this.sortierung.equals("batchDesc")) {
            answer = "batchID desc";
        } else if (this.sortierung.equals("projektAsc")) {
            answer = "projekte.Titel";
        } else if (this.sortierung.equals("projektDesc")) {
            answer = "projekte.Titel desc";
        } else if (this.sortierung.equals("vorgangsdatumAsc")) {
            answer = "erstellungsdatum";
        } else if (this.sortierung.equals("vorgangsdatumDesc")) {
            answer = "erstellungsdatum desc";
        } else if (this.sortierung.equals("fortschrittAsc")) {
            answer = "sortHelperStatus";
        } else if (this.sortierung.equals("fortschrittDesc")) {
            answer = "sortHelperStatus desc";
        } else if (this.sortierung.equals("idAsc")) {
            answer = "prozesse.ProzesseID";
        } else if (this.sortierung.equals("idDesc")) {
            answer = "prozesse.ProzesseID desc";
        } else if (sortierung.equals("institutionAsc")) {
            answer = "institution.shortName";
        } else if (sortierung.equals("institutionDesc")) {
            answer = "institution.shortName desc";
        }

        return answer;
    }

    /*
     * Eigenschaften
     */
    public String ProzessEigenschaftLoeschen() {
        myProzess.getEigenschaften().remove(myProzessEigenschaft);
        PropertyManager.deleteProcessProperty(myProzessEigenschaft);
        //            ProcessManager.saveProcess(myProzess);
        return "";
    }

    //    public String SchrittEigenschaftLoeschen() {
    //        try {
    //            mySchritt.getEigenschaften().remove(mySchrittEigenschaft);
    //            ProcessManager.saveProcess(myProzess);
    //        } catch (DAOException e) {
    //            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
    //        }
    //        return "";
    //    }

    public String VorlageEigenschaftLoeschen() {

        myVorlage.getEigenschaften().remove(myVorlageEigenschaft);
        PropertyManager.deleteTemplateProperty(myVorlageEigenschaft);
        //            ProcessManager.saveProcess(myProzess);

        return "";
    }

    public String WerkstueckEigenschaftLoeschen() {
        myWerkstueck.getEigenschaften().remove(myWerkstueckEigenschaft);
        PropertyManager.deleteMasterpieceProperty(myWerkstueckEigenschaft);
        //            ProcessManager.saveProcess(myProzess);
        return "";
    }

    public String ProzessEigenschaftNeu() {
        myProzessEigenschaft = new Processproperty();
        return "";
    }

    //    public String SchrittEigenschaftNeu() {
    //        mySchrittEigenschaft = new Schritteigenschaft();
    //        return "";
    //    }

    public String VorlageEigenschaftNeu() {
        myVorlageEigenschaft = new Templateproperty();
        return "";
    }

    public String WerkstueckEigenschaftNeu() {
        myWerkstueckEigenschaft = new Masterpieceproperty();
        return "";
    }

    public String ProzessEigenschaftUebernehmen() {
        if (!myProzess.getEigenschaften().contains(myProzessEigenschaft)) {
            myProzess.getEigenschaften().add(myProzessEigenschaft);
            myProzessEigenschaft.setProzess(myProzess);
        }
        //        Speichern();
        PropertyManager.saveProcessProperty(myProzessEigenschaft);
        return "";
    }

    //    public String SchrittEigenschaftUebernehmen() {
    //        mySchritt.getEigenschaften().add(mySchrittEigenschaft);
    //        mySchrittEigenschaft.setSchritt(mySchritt);
    //        Speichern();
    //        return "";
    //    }

    public String VorlageEigenschaftUebernehmen() {
        if (!myVorlage.getEigenschaften().contains(myVorlageEigenschaft)) {
            myVorlage.getEigenschaften().add(myVorlageEigenschaft);
            myVorlageEigenschaft.setVorlage(myVorlage);
        }
        PropertyManager.saveTemplateProperty(myVorlageEigenschaft);
        //        Speichern();
        return "";
    }

    public String WerkstueckEigenschaftUebernehmen() {
        if (!myWerkstueck.getEigenschaften().contains(myWerkstueckEigenschaft)) {
            myWerkstueck.getEigenschaften().add(myWerkstueckEigenschaft);
            myWerkstueckEigenschaft.setWerkstueck(myWerkstueck);
        }
        //        Speichern();
        PropertyManager.saveMasterpieceProperty(myWerkstueckEigenschaft);
        return "";
    }

    /*
     * Schritte
     */

    public String SchrittNeu() {
        // Process is needed for the predefined order
        this.createNewStepAllowParallelTask = false;
        this.mySchritt = new Step(this.myProzess);
        this.modusBearbeiten = "schritt";
        return "process_edit_step";
    }

    public String SchrittUebernehmen() {

        // Still on page when order is out of range (order < 1)
        if (this.mySchritt.getReihenfolge() != null && this.mySchritt.getReihenfolge() < 1) {
            this.modusBearbeiten = "schritt";
            //this.createNewStepAllowParallelTask = false;
            Helper.setFehlerMeldung("Order may not be less than 1. (Is currently " + this.mySchritt.getReihenfolge() + ")");
            return "process_edit_step";
        }

        if (mySchritt.isTypAutomatisch()) {
            int numberOfActions = 0;
            if (mySchritt.isDelayStep()) {
                numberOfActions = numberOfActions + 1;
            }
            if (mySchritt.isHttpStep()) {
                numberOfActions = numberOfActions + 1;
            }
            if (mySchritt.isTypExportDMS()) {
                numberOfActions = numberOfActions + 1;
            }
            if (mySchritt.getTypScriptStep()) {
                numberOfActions = numberOfActions + 1;
            }
            if (StringUtils.isNotBlank(mySchritt.getStepPlugin()) && !mySchritt.isDelayStep() && !mySchritt.isTypExportDMS()) {
                numberOfActions = numberOfActions + 1;
            }
            if (numberOfActions > 1) {
                Helper.setFehlerMeldung("step_error_to_many_actions");
                modusBearbeiten = "schritt";
                return "process_edit_step";
            }
        }
        this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        //This is needed later when the page is left. (Next page may be different)
        boolean createNewStep = !myProzess.getSchritte().contains(mySchritt);

        // Create new step and add it to process
        if (createNewStep) {
            // When parallel tasks aren't allowed, all steps
            // with higher order have to increment their order
            // Otherwise when no other step exists with the same order,
            // this step can simply inserted without any shifts of orders
            if (!this.createNewStepAllowParallelTask && this.myProzess.containsStepOfOrder(this.mySchritt.getReihenfolge().intValue())) {
                this.incrementOrderOfHigherSteps();
            }

            // Add step to process
            this.myProzess.getSchritte().add(this.mySchritt);
            this.mySchritt.setProzess(this.myProzess);

        }

        Speichern();
        updateUsergroupPaginator();
        updateUserPaginator();
        reload();

        modusBearbeiten = "prozess";
        return "process_edit_step";
    }

    // Increment the order of all steps coming after this.mySchritt
    // this.mySchritt is explicitly excluded here, that means
    // when you insert this.mySchritt into this.myProzess before calling this method,
    // this.mySchritt will keep its order
    public void incrementOrderOfHigherSteps() {
        List<Step> steps = this.myProzess.getSchritte();
        Step step;
        int order;
        for (int i = 0; i < steps.size(); i++) {
            step = steps.get(i);
            order = step.getReihenfolge();
            if (order >= this.mySchritt.getReihenfolge() && step != this.mySchritt) {
                step.setReihenfolge(order + 1);
                this.saveStepInStepManager(step);
            }
        }
    }

    public String SchrittLoeschen() {
        this.myProzess.getSchritte().remove(this.mySchritt);
        StepManager.deleteStep(mySchritt);
        deleteSymlinksFromUserHomes();
        return "process_edit";
    }

    private void deleteSymlinksFromUserHomes() {
        Helper.addMessageToProcessLog(myProzess.getId(), LogType.DEBUG, "Removed links in home directories for all users in process details.");

        WebDav myDav = new WebDav();
        /* alle Benutzer */
        for (User b : this.mySchritt.getBenutzerList()) {
            try {
                myDav.UploadFromHome(b, this.mySchritt.getProzess());
            } catch (RuntimeException e) {
            }
        }
        /* alle Benutzergruppen mit ihren Benutzern */
        for (Usergroup bg : this.mySchritt.getBenutzergruppenList()) {
            for (User b : bg.getBenutzer()) {
                try {
                    myDav.UploadFromHome(b, this.mySchritt.getProzess());
                } catch (RuntimeException e) {
                }
            }
        }
    }

    public String BenutzergruppeLoeschen() {
        this.mySchritt.getBenutzergruppen().remove(this.myBenutzergruppe);
        StepManager.removeUsergroupFromStep(mySchritt, myBenutzergruppe);
        updateUsergroupPaginator();
        return "";
    }

    public String BenutzergruppeHinzufuegen() {
        if (!mySchritt.getBenutzergruppen().contains(myBenutzergruppe)) {
            this.mySchritt.getBenutzergruppen().add(this.myBenutzergruppe);

            try {
                StepManager.saveStep(mySchritt);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        updateUsergroupPaginator();
        return "";
    }

    public DatabasePaginator getUsergroupPaginator() {
        return usergroupPaginator;
    }

    private void updateUsergroupPaginator() {
        String filter =
                " benutzergruppen.BenutzergruppenID not in (select BenutzerGruppenID from schritteberechtigtegruppen where schritteberechtigtegruppen.schritteID = "
                        + mySchritt.getId() + ")";
        UsergroupManager m = new UsergroupManager();
        usergroupPaginator = new DatabasePaginator("titel", filter, m, "");

    }

    public DatabasePaginator getUserPaginator() {
        return userPaginator;
    }

    private void updateUserPaginator() {
        String filter =
                "benutzer.BenutzerID not in (select BenutzerID from schritteberechtigtebenutzer where schritteberechtigtebenutzer.schritteID = "
                        + mySchritt.getId() + ")";
        UserManager m = new UserManager();
        userPaginator = new DatabasePaginator("Nachname", filter, m, "");

    }

    public String BenutzerLoeschen() {
        this.mySchritt.getBenutzer().remove(this.myBenutzer);
        StepManager.removeUserFromStep(mySchritt, myBenutzer);
        updateUserPaginator();
        return "";
    }

    public String BenutzerHinzufuegen() {
        if (!mySchritt.getBenutzer().contains(myBenutzer)) {
            this.mySchritt.getBenutzer().add(this.myBenutzer);
            try {
                StepManager.saveStep(mySchritt);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        updateUserPaginator();
        return "";
    }

    /*
     * Vorlagen
     */

    public String VorlageNeu() {
        this.myVorlage = new Template();
        this.myProzess.getVorlagen().add(this.myVorlage);
        this.myVorlage.setProzess(this.myProzess);
        TemplateManager.saveTemplate(myVorlage);
        //        Speichern();
        return "process_edit_template";
    }

    public String VorlageUebernehmen() {
        TemplateManager.saveTemplate(myVorlage);
        //        Speichern();
        return "";
    }

    public String VorlageLoeschen() {
        this.myProzess.getVorlagen().remove(this.myVorlage);
        TemplateManager.deleteTemplate(myVorlage);

        return "process_edit";
    }

    /*
     * werkstücke
     */

    public String WerkstueckNeu() {
        this.myWerkstueck = new Masterpiece();
        this.myProzess.getWerkstuecke().add(this.myWerkstueck);
        this.myWerkstueck.setProzess(this.myProzess);
        MasterpieceManager.saveMasterpiece(myWerkstueck);
        return "process_edit_workpiece";
    }

    public String WerkstueckUebernehmen() {
        MasterpieceManager.saveMasterpiece(myWerkstueck);
        return "";
    }

    public String WerkstueckLoeschen() {
        this.myProzess.getWerkstuecke().remove(this.myWerkstueck);
        MasterpieceManager.deleteMasterpiece(myWerkstueck);
        return "process_edit";
    }

    /*
     * Aktionen
     */

    public void ExportMets() {
        ExportMets export = new ExportMets();
        try {
            export.startExport(this.myProzess);
            Helper.addMessageToProcessLog(this.myProzess.getId(), LogType.DEBUG, "Started METS export using 'ExportMets'.");
        } catch (Exception e) {
            String[] parameter = { "METS", this.myProzess.getTitel() };

            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);
            //            ;An error occured while trying to export METS file for: " + this.myProzess.getTitel(), e);
            logger.error("ExportMETS error", e);
        }
    }

    public void ExportPdf() {
        ExportPdf export = new ExportPdf();
        try {
            export.startExport(this.myProzess);
            Helper.addMessageToProcessLog(this.myProzess.getId(), LogType.DEBUG, "Started PDF export using 'ExportPdf'.");
        } catch (Exception e) {
            String[] parameter = { "PDF", this.myProzess.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);

            Helper.setFehlerMeldung("An error occured while trying to export PDF file for: " + this.myProzess.getTitel(), e);
            logger.error("ExportPDF error", e);
        }
    }

    public void ExportDMS() {
        IExportPlugin export = null;
        String pluginName = ProcessManager.getExportPluginName(myProzess.getId());
        if (StringUtils.isNotEmpty(pluginName)) {
            try {
                export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
            } catch (Exception e) {
                logger.error("Can't load export plugin, use default plugin", e);
                export = new ExportDms();
            }
        }
        if (export == null) {
            export = new ExportDms();
            Helper.addMessageToProcessLog(this.myProzess.getId(), LogType.DEBUG, "Started export using 'ExportDMS'.");
        }
        try {
            export.startExport(this.myProzess);
        } catch (Exception e) {
            String[] parameter = { "DMS", this.myProzess.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);
            //            Helper.setFehlerMeldung("An error occured while trying to export to DMS for: " + this.myProzess.getTitel(), e);
            logger.error("ExportDMS error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void ExportDMSPage() {

        Boolean flagError = false;
        for (Process proz : (List<Process>) this.paginator.getList()) {
            IExportPlugin export = null;
            String pluginName = ProcessManager.getExportPluginName(proz.getId());
            if (StringUtils.isNotEmpty(pluginName)) {
                try {
                    export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
                } catch (Exception e) {
                    logger.error("Can't load export plugin, use default plugin", e);
                    export = new ExportDms();
                }
            }
            if (export == null) {
                export = new ExportDms();
            }
            try {
                export.startExport(proz);
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Started export using 'ExportDMSPage'.");
            } catch (Exception e) {
                // without this a new exception is thrown, if an exception
                // caught here doesn't have an
                // errorMessage
                String errorMessage;

                if (e.getMessage() != null) {
                    errorMessage = e.getMessage();
                } else {
                    errorMessage = e.toString();
                }
                Helper.setFehlerMeldung("ExportErrorID" + proz.getId() + ":", errorMessage);
                logger.error(e);
                flagError = true;
            }
        }
        if (flagError) {
            Helper.setFehlerMeldung("ExportFinishedWithErrors");
        } else {
            Helper.setMeldung(null, "ExportFinished", "");
        }
    }

    @SuppressWarnings("unchecked")
    public void ExportDMSSelection() {

        for (Process proz : (List<Process>) this.paginator.getList()) {

            if (proz.isSelected()) {
                IExportPlugin export = null;
                String pluginName = ProcessManager.getExportPluginName(proz.getId());
                if (StringUtils.isNotEmpty(pluginName)) {
                    try {
                        export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
                    } catch (Exception e) {
                        logger.error("Can't load export plugin, use default plugin", e);
                        export = new ExportDms();
                    }
                }
                if (export == null) {
                    export = new ExportDms();
                }
                try {
                    export.startExport(proz);
                    Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Started export using 'ExportDMSSelection'.");
                } catch (Exception e) {
                    Helper.setFehlerMeldung("ExportError", e.getMessage());
                    logger.error(e);
                }
            }
        }
        Helper.setMeldung(null, "ExportFinished", "");
    }

    @SuppressWarnings("unchecked")
    public void ExportDMSHits() {

        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            IExportPlugin export = null;
            String pluginName = ProcessManager.getExportPluginName(proz.getId());
            if (StringUtils.isNotEmpty(pluginName)) {
                try {
                    export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
                } catch (Exception e) {
                    logger.error("Can't load export plugin, use default plugin", e);
                    export = new ExportDms();
                }
            }
            if (export == null) {
                export = new ExportDms();
            }

            try {
                export.startExport(proz);
                Helper.addMessageToProcessLog(proz.getId(), LogType.DEBUG, "Started export using 'ExportDMSHits'.");
            } catch (Exception e) {
                Helper.setFehlerMeldung("ExportError", e.getMessage());
                logger.error(e);
            }
        }
        Helper.setMeldung(null, "ExportFinished", "");
    }

    public String UploadFromHomeAlle() {
        WebDav myDav = new WebDav();
        List<String> folder = myDav.UploadFromHomeAlle(DONEDIRECTORYNAME);
        myDav.removeFromHomeAlle(folder, DONEDIRECTORYNAME);
        Helper.setMeldung(null, "directoryRemovedAll", DONEDIRECTORYNAME);
        return "";
    }

    public String UploadFromHome() {
        WebDav myDav = new WebDav();
        myDav.UploadFromHome(this.myProzess);
        Helper.setMeldung(null, "directoryRemoved", this.myProzess.getTitel());
        Helper.addMessageToProcessLog(this.myProzess.getId(), LogType.DEBUG, "Process uploaded from home directory via process list.");
        return "";
    }

    public void DownloadToHome() {
        doDownloadToHome(this.myProzess);
    }

    private void doDownloadToHome(Process p) {
        /*
         * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde, ansonsten
         * Download
         */
        if (!p.isImageFolderInUse()) {
            WebDav myDav = new WebDav();
            myDav.DownloadToHome(p, 0, false);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Process downloaded into home directory incl. writing access from process list.");
        } else {
            Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + p.getTitel() + " " + Helper.getTranslation("isInUse"),
                    p.getImageFolderInUseUser().getNachVorname());
            WebDav myDav = new WebDav();
            myDav.DownloadToHome(p, 0, true);
            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Process downloaded into home directory with reading access from process list.");
        }
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomePage() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            doDownloadToHome(proz);
        }
        Helper.setMeldung(null, "createdInUserHome", "");
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomeSelection() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                doDownloadToHome(proz);
            }
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomeHits() {
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            doDownloadToHome(proz);
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void generateFilterWithIdentfiers() {
        String f = "\"id:";
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            f += proz.getId() + " ";
        }
        f += "\"";
        filter = f;
    }

    public void SchrittStatusUp() {
        if (this.mySchritt.getBearbeitungsstatusEnum() != StepStatus.DONE && this.mySchritt.getBearbeitungsstatusEnum() != StepStatus.DEACTIVATED) {
            this.mySchritt.setBearbeitungsstatusUp();
            this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
            //            StepObject so = StepObjectManager.getStepById(this.mySchritt.getId());
            Helper.addMessageToProcessLog(mySchritt.getProcessId(), LogType.DEBUG, "Changed status for step '" + mySchritt.getTitel() + "' to "
                    + mySchritt.getBearbeitungsstatusAsString() + " in process details.");
            if (this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                new HelperSchritte().CloseStepObjectAutomatic(mySchritt);
            } else {
                mySchritt.setBearbeitungszeitpunkt(new Date());
                //                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                //                if (ben != null) {
                //                    mySchritt.setBearbeitungsbenutzer(ben);
                //                }
            }
        }
        try {
            StepManager.saveStep(mySchritt);
        } catch (DAOException e) {
            logger.error(e);
        }
        myProzess.setSchritte(null);
        deleteSymlinksFromUserHomes();
    }

    public String SchrittStatusDown() {
        this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        //        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        //        if (ben != null) {
        //            mySchritt.setBearbeitungsbenutzer(ben);
        //        }
        this.mySchritt.setBearbeitungsstatusDown();
        Helper.addMessageToProcessLog(mySchritt.getProcessId(), LogType.DEBUG,
                "Changed status for step '" + mySchritt.getTitel() + "' to " + mySchritt.getBearbeitungsstatusAsString() + " in process details.");
        try {
            StepManager.saveStep(mySchritt);
            new HelperSchritte().updateProcessStatus(myProzess.getId());
        } catch (DAOException e) {
            logger.error(e);
        }
        myProzess.setSchritte(null);
        deleteSymlinksFromUserHomes();
        return "";
    }

    /*
     * =======================================================
     * 
     * Auswahl mittels Selectboxen
     * 
     * ========================================================
     */

    @SuppressWarnings("unchecked")
    public void SelectionAll() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            proz.setSelected(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void SelectionNone() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            proz.setSelected(false);
        }
    }

    /*
     * Getter und Setter
     */

    public Process getMyProzess() {
        return this.myProzess;
    }

    public void setMyProzess(Process myProzess) {
        this.myProzess = myProzess;
        this.myNewProcessTitle = myProzess.getTitel();
        loadProcessProperties();
        loadDisplayableMetadata();
    }

    public Processproperty getMyProzessEigenschaft() {
        return this.myProzessEigenschaft;
    }

    public void setMyProzessEigenschaft(Processproperty myProzessEigenschaft) {
        this.myProzessEigenschaft = myProzessEigenschaft;
    }

    public Step getMySchritt() {
        return this.mySchritt;
    }

    public void setMySchritt(Step mySchritt) {
        this.mySchritt = mySchritt;
        updateUsergroupPaginator();
        updateUserPaginator();
    }

    public void setMySchrittReload(Step mySchritt) {
        this.mySchritt = mySchritt;
        updateUsergroupPaginator();
        updateUserPaginator();
    }

    //    public Schritteigenschaft getMySchrittEigenschaft() {
    //        return this.mySchrittEigenschaft;
    //    }
    //
    //    public void setMySchrittEigenschaft(Schritteigenschaft mySchrittEigenschaft) {
    //        this.mySchrittEigenschaft = mySchrittEigenschaft;
    //    }

    public Template getMyVorlage() {
        return this.myVorlage;
    }

    public void setMyVorlage(Template myVorlage) {
        this.myVorlage = myVorlage;
    }

    public void setMyVorlageReload(Template myVorlage) {
        this.myVorlage = myVorlage;
    }

    public Templateproperty getMyVorlageEigenschaft() {
        return this.myVorlageEigenschaft;
    }

    public void setMyVorlageEigenschaft(Templateproperty myVorlageEigenschaft) {
        this.myVorlageEigenschaft = myVorlageEigenschaft;
    }

    public Masterpiece getMyWerkstueck() {
        return this.myWerkstueck;
    }

    public void setMyWerkstueck(Masterpiece myWerkstueck) {
        this.myWerkstueck = myWerkstueck;
    }

    public void setMyWerkstueckReload(Masterpiece myWerkstueck) {
        this.myWerkstueck = myWerkstueck;
    }

    public Masterpieceproperty getMyWerkstueckEigenschaft() {
        return this.myWerkstueckEigenschaft;
    }

    public void setMyWerkstueckEigenschaft(Masterpieceproperty myWerkstueckEigenschaft) {
        this.myWerkstueckEigenschaft = myWerkstueckEigenschaft;
    }

    public String getModusAnzeige() {
        return this.modusAnzeige;
    }

    public void setModusAnzeige(String modusAnzeige) {
        this.modusAnzeige = modusAnzeige;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }

    public String decrementOrder() {
        int oldOrder = Integer.valueOf(this.mySchritt.getReihenfolge().intValue());

        if (oldOrder > 1) {
            this.mySchritt.setReihenfolge(oldOrder - 1);
            this.saveStepInStepManager(this.mySchritt);
        }
        return this.reload();
    }

    public String incrementOrder() {
        int oldOrder = Integer.valueOf(this.mySchritt.getReihenfolge().intValue());
        this.mySchritt.setReihenfolge(oldOrder + 1);

        this.saveStepInStepManager(this.mySchritt);
        return this.reload();
    }

    public String exchangeTaskOrderDownwards() {
        return this.exchangeTaskOrder(-1);
    }

    public String exchangeTaskOrderUpwards() {
        return this.exchangeTaskOrder(1);
    }

    //direction:
    //+1 = up   (priority 1 -> 2 -> 3)
    //-1 = down (priority 3 -> 2 -> 1)
    public String exchangeTaskOrder(final int direction) {

        List<Step> steps = this.myProzess.getSchritte();
        int baseOrder = this.mySchritt.getReihenfolge().intValue();
        int targetOrder = this.getNextAvailableOrder(baseOrder, direction);//-1 means downwards, +1 means upwards

        if (targetOrder != baseOrder) {// Otherwise there is no next order, then nothing happens
            int currentOrder;

            // Set all steps with targetOrder to baseOrder
            for (int i = 0; i < steps.size(); i++) {
                currentOrder = steps.get(i).getReihenfolge().intValue();

                if (currentOrder == targetOrder) {
                    steps.get(i).setReihenfolge(baseOrder);
                    this.saveStepInStepManager(steps.get(i));
                }
            }
            // Set the step (with baseOrder) to targetOrder
            this.mySchritt.setReihenfolge(targetOrder);
            this.saveStepInStepManager(this.mySchritt);
        }
        return this.reload();
    }

    //direction:
    //+1 = up   (priority 1 -> 2 -> 3)
    //-1 = down (priority 3 -> 2 -> 1)
    private int getNextAvailableOrder(final int baseOrder, final int direction) {

        List<Step> steps = this.myProzess.getSchritte();
        int targetOrder = -1;
        int currentOrder;

        for (int i = 0; i < steps.size(); i++) {
            currentOrder = steps.get(i).getReihenfolge().intValue();
            // Is baseOrder < currentOrder < targetOrder or targetOrder undefined (-1)?
            if (direction == -1) {//downwards

                if (currentOrder < baseOrder) {
                    if (targetOrder == -1 || (targetOrder != -1 && currentOrder > targetOrder)) {
                        targetOrder = currentOrder;
                    }
                }
                // Is targetOrder < currentOrder < baseOrder or targetOrder undefined (-1)?
            } else if (direction == 1) {//upwards

                if (currentOrder > baseOrder) {
                    if (targetOrder == -1 || (targetOrder != 1 && currentOrder < targetOrder)) {
                        targetOrder = currentOrder;
                    }
                }
            }
        }
        // When there is no next order, the given order will be returned
        return (targetOrder > 0 ? targetOrder : baseOrder);
    }

    private void saveStepInStepManager(Step step) {
        try {
            StepManager.saveStep(step);
            String message = "Changed step order for step '" + step.getTitel() + "' to position " + step.getReihenfolge()
            + " in process details.";
            Helper.addMessageToProcessLog(step.getProcessId(), LogType.DEBUG, message);
            // set list to null to reload list of steps in new order
            this.myProzess.setSchritte(null);
        } catch (DAOException e) {
            logger.error(e);
        }
    }

    public String reload() {
        if (this.myProzess != null && this.myProzess.getId() != null) {
            this.myProzess = ProcessManager.getProcessById(this.myProzess.getId());
        }
        return "";
    }

    public User getMyBenutzer() {
        return this.myBenutzer;
    }

    public void setMyBenutzer(User myBenutzer) {
        this.myBenutzer = myBenutzer;
    }

    public Usergroup getMyBenutzergruppe() {
        return this.myBenutzergruppe;
    }

    public void setMyBenutzergruppe(Usergroup myBenutzergruppe) {
        this.myBenutzergruppe = myBenutzergruppe;
    }

    /*
     * Zuweisung der Projekte
     */

    public Integer getProjektAuswahl() {
        if (this.myProzess.getProjekt() != null) {
            return this.myProzess.getProjekt().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setProjektAuswahl(Integer inProjektAuswahl) {
        if (inProjektAuswahl != null && inProjektAuswahl.intValue() != 0) {
            try {
                Project p = ProjectManager.getProjectById(inProjektAuswahl);
                this.myProzess.setProjekt(p);
                this.myProzess.setProjectId(inProjektAuswahl);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Projekt kann nicht zugewiesen werden", "");
                logger.error(e);
            }
        }
    }

    public List<SelectItem> getProjektAuswahlListe() throws DAOException {
        List<SelectItem> myProjekte = new ArrayList<>();
        List<Project> temp = null;
        LoginBean login = Helper.getLoginBean();
        if (login != null && !login.hasRole(UserRole.Workflow_General_Show_All_Projects.name())) {
            temp = ProjectManager.getProjectsForUser(login.getMyBenutzer(), false);
        } else {
            temp = ProjectManager.getAllProjects();

        }

        for (Project proj : temp) {
            myProjekte.add(new SelectItem(proj.getId(), proj.getTitel(), null));
        }
        return myProjekte;
    }

    public Integer getRulesetSelection() {
        if (this.myProzess.getRegelsatz() != null) {
            return this.myProzess.getRegelsatz().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setRulesetSelection(Integer selected) {
        if (selected != null && selected.intValue() != 0) {
            try {
                Ruleset ruleset = RulesetManager.getRulesetById(selected);
                myProzess.setRegelsatz(ruleset);
                myProzess.setMetadatenKonfigurationID(selected);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Projekt kann nicht zugewiesen werden", "");
                logger.error(e);
            }
        }
    }

    public List<SelectItem> getRulesetSelectionList() {
        List<SelectItem> rulesets = new ArrayList<>();
        List<Ruleset> temp = RulesetManager.getAllRulesets();
        for (Ruleset ruleset : temp) {
            rulesets.add(new SelectItem(ruleset.getId(), ruleset.getTitel(), null));
        }
        return rulesets;
    }

    public Integer getDocketSelection() {
        if (this.myProzess.getDocket() != null) {
            return this.myProzess.getDocket().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setDocketSelection(Integer selected) {
        if (selected != null && selected.intValue() != 0) {
            try {
                Docket ruleset = DocketManager.getDocketById(selected);
                myProzess.setDocket(ruleset);
                myProzess.setDocketId(selected);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Docket kann nicht zugewiesen werden", "");
                logger.error(e);
            }
        }
    }

    public List<SelectItem> getDocketSelectionList() {
        List<SelectItem> myProjekte = new ArrayList<>();
        List<Docket> temp = DocketManager.getAllDockets();
        for (Docket docket : temp) {
            myProjekte.add(new SelectItem(docket.getId(), docket.getName(), null));
        }
        return myProjekte;
    }

    /*
     * Anzahlen der Artikel und Images
     */

    @SuppressWarnings("unchecked")
    public void CalcMetadataAndImagesPage() throws IOException, InterruptedException, SwapException, DAOException {
        CalcMetadataAndImages((List<Process>) this.paginator.getList());
    }

    @SuppressWarnings("unchecked")
    public void CalcMetadataAndImagesSelection() throws IOException, InterruptedException, SwapException, DAOException {
        ArrayList<Process> auswahl = new ArrayList<>();
        for (Process p : (List<Process>) this.paginator.getList()) {
            if (p.isSelected()) {
                auswahl.add(p);
            }
        }
        CalcMetadataAndImages(auswahl);
    }

    @SuppressWarnings("unchecked")
    public void CalcMetadataAndImagesHits() throws IOException, InterruptedException, SwapException, DAOException {
        CalcMetadataAndImages((List<Process>) this.paginator.getCompleteList());
    }

    private void CalcMetadataAndImages(List<Process> inListe) throws IOException, InterruptedException, SwapException, DAOException {
        //      XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
        this.myAnzahlList = new ArrayList<>();
        int allMetadata = 0;
        int allDocstructs = 0;
        int allImages = 0;

        int maxImages = 1;
        int maxDocstructs = 1;
        int maxMetadata = 1;

        int countOfProcessesWithImages = 0;
        int countOfProcessesWithMetadata = 0;
        int countOfProcessesWithDocstructs = 0;

        for (Process proz : inListe) {
            int tempImg = proz.getSortHelperImages();

            if (tempImg == 0) {
                tempImg = HistoryManager.getNumberOfImages(proz.getId());
            }
            int tempMetadata = proz.getSortHelperMetadata();
            int tempDocstructs = proz.getSortHelperDocstructs();

            ProcessCounterObject pco = new ProcessCounterObject(proz.getTitel(), tempMetadata, tempDocstructs, tempImg);
            this.myAnzahlList.add(pco);

            if (tempImg > maxImages) {
                maxImages = tempImg;
            }
            if (tempMetadata > maxMetadata) {
                maxMetadata = tempMetadata;
            }
            if (tempDocstructs > maxDocstructs) {
                maxDocstructs = tempDocstructs;
            }
            if (tempImg > 0) {
                countOfProcessesWithImages++;
            }
            if (tempMetadata > 0) {
                countOfProcessesWithMetadata++;
            }
            if (tempDocstructs > 0) {
                countOfProcessesWithDocstructs++;
            }

            /* Werte für die Gesamt- und Durchschnittsberechnung festhalten */
            allImages += tempImg;
            allMetadata += tempMetadata;
            allDocstructs += tempDocstructs;
        }

        if (countOfProcessesWithImages == 0) {
            countOfProcessesWithImages = 1;
        }
        if (countOfProcessesWithMetadata == 0) {
            countOfProcessesWithMetadata = 1;
        }
        if (countOfProcessesWithDocstructs == 0) {
            countOfProcessesWithDocstructs = 1;
        }
        /* die prozentualen Werte anhand der Maximumwerte ergänzen */
        for (ProcessCounterObject pco : this.myAnzahlList) {
            pco.setRelImages(pco.getImages() * 100 / maxImages);
            pco.setRelMetadata(pco.getMetadata() * 100 / maxMetadata);
            pco.setRelDocstructs(pco.getDocstructs() * 100 / maxDocstructs);
        }

        /* die Durchschnittsberechnung durchführen */
        //      int faktor = 1;
        //      if (this.myAnzahlList != null && this.myAnzahlList.size() > 0) {
        //          faktor = this.myAnzahlList.size();
        //      }
        this.myAnzahlSummary = new HashMap<>();
        this.myAnzahlSummary.put("sumProcesses", this.myAnzahlList.size());
        this.myAnzahlSummary.put("sumMetadata", allMetadata);
        this.myAnzahlSummary.put("sumDocstructs", allDocstructs);
        this.myAnzahlSummary.put("sumImages", allImages);
        this.myAnzahlSummary.put("averageImages", allImages / countOfProcessesWithImages);
        this.myAnzahlSummary.put("averageMetadata", allMetadata / countOfProcessesWithMetadata);
        this.myAnzahlSummary.put("averageDocstructs", allDocstructs / countOfProcessesWithDocstructs);
    }

    public HashMap<String, Integer> getMyAnzahlSummary() {
        return this.myAnzahlSummary;
    }

    private void renderHitNumberImage() {
        String renderString = this.goobiScriptHitsCount + " " + Helper.getTranslation("hits");
        BufferedImage im = new BufferedImage(500, 80, BufferedImage.TYPE_BYTE_INDEXED);
        Graphics2D g2d = im.createGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, 1000, 80);
        g2d.setColor(Color.RED);
        Font font = new Font("SansSerif", Font.PLAIN, 60);
        g2d.setFont(font);
        g2d.drawString(renderString, 0, 65);
        int width = g2d.getFontMetrics().stringWidth(renderString);
        g2d.dispose();
        im = im.getSubimage(0, 0, width, im.getHeight());
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(im, "png", baos);
            this.goobiScriptHitsImage = "data:image/png;base64, " + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error(e);
        }
    }

    /**
     * prepare the variables for user question with all hits
     */
    public void prepareGoobiScriptHits() {
        this.goobiScriptHitsCount = this.paginator.getIdList().size();
        this.goobiScriptMode = "hits";
        this.renderHitNumberImage();
    }

    /**
     * prepare the variables for user question with hits on the current page
     */
    public void prepareGoobiScriptPage() {
        this.goobiScriptHitsCount = paginator.getList().size();
        this.goobiScriptMode = "page";
        this.renderHitNumberImage();
    }

    /**
     * prepare the variables for user question with selected items
     */
    public void prepareGoobiScriptSelection() {
        this.goobiScriptHitsCount = (int) paginator.getList().stream().filter(p -> ((Process) p).isSelected()).count();
        this.goobiScriptMode = "selection";
        this.renderHitNumberImage();
    }

    private boolean checkSecurityResult() {
        if (this.goobiScriptHitsCountUser == null) {
            return false;
        }
        return this.goobiScriptHitsCount == this.goobiScriptHitsCountUser;
    }

    private void resetHitsCount() {
        this.goobiScriptHitsCountUser = null;
    }

    /**
     * runs the current GoobiScript in the correct mode ("page", "hits" or "selection")
     * 
     * @return
     */
    public String runGoobiScript() {
        switch (this.goobiScriptMode) {
            case "hits":
                return GoobiScriptHits();
            case "page":
                return GoobiScriptPage();
            default:
                return GoobiScriptSelection();
        }
    }

    /**
     * Return a list of all visible GoobiScript commands with their action name and the sample call
     * 
     * @return the list of GoobiScripts
     */
    public List<StringPair> getAllGoobiScripts() {
        if (allGoobiScripts == null) {
            allGoobiScripts = new ArrayList<>();

            Set<Class<? extends IGoobiScript>> myset = new Reflections("org.goobi.goobiScript.*").getSubTypesOf(IGoobiScript.class);
            for (Class<? extends IGoobiScript> cl : myset) {
                try {
                    @SuppressWarnings("deprecation")
                    IGoobiScript gs = cl.newInstance();
                    if (gs.isVisible()) {
                        allGoobiScripts.add(new StringPair(gs.getAction(), gs.getSampleCall()));
                    }
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e) {
                }
            }
            Collections.sort(allGoobiScripts, new StringPair.OneComparator());
        }
        return allGoobiScripts;
    }

    /**
     * Starte GoobiScript über alle Treffer
     */
    public String GoobiScriptHits() {
        if (!checkSecurityResult()) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "GoobiScript_wrong_answer");
            return "";
        } else {
            resetHitsCount();
            GoobiScript gs = new GoobiScript();
            return gs.execute(this.paginator.getIdList(), this.goobiScript);

        }
    }

    /**
     * Starte GoobiScript über alle Treffer der Seite
     */
    @SuppressWarnings("unchecked")
    public String GoobiScriptPage() {
        if (!checkSecurityResult()) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "GoobiScript_wrong_answer");
            return "";
        } else {
            resetHitsCount();
            GoobiScript gs = new GoobiScript();
            List<Integer> idList = new ArrayList<>();
            for (Process p : (List<Process>) paginator.getList()) {
                idList.add(p.getId());
            }
            return gs.execute(idList, this.goobiScript);
        }
    }

    /**
     * Starte GoobiScript über alle selectierten Treffer
     */
    @SuppressWarnings("unchecked")
    public String GoobiScriptSelection() {
        if (!checkSecurityResult()) {
            Helper.setFehlerMeldung("goobiScriptfield", "", "GoobiScript_wrong_answer");
            return "";
        } else {
            resetHitsCount();
            List<Integer> idList = new ArrayList<>();
            for (Process p : (List<Process>) this.paginator.getList()) {
                if (p.isSelected()) {
                    idList.add(p.getId());
                }
            }
            GoobiScript gs = new GoobiScript();
            return gs.execute(idList, this.goobiScript);
        }
    }

    @SuppressWarnings("unchecked")
    public int getGoobiScriptCountSelection() {
        List<Integer> idList = new ArrayList<>();
        for (Process p : (List<Process>) this.paginator.getList()) {
            if (p.isSelected()) {
                idList.add(p.getId());
            }
        }
        return idList.size();
    }

    /*
     * Statistische Auswertung
     */

    public void StatisticsStatusVolumes() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.STATUS_VOLUMES, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
        this.statisticsManager.calculate();
    }

    public void StatisticsUsergroups() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.USERGROUPS, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
        this.statisticsManager.calculate();
    }

    public void StatisticsRuntimeSteps() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
    }

    public void StatisticsProduction() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.PRODUCTION, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
    }

    public void StatisticsStorage() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.STORAGE, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
    }

    public void StatisticsCorrection() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.CORRECTIONS, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
    }

    public void StatisticsTroughput() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.THROUGHPUT, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
    }

    public void StatisticsProject() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.PROJECTS, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter);
        this.statisticsManager.calculate();
    }

    /**
     * ist called via jsp at the end of building a chart in include file Prozesse_Liste_Statistik.jsp and resets the statistics so that with the next
     * reload a chart is not shown anymore
     * 
     * @author Wulf
     */
    public String getResetStatistic() {
        this.showStatistics = false;
        return "";
    }

    public String getMyDatasetHoehe() {
        int bla = this.paginator.getTotalResults() * 20;
        return String.valueOf(bla);
    }

    public int getMyDatasetHoeheInt() {
        int bla = this.paginator.getTotalResults() * 20;
        return bla;
    }

    public NumberFormat getMyFormatter() {
        return new DecimalFormat("#,##0");
    }

    public PlotOrientation getMyOrientation() {
        return PlotOrientation.HORIZONTAL;
    }

    /*
     * Downloads
     */

    public void DownloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.myProzess);
        tiff.ExportStart();
    }

    public String getGoobiScript() {
        return this.goobiScript;
    }

    public void setGoobiScript(String goobiScript) {
        this.goobiScript = goobiScript;
    }

    public HashMap<String, Boolean> getAnzeigeAnpassen() {
        return this.anzeigeAnpassen;
    }

    public void setAnzeigeAnpassen(HashMap<String, Boolean> anzeigeAnpassen) {
        this.anzeigeAnpassen = anzeigeAnpassen;
    }

    public String getMyNewProcessTitle() {
        return this.myNewProcessTitle;
    }

    public void setMyNewProcessTitle(String myNewProcessTitle) {
        this.myNewProcessTitle = myNewProcessTitle;
    }

    public StatisticsManager getStatisticsManager() {
        return this.statisticsManager;
    }

    /*************************************************************************************
     * Getter for showStatistics loadProcessProperties();
     * 
     * @return the showStatistics
     *************************************************************************************/
    public boolean isShowStatistics() {
        return this.showStatistics;
    }

    /**************************************************************************************
     * Setter for showStatistics
     * 
     * @param showStatistics the showStatistics to set
     **************************************************************************************/
    public void setShowStatistics(boolean showStatistics) {
        this.showStatistics = showStatistics;
    }

    public static class ProcessCounterObject {
        private String title;
        private int metadata;
        private int docstructs;
        private int images;
        private int relImages;
        private int relDocstructs;
        private int relMetadata;

        public ProcessCounterObject(String title, int metadata, int docstructs, int images) {
            super();
            this.title = title;
            this.metadata = metadata;
            this.docstructs = docstructs;
            this.images = images;
        }

        public int getImages() {
            return this.images;
        }

        public int getMetadata() {
            return this.metadata;
        }

        public String getTitle() {
            return this.title;
        }

        public int getDocstructs() {
            return this.docstructs;
        }

        public int getRelDocstructs() {
            return this.relDocstructs;
        }

        public int getRelImages() {
            return this.relImages;
        }

        public int getRelMetadata() {
            return this.relMetadata;
        }

        public void setRelDocstructs(int relDocstructs) {
            this.relDocstructs = relDocstructs;
        }

        public void setRelImages(int relImages) {
            this.relImages = relImages;
        }

        public void setRelMetadata(int relMetadata) {
            this.relMetadata = relMetadata;
        }
    }

    /**
     * starts generation of xml logfile for current process
     */
    public void generateSimplifiedMetadataFile() {
        this.myProzess.downloadSimplifiedMetadataAsPDF();
    }

    public String getMyProcessId() {
        return String.valueOf(this.myProzess.getId());
    }

    public void setMyProcessId(String id) {
        try {
            int myid = Integer.valueOf(id).intValue();
            this.myProzess = ProcessManager.getProcessById(myid);
            //        } catch (DAOException e) {
            //            logger.error(e);
        } catch (NumberFormatException e) {
            logger.warn(e);
        }
    }

    public List<String> getXsltList() {
        List<String> answer = new ArrayList<>();
        Path folder = Paths.get("xsltFolder");
        if (StorageProvider.getInstance().isFileExists(folder) && StorageProvider.getInstance().isDirectory(folder)) {
            List<String> files = StorageProvider.getInstance().list(folder.toString());

            for (String file : files) {
                if (file.endsWith(".xslt") || file.endsWith(".xsl")) {
                    answer.add(file);
                }
            }
        }
        return answer;
    }

    public void setSelectedXslt(String select) {
        this.selectedXslt = select;
    }

    public String getSelectedXslt() {
        return this.selectedXslt;
    }

    public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
        this.myCurrentTable = myCurrentTable;
    }

    public StatisticsRenderingElement getMyCurrentTable() {
        return this.myCurrentTable;
    }

    public void downloadStatisticsAsExcel() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
                ServletOutputStream out = response.getOutputStream();
                XSSFWorkbook wb = (XSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public void downloadStatisticsAsCsv() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        CSVPrinter csvFilePrinter = null;
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("export.csv");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"export.csv\"");

                CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");
                csvFilePrinter = new CSVPrinter(response.getWriter(), csvFileFormat);
                CSVRenderer csvr = this.myCurrentTable.getCsvRenderer();

                // add all headers
                List<Object> csvHead = new ArrayList<>();
                csvHead.add(csvr.getDataTable().getUnitLabel());
                for (String s : csvr.getDataTable().getDataRows().get(0).getLabels()) {
                    csvHead.add(s);
                }
                csvFilePrinter.printRecord(csvHead);

                // add all rows
                for (DataRow dr : csvr.getDataTable().getDataRows()) {
                    List<Object> csvColumns = new ArrayList<>();
                    csvColumns.add(dr.getName());
                    for (int j = 0; j < dr.getNumberValues(); j++) {
                        csvColumns.add(dr.getValue(j));
                    }
                    csvFilePrinter.printRecord(csvColumns);
                }

                facesContext.responseComplete();
            } catch (Exception e) {

            } finally {
                try {
                    csvFilePrinter.close();
                } catch (IOException e) {

                }
            }
        }
    }

    private List<SearchColumn> prepareSearchColumnData() {
        List<SearchColumn> columnList = new ArrayList<>();
        boolean addAllColumns = false;
        for (SearchColumn sc : searchField) {
            if (sc.getValue().equals("all")) {
                addAllColumns = true;
                break;
            }
        }
        if (addAllColumns) {
            int currentOrder = 0;
            for (SelectItem si : possibleItems) {
                if (!si.getValue().equals("all") && !si.isDisabled() && !((String) si.getValue()).startsWith("index.")) {
                    SearchColumn sc = new SearchColumn(currentOrder++);
                    sc.setValue((String) si.getValue());
                    columnList.add(sc);
                }
            }
        } else {
            columnList = searchField;
        }

        return columnList;
    }

    public void generateResultAsPdf() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.pdf");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.pdf\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XSSFWorkbook wb =
                        sch.getResult(prepareSearchColumnData(), this.filter, sortList(), this.showClosedProcesses, this.showArchivedProjects);

                List<List<XSSFCell>> rowList = new ArrayList<>();
                XSSFSheet mySheet = wb.getSheetAt(0);
                Iterator<Row> rowIter = mySheet.rowIterator();
                while (rowIter.hasNext()) {
                    XSSFRow myRow = (XSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    List<XSSFCell> row = new ArrayList<>();
                    while (cellIter.hasNext()) {
                        XSSFCell myCell = (XSSFCell) cellIter.next();
                        row.add(myCell);
                    }
                    rowList.add(row);
                }
                Document document = new Document();
                Rectangle a4quer = new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth());
                PdfWriter.getInstance(document, out);
                document.setPageSize(a4quer);
                document.open();
                if (rowList.size() > 0) {
                    //                    Paragraph p = new Paragraph(rowList.get(0).get(0).toString());
                    //                    document.add(p);
                    PdfPTable table = new PdfPTable(rowList.get(0).size());
                    table.setSpacingBefore(20);

                    for (int i = 0; i < rowList.size(); i++) {

                        List<XSSFCell> row = rowList.get(i);
                        table.completeRow();
                        for (int j = 0; j < row.size(); j++) {
                            XSSFCell myCell = row.get(j);
                            String stringCellValue = myCell.toString();
                            table.addCell(stringCellValue);
                        }

                    }
                    document.add(table);
                }

                document.close();
                out.flush();
                facesContext.responseComplete();

            } catch (Exception e) {
            }
        }
    }

    public void generateResultXls() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.xlsx");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.xlsx\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XSSFWorkbook wb =
                        sch.getResult(prepareSearchColumnData(), this.filter, sortList(), this.showClosedProcesses, this.showArchivedProjects);

                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public void generateResultDoc() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.doc");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.doc\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XWPFDocument wb =
                        sch.getResultAsWord(prepareSearchColumnData(), this.filter, sortList(), this.showClosedProcesses, this.showArchivedProjects);
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public void generateResultRtf() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.rtf");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.rtf\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                sch.getResultAsRtf(prepareSearchColumnData(), this.filter, sortList(), this.showClosedProcesses, this.showArchivedProjects, out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public boolean isShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
    }

    public void setShowArchivedProjects(boolean showArchivedProjects) {
        this.showArchivedProjects = showArchivedProjects;
    }

    public boolean isShowArchivedProjects() {
        return this.showArchivedProjects;
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

    public int getSizeOfDisplayableMetadata() {
        return displayableMetadataMap.size();
    }

    private void loadDisplayableMetadata() {

        displayableMetadataMap = new LinkedHashMap<>();
        List<String> possibleMetadataNames = PropertyParser.getInstance().getDisplayableMetadataForProcess(myProzess);
        if (possibleMetadataNames.isEmpty()) {
            return;
        }

        for (String metadataName : possibleMetadataNames) {

            List<String> values = MetadataManager.getAllMetadataValues(myProzess.getId(), metadataName);
            if (!values.isEmpty()) {
                displayableMetadataMap.put(metadataName, values);
            }
        }
        //            if (StringUtils.isNotBlank(value)) {
        //                displayableMetadataMap.put(metadataName, value);
        //            }
    }

    private void loadProcessProperties() {
        try {
            this.myProzess = ProcessManager.getProcessById(this.myProzess.getId());
        } catch (Exception e) {
            logger.warn("could not refresh process with id " + this.myProzess.getId(), e);
        }
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getInstance().getPropertiesForProcess(this.myProzess);

        for (ProcessProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                Processproperty pe = new Processproperty();
                pe.setProzess(myProzess);
                pt.setProzesseigenschaft(pe);
                myProzess.getEigenschaften().add(pe);
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

    // TODO validierung nur bei Schritt abgeben, nicht bei normalen speichern
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
                    pe.setProzess(this.myProzess);
                    p.setProzesseigenschaft(pe);
                    this.myProzess.getEigenschaften().add(pe);
                }
                p.transfer();
                if (!this.myProzess.getEigenschaften().contains(p.getProzesseigenschaft())) {
                    this.myProzess.getEigenschaften().add(p.getProzesseigenschaft());
                }
            }

            List<Processproperty> props = this.myProzess.getEigenschaftenList();
            for (Processproperty pe : props) {
                if (pe.getTitel() == null) {
                    this.myProzess.getEigenschaften().remove(pe);
                }
            }

            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
            Helper.setMeldung("Properties saved");
        }
    }

    public void saveCurrentProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                String value = Helper.getTranslation("propertyNotValid", processProperty.getName());
                Helper.setFehlerMeldung(value);
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                Processproperty pe = new Processproperty();
                pe.setProzess(this.myProzess);
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            if (!this.processProperty.getProzesseigenschaft()
                    .getProzess()
                    .getEigenschaften()
                    .contains(this.processProperty.getProzesseigenschaft())) {
                this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().add(this.processProperty.getProzesseigenschaft());
            }

            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
            //                ProcessManager.saveProcess(this.myProzess);
            Helper.setMeldung("propertiesSaved");

        }
        loadProcessProperties();
    }

    public int getPropertyListSize() {
        if (this.processPropertyList == null) {
            return 0;
        }
        return this.processPropertyList.size();
    }

    public Map<Integer, PropertyListObject> getContainers() {
        return this.containers;
    }

    public List<Integer> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
    }

    public int getContainersSize() {
        if (this.containers == null) {
            return 0;
        }
        return this.containers.size();
    }

    public List<ProcessProperty> getSortedProperties() {
        Comparator<ProcessProperty> comp = new ProcessProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    public void deleteProperty() {
        List<ProcessProperty> ppList = getContainerProperties();
        for (ProcessProperty pp : ppList) {
            this.processPropertyList.remove(pp);
            this.myProzess.getEigenschaften().remove(pp.getProzesseigenschaft());
            PropertyManager.deleteProcessProperty(pp.getProzesseigenschaft());
        }
        loadProcessProperties();
    }

    public void duplicateProperty() {
        ProcessProperty pt = this.processProperty.getClone(0);
        this.processPropertyList.add(pt);
        saveProcessProperties();
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
        List<ProcessProperty> answer = new ArrayList<>();

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
        List<ProcessProperty> plist = new ArrayList<>();
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
                pe.setProzess(this.myProzess);
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();
            PropertyManager.saveProcessProperty(processProperty.getProzesseigenschaft());
        }
        //        try {
        //            ProcessManager.saveProcess(this.myProzess);
        Helper.setMeldung("propertySaved");
        //        } catch (DAOException e) {
        //            logger.error(e);
        //            Helper.setFehlerMeldung("propertiesNotSaved");
        //        }
        loadProcessProperties();

        return "";
    }

    public List<ProcessProperty> getContainerlessProperties() {
        List<ProcessProperty> answer = new ArrayList<>();
        for (ProcessProperty pp : this.processPropertyList) {
            if (pp.getContainer() == 0) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public void createNewProperty() {
        if (this.processPropertyList == null) {
            this.processPropertyList = new ArrayList<>();
        }
        ProcessProperty pp = new ProcessProperty();
        pp.setType(Type.TEXT);
        pp.setContainer(0);
        this.processProperty = pp;
    }

    public String getUserDisplayMode() {
        return userDisplayMode;
    }

    public void setUserDisplayMode(String userDisplayMode) {
        this.userDisplayMode = userDisplayMode;
    }

    public List<SearchColumn> getSearchField() {
        return searchField;
    }

    public void setSearchField(List<SearchColumn> searchField) {
        this.searchField = searchField;
    }

    public int getSizeOfFieldList() {
        return searchField.size();
    }

    public SearchColumn getCurrentField() {
        return currentField;
    }

    public void setCurrentField(SearchColumn currentField) {
        this.currentField = currentField;
    }

    public void addField() {
        searchField.add(new SearchColumn(order++));
    }

    public void deleteField() {
        searchField.remove(currentField);
    }

    public List<SelectItem> getPossibleItems() {
        if (possibleItems == null) {
            possibleItems = new SearchResultHelper().getPossibleColumns();
        }
        return possibleItems;
    }

    public void setPossibleItems(List<SelectItem> possibleItems) {
        this.possibleItems = possibleItems;
    }

    public boolean isDispaySearchResult() {
        return dispaySearchResult;
    }

    public void setDispaySearchResult(boolean dispaySearchResult) {
        this.dispaySearchResult = dispaySearchResult;
    }

    public void setConfirmLink(boolean confirm) {

    }

    public boolean getConfirmLink() {
        return ConfigurationHelper.getInstance().isConfirmLinking();
    }

    public boolean getAllowFolderLinkingForProcessList() {
        return ConfigurationHelper.getInstance().isAllowFolderLinkingForProcessList();
    }

    public List<String> getPossibleStepPlugins() {
        return stepPluginList;
    }

    public List<String> getPossibleValidationPlugins() {
        return validationPluginList;
    }

    public List<QueueType> getPossibleMessageQueues() {
        return QueueType.getSelectable();
    }

    public String cloneProcess() {
        myProzess.clone();
        return FilterVorlagen();
    }

    public String startPlugin() {
        if (StringUtils.isNotBlank(mySchritt.getStepPlugin())) {
            if (mySchritt.isTypExportDMS()) {
                IExportPlugin dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, mySchritt.getStepPlugin());
                try {
                    dms.startExport(mySchritt.getProzess());
                } catch (DocStructHasNoTypeException | PreferencesException | WriteException | MetadataTypeNotAllowedException | ReadException
                        | TypeNotAllowedForParentException | IOException | InterruptedException | ExportFileException | UghHelperException
                        | SwapException | DAOException e) {
                    logger.error(e);
                }
            } else if (mySchritt.isDelayStep()) {
                Helper.setFehlerMeldung("cannotStartPlugin");
            } else {
                currentPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
                if (currentPlugin != null) {
                    bean.setMyPlugin(currentPlugin);
                    currentPlugin.initialize(mySchritt, "/process_edit");
                    if (currentPlugin.getPluginGuiType() == PluginGuiType.FULL || currentPlugin.getPluginGuiType() == PluginGuiType.PART_AND_FULL) {

                        String mypath = currentPlugin.getPagePath();
                        currentPlugin.execute();
                        return mypath;
                    } else if (currentPlugin.getPluginGuiType() == PluginGuiType.PART) {
                        //                        FacesContext context = FacesContextHelper.getCurrentFacesContext();
                        //                        Map<String, Object> requestMap = context.getExternalContext().getSessionMap();
                        //                        StepBean bean = (StepBean) requestMap.get("AktuelleSchritteForm");
                        //                        if (bean == null) {
                        //                            bean = new StepBean();
                        //                            requestMap.put("AktuelleSchritteForm", bean);
                        //                        }

                        String mypath = "/uii/task_edit_simulator";
                        currentPlugin.execute();
                        return mypath;
                    } else if (currentPlugin.getPluginGuiType() == PluginGuiType.NONE) {
                        currentPlugin.execute();
                        currentPlugin.finish();
                        return "";
                    } else {
                        Helper.setFehlerMeldung("cannotStartPlugin");
                    }

                }
            }
        }

        return "";
    }

    /**
     * generate a list of all available process templates
     * 
     * @return
     */

    public List<Process> getAvailableProcessTemplates() {
        if (availableProcessTemplates == null) {
            String sql = FilterHelper.criteriaBuilder("", true, null, null, null, true, false);
            availableProcessTemplates = ProcessManager.getProcesses("prozesse.titel", sql);
        }
        return availableProcessTemplates;
    }

    public void changeTemplate() {
        BeanHelper helper = new BeanHelper();
        helper.changeProcessTemplate(processToChange, template);
        loadProcessProperties();
    }

    /**
     * Create the database information xml file and send it to the servlet output stream
     */
    public void downloadProcessDatebaseInformation() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {

            org.jdom2.Document doc = new XsltPreparatorDocket().createExtendedDocument(myProzess);

            String outputFileName = myProzess.getId() + "_db_export.xml";

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(outputFileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + outputFileName + "\"");

            try {
                ServletOutputStream out = response.getOutputStream();
                XMLOutputter outp = new XMLOutputter();
                outp.setFormat(Format.getPrettyFormat());
                outp.output(doc, out);
                out.flush();

            } catch (IOException e) {
                Helper.setFehlerMeldung("could not export database information: ", e);
            }
            facesContext.responseComplete();
        }
    }

    /**
     * Check if the current element is not the last element of the filtered list
     * 
     * @return
     */

    public boolean isHasNextEntry() {
        if (paginator == null) {
            return false;
        }

        List<Integer> idList = paginator.getIdList();
        if (idList == null || idList.isEmpty()) {
            return false;
        }

        Integer lastId = idList.get(idList.size() - 1);
        if (myProzess.getId().equals(lastId)) {
            return false;
        }
        return true;
    }

    /**
     * Check if current process is not the first element of filtered list
     * 
     * @return
     */

    public boolean isHasPreviousEntry() {
        if (paginator == null) {
            return false;
        }
        List<Integer> idList = paginator.getIdList();
        if (idList == null || idList.isEmpty()) {
            return false;
        }

        Integer lastId = idList.get(0);
        if (myProzess.getId().equals(lastId)) {
            return false;
        }
        return true;
    }

    /**
     * Navigate to the next element of the filtered list
     * 
     */

    public void nextEntry() {
        List<Integer> idList = paginator.getIdList();
        if (idList == null || idList.isEmpty() || idList.size() == 1) {
            return;
        }
        ListIterator<Integer> it = idList.listIterator();
        Integer newProcessId = null;
        while (it.hasNext()) {
            Integer currentId = it.next();
            if (currentId.equals(myProzess.getId())) {
                newProcessId = it.hasNext() ? it.next() : null;
                break;
            }
        }
        if (newProcessId != null) {
            myProzess = ProcessManager.getProcessById(newProcessId);
        }

    }

    /**
     * Navigate to the previous element of the filtered list
     * 
     */

    public void previousEntry() {
        List<Integer> idList = paginator.getIdList();
        if (idList == null || idList.isEmpty() || idList.size() == 1) {
            return;
        }
        Integer newProcessId = null;
        for (int i = 0; i < idList.size(); i++) {
            Integer currentId = idList.get(i);
            if (currentId.equals(myProzess.getId()) && i != 0) {
                newProcessId = idList.get(i - 1);
                break;
            }
        }

        //        Iterator<Integer> it =idList.iterator();
        //        while (it.hasNext()) {
        //            Integer currentId = it.next();
        //            if (currentId.equals(myProzess.getId())) {
        //                System.out.println("current " +currentId);
        //                newProcessId = it.previous();
        //                System.out.println("prev " +newProcessId);
        //                break;
        //            }
        //        }
        if (newProcessId != null) {
            myProzess = ProcessManager.getProcessById(newProcessId);
        }
    }
}
