package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.naming.NamingException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
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
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.UserRole;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.goobi.production.plugin.interfaces.IOpacPluginVersion2;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.Type;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import de.schlichtherle.io.FileOutputStream;
import de.sub.goobi.config.ConfigProjects;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ProcessTitleGenerator;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.UghHelper;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.enums.ManipulationType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.TempImage;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import jakarta.enterprise.inject.Default;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
import ugh.dl.Prefs;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.exceptions.WriteException;
import ugh.fileformats.mets.XStream;

@Named("ProzesskopieForm")
@WindowScoped
@Default
@Log4j2
public class ProzesskopieForm implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 2579641488883675182L;
    private Helper help = new Helper();
    private transient UghHelper ughHelper = new UghHelper();
    private transient BeanHelper bHelper = new BeanHelper();
    private transient Fileformat myRdf;
    @Getter
    @Setter
    private String opacSuchfeld = "12";
    @Getter
    @Setter
    private String opacSuchbegriff;
    @Getter
    private String opacKatalog;
    @Getter
    @Setter
    private Process prozessVorlage = new Process();
    @Getter
    @Setter
    private Process prozessKopie = new Process();

    private transient ConfigOpac co;
    /* komplexe Anlage von Vorgängen anhand der xml-Konfiguration */
    @Getter
    private boolean useOpac;
    @Getter
    private boolean useTemplates;

    @Getter
    private HashMap<String, Boolean> standardFields;
    @Getter
    @Setter
    private transient List<AdditionalField> additionalFields;
    @Getter
    @Setter
    private List<String> digitalCollections;
    @Getter
    @Setter
    private String tifHeaderImagedescription = "";
    @Getter
    @Setter
    private String tifHeaderDocumentname = "";

    private String naviFirstPage;
    @Getter
    @Setter
    private Integer auswahl;
    @Getter
    private String docType;
    private String atstsl = "";
    private List<String> possibleDigitalCollection;
    private Integer guessedImages = 0;
    @Getter
    @Setter
    private String addToWikiField = "";
    @Getter
    @Setter
    private boolean importantWikiField = false;
    private transient List<ConfigOpacCatalogue> catalogues;
    private List<String> catalogueTitles;
    private transient ConfigOpacCatalogue currentCatalogue;

    public static final String DIRECTORY_SUFFIX = "_tif";

    private transient Path temporaryFolder = null;

    @Getter
    private transient TreeSet<UploadImage> uploadedFiles = new TreeSet<>();

    @Getter
    private String uploadFolder;

    @Getter
    @Setter
    private String uploadRegex;

    @Getter
    @Setter
    private String fileUploadErrorMessage;

    @Getter
    @Setter
    private String fileComment;

    @Getter
    @Setter
    private transient Part uploadedFile = null;

    @Getter
    private List<SelectItem> configuredFolderNames;

    @Getter
    private List<String> configuredFolderRegex;

    @Getter
    private List<String> configuredFolderErrorMessageKeys;

    @Getter
    private boolean enableFileUpload = false;

    private List<Project> availableProjects = new ArrayList<>();

    private List<ProcessProperty> configuredProperties;

    @Getter
    private Process existingProcess;

    @Getter
    private boolean showExistingProcessTables;

    @Getter
    private boolean showExistingProcessButton;

    public String prepare() {

        currentCatalogue = null;
        opacKatalog = "";
        atstsl = "";
        opacSuchbegriff = "";
        this.guessedImages = 0;
        if (ConfigurationHelper.getInstance().isResetJournal()) {
            addToWikiField = "";
        }

        if (this.prozessVorlage.getContainsUnreachableSteps()) {
            if (prozessVorlage.getSchritteList().isEmpty()) {
                Helper.setFehlerMeldung("noStepsInWorkflow");
            }
            for (Step s : this.prozessVorlage.getSchritteList()) {
                if (s.getBenutzergruppenSize() == 0 && s.getBenutzerSize() == 0) {
                    Helper.setFehlerMeldung(Helper.getTranslation("noUserInStep", s.getTitel()));
                }
            }

            return "";
        }
        if (Boolean.TRUE.equals(prozessVorlage.getProjekt().getProjectIsArchived())) {
            Helper.setFehlerMeldung("projectIsArchived");
            return "";
        }

        clearValues();
        this.co = ConfigOpac.getInstance();
        catalogues = co.getAllCatalogues(prozessVorlage.getTitel());

        catalogueTitles = new ArrayList<>(catalogues.size());
        for (ConfigOpacCatalogue coc : catalogues) {
            catalogueTitles.add(coc.getTitle());
        }

        readProjectConfigs();
        this.myRdf = null;
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
         *  Kopie der Processvorlage anlegen
         */
        this.bHelper.SchritteKopieren(this.prozessVorlage, this.prozessKopie);
        this.bHelper.ScanvorlagenKopieren(this.prozessVorlage, this.prozessKopie);
        this.bHelper.WerkstueckeKopieren(this.prozessVorlage, this.prozessKopie);

        configuredProperties = PropertyParser.getInstance().getProcessCreationProperties(prozessKopie, prozessVorlage.getTitel());

        initializePossibleDigitalCollections();

        this.showExistingProcessButton = false;
        this.showExistingProcessTables = false;

        return this.naviFirstPage;
    }

    /**
     * Tries to initialize the config projects object (the project configuration XML file). If the file could be read, the config projects object is
     * returned. Otherwise, the error message is displayed in the GUI and null is returned.
     *
     * @param projectTitle The name of the project of interest
     * @return The config projects object if the file could be read or null in case of an error
     */
    public static ConfigProjects initializeConfigProjects(String projectTitle) {
        try {
            return new ConfigProjects(projectTitle);
        } catch (IOException ioException) {
            Helper.setFehlerMeldung("File could not be found or read (IOException): ", ioException.getMessage());
        } catch (PatternSyntaxException exception) {
            String message = Helper.getTranslation("projectErrorConfigurationHasInvalidTitle");
            Helper.setFehlerMeldung(message + ": " + exception.getPattern());
        }
        return null;
    }

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

        this.naviFirstPage = "process_new1";
        if (useOpac && StringUtils.isBlank(opacKatalog)) {
            setOpacKatalog(cp.getParamString("createNewProcess/opac/catalogue"));
            opacSuchfeld = cp.getParamString("createNewProcess/opac/catalogue/@searchfield", "12");
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
            AdditionalField fa = readAdditionalFieldConfiguration(item);
            this.additionalFields.add(fa);
        }

        // check if file upload is allowed
        enableFileUpload = cp.getParamBoolean("createNewProcess/fileupload/@use");
        configuredFolderNames = new ArrayList<>();
        configuredFolderRegex = new ArrayList<>();
        configuredFolderErrorMessageKeys = new ArrayList<>();
        if (enableFileUpload) {
            List<HierarchicalConfiguration> folderObjects = cp.getList("createNewProcess/fileupload/folder");
            if (folderObjects != null) {
                for (HierarchicalConfiguration folderObject : folderObjects) {

                    readImportFolderConfiguration(folderObject);
                }
            }
            if (configuredFolderNames.isEmpty()) {
                enableFileUpload = false;
            } else {
                uploadFolder = (String) configuredFolderNames.get(0).getValue();
                uploadRegex = configuredFolderRegex.get(0);
            }
        }
    }

    private void readImportFolderConfiguration(HierarchicalConfiguration folderObject) {
        String regex = folderObject.getString("@regex", "/^.*$/");
        configuredFolderRegex.add(regex);

        String key = folderObject.getString("@messageKey", "");
        configuredFolderErrorMessageKeys.add(key);

        String name = folderObject.getString(".");
        switch (name) {
            case "intern": //NOSONAR
                configuredFolderNames.add(new SelectItem("intern", Helper.getTranslation("process_log_file_FolderSelectionInternal")));
                break;
            case "export": //NOSONAR
                configuredFolderNames.add(new SelectItem("export", Helper.getTranslation("process_log_file_FolderSelectionExportToViewer")));
                break;
            case "master":
                if (ConfigurationHelper.getInstance().isUseMasterDirectory()) {
                    configuredFolderNames.add(new SelectItem("master", Helper.getTranslation("process_log_file_masterFolder")));
                }
                break;
            case "media":
                configuredFolderNames.add(new SelectItem("media", Helper.getTranslation("process_log_file_mediaFolder")));
                break;
            default:
                if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getAdditionalProcessFolderName(name))) {
                    configuredFolderNames.add(new SelectItem(name, Helper.getTranslation("process_log_file_" + name + "Folder")));
                }
                break;
        }
    }

    private AdditionalField readAdditionalFieldConfiguration(HierarchicalConfiguration item) {
        AdditionalField fa = new AdditionalField();
        fa.setFrom(item.getString("@from"));
        fa.setTitel(item.getString("."));
        fa.setRequired(item.getBoolean("@required", false));
        fa.setIsdoctype(item.getString("@isdoctype"));
        fa.setIsnotdoctype(item.getString("@isnotdoctype"));
        fa.setInitStart(item.getString("@initStart"));
        fa.setInitEnd(item.getString("@initEnd"));
        fa.setFieldType(item.getString("@type", "text"));
        fa.setPattern(item.getString("@pattern"));

        if (StringUtils.isNotBlank(item.getString("@metadata")) && item.getBoolean("@ughbinding", true)) {
            fa.setUghbinding(true);
            fa.setDocstruct(item.getString("@docstruct", "topstruct"));
            fa.setMetadata(item.getString("@metadata"));
        }
        if (item.getBoolean("@autogenerated", false)) {
            fa.setAutogenerated(true);
        }
        /*
         * -------------------------------- prüfen, ob das aktuelle Item eine Auswahlliste werden soll --------------------------------
         */
        List<HierarchicalConfiguration> parameterList = item.configurationsAt("select");
        /* Children durchlaufen und SelectItems erzeugen */

        fa.setMultiselect(item.getBoolean("@multiselect", false));

        if (parameterList.size() == 1) {
            fa.setWert(parameterList.get(0).getString("."));
            fa.setMultiselect(false);
        }
        if (!parameterList.isEmpty()) {
            fa.setSelectList(parameterList.stream()
                    .map(hc -> new SelectItem(hc.getString("."), hc.getString("@label"), null))
                    .toList());
        }

        String vocabularyTitle = item.getString("@vocabulary");
        Optional<String> filter = Optional.ofNullable(item.getString("@vocabulary-filter"));
        Optional<String> filterQuery = Optional.empty();
        if (StringUtils.isNotBlank(vocabularyTitle)) {
            Vocabulary vocabulary = VocabularyAPIManager.getInstance().vocabularies().findByName(vocabularyTitle);
            VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(vocabulary.getSchemaId());

            if (filter.isPresent() && filter.get().contains("=")) {
                String[] parts = filter.get().split("=");
                String field = parts[0];
                String value = parts[1];

                String finalFieldName = field;
                Optional<FieldDefinition> searchField = schema.getDefinitions()
                        .stream()
                        .filter(d -> d.getName().equals(finalFieldName))
                        .findFirst();

                if (searchField.isEmpty()) {
                    Helper.setFehlerMeldung("Field " + field + " not found in vocabulary " + vocabulary.getName());
                    return fa;
                }

                filterQuery = Optional.of(searchField.get().getId() + ":" + value);
            }

            List<ExtendedVocabularyRecord> records = VocabularyAPIManager.getInstance()
                    .vocabularyRecords()
                    .list(vocabulary.getId())
                    .search(filterQuery)
                    .all()
                    .request()
                    .getContent();
            fa.setSelectList(
                    records.stream()
                            .map(ExtendedVocabularyRecord::getMainValue)
                            .sorted()
                            .map(v -> new SelectItem(v, v))
                            .collect(Collectors.toList()));
        }
        return fa;
    }

    /* =============================================================== */

    public List<SelectItem> getProzessTemplates() {
        List<SelectItem> myProcessTemplates = new ArrayList<>();
        StringBuilder filter = new StringBuilder(" istTemplate = false AND inAuswahllisteAnzeigen = true ");

        /* Einschränkung auf bestimmte Projekte, wenn kein Admin */
        User aktuellerNutzer = Helper.getCurrentUser();

        /*
         * wenn die maximale Berechtigung nicht Admin ist, dann nur bestimmte
         */
        if (aktuellerNutzer != null && !Helper.getLoginBean().hasRole(UserRole.Workflow_General_Show_All_Projects.name())) {

            filter.append(" AND prozesse.ProjekteID in (select ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = ")
                    .append(aktuellerNutzer.getId())
                    .append(")");
        }
        Institution inst = null;
        if (aktuellerNutzer != null && !aktuellerNutzer.isSuperAdmin()) {
            //             limit result to institution of current user
            inst = aktuellerNutzer.getInstitution();
        }

        List<Process> selectList = ProcessManager.getProcesses("prozesse.titel", filter.toString(), inst);
        for (Process proz : selectList) {
            myProcessTemplates.add(new SelectItem(proz.getId(), proz.getTitel(), null));
        }
        return myProcessTemplates;
    }

    /* =============================================================== */

    /**
     * OpacAnfrage
     */
    public String opacAuswerten() {
        clearValues();
        readProjectConfigs();
        try {

            /* den Opac abfragen und ein RDF draus bauen lassen */
            this.myRdf = currentCatalogue.getOpacPlugin()
                    .search(this.opacSuchfeld, this.opacSuchbegriff, currentCatalogue, this.prozessKopie.getRegelsatz().getPreferences());
            if (myRdf == null) {
                Helper.setFehlerMeldung("No hit found", "");
                return "";
            }

            if (currentCatalogue.getOpacPlugin().getOpacDocType() != null) {
                this.docType = currentCatalogue.getOpacPlugin().getOpacDocType().getTitle();
            }
            this.atstsl = currentCatalogue.getOpacPlugin().getAtstsl();
            fillFieldsFromMetadataFile();
            /* über die Treffer informieren */
            if (currentCatalogue.getOpacPlugin().getHitcount() == 0) {
                Helper.setFehlerMeldung("No hit found", "");
            }
            if (currentCatalogue.getOpacPlugin().getHitcount() > 1) {
                Helper.setMeldung(null, "Found more then one hit", " - use first hit");
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on reading opac ", e);
            log.error(e.toString(), e);
        }
        return "";
    }

    /* =============================================================== */

    /**
     * die Eingabefelder für die Eigenschaften mit Inhalten aus der RDF-Datei füllen
     *
     * @throws PreferencesException
     */
    private void fillFieldsFromMetadataFile() throws PreferencesException {
        if (this.myRdf != null) {

            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                    /* welches Docstruct */
                    DocStruct myTempStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
                    if ("child".equals(field.getDocstruct()) || "firstchild".equals(field.getDocstruct())) { //NOSONAR
                        try {
                            myTempStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            // nothing to do here
                        }
                    }
                    if ("boundbook".equals(field.getDocstruct())) {
                        myTempStruct = this.myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        if ("ListOfCreators".equals(field.getMetadata())) { //NOSONAR
                            /* bei Autoren die Namen zusammenstellen */
                            generateAllAuthorsList(field, myTempStruct);
                        } else {
                            /* bei normalen Feldern die Inhalte auswerten */
                            MetadataType mdt = this.ughHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
                            if (field.isMultiselect()) {
                                // multiselect
                                for (String selectedValue : field.getValues()) {
                                    createMetadata(myTempStruct, mdt, selectedValue);
                                }
                            } else {

                                Metadata md = this.ughHelper.getMetadata(myTempStruct, mdt);
                                if (md != null) {
                                    if ((md.getValue() != null && !md.getValue().isEmpty()) || field.getWert() == null || field.getWert().isEmpty()) {
                                        field.setWert(md.getValue());
                                    }
                                    md.setValue(field.getWert().replace("&amp;", "&"));
                                }
                            }
                        }
                    } catch (UghHelperException e) {
                        log.error(e);
                        Helper.setFehlerMeldung(e.getMessage(), "");
                    }
                    if (field.getWert() != null && !"".equals(field.getWert())) {
                        field.setWert(field.getWert().replace("&amp;", "&"));
                    }
                } // end if ughbinding
            } // end for
        } // end if myrdf==null
    }

    private void generateAllAuthorsList(AdditionalField field, DocStruct myTempStruct) {
        String myautoren = "";
        StringBuilder authors = new StringBuilder();
        if (myTempStruct.getAllPersons() != null) {
            for (Person p : myTempStruct.getAllPersons()) {
                if (StringUtils.isNotBlank(p.getLastname())) {
                    authors.append(p.getLastname());
                }
                if (StringUtils.isNotBlank(p.getFirstname())) {
                    authors.append(", ");
                    authors.append(p.getFirstname());
                }
                authors.append("; ");
            }
            myautoren = authors.toString();
            if (myautoren.endsWith("; ")) {
                myautoren = myautoren.substring(0, myautoren.length() - 2);
            }
        }
        field.setWert(myautoren);
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
        this.standardFields.put("preferences", true);
        this.standardFields.put("images", true);
        standardFields.put("fileUpload", true);
        standardFields.put("processtitle", true);

        this.additionalFields = new ArrayList<>();
        this.tifHeaderDocumentname = "";
        this.tifHeaderImagedescription = "";
        clearUploadedData();
    }

    /**
     * Auswahl des Processes auswerten
     *
     * @throws DAOException
     * @throws NamingException
     * @throws SQLException ============================================================== ==
     */
    public String readMetadataFromTemplate() throws DAOException {
        /* den ausgewählten Process laden */
        if (auswahl == null || auswahl == 0) {
            Helper.setFehlerMeldung("ErrorTemplateSelectionIsEmpty");
            return "";
        }
        Process tempProcess = ProcessManager.getProcessById(this.auswahl);

        if (tempProcess.getWerkstueckeSize() > 0) {
            /* erstes Werkstück durchlaufen */
            fillTemplateFromMasterpiece(tempProcess);
        }

        if (tempProcess.getVorlagenSize() > 0) {
            fillTemplateFromTemplate(tempProcess);
        }

        if (tempProcess.getEigenschaftenSize() > 0) {
            fillTemplateFromProperties(tempProcess);
        }

        try {
            this.myRdf = tempProcess.readMetadataAsTemplateFile();

            /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
            DocStruct colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();

            List<Metadata> firstChildMetadata =
                    colStruct.getAllChildren() == null || colStruct.getAllChildren().isEmpty() ? Collections.emptyList()
                            : colStruct.getAllChildren().get(0).getAllMetadata();
            fillTemplateFromMetadata(colStruct.getAllMetadata(), "topstruct");
            fillTemplateFromMetadata(firstChildMetadata, "firstchild");

            removeCollections(colStruct);

            if (colStruct.getAllChildren() != null) {
                colStruct = colStruct.getAllChildren().get(0);
                removeCollections(colStruct);
            }
        } catch (Exception e) {
            Helper.setFehlerMeldung("Error on reading template-metadata", e);
            log.error("Error on reading template-metadata", e);
        }

        return "";
    }

    private void fillTemplateFromProperties(Process tempProcess) {
        for (Processproperty pe : tempProcess.getEigenschaften()) {
            for (AdditionalField field : this.additionalFields) {
                if (field.getTitel().equals(pe.getTitel())) {
                    setFieldValue(field, pe.getWert());
                }
            }
            if ("digitalCollection".equals(pe.getTitel())) {
                digitalCollections.add(pe.getWert());
            }
        }
    }

    private void fillTemplateFromTemplate(Process tempProcess) {
        /* erste Vorlage durchlaufen */
        Template vor = tempProcess.getVorlagenList().get(0);
        for (Templateproperty eig : vor.getEigenschaften()) {
            for (AdditionalField field : this.additionalFields) {
                if (field.getTitel().equals(eig.getTitel())) {
                    setFieldValue(field, eig.getWert());
                }
            }
        }
    }

    private void fillTemplateFromMasterpiece(Process tempProcess) {
        Masterpiece werk = tempProcess.getWerkstueckeList().get(0);
        for (Masterpieceproperty eig : werk.getEigenschaften()) {
            for (AdditionalField field : this.additionalFields) {
                if (field.getTitel().equals(eig.getTitel())) {
                    setFieldValue(field, eig.getWert());
                }
                if ("DocType".equals(eig.getTitel())) {
                    docType = eig.getWert();
                }
            }
        }
    }

    private void fillTemplateFromMetadata(List<Metadata> struct, String structName) {
        List<Metadata> toRemove = new ArrayList<>(struct.size());
        for (Metadata m : struct) {
            Optional<AdditionalField> additionalField = this.additionalFields.stream()
                    .filter(f -> structName.equals(f.getDocstruct()))
                    .filter(f -> f.getMetadata() != null && f.getMetadata().equals(m.getType().getName()))
                    .findFirst();
            if (additionalField.isPresent()) {
                setFieldValue(additionalField.get(), m.getValue());
                toRemove.add(m);
            }
        }
        // We need to remove all metadata that are saved into additional fields, because they would be possibly added twice in the end
        toRemove.forEach(struct::remove);
    }

    private void setFieldValue(AdditionalField field, String value) {
        if (field.isMultiselect()) {
            List<String> existingValues = field.getValues();
            existingValues.add(value);
            field.setValues(existingValues);
        } else {
            field.setWert(value);
        }
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
        if (this.prozessKopie.getTitel() == null || "".equals(this.prozessKopie.getTitel())) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + Helper.getTranslation("ProcessCreationErrorTitleEmpty")); //NOSONAR
        }

        String validateRegEx = ConfigurationHelper.getInstance().getProcessTitleValidationRegex();
        if (!this.prozessKopie.getTitel().matches(validateRegEx)) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UngueltigerTitelFuerVorgang"));
        }

        /* prüfen, ob der Processtitel schon verwendet wurde */
        if (this.prozessKopie.getTitel() != null
                && ProcessManager.countProcessTitle(prozessKopie.getTitel(), prozessKopie.getProjekt().getInstitution()) > 0) {
            valide = false;
            existingProcess = ProcessManager.getProcessByExactTitle(prozessKopie.getTitel());
            showExistingProcessButton = true;
        }

        /*
         * -------------------------------- Prüfung der standard-Eingaben, die angegeben werden müssen --------------------------------
         */
        /* keine Collektion ausgewählt */
        if (this.standardFields.get("collections") && getDigitalCollections().size() == 0) {
            valide = false;
            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " "
                    + Helper.getTranslation("ProcessCreationErrorNoCollection"));
        }

        /*
         * -------------------------------- Prüfung der additional-Eingaben, die angegeben werden müssen --------------------------------
         */
        for (AdditionalField field : this.additionalFields) {
            if ((field.getWert() == null || field.getWert().isBlank()) && field.isRequired() && field.getShowDependingOnDoctype(getDocType())) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + field.getTitel() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));

            }
        }

        // property validation

        for (ProcessProperty pt : configuredProperties) {
            if (!pt.isValid()
                    || (AccessCondition.WRITEREQUIRED.equals(pt.getShowProcessGroupAccessCondition())
                            && StringUtils.isBlank(pt.getValue()))) {
                valide = false;
                Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + pt.getName() + " "
                        + Helper.getTranslation("ProcessCreationErrorFieldIsEmpty"));
            }

        }

        return valide;
    }

    public void showTablesOfExistingProcess() {
        log.debug("showing tables of existing process");
        showExistingProcessTables = true;
        showExistingProcessButton = false;
    }

    /**
     * print the infos of the existing process
     *
     * @param processName title that has already been used by some process
     */
    @SuppressWarnings("unused")
    private void printExistingProcessInfos(String processName) {
        if (StringUtils.isBlank(processName)) {
            return;
        }

        Process currentProcess = ProcessManager.getProcessByExactTitle(processName);

        // basic infos
        String processTitle = currentProcess.getTitel();
        String creationDate = currentProcess.getErstellungsdatumAsString();
        String projectName = currentProcess.getProjekt().getTitel();
        StringBuilder basicBuilder = new StringBuilder("BASIC INFOS OF THE EXISTING PROCESS:\n");
        basicBuilder.append("process title: ").append(processTitle).append("\n");
        basicBuilder.append("creation date: ").append(creationDate).append("\n");
        basicBuilder.append("project name: ").append(projectName).append("\n");
        Helper.setFehlerMeldung(basicBuilder.toString());

        // infos of steps
        List<Step> processSteps = currentProcess.getSchritte();
        StringBuilder stepBuilder = new StringBuilder("LIST OF STEPS AND THEIR STATUS:\n");
        for (Step step : processSteps) {
            int stepId = step.getReihenfolge();
            String stepName = step.getTitel();
            String stepStatus = step.getBearbeitungsstatusEnum().toString();
            stepBuilder.append(stepId).append(". ");
            stepBuilder.append(stepName).append(": ");
            stepBuilder.append(stepStatus);
            stepBuilder.append("\n");
        }
        Helper.setFehlerMeldung(stepBuilder.toString());

        // journal entries
        List<JournalEntry> journalEntries = currentProcess.getJournal();
        StringBuilder journalBuilder = new StringBuilder("LIST OF JOURNAL ENTRIES:\n");
        for (JournalEntry entry : journalEntries) {
            String entryDate = entry.getFormattedCreationDate();
            String entryUser = entry.getUserName();
            String entryContent = entry.getContent();
            journalBuilder.append(entryDate).append(" --- ");
            journalBuilder.append(entryUser).append(" --- ");
            journalBuilder.append(entryContent);
            journalBuilder.append("\n");
        }
        Helper.setFehlerMeldung(journalBuilder.toString());
    }

    /* =============================================================== */

    public String openFirstPage() {
        return this.naviFirstPage;
    }

    /* =============================================================== */

    public String openPage2() {
        if (this.prozessKopie.getTitel() == null || "".equals(this.prozessKopie.getTitel())) {
            calculateProcessTitle();
        }
        if (!isContentValid()) {
            return this.naviFirstPage;
        } else {
            return "process_new2";
        }
    }

    /**
     * Anlegen des Processes und Speichern der Metadaten ================================================================
     *
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */
    public String createNewProcess()
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException {

        if (this.prozessKopie.getTitel() == null || "".equals(this.prozessKopie.getTitel())) {
            calculateProcessTitle();
        }
        if (!isContentValid()) {
            return this.naviFirstPage;
        }
        addProperties();
        LoginBean loginForm = Helper.getLoginBean();
        for (Step step : this.prozessKopie.getSchritteList()) {
            prepareSteps(loginForm, step);
        }

        this.prozessKopie.setSortHelperImages(this.guessedImages);

        for (ProcessProperty pt : configuredProperties) {
            Processproperty pe = new Processproperty();
            pe.setProzess(prozessKopie);
            pt.setProzesseigenschaft(pe);
            prozessKopie.getEigenschaften().add(pe);
            pt.transfer();
        }

        ProcessManager.saveProcess(this.prozessKopie);

        if (currentCatalogue != null && currentCatalogue.getOpacPlugin() != null && currentCatalogue.getOpacPlugin() instanceof IOpacPluginVersion2) {
            storeOpacData();
        }

        if (StringUtils.isNotBlank(addToWikiField)) {
            writeJournalEntry(loginForm);
        }

        /*
         * wenn noch keine RDF-Datei vorhanden ist (weil keine Opac-Abfrage stattfand, dann jetzt eine anlegen
         */
        if (this.myRdf == null) {
            try {
                createNewFileformat();
            } catch (TypeNotAllowedForParentException | TypeNotAllowedAsChildException e) {
                Helper.setFehlerMeldung("ProcessCreationError_mets_save_error"); //NOSONAR
                Helper.setFehlerMeldung(e);
                ProcessManager.deleteProcess(prozessKopie);

                //this ensures that the process will be saved later, if corrected. If
                //the id is not null, then it is assumed that the process is already saved.
                prozessKopie.setId(null);
                return "";
            }
        }

        /*--------------------------------
         * wenn eine RDF-Konfiguration
         * vorhanden ist (z.B. aus dem Opac-Import, oder frisch angelegt), dann
         * diese ergänzen
         * --------------------------------*/
        if (this.myRdf != null) {
            for (AdditionalField field : this.additionalFields) {
                if (field.isUghbinding() && field.getShowDependingOnDoctype(getDocType())) {
                    /* welches Docstruct */
                    DocStruct myTempStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
                    DocStruct myTempChild = null;
                    if ("child".equals(field.getDocstruct()) || "firstchild".equals(field.getDocstruct())) {
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
                    if (!"firstchild".equals(field.getDocstruct()) && field.getDocstruct().contains("firstchild")) {
                        try {
                            myTempChild = this.myRdf.getDigitalDocument().getLogicalDocStruct().getAllChildren().get(0);
                        } catch (RuntimeException e) {
                            // nothing to do
                        }
                    }
                    if ("boundbook".equals(field.getDocstruct())) {
                        myTempStruct = this.myRdf.getDigitalDocument().getPhysicalDocStruct();
                    }
                    /* welches Metadatum */
                    try {
                        /*
                         * bis auf die Autoren alle additionals in die Metadaten übernehmen
                         */
                        if (!"ListOfCreators".equals(field.getMetadata())) {
                            MetadataType mdt = this.ughHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), field.getMetadata());
                            if (field.isMultiselect()) {
                                // multiselect
                                for (String selectedValue : field.getValues()) {
                                    createMetadata(myTempStruct, mdt, selectedValue);
                                    if (myTempChild != null) {
                                        createMetadata(myTempChild, mdt, selectedValue);
                                    }
                                }
                            } else {
                                Metadata md = this.ughHelper.getMetadata(myTempStruct, mdt);
                                if (md != null) {
                                    md.setValue(field.getWert());
                                } else if (this.ughHelper.lastErrorMessage != null && field.getWert() != null && !field.getWert().isEmpty())
                                //if the md could not be found, warn!
                                {
                                    Helper.setFehlerMeldung(this.ughHelper.lastErrorMessage);
                                    String strError = mdt.getName() + " : " + field.getWert();
                                    Helper.setFehlerMeldung(strError);
                                }
                                /*
                                 * wenn dem Topstruct und dem Firstchild der Wert gegeben werden soll
                                 */
                                if (myTempChild != null) {
                                    md = this.ughHelper.getMetadata(myTempChild, mdt);
                                    if (md != null) {
                                        md.setValue(field.getWert());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Helper.setFehlerMeldung(e);

                    }
                } // end if ughbinding
            } // end for

            /*
             * -------------------------------- Collectionen hinzufügen --------------------------------
             */
            DocStruct colStruct = this.myRdf.getDigitalDocument().getLogicalDocStruct();
            try {
                addCollections(colStruct);
                /* falls ein erstes Kind vorhanden ist, sind die Collectionen dafür */
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
                MetadataType mdt = this.ughHelper.getMetadataType(this.prozessKopie, "pathimagefiles");
                List<? extends Metadata> alleImagepfade = this.myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadataByType(mdt);
                if (alleImagepfade != null && !alleImagepfade.isEmpty()) {
                    for (Metadata md : alleImagepfade) {
                        this.myRdf.getDigitalDocument().getPhysicalDocStruct().getAllMetadata().remove(md);
                    }
                }
                Metadata newmd = new Metadata(mdt);
                if (SystemUtils.IS_OS_WINDOWS) {
                    newmd.setValue("file:/" + this.prozessKopie.getImagesDirectory() + this.prozessKopie.getTitel().trim() + DIRECTORY_SUFFIX);
                } else {
                    newmd.setValue("file://" + this.prozessKopie.getImagesDirectory() + this.prozessKopie.getTitel().trim() + DIRECTORY_SUFFIX);
                }
                this.myRdf.getDigitalDocument().getPhysicalDocStruct().addMetadata(newmd);

                /* Rdf-File schreiben */
                this.prozessKopie.writeMetadataFile(this.myRdf);
                try {
                    this.prozessKopie.readMetadataFile();
                } catch (IOException e) {
                    Helper.setFehlerMeldung("ProcessCreationError_mets_save_error");
                    ProcessManager.deleteProcess(prozessKopie);

                    //this ensures that the process will be saved later, if corrected. If
                    //the id is not null, then it is assumed that the process is already saved.
                    prozessKopie.setId(null);
                    return "";
                }
                /*
                 * -------------------------------- soll der Process als Vorlage verwendet werden? --------------------------------
                 */
                if (this.useTemplates && this.prozessKopie.isInAuswahllisteAnzeigen()) {
                    this.prozessKopie.writeMetadataAsTemplateFile(this.myRdf);
                }

            } catch (UghHelperException | UGHException e) {
                Helper.setFehlerMeldung("ProcessCreationError_mets_save_error");
                Helper.setFehlerMeldung(e.getMessage());
                log.error("creation of new process throws an error: ", e);
                ProcessManager.deleteProcess(prozessKopie);

                //this ensures that the process will be saved later, if corrected. If
                //the id is not null, then it is assumed that the process is already saved.
                prozessKopie.setId(null);
                return "";
            }

        }

        // Adding process to history
        if (!HistoryAnalyserJob.updateHistoryForProzess(this.prozessKopie)) {
            Helper.setFehlerMeldung("historyNotUpdated");
            return "";
        } else {
            ProcessManager.saveProcess(this.prozessKopie);
        }

        //  read all uploaded files, copy them to the right destination, create log entries
        if (!uploadedFiles.isEmpty()) {
            for (UploadImage image : uploadedFiles) {
                if (!image.isDeleted()) {
                    Path folder = null;

                    if ("intern".equals(image.getFoldername())) {
                        folder = Paths.get(prozessKopie.getProcessDataDirectory(),
                                ConfigurationHelper.getInstance().getFolderForInternalJournalFiles());
                    } else if ("export".equals(image.getFoldername())) {
                        folder = Paths.get(prozessKopie.getExportDirectory());
                    } else {
                        folder = Paths.get(prozessKopie.getConfiguredImageFolder(image.getFoldername()));
                    }

                    if (!StorageProvider.getInstance().isFileExists(folder)) {
                        StorageProvider.getInstance().createDirectories(folder);
                    }
                    Path source = image.getImagePath();
                    Path destination = Paths.get(folder.toString(), source.getFileName().toString());
                    StorageProvider.getInstance().copyFile(source, destination);

                    if ("intern".equals(image.getFoldername()) || "export".equals(image.getFoldername())) {
                        JournalEntry entry = new JournalEntry(prozessKopie.getId(), new Date(), Helper.getCurrentUser().getNachVorname(),
                                LogType.FILE, image.getDescriptionText(), EntryType.PROCESS);
                        entry.setFilename(destination.toString());
                        JournalManager.saveJournalEntry(entry);
                    }
                }

            }
            // finally clean up
            for (UploadImage image : uploadedFiles) {
                try {
                    StorageProvider.getInstance().deleteFile(image.getImagePath());
                } catch (Exception e) {
                    // do nothing, as this happens if the same file gets used in multiple target folders
                }
            }
        }
        List<Step> steps = StepManager.getStepsForProcess(prozessKopie.getId());
        for (Step s : steps) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum()) && s.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                myThread.startOrPutToQueue();
            }
        }
        prozessKopie = ProcessManager.getProcessById(prozessKopie.getId());
        return "process_new3";
    }

    private void createMetadata(DocStruct myTempStruct, MetadataType mdt, String selectedValue) {
        try {
            Metadata md = new Metadata(mdt);
            md.setValue(selectedValue);
            md.setParent(myTempStruct);
            myTempStruct.addMetadata(md);
        } catch (UGHException e) {
            log.error(e);
        }
    }

    private void writeJournalEntry(LoginBean loginForm) {
        User user = loginForm.getMyBenutzer();
        JournalEntry logEntry =
                new JournalEntry(prozessKopie.getId(), new Date(), user.getNachVorname(), importantWikiField ? LogType.IMPORTANT_USER : LogType.USER,
                        addToWikiField, EntryType.PROCESS);
        JournalManager.saveJournalEntry(logEntry);
        prozessKopie.getJournal().add(logEntry);
    }

    private void storeOpacData() throws SwapException, IOException {
        IOpacPluginVersion2 opacPluginV2 = (IOpacPluginVersion2) currentCatalogue.getOpacPlugin();
        // check if the plugin created files
        if (opacPluginV2.getRecordPathList() != null) {
            for (Path r : opacPluginV2.getRecordPathList()) {
                // if this is the case, move the files to the import/ folder
                Path destination = Paths.get(prozessKopie.getImportDirectory(), r.getFileName().toString());
                StorageProvider.getInstance().createDirectories(destination.getParent());
                StorageProvider.getInstance().move(r, destination);
            }
        }
        // check if the plugin provides the data as string
        if (opacPluginV2.getRawDataAsString() != null) {
            // if this is the case, store it in a file in import/
            for (Entry<String, String> entry : opacPluginV2.getRawDataAsString().entrySet()) {
                Path destination = Paths.get(prozessKopie.getImportDirectory(), entry.getKey().replaceAll("\\W", "_"));
                StorageProvider.getInstance().createDirectories(destination.getParent());
                Files.write(destination, entry.getValue().getBytes());
            }
        }
    }

    private void prepareSteps(LoginBean loginForm, Step step) {
        /*
         * -------------------------------- only if its done, set edit start and end date --------------------------------
         */
        if (step.getBearbeitungsstatusEnum() == StepStatus.DONE) {

            step.setBearbeitungszeitpunkt(this.prozessKopie.getErstellungsdatum());
            step.setEditTypeEnum(StepEditType.AUTOMATIC);

            if (loginForm != null) {
                step.setBearbeitungsbenutzer(loginForm.getMyBenutzer());
            }

            step.setBearbeitungsbeginn(this.prozessKopie.getErstellungsdatum());
            // this concerns steps, which are set as done right on creation
            // bearbeitungsbeginn is set to creation timestamp of process
            // because the creation of it is basically begin of work
            Date myDate = new Date();
            step.setBearbeitungszeitpunkt(myDate);
            step.setBearbeitungsende(myDate);
        }
    }

    /* =============================================================== */

    private void addCollections(DocStruct colStruct) {
        for (String s : this.digitalCollections) {
            try {
                Metadata md = new Metadata(this.ughHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection"));
                md.setValue(s);
                md.setParent(colStruct);
                colStruct.addMetadata(md);
            } catch (UghHelperException | DocStructHasNoTypeException | MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung(e.getMessage(), "");
            }
        }
    }

    /**
     * alle Kollektionen eines übergebenen DocStructs entfernen ================================================================
     */
    private void removeCollections(DocStruct colStruct) {
        try {
            MetadataType mdt = this.ughHelper.getMetadataType(this.prozessKopie.getRegelsatz().getPreferences(), "singleDigCollection");
            ArrayList<Metadata> myCollections = new ArrayList<>(colStruct.getAllMetadataByType(mdt));
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

    private void createNewFileformat() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        Prefs myPrefs = this.prozessKopie.getRegelsatz().getPreferences();
        try {
            DigitalDocument dd = new DigitalDocument();
            Fileformat ff = new XStream(myPrefs);
            ff.setDigitalDocument(dd);
            /* BoundBook hinzufügen */
            DocStructType dst = myPrefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            ConfigOpacDoctype configOpacDoctype = co.getDoctypeByName(this.docType);

            /* Monographie */
            if (!configOpacDoctype.isPeriodical() && !configOpacDoctype.isMultiVolume()) {
                DocStructType dsty = myPrefs.getDocStrctTypeByName(configOpacDoctype.getRulesetType());
                DocStruct ds = dd.createDocStruct(dsty);
                dd.setLogicalDocStruct(ds);
                this.myRdf = ff;
            }

            /* periodica */
            else if (configOpacDoctype.isPeriodical() || configOpacDoctype.isMultiVolume()) {

                DocStructType anchor = myPrefs.getDocStrctTypeByName(configOpacDoctype.getRulesetType());
                DocStruct ds = dd.createDocStruct(anchor);
                dd.setLogicalDocStruct(ds);

                DocStructType dstyvolume = myPrefs.getDocStrctTypeByName(configOpacDoctype.getRulesetChildType());
                DocStruct dsvolume = dd.createDocStruct(dstyvolume);
                ds.addChild(dsvolume);
                this.myRdf = ff;
            }

        } catch (PreferencesException e) {
            log.error(e);
        }
    }

    private void addProperties() {
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
        for (AdditionalField field : this.additionalFields) {
            if (field.getShowDependingOnDoctype(getDocType())) {
                List<String> values;
                if (field.isMultiselect()) {
                    values = field.getValues();
                } else {
                    values = new ArrayList<>();
                    values.add(field.getWert());
                }
                for (String value : values) {
                    if ("work".equals(field.getFrom()) || "werk".equals(field.getFrom())) {
                        bh.EigenschaftHinzufuegen(werk, field.getTitel(), value);
                    }
                    if ("template".equals(field.getFrom()) || "vorlage".equals(field.getFrom())) {
                        bh.EigenschaftHinzufuegen(vor, field.getTitel(), value);
                    }
                    if ("process".equals(field.getFrom()) || "prozess".equals(field.getFrom())) {
                        bh.EigenschaftHinzufuegen(this.prozessKopie, field.getTitel(), value);
                    }
                }
            }
        }

        for (String col : digitalCollections) {
            bh.EigenschaftHinzufuegen(prozessKopie, "digitalCollection", col);
        }
        /* Doctype */
        bh.EigenschaftHinzufuegen(werk, "DocType", this.docType);
        /* Tiffheader */
        bh.EigenschaftHinzufuegen(werk, "TifHeaderImagedescription", this.tifHeaderImagedescription);
        bh.EigenschaftHinzufuegen(werk, "TifHeaderDocumentname", this.tifHeaderDocumentname);
        bh.EigenschaftHinzufuegen(prozessKopie, "Template", prozessVorlage.getTitel());
        bh.EigenschaftHinzufuegen(prozessKopie, "TemplateID", String.valueOf(prozessVorlage.getId()));
    }

    public void setDocType(String docType) throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        if (this.docType == null || !this.docType.equals(docType)) {
            this.docType = docType;
            if (myRdf != null) {

                Fileformat tmp = myRdf;

                createNewFileformat();

                try {
                    if (myRdf.getDigitalDocument().getLogicalDocStruct().equals(tmp.getDigitalDocument().getLogicalDocStruct())) {
                        myRdf = tmp;
                    } else {
                        DocStruct oldLogicalDocstruct = tmp.getDigitalDocument().getLogicalDocStruct();
                        DocStruct newLogicalDocstruct = myRdf.getDigitalDocument().getLogicalDocStruct();
                        // both have no childen
                        if (oldLogicalDocstruct.getAllChildren() == null && newLogicalDocstruct.getAllChildren() == null) {
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                        }
                        // old has a child, new has no child
                        else if (oldLogicalDocstruct.getAllChildren() != null && newLogicalDocstruct.getAllChildren() == null) {
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.getAllChildren().get(0), newLogicalDocstruct);
                        }
                        // new has a child, bot old not
                        else if (oldLogicalDocstruct.getAllChildren() == null && newLogicalDocstruct.getAllChildren() != null) {
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.copy(true, false), newLogicalDocstruct.getAllChildren().get(0));
                        }

                        // both have childen
                        else if (oldLogicalDocstruct.getAllChildren() != null && newLogicalDocstruct.getAllChildren() != null) {
                            copyMetadata(oldLogicalDocstruct, newLogicalDocstruct);
                            copyMetadata(oldLogicalDocstruct.getAllChildren().get(0), newLogicalDocstruct.getAllChildren().get(0));
                        }
                    }
                } catch (PreferencesException e) {
                    log.error(e);
                }
                try {
                    fillFieldsFromMetadataFile();
                } catch (PreferencesException e) {
                    log.error(e);
                }
            }
        }
    }

    private void copyMetadata(DocStruct oldDocStruct, DocStruct newDocStruct) {

        if (oldDocStruct.getAllMetadata() != null) {
            for (Metadata md : oldDocStruct.getAllMetadata()) {
                try {
                    newDocStruct.addMetadata(md);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    // nothing to do

                }
            }
        }
        if (oldDocStruct.getAllPersons() != null) {
            for (Person p : oldDocStruct.getAllPersons()) {
                try {
                    newDocStruct.addPerson(p);
                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    // nothing to do
                }
            }
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

        String filename = this.help.getGoobiConfigDirectory() + "goobi_digitalCollections.xml";
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
            for (Element projekt : projekte) {
                // collect default collections
                if ("default".equals(projekt.getName())) { //NOSONAR
                    List<Element> myCols = projekt.getChildren("DigitalCollection");
                    for (Element col : myCols) {
                        if (col.getAttribute("default") != null && "true".equalsIgnoreCase(col.getAttributeValue("default"))) {
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
                                if (col.getAttribute("default") != null && "true".equalsIgnoreCase(col.getAttributeValue("default"))) {
                                    digitalCollections.add(col.getText());
                                }

                                this.possibleDigitalCollection.add(col.getText());
                            }
                        }
                    }
                }
            }
        } catch (JDOMException | IOException e1) {
            log.error("error while parsing digital collections", e1);
            Helper.setFehlerMeldung("Error while parsing digital collections", e1);
        }

        if (this.possibleDigitalCollection.isEmpty()) {
            this.possibleDigitalCollection = defaultCollections;
        }

        // if only one collection is possible take it directly

        if (isSingleChoiceCollection()) {
            this.digitalCollections.add(getDigitalCollectionIfSingleChoice());
        }
    }

    public Map<String, String> getAllSearchFields() {
        if (co.getCatalogueByName(opacKatalog) != null) {
            return co.getCatalogueByName(opacKatalog).getSearchFields();
        } else {
            return ConfigOpac.getInstance().getSearchFieldMap();
        }
    }

    public List<String> getAllOpacCatalogues() {
        return catalogueTitles;
    }

    public List<ConfigOpacDoctype> getAllDoctypes() {
        return co.getAllDoctypes();
    }

    /*
     * changed, so that on first request list gets set if there is only one choice
     */

    public void setOpacKatalog(String opacKatalog) {
        // currentCatalogue is set to null in prepare(), but is required in opacAuswerten().
        // So reset it here if it is null or if the catalog name (this.opacKatalog) has changed
        if (this.currentCatalogue == null || !this.opacKatalog.equals(opacKatalog)) {
            this.opacKatalog = opacKatalog;
            currentCatalogue = null;
            for (ConfigOpacCatalogue catalogue : catalogues) {
                if (opacKatalog.equals(catalogue.getTitle())) {
                    currentCatalogue = catalogue;
                    break;
                }
            }

            if (currentCatalogue == null && !catalogues.isEmpty()) {
                // get first catalogue in case configured catalogue doesn't exist
                currentCatalogue = catalogues.get(0);
                this.opacKatalog = currentCatalogue.getTitle();
            }
            if (currentCatalogue != null) {
                currentCatalogue.getOpacPlugin().setTemplateName(prozessVorlage.getTitel());
                currentCatalogue.getOpacPlugin().setProjectName(prozessVorlage.getProjekt().getTitel());
            }
        }
    }

    /**
     * Get the UI part of the opac plugin as path to let it be embedded into the process creation interface
     *
     * @return
     */
    public String getPluginGui() {
        if (currentCatalogue == null || currentCatalogue.getOpacPlugin() == null) {
            return "/uii/template/includes/process/process_new_opac.xhtml";
        } else {
            return currentCatalogue.getOpacPlugin().getGui();
        }
    }

    /*
     * Helper
     */

    /**
     * Processtitel und andere Details generieren
     */
    public void calculateProcessTitle() {
        String currentAuthors = "";
        String currentTitle = "";
        int counter = 0;
        for (AdditionalField field : this.additionalFields) {
            if (field.getAutogenerated() && field.getWert().isEmpty()) {
                field.setWert(String.valueOf(System.currentTimeMillis() + counter));
                counter++;
            }
            if (field.getMetadata() != null && "TitleDocMain".equals(field.getMetadata()) && currentTitle.isEmpty()) {
                currentTitle = field.getWert();
            } else if (field.getMetadata() != null && "ListOfCreators".equals(field.getMetadata()) && currentAuthors.isEmpty()) {
                currentAuthors = field.getWert();
            }

        }
        String titeldefinition = "";
        ConfigProjects cp = ProzesskopieForm.initializeConfigProjects(this.prozessVorlage.getProjekt().getTitel());
        if (cp == null) {
            return;
        }

        String replacement = "";
        List<HierarchicalConfiguration> processTitleList = cp.getList("createNewProcess/itemlist/processtitle");

        for (HierarchicalConfiguration hc : processTitleList) {
            String titel = hc.getString(".");
            String isdoctype = hc.getString("@isdoctype");
            String isnotdoctype = hc.getString("@isnotdoctype");

            String replacementText = hc.getString("@replacewith", "");

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
                replacement = replacementText;
                break;
            }
        }

        StringTokenizer tokenizer = new StringTokenizer(titeldefinition, "+");
        ProcessTitleGenerator gen = new ProcessTitleGenerator();
        gen.setSpecialCharacterReplacement(replacement);
        gen.setSeparator("");
        /* jetzt den Bandtitel parsen */
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();

            /*
             * wenn der String mit ' anfängt und mit ' endet, dann den Inhalt so übernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'")) {
                gen.addToken(myString.substring(1, myString.length() - 1), ManipulationType.NORMAL);
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField myField : this.additionalFields) {
                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if (("ATS".equals(myField.getTitel()) || "TSL".equals(myField.getTitel())) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || "".equals(myField.getWert()))) {
                        if (StringUtils.isBlank(atstsl)) {
                            atstsl = createAtstsl(currentTitle, currentAuthors);
                        }
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType())) {
                        String value = myField.getWert();
                        if (value == null) {
                            value = "";
                        }

                        // Skip process title generation if a required field is not present
                        if (myField.isRequired() && value.isBlank()) {
                            Helper.setFehlerMeldung(Helper.getTranslation("UnvollstaendigeDaten") + " " + myField.getTitel());
                            return;
                        }
                        gen.addToken(calcProcesstitelCheck(myField.getTitel(), value), ManipulationType.NORMAL);
                    }
                }
            }
        }
        String newTitle = gen.generateTitle();
        if (newTitle.endsWith("_")) {
            newTitle = newTitle.substring(0, newTitle.length() - 1);
        }
        // remove non-ascii characters for the sake of TIFF header limits
        String regex = ConfigurationHelper.getInstance().getProcessTitleReplacementRegex();

        String filteredTitle = newTitle.replaceAll(regex, replacement);
        prozessKopie.setTitel(filteredTitle);
        calcTiffheader();
    }

    /* =============================================================== */

    private String calcProcesstitelCheck(String inFeldName, String inFeldWert) {
        String rueckgabe = inFeldWert;

        /*
         * -------------------------------- Bandnummer --------------------------------
         */
        if ("Bandnummer".equals(inFeldName) || "Volume number".equals(inFeldName)) {
            try {
                int bandint = Integer.parseInt(inFeldWert);
                java.text.DecimalFormat df = new java.text.DecimalFormat("#0000");
                rueckgabe = df.format(bandint);
            } catch (NumberFormatException e) {
                if ("Bandnummer".equals(inFeldName)) {
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten: ") + "Bandnummer ist keine gültige Zahl");
                } else {
                    Helper.setFehlerMeldung(Helper.getTranslation("UngueltigeDaten: ") + "Volume number is not a valid number");
                }
            }
            if (rueckgabe != null && rueckgabe.length() < 4) {
                rueckgabe = "0000".substring(rueckgabe.length()) + rueckgabe;
            }
        }

        if (("Signatur".equalsIgnoreCase(inFeldName) || "Shelfmark".equalsIgnoreCase(inFeldName)) && StringUtils.isNotBlank(rueckgabe)) {
            String[] parts = rueckgabe.split("[ /,.]");
            StringBuilder sb = new StringBuilder();
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    if (sb.length() > 0) {
                        sb.append("-");
                    }
                    sb.append(part);
                }
            }
            rueckgabe = sb.toString().replaceAll("[^\\w-]", "");
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

        tifDefinition = cp.getParamString("tifheader/" + this.docType, "intranda");

        /*
         * -------------------------------- evtuelle Ersetzungen --------------------------------
         */
        tifDefinition = tifDefinition.replace("[[", "<");
        tifDefinition = tifDefinition.replace("]]", ">");

        /*
         * -------------------------------- Documentname ist im allgemeinen = Processtitel --------------------------------
         */
        this.tifHeaderDocumentname = this.prozessKopie.getTitel();
        this.tifHeaderImagedescription = "";
        StringBuilder sb = new StringBuilder();

        /*
         * -------------------------------- Imagedescription --------------------------------
         */
        StringTokenizer tokenizer = new StringTokenizer(tifDefinition, "+");
        /* jetzt den Tiffheader parsen */
        String title = "";
        while (tokenizer.hasMoreTokens()) {
            String myString = tokenizer.nextToken();
            /*
             * wenn der String mit ' anfaengt und mit ' endet, dann den Inhalt so uebernehmen
             */
            if (myString.startsWith("'") && myString.endsWith("'") && myString.length() > 2) {
                sb.append(myString.substring(1, myString.length() - 1));
            } else if ("$Doctype".equals(myString)) {
                /* wenn der Doctype angegeben werden soll */
                sb.append(this.co.getDoctypeByName(this.docType).getTifHeaderType());
            } else {
                /* andernfalls den string als Feldnamen auswerten */
                for (AdditionalField myField : this.additionalFields) {
                    if (("Titel".equals(myField.getTitel()) || "Title".equals(myField.getTitel())) && myField.getWert() != null
                            && !"".equals(myField.getWert())) {
                        title = myField.getWert();
                    }
                    /*
                     * wenn es das ATS oder TSL-Feld ist, dann den berechneten atstsl einsetzen, sofern noch nicht vorhanden
                     */
                    if (("ATS".equals(myField.getTitel()) || "TSL".equals(myField.getTitel())) && myField.getShowDependingOnDoctype(getDocType())
                            && (myField.getWert() == null || "".equals(myField.getWert()))) {
                        myField.setWert(this.atstsl);
                    }

                    /* den Inhalt zum Titel hinzufügen */
                    if (myField.getTitel().equals(myString) && myField.getShowDependingOnDoctype(getDocType()) && myField.getWert() != null) {
                        sb.append(calcProcesstitelCheck(myField.getTitel(), myField.getWert()));
                    }

                }
            }
            // reduce to 255 character
        }
        tifHeaderImagedescription = sb.toString();
        int length = this.tifHeaderImagedescription.length();
        if (length > 255) {
            try {
                int toCut = length - 255;
                String newTitle = title.substring(0, title.length() - toCut);
                this.tifHeaderImagedescription = this.tifHeaderImagedescription.replace(title, newTitle);
            } catch (IndexOutOfBoundsException e) {
                // do nothing, keep unchanged imagedescription
            }
        }
    }

    /**
     * @param imagesGuessed the imagesGuessed to set
     */
    public void setImagesGuessed(Integer imagesGuessed) {
        if (imagesGuessed == null) {
            imagesGuessed = 0;
        }
        this.guessedImages = imagesGuessed;
    }

    /**
     * @return the imagesGuessed
     */
    public Integer getImagesGuessed() {
        return this.guessedImages;
    }

    public Integer getRulesetSelection() {
        if (this.prozessKopie.getRegelsatz() != null) {
            return this.prozessKopie.getRegelsatz().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setRulesetSelection(Integer selected) {
        if (selected.intValue() != 0) {
            try {
                Ruleset ruleset = RulesetManager.getRulesetById(selected);
                prozessKopie.setRegelsatz(ruleset);
                prozessKopie.setMetadatenKonfigurationID(selected);
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

    public String createAtstsl(String title, String author) {
        StringBuilder result = new StringBuilder(8);
        if (StringUtils.isNotBlank(author)) {
            result.append(author.length() > 4 ? author.substring(0, 4) : author);
            result.append(title.length() > 4 ? title.substring(0, 4) : title);
        } else {
            StringTokenizer titleWords = new StringTokenizer(title);
            int wordNo = 1;
            while (titleWords.hasMoreTokens() && wordNo < 5) {
                String word = titleWords.nextToken();
                switch (wordNo) {
                    case 1:
                        result.append(word.length() > 4 ? word.substring(0, 4) : word);
                        break;
                    case 2:
                    case 3:
                        result.append(word.length() > 2 ? word.substring(0, 2) : word);
                        break;
                    case 4:
                        result.append(word.length() > 1 ? word.substring(0, 1) : word);
                        break;
                    default:

                }
                wordNo++;
            }
        }
        String res = UghHelper.convertUmlaut(result.toString()).toLowerCase();
        return res.replaceAll("[\\W]", ""); // delete umlauts etc.
    }

    public List<Project> getAvailableProjects() throws DAOException {
        if (availableProjects.isEmpty()) {
            availableProjects = ProjectManager.getProjectsForUser(Helper.getCurrentUser(), true);
        }
        return availableProjects;
    }

    public void clearAvailableProjects() {
        availableProjects.clear();
    }

    public boolean isUserInProcessProject(Process process) throws DAOException {
        return this.getAvailableProjects().contains(process.getProjekt());
    }

    public IOpacPlugin getOpacPlugin() {
        if (currentCatalogue != null) {
            return currentCatalogue.getOpacPlugin();
        }
        return null;
    }

    // file upload area

    public void setUploadFolder(String folder) {
        this.uploadFolder = folder;
        this.uploadRegex = this.getRegexOfFolder(folder);
        log.debug("Regex: " + this.uploadRegex);
    }

    public String generateFileUploadErrorMessage() {
        String key = this.getErrorMessageKeyOfFolder(this.uploadFolder);
        String message = "";

        if (StringUtils.isNotBlank(key)) {
            String result = Helper.getTranslation(key, this.uploadRegex);
            if (StringUtils.isNotBlank(result) && !result.equals(key)) {
                message = result;
            } else {
                message = "";
            }
        }
        if (StringUtils.isBlank(message)) {
            message = "The selected file could not be uploaded because it does not match the specified file format.";
        }
        return message;
    }

    private String getRegexOfFolder(String folder) {
        return this.configuredFolderRegex.get(this.getIndexOfFolder(folder));
    }

    private String getErrorMessageKeyOfFolder(String folder) {
        return this.configuredFolderErrorMessageKeys.get(this.getIndexOfFolder(folder));
    }

    private int getIndexOfFolder(String folder) {
        for (int index = 0; index < this.configuredFolderNames.size(); index++) {
            if (this.configuredFolderNames.get(index).getValue().equals(folder)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Get get temporary folder to upload to
     *
     * @return path to temporary folder
     */
    private Path getTemporaryFolder() {
        if (temporaryFolder == null) {
            try {
                temporaryFolder = Files.createTempDirectory(Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder()), "upload");
            } catch (IOException e) {
                log.error(e);
            }
        }
        return temporaryFolder;
    }

    /**
     * Handle the upload of a file
     *
     * @param event
     */
    public void uploadFile(FileUploadEvent event) {
        try {
            UploadedFile upload = event.getFile();
            saveFileTemporary(upload.getFileName(), upload.getInputStream());
        } catch (IOException e) {
            log.error("Error while uploading files", e);
        }
    }

    /**
     * Save the uploaded file temporary in the tmp-folder inside of goobi in a subfolder for the user
     *
     * @param fileName
     * @param in
     * @throws IOException
     */
    private void saveFileTemporary(String fileName, InputStream in) throws IOException {
        Path tmpFolder = getTemporaryFolder();
        if (tmpFolder == null) {
            tmpFolder = Paths.get(ConfigurationHelper.getInstance().getTemporaryFolder());
        }
        String folderName = tmpFolder.toString();
        File file = new File(folderName, fileName);
        try (OutputStream out = new FileOutputStream(file)) {
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }

        UploadImage currentImage = new UploadImage(file.toPath(), 300, uploadFolder, fileComment);
        uploadedFiles.add(currentImage);
        try {
            in.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * prepare variables and lists for next uploads
     */
    private void clearUploadedData() {
        if (temporaryFolder != null) {
            // delete data in folder
            StorageProvider.getInstance().deleteDir(temporaryFolder);
        }
        uploadedFile = null;
        uploadedFiles.clear();
        fileComment = null;
        temporaryFolder = null;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    public class UploadImage extends TempImage implements Comparable<UploadImage> {
        private String foldername;
        private String descriptionText;
        private boolean deleted = false;

        public UploadImage(Path imagePath, Integer thumbnailSize, String foldername, String descriptionText) throws IOException {
            super(imagePath.getParent().getFileName().toString(), imagePath.getFileName().toString(), thumbnailSize);
            this.foldername = foldername;
            this.descriptionText = descriptionText;
        }

        @Override
        public int compareTo(UploadImage o) {
            String one = this.foldername + "_" + this.getTooltip();
            String two = o.foldername + "_" + o.getTooltip();
            return one.compareTo(two);
        }
    }

    public List<ProcessProperty> getConfiguredProperties() {
        List<ProcessProperty> properties = new ArrayList<>();

        for (ProcessProperty prop : configuredProperties) {
            boolean match = true;
            if (!prop.getProcessCreationConditions().isEmpty()) {
                // check if condition matches
                match = false;
                for (StringPair sp : prop.getProcessCreationConditions()) {
                    for (ProcessProperty other : configuredProperties) {
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
            if (match) {
                properties.add(prop);
            }
        }

        return properties;
    }
}
