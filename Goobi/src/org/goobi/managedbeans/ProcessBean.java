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
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.goobi.beans.Docket;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Project;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.WikiFieldHelper;
import org.goobi.production.export.ExportXmlLog;
import org.goobi.production.flow.helper.SearchResultGeneration;
import org.goobi.production.flow.statistics.StatisticsManager;
import org.goobi.production.flow.statistics.StatisticsRenderingElement;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.properties.IProperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.jdom.transform.XSLTransformException;
import org.jfree.chart.plot.PlotOrientation;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.goobi.beans.Process;

//import de.sub.goobi.beans.Schritteigenschaft;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.export.download.ExportMets;
import de.sub.goobi.export.download.ExportPdf;
import de.sub.goobi.export.download.Multipage;
import de.sub.goobi.export.download.TiffHeader;
import de.sub.goobi.forms.ProzesskopieForm;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.PropertyListObject;
import de.sub.goobi.helper.WebDav;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;

@ManagedBean(name = "ProzessverwaltungForm")
@SessionScoped
public class ProcessBean extends BasicBean {
    private static final long serialVersionUID = 2838270843176821134L;
    private static final Logger logger = Logger.getLogger(ProcessBean.class);
    private Process myProzess = new Process();
    private Step mySchritt = new Step();
    private StatisticsManager statisticsManager;
    private List<ProcessCounterObject> myAnzahlList;
    private HashMap<String, Integer> myAnzahlSummary;
    private Processproperty myProzessEigenschaft;
    //    private Schritteigenschaft mySchrittEigenschaft;
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
    private Map<Integer, PropertyListObject> containers = new TreeMap<Integer, PropertyListObject>();
    private Integer container;
    private String addToWikiField = "";
    private String userDisplayMode = "";

    private boolean showStatistics = false;

    private static String DONEDIRECTORYNAME = "fertig/";

    public ProcessBean() {
        this.anzeigeAnpassen = new HashMap<String, Boolean>();
        this.anzeigeAnpassen.put("lockings", false);
        this.anzeigeAnpassen.put("swappedOut", false);
        this.anzeigeAnpassen.put("selectionBoxes", false);
        this.anzeigeAnpassen.put("processId", false);
        this.anzeigeAnpassen.put("batchId", false);
        this.sortierung = "titel";
        /*
         * Vorgangsdatum generell anzeigen?
         */
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        if (login.getMyBenutzer() != null) {
            this.anzeigeAnpassen.put("processDate", login.getMyBenutzer().isConfVorgangsdatumAnzeigen());
        } else {
            this.anzeigeAnpassen.put("processDate", false);
        }
        DONEDIRECTORYNAME = ConfigurationHelper.getInstance().getDoneDirectoryName();

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
        Reload();

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
                } else {
                    /* Prozesseigenschaften */
                    if (myProzess.getEigenschaftenList() != null && !myProzess.getEigenschaftenList().isEmpty()) {
                        for (Processproperty pe : this.myProzess.getEigenschaftenList()) {
                            if (pe != null && pe.getWert() != null) {
                                if (pe.getWert().contains(this.myProzess.getTitel())) {
                                    pe.setWert(pe.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
                                }
                            }
                        }
                    }
                    /* Scanvorlageneigenschaften */
                    if (myProzess.getVorlagenList() != null && !myProzess.getVorlagenList().isEmpty()) {
                        for (Template vl : this.myProzess.getVorlagenList()) {
                            for (Templateproperty ve : vl.getEigenschaftenList()) {
                                if (ve.getWert().contains(this.myProzess.getTitel())) {
                                    ve.setWert(ve.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
                                }
                            }
                        }
                    }
                    /* Werkstückeigenschaften */
                    if (myProzess.getWerkstueckeList() != null && !myProzess.getWerkstueckeList().isEmpty()) {
                        for (Masterpiece w : this.myProzess.getWerkstueckeList()) {
                            for (Masterpieceproperty we : w.getEigenschaftenList()) {
                                if (we.getWert().contains(this.myProzess.getTitel())) {
                                    we.setWert(we.getWert().replaceAll(this.myProzess.getTitel(), this.myNewProcessTitle));
                                }
                            }
                        }
                    }
                    try {
                        {
                            // renaming image directories
                            String imageDirectory = myProzess.getImagesDirectory();
                            File dir = new File(imageDirectory);
                            if (dir.exists() && dir.isDirectory()) {
                                File[] subdirs = dir.listFiles();
                                for (File imagedir : subdirs) {
                                    if (imagedir.isDirectory()) {
                                        imagedir.renameTo(new File(imagedir.getAbsolutePath().replace(myProzess.getTitel(), myNewProcessTitle)));
                                    }
                                }
                            }
                        }
                        {
                            // renaming ocr directories
                            String ocrDirectory = myProzess.getOcrDirectory();
                            File dir = new File(ocrDirectory);
                            if (dir.exists() && dir.isDirectory()) {
                                File[] subdirs = dir.listFiles();
                                for (File imagedir : subdirs) {
                                    if (imagedir.isDirectory()) {
                                        imagedir.renameTo(new File(imagedir.getAbsolutePath().replace(myProzess.getTitel(), myNewProcessTitle)));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.trace("could not rename folder", e);
                    }

                    /* Vorgangstitel */
                    this.myProzess.setTitel(this.myNewProcessTitle);

                    if (!this.myProzess.isIstTemplate()) {
                        /* Tiffwriter-Datei löschen */
                        GoobiScript gs = new GoobiScript();
                        List<Process> pro = new ArrayList<Process>();
                        pro.add(this.myProzess);
                        gs.deleteTiffHeaderFile(pro);
                        gs.updateImagePath(pro);
                    }
                }

            }

            try {
                ProcessManager.saveProcess(this.myProzess);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            }
        } else {
            Helper.setFehlerMeldung("titleEmpty");
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

    public String ContentLoeschen() {
        // deleteMetadataDirectory();
        try {
            File ocr = new File(this.myProzess.getOcrDirectory());
            if (ocr.exists()) {
                Helper.deleteDir(ocr);
            }
            File images = new File(this.myProzess.getImagesDirectory());
            if (images.exists()) {
                Helper.deleteDir(images);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Can not delete metadata directory", e);
        }

        Helper.setMeldung("Content deleted");
        return "";
    }

    private void deleteMetadataDirectory() {
        for (Step step : this.myProzess.getSchritteList()) {
            this.mySchritt = step;
            deleteSymlinksFromUserHomes();
        }
        try {
            Helper.deleteDir(new File(this.myProzess.getProcessDataDirectory()));
            File ocr = new File(this.myProzess.getOcrDirectory());
            if (ocr.exists()) {
                Helper.deleteDir(ocr);
            }
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
            ProzesskopieForm pkf = (ProzesskopieForm) Helper.getManagedBeanValue("#{ProzesskopieForm}");
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
        this.mySchritt = new Step();
        this.modusBearbeiten = "schritt";
        return "process_edit_step";
    }

    public void SchrittUebernehmen() {
        this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
        mySchritt.setBearbeitungszeitpunkt(new Date());
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        if (!myProzess.getSchritte().contains(mySchritt)) {
            this.myProzess.getSchritte().add(this.mySchritt);
            this.mySchritt.setProzess(this.myProzess);
        }
        Speichern();
    }

    public String SchrittLoeschen() {
        this.myProzess.getSchritte().remove(this.mySchritt);
        StepManager.deleteStep(mySchritt);
        deleteSymlinksFromUserHomes();
        return "process_edit";
    }

    private void deleteSymlinksFromUserHomes() {
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

    public String BenutzerLoeschen() {
        this.mySchritt.getBenutzer().remove(this.myBenutzer);
        StepManager.removeUserFromStep(mySchritt, myBenutzer);
        return "";
    }

    public String BenutzergruppeLoeschen() {
        this.mySchritt.getBenutzergruppen().remove(this.myBenutzergruppe);
        StepManager.removeUsergroupFromStep(mySchritt, myBenutzergruppe);
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
        } catch (Exception e) {
            List<String> param = new ArrayList<String>();
            param.add("METS");
            param.add(this.myProzess.getTitel());

            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", param), e);
            //            ;An error occured while trying to export METS file for: " + this.myProzess.getTitel(), e);
            logger.error("ExportMETS error", e);
        }
    }

    public void ExportPdf() {
        ExportPdf export = new ExportPdf();
        try {
            export.startExport(this.myProzess);
        } catch (Exception e) {
            List<String> param = new ArrayList<String>();
            param.add("PDF");
            param.add(this.myProzess.getTitel());
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", param), e);

            Helper.setFehlerMeldung("An error occured while trying to export PDF file for: " + this.myProzess.getTitel(), e);
            logger.error("ExportPDF error", e);
        }
    }

    public void ExportDMS() {
        ExportDms export = new ExportDms();
        try {
            export.startExport(this.myProzess);
        } catch (Exception e) {
            List<String> param = new ArrayList<String>();
            param.add("DMS");
            param.add(this.myProzess.getTitel());
            Helper.setFehlerMeldung(Helper.getTranslation("BatchExportError", param), e);
            //            Helper.setFehlerMeldung("An error occured while trying to export to DMS for: " + this.myProzess.getTitel(), e);
            logger.error("ExportDMS error", e);
        }
    }

    @SuppressWarnings("unchecked")
    public void ExportDMSPage() {
        ExportDms export = new ExportDms();
        Boolean flagError = false;
        for (Process proz : (List<Process>) this.paginator.getList()) {
            try {
                export.startExport(proz);
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
        ExportDms export = new ExportDms();
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                try {
                    export.startExport(proz);
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
        ExportDms export = new ExportDms();
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            try {
                export.startExport(proz);
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
        return "";
    }

    public void DownloadToHome() {
        /*
         * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde, ansonsten
         * Download
         */
        if (!this.myProzess.isImageFolderInUse()) {
            WebDav myDav = new WebDav();
            myDav.DownloadToHome(this.myProzess, 0, false);
        } else {
            Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + this.myProzess.getTitel() + " " + Helper.getTranslation("isInUse"),
                    this.myProzess.getImageFolderInUseUser().getNachVorname());
            WebDav myDav = new WebDav();
            myDav.DownloadToHome(this.myProzess, 0, true);
        }
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomePage() {
        WebDav myDav = new WebDav();
        for (Process proz : (List<Process>) this.paginator.getList()) {
            /*
             * zunächst prüfen, ob dieser Band gerade von einem anderen Nutzer in Bearbeitung ist und in dessen Homeverzeichnis abgelegt wurde,
             * ansonsten Download
             */
            if (!proz.isImageFolderInUse()) {
                myDav.DownloadToHome(proz, 0, false);
            } else {
                Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"), proz
                        .getImageFolderInUseUser().getNachVorname());
                myDav.DownloadToHome(proz, 0, true);
            }
        }
        Helper.setMeldung(null, "createdInUserHome", "");
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomeSelection() {
        WebDav myDav = new WebDav();
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                if (!proz.isImageFolderInUse()) {
                    myDav.DownloadToHome(proz, 0, false);
                } else {
                    Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"),
                            proz.getImageFolderInUseUser().getNachVorname());
                    myDav.DownloadToHome(proz, 0, true);
                }
            }
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void DownloadToHomeHits() {
        WebDav myDav = new WebDav();
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            if (!proz.isImageFolderInUse()) {
                myDav.DownloadToHome(proz, 0, false);
            } else {
                Helper.setMeldung(null, Helper.getTranslation("directory ") + " " + proz.getTitel() + " " + Helper.getTranslation("isInUse"), proz
                        .getImageFolderInUseUser().getNachVorname());
                myDav.DownloadToHome(proz, 0, true);
            }
        }
        Helper.setMeldung(null, "createdInUserHomeAll", "");
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusHochsetzenPage() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            stepStatusUp(proz);
        }
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusHochsetzenSelection() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                stepStatusUp(proz);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusHochsetzenHits() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            stepStatusUp(proz);
        }
    }

    private void stepStatusUp(Process proz) throws DAOException {
        List<Step> stepList = new ArrayList<Step>(proz.getSchritteList());

        for (Step so : stepList) {
            if (!so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
                so.setBearbeitungsstatusEnum(StepStatus.getStatusFromValue(so.getBearbeitungsstatusEnum().getValue() + 1));
                so.setEditTypeEnum(StepEditType.ADMIN);
                if (so.getBearbeitungsstatusEnum().equals(StepStatus.DONE)) {
                    new HelperSchritte().CloseStepObjectAutomatic(so, true);
                } else {
                    User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                    if (ben != null) {
                        so.setBearbeitungsbenutzer(ben);
                    }
                    ProcessManager.saveProcess(proz);
                }
                break;
            }
        }
    }

    private void debug(String message, List<Step> bla) {
        for (Step s : bla) {
            logger.warn(message + " " + s.getTitel() + "   " + s.getReihenfolge());
        }
    }

    private void stepStatusDown(Process proz) throws DAOException {
        List<Step> tempList = new ArrayList<Step>(proz.getSchritteList());
        debug("templist: ", tempList);

        Collections.reverse(tempList);
        debug("reverse: ", tempList);

        for (Step step : tempList) {
            if (step.getBearbeitungsstatusEnum() != StepStatus.LOCKED) {
                step.setEditTypeEnum(StepEditType.ADMIN);
                mySchritt.setBearbeitungszeitpunkt(new Date());
                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setBearbeitungsbenutzer(ben);
                }
                step.setBearbeitungsstatusDown();
                break;
            }
        }
        ProcessManager.saveProcess(proz);
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusRuntersetzenPage() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            stepStatusDown(proz);
        }
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusRuntersetzenSelection() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getList()) {
            if (proz.isSelected()) {
                stepStatusDown(proz);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void BearbeitungsstatusRuntersetzenHits() throws DAOException {
        for (Process proz : (List<Process>) this.paginator.getCompleteList()) {
            stepStatusDown(proz);
        }
    }

    public void SchrittStatusUp() {
        if (this.mySchritt.getBearbeitungsstatusEnum() != StepStatus.DONE) {
            this.mySchritt.setBearbeitungsstatusUp();
            this.mySchritt.setEditTypeEnum(StepEditType.ADMIN);
            //            StepObject so = StepObjectManager.getStepById(this.mySchritt.getId());
            if (this.mySchritt.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                new HelperSchritte().CloseStepObjectAutomatic(mySchritt, true);
            } else {
                mySchritt.setBearbeitungszeitpunkt(new Date());
                User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
                if (ben != null) {
                    mySchritt.setBearbeitungsbenutzer(ben);
                }
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
        User ben = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        if (ben != null) {
            mySchritt.setBearbeitungsbenutzer(ben);
        }
        this.mySchritt.setBearbeitungsstatusDown();
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
    }

    public void setMySchrittReload(Step mySchritt) {
        this.mySchritt = mySchritt;
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
        this.sortierung = "titelAsc";
        this.modusAnzeige = modusAnzeige;
    }

    public String getModusBearbeiten() {
        return this.modusBearbeiten;
    }

    public void setModusBearbeiten(String modusBearbeiten) {
        this.modusBearbeiten = modusBearbeiten;
    }

    public String reihenfolgeUp() {
        this.mySchritt.setReihenfolge(Integer.valueOf(this.mySchritt.getReihenfolge().intValue() - 1));
        try {
            StepManager.saveStep(mySchritt);
            // set list to null to reload list of steps in new order
            myProzess.setSchritte(null);
        } catch (DAOException e) {
            logger.error(e);
        }
        return Reload();
    }

    public String reihenfolgeDown() {
        this.mySchritt.setReihenfolge(Integer.valueOf(this.mySchritt.getReihenfolge().intValue() + 1));
        try {
            StepManager.saveStep(mySchritt);
            // set list to null to reload list of steps in new order
            myProzess.setSchritte(null);
        } catch (DAOException e) {
            logger.error(e);
        }
        return Reload();
    }

    public String Reload() {
        if (myProzess != null && myProzess.getId() != null) {
            myProzess = ProcessManager.getProcessById(myProzess.getId());
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
        if (inProjektAuswahl.intValue() != 0) {
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
        List<SelectItem> myProjekte = new ArrayList<SelectItem>();
        List<Project> temp = ProjectManager.getAllProjects();
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
        if (selected.intValue() != 0) {
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
        List<SelectItem> rulesets = new ArrayList<SelectItem>();
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
        if (selected.intValue() != 0) {
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
        List<SelectItem> myProjekte = new ArrayList<SelectItem>();
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
        ArrayList<Process> auswahl = new ArrayList<Process>();
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
        //		XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();
        this.myAnzahlList = new ArrayList<ProcessCounterObject>();
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
        //		int faktor = 1;
        //		if (this.myAnzahlList != null && this.myAnzahlList.size() > 0) {
        //			faktor = this.myAnzahlList.size();
        //		}
        this.myAnzahlSummary = new HashMap<String, Integer>();
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

    public List<ProcessCounterObject> getMyAnzahlList() {
        return this.myAnzahlList;
    }

    /**
     * Starte GoobiScript über alle Treffer
     */
    @SuppressWarnings("unchecked")
    public void GoobiScriptHits() {
        GoobiScript gs = new GoobiScript();
        gs.execute((List<Process>) this.paginator.getCompleteList(), this.goobiScript);
    }

    /**
     * Starte GoobiScript über alle Treffer der Seite
     */
    @SuppressWarnings("unchecked")
    public void GoobiScriptPage() {
        GoobiScript gs = new GoobiScript();
        gs.execute((List<Process>) this.paginator.getList(), this.goobiScript);
    }

    /**
     * Starte GoobiScript über alle selectierten Treffer
     */
    @SuppressWarnings("unchecked")
    public void GoobiScriptSelection() {
        ArrayList<Process> auswahl = new ArrayList<Process>();
        for (Process p : (List<Process>) this.paginator.getList()) {
            if (p.isSelected()) {
                auswahl.add(p);
            }
        }
        GoobiScript gs = new GoobiScript();
        gs.execute(auswahl, this.goobiScript);
    }

    /*
     * Statistische Auswertung
     */

    public void StatisticsStatusVolumes() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.STATUS_VOLUMES, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
        this.statisticsManager.calculate();
    }

    public void StatisticsUsergroups() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.USERGROUPS, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
        this.statisticsManager.calculate();
    }

    public void StatisticsRuntimeSteps() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.SIMPLE_RUNTIME_STEPS, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
    }

    public void StatisticsProduction() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.PRODUCTION, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
    }

    public void StatisticsStorage() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.STORAGE, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
    }

    public void StatisticsCorrection() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.CORRECTIONS, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
    }

    public void StatisticsTroughput() {
        this.statisticsManager =
                new StatisticsManager(StatisticsMode.THROUGHPUT, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
    }

    public void StatisticsProject() {
        this.statisticsManager = new StatisticsManager(StatisticsMode.PROJECTS, FacesContext.getCurrentInstance().getViewRoot().getLocale(), filter);
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

    public void DownloadMultiTiff() throws IOException, InterruptedException, SwapException, DAOException {
        Multipage mp = new Multipage();
        mp.ExportStart(this.myProzess);
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
     * Getter for showStatistics
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

    public void CreateXML() {
        ExportXmlLog xmlExport = new ExportXmlLog();
        try {
            LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
            String ziel = login.getMyBenutzer().getHomeDir() + this.myProzess.getTitel() + "_log.xml";
            xmlExport.startExport(this.myProzess, ziel);
        } catch (IOException e) {
            Helper.setFehlerMeldung("could not write logfile to home directory: ", e);
        } catch (InterruptedException e) {
            Helper.setFehlerMeldung("could not execute command to write logfile to home directory", e);
        }
    }

    /**
     * transforms xml logfile with given xslt and provides download
     */
    public void TransformXml() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {
            String OutputFileName = "export.xml";
            /*
             * Vorbereiten der Header-Informationen
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

            ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(OutputFileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + OutputFileName + "\"");

            response.setContentType("text/xml");

            try {
                ServletOutputStream out = response.getOutputStream();
                ExportXmlLog export = new ExportXmlLog();
                export.startTransformation(out, this.myProzess, this.selectedXslt);
                out.flush();
            } catch (ConfigurationException e) {
                Helper.setFehlerMeldung("could not create logfile: ", e);
            } catch (XSLTransformException e) {
                Helper.setFehlerMeldung("could not create transformation: ", e);
            } catch (IOException e) {
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
            int myid = new Integer(id);
            this.myProzess = ProcessManager.getProcessById(myid);
            //        } catch (DAOException e) {
            //            logger.error(e);
        } catch (NumberFormatException e) {
            logger.warn(e);
        }
    }

    public List<String> getXsltList() {
        List<String> answer = new ArrayList<String>();
        File folder = new File("xsltFolder");
        if (folder.isDirectory() && folder.exists()) {
            String[] files = folder.list();

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

    public String downloadDocket() {
        return this.myProzess.downloadDocket();
    }

    public void setMyCurrentTable(StatisticsRenderingElement myCurrentTable) {
        this.myCurrentTable = myCurrentTable;
    }

    public StatisticsRenderingElement getMyCurrentTable() {
        return this.myCurrentTable;
    }

    public void CreateExcel() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
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
                HSSFWorkbook wb = (HSSFWorkbook) this.myCurrentTable.getExcelRenderer().getRendering();
                wb.write(out);
                out.flush();
                facesContext.responseComplete();

            } catch (IOException e) {

            }
        }
    }

    public void generateResultAsPdf() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
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

                SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses, this.showArchivedProjects);
                HSSFWorkbook wb = sr.getResult();
                List<List<HSSFCell>> rowList = new ArrayList<List<HSSFCell>>();
                HSSFSheet mySheet = wb.getSheetAt(0);
                Iterator<Row> rowIter = mySheet.rowIterator();
                while (rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    List<HSSFCell> row = new ArrayList<HSSFCell>();
                    while (cellIter.hasNext()) {
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        row.add(myCell);
                    }
                    rowList.add(row);
                }
                Document document = new Document();
                Rectangle a4quer = new Rectangle(PageSize.A3.getHeight(), PageSize.A3.getWidth());
                PdfWriter.getInstance(document, out);
                document.setPageSize(a4quer);
                document.open();
                if (rowList.size() > 0) {
                    Paragraph p = new Paragraph(rowList.get(0).get(0).toString());

                    document.add(p);
                    PdfPTable table = new PdfPTable(9);
                    table.setSpacingBefore(20);
                    for (int i = 1; i < rowList.size(); i++) {

                        List<HSSFCell> row = rowList.get(i);
                        for (int j = 0; j < row.size(); j++) {
                            HSSFCell myCell = row.get(j);
                            // TODO aufhübschen und nicht toString() nutzen

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

    public void generateResult() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (!facesContext.getResponseComplete()) {

            /*
             * -------------------------------- Vorbereiten der Header-Informationen --------------------------------
             */
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("search.xls");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"search.xls\"");
                ServletOutputStream out = response.getOutputStream();
                SearchResultGeneration sr = new SearchResultGeneration(this.filter, this.showClosedProcesses, this.showArchivedProjects);
                HSSFWorkbook wb = sr.getResult();
                wb.write(out);
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

    /**
     * @return values for wiki field
     */
    public String getWikiField() {
        return this.myProzess.getWikifield();

    }

    /**
     * sets new value for wiki field
     * 
     * @param inString
     */
    public void setWikiField(String inString) {
        this.myProzess.setWikifield(inString);
    }

    public String getAddToWikiField() {
        return this.addToWikiField;
    }

    public void setAddToWikiField(String addToWikiField) {
        this.addToWikiField = addToWikiField;
    }

    public void addToWikiField() {
        if (addToWikiField != null && addToWikiField.length() > 0) {
            User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
            String message = this.addToWikiField + " (" + user.getNachVorname() + ")";
            this.myProzess.setWikifield(WikiFieldHelper.getWikiMessage(this.myProzess, this.myProzess.getWikifield(), "user", message));
            this.addToWikiField = "";
            try {
                ProcessManager.saveProcess(myProzess);
            } catch (DAOException e) {
                logger.error(e);
            }
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
        try {
            this.myProzess = ProcessManager.getProcessById(this.myProzess.getId());
        } catch (Exception e) {
            logger.warn("could not refresh process with id " + this.myProzess.getId(), e);
        }
        this.containers = new TreeMap<Integer, PropertyListObject>();
        this.processPropertyList = PropertyParser.getPropertiesForProcess(this.myProzess);

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
                List<String> param = new ArrayList<String>();
                param.add(p.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
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
                List<String> param = new ArrayList<String>();
                param.add(processProperty.getName());
                String value = Helper.getTranslation("propertyNotValid", param);
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

            if (!this.processProperty.getProzesseigenschaft().getProzess().getEigenschaften().contains(this.processProperty.getProzesseigenschaft())) {
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
        return new ArrayList<Integer>(this.containers.keySet());
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
        List<ProcessProperty> answer = new ArrayList<ProcessProperty>();

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
                pe.setProzess(this.myProzess);
                this.processProperty.setProzesseigenschaft(pe);
                this.myProzess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

        }
        try {
            ProcessManager.saveProcess(this.myProzess);
            Helper.setMeldung("propertySaved");
        } catch (DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("propertiesNotSaved");
        }
        loadProcessProperties();

        return "";
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

    public void createNewProperty() {
        if (this.processPropertyList == null) {
            this.processPropertyList = new ArrayList<ProcessProperty>();
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

}
