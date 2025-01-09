package org.goobi.production.cli.helper;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.NamingException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
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
import de.sub.goobi.forms.ProzesskopieForm;
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
import jakarta.faces.model.SelectItem;
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

    public static final String FIELD_FIRSTCHILD = "firstchild";
    public static final String FIELD_BOUNDBOOK = "boundbook";
    public static final String FIELD_LIST_OF_CREATORS = "ListOfCreators";
    public static final String FIELD_SINGLE_DIG_COLLECTION = "singleDigCollection";

    public static final String DIRECTORY_SUFFIX = "_tif";

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

    /* =============================================================== */

    public String prepare(ImportObject io) {
        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            return "";
        }

        clearValues();

        this.co = ConfigOpac.getInstance();

        try {
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            this.myRdf = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            this.myRdf.read(this.metadataFile);
        } catch (ReadException | IOException e) {
            log.error(e);
        }

        this.prozessKopie = new Process();
        this.prozessKopie.setTitel("");
        this.prozessKopie.setIstTemplate(false);
        this.prozessKopie.setInAuswahllisteAnzeigen(false);
        this.prozessKopie.setProjekt(this.prozessVorlage.getProjekt());
        this.prozessKopie.setRegelsatz(this.prozessVorlage.getRegelsatz());
        this.prozessKopie.setDocket(this.prozessVorlage.getDocket());
        this.prozessKopie.setExportValidator(this.prozessVorlage.getExportValidator());
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

    public String prepare() {
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
        } catch (ReadException | IOException e) {
            log.error(e);
        }

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
        ConfigProjects cp = ProzesskopieForm.initializeConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        if (cp == null) {
            return;
        }

        this.docType = cp.getParamString("createNewProcess/defaultdoctype", this.co.getAllDoctypes().get(0).getTitle());
        this.useOpac = cp.getParamBoolean("createNewProcess/opac/@use");
        this.useTemplates = cp.getParamBoolean("createNewProcess/templates/@use");
        this.naviFirstPage = "ProzessverwaltungKopie1";
        if ("".equals(this.opacKatalog)) {
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

    public String opacAuswerten(ImportObject io) {
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
            log.error(e);
        }
        return "";
    }

    /**
     * OpacAnfrage
     */

    public String opacAuswerten() {
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
            log.error(e);
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
            UghHelper ughHelper = new UghHelper();

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                    /* welches Docstruct */

                    DocStruct myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct();
                    if (FIELD_FIRSTCHILD.equals(field.getDocstruct())) {
                        try {
                            myTempStruct = myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException exception) {
                            // do nothing
                        }
                    }
                    if (FIELD_BOUNDBOOK.equals(field.getDocstruct())) {
                        myTempStruct = myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if (FIELD_LIST_OF_CREATORS.equals(field.getMetadata())) {
                            /* bei Autoren die Namen zusammenstellen */
                            StringBuilder authorBuilder = new StringBuilder();
                            if (myTempStruct.getAllPersons() != null) {
                                for (Person p : myTempStruct.getAllPersons()) {
                                    authorBuilder.append(p.getLastname());
                                    if (StringUtils.isNotBlank(p.getFirstname())) {
                                        authorBuilder.append(", ").append(p.getFirstname());
                                    }
                                    authorBuilder.append("; ");
                                }
                                if (authorBuilder.toString().endsWith("; ")) {
                                    authorBuilder.delete(authorBuilder.length() - 2, authorBuilder.length());
                                }
                            }
                            field.setWert(authorBuilder.toString());
                        } else {
                            /* bei normalen Feldern die Inhalte auswerten */
                            MetadataType mdt = ughHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
                            Metadata md = ughHelper.getMetadata(myTempStruct, mdt);
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
            boolean listNotEmpty = field.getSelectList() != null && !field.getSelectList().isEmpty();
            if (!field.isUghbinding() && field.getShowDependingOnDoctype(getDocType()) && listNotEmpty) {
                field.setWert((String) field.getSelectList().get(0).getValue());

            }
        }
        calcTiffheader();

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

    public String readMetadataFromTemplate() throws DAOException {
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
            String message = "Fehler beim Anlegen des Vorgangs";
            Helper.setFehlerMeldung(message, e);
            log.error(message, e);
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
        String incompleteData = Helper.getTranslation("UnvollstaendigeDaten");
        /*
         * -------------------------------- Vorbedingungen prüfen --------------------------------
         */
        boolean valide = true;

        /*
         * -------------------------------- grundsätzlich den Vorgangstitel prüfen --------------------------------
         */
        /* kein Titel */
        if (this.prozessKopie.getTitel() == null || "".equals(this.prozessKopie.getTitel())) {
            valide = false;
            Helper.setFehlerMeldung(incompleteData + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
        }

        String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
        if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
            valide = false;
            Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
        }

        /* prüfen, ob der Prozesstitel schon verwendet wurde */
        if (this.prozessKopie.getTitel() != null) {
            long anzahl = 0;
            anzahl = ProcessManager.countProcessTitle(this.prozessKopie.getTitel(), prozessKopie.getProjekt().getInstitution());

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
            Helper.setFehlerMeldung(incompleteData + " " + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        /*
         * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
         */
        for (AdditionalField field : this.additionalFields) {
            if (field.getSelectList() == null && field.isRequired() && field.getShowDependingOnDoctype(getDocType())
                    && (StringUtils.isBlank(field.getWert()))) {
                valide = false;
                Helper.setFehlerMeldung(incompleteData + " " + field.getTitel() + " " + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }
        }
        return valide;
    }

    /* =============================================================== */

    public String openFirstPage() {
        return this.naviFirstPage;
    }

    /* =============================================================== */

    public String openPage2() {
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
            if (this.prozessKopie.getTitel() == null || "".equals(this.prozessKopie.getTitel())) {
                valide = false;
                Helper.setFehlerMeldung(
                        Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty"));
            }

            String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
            if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
                valide = false;
                Helper.setFehlerMeldung("UngueltigerTitelFuerVorgang");
            }

            /* prüfen, ob der Prozesstitel schon verwendet wurde */
            if (this.prozessKopie.getTitel() != null) {
                long anzahl = 0;
                anzahl = ProcessManager.countProcessTitle(this.prozessKopie.getTitel(), prozessKopie.getProjekt().getInstitution());

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

    public Process createNewProcess2()
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
        } catch (DAOException e) {
            log.error("Error while creating and saving process without RDF file: ", e);
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
                        if (FIELD_FIRSTCHILD.equals(field.getDocstruct())) {
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
                        if (!FIELD_FIRSTCHILD.equals(field.getDocstruct()) && field.getDocstruct().contains(FIELD_FIRSTCHILD)) {
                            try {
                                myTempChild = this.myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                            } catch (RuntimeException exception) {
                                log.error(exception);
                            }
                        }
                        if (FIELD_BOUNDBOOK.equals(field.getDocstruct())) {
                            myTempStruct = this.myRdf.getDigitalDocument().getPhysicalDocStruct();
                        }
                        /* welches Metadatum */
                        try {
                            /*
                             * bis auf die Autoren alle additionals in die Metadaten übernehmen
                             */
                            if (!FIELD_LIST_OF_CREATORS.equals(field.getMetadata())) {
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
                        } catch (NullPointerException | UghHelperException | MetadataTypeNotAllowedException exception) {
                            log.error(exception);
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
                        if (alleImagepfade != null && !alleImagepfade.isEmpty()) {
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
                    log.error("Process could not be created because docstruct has no type: ", e);
                } catch (UghHelperException e) {
                    Helper.setFehlerMeldung("UghHelperException", e.getMessage());
                    log.error("Process could not be created due to UGH exception: ", e);
                } catch (MetadataTypeNotAllowedException e) {
                    Helper.setFehlerMeldung("MetadataTypeNotAllowedException", e.getMessage());
                    log.error("Process could not be created because metadata type is not allowed: ", e);
                } catch (TypeNotAllowedForParentException e) {
                    Helper.setFehlerMeldung("TypeNotAllowedForParentException", e.getMessage());
                    log.error("Process could not be created because parent type is not allowed: ", e);
                }
            }
        } else {
            this.prozessKopie.writeMetadataFile(this.myRdf);

        }

        // Adding process to history
        if (Boolean.FALSE.equals(HistoryAnalyserJob.updateHistoryForProzess(this.prozessKopie))) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                ProcessManager.saveProcess(this.prozessKopie);
            } catch (DAOException e) {
                log.error("Error while creating and saving process with RDF file: ", e);
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
            ProcessManager.saveProcess(this.prozessKopie);
        } catch (DAOException e) {
            log.error("Error while creating process without RDF file: ", e);
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

        this.prozessKopie.writeMetadataFile(this.myRdf);

        // Adding process to history
        if (Boolean.FALSE.equals(HistoryAnalyserJob.updateHistoryForProzess(this.prozessKopie))) {
            Helper.setFehlerMeldung("historyNotUpdated");
        } else {
            try {
                ProcessManager.saveProcess(this.prozessKopie);
            } catch (DAOException e) {
                log.error("Error while creating process with RDF file: ", e);
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
                Metadata md =
                        new Metadata(this.ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), FIELD_SINGLE_DIG_COLLECTION));
                md.setValue(s);
                md.setParent(colStruct);
                colStruct.addMetadata(md);
            } catch (UghHelperException | DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
                log.error(e);
            }
        }
    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
     */
    private void removeCollections(DocStruct colStruct) {
        try {
            MetadataType mdt = this.ughHelp.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), FIELD_SINGLE_DIG_COLLECTION);
            ArrayList<Metadata> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt)); // implies that myCollections != null
            if (!myCollections.isEmpty()) {
                for (Metadata md : myCollections) {
                    colStruct.removeMetadata(md, true);
                }
            }
        } catch (UghHelperException | DocStructHasNoTypeException e) {
            Helper.setFehlerMeldung(e.getMessage(), "");
            log.error(e);
        }
    }

    /* =============================================================== */

    private void createNewFileformat() {

        Fileformat ff;
        try {
            String type = MetadatenHelper.getMetaFileType(metadataFile);
            ff = MetadatenHelper.getFileformatByName(type, prozessVorlage.getRegelsatz());
            ff.read(this.metadataFile);
        } catch (ReadException | IOException e) {
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
                    if ("werk".equals(field.getFrom())) {
                        bh.EigenschaftHinzufuegen(werk, field.getTitel(), field.getWert());
                    }
                    if ("vorlage".equals(field.getFrom())) {
                        bh.EigenschaftHinzufuegen(vor, field.getTitel(), field.getWert());
                    }
                    if ("prozess".equals(field.getFrom())) {
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
            String defaultProjectName = "default";
            List<Element> projekte = root.getChildren();
            for (Element projekt : projekte) {
                // collect default collections
                if (defaultProjectName.equals(projekt.getName())) {
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Element col : myCols) {
                        if (col.getAttribute(defaultProjectName) != null && "true".equalsIgnoreCase(col.getAttributeValue(defaultProjectName))) {
                            digitalCollections.add(col.getText());
                        }

                        defaultCollections.add(col.getText());
                    }
                } else {
                    // run through the projects
                    List<Element> projektnamen = projekt.getChildren("name");
                    for (Element projektname : projektnamen) {
                        // all all collections to list
                        if (projektname.getText().equalsIgnoreCase(this.prozessKopie.getProjekt().getTitel())) {
                            List<Element> myCols = projekt.getChildren("DigitalCollection");
                            for (Element col : myCols) {
                                if (col.getAttribute(defaultProjectName) != null
                                        && "true".equalsIgnoreCase(col.getAttributeValue(defaultProjectName))) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollection.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e1) {
            String message = "error while parsing digital collections";
            log.error(message, e1);
            Helper.setFehlerMeldung(message, e1);
        }

        if (this.possibleDigitalCollection.isEmpty()) {
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

    public void calculateProcessTitle() {
        StringBuilder newTitleBuilder = new StringBuilder();
        String titeldefinition = "";
        ConfigProjects cp = ProzesskopieForm.initializeConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        if (cp == null) {
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

            boolean containsDoctype = StringUtils.containsIgnoreCase(isdoctype, this.docType);
            boolean containsNotDoctype = StringUtils.containsIgnoreCase(isnotdoctype, this.docType);

            /* wenn nix angegeben wurde, dann anzeigen */
            boolean useTitle = "".equals(isdoctype) && "".equals(isnotdoctype);

            /* wenn beides angegeben wurde */
            useTitle = useTitle || (!"".equals(isdoctype) && !"".equals(isnotdoctype) && containsDoctype && !containsNotDoctype);

            /* wenn nur pflicht angegeben wurde */
            useTitle = useTitle || ("".equals(isnotdoctype) && containsDoctype);

            /* wenn nur "darf nicht" angegeben wurde */
            useTitle = useTitle || ("".equals(isdoctype) && !containsNotDoctype);

            if (useTitle) {
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
                newTitleBuilder.append(myString.substring(1, myString.length() - 1));
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField myField : this.additionalFields) {

                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if (("ATS".equals(myField.getTitel()) || "TSL".equals(myField.getTitel())) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || "".equals(myField.getWert()))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType()) && myField.getWert() != null) {
                        newTitleBuilder.append(CalcProzesstitelCheck(myField.getTitel(), myField.getWert()));
                    }
                }
            }
        }
        String newTitle = newTitleBuilder.toString();
        if (newTitle.endsWith("_")) {
            newTitle = newTitle.substring(0, newTitle.length() - 1);
        }
        this.prozessKopie.setTitel(newTitle);
        calcTiffheader();
    }

    /* =============================================================== */

    private String CalcProzesstitelCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        /*
         * -------------------------------- Bandnummer --------------------------------
         */
        if ("Bandnummer".equals(inFeldName)) {
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

    public void calcTiffheader() {
        String tifDefinition = "";
        ConfigProjects cp = ProzesskopieForm.initializeConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        if (cp == null) {
            return;
        }

        tifDefinition = cp.getParamString("tifheader/" + this.docType.toLowerCase(), "blabla");

        /*
         * -------------------------------- evtuelle Ersetzungen --------------------------------
         */

        tifDefinition = tifDefinition.replace("[[", "<");
        tifDefinition = tifDefinition.replace("]]", ">");

        /*
         * -------------------------------- Documentname ist im allgemeinen = Prozesstitel --------------------------------
         */
        this.tifHeader_documentname = this.prozessKopie.getTitel();
        StringBuilder imageDescriptionBuilder = new StringBuilder(); // will be used to build this.tifHeader_imagedescription
        /*
         * -------------------------------- Imagedescription --------------------------------
         */
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfaengt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2) {
                imageDescriptionBuilder.append(myString.substring(1, myString.length() - 1));
            } else if ("$Doctype".equals(myString)) {

                imageDescriptionBuilder.append(this.docType);
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField myField : this.additionalFields) {
                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if (("ATS".equals(myField.getTitel()) || "TSL".equals(myField.getTitel())) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || "".equals(myField.getWert()))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType()) && myField.getWert() != null) {
                        imageDescriptionBuilder.append(CalcProzesstitelCheck(myField.getTitel(), myField.getWert()));
                    }
                }
            }
        }
        this.tifHeader_imagedescription = imageDescriptionBuilder.toString();
    }

    private void addProperty(Template inVorlage, Templateproperty property) {
        if ("0".equals(property.getContainer())) {
            for (Templateproperty ve : inVorlage.getEigenschaftenList()) {
                if (ve.getTitel().equals(property.getTitel()) && !"0".equals(ve.getContainer())) {
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
        if ("0".equals(property.getContainer())) {
            for (Processproperty pe : inProcess.getEigenschaftenList()) {
                if (pe.getTitel().equals(property.getTitel()) && !"0".equals(property.getContainer())) {
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
        if ("0".equals(property.getContainer())) {
            for (Masterpieceproperty we : inWerk.getEigenschaftenList()) {
                if (we.getTitel().equals(property.getTitel()) && !"0".equals(we.getContainer())) {
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
