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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
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
import java.util.TreeMap;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.goobi.api.mq.QueueType;
import org.goobi.api.mq.TaskTicket;
import org.goobi.api.mq.TicketGenerator;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Docket;
import org.goobi.beans.ExportValidator;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.goobiScript.GoobiScriptManager;
import org.goobi.goobiScript.GoobiScriptResult;
import org.goobi.goobiScript.GoobiScriptTemplate;
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
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.XSLTransformException;
import org.jfree.chart.plot.PlotOrientation;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.intranda.commons.chart.renderer.CSVRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.sub.goobi.config.ConfigExportValidation;
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
import de.sub.goobi.persistence.managers.GoobiScriptTemplateManager;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import io.goobi.workflow.xslt.XsltPreparatorDocket;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.jms.JMSException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;

@Named("ProzessverwaltungForm")
@WindowScoped
@Log4j2
public class ProcessBean extends BasicBean implements Serializable {
    private static final long serialVersionUID = 2838270843176821134L;

    private static final String MODUS_ANZEIGE_AKTUELL = "aktuell";
    private static final String MODUS_ANZEIGE_VORLAGEN = "vorlagen";

    private static final String MODUS_BEARBEITEN_PROZESS = "prozess";
    private static final String MODUS_BEARBEITEN_SCHRITT = "schritt";

    private static final String MODUS_GOOBI_SCRIPT_HITS = "hits";
    private static final String MODUS_GOOBI_SCRIPT_PAGE = "page";
    private static final String MODUS_GOOBI_SCRIPT_SELECTION = "selection";

    private static final String PAGE_PROCESS_ALL = "process_all";
    private static final String PAGE_PROCESS_EDIT = "process_edit";
    private static final String PAGE_PROCESS_EDIT_STEP = "process_edit_step";

    private static final String PAGE_TASK_EDIT_SIMULATOR = "/uii/task_edit_simulator";

    private static final String DB_EXPORT_FILE_SUFFIX = "_db_export.xml";
    private static final String LOG_XML_FILE_SUFFIX = "_log.xml";

    private static final String FOLDER_IMPORT = "import";

    private static final String TRUE = Boolean.TRUE.toString();
    private static final String FALSE = Boolean.FALSE.toString();

    @Getter
    private Process myProzess = new Process();
    @Getter
    private Step mySchritt = new Step();
    @Getter
    private StatisticsManager statisticsManager;

    @Getter
    private List<ProcessCounterObject> myAnzahlList;
    @Getter
    private HashMap<String, Integer> myAnzahlSummary;
    @Getter
    @Setter
    private GoobiProperty myProzessEigenschaft;
    @Getter
    @Setter
    private User myBenutzer;

    @Getter
    @Setter
    private GoobiProperty myVorlageEigenschaft;

    @Getter
    @Setter
    private GoobiProperty myWerkstueckEigenschaft;
    @Getter
    @Setter
    private Usergroup myBenutzergruppe;
    @Getter
    @Setter
    private String modusAnzeige = MODUS_ANZEIGE_AKTUELL;
    @Getter
    @Setter
    private String modusBearbeiten = "";
    @Getter
    @Setter
    private String goobiScript;
    @Getter
    @Setter
    private HashMap<String, Boolean> anzeigeAnpassen;
    @Getter
    @Setter
    private String myNewProcessTitle;
    @Getter
    @Setter
    private String selectedXslt = "";
    @Getter
    @Setter
    private StatisticsRenderingElement myCurrentTable;
    @Getter
    @Setter
    private boolean showClosedProcesses = false;
    @Getter
    @Setter
    private boolean showArchivedProjects = false;
    private List<DisplayProperty> processPropertyList;
    @Getter
    @Setter
    private DisplayProperty processProperty;
    @Getter
    private Map<String, PropertyListObject> containers = new TreeMap<>();
    @Getter
    private String container;
    @Getter
    @Setter
    private String userDisplayMode = "";

    @Getter
    @Setter
    private boolean dispaySearchResult = false;
    @Getter
    @Setter
    private List<SearchColumn> searchField = new ArrayList<>();
    @Setter
    private List<SelectItem> possibleItems = null;
    @Getter
    @Setter
    private SearchColumn currentField = null;
    private int order = 0;

    @Getter
    @Setter
    private boolean showStatistics = false;

    private static String doneDirectoryName = "fertig/";

    @Getter
    private DatabasePaginator usergroupPaginator;
    @Getter
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

    @Getter
    @Setter
    private List<Map<String, String>> parsedGoobiScripts;

    private List<Process> availableProcessTemplates = null;

    @Getter
    @Setter
    private Process processToChange;
    @Getter
    @Setter
    private Process template;

    private List<StringPair> allGoobiScripts;
    private List<StringPair> hiddenGoobiScripts;

    @Getter
    @Setter
    private boolean createNewStepAllowParallelTask;

    @Getter
    private Map<String, List<String>> displayableMetadataMap = new HashMap<>();

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private StepBean bean;

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private GoobiScriptManager goobiScriptManager;

    public ProcessBean() {
        this.anzeigeAnpassen = new HashMap<>();

        anzeigeAnpassen.put("numberOfImages", false);
        sortField = "prozesse.titel";
        /*
         * Vorgangsdatum generell anzeigen?
         */
        User user = Helper.getLoginBean().getMyBenutzer();
        if (user != null) {

            this.anzeigeAnpassen.put("lockings", user.isDisplayLocksColumn());
            this.anzeigeAnpassen.put("swappedOut", user.isDisplaySwappingColumn());
            this.anzeigeAnpassen.put("selectionBoxes", user.isDisplaySelectBoxes());
            this.anzeigeAnpassen.put("processId", user.isDisplayIdColumn());
            this.anzeigeAnpassen.put("batchId", user.isDisplayBatchColumn());
            this.anzeigeAnpassen.put("processDate", user.isDisplayProcessDateColumn());
            this.anzeigeAnpassen.put("processRuleset", user.isDisplayRulesetColumn());
            this.anzeigeAnpassen.put("thumbnail", user.isDisplayThumbColumn());
            this.anzeigeAnpassen.put("metadatadetails", user.isDisplayMetadataColumn());
            this.anzeigeAnpassen.put("gridview", user.isDisplayGridView());
            this.anzeigeAnpassen.put("numberOfImages", user.isDisplayNumberOfImages());

            showClosedProcesses = user.isDisplayFinishedProcesses();
            showArchivedProjects = user.isDisplayDeactivatedProjects();
            anzeigeAnpassen.put("institution", user.isDisplayInstitutionColumn());

            boolean showEditionData = ConfigurationHelper.getInstance().isProcesslistShowEditionData();
            anzeigeAnpassen.put("editionUser", showEditionData && user.isDisplayLastEditionUser());
            anzeigeAnpassen.put("editionTask", showEditionData && user.isDisplayLastEditionTask());
            anzeigeAnpassen.put("lastStatusUpdate", user.isDisplayLastEditionDate());
            String defaultProcessListSortField = user.getProcessListDefaultSortField();
            if (StringUtils.isNotBlank(defaultProcessListSortField)) {
                sortField = defaultProcessListSortField + user.getProcessListDefaultSortOrder();
            }

        } else {
            this.anzeigeAnpassen.put("lockings", false);
            this.anzeigeAnpassen.put("swappedOut", false);
            this.anzeigeAnpassen.put("selectionBoxes", false);
            this.anzeigeAnpassen.put("processId", false);
            this.anzeigeAnpassen.put("batchId", false);
            this.anzeigeAnpassen.put("processDate", false);
            anzeigeAnpassen.put("institution", false);
            this.anzeigeAnpassen.put("processRuleset", false);
            this.anzeigeAnpassen.put("numberOfImages", false);
        }
        doneDirectoryName = ConfigurationHelper.getInstance().getDoneDirectoryName(); // NOSONAR

        searchField.add(new SearchColumn(order++));

        stepPluginList = PluginLoader.getListOfPlugins(PluginType.Step);
        stepPluginList.addAll(PluginLoader.getListOfPlugins(PluginType.Export));
        Collections.sort(stepPluginList);

        validationPluginList = PluginLoader.getListOfPlugins(PluginType.Validation);
        Collections.sort(validationPluginList);
    }

    /**
     * needed for ExtendedSearch.
     *
     * @return true
     */
    public boolean getInitialize() {
        return true;
    }

    public String Neu() {
        this.myProzess = new Process();
        this.myNewProcessTitle = "";
        this.modusBearbeiten = MODUS_BEARBEITEN_PROZESS;
        return PAGE_PROCESS_EDIT;
    }

    public String NeuVorlage() {
        this.myProzess = new Process();
        this.myNewProcessTitle = "";
        this.myProzess.setIstTemplate(true);
        this.modusBearbeiten = MODUS_BEARBEITEN_PROZESS;
        return PAGE_PROCESS_EDIT;
    }

    public String editProcess() {
        reload();

        return PAGE_PROCESS_EDIT;
    }

    public String Speichern() {
        /*
         * wenn der Vorgangstitel geändert wurde, wird dieser geprüft und bei erfolgreicher Prüfung an allen relevanten Stellen mitgeändert
         */
        if (this.myProzess != null && this.myProzess.getTitel() != null) {
            if (!this.myProzess.getTitel().equals(this.myNewProcessTitle)) {
                String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
                if (!this.myNewProcessTitle.matches(validateRegEx)) {
                    this.modusBearbeiten = MODUS_BEARBEITEN_PROZESS;
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
                    return "";
                } else if (ProcessManager.countProcessTitle(myNewProcessTitle, myProzess.getProjekt().getInstitution()) != 0) {
                    this.modusBearbeiten = MODUS_BEARBEITEN_PROZESS;
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("UngueltigeDaten:") + " " + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
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
        ProcessManager.deleteProcess(this.myProzess);
        Helper.setMeldung("Process deleted");
        if (MODUS_ANZEIGE_VORLAGEN.equals(this.modusAnzeige)) {
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
        String dbExportFileName = processId + DB_EXPORT_FILE_SUFFIX;

        Path processFolder = Paths.get(this.myProzess.getProcessDataDirectory());
        Path importFolder = processFolder.resolve(FOLDER_IMPORT);

        Path dbExportFile = importFolder.resolve(dbExportFileName);

        if (!StorageProvider.getInstance().isFileExists(dbExportFile)) {
            Helper.setFehlerMeldung("DB export file does not exist in " + dbExportFile);
            return;
        }

        StorageProvider.getInstance().copyFile(dbExportFile, processFolder.resolve(dbExportFileName));

        TaskTicket importTicket = TicketGenerator.generateSimpleTicket("DatabaseInformationTicket");
        //filename of xml file is "<processId>_db_export.xml"
        importTicket.setProcessName(processId);

        importTicket.getProperties().put("processFolder", processFolder.toString());
        importTicket.getProperties().put("createNewProcessId", FALSE);
        importTicket.getProperties().put("tempFolder", null);
        importTicket.getProperties().put("rule", "Autodetect rule");
        importTicket.getProperties().put("deleteOldProcess", TRUE);
        try {
            TicketGenerator.submitInternalTicket(importTicket, QueueType.FAST_QUEUE, "DatabaseInformationTicket", 0);
        } catch (JMSException e) {
            log.error("Error adding TaskTicket to queue", e);

            JournalEntry errorEntry = new JournalEntry(myProzess.getId(), new Date(), "automatic", LogType.ERROR,
                    "Error reading metadata for process" + this.myProzess.getTitel(), EntryType.PROCESS);
            JournalManager.saveJournalEntry(errorEntry);
        }
    }

    public String ContentLoeschen() {
        try {
            Path ocr = Paths.get(this.myProzess.getOcrDirectory());
            if (StorageProvider.getInstance().isFileExists(ocr)) {
                StorageProvider.getInstance().deleteDir(ocr);
            }
            Path images = Paths.get(this.myProzess.getImagesDirectory());
            if (StorageProvider.getInstance().isFileExists(images)) {
                StorageProvider.getInstance().deleteDir(images);
            }
        } catch (IOException | SwapException e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }
        Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, "Deleted content for this process in process details.");

        Helper.setMeldung("Content deleted");
        return "";
    }

    private void deleteMetadataDirectory() {
        // get all assigned users
        List<User> assignedUsers = new ArrayList<>();
        for (Step step : this.myProzess.getSchritteList()) {

            if (step.isTypImagesLesen() || step.isTypImagesSchreiben()) {
                for (User b : step.getBenutzerList()) {
                    if (!assignedUsers.contains(b)) {
                        assignedUsers.add(b);
                    }
                }
                for (Usergroup bg : step.getBenutzergruppenList()) {
                    for (User b : bg.getBenutzer()) {
                        if (!assignedUsers.contains(b)) {
                            assignedUsers.add(b);
                        }
                    }
                }
            }
        }
        // remove any symlinks/mounts
        WebDav myDav = new WebDav();
        for (User b : assignedUsers) {
            try {
                myDav.uploadFromHome(b, this.mySchritt.getProzess());
            } catch (RuntimeException exception) {
                // ignore this
            }
        }
        // delete process folder
        try {
            StorageProvider.getInstance().deleteDir(Paths.get(this.myProzess.getProcessDataDirectory()));
        } catch (IOException | SwapException e) {
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

        String searchValue = filter;
        if (StringUtils.isNotBlank(additionalFilter) && StringUtils.isNotBlank(filter)) {
            searchValue = additionalFilter.replace("{}", filter);
        }

        String sql = FilterHelper.criteriaBuilder(searchValue, false, null, null, null, true, false);
        if ("vorlagen".equals(this.modusAnzeige)) {
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
        if (!this.showClosedProcesses && !"vorlagen".equals(this.modusAnzeige)) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " projekte.projectIsArchived = false ";
        }

        paginator = new DatabasePaginator(sortField, sql, m, "process_all");

        this.modusAnzeige = "aktuell";
        return "process_all";
    }

    public String FilterAktuelleProzesseOfGoobiScript(String status) {

        List<GoobiScriptResult> resultList = Helper.getSessionBean().getGsm().getGoobiScriptResults();
        StringBuilder bld = new StringBuilder("\"id:");
        synchronized (resultList) {
            for (GoobiScriptResult gsr : resultList) {
                if (gsr.getResultType().toString().equals(status)) {
                    bld.append(gsr.getProcessId()).append(" ");
                }
            }
        }
        bld.append("\"");
        filter = bld.toString();
        return FilterAktuelleProzesse();
    }

    public String FilterVorlagen() {
        this.statisticsManager = null;
        this.myAnzahlList = null;

        ProzesskopieForm pkf = Helper.getBeanByClass(ProzesskopieForm.class);
        pkf.clearAvailableProjects();

        String searchValue = filter;
        if (StringUtils.isNotBlank(additionalFilter)) {
            searchValue = additionalFilter.replace("{}", filter);
        }

        String sql = FilterHelper.criteriaBuilder(searchValue, true, null, null, null, true, false);

        if (!this.showClosedProcesses && !"vorlagen".equals(this.modusAnzeige)) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " projekte.projectIsArchived = false ";
        }
        ProcessManager m = new ProcessManager();
        paginator = new DatabasePaginator(sortField, sql, m, "process_all");
        this.modusAnzeige = "vorlagen";
        return "process_all";
    }

    public String NeuenVorgangAnlegen() {
        FilterVorlagen();
        if (this.paginator.getTotalResults() == 1) {
            Process einziger = (Process) this.paginator.getList().get(0);
            ProzesskopieForm processCopyForm = Helper.getBeanByClass(ProzesskopieForm.class);
            processCopyForm.setProzessVorlage(einziger);
            return processCopyForm.prepare();
        } else {
            return PAGE_PROCESS_ALL;
        }
    }

    /**
     * Anzeige der Sammelbände filtern
     */
    public String FilterAlleStart() {
        this.statisticsManager = null;
        this.myAnzahlList = null;
        String searchValue = filter;
        if (StringUtils.isNotBlank(additionalFilter) && StringUtils.isNotBlank(filter)) {
            searchValue = additionalFilter.replace("{}", filter);
        }

        String sql = FilterHelper.criteriaBuilder(searchValue, null, null, null, null, true, false);
        if ("vorlagen".equals(this.modusAnzeige)) {
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
        if (!this.showClosedProcesses && !"vorlagen".equals(this.modusAnzeige)) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " projekte.projectIsArchived = false ";
        }

        ProcessManager m = new ProcessManager();
        paginator = new DatabasePaginator(sortField, sql, m, "process_all");

        return "process_all";
    }

    /*
     * Eigenschaften
     */
    public String ProzessEigenschaftLoeschen() {
        myProzess.getEigenschaften().remove(myProzessEigenschaft);
        PropertyManager.deleteProperty(myProzessEigenschaft);
        return "";
    }

    public String ProzessEigenschaftNeu() {
        myProzessEigenschaft = new GoobiProperty(PropertyOwnerType.PROCESS);
        return "";
    }

    public String ProzessEigenschaftUebernehmen() {
        if (!myProzess.getEigenschaften().contains(myProzessEigenschaft)) {
            myProzess.getEigenschaften().add(myProzessEigenschaft);
            myProzessEigenschaft.setOwner(myProzess);
        }
        PropertyManager.saveProperty(myProzessEigenschaft);
        return "";
    }

    /*
     * Schritte
     */

    public String SchrittNeu() {
        // Process is needed for the predefined order
        this.createNewStepAllowParallelTask = false;
        this.mySchritt = new Step(this.myProzess);
        this.modusBearbeiten = MODUS_BEARBEITEN_SCHRITT;
        return PAGE_PROCESS_EDIT_STEP;
    }

    public String SchrittUebernehmen() {

        // Still on page when order is out of range (order < 1)
        if (this.mySchritt.getReihenfolge() != null && this.mySchritt.getReihenfolge() < 1) {
            this.modusBearbeiten = MODUS_BEARBEITEN_SCHRITT;
            Helper.setFehlerMeldung("Order may not be less than 1. (Is currently " + this.mySchritt.getReihenfolge() + ")");
            return PAGE_PROCESS_EDIT_STEP;
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
            if (mySchritt.isTypScriptStep()) {
                numberOfActions = numberOfActions + 1;
            }
            if (StringUtils.isNotBlank(mySchritt.getStepPlugin()) && !mySchritt.isDelayStep() && !mySchritt.isTypExportDMS()) {
                numberOfActions = numberOfActions + 1;
            }
            if (numberOfActions > 1) {
                Helper.setFehlerMeldung("step_error_to_many_actions");
                modusBearbeiten = MODUS_BEARBEITEN_SCHRITT;
                return PAGE_PROCESS_EDIT_STEP;
            }
        } else {
            //not automatic: then remove from message queue:
            mySchritt.setMessageQueue(QueueType.NONE);
        }

        this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User user = Helper.getCurrentUser();
        if (user != null) {
            mySchritt.setBearbeitungsbenutzer(user);
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

        this.modusBearbeiten = "";
        return PAGE_PROCESS_EDIT_STEP;
    }

    /**
     * Increments the order of all steps coming after this.mySchritt. this.mySchritt is explicitly expluded here, that means this.mySchritt always
     * keeps its order, even if it is inserted directly before the current step.
     */
    public void incrementOrderOfHigherSteps() {
        List<Step> steps = this.myProzess.getSchritte();
        for (Step step : steps) {
            int order = step.getReihenfolge();
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
        return PAGE_PROCESS_EDIT;
    }

    private void deleteSymlinksFromUserHomes() {
        Helper.addMessageToProcessJournal(myProzess.getId(), LogType.DEBUG, "Removed links in home directories for all users in process details.");

        WebDav myDav = new WebDav();
        /* alle Benutzer */
        for (User b : this.mySchritt.getBenutzerList()) {
            try {
                myDav.uploadFromHome(b, this.mySchritt.getProzess());
            } catch (RuntimeException exception) {
                // ignore this
            }
        }
        /* alle Benutzergruppen mit ihren Benutzern */
        for (Usergroup bg : this.mySchritt.getBenutzergruppenList()) {
            for (User b : bg.getBenutzer()) {
                try {
                    myDav.uploadFromHome(b, this.mySchritt.getProzess());
                } catch (RuntimeException exception) {
                    // ignore this
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
                log.error(e);
            }
        }
        updateUsergroupPaginator();
        return "";
    }

    private void updateUsergroupPaginator() {
        String filter = " benutzergruppen.BenutzergruppenID not in "
                + "(select BenutzerGruppenID from schritteberechtigtegruppen where schritteberechtigtegruppen.schritteID = " + mySchritt.getId()
                + ")";

        UsergroupManager m = new UsergroupManager();
        usergroupPaginator = new DatabasePaginator("titel", filter, m, "");
    }

    private void updateUserPaginator() {
        String filter = "benutzer.BenutzerID not in"
                + "(select BenutzerID from schritteberechtigtebenutzer where schritteberechtigtebenutzer.schritteID = " + mySchritt.getId() + ") AND "
                + "benutzer.BenutzerID not in (select BenutzerID from benutzer where benutzer.userstatus = 'deleted')";

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
                log.error(e);
            }
        }
        updateUserPaginator();
        return "";
    }

    /*
     * Aktionen
     */

    public void exportMets() {
        ExportMets export = new ExportMets();
        try {
            export.startExport(this.myProzess);
            Helper.addMessageToProcessJournal(this.myProzess.getId(), LogType.DEBUG, "Started METS export using 'ExportMets'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | DocStructHasNoTypeException | UGHException | ExportFileException | UghHelperException | SwapException
                | DAOException e) {
            String[] parameter = { "METS", this.myProzess.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);
            log.error("ExportMETS error", e);
        }
    }

    public void downloadMets() {
        ExportMets export = new ExportMets();
        try {
            export.downloadMets(this.myProzess);
            Helper.addMessageToProcessJournal(this.myProzess.getId(), LogType.DEBUG, "Started METS export using 'ExportMets'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | DocStructHasNoTypeException | UGHException | SwapException | DAOException e) {
            String[] parameter = { "METS", this.myProzess.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);
            log.error("ExportMETS error", e);
        }
    }

    public void exportPdf() {
        ExportPdf export = new ExportPdf();
        try {
            export.startExport(this.myProzess);
            Helper.addMessageToProcessJournal(this.myProzess.getId(), LogType.DEBUG, "Started PDF export using 'ExportPdf'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | DocStructHasNoTypeException | UGHException | ExportFileException | UghHelperException | SwapException
                | DAOException e) {
            String[] parameter = { "PDF", this.myProzess.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);

            Helper.setFehlerMeldung("An error occured while trying to export PDF file for: " + this.myProzess.getTitel(), e);
            log.error("ExportPDF error", e);
        }
    }

    public void exportDMS() {
        if (this.myProzess.getContainsExportStep()) {
            String pluginName = ProcessManager.getExportPluginName(myProzess.getId());
            ProcessBean.executeExportPlugin(pluginName, this.myProzess);
        } else {
            Helper.setFehlerMeldung("noExportTaskError");
        }
    }

    public void exportStep() {
        ProcessBean.executeExportPlugin(mySchritt.getStepPlugin(), this.myProzess);
    }

    private static void executeExportPlugin(String pluginName, Process process) {
        IExportPlugin export = null;
        if (StringUtils.isNotEmpty(pluginName)) {
            try {
                export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
            } catch (Exception e) {
                String message = "Can't load export plugin, use default export";
                log.error(message, e);
                Helper.setFehlerMeldung(message);
                export = new ExportDms();
            }
        }
        if (export == null) {
            export = new ExportDms();
            Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, "Started export using 'ExportDMS'.");
        }
        try {
            boolean success = export.startExport(process);
            if (!success) {
                String message = "ExportDMS was not successfull";
                Helper.setFehlerMeldung(message);
                log.warn(message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | DocStructHasNoTypeException | UGHException | ExportFileException | UghHelperException | SwapException
                | DAOException e) {
            String[] parameter = { "DMS", process.getTitel() };
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", parameter), e);
            log.error("ExportDMS error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void exportDMSPage() {

        for (Process proz : (List<Process>) this.paginator.getList()) {
            exportSingleProcess(proz);
        }

    }

    @SuppressWarnings("unchecked")
    public void exportDMSSelection() {

        for (Process proz : (List<Process>) this.paginator.getList()) {

            if (proz.isSelected()) {
                exportSingleProcess(proz);
            }
        }
        Helper.setMeldung(null, "ExportFinished", "");
    }

    private void exportSingleProcess(Process proz) {
        IExportPlugin export = null;
        String pluginName = ProcessManager.getExportPluginName(proz.getId());
        if (StringUtils.isNotEmpty(pluginName)) {
            try {
                export = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, pluginName);
            } catch (Exception e) {
                String message = "Can't load export plugin, use default export";
                log.error(message, e);
                Helper.setFehlerMeldung(message);
                export = new ExportDms();
            }
        }
        if (export == null) {
            export = new ExportDms();
        }
        try {
            export.startExport(proz);
            Helper.addMessageToProcessJournal(proz.getId(), LogType.DEBUG, "Started export using 'ExportDMSSelection'.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException | DocStructHasNoTypeException | UGHException | ExportFileException | UghHelperException | SwapException
                | DAOException e) {
            Helper.setFehlerMeldung("ExportError", e.getMessage());
            log.error(e);
        }
    }

    @SuppressWarnings("unchecked")
    public void exportDMSHits() {

        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            exportSingleProcess(proz);
        }
        Helper.setMeldung(null, "ExportFinished", "");
    }

    public String uploadFromHomeAlle() {
        WebDav myDav = new WebDav();
        List<String> folder = myDav.uploadFromHomeAll(doneDirectoryName);
        myDav.removeFromHomeAlle(folder, doneDirectoryName);
        Helper.setMeldung(null, "directoryRemovedAll", doneDirectoryName);
        return "";
    }

    public String uploadFromHome() {
        WebDav myDav = new WebDav();
        myDav.uploadFromHome(this.myProzess);
        Helper.setMeldung(null, "directoryRemoved", this.myProzess.getTitel());
        Helper.addMessageToProcessJournal(this.myProzess.getId(), LogType.DEBUG, "Process uploaded from home directory via process list.");
        return "";
    }

    public void downloadToHome() {
        doDownloadToHome(this.myProzess);
    }

    private void doDownloadToHome(Process process) {
        /*
         * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde, ansonsten
         * Download
         */
        String message = "Process downloaded into home directory from process list. Available access rights: ";
        if (!process.isImageFolderInUse()) {
            WebDav myDav = new WebDav();
            myDav.downloadToHome(process, 0, false);
            Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, message + "write");
        } else {
            String information = Helper.getTranslation("directory ") + " " + process.getTitel() + " " + Helper.getTranslation("isInUse");
            String userName = process.getImageFolderInUseUser().getNachVorname();
            Helper.setMeldung(null, information, userName);
            WebDav myDav = new WebDav();
            myDav.downloadToHome(process, 0, true);
            Helper.addMessageToProcessJournal(process.getId(), LogType.DEBUG, message + "read");
        }
    }

    @SuppressWarnings("unchecked")
    public void downloadToHomePage() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            doDownloadToHome(proz);
        }
        Helper.setMeldung(null, "createdInUserHome", "");
    }

    @SuppressWarnings("unchecked")
    public void downloadToHomeSelection() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                doDownloadToHome(proz);
            }
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void downloadToHomeHits() {
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            doDownloadToHome(proz);
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void generateFilterWithIdentfiers() {
        StringBuilder bld = new StringBuilder("\"id:");
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            bld.append(proz.getId()).append(" ");
        }
        bld.append("\"");
        filter = bld.toString();
    }

    public void stepStatusUp() {
        StepStatus status = this.mySchritt.getBearbeitungsstatusEnum();
        if (status != StepStatus.DONE && status != StepStatus.DEACTIVATED) {
            this.mySchritt.setBearbeitungsstatusUp();
            this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
            String statusString = this.mySchritt.getBearbeitungsstatusAsString();
            String message = "Changed status for step '" + mySchritt.getTitel() + "' to " + statusString + " in process details (moved status up).";
            Helper.addMessageToProcessJournal(mySchritt.getProcessId(), LogType.DEBUG, message);
            if (this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                new HelperSchritte().CloseStepObjectAutomatic(mySchritt);
            } else {
                mySchritt.setBearbeitungszeitpunkt(new Date());
            }
        }
        try {
            StepManager.saveStep(mySchritt);
        } catch (DAOException e) {
            log.error(e);
        }
        myProzess.setSchritte(null);
        deleteSymlinksFromUserHomes();
    }

    public String stepStatusDown() {
        this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
        mySchritt.setBearbeitungszeitpunkt(new Date());

        this.mySchritt.setBearbeitungsstatusDown();
        String statusString = this.mySchritt.getBearbeitungsstatusAsString();
        String message = "Changed status for step '" + mySchritt.getTitel() + "' to " + statusString + " in process details (moved status down).";
        Helper.addMessageToProcessJournal(mySchritt.getProcessId(), LogType.DEBUG, message);
        try {
            StepManager.saveStep(mySchritt);
            new HelperSchritte().updateProcessStatus(myProzess.getId());
        } catch (DAOException e) {
            log.error(e);
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
    public void selectionAll() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            proz.setSelected(true);
        }
    }

    @SuppressWarnings("unchecked")
    public void selectionNone() {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            proz.setSelected(false);
        }
    }

    /*
     * Getter und Setter
     */

    public void setMyProzess(Process myProzess) {
        this.myProzess = myProzess;
        this.myNewProcessTitle = myProzess.getTitel();
        loadProcessProperties();
        loadDisplayableMetadata();
    }

    public void setMySchritt(Step mySchritt) {
        this.mySchritt = mySchritt;
        updateUsergroupPaginator();
        updateUserPaginator();
    }

    public void setMySchrittReload(Step mySchritt) {
        setMySchritt(mySchritt);
    }

    public String decrementOrder() {
        int oldOrder = this.mySchritt.getReihenfolge();

        if (oldOrder > 1) {
            this.mySchritt.setReihenfolge(oldOrder - 1);
            this.saveStepInStepManager(this.mySchritt);
        }
        return this.reload();
    }

    public String incrementOrder() {
        int oldOrder = this.mySchritt.getReihenfolge();
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
        int targetOrder = this.getNextAvailableOrder(baseOrder, direction); //-1 means downwards, +1 means upwards

        if (targetOrder != baseOrder) { // Otherwise there is no next order, then nothing happens
            int currentOrder;

            // Set all steps with targetOrder to baseOrder
            for (Step step : steps) {
                currentOrder = step.getReihenfolge().intValue();

                if (currentOrder == targetOrder) {
                    step.setReihenfolge(baseOrder);
                    this.saveStepInStepManager(step);
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

        for (Step step : steps) {
            currentOrder = step.getReihenfolge().intValue();
            // Is baseOrder < currentOrder < targetOrder or targetOrder undefined (-1)?
            if (direction == -1) { //downwards

                if (currentOrder < baseOrder) {
                    if (targetOrder == -1 || (targetOrder != -1 && currentOrder > targetOrder)) {
                        targetOrder = currentOrder;
                    }
                }
                // Is targetOrder < currentOrder < baseOrder or targetOrder undefined (-1)?
            } else if (direction == 1) { //upwards

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
            String message = "Changed step order for step '" + step.getTitel() + "' to position " + step.getReihenfolge() + " in process details.";
            Helper.addMessageToProcessJournal(step.getProcessId(), LogType.DEBUG, message);
            // set list to null to reload list of steps in new order
            this.myProzess.setSchritte(null);
        } catch (DAOException e) {
            log.error(e);
        }
    }

    public String reload() {
        if (this.myProzess != null && this.myProzess.getId() != null) {
            this.myProzess = ProcessManager.getProcessById(this.myProzess.getId());
            loadProcessProperties();
        }
        return "";
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
                log.error(e);
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
                log.error(e);
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
                log.error(e);
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

    public String getExportValidationSelection() {
        if (this.myProzess.getExportValidator() != null) {
            return myProzess.getExportValidator().getLabel();
        } else {
            return "";
        }
    }

    public void setExportValidationSelection(String selected) {
        if (StringUtils.isNotBlank(selected)) {
            for (ExportValidator exportValidator : ConfigExportValidation.getConfiguredExportValidators()) {
                if (exportValidator.getLabel().equals(selected)) {
                    myProzess.setExportValidator(exportValidator);
                }
            }
        } else {
            this.myProzess.setExportValidator(null);
        }
    }

    public List<SelectItem> getExportValidationSelectionList() {
        List<SelectItem> options = new ArrayList<>();
        options.add(new SelectItem("", Helper.getTranslation("noValidation")));
        for (ExportValidator exportValidator : ConfigExportValidation.getConfiguredExportValidators()) {
            options.add(new SelectItem(exportValidator.getLabel(), exportValidator.getLabel(), null));
        }
        return options;
    }

    /*
     * Anzahlen der Artikel und Images
     */

    @SuppressWarnings("unchecked")
    public void calcMetadataAndImagesPage() throws IOException, InterruptedException, SwapException, DAOException {
        calcMetadataAndImages((List<Process>) this.paginator.getList());
    }

    @SuppressWarnings("unchecked")
    public void calcMetadataAndImagesSelection() throws IOException, InterruptedException, SwapException, DAOException {
        List<Process> auswahl = new ArrayList<>();
        for (Process p : (List<Process>) this.paginator.getList()) {
            if (p.isSelected()) {
                auswahl.add(p);
            }
        }
        calcMetadataAndImages(auswahl);
    }

    @SuppressWarnings("unchecked")
    public void calcMetadataAndImagesHits() throws IOException, InterruptedException, SwapException, DAOException {
        calcMetadataAndImages((List<Process>) this.paginator.getCompleteList());
    }

    private void calcMetadataAndImages(List<Process> inListe) throws IOException, InterruptedException, SwapException, DAOException { // NOSONAR
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

        this.myAnzahlSummary = new HashMap<>();
        this.myAnzahlSummary.put("sumProcesses", this.myAnzahlList.size());
        this.myAnzahlSummary.put("sumMetadata", allMetadata);
        this.myAnzahlSummary.put("sumDocstructs", allDocstructs);
        this.myAnzahlSummary.put("sumImages", allImages);
        this.myAnzahlSummary.put("averageImages", allImages / countOfProcessesWithImages);
        this.myAnzahlSummary.put("averageMetadata", allMetadata / countOfProcessesWithMetadata);
        this.myAnzahlSummary.put("averageDocstructs", allDocstructs / countOfProcessesWithDocstructs);
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
            log.error(e);
        }
    }

    /**
     * prepare the variables for user question with all hits.
     */
    public void prepareGoobiScriptHits() {
        this.goobiScriptHitsCount = this.paginator.getIdList().size();
        this.goobiScriptMode = MODUS_GOOBI_SCRIPT_HITS;
        this.parseGoobiScripts();
    }

    /**
     * prepare the variables for user question with hits on the current page.
     */
    public void prepareGoobiScriptPage() {
        this.goobiScriptHitsCount = paginator.getList().size();
        this.goobiScriptMode = MODUS_GOOBI_SCRIPT_PAGE;
        this.parseGoobiScripts();
    }

    /**
     * prepare the variables for user question with selected items.
     */
    public void prepareGoobiScriptSelection() {
        this.goobiScriptHitsCount = (int) paginator.getList().stream().filter(p -> ((Process) p).isSelected()).count();
        this.goobiScriptMode = MODUS_GOOBI_SCRIPT_SELECTION;
        this.parseGoobiScripts();
    }

    private void parseGoobiScripts() {
        this.parsedGoobiScripts = GoobiScript.parseGoobiscripts(this.goobiScript);
        if (this.parsedGoobiScripts != null) {
            this.renderHitNumberImage();
        } else {
            Helper.setFehlerMeldung("", "Can't parse GoobiScript. Please check your Syntax. Only valid YAML is allowed.");
        }
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
     * runs the current GoobiScript in the correct mode ("page", "hits" or "selection").
     *
     * @return redirect to process list
     */
    public String runGoobiScript() {
        if (!checkSecurityResult()) {
            Helper.setFehlerMeldung("", "GoobiScript_wrong_answer");
            return "";
        } else {
            resetHitsCount();
            List<Integer> processIds;
            switch (this.goobiScriptMode) {
                case MODUS_GOOBI_SCRIPT_HITS:
                    processIds = this.getProcessIdsForHits();
                    break;
                case MODUS_GOOBI_SCRIPT_PAGE:
                    processIds = this.getProcessIdsForPage();
                    break;
                case MODUS_GOOBI_SCRIPT_SELECTION:
                default:
                    processIds = this.getProcessIdsForSelection();
            }
            this.logGoobiScriptExecution(processIds);
            GoobiScript gs = new GoobiScript();
            gs.execute(processIds, this.parsedGoobiScripts, goobiScriptManager);
            return PAGE_PROCESS_ALL + "?faces-redirect=" + TRUE;
        }
    }

    /**
     * Logs the started goobi scripts on the DEBUG level in the log4j log. The output contains information about the executing user, the entered
     * script and the list of ids of all affected processes.
     *
     * @param processIds The list of ids of affected processes
     */
    private void logGoobiScriptExecution(List<Integer> processIds) {
        Collections.sort(processIds);
        User user = Helper.getCurrentUser();
        StringBuilder buffer = new StringBuilder();
        buffer.append("User \"");
        buffer.append(user != null ? user.getLogin() : "[unknown user]");
        buffer.append("\" executed GoobiScript ...\n");
        buffer.append(this.goobiScript);
        buffer.append("\n... for the processes with following ids:\n");
        buffer.append(processIds.toString());
        log.debug(buffer.toString());
    }

    /**
     * Return a list of all visible GoobiScript commands with their action name and the sample call.
     *
     * @return the list of GoobiScripts
     */
    public List<StringPair> getAllGoobiScripts() {
        if (allGoobiScripts == null) {
            allGoobiScripts = new ArrayList<>();
            for (IGoobiScript gs : goobiScriptManager.getAvailableGoobiScripts()) {
                if (gs.isVisible()) {
                    allGoobiScripts.add(new StringPair(gs.getAction(), gs.getSampleCall()));
                }
            }
        }
        Collections.sort(allGoobiScripts, new StringPair.OneComparator());
        return allGoobiScripts;
    }

    /**
     * Return a list of all hidden GoobiScript commands with their action name and the sample call.
     *
     * @return the list of hidden GoobiScripts
     */
    public List<StringPair> getHiddenGoobiScripts() {
        if (hiddenGoobiScripts == null) {
            hiddenGoobiScripts = new ArrayList<>();
            for (IGoobiScript gs : goobiScriptManager.getAvailableGoobiScripts()) {
                if (!gs.isVisible()) {
                    hiddenGoobiScripts.add(new StringPair(gs.getAction(), gs.getSampleCall()));
                }
            }
        }
        Collections.sort(hiddenGoobiScripts, new StringPair.OneComparator());
        return hiddenGoobiScripts;
    }

    /**
     * Returns the list of process ids for the current search results.
     *
     * @return The list of ids of all found processes
     */
    private List<Integer> getProcessIdsForHits() {
        return this.paginator.getIdList();
    }

    /**
     * Returns the list of process ids for the current page.
     *
     * @return The list of ids of all processes on the current page
     */
    private List<Integer> getProcessIdsForPage() {
        List<Integer> idList = new ArrayList<>();
        for (DatabaseObject processObject : paginator.getList()) {
            Process process = (Process) processObject;
            idList.add(process.getId());
        }
        return idList;
    }

    /**
     * Returns the list of process ids for the current selection.
     *
     * @return The list of ids of selected processes
     */
    private List<Integer> getProcessIdsForSelection() {
        List<Integer> idList = new ArrayList<>();
        for (DatabaseObject processObject : this.paginator.getList()) {
            Process process = (Process) processObject;
            if (process.isSelected()) {
                idList.add(process.getId());
            }
        }
        return idList;
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

    public void statisticsStatusVolumes() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.STATUS_VOLUMES,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
        this.statisticsManager.calculate();
    }

    public void statisticsUsergroups() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.USERGROUPS,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
        this.statisticsManager.calculate();
    }

    public void statisticsRuntimeSteps() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
    }

    public void statisticsProduction() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.PRODUCTION,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
    }

    public void statisticsStorage() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.STORAGE, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(),
                filter, showClosedProcesses, showArchivedProjects);
    }

    public void statisticsCorrection() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.CORRECTIONS,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
    }

    public void statisticsTroughput() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.THROUGHPUT,
                FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(), filter, showClosedProcesses, showArchivedProjects);
    }

    public void statisticsProject() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.PROJECTS, FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale(),
                filter, showClosedProcesses, showArchivedProjects);
        this.statisticsManager.calculate();
    }

    /**
     * ist called via jsp at the end of building a chart in include file Prozesse_Liste_Statistik.jsp and resets the statistics so that with the next
     * reload a chart is not shown anymore.
     *
     * @return current page
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
        return this.paginator.getTotalResults() * 20;
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

    public void downloadTiffHeader() throws IOException {
        TiffHeader tiff = new TiffHeader(this.myProzess);
        tiff.exportStart();
    }

    @Getter
    public static class ProcessCounterObject implements Serializable {
        private static final long serialVersionUID = -4287461260229760734L;
        private String title;
        private int metadata;
        private int docstructs;
        private int images;
        @Setter
        private int relImages;
        @Setter
        private int relDocstructs;
        @Setter
        private int relMetadata;

        public ProcessCounterObject(String title, int metadata, int docstructs, int images) {
            super();
            this.title = title;
            this.metadata = metadata;
            this.docstructs = docstructs;
            this.images = images;
        }
    }

    /**
     * starts generation of xml logfile for current process.
     */
    public void generateSimplifiedMetadataFile() {
        this.myProzess.downloadSimplifiedMetadataAsPDF();
    }

    /**
     * starts generation of xml logfile for current process.
     */

    public void createXML() {
        XsltPreparatorDocket xmlExport = new XsltPreparatorDocket();
        try {
            String ziel = Helper.getCurrentUser().getHomeDir() + this.myProzess.getTitel() + LOG_XML_FILE_SUFFIX;
            xmlExport.startExport(this.myProzess, ziel);
        } catch (IOException e) {
            Helper.setFehlerMeldung("could not write logfile to home directory: ", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * transforms xml logfile with given xslt and provides download.
     */
    public void transformXml() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("export.xml");
            try {
                ServletOutputStream out = response.getOutputStream();
                XsltPreparatorDocket export = new XsltPreparatorDocket();
                export.startTransformation(out, this.myProzess, this.selectedXslt);
                out.flush();
            } catch (ConfigurationException e) {
                Helper.setFehlerMeldung("could not create logfile: ", e);
            } catch (XSLTransformException | IOException e) {
                Helper.setFehlerMeldung("could not create transformation: ", e);
            }
            facesContext.responseComplete();
        }
    }

    public String getMyProcessId() {
        return String.valueOf(this.myProzess.getId());
    }

    public void setMyProcessId(String id) {
        try {
            int myid = Integer.parseInt(id);
            this.myProzess = ProcessManager.getProcessById(myid);

        } catch (NumberFormatException e) {
            log.warn(e);
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

    public String downloadDocket() {
        return this.myProzess.downloadDocket();
    }

    public void downloadStatisticsAsExcel() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("export.xls");
            try {
                ServletOutputStream out = response.getOutputStream();
                Workbook wb = (Workbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void downloadStatisticsAsCsv() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        CSVPrinter csvFilePrinter = null;
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("export.csv");
            try { // NOSONAR try-with-resource not possible as CSVPrinter does not implement AutoCloseable
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
            } catch (IOException exception) {
                log.error(exception);
            } finally {
                try {
                    if (csvFilePrinter != null) {
                        csvFilePrinter.close();
                    }
                } catch (IOException exception) {
                    log.error(exception);
                }
            }
        }
    }

    private List<SearchColumn> prepareSearchColumnData() {
        List<SearchColumn> columnList = new ArrayList<>();
        boolean addAllColumns = false;
        for (SearchColumn sc : searchField) {
            if ("all".equals(sc.getValue())) {
                addAllColumns = true;
                break;
            }
        }
        if (addAllColumns) {
            int currentOrder = 0;
            for (SelectItem si : possibleItems) {
                if (!"all".equals(si.getValue()) && !si.isDisabled() && !((String) si.getValue()).startsWith("index.")) {
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

    public void generateResultAsPdf() { //NOSONAR
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("search.pdf");
            try {
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XSSFWorkbook wb =
                        sch.getResult(prepareSearchColumnData(), this.filter, sortField, this.showClosedProcesses, this.showArchivedProjects);

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
                try (Document document = new Document()) {
                    Rectangle a4quer = new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth());
                    PdfWriter.getInstance(document, out);
                    document.setPageSize(a4quer);
                    document.open();
                    if (!rowList.isEmpty()) {
                        PdfPTable table = new PdfPTable(rowList.get(0).size());
                        table.setSpacingBefore(20);

                        for (List<XSSFCell> row : rowList) {

                            table.completeRow();
                            for (XSSFCell myCell : row) {
                                String stringCellValue = myCell.toString();
                                table.addCell(stringCellValue);
                            }

                        }
                        document.add(table);
                    }
                }
                out.flush();
                facesContext.responseComplete();

            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    public void generateResultXls() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("search.xlsx");
            try {
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XSSFWorkbook wb =
                        sch.getResult(prepareSearchColumnData(), this.filter, sortField, this.showClosedProcesses, this.showArchivedProjects);

                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    public void generateResultDoc() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("search.doc");
            try {
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                XWPFDocument wb =
                        sch.getResultAsWord(prepareSearchColumnData(), this.filter, sortField, this.showClosedProcesses, this.showArchivedProjects);
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    public void generateResultRtf() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse("search.rtf");
            try {
                ServletOutputStream out = response.getOutputStream();
                SearchResultHelper sch = new SearchResultHelper();
                sch.getResultAsRtf(prepareSearchColumnData(), this.filter, sortField, this.showClosedProcesses, this.showArchivedProjects, out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    public List<DisplayProperty> getProcessProperties() {
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
    }

    private void loadProcessProperties() {

        this.myProzess = ProcessManager.getProcessById(this.myProzess.getId());

        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getInstance().getPropertiesForProcess(this.myProzess);

        for (DisplayProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(myProzess);
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
            saveValidProperties();
        }
    }

    private void saveValidProperties() {
        for (DisplayProperty p : this.processPropertyList) {
            if (p.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(this.myProzess);
                p.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            p.transfer();
            if (!this.myProzess.getEigenschaften().contains(p.getProzesseigenschaft())) {
                this.myProzess.getEigenschaften().add(p.getProzesseigenschaft());
            }
        }

        List<GoobiProperty> props = this.myProzess.getEigenschaftenList();
        for (GoobiProperty pe : props) {
            if (pe.getPropertyName() == null) {
                this.myProzess.getEigenschaften().remove(pe);
            } else {
                PropertyManager.saveProperty(pe);

            }
        }
        loadProcessProperties();
        Helper.setMeldung("Properties saved");
    }

    public void saveCurrentProperty() {
        List<DisplayProperty> ppList = getContainerProperties();
        for (DisplayProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                String value = Helper.getTranslation("propertyNotValid", processProperty.getName());
                Helper.setFehlerMeldung(value);
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(myProzess);
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            if (!this.processProperty.getProzesseigenschaft()
                    .getOwner()
                    .getProperties()
                    .contains(this.processProperty.getProzesseigenschaft())) {
                this.processProperty.getProzesseigenschaft().getOwner().getProperties().add(this.processProperty.getProzesseigenschaft());
            }

            PropertyManager.saveProperty(processProperty.getProzesseigenschaft());
        }
        Helper.setMeldung("propertiesSaved");
        loadProcessProperties();
    }

    public int getPropertyListSize() {
        if (this.processPropertyList == null) {
            return 0;
        }
        return this.processPropertyList.size();
    }

    public List<String> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
    }

    public int getContainersSize() {
        if (this.containers == null) {
            return 0;
        }
        return this.containers.size();
    }

    public List<DisplayProperty> getSortedProperties() {
        Comparator<DisplayProperty> comp = new DisplayProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    public void deleteProperty() {
        List<DisplayProperty> ppList = getContainerProperties();
        for (DisplayProperty pp : ppList) {
            this.processPropertyList.remove(pp);
            this.myProzess.getEigenschaften().remove(pp.getProzesseigenschaft());
            PropertyManager.deleteProperty(pp.getProzesseigenschaft());
        }
        loadProcessProperties();
    }

    public void duplicateProperty() {
        DisplayProperty pt = this.processProperty.getClone("0");
        this.processPropertyList.add(pt);
        saveProcessProperties();
    }

    public void setContainer(String container) {
        this.container = container;
        if (container != null && !"0".equals(container)) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    public List<DisplayProperty> getContainerProperties() {
        List<DisplayProperty> answer = new ArrayList<>();

        if (this.container != null && !"0".equals(container)) {
            for (DisplayProperty pp : this.processPropertyList) {
                if (pp.getContainer().equals(container)) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    public String duplicateContainer() {
        String currentContainer = this.processProperty.getContainer();
        List<DisplayProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (DisplayProperty pt : this.processPropertyList) {
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
        for (DisplayProperty pt : plist) {
            DisplayProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            if (this.processProperty.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(myProzess);
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();
            PropertyManager.saveProperty(processProperty.getProzesseigenschaft());
        }
        Helper.setMeldung("propertySaved");
        loadProcessProperties();

        return "";
    }

    public List<DisplayProperty> getContainerlessProperties() {
        List<DisplayProperty> answer = new ArrayList<>();
        for (DisplayProperty pp : this.processPropertyList) {
            if ("0".equals(pp.getContainer())) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public void createNewProperty() {
        if (this.processPropertyList == null) {
            this.processPropertyList = new ArrayList<>();
        }
        DisplayProperty pp = new DisplayProperty();
        pp.setType(Type.TEXT);
        pp.setContainer("0");
        this.processProperty = pp;
    }

    public int getSizeOfFieldList() {
        return searchField.size();
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

    public List<GoobiScriptTemplate> getGoobiScriptTemplates() {

        List<GoobiScriptTemplate> templates = GoobiScriptTemplateManager.getAllGoobiScriptTemplates();

        return templates;
    }

    public void setConfirmLink(boolean confirm) {
        // do nothing, its needed for jsf
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
        new Process(myProzess);
        return FilterVorlagen();
    }

    public String startPlugin() {
        if (StringUtils.isNotBlank(mySchritt.getStepPlugin())) {

            if (mySchritt.isTypExportDMS()) {
                startExport();
            } else if (mySchritt.isDelayStep()) {
                Helper.setFehlerMeldung("cannotStartPlugin");
            } else {
                String message = "Plugin " + mySchritt.getStepPlugin() + " was executed from process details";
                Helper.addMessageToProcessJournal(mySchritt.getProcessId(), LogType.DEBUG, message);
                return startStepPlugin();
            }
        }

        return "";
    }

    private String startStepPlugin() {
        IStepPlugin currentPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, mySchritt.getStepPlugin());
        if (currentPlugin != null) {
            currentPlugin.initialize(mySchritt, "/" + PAGE_PROCESS_EDIT);
            if (currentPlugin.getPluginGuiType() == PluginGuiType.FULL || currentPlugin.getPluginGuiType() == PluginGuiType.PART_AND_FULL) {
                bean.setMyPlugin(currentPlugin);
                String mypath = currentPlugin.getPagePath();
                currentPlugin.execute();
                return mypath;
            } else if (currentPlugin.getPluginGuiType() == PluginGuiType.PART) {

                bean.setMyPlugin(currentPlugin);
                String mypath = PAGE_TASK_EDIT_SIMULATOR;
                currentPlugin.execute();
                return mypath;
            } else if (currentPlugin.getPluginGuiType() == PluginGuiType.NONE) {
                currentPlugin.execute();
                currentPlugin.finish();
            } else {
                Helper.setFehlerMeldung("cannotStartPlugin");
            }
        }
        return "";
    }

    private void startExport() {
        IExportPlugin dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, mySchritt.getStepPlugin());
        try {
            dms.startExport(mySchritt.getProzess());
        } catch (DocStructHasNoTypeException | PreferencesException | WriteException | MetadataTypeNotAllowedException | ReadException
                | TypeNotAllowedForParentException | IOException | ExportFileException | UghHelperException | SwapException | DAOException e) {
            log.error(e);
            Helper.setFehlerMeldung("Can't load export plugin.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * generate a list of all available process templates.
     *
     * @return process list
     */

    public List<Process> getAvailableProcessTemplates() {
        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            // limit result to institution of current user
            inst = user.getInstitution();
        }

        if (availableProcessTemplates == null) {
            String sql = FilterHelper.criteriaBuilder("", true, null, null, null, true, false);
            availableProcessTemplates = ProcessManager.getProcesses("prozesse.titel", sql, inst);
        }
        return availableProcessTemplates;
    }

    public void changeTemplate() {
        BeanHelper helper = new BeanHelper();
        helper.changeProcessTemplate(processToChange, template);
        loadProcessProperties();
    }

    /**
     * Create the database information xml file and send it to the servlet output stream.
     */
    public void downloadProcessDatebaseInformation() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            org.jdom2.Document doc = new XsltPreparatorDocket().createExtendedDocument(myProzess);
            // Prepare header information
            HttpServletResponse response = this.createHttpServletResponse(myProzess.getId() + DB_EXPORT_FILE_SUFFIX);
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

    public HttpServletResponse createHttpServletResponse(String fileName) {

        // Get context objects
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        ExternalContext externalContext = facesContext.getExternalContext();
        ServletContext servletContext = (ServletContext) externalContext.getContext();

        // Create and initialize response object
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        response.setContentType(servletContext.getMimeType(fileName));
        response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

        return response;
    }

    public boolean isFoldersArchived() throws IOException, SwapException {
        Path images = Paths.get(this.myProzess.getImagesDirectory());
        try (Stream<Path> filesInImages = Files.list(images)) {
            return filesInImages.anyMatch(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".xml"));
        }
    }

    /**
     * Check if the current element is not the last element of the filtered list.
     *
     * @return true if current page is not last
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
        return !myProzess.getId().equals(lastId);
    }

    /**
     * Check if current process is not the first element of filtered list.
     *
     * @return true if current page is not first
     *
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
        return !myProzess.getId().equals(lastId);
    }

    /**
     * Navigate to the next element of the filtered list.
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
            myNewProcessTitle = myProzess.getTitel();
        }

    }

    /**
     * Navigate to the previous element of the filtered list.
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

        if (newProcessId != null) {
            myProzess = ProcessManager.getProcessById(newProcessId);
            myNewProcessTitle = myProzess.getTitel();
        }
    }

}
