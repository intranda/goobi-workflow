package org.goobi.production.cli.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.model.SelectItem;
import javax.naming.NamingException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.beans.User;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.importer.ImportObject;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.AdditionalField;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

@Log4j2
public class CopyProcess {

    UghHelper ughHelp = new UghHelper();
    private BeanHelper bhelp = new BeanHelper();
    private Fileformat myRdf;
    @Getter
    @Setter
    private String opacSuchfeld = "12";
    @Getter
    @Setter
    private String opacSuchbegriff;
    @Getter
    @Setter
    private String opacKatalog;
    @Getter
    @Setter
    private Process prozessVorlage = new Process();
    @Getter
    @Setter
    private Process prozessKopie = new Process();
    private ConfigOpac co;
    /* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
    @Getter
    private boolean useOpac;
    @Getter
    private boolean useTemplates;
    @Setter
    @Getter
    private String metadataFile;

    @Getter
    private HashMap<String, Boolean> standardFields;
    @Getter
    private List<AdditionalField> additionalFields;
    @Getter
    @Setter
    private List<String> digitalCollections;
    @Getter
    @Setter
    private String tifHeader_imagedescription = "";
    @Getter
    @Setter
    private String tifHeader_documentname = "";

    private String naviFirstPage;
    @Getter
    @Setter
    private Integer auswahl;
    @Getter
    @Setter
    private String docType;
    private String atstsl = "";
    private List<String> possibleDigitalCollection;
    private boolean updateData = false;

    public final static String DIRECTORY_SUFFIX = "_tif";

    /* =============================================================== */

    public String Prepare(ImportObject io) {
        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            return "";
        }

        clearValues();

        this.co = ConfigOpac.getInstance();

        try {
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            this.myRdf = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            this.myRdf.read(this.metadataFile);
        } catch (ReadException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        ;
        this.prozessKopie = new Process();
        this.prozessKopie.setTitel("");
        this.prozessKopie.setIstTemplate(false);
        this.prozessKopie.setInAuswahllisteAnzeigen(false);
        this.prozessKopie.setProjekt(this.prozessVorlage.getProjekt());
        this.prozessKopie.setRegelsatz(this.prozessVorlage.getRegelsatz());
        this.prozessKopie.setDocket(this.prozessVorlage.getDocket());
        this.digitalCollections = new ArrayList<>();

        /*
         * -------------------------------- Kopie der Prozessvorlage anlegen --------------------------------
         */
        this.bhelp.SchritteKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.ScanvorlagenKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.WerkstueckeKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.EigenschaftenKopieren(this.prozessVorlage, this.prozessKopie);

        return this.naviFirstPage;
    }

    public String Prepare() {
        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            for (Step s : this.prozessVorlage.getSchritteList()) {
                if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                    Helper.setFehlerMeldung("Kein Benutzer festgelegt für: ", s.getTitel());
                }
            }
            return "";
        }

        clearValues();
        this.co = ConfigOpac.getInstance();
        try {
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            this.myRdf = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            this.myRdf.read(this.metadataFile);
        } catch (ReadException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }
        ;
        this.prozessKopie = new Process();
        this.prozessKopie.setTitel("");
        this.prozessKopie.setIstTemplate(false);
        this.prozessKopie.setInAuswahllisteAnzeigen(false);
        this.prozessKopie.setProjekt(this.prozessVorlage.getProjekt());
        this.prozessKopie.setRegelsatz(this.prozessVorlage.getRegelsatz());
        this.digitalCollections = new ArrayList<>();

        /*
         * -------------------------------- Kopie der Prozessvorlage anlegen --------------------------------
         */
        this.bhelp.SchritteKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.ScanvorlagenKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.WerkstueckeKopieren(this.prozessVorlage, this.prozessKopie);
        this.bhelp.EigenschaftenKopieren(this.prozessVorlage, this.prozessKopie);

        initializePossibleDigitalCollections();

        return this.naviFirstPage;
    }

    /* =============================================================== */

    private void readProjectConfigs() {
        /*--------------------------------
         * projektabhängig die richtigen Felder in der Gui anzeigen
         * --------------------------------*/
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        this.docType = cp.getParamString("createNewProcess/defaultdoctype", this.co.getAllDoctypes().get(0).getTitle());
        this.useOpac = cp.getParamBoolean("createNewProcess/opac/@use");
        this.useTemplates = cp.getParamBoolean("createNewProcess/templates/@use");
        this.naviFirstPage = "ProzessverwaltungKopie1";
        if (this.opacKatalog.equals("")) {
            this.opacKatalog = cp.getParamString("createNewProcess/opac/catalogue");
        }

        /*
         * -------------------------------- die auszublendenden Standard-Felder ermitteln --------------------------------
         */
        for (String t : cp.getParamList("createNewProcess/itemlist/hide")) {
            this.standardFields.put(t, false);
        }

        /*
         * -------------------------------- die einzublendenen (zusätzlichen) Eigenschaften ermitteln --------------------------------
         */
        List<HierarchicalConfiguration> itemList = cp.getList("createNewProcess/itemlist/item");
        for (HierarchicalConfiguration item : itemList) {
            AdditionalField fa = new AdditionalField();
            fa.setFrom(item.getString("@from"));
            fa.setTitel(item.getString("."));
            fa.setRequired(item.getBoolean("@required", false));
            fa.setIsdoctype(item.getString("@isdoctype"));
            fa.setIsnotdoctype(item.getString("@isnotdoctype"));

            // attributes added 30.3.09
            String test = (item.getString("@initStart"));
            fa.setInitStart(test);

            fa.setInitEnd(item.getString("@initEnd"));

            /*
             * -------------------------------- Bindung an ein Metadatum eines Docstructs --------------------------------
             */
            if (item.getBoolean("@ughbinding", false)) {
                fa.setUghbinding(true);
                fa.setDocstruct(item.getString("@docstruct"));
                fa.setMetadata(item.getString("@metadata"));
            }

            /*
             * -------------------------------- prüfen, ob das aktuelle Item eine Auswahlliste werden soll --------------------------------
             */
            List<HierarchicalConfiguration> selectItems = item.configurationsAt("select");
            if (selectItems != null && !selectItems.isEmpty()) {
                List<SelectItem> items = new ArrayList<>();
                for (HierarchicalConfiguration hc : selectItems) {
                    String svalue = hc.getString("@label");
                    String sid = hc.getString(".");
                    items.add(new SelectItem(sid, svalue, null));
                }
                fa.setSelectList(items);
            }
            this.additionalFields.add(fa);
        }
    }

    public String OpacAuswerten(ImportObject io) {
        clearValues();
        readProjectConfigs();
        try {
            /* den Opac abfragen und ein RDF draus bauen lassen */
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            this.myRdf = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            this.myRdf.read(this.metadataFile);

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

            fillFieldsFromConfig();

        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
            e.printStackTrace();
        }
        return "";
    }

    /**
     * OpacAnfrage
     */

    public String OpacAuswerten() {
        clearValues();
        readProjectConfigs();
        try {
            /* den Opac abfragen und ein RDF draus bauen lassen */
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            this.myRdf = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            this.myRdf.read(this.metadataFile);

            this.docType = this.myRdf.getDigitalDocument().getLogicalDocStruct().getType().getName();

            fillFieldsFromMetadataFile(this.myRdf);

            fillFieldsFromConfig();

        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen des Opac-Ergebnisses ", e);
            e.printStackTrace();
        }
        return "";
    }

    /* =============================================================== */

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei füllen
     * 
     * @throws PreferencesException
     */
    private void fillFieldsFromMetadataFile(Fileformat myRdf) throws PreferencesException {
        if (myRdf != null) {
            UghHelper ughHelp = new UghHelper();

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                    /* welches Docstruct */

                    DocStruct myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
                    if (field.getDocstruct().equals("firstchild")) {
                        try {
                            myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                        }
                    }
                    if (field.getDocstruct().equals("boundbook")) {
                        myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (field.getMetadata().equals("ListOfCreators")) {
                            /* bei Autoren die Namen zusammenstellen */
                            String myautoren = "";
                            if (myTempStruct.getAllPersons() != null) {
                                for (Person p : myTempStruct.getAllPersons()) {
                                    myautoren += p.getLastname();
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        myautoren += ", " + p.getFirstname();
                                    }
                                    myautoren += "; ";
                                }
                                if (myautoren.endsWith("; ")) {
                                    myautoren = myautoren.substring(0, myautoren.length() - 2);
                                }
                            }
                            field.setWert(myautoren);
                        } else {
                            /* bei normalen Feldern die Inhalte auswerten */
                            MetadataType mdt = ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
                            Metadata md = ughHelp.getMetadata(myTempStruct, mdt);
                            if (md != null) {
                                field.setWert(md.getValue());
                            }
                        }
                    } catch (UghHelperException e) {
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                } // end if ughbinding
            } // end for
        } // end if myrdf==null
    }

    private void fillFieldsFromConfig() {
        for (AdditionalField field : this.additionalFields) {
            if (!field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                if (field.getSelectList() != null && field.getSelectList().size() > 0) {
                    field.setWert((String) field.getSelectList().get(0).getValue());
                }

            }
        }
        CalcTiffheader();

    }

    /**
     * alle Konfigurationseigenschaften und Felder zurücksetzen ================================================================
     */
    private void clearValues() {
        if (this.opacKatalog == null) {
            this.opacKatalog = "";
        }
        this.standardFields = new HashMap<>();
        this.standardFields.put("collections", true);
        this.standardFields.put("doctype", true);
        this.standardFields.put("regelsatz", true);
        this.additionalFields = new ArrayList<>();
        this.tifHeader_documentname = "";
        this.tifHeader_imagedescription = "";
    }

    /**
     * Auswahl des Prozesses auswerten
     * 
     * @throws DAOException
     * @throws NamingException
     * @throws SQLException ============================================================ == ==
     */

    public String TemplateAuswahlAuswerten() throws DAOException {
        /* den ausgewählten Prozess laden */
        Process tempProzess = ProcessManager.getProcessById(this.auswahl);
        if (tempProzess.getWerkstueckeSize() > 0) {
            /* erstes Werkstück durchlaufen */
            Masterpiece werk = tempProzess.getWerkstueckeList().get(0);
            for (Masterpieceproperty eig : werk.getEigenschaften()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitel().equals(eig.getTitel())) {
                        field.setWert(eig.getWert());
                    }
                }
            }
        }

        if (tempProzess.getVorlagenSize() > 0) {
            /* erste Vorlage durchlaufen */
            Template vor = tempProzess.getVorlagenList().get(0);
            for (Templateproperty eig : vor.getEigenschaften()) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.getTitel().equals(eig.getTitel())) {
                        field.setWert(eig.getWert());
                    }
                }
            }
        }

        try {
            this.myRdf = tempProzess.readMetadataAsTemplateFile();
        } catch (Exception e) {
            Helper.setFehlerMeldung("Fehler beim Einlesen der Template-Metadaten ", e);
        }

        /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
        try {
            DocStruct colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
            removeCollections(colStruct);
            colStruct = colStruct.getAllChildren().get(0);
            removeCollections(colStruct);
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Fehler beim Anlegen des Vorgangs", e);
            log.error("Fehler beim Anlegen des Vorgangs", e);
        } catch (RuntimeException e) {
            /*
             * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
             */
        }

        return "";
    }

    /**
     * Validierung der Eingaben
     * 
     * @return sind Fehler bei den Eingaben vorhanden? ================================================================
     */
    private boolean isContentValid() {
        /*
         * -------------------------------- Vorbedingungen prüfen --------------------------------
         */
        boolean valide = true;

        /*
         * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
         */
        /* kein Titel */
        if (this.prozessKopie.getTitel() == null || this.prozessKopie.getTitel().equals("")) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
        }

        String validateRegEx = ConfigurationHelper.getInstance().getProcessTiteValidationlRegex();
        if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
            valide = false;
            Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
        }

        /* prüfen, ob der Prozesstitel schon verwendet wurde */
        if (this.prozessKopie.getTitel() != null) {
            long anzahl = 0;
            //			try {
            anzahl = ProcessManager.countProcessTitle(this.prozessKopie.getTitel(), prozessKopie.getProjekt().getInstitution());
            //			} catch (DAOException e) {
            //				Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
            //				valide = false;
            //			}
            if (anzahl > 0) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten: ") + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
            }
        }

        /*
         * -------------------------------- Prüfung der standard-Eingaben, die angegeben werden müssen --------------------------------
         */
        /* keine Collektion ausgewählt */
        if (this.standardFields.get("collections") && getDigitalCollections().size() == 0) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        /*
         * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
         */
        for (AdditionalField field : this.additionalFields) {
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype(getDocType())
                    && (StringUtils.isBlank(field.getWert()))) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitel() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }
        }
        return valide;
    }

    /* =============================================================== */

    public String GoToSeite1() {
        return this.naviFirstPage;
    }

    /* =============================================================== */

    public String GoToSeite2() {
        if (!isContentValid()) {
            return this.naviFirstPage;
        } else {
            return "ProzessverwaltungKopie2";
        }
    }

    public boolean testTitle() {
        boolean valide = true;

        if (ConfigurationHelper.getInstance().isMassImportUniqueTitle()) {
            /*
             * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
             */
            /* kein Titel */
            if (this.prozessKopie.getTitel() == null || this.prozessKopie.getTitel().equals("")) {
                valide = false;
                Helper.setFehlerMeldung(
                        Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigurationHelper.getInstance().getProcessTiteValidationlRegex();
            if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
                valide = false;
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            /* prüfen, ob der Prozesstitel schon verwendet wurde */
            if (this.prozessKopie.getTitel() != null) {
                long anzahl = 0;
                //				try {
                anzahl = ProcessManager.countProcessTitle(this.prozessKopie.getTitel(), prozessKopie.getProjekt().getInstitution());
                //				} catch (DAOException e) {
                //					Helper.setFehlerMeldung("Fehler beim Einlesen der Vorgaenge", e.getMessage());
                //					valide = false;
                //				}
                if (anzahl > 0) {
                    valide = false;
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("UngueltigeDaten:") + Helper.getTranslation("ProcessCreationErrorTitleAllreadyInUse"));
                }
            }
        }
        return valide;
    }

    /**
     * Anlegen des Prozesses und Speichern der Metadaten ================================================================
     * 
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */

    public Process NeuenProzessAnlegen2()
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException {

        this.prozessKopie.setId(null);

        EigenschaftenHinzufuegen(null);

        for (Step step : this.prozessKopie.getSchritteList()) {
            /*
             * -------------------------------- always save date and user for each step --------------------------------
             */
            step.setBearbeitungszeitpunkt(this.prozessKopie.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            User user = Helper.getCurrentUser();
            if (user != null) {
                step.setBearbeitungsbenutzer(user);
            }

            /*
             * -------------------------------- only if its done, set edit start and end date --------------------------------
             */
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(this.prozessKopie.getErstellungsdatum());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }

        try {

            ProcessManager.saveProcess(this.prozessKopie);
            //			dao.refresh(this.prozessKopie);
        } catch (DAOException e) {
            e.printStackTrace();
            log.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        // /*--------------------------------
        // * wenn eine RDF-Konfiguration
        // * vorhanden ist (z.B. aus dem Opac-Import, oder frisch angelegt),
        // dann
        // * diese ergänzen
        // * --------------------------------*/
        if (this.updateData) {
            if (this.myRdf != null) {
                for (AdditionalField field : this.additionalFields) {
                    if (field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                        /* welches Docstruct */
                        DocStruct myTempStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
                        DocStruct myTempChild = null;
                        if (field.getDocstruct().equals("firstchild")) {
                            try {
                                myTempStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                            } catch (RuntimeException e) {
                                /*
                                 * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
                                 */
                            }
                        }
                        /*
                         * falls topstruct und firstchild das Metadatum bekommen sollen
                         */
                        if (!field.getDocstruct().equals("firstchild") && field.getDocstruct().contains("firstchild")) {
                            try {
                                myTempChild = this.myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                            } catch (RuntimeException e) {
                            }
                        }
                        if (field.getDocstruct().equals("boundbook")) {
                            myTempStruct = this.myRdf.getDigitalDocument().getPhysicalDocStruct();
                        }
                        /* welches Metadatum */
                        try {
                            /*
                             * bis auf die Autoren alle additionals in die Metadaten übernehmen
                             */
                            if (!field.getMetadata().equals("ListOfCreators")) {
                                MetadataType mdt =
                                        this.ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
                                Metadata md = this.ughHelp.getMetadata(myTempStruct, mdt);
                                /*
                                 * wenn das Metadatum null ist, dann jetzt initialisieren
                                 */
                                if (md == null) {
                                    md = new Metadata(mdt);
                                    md.setParent(myTempStruct);
                                    myTempStruct.addMetadata(md);
                                }
                                md.setValue(field.getWert());
                                /*
                                 * wenn dem Topstruct und dem Firstchild der Wert gegeben werden soll
                                 */
                                if (myTempChild != null) {
                                    md = this.ughHelp.getMetadata(myTempChild, mdt);

                                    md.setValue(field.getWert());
                                }
                            }
                        } catch (NullPointerException e) {
                        } catch (UghHelperException e) {

                        } catch (MetadataTypeNotAllowedException e) {

                        }
                    } // end if ughbinding
                } // end for

                /*
                 * -------------------------------- Collectionen hinzufügen --------------------------------
                 */
                DocStruct colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
                try {
                    addCollections(colStruct);
                    /*
                     * falls ein erstes Kind vorhanden ist, sind die Collectionen dafür
                     */
                    colStruct = colStruct.getAllChildren().get(0);
                    addCollections(colStruct);
                } catch (RuntimeException e) {
                    /*
                     * das Firstchild unterhalb des Topstructs konnte nicht ermittelt werden
                     */
                }

                /*
                 * -------------------------------- Imagepfad hinzufügen (evtl. vorhandene zunächst löschen) --------------------------------
                 */
                try {
                    DigitalDocument dd = this.myRdf.getDigitalDocument();
                    DocStructType dst = this.prozessVorlage.getRegelsatz().getPreferences().getDocStrctTypeByName("BoundBook");
                    DocStruct dsBoundBook = dd.createDocStruct(dst);
                    dd.setPhysicalDocStruct(dsBoundBook);

                    UghHelper ughhelp = new UghHelper();
                    MetadataType mdt = ughhelp.getMetadataType(this.prozessKopie, "pathimagefiles");

                    if (this.myRdf != null && this.myRdf.getDigitalDocument() != null
                            && this.myRdf.getDigitalDocument().getPhysicalDocStruct() != null) {
                        List<? extends Metadata> alleImagepfade = this.myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                        if (alleImagepfade != null && alleImagepfade.size() > 0) {
                            for (Metadata md : alleImagepfade) {
                                this.myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                            }
                        }
                        Metadata newmd = new Metadata(mdt);
                        newmd.setValue(prozessKopie.getImagesTifDirectory(false));
                        this.myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);
                    }
                    /* Rdf-File schreiben */
                    this.prozessKopie.writeMetadataFile(this.myRdf);

                    /*
                     * -------------------------------- soll der Prozess als Vorlage verwendet werden? --------------------------------
                     */
                    if (this.useTemplates && this.prozessKopie.isInAuswahllisteAnzeigen()) {
                        this.prozessKopie.writeMetadataAsTemplateFile(this.myRdf);
                    }

                } catch (ugh.exceptions.DocStructHasNoTypeException e) {
                    Helper.setFehlerMeldung("DocStructHasNoTypeException", e.getMessage());
                    log.error("creation of new process throws an error: ", e);
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("UghHelperException", e.getMessage());
                    log.error("creation of new process throws an error: ", e);
                } catch (MetadataTypeNotAllowedException e) {
                    Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());
                    log.error("creation of new process throws an error: ", e);
                } catch (TypeNotAllowedForParentException e) {
                    log.error(e);
                }
            }
        } else {
            this.prozessKopie.writeMetadataFile(this.myRdf);

        }

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProzess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                ProcessManager.saveProcess(this.prozessKopie);
            } catch (DAOException e) {
                e.printStackTrace();
                log.error("error on save: ", e);
                return this.prozessKopie;
            }
        }

        this.prozessKopie.readMetadataFile();

        return this.prozessKopie;

    }

    public Process createProcess(ImportObject io)
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException {

        this.prozessKopie.setId(null);
        EigenschaftenHinzufuegen(io);

        for (Step step : this.prozessKopie.getSchritteList()) {
            /*
             * -------------------------------- always save date and user for each step --------------------------------
             */
            step.setBearbeitungszeitpunkt(this.prozessKopie.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);
            User user = Helper.getCurrentUser();
            if (user != null) {
                step.setBearbeitungsbenutzer(user);
            }

            /*
             * -------------------------------- only if its done, set edit start and end date --------------------------------
             */
            if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {
                step.setBearbeitungsbeginn(this.prozessKopie.getErstellungsdatum());
                // this concerns steps, which are set as done right on creation
                // bearbeitungsbeginn is set to creation timestamp of process
                // because the creation of it is basically begin of work
                Date myDate = new Date();
                step.setBearbeitungszeitpunkt(myDate);
                step.setBearbeitungsende(myDate);
            }

        }
        if (io.getBatch() != null) {
            this.prozessKopie.setBatch(io.getBatch());
        }
        try {
            //			ProzessDAO dao = new ProzessDAO();
            ProcessManager.saveProcess(this.prozessKopie);
            //			dao.refresh(this.prozessKopie);
        } catch (DAOException e) {
            e.printStackTrace();
            log.error("error on save: ", e);
            return this.prozessKopie;
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            createNewFileformat();
        }

        Path f = Paths.get(this.prozessKopie.getProcessDataDirectoryIgnoreSwapping());
        if (!StorageProvider.getInstance().isFileExists(f)) {
            StorageProvider.getInstance().createDirectories(f);
        }
        //            Helper.setFehlerMeldung("Could not create process directory");
        //            log.error("Could not create process directory");
        //            return this.prozessKopie;
        //        }

        this.prozessKopie.writeMetadataFile(this.myRdf);

        // }

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProzess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                ProcessManager.saveProcess(this.prozessKopie);
            } catch (DAOException e) {
                e.printStackTrace();
                log.error("error on save: ", e);
                return this.prozessKopie;
            }
        }

        this.prozessKopie.readMetadataFile();

        /* damit die Sortierung stimmt nochmal einlesen */
        return this.prozessKopie;

    }

    /* =============================================================== */

    private void addCollections(DocStruct colStruct) {
        for (String s : this.digitalCollections) {
            try {
                Metadata md = new Metadata(this.ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection"));
                md.setValue(s);
                md.setParent(colStruct);
                colStruct.addMetadata(md);
            } catch (UghHelperException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
                e.printStackTrace();
            } catch (DocStructHasNoTypeException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
                e.printStackTrace();
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
                e.printStackTrace();
            }
        }
    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
     */
    private void removeCollections(DocStruct colStruct) {
        try {
            MetadataType mdt = this.ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection");
            ArrayList<Metadata> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt));
            if (myCollections != null && myCollections.size() > 0) {
                for (Metadata md : myCollections) {
                    colStruct.removeMetadata(md, true);
                }
            }
        } catch (UghHelperException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            e.printStackTrace();
        } catch (DocStructHasNoTypeException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            e.printStackTrace();
        }
    }

    /* =============================================================== */

    private void createNewFileformat() {

        Fileformat ff;
        try {
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            ff = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            ff.read(this.metadataFile);
        } catch (ReadException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        }

    }

    private void EigenschaftenHinzufuegen(ImportObject io) {
        /*
         * -------------------------------- Vorlageneigenschaften initialisieren --------------------------------
         */

        Template vor;
        if (this.prozessKopie.getVorlagenSize() > 0) {
            vor = this.prozessKopie.getVorlagenList().get(0);
        } else {
            vor = new Template();
            vor.setProzess(this.prozessKopie);
            List<Template> vorlagen = new ArrayList<>();
            vorlagen.add(vor);
            this.prozessKopie.setVorlagen(vorlagen);
        }

        /*
         * -------------------------------- Werkstückeigenschaften initialisieren --------------------------------
         */
        Masterpiece werk;
        if (this.prozessKopie.getWerkstueckeSize() > 0) {
            werk = this.prozessKopie.getWerkstueckeList().get(0);
        } else {
            werk = new Masterpiece();
            werk.setProzess(this.prozessKopie);
            List<Masterpiece> werkstuecke = new ArrayList<>();
            werkstuecke.add(werk);
            this.prozessKopie.setWerkstuecke(werkstuecke);
        }

        /*
         * -------------------------------- jetzt alle zusätzlichen Felder durchlaufen und die Werte hinzufügen --------------------------------
         */
        BeanHelper bh = new BeanHelper();
        if (io == null) {

            for (AdditionalField field : this.additionalFields) {
                if (field.getShowDependingOnDoctype(getDocType())) {
                    if (field.getFrom().equals("werk")) {
                        bh.EigenschaftHinzufuegen(werk, field.getTitel(), field.getWert());
                    }
                    if (field.getFrom().equals("vorlage")) {
                        bh.EigenschaftHinzufuegen(vor, field.getTitel(), field.getWert());
                    }
                    if (field.getFrom().equals("prozess")) {
                        bh.EigenschaftHinzufuegen(this.prozessKopie, field.getTitel(), field.getWert());
                    }
                }
            }
            /* Doctype */
            bh.EigenschaftHinzufuegen(werk, "DocType", this.docType);
            /* Tiffheader */
            bh.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", this.tifHeader_imagedescription);
            bh.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", this.tifHeader_documentname);
        } else {
            bh.EigenschaftHinzufuegen(werk, "DocType", this.docType);
            /* Tiffheader */
            bh.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", this.tifHeader_imagedescription);
            bh.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", this.tifHeader_documentname);

            for (Processproperty pe : io.getProcessProperties()) {
                addProperty(this.prozessKopie, pe);
            }
            for (Masterpieceproperty we : io.getWorkProperties()) {
                addProperty(werk, we);
            }

            for (Templateproperty ve : io.getTemplateProperties()) {
                addProperty(vor, ve);
            }
            bh.EigenschaftHinzufuegen(prozessKopie, "Template", prozessVorlage.getTitel());
            bh.EigenschaftHinzufuegen(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
        }
    }

    public Collection<SelectItem> getArtists() {
        ArrayList<SelectItem> artisten = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(ConfigurationHelper.getInstance().getTiffHeaderArtists(), "|");
        boolean tempBol = true;
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            if (tempBol) {
                artisten.add(new SelectItem(tok));
            }
            tempBol = !tempBol;
        }
        return artisten;
    }

    /*
     * this is needed for GUI, render multiple select only if this is false if this is true use the only choice
     * 
     * @author Wulf
     */

    public boolean isSingleChoiceCollection() {
        return (getPossibleDigitalCollections() != null && getPossibleDigitalCollections().size() == 1);

    }

    /*
     * this is needed for GUI, render multiple select only if this is false if isSingleChoiceCollection is true use this choice
     * 
     * @author Wulf
     */

    public String getDigitalCollectionIfSingleChoice() {
        List<String> pdc = getPossibleDigitalCollections();
        if (pdc.size() == 1) {
            return pdc.get(0);
        } else {
            return null;
        }
    }

    public List<String> getPossibleDigitalCollections() {
        return this.possibleDigitalCollection;
    }

    private void initializePossibleDigitalCollections() {
        this.possibleDigitalCollection = new ArrayList<>();
        ArrayList<String> defaultCollections = new ArrayList<>();
        String filename = new Helper().getGoobiConfigDirectory() + "goobi_digitalCollections.xml";
        if (!StorageProvider.getInstance().isFileExists(Paths.get(filename))) {
            Helper.setFehlerMeldung("File not found: ", filename);
            return;
        }
        this.digitalCollections = new ArrayList<>();
        try {
            /* Datei einlesen und Root ermitteln */
            SAXBuilder builder = XmlTools.getSAXBuilder();
            Document doc = builder.build(filename);
            Element root = doc.getRootElement();
            /* alle Projekte durchlaufen */
            List<Element> projekte = root.getChildren();
            for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
                Element projekt = iter.next();

                // collect default collections
                if (projekt.getName().equals("default")) {
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                        Element col = it2.next();

                        if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                            digitalCollections.add(col.getText());
                        }

                        defaultCollections.add(col.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = projekt.getChildren("name");
                    for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
                        Element projektname = iterator.next();
                        // all all collections to list
                        if (projektname.getText().equalsIgnoreCase(this.prozessKopie.getProjekt().getTitel())) {
                            List<Element> myCols = projekt.getChildren("DigitalCollection");
                            for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
                                Element col = it2.next();

                                if (col.getAttribute("default") != null && col.getAttributeValue("default").equalsIgnoreCase("true")) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollection.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException e1) {
            log.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        } catch (IOException e1) {
            log.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollection.size() == 0) {
            this.possibleDigitalCollection = defaultCollections;
        }

        // if only one collection is possible take it directly

        if (isSingleChoiceCollection()) {
            this.digitalCollections.add(getDigitalCollectionIfSingleChoice());
        }
    }

    /*
     * ##################################################### ##################################################### ## ## Helper ##
     * ##################################################### ####################################################
     */

    /**
     * Prozesstitel und andere Details generieren ================================================================
     */

    @SuppressWarnings("rawtypes")
    public void CalcProzesstitel() {
        String newTitle = "";
        String titeldefinition = "";
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }
        List<HierarchicalConfiguration> processTitleList = cp.getList("createNewProcess/itemlist/processtitle");

        for (HierarchicalConfiguration hc : processTitleList) {
            String titel = hc.getString(".");
            String isdoctype = hc.getString("@isdoctype");
            String isnotdoctype = hc.getString("@isnotdoctype");

            if (titel == null) {
                titel = "";
            }
            if (isdoctype == null) {
                isdoctype = "";
            }
            if (isnotdoctype == null) {
                isnotdoctype = "";
            }

            /* wenn nix angegeben wurde, dann anzeigen */
            if (isdoctype.equals("") && isnotdoctype.equals("")) {
                titeldefinition = titel;
                break;
            }

            /* wenn beides angegeben wurde */
            if (!isdoctype.equals("") && !isnotdoctype.equals("") && StringUtils.containsIgnoreCase(isdoctype, this.docType)
                    && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }

            /* wenn nur pflicht angegeben wurde */
            if (isnotdoctype.equals("") && StringUtils.containsIgnoreCase(isdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
            /* wenn nur "darf nicht" angegeben wurde */
            if (isdoctype.equals("") && !StringUtils.containsIgnoreCase(isnotdoctype, this.docType)) {
                titeldefinition = titel;
                break;
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(titeldefinition, "+");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'")) {
                newTitle += myString.substring(1, myString.length() - 1);
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (Iterator it2 = this.additionalFields.iterator(); it2.hasNext();) {
                    AdditionalField myField = (AdditionalField) it2.next();

                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || myField.getWert().equals(""))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType()) && myField.getWert() != null) {
                        newTitle += CalcProzesstitelCheck(myField.getTitel(), myField.getWert());
                    }
                }
            }
        }

        if (newTitle.endsWith("_")) {
            newTitle = newTitle.substring(0, newTitle.length() - 1);
        }
        this.prozessKopie.setTitel(newTitle);
        CalcTiffheader();
    }

    /* =============================================================== */

    private String CalcProzesstitelCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        /*
         * -------------------------------- Bandnummer --------------------------------
         */
        if (inFeldName.equals("Bandnummer")) {
            try {
                int bandint = Integer.parseInt(inFeldWert);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                rueckgabe = df.format(bandint);
            } catch (NumberFormatException e) {
                Helper.setFehlerMeldung("Ungültige Daten: ", "Bandnummer ist keine gültige Zahl");
            }
            if (rueckgabe != null && rueckgabe.length() < 4) {
                rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
            }
        }

        return rueckgabe;
    }

    /* =============================================================== */

    public void CalcTiffheader() {
        String tif_definition = "";
        ConfigProjects cp = null;
        try {
            cp = new ConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        } catch (IOException e) {
            Helper.setFehlerMeldung("IOException", e.getMessage());
            return;
        }

        tif_definition = cp.getParamString("tifheader/" + this.docType.toLowerCase(), "blabla");

        /*
         * -------------------------------- evtuelle Ersetzungen --------------------------------
         */
        tif_definition = tif_definition.replaceAll("\\[\\[", "<");
        tif_definition = tif_definition.replaceAll("\\]\\]", ">");

        /*
         * -------------------------------- Documentname ist im allgemeinen = Prozesstitel --------------------------------
         */
        this.tifHeader_documentname = this.prozessKopie.getTitel();
        this.tifHeader_imagedescription = "";
        /*
         * -------------------------------- Imagedescription --------------------------------
         */
        StringTokenizer tokenizer = new StringTokenizer(tif_definition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfaengt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2) {
                this.tifHeader_imagedescription += myString.substring(1, myString.length() - 1);
            } else if (myString.equals("$Doctype")) {

                this.tifHeader_imagedescription += this.docType;
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (Iterator<AdditionalField> it2 = this.additionalFields.iterator(); it2.hasNext();) {
                    AdditionalField myField = it2.next();

                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if ((myField.getTitel().equals("ATS") || myField.getTitel().equals("TSL")) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || myField.getWert().equals(""))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType()) && myField.getWert() != null) {
                        this.tifHeader_imagedescription += CalcProzesstitelCheck(myField.getTitel(), myField.getWert());
                    }
                }
            }
            // }
        }
    }

    private void addProperty(Template inVorlage, Templateproperty property) {
        if (property.getContainer() == 0) {
            for (Templateproperty ve : inVorlage.getEigenschaftenList()) {
                if (ve.getTitel().equals(property.getTitel()) && ve.getContainer() > 0) {
                    ve.setWert(property.getWert());
                    return;
                }
            }
        }
        Templateproperty eig = new Templateproperty();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setVorlage(inVorlage);
        List<Templateproperty> eigenschaften = inVorlage.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }

    private void addProperty(Process inProcess, Processproperty property) {
        if (property.getContainer() == 0) {
            for (Processproperty pe : inProcess.getEigenschaftenList()) {
                if (pe.getTitel().equals(property.getTitel()) && pe.getContainer() > 0) {
                    pe.setWert(property.getWert());
                    return;
                }
            }
        }
        Processproperty eig = new Processproperty();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setProzess(inProcess);
        List<Processproperty> eigenschaften = inProcess.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }

    private void addProperty(Masterpiece inWerk, Masterpieceproperty property) {
        if (property.getContainer() == 0) {
            for (Masterpieceproperty we : inWerk.getEigenschaftenList()) {
                if (we.getTitel().equals(property.getTitel()) && we.getContainer() > 0) {
                    we.setWert(property.getWert());
                    return;
                }
            }
        }
        Masterpieceproperty eig = new Masterpieceproperty();
        eig.setTitel(property.getTitel());
        eig.setWert(property.getWert());
        eig.setAuswahl(property.getAuswahl());
        eig.setContainer(property.getContainer());
        eig.setType(property.getType());
        eig.setWerkstueck(inWerk);
        List<Masterpieceproperty> eigenschaften = inWerk.getEigenschaften();
        if (eigenschaften == null) {
            eigenschaften = new ArrayList<>();
        }
        eigenschaften.add(eig);
    }
}
