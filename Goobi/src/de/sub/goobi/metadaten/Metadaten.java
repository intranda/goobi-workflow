package de.sub.goobi.metadaten;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.AltoChange;
import org.goobi.beans.Process;
import org.goobi.beans.SimpleAlto;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.OrderedKeyMap;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.jdom2.JDOMException;
import org.omnifaces.util.Faces;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import de.sub.goobi.helper.HttpClientHelper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.Transliteration;
import de.sub.goobi.helper.TreeNode;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import lombok.Getter;
import lombok.Setter;
import ugh.dl.Corporate;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.HoldingElement;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.IncompletePersonObjectException;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.WriteException;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 17.01.2005
 */
@Named("Metadaten")
@WindowScoped
public class Metadaten implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2361148967408139027L;

    @Getter
    @Setter
    private boolean displayInsertion = false;
    @Getter
    @Setter
    private String filterProcessTitle = "";
    @Getter
    private Process filteredProcess = null;
    private String oldDocstructName = "";
    private static final Logger logger = LogManager.getLogger(Metadaten.class);
    MetadatenImagesHelper imagehelper;
    MetadatenHelper metahelper;
    @Getter
    @Setter
    private boolean treeReloaden = false;
    private Fileformat gdzfile;
    @Getter
    private DocStruct myDocStruct;
    @Setter
    private DocStruct tempStrukturelement;
    @Getter
    @Setter
    private List<MetadatumImpl> myMetadaten = new LinkedList<>();
    @Getter
    @Setter
    private List<MetaPerson> myPersonen = new LinkedList<>();
    @Getter
    @Setter
    private List<MetaCorporate> corporates = new LinkedList<>();

    @Getter
    private List<MetadataGroupImpl> groups = new LinkedList<>();
    @Getter
    @Setter
    private MetadataGroupImpl currentGroup;
    private MetadataGroupImpl selectedGroup;
    @Getter
    @Setter
    private List<MetadataGroupImpl> tempMetadataGroups = new ArrayList<>();
    private String tempGroupType;
    @Getter
    @Setter
    private MetadatumImpl curMetadatum;
    @Getter
    @Setter
    private Metadata currentMetadata;
    @Getter
    @Setter
    private Corporate currentCorporate;
    @Getter
    @Setter
    private MetaPerson curPerson;
    @Getter
    @Setter
    private Person currentPerson;
    @Getter
    @Setter
    private DigitalDocument document;
    @Getter
    @Setter
    private Process myProzess;
    @Getter
    private Prefs myPrefs;
    @Setter
    private String myBenutzerID;
    private String tempTyp;
    @Getter
    @Setter
    private String tempWert;
    @Getter
    @Setter
    private String tempPersonVorname;
    @Getter
    @Setter
    private String tempPersonNachname;
    @Getter
    @Setter
    private String tempPersonRolle;
    @Getter
    @Setter
    private String tempCorporateMainName;
    @Getter
    @Setter
    private String tempCorporateSubName;
    @Getter
    @Setter
    private String tempCorporatePartName;

    @Getter
    private String currentTifFolder;
    private List<String> allTifFolders;
    /* Variablen für die Zuweisung der Seiten zu Strukturelementen */
    @Getter
    @Setter
    private String alleSeitenAuswahl_ersteSeite;
    @Getter
    @Setter
    private String alleSeitenAuswahl_letzteSeite;
    @Getter
    @Setter
    private String[] structSeitenAuswahl;
    @Getter
    @Setter
    private String[] alleSeitenAuswahl;
    //    private SelectItem alleSeiten[];
    @Getter
    private OrderedKeyMap<String, PhysicalObject> pageMap;
    private MetadatumImpl logicalPageNumForPages[];
    @Getter
    @Setter
    private ArrayList<MetadatumImpl> tempMetadatumList = new ArrayList<>();
    @Getter
    @Setter
    private MetadatumImpl selectedMetadatum;

    @Setter
    private MetaCorporate selectedCorporate;
    @Getter
    @Setter
    private PhysicalObject currentPage;
    @Getter
    @Setter
    private String currentRepresentativePage = "";
    @Getter
    @Setter
    private boolean resetRepresentative = false;

    private PhysicalObject lastAddedObject = null;
    @Getter
    private boolean enablePageArea;

    @Getter
    @Setter
    private boolean enableFastPagination = true;
    @Getter
    @Setter
    private String paginierungWert;
    @Getter
    @Setter
    private int paginierungAbSeiteOderMarkierung;
    @Getter
    @Setter
    private String paginierungArt;
    @Getter
    @Setter
    private int paginierungSeitenProImage = 1; // 1=normale Paginierung, 2=zwei
    // Spalten auf einem Image,
    // 3=nur jede zweite Seite hat
    // Seitennummer
    @Getter
    @Setter
    private boolean fictitious = false;

    @Getter
    @Setter
    private String paginationPrefix;
    @Getter
    @Setter
    private String paginationSuffix;

    private SelectItem structSeiten[];
    private MetadatumImpl structSeitenNeu[];
    private DocStruct logicalTopstruct;
    private DocStruct physicalTopstruct;
    private DocStruct currentTopstruct;
    @Getter
    @Setter
    private boolean modusHinzufuegen = false;
    @Getter
    @Setter
    private boolean modusHinzufuegenPerson = false;
    @Getter
    @Setter
    private boolean modeAddGroup = false;
    @Getter
    @Setter
    private boolean modeAddCorporate = false;
    @Getter
    @Setter
    private String modusAnsicht = "Metadaten";
    @Getter
    private boolean modusCopyDocstructFromOtherProcess = false;

    private TreeNodeStruct3 treeOfFilteredProcess;
    private TreeNodeStruct3 tree3;
    private String myBild;

    // old image parameter
    @Getter
    private int bildNummer = 0;
    @Getter
    private int myBildLetztes = 0;
    private int myBildCounter = 0;
    @Getter
    @Setter
    private int myBildGroesse = 30;
    private String bildNummerGeheZu = "";
    @Getter
    @Setter
    private int numberOfNavigation = 0;
    @Getter
    private boolean bildAnzeigen = true;
    @Getter
    @Setter
    private boolean bildZuStrukturelement = false;
    private String addDocStructType1;
    private String addDocStructType2;
    private String zurueck = "Main";
    private MetadatenSperrung sperrung = new MetadatenSperrung();
    @Getter
    @Setter
    private boolean nurLesenModus;
    private String neuesElementWohin = "4";
    private boolean modusStrukturelementVerschieben = false;
    @Getter
    @Setter
    private String additionalOpacPpns;
    @Getter
    @Setter
    private String opacSuchfeld = "12";
    private String opacKatalog;
    @Getter
    @Setter
    private String ajaxSeiteStart = "";
    @Getter
    @Setter
    private String ajaxSeiteEnde = "";
    @Getter
    @Setter
    private String pagesStart = "";
    @Getter
    @Setter
    private String pagesEnd = "";
    @Getter
    @Setter
    private String pageArea = "";
    @Getter
    private boolean pageAreaEditionMode = false;
    @Getter
    @Setter
    private String pagesStartCurrentElement = "";
    @Getter
    @Setter
    private String pagesEndCurrentElement = "";
    @Getter
    @Setter
    private HashMap<String, Boolean> treeProperties;
    @Getter
    @Setter
    private int treeWidth = 180;
    @Setter
    private FileManipulation fileManipulation;

    private double currentImageNo = 0;
    private double totalImageNo = 0;
    @Setter
    private Integer progress;

    private boolean tiffFolderHasChanged = true;
    private List<String> dataList = new ArrayList<>();
    @Getter
    private List<MetadatumImpl> addableMetadata = new LinkedList<>();

    @Getter
    private List<MetaCorporate> addableCorporates = new LinkedList<>();

    @Getter
    private List<MetaPerson> addablePersondata = new LinkedList<>();
    @Getter
    private int pageNumber = 0;
    // new parameter image parameter for OpenSeadragon
    @Getter
    private int numberOfImagesPerPage = 96;
    private int thumbnailSizeInPixel = 200;
    @Getter
    @Setter
    private int pageNo = 0;
    @Getter
    private int imageIndex = 0;
    private String imageFolderName = "";
    @Getter
    private List<Image> allImages;
    @Getter
    private Image image = null;
    @Getter
    private int containerWidth = 600;;
    private boolean processHasNewTemporaryMetadataFiles = false;
    private boolean sizeChanged = false;
    private boolean noUpdateImageIndex = false;

    private List<String> normdataList = new ArrayList<>();

    @Getter
    @Setter
    private String gndSearchValue;
    @Getter
    @Setter
    private String geonamesSearchValue;
    @Getter
    @Setter
    private String searchOption;
    @Getter
    @Setter
    private String danteSearchValue;

    @Getter
    @Setter
    private String kulturnavSearchValue;

    @Getter
    private SearchableMetadata currentMetadataToPerformSearch;
    @Getter
    @Setter
    private boolean displayHiddenMetadata = false;

    @Getter
    @Setter
    private boolean pagesRTL = false;

    @Getter
    @Setter
    private String altoChanges;

    private List<SelectItem> addableMetadataTypes = new ArrayList<>();
    private List<SelectItem> addableCorporateTypes = new ArrayList<>();

    private List<ConfigOpacCatalogue> catalogues = null;
    private List<String> catalogueTitles;
    private ConfigOpacCatalogue currentCatalogue;

    @Getter
    @Setter
    private boolean doublePage;

    @Getter
    private String currentJsonAlto;

    public enum MetadataTypes {
        PERSON,
        CORPORATE,
        METATDATA
    }

    /**
     * Konstruktor ================================================================
     */
    public Metadaten() {
        this.treeProperties = new HashMap<>();
        LoginBean login = Helper.getLoginBean();
        if (login != null && login.getMyBenutzer() != null) {
            this.treeProperties.put("showtreelevel", login.getMyBenutzer().isMetsDisplayHierarchy());
            this.treeProperties.put("showtitle", login.getMyBenutzer().isMetsDisplayTitle());
            bildZuStrukturelement = login.getMyBenutzer().isMetsLinkImage();
            this.treeProperties.put("showfirstpagenumber", login.getMyBenutzer().isMetsDisplayPageAssignments());
            this.treeProperties.put("fullexpanded", Boolean.valueOf(true));
            this.treeProperties.put("showpagesasajax", Boolean.valueOf(false));
            this.treeProperties.put("showThumbnails", Boolean.valueOf(false));
        } else {
            this.treeProperties = new HashMap<>();
            this.treeProperties.put("showtreelevel", Boolean.valueOf(false));
            this.treeProperties.put("showtitle", Boolean.valueOf(false));
            this.treeProperties.put("fullexpanded", Boolean.valueOf(true));
            this.treeProperties.put("showfirstpagenumber", Boolean.valueOf(false));
            this.treeProperties.put("showpagesasajax", Boolean.valueOf(false));
            this.treeProperties.put("showThumbnails", Boolean.valueOf(false));
        }
        treeProperties.put("showMetadataPopup", ConfigurationHelper.getInstance().isMetsEditorShowMetadataPopup());
    }

    /**
     * die Anzeige der Details ändern (z.B. nur die Metadaten anzeigen, oder nur die Paginierungssequenzen)
     *
     * @return Navigationsanweisung "null" als String (also gleiche Seite reloaden)
     */
    public String AnsichtAendern() {
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String Hinzufuegen() {
        this.modusHinzufuegen = true;
        currentMetadataToPerformSearch = null;
        getMetadatum().setValue("");
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String AddGroup() {
        this.modeAddGroup = true;
        currentMetadataToPerformSearch = null;
        // reset selected groups
        selectedGroup = null;
        getSelectedGroup();

        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String HinzufuegenPerson() {
        this.modusHinzufuegenPerson = true;
        this.tempPersonNachname = "";
        this.tempPersonVorname = "";
        currentMetadataToPerformSearch = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String AddCorporate() {
        modeAddCorporate = true;
        currentMetadataToPerformSearch = null;
        tempCorporateMainName = null;
        tempCorporateSubName = null;
        tempCorporatePartName = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String Abbrechen() {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        this.modeAddGroup = false;
        modeAddCorporate = false;
        getMetadatum().setValue("");
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String Reload() {

        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        } else {
            try {
                processHasNewTemporaryMetadataFiles = false;
                setRepresentativeMetadata();
                this.myProzess.writeMetadataFile(this.gdzfile);

            } catch (Exception e) {
                Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
                logger.error(e);
            }
            MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
            return "";
        }
    }

    public void setRepresentativeMetadata() {
        DocStruct logical = document.getLogicalDocStruct();
        if (logical.getType().isAnchor()) {
            logical = logical.getAllChildren().get(0);
        }
        if (logical.getAllMetadata() != null) {
            boolean match = false;
            for (Metadata md : logical.getAllMetadata()) {
                if (md.getType().getName().equals("_directionRTL")) {
                    md.setValue(String.valueOf(this.pagesRTL));
                    match = true;
                }
            }
            if (!match) {
                MetadataType mdt = myPrefs.getMetadataTypeByName("_directionRTL");
                if (mdt != null) {
                    try {
                        Metadata md = new Metadata(mdt);
                        md.setValue(String.valueOf(this.pagesRTL));
                        logical.addMetadata(md);
                    } catch (MetadataTypeNotAllowedException e) {

                    }
                }
            }
        }
        boolean match = false;
        if (this.document.getPhysicalDocStruct() != null && this.document.getPhysicalDocStruct().getAllMetadata() != null
                && this.document.getPhysicalDocStruct().getAllMetadata().size() > 0) {
            for (Metadata md : this.document.getPhysicalDocStruct().getAllMetadata()) {
                if (md.getType().getName().equals("_representative")) {
                    if (StringUtils.isNotBlank(currentRepresentativePage)) {
                        Integer value = Integer.valueOf(currentRepresentativePage);
                        md.setValue(String.valueOf(value));
                    } else {
                        md.setValue("");
                    }
                    match = true;
                }
            }
        }
        if (!match) {
            MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
            try {
                Metadata md = new Metadata(mdt);
                if (StringUtils.isNotBlank(currentRepresentativePage)) {
                    Integer value = Integer.valueOf(currentRepresentativePage);
                    md.setValue(String.valueOf(value));
                } else {
                    md.setValue("");
                }
                this.document.getPhysicalDocStruct().addMetadata(md);
            } catch (MetadataTypeNotAllowedException e) {

            }

        }
    }

    public String toggleImageView() {
        pageNo = ((imageIndex) / numberOfImagesPerPage);
        return "";
    }

    //
    public String automaticSave() {

        try {
            processHasNewTemporaryMetadataFiles = true;
            updateRepresentativePage();
            this.myProzess.saveTemporaryMetsFile(this.gdzfile);

        } catch (Exception e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
        }
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        return "";

    }

    private MetadataGroup cloneMetadataGroup(MetadataGroup inGroup) throws MetadataTypeNotAllowedException {
        MetadataGroup mg = new MetadataGroup(inGroup.getType());
        // copy metadata
        for (Metadata md : inGroup.getMetadataList()) {
            Metadata metadata = new Metadata(md.getType());
            metadata.setValue(md.getValue());
            if (StringUtils.isNotBlank(md.getAuthorityValue())) {
                metadata.setAutorityFile(md.getAuthorityID(), md.getAuthorityURI(), md.getAuthorityValue());
            }
            mg.addMetadata(metadata);
        }

        // copy persons
        for (Person p : inGroup.getPersonList()) {
            Person person = new Person(p.getType());
            person.setFirstname(p.getFirstname());
            person.setLastname(p.getLastname());
            person.setAutorityFile(p.getAuthorityID(), p.getAuthorityURI(), p.getAuthorityValue());
            if (p.getAdditionalNameParts() != null && !p.getAdditionalNameParts().isEmpty()) {
                for (NamePart np : p.getAdditionalNameParts()) {
                    NamePart newNamePart = new NamePart(np.getType(), np.getValue());
                    person.addNamePart(newNamePart);
                }
            }
            mg.addPerson(person);
        }

        // copy corporations
        for (Corporate c : inGroup.getCorporateList()) {
            Corporate corporate = new Corporate(c.getType());
            corporate.setMainName(c.getMainName());
            if (c.getSubNames() != null) {
                for (NamePart subName : c.getSubNames()) {
                    corporate.addSubName(subName);
                }
            }
            corporate.setPartName(c.getPartName());
            if (c.getAuthorityID() != null && c.getAuthorityURI() != null && c.getAuthorityValue() != null) {
                corporate.setAutorityFile(c.getAuthorityID(), c.getAuthorityURI(), c.getAuthorityValue());
            }
            mg.addCorporate(corporate);
        }

        // copy sub groups
        for (MetadataGroup subGroup : inGroup.getAllMetadataGroups()) {
            MetadataGroup copyOfSubGroup = cloneMetadataGroup(subGroup);
            mg.addMetadataGroup(copyOfSubGroup);
        }

        return mg;
    }

    public String CopyGroup() {
        try {
            MetadataGroup newMetadataGroup = cloneMetadataGroup(currentGroup.getMetadataGroup());
            currentGroup.getMetadataGroup().getParent().addMetadataGroup(newMetadataGroup);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e);
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String Kopieren() {
        return Copy();
    }

    public String Copy() {
        Metadata md;
        try {
            md = new Metadata(currentMetadata.getType());

            md.setValue(currentMetadata.getValue());

            if (currentMetadata.getAuthorityID() != null && currentMetadata.getAuthorityURI() != null
                    && currentMetadata.getAuthorityValue() != null) {
                md.setAutorityFile(currentMetadata.getAuthorityID(), currentMetadata.getAuthorityURI(), currentMetadata.getAuthorityValue());
            }

            currentMetadata.getParent().addMetadata(md);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        currentMetadata = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String copyCorporate() {
        try {
            Corporate corporate = new Corporate(currentCorporate.getType());
            corporate.setMainName(currentCorporate.getMainName());
            if (currentCorporate.getSubNames() != null) {
                for (NamePart subName : currentCorporate.getSubNames()) {
                    corporate.addSubName(subName);
                }
            }
            corporate.setPartName(currentCorporate.getPartName());
            if (currentCorporate.getAuthorityID() != null && currentCorporate.getAuthorityURI() != null
                    && currentCorporate.getAuthorityValue() != null) {
                corporate.setAutorityFile(currentCorporate.getAuthorityID(), currentCorporate.getAuthorityURI(),
                        currentCorporate.getAuthorityValue());
            }

            currentCorporate.getParent().addCorporate(corporate);
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e);
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        currentCorporate = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String KopierenPerson() {
        return CopyPerson();
    }

    public String CopyPerson() {
        Person per;
        try {
            per = new Person(this.myPrefs.getMetadataTypeByName(currentPerson.getRole()));
            per.setFirstname(currentPerson.getFirstname());
            per.setLastname(currentPerson.getLastname());
            per.setRole(currentPerson.getRole());

            if (currentPerson.getAdditionalNameParts() != null && !currentPerson.getAdditionalNameParts().isEmpty()) {
                for (NamePart np : curPerson.getAdditionalNameParts()) {
                    NamePart newNamePart = new NamePart(np.getType(), np.getValue());
                    per.addNamePart(newNamePart);
                }
            }
            if (currentPerson.getAuthorityID() != null && currentPerson.getAuthorityURI() != null && currentPerson.getAuthorityValue() != null) {
                per.setAutorityFile(currentPerson.getAuthorityID(), currentPerson.getAuthorityURI(), currentPerson.getAuthorityValue());
            }

            currentPerson.getParent().addPerson(per);
        } catch (IncompletePersonObjectException e) {
            logger.error("Fehler beim Kopieren von Personen (IncompletePersonObjectException): " + e.getMessage());
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Fehler beim Kopieren von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String ChangeCurrentDocstructType() {

        if (this.myDocStruct != null && this.tempWert != null) {
            try {
                DocStruct rueckgabe = this.metahelper.ChangeCurrentDocstructType(this.myDocStruct, this.tempWert);
                MetadatenalsBeanSpeichern(rueckgabe);
                MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
            } catch (DocStructHasNoTypeException | MetadataTypeNotAllowedException | TypeNotAllowedAsChildException
                    | TypeNotAllowedForParentException e) {
                Helper.setFehlerMeldung("Error while changing DocStructTypes: ", e.getMessage());
                logger.error("Error while changing DocStructTypes: " + e);
            }
        }
        return "metseditor";
    }

    public String addNewMetadata() {
        try {
            Metadata md = new Metadata(this.myPrefs.getMetadataTypeByName(this.tempTyp));
            md.setValue(this.selectedMetadatum.getValue());

            if (StringUtils.isNotBlank(selectedMetadatum.getNormdataValue())) {
                md.setAutorityFile(selectedMetadatum.getMd().getAuthorityID(), selectedMetadatum.getMd().getAuthorityURI(),
                        selectedMetadatum.getMd().getAuthorityValue());
            }
            if (currentGroup != null) {
                currentGroup.getMetadataGroup().addMetadata(md);
            } else {
                this.myDocStruct.addMetadata(md);
                /*
                 * wenn TitleDocMain, dann gleich Sortiertitel mit gleichem Inhalt anlegen
                 */
                if (this.tempTyp.equals("TitleDocMain") && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
                    try {
                        Metadata md2 = new Metadata(this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                        md2.setValue(this.selectedMetadatum.getValue());
                        this.myDocStruct.addMetadata(md2);
                    } catch (MetadataTypeNotAllowedException e) {
                        logger.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
                    }
                }
            }
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        this.modusHinzufuegen = false;
        this.selectedMetadatum.setValue("");
        this.tempWert = "";
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        return "";
    }

    public String addNewCorporate() {
        try {
            Corporate corporate = new Corporate(myPrefs.getMetadataTypeByName(tempPersonRolle));
            corporate.setMainName(tempCorporateMainName);

            if (StringUtils.isNotBlank(tempCorporateSubName)) {
                corporate.addSubName(new NamePart("subname", tempCorporateSubName));
            }
            corporate.setPartName(tempCorporatePartName);
            tempCorporateMainName = null;
            tempCorporateSubName = null;
            tempCorporatePartName = null;
            if (currentGroup != null) {
                currentGroup.getMetadataGroup().addCorporate(corporate);
            } else {
                this.myDocStruct.addCorporate(corporate);
            }
        } catch (IncompletePersonObjectException e) {
            Helper.setFehlerMeldung("Incomplete data for person", "");

            return "";
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setFehlerMeldung("Person is for this structure not allowed", "");
            return "";
        }
        this.modeAddCorporate = false;
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String saveGroup() {
        try {
            MetadataGroup md = new MetadataGroup(this.myPrefs.getMetadataGroupTypeByName(this.tempGroupType));
            for (MetadatumImpl mdi : selectedGroup.getMetadataList()) {
                if (StringUtils.isNotBlank(mdi.getMd().getValue())) {
                    Metadata metadata = new Metadata(mdi.getMd().getType());
                    metadata.setValue(mdi.getMd().getValue());
                    md.addMetadata(metadata);
                }
            }
            for (MetaPerson mp : selectedGroup.getPersonList()) {
                Person p = new Person(mp.getP().getType());
                p.setFirstname(mp.getP().getFirstname());
                p.setLastname(mp.getP().getLastname());
                p.setAutorityFile(mp.getP().getAuthorityID(), mp.getP().getAuthorityURI(), mp.getP().getAuthorityValue());
                md.addPerson(p);
            }
            for (MetaCorporate mc : selectedGroup.getCorporateList()) {
                Corporate corporate = new Corporate(mc.getCorporate().getType());
                corporate.setMainName(mc.getCorporate().getMainName());
                corporate.setSubNames(mc.getCorporate().getSubNames());
                corporate.setPartName(mc.getCorporate().getPartName());
                md.addCorporate(currentCorporate);
            }
            if (currentGroup != null) {
                currentGroup.getMetadataGroup().addMetadataGroup(md);
            } else {
                this.myDocStruct.addMetadataGroup(md);
            }
        } catch (MetadataTypeNotAllowedException e) {
            logger.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        this.modeAddGroup = false;

        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        return "";
    }

    public String loadRightFrame() {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        if (neuesElementWohin.equals("3") || neuesElementWohin.equals("4")) {
            if (!docStructIsAllowed(getAddableDocStructTypenAlsKind(), getAddDocStructType2())) {
                setAddDocStructType2("");
            }

        } else {

            if (!docStructIsAllowed(getAddableDocStructTypenAlsNachbar(), getAddDocStructType1())) {
                setAddDocStructType1("");
            }
        }
        if (!tempMetadatumList.isEmpty()) {
            tempTyp = tempMetadatumList.get(0).getMd().getType().getName();
            selectedMetadatum = tempMetadatumList.get(0);
        }

        return "metseditor";
    }

    private boolean docStructIsAllowed(SelectItem[] itemList, String docstruct) {
        if (itemList != null && itemList.length > 0) {
            for (SelectItem item : itemList) {
                DocStructType type = (DocStructType) item.getValue();
                if (type.getName().equals(docstruct)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String addNewPerson() {
        try {
            Person per = new Person(this.myPrefs.getMetadataTypeByName(this.tempPersonRolle));
            per.setFirstname(this.tempPersonVorname);
            per.setLastname(this.tempPersonNachname);
            per.setRole(this.tempPersonRolle);
            if (currentGroup != null) {
                currentGroup.getMetadataGroup().addPerson(per);
            } else {
                this.myDocStruct.addPerson(per);
            }
        } catch (IncompletePersonObjectException e) {
            Helper.setFehlerMeldung("Incomplete data for person", "");

            return "";
        } catch (MetadataTypeNotAllowedException e) {
            Helper.setFehlerMeldung("Person is for this structure not allowed", "");
            return "";
        }
        this.modusHinzufuegenPerson = false;
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String deleteGroup() {
        MetadataGroup mg = currentGroup.getMetadataGroup();
        mg.getParent().removeMetadataGroup(mg, true);
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String Loeschen() {
        HoldingElement he = curMetadatum.getMd().getParent();
        if (he != null) {
            he.removeMetadata(curMetadatum.getMd(), true);
        } else {
            // we have a default metadata field, clear it
            curMetadatum.setValue("");
            curMetadatum.setNormdataValue("");
            curMetadatum.setNormDatabase("");
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String delete() {
        HoldingElement he = currentMetadata.getParent();
        if (he != null) {
            he.removeMetadata(currentMetadata, true);
        } else {
            // we have a default metadata field, clear it
            currentMetadata.setValue("");
            currentMetadata.setAutorityFile("", "", "");
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String deletePerson() {
        HoldingElement he = currentPerson.getParent();
        if (he != null) {
            he.removePerson(currentPerson, false);
        } else {
            // we have a default field, clear it
            currentPerson.setFirstname("");
            currentPerson.setLastname("");
            currentPerson.setAutorityFile("", "", "");
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String LoeschenPerson() {
        HoldingElement he = curPerson.getP().getParent();
        if (he != null) {
            he.removePerson(this.curPerson.getP(), false);
        } else {
            // we have a default field, clear it
            curPerson.setVorname("");
            curPerson.setNachname("");
            curPerson.setNormdataValue("");
            curPerson.setNormDatabase("");
        }

        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String deleteCorporate() {
        HoldingElement he = currentCorporate.getParent();
        if (he != null) {
            he.removeCorporate(currentCorporate, false);
        } else {
            // we have a default field, clear it
            currentCorporate.setValue("");
            currentCorporate.setAutorityFile("", "", "");
        }
        MetadatenalsBeanSpeichern(myDocStruct);
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    /**
     * die noch erlaubten Rollen zurückgeben ================================================================
     */
    public List<SelectItem> getAddableRollen() {
        if (currentGroup != null) {
            return currentGroup.getAddablePersons();
        }

        return this.metahelper.getAddablePersonRoles(this.myDocStruct, "");
    }

    public List<SelectItem> getAddableCorporateRoles() {
        if (currentGroup != null) {
            return currentGroup.getAddableCorporations();
        }

        return this.metahelper.getAddableCorporateRoles(myDocStruct, "");
    }

    public int getSizeOfRoles() {
        try {
            return getAddableRollen().size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setSizeOfRoles(int i) {
        // do nothing, needed for jsp only
    }

    public int getSizeOfMetadata() {
        try {
            return getAddableMetadataTypes().size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public int getSizeOfCorporates() {
        try {
            return getAddableCorporateTypes().size();
        } catch (NullPointerException e) {
            return 0;
        }
    }

    public void setSizeOfMetadata(int i) {
        // do nothing, needed for jsp only
    }

    public int getSizeOfMetadataGroups() {

        List<MetadataGroupType> types = this.myDocStruct.getAddableMetadataGroupTypes();
        if (types == null) {
            return 0;
        }
        return types.size();
    }

    public void setSizeOfMetadataGroups(int i) {
        // do nothing, needed for jsp only
    }

    /**
     * die noch erlaubten Metadaten zurückgeben ================================================================
     */

    public List<SelectItem> getAddableMetadataTypes() {
        if (currentGroup != null) {
            return currentGroup.getAddableMetadata();
        }

        if (addableMetadataTypes.isEmpty()) {
            addableMetadataTypes = createAddableMetadataTypes();
        }
        return addableMetadataTypes;
    }

    private List<SelectItem> createAddableMetadataTypes() {
        ArrayList<SelectItem> myList = new ArrayList<>();
        /*
         * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = this.myDocStruct.getAddableMetadataTypes(false);

        if (types == null) {
            return myList;
        }

        /*
         * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
         */
        for (MetadataType mdt : new ArrayList<>(types)) {
            if (mdt.getIsPerson() || mdt.isCorporate()) {
                types.remove(mdt);
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        int counter = types.size();

        for (MetadataType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), this.metahelper.getMetadatatypeLanguage(mdt)));
            try {
                Metadata md = new Metadata(mdt);
                MetadatumImpl mdum = new MetadatumImpl(md, counter, this.myPrefs, this.myProzess, this);
                counter++;
                this.tempMetadatumList.add(mdum);

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        if (StringUtils.isBlank(tempTyp) && !tempMetadatumList.isEmpty()) {
            tempTyp = tempMetadatumList.get(0).getMd().getType().getName();
            selectedMetadatum = tempMetadatumList.get(0);
        }
        return myList;
    }

    public List<SelectItem> getAddableCorporateTypes() {
        if (addableCorporateTypes.isEmpty()) {
            addableCorporateTypes = createAddableCorporateTypes();
        }
        return addableCorporateTypes;
    }

    private List<SelectItem> createAddableCorporateTypes() {
        ArrayList<SelectItem> myList = new ArrayList<>();
        /*
         * -------------------------------- zuerst mal alle addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = this.myDocStruct.getAddableMetadataTypes(false);
        if (types == null) {
            return myList;
        }

        /*
         * --------------------- alle Metadatentypen, die keine Person sind, oder mit einem Unterstrich anfangen rausnehmen -------------------
         */
        for (MetadataType mdt : new ArrayList<>(types)) {
            if (!mdt.isCorporate()) {
                types.remove(mdt);
            }
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        int counter = types.size();

        for (MetadataType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), this.metahelper.getMetadatatypeLanguage(mdt)));
            try {
                Metadata md = new Metadata(mdt);
                MetadatumImpl mdum = new MetadatumImpl(md, counter, this.myPrefs, this.myProzess, this);
                counter++;
                this.tempMetadatumList.add(mdum);

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        if (StringUtils.isBlank(tempTyp) && !tempMetadatumList.isEmpty()) {
            tempTyp = tempMetadatumList.get(0).getMd().getType().getName();
            selectedMetadatum = tempMetadatumList.get(0);
        }
        return myList;
    }

    public List<SelectItem> getAddableMetadataGroupTypes() {
        List<SelectItem> myList = new ArrayList<>();
        List<MetadataGroupType> types = null;

        if (currentGroup != null) {

            List<String> groupNames = currentGroup.getMetadataGroup().getAddableMetadataGroupTypes();
            if (groupNames != null) {
                types = new ArrayList<>();
                for (String groupName : groupNames) {
                    MetadataGroupType mgt = myPrefs.getMetadataGroupTypeByName(groupName);
                    types.add(mgt);
                }
            }

        } else {
            types = this.myDocStruct.getAddableMetadataGroupTypes();
        }

        if (types == null) {
            return myList;
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenGroupTypes");
        Collections.sort(types, c);

        for (MetadataGroupType mdt : types) {
            myList.add(new SelectItem(mdt.getName(), this.metahelper.getMetadataGroupTypeLanguage(mdt)));
            try {
                MetadataGroup md = new MetadataGroup(mdt);
                MetadataGroupImpl mdum = new MetadataGroupImpl(myPrefs, myProzess, md, this, metahelper.getMetadataGroupTypeLanguage(mdt), null, 0);
                this.tempMetadataGroups.add(mdum);
                // TODO initialize all fields

            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
            }
        }
        return myList;
    }

    public List<MetadataGroupImpl> getTempMetadataGroupList() {
        return this.tempMetadataGroups;
    }

    public void setTempMetadataGroupList(List<MetadataGroupImpl> tempMetadatumList) {
        this.tempMetadataGroups = tempMetadatumList;
    }

    /**
     * die MetadatenTypen zurückgeben ================================================================
     */
    public SelectItem[] getMetadatenTypen() {
        /*
         * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataType> types = this.myDocStruct.getAddableMetadataTypes(false);

        if (types == null) {
            return new SelectItem[0];
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenTypen");
        Collections.sort(types, c);

        /*
         * -------------------------------- nun ein Array mit der richtigen Größe anlegen --------------------------------
         */
        int zaehler = types.size();
        SelectItem myTypen[] = new SelectItem[zaehler];

        /*
         * -------------------------------- und anschliessend alle Elemente in das Array packen --------------------------------
         */
        zaehler = 0;
        for (MetadataType mdt : types) {

            myTypen[zaehler] = new SelectItem(mdt.getName(), this.metahelper.getMetadatatypeLanguage(mdt));
            zaehler++;

        }

        /*
         * -------------------------------- alle Typen, die einen Unterstrich haben nochmal rausschmeissen --------------------------------
         */
        SelectItem myTypenOhneUnterstrich[] = new SelectItem[zaehler];
        for (int i = 0; i < zaehler; i++) {
            myTypenOhneUnterstrich[i] = myTypen[i];
        }
        return myTypenOhneUnterstrich;
    }

    public SelectItem[] getMetadataGroupTypes() {
        /*
         * -------------------------------- zuerst mal die addierbaren Metadatentypen ermitteln --------------------------------
         */
        List<MetadataGroupType> types = this.myDocStruct.getAddableMetadataGroupTypes();

        if (types == null) {
            return new SelectItem[0];
        }

        /*
         * -------------------------------- die Metadatentypen sortieren --------------------------------
         */
        HelperComparator c = new HelperComparator();
        c.setSortierart("MetadatenGroupTypes");
        Collections.sort(types, c);

        /*
         * -------------------------------- nun ein Array mit der richtigen Größe anlegen --------------------------------
         */
        int zaehler = types.size();
        SelectItem myTypen[] = new SelectItem[zaehler];

        /*
         * -------------------------------- und anschliessend alle Elemente in das Array packen --------------------------------
         */
        zaehler = 0;
        for (MetadataGroupType mdt : types) {

            myTypen[zaehler] = new SelectItem(mdt.getName(), this.metahelper.getMetadataGroupTypeLanguage(mdt));
            zaehler++;

        }

        /*
         * -------------------------------- alle Typen, die einen Unterstrich haben nochmal rausschmeissen --------------------------------
         */
        SelectItem myTypenOhneUnterstrich[] = new SelectItem[zaehler];
        for (int i = 0; i < zaehler; i++) {
            myTypenOhneUnterstrich[i] = myTypen[i];
        }
        return myTypenOhneUnterstrich;
    }

    /*
     * ##################################################### ##################################################### ## ## Metadaten lesen und schreiben
     * ## ##################################################### ####################################################
     */

    /**
     * Metadaten Einlesen
     *
     */

    public String XMLlesen() {
        // myBild="";
        // this.myBildNummer = 1;

        String result = "";

        result = readXmlAndBuildTree();

        return result;
    }

    private String readXmlAndBuildTree() {

        /*
         * re-reading the config for display rules
         */
        ConfigDisplayRules.getInstance().refresh();

        try {
            Integer id = Integer.valueOf(Helper.getRequestParameter("ProzesseID"));
            this.myProzess = ProcessManager.getProcessById(id);
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
            return Helper.getRequestParameter("zurueck");
            // } catch (DAOException e1) {
            // Helper.setFehlerMeldung("error while loading process data" + e1.getMessage());
            // return Helper.getRequestParameter("zurueck");
        }
        processHasNewTemporaryMetadataFiles = false;
        this.myBenutzerID = Helper.getRequestParameter("BenutzerID");
        this.alleSeitenAuswahl_ersteSeite = "";
        this.alleSeitenAuswahl_letzteSeite = "";
        this.zurueck = Helper.getRequestParameter("zurueck");
        this.nurLesenModus = Helper.getRequestParameter("nurLesen").equals("true") ? true : false;
        this.neuesElementWohin = "4";
        this.tree3 = null;
        image = null;
        myBild = null;
        dataList = null;
        treeProperties.put("showThumbnails", false);
        treeProperties.put("showOcr", false);
        if (Helper.getRequestParameter("discardChanges").equals("true")) {
            myProzess.removeTemporaryMetadataFiles();
        } else if (Helper.getRequestParameter("overwriteChanges").equals("true")) {
            myProzess.overwriteMetadata();
            myProzess.removeTemporaryMetadataFiles();
        }

        try {
            String returnvalue = XMLlesenStart();
            if (returnvalue == null) {
                return "";
            }
        } catch (SwapException e) {
            Helper.setFehlerMeldung(e);
            return Helper.getRequestParameter("zurueck");
        } catch (ReadException e) {
            Helper.setFehlerMeldung(e.getMessage());
            return Helper.getRequestParameter("zurueck");
        } catch (PreferencesException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter("zurueck");
        } catch (WriteException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter("zurueck");
        } catch (IOException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter("zurueck");
        } catch (InterruptedException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter("zurueck");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter("zurueck");
        }
        getAddDocStructType2();
        createAddableData();

        TreeExpand();
        this.sperrung.setLocked(this.myProzess.getId().intValue(), this.myBenutzerID);
        return "metseditor";
    }

    /**
     * Metadaten Einlesen
     *
     * @throws ReadException
     * @throws InterruptedException
     * @throws IOException
     * @throws PreferencesException ============================================================ == ==
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */

    public String XMLlesenStart()
            throws ReadException, IOException, InterruptedException, PreferencesException, SwapException, DAOException, WriteException {
        currentRepresentativePage = "";
        this.myPrefs = this.myProzess.getRegelsatz().getPreferences();
        enablePageArea = myPrefs.getDocStrctTypeByName("area") != null;
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        this.modusStrukturelementVerschieben = false;
        this.modusCopyDocstructFromOtherProcess = false;

        this.currentTifFolder = null;
        readAllTifFolders();
        /*
         * -------------------------------- Dokument einlesen --------------------------------
         */
        this.gdzfile = this.myProzess.readMetadataFile();
        if (gdzfile == null) {
            return null;
        }
        this.document = this.gdzfile.getDigitalDocument();

        this.document.addAllContentFiles();
        this.metahelper = new MetadatenHelper(this.myPrefs, this.document);
        this.imagehelper = new MetadatenImagesHelper(this.myPrefs, this.document);

        /*
         * -------------------------------- Das Hauptelement ermitteln --------------------------------
         */

        this.logicalTopstruct = this.document.getLogicalDocStruct();

        // this exception needs some serious feedback because data is corrupted
        if (this.logicalTopstruct == null) {
            throw new ReadException(Helper.getTranslation("metaDataError"));
        }
        checkImageNames();
        retrieveAllImages();
        // check filenames, correct them

        // initialize image list
        numberOfImagesPerPage = ConfigurationHelper.getInstance().getMetsEditorNumberOfImagesPerPage();
        thumbnailSizeInPixel = ConfigurationHelper.getInstance().getMetsEditorThumbnailSize();

        pageNo = 0;
        imageIndex = 0;
        loadCurrentImages(true);

        if (this.document.getPhysicalDocStruct().getAllMetadata() != null && this.document.getPhysicalDocStruct().getAllMetadata().size() > 0) {

            List<Metadata> lstMetadata = this.document.getPhysicalDocStruct().getAllMetadata();
            for (Metadata md : lstMetadata) {
                if (md.getType().getName().equals("_representative")) {
                    try {
                        Integer value = Integer.valueOf(md.getValue());
                        currentRepresentativePage = String.valueOf(value);
                        updateRepresentativePage();
                    } catch (Exception e) {

                    }
                }
            }
            DocStruct docstruct = logicalTopstruct;
            if (docstruct.getType().isAnchor()) {
                docstruct = docstruct.getAllChildren().get(0);
            }
            lstMetadata = docstruct.getAllMetadata();
            for (Metadata md : lstMetadata) {
                if (md.getType().getName().equals("_directionRTL")) {
                    try {
                        Boolean value = Boolean.valueOf(md.getValue());
                        this.pagesRTL = value;
                    } catch (Exception e) {

                    }
                }
            }
        }

        createDefaultValues(this.logicalTopstruct);
        MetadatenalsBeanSpeichern(this.logicalTopstruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.logicalTopstruct, false);
        physicalTopstruct = document.getPhysicalDocStruct();
        currentTopstruct = logicalTopstruct;
        if (this.nurLesenModus || allImages.isEmpty()) {
            // inserted to make Paginierung the starting view
            this.modusAnsicht = "Metadaten";
        }
        return "metseditor";
    }

    private void loadCurrentImages(boolean jumpToFirstPage) {
        allImages = new ArrayList<>();
        try {
            List<String> imageNames = imagehelper.getImageFiles(myProzess, currentTifFolder);
            if (imageNames != null && !imageNames.isEmpty()) {
                imageFolderName = myProzess.getImagesDirectory() + currentTifFolder + File.separator;
                imageFolderName = imageFolderName.replaceAll("\\\\", "/");
                int order = 1;
                for (String imagename : imageNames) {
                    Image image = new Image(myProzess, imageFolderName, imagename, order++, thumbnailSizeInPixel);
                    allImages.add(image);
                }
                if (jumpToFirstPage) {
                    setImageIndex(0);
                }
            }
        } catch (InvalidImagesException | SwapException | DAOException | IOException | InterruptedException e1) {
            logger.error(e1);
        }
    }

    /**
     * navigate one image to the right
     */
    public void imageRight() {
        if (pagesRTL) {
            setImageIndex(imageIndex - 1);
        } else {
            setImageIndex(imageIndex + 1);
        }
    }

    /**
     * navigate two images to the right
     */
    public void imageRight2() {
        imageRight();
        imageRight();
    }

    /**
     * navigate one image to the left
     */
    public void imageLeft() {
        if (pagesRTL) {
            setImageIndex(imageIndex + 1);
        } else {
            setImageIndex(imageIndex - 1);
        }
    }

    /**
     * navigate two images to the left
     */
    public void imageLeft2() {
        imageLeft();
        imageLeft();
    }

    /**
     * navigate to most left image
     */
    public void imageLeftmost() {
        if (pagesRTL) {
            setImageIndex(getSizeOfImageList() - 1);
        } else {
            setImageIndex(0);
        }
    }

    /**
     * navigate to most right image
     */
    public void imageRightmost() {
        if (pagesRTL) {
            setImageIndex(0);
        } else {
            setImageIndex(getSizeOfImageList() - 1);
        }
    }

    private void createDefaultValues(DocStruct element) {
        if (ConfigurationHelper.getInstance().isMetsEditorEnableDefaultInitialisation()) {
            MetadatenalsBeanSpeichern(element);
            if (element.getAllChildren() != null && element.getAllChildren().size() > 0) {
                for (DocStruct ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }

    public boolean isCheckForRepresentative() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
        if (mdt != null) {
            return true;
        }
        return false;
    }

    public boolean isCheckForReadingDirection() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_directionRTL");
        if (mdt != null) {
            return true;
        }
        return false;
    }

    /**
     * Metadaten Schreiben
     *
     * @throws InterruptedException
     * @throws IOException ============================================================ == ==
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     * @throws PreferencesException
     */
    public String XMLschreiben() {

        XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

        this.myProzess.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.DOCSTRUCT));
        this.myProzess.setSortHelperMetadata(zaehlen.getNumberOfUghElements(this.logicalTopstruct, CountType.METADATA));
        try {
            this.myProzess
            .setSortHelperImages(StorageProvider.getInstance().getNumberOfFiles(Paths.get(this.myProzess.getImagesOrigDirectory(true))));
            ProcessManager.saveProcess(this.myProzess);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            logger.error(e);
        } catch (Exception e) {
            Helper.setFehlerMeldung("error while counting current images", e);
            logger.error(e);
        }
        /* xml-Datei speichern */
        // MetadatenDebuggen(gdzfile.getDigitalDocument().getLogicalDocStruct());
        /*
         * --------------------- vor dem Speichern alle ungenutzen Docstructs rauswerfen -------------------
         */
        this.metahelper.deleteAllUnusedElements(this.document.getLogicalDocStruct());
        updateRepresentativePage();

        //reading direction
        setRepresentativeMetadata();

        try {
            // if (!new MetadatenVerifizierungWithoutHibernate().validateIdentifier(gdzfile.getDigitalDocument().getLogicalDocStruct())) {
            // return false;
            // }
            this.myProzess.writeMetadataFile(this.gdzfile);
        } catch (Exception e) {
            Helper.setFehlerMeldung("Metafile is not writable.", e);
            logger.error(e);
            return "Metadaten";
        }
        myProzess.removeTemporaryMetadataFiles();

        SperrungAufheben();
        return this.zurueck;
    }

    /**
     * vom aktuellen Strukturelement alle Metadaten einlesen
     *
     * @param inStrukturelement ============================================================== ==
     */

    private void MetadatenalsBeanSpeichern(DocStruct inStrukturelement) {
        this.myDocStruct = inStrukturelement;
        addableMetadataTypes.clear();
        groups.clear();

        LinkedList<MetadatumImpl> lsMeta = new LinkedList<>();
        LinkedList<MetaCorporate> lsCorp = new LinkedList<>();
        LinkedList<MetaPerson> lsPers = new LinkedList<>();
        List<MetadataGroupImpl> metaGroups = new LinkedList<>();
        /*
         * -------------------------------- alle Metadaten und die DefaultDisplay-Werte anzeigen --------------------------------
         */
        List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(inStrukturelement, Helper.getMetadataLanguage(),
                MetadataTypes.METATDATA, this.myProzess, displayHiddenMetadata);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.myProzess, this);
                meta.getSelectedItem();
                lsMeta.add(meta);
            }
        }

        /*
         * -------------------------------- alle Personen und die DefaultDisplay-Werte ermitteln --------------------------------
         */
        myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(inStrukturelement, Helper.getMetadataLanguage(), MetadataTypes.PERSON,
                this.myProzess, displayHiddenMetadata);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                lsPers.add(new MetaPerson((Person) metadata, 0, this.myPrefs, inStrukturelement, myProzess, this));
            }
        }

        //                corporates
        myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(inStrukturelement, Helper.getMetadataLanguage(), MetadataTypes.CORPORATE,
                this.myProzess, displayHiddenMetadata);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                lsCorp.add(new MetaCorporate((Corporate) metadata, myPrefs, inStrukturelement, myProzess, this));
            }
        }

        List<MetadataGroup> groups =
                this.metahelper.getMetadataGroupsInclDefaultDisplay(inStrukturelement, Helper.getMetadataLanguage(), this.myProzess);
        if (groups != null) {
            int counter = 1;
            for (MetadataGroup mg : groups) {
                metaGroups.add(new MetadataGroupImpl(myPrefs, myProzess, mg, this, "" + counter++, null, 0));
            }
        }

        corporates = lsCorp;
        this.myMetadaten = lsMeta;
        this.myPersonen = lsPers;
        for (MetadataGroupImpl impl : metaGroups) {
            this.groups.addAll(impl.getAsFlatList());

        }

        /*
         * -------------------------------- die zugehörigen Seiten ermitteln --------------------------------
         */
        StructSeitenErmitteln(this.myDocStruct);
    }

    /*
     * ##################################################### ##################################################### ## ## Treeview ##
     * ##################################################### ####################################################
     */

    private String MetadatenalsTree3Einlesen1(TreeNodeStruct3 inTree, DocStruct inLogicalTopStruct, boolean expandAll) {
        this.tree3 = buildTree(inTree, inLogicalTopStruct, expandAll);

        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        pageAreaEditionMode = false;
        return "metseditor";
    }

    @SuppressWarnings("rawtypes")
    private TreeNodeStruct3 buildTree(TreeNodeStruct3 inTree, DocStruct inLogicalTopStruct, boolean expandAll) {
        HashMap map;
        TreeNodeStruct3 knoten;
        List<DocStruct> status = new ArrayList<>();

        /*
         * -------------------------------- den Ausklapp-Zustand aller Knoten erfassen --------------------------------
         */
        if (inTree != null) {
            for (Object element : inTree.getChildrenAsList()) {
                map = (HashMap) element;
                knoten = (TreeNodeStruct3) map.get("node");
                if (knoten.isExpanded()) {
                    status.add(knoten.getStruct());
                }
            }
        }

        if (inLogicalTopStruct == null) {
            return inTree;
        }
        /*
         * -------------------------------- Die Struktur als Tree3 aufbereiten --------------------------------
         */
        String label = inLogicalTopStruct.getType().getNameByLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = inLogicalTopStruct.getType().getName();
        }

        inTree = new TreeNodeStruct3(label, inLogicalTopStruct);
        MetadatenalsTree3Einlesen2(inLogicalTopStruct, inTree);

        /*
         * -------------------------------- den Ausklappzustand nach dem neu-Einlesen wieder herstellen --------------------------------
         */
        for (Object element : inTree.getChildrenAsListAlle()) {
            map = (HashMap) element;
            knoten = (TreeNodeStruct3) map.get("node");
            // Ausklappstatus wiederherstellen
            if (status.contains(knoten.getStruct()) || expandAll) {
                knoten.setExpanded(true);
            }
            // Selection wiederherstellen
            if (this.myDocStruct == knoten.getStruct()) {
                knoten.setSelected(true);
            }
        }
        return inTree;
    }

    /**
     * Metadaten in Tree3 ausgeben
     *
     * @param inStrukturelement ============================================================== ==
     */
    private void MetadatenalsTree3Einlesen2(DocStruct inStrukturelement, TreeNodeStruct3 OberKnoten) {
        if (currentTopstruct != null && currentTopstruct.getType().getName().equals("BoundBook")) {
            if (inStrukturelement.getAllMetadata() != null) {
                String phys = "";
                String log = "";
                for (Metadata md : inStrukturelement.getAllMetadata()) {
                    OberKnoten.addMetadata(md.getType().getLanguage(Helper.getMetadataLanguage()), md.getValue());
                    if (md.getType().getName().equals("logicalPageNumber")) {
                        log = md.getValue();
                    }
                    if (md.getType().getName().equals("physPageNumber")) {
                        phys = md.getValue();
                    }
                }
                if (phys != null && phys.length() > 0) {
                    OberKnoten.setFirstImage(new MutablePair<>(phys, log));
                }
            }
        } else {
            String mainTitle = MetadatenErmitteln(inStrukturelement, "TitleDocMain");
            OberKnoten.setMainTitle(mainTitle);
            OberKnoten.addMetadata(Helper.getTranslation("haupttitel"), mainTitle);
            OberKnoten.addMetadata(Helper.getTranslation("identifier"), MetadatenErmitteln(inStrukturelement, "IdentifierDigital"));
            MutablePair<String, String> first = this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_FIRST);
            if (first != null) {
                OberKnoten.setFirstImage(first);
                OberKnoten.addMetadata(Helper.getTranslation("firstImage"),
                        OberKnoten.getFirstImage().getLeft() + ":" + OberKnoten.getFirstImage().getRight());
            }
            MutablePair<String, String> last = this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_LAST);
            if (last != null) {
                OberKnoten.setLastImage(last);
                OberKnoten.addMetadata(Helper.getTranslation("lastImage"),
                        OberKnoten.getLastImage().getLeft() + ":" + OberKnoten.getLastImage().getRight());
            }
            OberKnoten.addMetadata(Helper.getTranslation("partNumber"), MetadatenErmitteln(inStrukturelement, "PartNumber"));
            OberKnoten.addMetadata(Helper.getTranslation("dateIssued"), MetadatenErmitteln(inStrukturelement, "DateIssued"));
        }
        // wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
        if (inStrukturelement.getType().getName().equals("Periodical") || inStrukturelement.getType().getName().equals("PeriodicalVolume")) {
            OberKnoten.setExpanded(true);
        }

        /*
         * -------------------------------- vom aktuellen Strukturelement alle Kinder in den Tree packen --------------------------------
         */
        List<DocStruct> meineListe = inStrukturelement.getAllChildren();
        if (meineListe != null) {
            /* es gibt Kinder-Strukturelemente */
            for (DocStruct kind : meineListe) {
                String label = kind.getType().getNameByLanguage(Helper.getMetadataLanguage());
                if (label == null) {
                    label = kind.getType().getName();
                }
                TreeNodeStruct3 tns = new TreeNodeStruct3(label, kind);
                OberKnoten.addChild(tns);
                MetadatenalsTree3Einlesen2(kind, tns);
            }
        }
    }

    /**
     * Metadaten gezielt zurückgeben
     *
     * @param inStrukturelement ============================================================== ==
     */
    private String MetadatenErmitteln(DocStruct inStrukturelement, String inTyp) {
        String rueckgabe = "";
        List<Metadata> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                if (md.getType().getName().equals(inTyp)) {
                    rueckgabe += (md.getValue() == null ? "" : md.getValue()) + " ";
                }
            }
        }
        return rueckgabe.trim();
    }

    @SuppressWarnings("rawtypes")
    public void setMyStrukturelement(DocStruct inStruct) {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        MetadatenalsBeanSpeichern(inStruct);

        /*
         * -------------------------------- die Selektion kenntlich machen --------------------------------
         */
        for (Object element : this.tree3.getChildrenAsListAlle()) {

            HashMap map = (HashMap) element;
            TreeNodeStruct3 knoten = (TreeNodeStruct3) map.get("node");
            // Selection wiederherstellen
            if (this.myDocStruct == knoten.getStruct()) {
                knoten.setSelected(true);
            } else {
                knoten.setSelected(false);
            }
        }

        SperrungAktualisieren();
    }

    /**
     * Knoten nach oben schieben ================================================================
     */
    public String KnotenUp() {
        try {
            this.metahelper.KnotenUp(this.myDocStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    /**
     * Knoten nach unten schieben ================================================================
     */
    public String KnotenDown() {
        try {
            this.metahelper.KnotenDown(this.myDocStruct);
        } catch (TypeNotAllowedAsChildException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
            }
        }
        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    /**
     * Knoten zu einer anderen Stelle
     *
     * @throws TypeNotAllowedAsChildException ============================================================ == ==
     */
    public String KnotenVerschieben() throws TypeNotAllowedAsChildException {
        this.myDocStruct.getParent().removeChild(this.myDocStruct);
        this.tempStrukturelement.addChild(this.myDocStruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        this.neuesElementWohin = "1";
        return "metseditor";
    }

    /**
     * Knoten nach oben schieben
     *
     * @throws IOException ============================================================ == ==
     */
    public String KnotenDelete() throws IOException {
        if (this.myDocStruct != null && this.myDocStruct.getParent() != null) {
            DocStruct tempParent = this.myDocStruct.getParent().getPreviousChild(this.myDocStruct);
            if (tempParent == null) {
                tempParent = this.myDocStruct;
            }

            this.myDocStruct.getParent().removeChild(this.myDocStruct);
            this.myDocStruct = tempParent;

        }
        // den Tree neu einlesen
        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    public String duplicateNode() {
        if (myDocStruct != null && myDocStruct.getParent() != null) {
            List<DocStruct> siblings = myDocStruct.getParent().getAllChildren();
            int index = siblings.indexOf(myDocStruct);
            try {
                DocStruct ds = document.createDocStruct(myDocStruct.getType());
                ds.setParent(myDocStruct.getParent());
                List<Reference> references = myDocStruct.getAllToReferences();
                if (!references.isEmpty()) {
                    Reference last = references.get(references.size() - 1);
                    ds.addReferenceTo(last.getTarget(), "logical_physical");
                }
                siblings.add(index + 1, ds);
                myDocStruct = ds;
            } catch (TypeNotAllowedForParentException e) {
                logger.error(e);
            }
        }

        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    /**
     * add new structure element dependent on users selection
     *
     * @throws TypeNotAllowedForParentException
     * @throws IOException
     * @throws TypeNotAllowedForParentException
     * @throws TypeNotAllowedAsChildException
     * @throws TypeNotAllowedAsChildException
     */
    public String KnotenAdd() throws TypeNotAllowedForParentException, TypeNotAllowedAsChildException {
        DocStruct ds = null;

        // add element before the currently selected element
        if (this.neuesElementWohin.equals("1")) {
            if (getAddDocStructType1() == null || getAddDocStructType1().equals("")) {
                return "metseditor";
            }
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType1());
            ds = this.document.createDocStruct(dst);
            if (this.myDocStruct == null) {
                return "metseditor";
            }
            DocStruct parent = this.myDocStruct.getParent();
            if (parent == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("the current element has no parent element");
                }
                return "metseditor";
            }
            List<DocStruct> alleDS = new ArrayList<>();

            // run through all elements of the parent
            for (DocStruct tempDS : parent.getAllChildren()) {
                // if correct item is found, add it
                if (tempDS == this.myDocStruct) {
                    alleDS.add(ds);
                }
                alleDS.add(tempDS);
            }

            // delete all children
            for (DocStruct docStruct : alleDS) {
                parent.removeChild(docStruct);
            }
            // add all children again
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        // add element after the currently selected element
        if (this.neuesElementWohin.equals("2")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType1());
            ds = this.document.createDocStruct(dst);
            DocStruct parent = this.myDocStruct.getParent();
            if (parent == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("the current element has no parent element");
                }
                return "metseditor";
            }
            List<DocStruct> alleDS = new ArrayList<>();

            // run through all elements of the parent
            for (DocStruct tempDS : parent.getAllChildren()) {
                alleDS.add(tempDS);
                /* wenn das aktuelle Element das gesuchte ist */
                if (tempDS == this.myDocStruct) {
                    alleDS.add(ds);
                }
            }

            // delete all children
            for (DocStruct docStruct : alleDS) {
                parent.removeChild(docStruct);
            }

            // add all children again
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        // add element as first child element
        if (this.neuesElementWohin.equals("3")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType2());
            ds = this.document.createDocStruct(dst);
            DocStruct parent = this.myDocStruct;
            if (parent == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("the current element has no parent element");
                }
                return "metseditor";
            }
            List<DocStruct> alleDS = new ArrayList<>();
            alleDS.add(ds);

            if (parent.getAllChildren() != null && parent.getAllChildren().size() != 0) {
                alleDS.addAll(parent.getAllChildren());
                parent.getAllChildren().retainAll(new ArrayList<DocStruct>());
            }

            // add all children again
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        // add element as first child element
        if (this.neuesElementWohin.equals("4")) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType2());
            ds = this.document.createDocStruct(dst);
            this.myDocStruct.addChild(ds);
        }

        if (!addableMetadata.isEmpty()) {
            for (MetadatumImpl mdi : addableMetadata) {
                try {
                    Metadata md = new Metadata(mdi.getMd().getType());
                    md.setValue(mdi.getValue());
                    md.setAuthorityID(mdi.getMd().getAuthorityID());
                    md.setAuthorityURI(mdi.getMd().getAuthorityURI());
                    md.setAuthorityValue(mdi.getMd().getAuthorityValue());
                    ds.addMetadata(md);

                } catch (MetadataTypeNotAllowedException | DocStructHasNoTypeException e) {
                    logger.error(e);
                }
            }
        }
        if (!addablePersondata.isEmpty()) {
            for (MetaPerson mdp : addablePersondata) {
                try {
                    Person p = mdp.getP();
                    Person md = new Person(p.getType());
                    md.setFirstname(p.getFirstname());
                    md.setLastname(p.getLastname());
                    md.setAuthorityID(p.getAuthorityID());
                    md.setAuthorityURI(p.getAuthorityURI());
                    md.setAuthorityValue(p.getAuthorityValue());
                    ds.addPerson(p);
                } catch (MetadataTypeNotAllowedException | IncompletePersonObjectException e) {
                    logger.error(e);
                }
            }
        }
        if (!addableCorporates.isEmpty()) {
            for (MetaCorporate corp : addableCorporates) {
                Corporate corporate = corp.getCorporate();
                try {
                    ds.addCorporate(corporate);
                } catch (MetadataTypeNotAllowedException e) {
                    logger.error(e);
                }
            }
        }

        if (!this.pagesStart.equals("") && !this.pagesEnd.equals("")) {
            if (lastAddedObject != null && pagesStart.equals(lastAddedObject.getLabel()) && pagesEnd.equals(lastAddedObject.getLabel())) {
                ds.addReferenceTo(lastAddedObject.getDocStruct(), "logical_physical");
            } else {
                DocStruct temp = this.myDocStruct;
                this.myDocStruct = ds;
                this.ajaxSeiteStart = this.pagesStart;
                this.ajaxSeiteEnde = this.pagesEnd;
                try {
                    this.noUpdateImageIndex = true;
                    AjaxSeitenStartUndEndeSetzen();
                } finally {
                    this.noUpdateImageIndex = false;
                }
                this.myDocStruct = temp;
            }
        } else if (!pageArea.equals("")) {
            ds.addReferenceTo(lastAddedObject.getDocStruct(), "logical_physical");
        }
        // if easy pagination is switched on, use the last page as first page for next structure element
        if (enableFastPagination) {
            pagesStart = pagesEnd;
        } else {
            pagesStart = "";
        }
        pagesEnd = "";
        pageArea = "";

        oldDocstructName = "";
        createAddableData();
        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    /**
     * mögliche Docstructs als Kind zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsKind() {
        SelectItem[] itemList = this.metahelper.getAddableDocStructTypen(this.myDocStruct, false);
        return itemList;
    }

    /**
     * mögliche Docstructs als Nachbar zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsNachbar() {
        SelectItem[] itemList = this.metahelper.getAddableDocStructTypen(this.myDocStruct, true);
        return itemList;
    }

    private String getSelectedStructType(SelectItem[] itemList, String docTypeName) {
        if (itemList != null && itemList.length > 0) {
            for (SelectItem item : itemList) {
                if (item.getValue().toString().equals(docTypeName)) {
                    return docTypeName;
                }
            }
            return itemList[0].getValue().toString();
        } else {
            return "";
        }
    }

    /*
     * ##################################################### ##################################################### ## ## Strukturdaten: Seiten ##
     * ##################################################### ####################################################
     */

    private void checkImageNames() {
        try {
            imagehelper.checkImageNames(this.myProzess, currentTifFolder);
        } catch (TypeNotAllowedForParentException | SwapException | DAOException | IOException | InterruptedException e) {
            logger.error(e);
        }
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images ================================================================
     *
     * @throws DAOException
     * @throws SwapException
     */
    public String createPagination() throws TypeNotAllowedForParentException, IOException, InterruptedException, SwapException, DAOException {
        this.imagehelper.createPagination(this.myProzess, this.currentTifFolder);
        retrieveAllImages();

        // added new
        if (ConfigurationHelper.getInstance().isMetsEditorEnableImageAssignment()) {
            DocStruct log = this.document.getLogicalDocStruct();
            if (log.getType().isAnchor()) {
                if (log.getAllChildren() != null && log.getAllChildren().size() > 0) {
                    log = log.getAllChildren().get(0);
                } else {
                    return "";
                }
            }

            if (log.getAllChildren() != null) {
                for (DocStruct child : log.getAllChildren()) {
                    List<Reference> childRefs = child.getAllReferences("to");
                    for (Reference toAdd : childRefs) {
                        boolean match = false;
                        for (Reference ref : log.getAllReferences("to")) {
                            if (ref.getTarget().equals(toAdd.getTarget())) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            log.getAllReferences("to").add(toAdd);
                        }
                    }
                }
            }
        }
        return "";
    }

    public String reloadPagination() throws TypeNotAllowedForParentException, SwapException, DAOException, IOException, InterruptedException {

        DocStruct physical = document.getPhysicalDocStruct();
        if (physical != null && physical.getAllChildren() != null) {
            List<DocStruct> pages = physical.getAllChildren();

            for (DocStruct page : pages) {

                document.getFileSet().removeFile(page.getAllContentFiles().get(0));

                List<Reference> refs = new ArrayList<>(page.getAllFromReferences());
                for (ugh.dl.Reference ref : refs) {
                    ref.getSource().removeReferenceTo(page);
                }
                if (page.getAllChildren() != null) {
                    for (DocStruct area : page.getAllChildren()) {
                        List<Reference> arearefs = new ArrayList<>(area.getAllFromReferences());
                        for (ugh.dl.Reference ref : arearefs) {
                            ref.getSource().removeReferenceTo(area);
                        }
                    }
                }
            }
        }
        while (physical.getAllChildren() != null && !physical.getAllChildren().isEmpty()) {
            physical.removeChild(physical.getAllChildren().get(0));
        }

        //                BildErmitteln(0);
        createPagination();
        loadCurrentImages(true);
        return "";
        //        return createPagination();
    }

    /**
     * alle Seiten ermitteln ================================================================
     */
    public void retrieveAllImages() {
        DigitalDocument document = null;
        try {
            document = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMeldung(null, "Can not get DigitalDocument: ", e.getMessage());
        }

        List<DocStruct> meineListe = document.getPhysicalDocStruct().getAllChildrenAsFlatList();
        if (meineListe == null) {
            pageMap = null;
            return;
        }
        int numberOfPages = 0;
        if (document.getPhysicalDocStruct() != null && document.getPhysicalDocStruct().getAllChildren() != null) {
            numberOfPages = document.getPhysicalDocStruct().getAllChildren().size();
        }
        logicalPageNumForPages = new MetadatumImpl[numberOfPages];
        pageMap = new OrderedKeyMap<>();
        int counter = 0;
        String lastLogPageNo = null;
        String lastPhysPageNo = null;
        for (DocStruct pageStruct : meineListe) {
            String logPageNo = null;
            String physPageNo = null;
            String coordinates = null;
            Metadata logPageNoMd = null;
            for (Metadata md : pageStruct.getAllMetadata()) {
                if (md.getType().getName().equals("logicalPageNumber")) {
                    logPageNo = md.getValue();
                    logPageNoMd = md;
                } else if (md.getType().getName().equals("physPageNumber")) {
                    physPageNo = md.getValue();
                } else if (md.getType().getName().equals("_COORDS")) {
                    coordinates = md.getValue();
                }
            }
            PhysicalObject pi = new PhysicalObject();
            pi.setDocStruct(pageStruct);
            if ("div".equals(pageStruct.getDocstructType())) {
                lastLogPageNo = logPageNo;
                lastPhysPageNo = physPageNo;
                logicalPageNumForPages[counter] = new MetadatumImpl(logPageNoMd, counter, myPrefs, myProzess, this);
                pi.setPhysicalPageNo(lastPhysPageNo);
                String doublePage = pageStruct.getAdditionalValue();
                pi.setDoublePage(StringUtils.isNotBlank(doublePage) && doublePage.equals("double page"));
                pi.setLogicalPageNo(lastLogPageNo);
                counter++;
                pageMap.put(lastPhysPageNo, pi);
            } else {
                // add placeholder for areas
                pi.setPhysicalPageNo(lastPhysPageNo + "_" + meineListe.indexOf(pageStruct));
                pi.setLogicalPageNo(lastPhysPageNo + ": " + lastLogPageNo);
                pageMap.put(lastPhysPageNo + "_" + meineListe.indexOf(pageStruct), pi);
            }
            pi.setType(pageStruct.getDocstructType());
            pi.setImagename(pageStruct.getImageName());
            pi.setCoordinates(coordinates);
        }

        if (StringUtils.isNotBlank(currentRepresentativePage)) {
            PhysicalObject po = pageMap.get(currentRepresentativePage);
            po.setRepresentative(true);

        }

    }

    public void addPageArea() {
        BildGeheZu();
        DocStruct page = currentPage.getDocStruct();
        if (page != null) {
            DocStructType dst = myPrefs.getDocStrctTypeByName("area");
            try {
                DocStruct ds = document.createDocStruct(dst);
                page.addChild(ds);
                Metadata logicalPageNumber = new Metadata(myPrefs.getMetadataTypeByName("logicalPageNumber"));
                logicalPageNumber.setValue(MetadatenErmitteln(page, "logicalPageNumber"));
                ds.addMetadata(logicalPageNumber);

                Metadata physPageNumber = new Metadata(myPrefs.getMetadataTypeByName("physPageNumber"));
                physPageNumber.setValue(MetadatenErmitteln(page, "physPageNumber"));
                ds.addMetadata(physPageNumber);
                Metadata md = new Metadata(myPrefs.getMetadataTypeByName("_COORDS"));
                ds.addMetadata(md);
                ds.setDocstructType("area");

            } catch (TypeNotAllowedAsChildException | TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
                logger.error(e);
            }
        }
        retrieveAllImages();
        getRectangles();
    }

    DocStruct newPageArea;

    public void addPageAreaAndAssignToDocststuct() {
        DocStruct page = physicalTopstruct.getAllChildren().get(imageIndex);
        newPageArea = null;
        if (page != null) {
            DocStructType dst = myPrefs.getDocStrctTypeByName("area");
            try {
                newPageArea = document.createDocStruct(dst);
                page.addChild(newPageArea);
                Metadata logicalPageNumber = new Metadata(myPrefs.getMetadataTypeByName("logicalPageNumber"));
                logicalPageNumber.setValue(MetadatenErmitteln(page, "logicalPageNumber"));
                newPageArea.addMetadata(logicalPageNumber);

                Metadata physPageNumber = new Metadata(myPrefs.getMetadataTypeByName("physPageNumber"));
                physPageNumber.setValue(MetadatenErmitteln(page, "physPageNumber"));
                newPageArea.addMetadata(physPageNumber);
                Metadata md = new Metadata(myPrefs.getMetadataTypeByName("_COORDS"));
                newPageArea.addMetadata(md);
                newPageArea.setDocstructType("area");

            } catch (TypeNotAllowedAsChildException | TypeNotAllowedForParentException | MetadataTypeNotAllowedException e) {
                logger.error(e);
            }

        }
        retrieveAllImages();
        getRectangles();
        if (newPageArea != null) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject object = pageMap.get(pageObject);
                if (object.getDocStruct().equals(newPageArea)) {
                    pagesStart = "";
                    pagesEnd = "";
                    pageArea = object.getLabel();
                    lastAddedObject = object;
                }
            }
        }
    }

    public void setRectangles(String json) {
        if (StringUtils.isBlank(json)) {
            return;
        }
        PageAreaRectangle[] data = new Gson().fromJson(json, PageAreaRectangle[].class);
        List<DocStruct> pages = document.getPhysicalDocStruct().getAllChildren();
        DocStruct page = pages.get(imageIndex);

        for (PageAreaRectangle rect : data) {
            DocStruct area = page.getAllChildren().get(Integer.valueOf(rect.getId()));
            for (Metadata md : area.getAllMetadataByType(myPrefs.getMetadataTypeByName("_COORDS"))) {
                md.setValue(rect.getX() + "," + rect.getY() + "," + rect.getW() + "," + rect.getH());
            }

        }

    }

    public String getRectangles() {
        StringBuilder sb = new StringBuilder();
        List<DocStruct> pages = document.getPhysicalDocStruct().getAllChildren();
        if (pages == null || pages.isEmpty() || pages.size() <= imageIndex) {
            return "";
        }
        DocStruct page = pages.get(imageIndex);
        sb.append("[");
        if (page.getAllChildren() == null) {
            return "";
        }
        int index = 0;
        for (DocStruct area : page.getAllChildren()) {
            String coordinates = MetadatenErmitteln(area, "_COORDS");
            sb.append("{");
            sb.append("\"id\":\"");
            sb.append(index++);

            String x = "";
            String y = "";
            String w = "";
            String h = "";
            if (StringUtils.isNotBlank(coordinates)) {

                Pattern pattern = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+)");
                Matcher matcher = pattern.matcher(coordinates);

                if (matcher.matches()) {
                    x = matcher.group(1);
                    y = matcher.group(2);
                    w = matcher.group(3);
                    h = matcher.group(4);
                }

                sb.append("\",\"x\":\"");
                sb.append(x);
                sb.append("\",\"y\":\"");
                sb.append(y);
                sb.append("\",\"w\":\"");
                sb.append(w);
                sb.append("\",\"h\":\"");
                sb.append(h);
            }
            sb.append("\"},");
            if (StringUtils.isBlank(x)) {
                pageAreaEditionMode = true;
            }
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");
        return sb.toString();
    }

    public void deletePageArea() {
        if (currentPage == null || currentPage.getType().equals("div")) {
            return;
        }

        DocStruct pageArea = currentPage.getDocStruct();
        DocStruct page = pageArea.getParent();
        page.removeChild(pageArea);
        List<Reference> fromReferences = pageArea.getAllFromReferences();
        List<DocStruct> linkedDocstructs = new ArrayList<>();
        for (Reference ref : fromReferences) {
            linkedDocstructs.add(ref.getSource());
        }
        for (DocStruct ds : linkedDocstructs) {
            ds.removeReferenceTo(pageArea);
        }
        retrieveAllImages();
        pageAreaEditionMode = false;
    }

    public void cancelPageAreaEdition() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject object = pageMap.get(pageObject);
            if (object.getDocStruct().equals(newPageArea)) {
                currentPage = object;
                break;
            }
        }
        pageArea = "";
        pageAreaEditionMode = false;
        deletePageArea();
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln ================================================================
     */
    private void StructSeitenErmitteln(DocStruct inStrukturelement) {
        if (inStrukturelement == null) {
            return;
        }
        List<Reference> listReferenzen = inStrukturelement.getAllReferences("to");
        int zaehler = 0;
        int imageNr = 0;
        if (listReferenzen != null) {
            /*
             * -------------------------------- Referenzen sortieren --------------------------------
             */
            Collections.sort(listReferenzen, new Comparator<Reference>() {
                @Override
                public int compare(final Reference o1, final Reference o2) {
                    final Reference r1 = o1;
                    final Reference r2 = o2;
                    Integer page1 = 0;
                    Integer page2 = 0;

                    MetadataType mdt = Metadaten.this.myPrefs.getMetadataTypeByName("physPageNumber");
                    List<? extends Metadata> listMetadaten = r1.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        Metadata meineSeite = listMetadaten.get(0);
                        page1 = Integer.parseInt(meineSeite.getValue());
                    }
                    listMetadaten = r2.getTarget().getAllMetadataByType(mdt);
                    if (listMetadaten != null && listMetadaten.size() > 0) {
                        Metadata meineSeite = listMetadaten.get(0);
                        page2 = Integer.parseInt(meineSeite.getValue());
                    }
                    if (page1.equals(page2)) {
                        if (r1.getTarget().getDocstructType().equals("div")) {
                            page1 = 0;
                        }
                    }

                    return page1.compareTo(page2);
                }
            });

            /* die Größe der Arrays festlegen */
            this.structSeiten = new SelectItem[listReferenzen.size()];
            this.structSeitenNeu = new MetadatumImpl[listReferenzen.size()];
            List<DocStruct> pageList = document.getPhysicalDocStruct().getAllChildrenAsFlatList();
            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (Reference ref : listReferenzen) {
                DocStruct target = ref.getTarget();
                StructSeitenErmitteln2(target, zaehler, pageList);
                if (imageNr == 0) {
                    imageNr = StructSeitenErmitteln3(target);
                }
                zaehler++;
            }

        }

        /*
         * Wenn eine Verkn�pfung zwischen Strukturelement und Bildern sein soll, das richtige Bild anzeigen
         */
        if (this.bildZuStrukturelement && !this.noUpdateImageIndex) {
            if (currentTopstruct != null && currentTopstruct.getType().getName().equals("BoundBook")) {
                imageNr = StructSeitenErmitteln3(inStrukturelement);
            }

            if (!allImages.isEmpty()) {
                setImageIndex(imageNr - 1);
                if (imageNr % numberOfImagesPerPage == 0) {
                    pageNo = (imageNr - 1) / numberOfImagesPerPage;
                } else {
                    pageNo = imageNr / numberOfImagesPerPage;
                }
                getPaginatorList();
            }

        }
    }

    /**
     * alle Seiten des aktuellen Strukturelements ermitteln 2 ================================================================
     */
    private void StructSeitenErmitteln2(DocStruct inStrukturelement, int inZaehler, List<DocStruct> physicalDocStructs) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return;
        }
        String pageIdentifier = null;
        if (inStrukturelement.getDocstructType().equals("div")) {
            pageIdentifier = MetadatenErmitteln(inStrukturelement, "physPageNumber");
        } else {
            pageIdentifier = MetadatenErmitteln(inStrukturelement, "physPageNumber") + "_" + physicalDocStructs.indexOf(inStrukturelement);
        }
        for (Metadata meineSeite : listMetadaten) {
            this.structSeitenNeu[inZaehler] = new MetadatumImpl(meineSeite, inZaehler, this.myPrefs, this.myProzess, this);
            DocStruct ds = (DocStruct) meineSeite.getParent();
            if (inStrukturelement.getDocstructType().equals("div")) {
                this.structSeiten[inZaehler] =
                        new SelectItem(pageIdentifier, MetadatenErmitteln(ds, "physPageNumber").trim() + ": " + meineSeite.getValue());
            } else {
                this.structSeiten[inZaehler] = new SelectItem(pageIdentifier,
                        Helper.getTranslation("mets_pageArea", MetadatenErmitteln(ds, "physPageNumber") + ": " + meineSeite.getValue()));
            }
        }
    }

    /**
     * noch für Testzweck zum direkten öffnen der richtigen Startseite 3 ================================================================
     */
    private int StructSeitenErmitteln3(DocStruct inStrukturelement) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.size() == 0) {
            return 0;
        }
        int rueckgabe = 0;
        for (Metadata meineSeite : listMetadaten) {
            rueckgabe = Integer.parseInt(meineSeite.getValue());
        }
        return rueckgabe;
    }

    /**
     * die Paginierung ändern
     */

    public String Paginierung() {

        int numberOfPages = 0;
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.isSelected()) {
                numberOfPages++;
            }
        }

        int[] pageSelection = new int[numberOfPages];
        numberOfPages = 0;
        for (String key : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(key);
            if (po.isSelected()) {
                pageSelection[numberOfPages] = Integer.parseInt(po.getPhysicalPageNo());
                numberOfPages = numberOfPages + 1;
            }
        }

        Paginator.Mode mode;
        switch (paginierungSeitenProImage) {
            case 2:
                mode = Paginator.Mode.COLUMNS;
                break;
            case 3:
                mode = Paginator.Mode.FOLIATION;
                break;
            case 4:
                mode = Paginator.Mode.RECTOVERSO;
                break;
            case 5:
                mode = Paginator.Mode.RECTOVERSO_FOLIATION;
                break;
            case 6:
                mode = Paginator.Mode.DOUBLE_PAGES;
                break;
            default:
                mode = Paginator.Mode.PAGES;
        }

        Paginator.Type type;
        switch (Integer.parseInt(paginierungArt)) {
            case 1:
                type = Paginator.Type.ARABIC;
                break;
            case 2:
                type = Paginator.Type.ROMAN;
                break;
            case 6:
                type = Paginator.Type.FREETEXT;
                break;
            default:
                type = Paginator.Type.UNCOUNTED;
                fictitious = false;
                paginationPrefix = "";
                paginationSuffix = "";
                break;
        }

        Paginator.Scope scope;
        switch (paginierungAbSeiteOderMarkierung) {
            case 1:
                scope = Paginator.Scope.FROMFIRST;
                break;
            default:
                scope = Paginator.Scope.SELECTED;
                break;

        }

        try {
            Paginator p = new Paginator().setPageSelection(pageSelection)
                    .setPagesToPaginate(logicalPageNumForPages)
                    .setPaginationScope(scope)
                    .setPaginationType(type)
                    .setPaginationMode(mode)
                    .setFictitious(fictitious)
                    .setPaginationStartValue(paginierungWert);
            p.setPrefix(paginationPrefix);
            p.setSuffix(paginationSuffix);
            p.run();
        } catch (IllegalArgumentException iae) {
            Helper.setFehlerMeldung("fehlerBeimEinlesen", iae.getMessage());
        }

        boolean firstMatch = false;
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.isSelected() || firstMatch) {
                firstMatch = true;
                if (scope == Paginator.Scope.SELECTED && po.isSelected()) {
                    if (doublePage) {
                        po.getDocStruct().setAdditionalValue("double page");
                    } else {
                        po.getDocStruct().setAdditionalValue("");
                    }
                } else if (scope == Paginator.Scope.FROMFIRST && firstMatch) {
                    if (doublePage) {
                        po.getDocStruct().setAdditionalValue("double page");
                    } else {
                        po.getDocStruct().setAdditionalValue("");
                    }
                }
            }
        }

        /*
         * -------------------------------- zum Schluss nochmal alle Seiten neu einlesen --------------------------------
         */
        retrieveAllImages();
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        doublePage = false;
        return null;
    }

    /**
     * alle Knoten des Baums expanden oder collapsen ================================================================
     */
    public String TreeExpand() {
        this.tree3.expandNodes(this.treeProperties.get("fullexpanded"));
        return "metseditor";
    }

    /*
     * ##################################################### ##################################################### ## ## Bilder-Anzeige ##
     * ##################################################### ####################################################
     */

    public String BildBlaettern() {
        BildErmitteln(numberOfNavigation);
        return "";
    }

    public String BildGeheZu() {
        int eingabe;
        try {
            eingabe = Integer.parseInt(this.bildNummerGeheZu);
        } catch (Exception e) {
            eingabe = this.bildNummer;
        }
        setImageIndex(eingabe - 1);

        return "";
    }

    public String loadImageInThumbnailList() {
        int imageNumber = 0;
        try {
            imageNumber = Integer.parseInt(this.bildNummerGeheZu);
        } catch (Exception e) {
            imageNumber = this.bildNummer;
        }
        setImageIndex(imageNumber - 1);

        //  get correct paginated page
        if (imageNumber % numberOfImagesPerPage == 0) {
            pageNo = (imageNumber - 1) / numberOfImagesPerPage;
        } else {
            pageNo = imageNumber / numberOfImagesPerPage;
        }

        getPaginatorList();
        return "";
    }

    public List<String> getAllTifFolders() {
        return this.allTifFolders;
    }

    public void readAllTifFolders() throws IOException, InterruptedException, SwapException, DAOException {
        this.allTifFolders = new ArrayList<>();
        Path dir = Paths.get(this.myProzess.getImagesDirectory());

        List<String> verzeichnisse = StorageProvider.getInstance().listDirNames(dir.toString());
        for (int i = 0; i < verzeichnisse.size(); i++) {
            this.allTifFolders.add(verzeichnisse.get(i));
        }

        Path thumbsDir = Paths.get(myProzess.getThumbsDirectory());
        if (StorageProvider.getInstance().isDirectory(thumbsDir)) {
            List<String> thumbDirs = StorageProvider.getInstance().listDirNames(thumbsDir.toString());
            for (String thumbDirName : thumbDirs) {
                String matchingImageDir = myProzess.getMatchingImageDir(thumbDirName);
                if (!allTifFolders.contains(matchingImageDir)) {
                    allTifFolders.add(matchingImageDir);
                }
            }
        }

        if (!ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName().equals("")) {
            String foldername = ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName();
            for (String directory : this.allTifFolders) {
                if (directory.equals(foldername)) {
                    this.currentTifFolder = directory;
                    break;
                }
            }
        }

        if (StringUtils.isBlank(currentTifFolder) && !allTifFolders.isEmpty()) {
            this.currentTifFolder = Paths.get(this.myProzess.getImagesTifDirectory(true)).getFileName().toString();
            if (!this.allTifFolders.contains(this.currentTifFolder)) {
                this.currentTifFolder = allTifFolders.get(0);
            }
        }
    }

    public void BildErmitteln(int welches) {
        /*
         * wenn die Bilder nicht angezeigt werden, brauchen wir auch das Bild nicht neu umrechnen
         */
        if (!this.bildAnzeigen) {
            return;
        }

        // try {
        if (dataList == null || dataList.isEmpty() || tiffFolderHasChanged) {
            tiffFolderHasChanged = false;

            if (this.currentTifFolder != null) {
                try {
                    // dataList = this.imagehelper.getImageFiles(mydocument.getPhysicalDocStruct());
                    dataList = this.imagehelper.getImageFiles(this.myProzess, this.currentTifFolder);
                    if (dataList == null) {
                        myBild = null;
                        bildNummer = -1;
                        return;
                    }
                    //
                } catch (InvalidImagesException e1) {
                    logger.error("Images could not be read", e1);
                    Helper.setFehlerMeldung("images could not be read", e1);
                }
            } else {
                try {
                    createPagination();
                    dataList = this.imagehelper.getImageFiles(document.getPhysicalDocStruct());
                } catch (TypeNotAllowedForParentException e) {
                    logger.error(e);
                } catch (SwapException e) {
                    logger.error(e);
                } catch (DAOException e) {
                    logger.error(e);
                } catch (IOException e) {
                    logger.error(e);
                } catch (InterruptedException e) {
                    logger.error(e);
                }
            }
        }

        if (dataList != null && dataList.size() > 0) {
            this.myBildLetztes = dataList.size();
            for (int i = 0; i < dataList.size(); i++) {
                if (this.myBild == null) {
                    this.myBild = dataList.get(0);
                }
                String index = dataList.get(i).substring(0, dataList.get(i).lastIndexOf("."));
                String myPicture = this.myBild.substring(0, this.myBild.lastIndexOf("."));
                /* wenn das aktuelle Bild gefunden ist, das neue ermitteln */
                if (index.equals(myPicture)) {
                    int pos = i + welches;
                    /* aber keine Indexes ausserhalb des Array erlauben */
                    if (pos < 0) {
                        pos = 0;
                    }
                    if (pos > dataList.size() - 1) {
                        pos = dataList.size() - 1;
                    }
                    /* das aktuelle tif erfassen */
                    if (dataList.size() > pos) {
                        this.myBild = dataList.get(pos);
                    } else {
                        this.myBild = dataList.get(dataList.size() - 1);
                    }
                    /* die korrekte Seitenzahl anzeigen */
                    this.bildNummer = pos + 1;
                    /* Pages-Verzeichnis ermitteln */
                    String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();
                    /*
                     * den Counter für die Bild-ID auf einen neuen Wert setzen, damit nichts gecacht wird
                     */
                    this.myBildCounter++;

                    /* Session ermitteln */
                    FacesContext context = FacesContextHelper.getCurrentFacesContext();
                    HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                    String mySession = session.getId() + "_" + this.myBildCounter + ".png";

                    /* das neue Bild zuweisen */
                    try {
                        String tiffconverterpfad =
                                this.myProzess.getImagesDirectory() + this.currentTifFolder + FileSystems.getDefault().getSeparator() + this.myBild;
                        if (!StorageProvider.getInstance().isFileExists(Paths.get(tiffconverterpfad))) {
                            tiffconverterpfad = this.myProzess.getImagesTifDirectory(true) + this.myBild;
                            Helper.setFehlerMeldung("formularOrdner:TifFolders", "",
                                    "image " + this.myBild + " does not exist in folder " + this.currentTifFolder + ", using image from "
                                            + Paths.get(this.myProzess.getImagesTifDirectory(true)).getFileName().toString());
                        }
                        this.imagehelper.scaleFile(tiffconverterpfad, myPfad + mySession, this.myBildGroesse, 0);
                    } catch (Exception e) {
                        Helper.setFehlerMeldung("could not find image folder", e);
                        logger.error(e);
                    }
                    break;
                }
            }
        }
        BildPruefen();
    }

    private void BildPruefen() {
        /* wenn bisher noch kein Bild da ist, das erste nehmen */
        boolean exists = false;
        try {
            if (this.currentTifFolder != null && this.myBild != null) {
                exists = StorageProvider.getInstance()
                        .isFileExists(Paths.get(
                                this.myProzess.getImagesDirectory() + this.currentTifFolder + FileSystems.getDefault().getSeparator() + this.myBild));
            }
        } catch (Exception e) {
            this.bildNummer = -1;
            logger.error(e);
        }
        /* wenn das Bild nicht existiert, den Status ändern */
        if (!exists) {
            this.bildNummer = -1;
        }
    }

    private boolean SperrungAktualisieren() {
        /*
         * wenn die Sperrung noch aktiv ist und auch für den aktuellen Nutzer gilt, Sperrung aktualisieren
         */
        if (MetadatenSperrung.isLocked(this.myProzess.getId().intValue())) {
            if (!this.sperrung.getLockBenutzer(this.myProzess.getId().intValue()).equals(this.myBenutzerID)) {
                // locked by someone else
                return false;
            }
        }
        this.sperrung.setLocked(this.myProzess.getId().intValue(), this.myBenutzerID);
        return true;
    }

    private void SperrungAufheben() {
        if (MetadatenSperrung.isLocked(this.myProzess.getId().intValue())
                && this.sperrung.getLockBenutzer(this.myProzess.getId().intValue()).equals(this.myBenutzerID)) {
            this.sperrung.setFree(this.myProzess.getId().intValue());
        }
    }

    /*
     * ##################################################### ##################################################### ## ## Navigationsanweisungen ##
     * ##################################################### ####################################################
     */

    /**
     * zurück zur Startseite, Metadaten vorher freigeben
     */
    public String goMain() {
        SperrungAufheben();
        return "index";
    }

    /**
     * zurück gehen
     */
    public String goZurueck() {
        SperrungAufheben();
        return this.zurueck;
    }

    public String discard() {
        myProzess.removeTemporaryMetadataFiles();
        SperrungAufheben();
        return this.zurueck;
    }

    public boolean isCheckForNewerTemporaryMetadataFiles() {
        return processHasNewTemporaryMetadataFiles;
    }

    /*
     * ##################################################### ##################################################### ## ## Transliteration bestimmter
     * Felder ## ##################################################### ####################################################
     */

    public String Transliterieren() {
        Metadata md = this.curMetadatum.getMd();

        /*
         * -------------------------------- wenn es ein russischer Titel ist, dessen Transliterierungen anzeigen --------------------------------
         */
        if (md.getType().getName().equals("RUSMainTitle")) {
            Transliteration trans = new Transliteration();

            try {
                MetadataType mdt = this.myPrefs.getMetadataTypeByName("MainTitleTransliterated");
                Metadata mdDin = new Metadata(mdt);
                Metadata mdIso = new Metadata(mdt);
                mdDin.setValue(trans.transliterate_din(md.getValue()));
                mdIso.setValue(trans.transliterate_iso(md.getValue()));

                this.myDocStruct.addMetadata(mdDin);
                this.myDocStruct.addMetadata(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    public String TransliterierenPerson() {
        Person md = this.curPerson.getP();

        /*
         * -------------------------------- wenn es ein russischer Autor ist, dessen Transliterierungen anlegen --------------------------------
         */
        if (md.getRole().equals("Author")) {
            Transliteration trans = new Transliteration();
            try {
                MetadataType mdtDin = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedDIN");
                MetadataType mdtIso = this.myPrefs.getMetadataTypeByName("AuthorTransliteratedISO");
                Person mdDin = new Person(mdtDin);
                Person mdIso = new Person(mdtIso);

                mdDin.setFirstname(trans.transliterate_din(md.getFirstname()));
                mdDin.setLastname(trans.transliterate_din(md.getLastname()));
                mdIso.setFirstname(trans.transliterate_iso(md.getFirstname()));
                mdIso.setLastname(trans.transliterate_iso(md.getLastname()));
                mdDin.setRole("AuthorTransliteratedDIN");
                mdIso.setRole("AuthorTransliteratedISO");

                this.myDocStruct.addPerson(mdDin);
                this.myDocStruct.addPerson(mdIso);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error("Fehler beim Hinzufügen der Transliterationen (MetadataTypeNotAllowedException): " + e.getMessage());
            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);

        /* zum Schluss die Sperrung aktualisieren */
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return "";
    }

    /*
     * ##################################################### ##################################################### ## ## aus einer Liste von PPNs
     * Strukturelemente aus dem Opac ## holen und dem aktuellen Strukturelement unterordnen ## #####################################################
     * ####################################################
     */

    /**
     * mehrere PPNs aus dem Opac abfragen und dem aktuellen Strukturelement unterordnen
     * ================================================================
     */
    public String AddAdditionalOpacPpns() {
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns.replace("\r\n", "\n").replace("\r", "\n"), "\n");
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(getOpacKatalog());
        IOpacPlugin iopac = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                Fileformat addrdf = iopac.search(this.opacSuchfeld, tok, coc, this.myPrefs);
                if (addrdf != null) {
                    this.myDocStruct.addChild(addrdf.getDigitalDocument().getLogicalDocStruct());
                    MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
                logger.error("Error while importing from catalogue: " + e.getMessage());
            }
        }
        return "Metadaten3links";
    }

    /**
     * eine PPN aus dem Opac abfragen und dessen Metadaten dem aktuellen Strukturelement zuweisen
     * ================================================================
     */
    public String AddMetadaFromOpacPpn() {
        StringTokenizer tokenizer = new StringTokenizer(this.additionalOpacPpns, "\n");
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            try {
                ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(getOpacKatalog());
                IOpacPlugin iopac = (IOpacPlugin) PluginLoader.getPluginByTitle(PluginType.Opac, coc.getOpacType());
                Fileformat addrdf = iopac.search(this.opacSuchfeld, tok, coc, this.myPrefs);
                if (addrdf != null) {

                    // remove empty default elements
                    List<Metadata> metadataList = this.myDocStruct.getAllMetadata();
                    if (metadataList != null) {
                        List<Metadata> metadataListClone = new ArrayList<>(metadataList);
                        for (Metadata md : metadataListClone) {
                            if (md.getValue() == null || md.getValue().isEmpty()) {
                                this.myDocStruct.removeMetadata(md, true);
                            }
                        }
                    }
                    List<Person> personList = myDocStruct.getAllPersons();
                    if (personList != null) {
                        List<Person> personListClone = new ArrayList<>(personList);
                        for (Person p : personListClone) {
                            if (p.getFirstname().isEmpty() && p.getLastname().isEmpty()) {
                                myDocStruct.removePerson(p);
                            }
                        }
                    }

                    /* die Liste aller erlaubten Metadatenelemente erstellen */
                    List<String> erlaubte = new ArrayList<>();
                    for (MetadataType mt : this.myDocStruct.getAddableMetadataTypes(false)) {
                        erlaubte.add(mt.getName());
                    }

                    /*
                     * wenn der Metadatentyp in der Liste der erlaubten Typen, dann hinzufügen
                     */
                    for (Metadata m : addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadata()) {
                        if (erlaubte.contains(m.getType().getName())) {
                            this.myDocStruct.addMetadata(m);
                        }
                    }

                    for (Person m : addrdf.getDigitalDocument().getLogicalDocStruct().getAllPersons()) {
                        if (erlaubte.contains(m.getType().getName())) {
                            this.myDocStruct.addPerson(m);
                        }
                    }

                    for (MetadataGroup m : addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadataGroups()) {
                        if (myDocStruct.getAddableMetadataGroupTypes().contains(m.getType())) {
                            myDocStruct.addMetadataGroup(m);
                        }

                    }

                    MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
                logger.error("Error while importing from catalogue: " + e.getMessage());
            }
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        this.modusAnsicht = "Metadaten";
        return "";
    }

    /*
     * ##################################################### ##################################################### ## ## Metadatenvalidierung ##
     * ##################################################### ####################################################
     */

    public void Validate() {
        MetadatenVerifizierung mv = new MetadatenVerifizierung();
        boolean valid = mv.validate(this.gdzfile, this.myPrefs, this.myProzess);
        if (valid) {
            Helper.setMeldung("ValidationSuccessful");
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
    }

    /*
     * ##################################################### ##################################################### ## ## Auswahl der Seiten über Ajax
     * ## ##################################################### ####################################################
     */

    public void CurrentStartpage() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getPhysicalPageNo().equals(String.valueOf(this.pageNumber + 1))) {
                this.pagesStart = po.getLabel();
            }
        }
        pageArea = "";
    }

    public void CurrentEndpage() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getPhysicalPageNo().equals(String.valueOf(this.pageNumber + 1))) {
                this.pagesEnd = po.getLabel();
            }
        }
        pageArea = "";
    }

    public void startpage() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getPhysicalPageNo().equals(String.valueOf(this.pageNumber + 1))) {
                this.pagesStartCurrentElement = po.getLabel();
            }
        }
    }

    public void endpage() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getPhysicalPageNo().equals(String.valueOf(this.pageNumber + 1))) {
                this.pagesEndCurrentElement = po.getLabel();
            }
        }
    }

    public void setPages() {
        this.ajaxSeiteStart = this.pagesStartCurrentElement;
        this.ajaxSeiteEnde = this.pagesEndCurrentElement;

        AjaxSeitenStartUndEndeSetzen();
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber - 1;

    }

    public List<String> getAjaxAlleSeiten(String prefix) {
        if (logger.isDebugEnabled()) {
            logger.debug("Ajax-Liste abgefragt");
        }
        List<String> li = new ArrayList<>();
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getLabel().contains(prefix)) {
                li.add(po.getLabel());
            }
        }
        return li;
    }

    /**
     * die Seiten über die Ajax-Felder festlegen ================================================================
     */
    public void AjaxSeitenStartUndEndeSetzen() {
        boolean startseiteOk = false;
        boolean endseiteOk = false;

        /*
         * alle Seiten durchlaufen und prüfen, ob die eingestellte Seite überhaupt existiert
         */
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getLabel().equals(this.ajaxSeiteStart)) {
                startseiteOk = true;
                this.alleSeitenAuswahl_ersteSeite = po.getPhysicalPageNo();
            }
            if (po.getLabel().equals(this.ajaxSeiteEnde)) {
                endseiteOk = true;
                this.alleSeitenAuswahl_letzteSeite = po.getPhysicalPageNo();
            }
        }

        /* wenn die Seiten ok sind */
        if (startseiteOk && endseiteOk) {
            SeitenStartUndEndeSetzen();
        } else {
            Helper.setFehlerMeldung("Selected image(s) unavailable");
        }
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String SeitenStartUndEndeSetzen() {
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        int startPage = pageMap.getIndexPosition(alleSeitenAuswahl_ersteSeite);
        int endPage = pageMap.getIndexPosition(alleSeitenAuswahl_letzteSeite);
        int anzahlAuswahl = endPage - startPage + 1;
        if (anzahlAuswahl > 0) {
            /* alle bisher zugewiesenen Seiten entfernen */
            this.myDocStruct.getAllToReferences().clear();
            List<String> pageIdentifierList = pageMap.getKeyList().subList(startPage, endPage + 1);

            for (String pageIdentifier : pageIdentifierList) {
                myDocStruct.addReferenceTo(pageMap.get(pageIdentifier).getDocStruct(), "logical_physical");
            }

        } else {
            Helper.setFehlerMeldung("mets_paginationAssignmentError");
        }
        StructSeitenErmitteln(this.myDocStruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */

    public String SeitenVonChildrenUebernehmen() {
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }

        /* alle Kinder des aktuellen DocStructs durchlaufen */
        this.myDocStruct.getAllReferences("to").removeAll(this.myDocStruct.getAllReferences("to"));
        if (this.myDocStruct.getAllChildren() != null) {
            for (DocStruct child : this.myDocStruct.getAllChildren()) {
                List<Reference> childRefs = child.getAllReferences("to");
                for (Reference toAdd : childRefs) {
                    boolean match = false;
                    for (Reference ref : this.myDocStruct.getAllReferences("to")) {
                        if (ref.getTarget().equals(toAdd.getTarget())) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        this.myDocStruct.getAllReferences("to").add(toAdd);
                    }
                }
            }
        }
        StructSeitenErmitteln(this.myDocStruct);
        return null;
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */

    public String BildErsteSeiteAnzeigen() {
        this.bildAnzeigen = true;
        if (this.treeProperties.get("showpagesasajax")) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                if (po.getLabel().equals(this.ajaxSeiteStart)) {
                    this.alleSeitenAuswahl_ersteSeite = po.getPhysicalPageNo();
                    break;
                }
            }
        }
        try {
            int pageNumber = Integer.parseInt(this.alleSeitenAuswahl_ersteSeite) - this.bildNummer + 1;
            setImageIndex(pageNumber - 1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String BildLetzteSeiteAnzeigen() {
        this.bildAnzeigen = true;
        if (this.treeProperties.get("showpagesasajax")) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                if (po.getLabel().equals(this.ajaxSeiteEnde)) {
                    this.alleSeitenAuswahl_letzteSeite = po.getPhysicalPageNo();
                    break;
                }
            }
        }
        try {
            int pageNumber = Integer.parseInt(this.alleSeitenAuswahl_letzteSeite) - this.bildNummer + 1;
            setImageIndex(pageNumber - 1);
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * ausgewählte Seiten dem aktuellen Strukturelement hinzufügen ================================================================
     */
    public String SeitenHinzu() {
        /* alle markierten Seiten durchlaufen */
        for (String key : this.alleSeitenAuswahl) {
            boolean schonEnthalten = false;

            /*
             * wenn schon References vorhanden, prüfen, ob schon enthalten, erst dann zuweisen
             */
            if (this.myDocStruct.getAllToReferences("logical_physical") != null) {
                for (Reference obj : this.myDocStruct.getAllToReferences("logical_physical")) {
                    if (obj.getTarget() == pageMap.get(key).getDocStruct()) {
                        schonEnthalten = true;
                        break;
                    }
                }
            }
            if (!schonEnthalten) {
                this.myDocStruct.addReferenceTo(pageMap.get(key).getDocStruct(), "logical_physical");
            }
        }

        StructSeitenErmitteln(this.myDocStruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        alleSeitenAuswahl = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return null;
    }

    /**
     * ausgewählte Seiten aus dem Strukturelement entfernen ================================================================
     */
    public String SeitenWeg() {
        for (String key : this.structSeitenAuswahl) {
            this.myDocStruct.removeReferenceTo(pageMap.get(key).getDocStruct());
        }
        StructSeitenErmitteln(this.myDocStruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        this.structSeitenAuswahl = null;
        if (!SperrungAktualisieren()) {
            return "metseditor_timeout";
        }
        return null;
    }

    /*
     * ##################################################### ##################################################### ## ## OCR ##
     * ##################################################### ####################################################
     */

    public boolean isImageHasOcr() {
        try {
            return FilesystemHelper.isOcrFileExists(myProzess, image.getTooltip().substring(0, image.getTooltip().lastIndexOf(".")));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isShowOcrButton() {
        if (ConfigurationHelper.getInstance().isMetsEditorUseExternalOCR()) {
            return ConfigurationHelper.getInstance().isMetsEditorShowOCRButton();
        } else {
            return isImageHasOcr();
        }
    }

    public String getOcrResult() {
        String ocrResult = "";
        if (ConfigurationHelper.getInstance().isMetsEditorUseExternalOCR()) {
            String myOcrUrl = "";

            myOcrUrl = getOcrBasisUrl(image.getOrder());

            CloseableHttpClient client = null;
            HttpGet method = new HttpGet(myOcrUrl);
            InputStream stream = null;
            try {
                client = HttpClientBuilder.create().build();

                stream = client.execute(method, HttpClientHelper.streamResponseHandler);
                if (stream != null) {
                    ocrResult = IOUtils.toString(stream, "UTF-8");
                }
                // this.ocrResult = method.getResponseBodyAsString();

            } catch (IOException e) {
                ocrResult = "Fatal transport error: " + e.getMessage();
            } finally {
                method.releaseConnection();
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        client = null;
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        stream = null;
                    }
                }
            }
        } else {
            String ocrFileNew = image.getTooltip().substring(0, image.getTooltip().lastIndexOf("."));
            ocrResult = FilesystemHelper.getOcrFileContent(myProzess, ocrFileNew).replaceAll("(?: ?<br\\/>){3,}", "\n").replaceAll("<br ?/>", "\n");
        }
        return ocrResult;
    }

    public void loadJsonAlto() throws IOException, JDOMException, SwapException, DAOException, InterruptedException {
        Path altoFile = getCurrentAltoPath();
        SimpleAlto alto = new SimpleAlto();
        if (Files.exists(altoFile)) {
            alto = SimpleAlto.readAlto(altoFile);
        }

        this.currentJsonAlto = new Gson().toJson(alto);
    }

    public String getJsonAlto() {
        return currentJsonAlto;
    }

    private Path getCurrentAltoPath() throws SwapException, DAOException, IOException, InterruptedException {
        String ocrFileNew = image.getTooltip().substring(0, image.getTooltip().lastIndexOf("."));
        Path altoFile = Paths.get(myProzess.getOcrAltoDirectory(), ocrFileNew + ".xml");
        if (!Files.exists(altoFile)) {
            altoFile = altoFile.getParent().resolve(ocrFileNew + ".alto");
        }
        return altoFile;
    }

    public void saveAlto() {
        AltoChange[] changes = new Gson().fromJson(this.altoChanges, AltoChange[].class);
        try {
            AltoSaver.saveAltoChanges(getCurrentAltoPath(), changes);
            this.loadJsonAlto();
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, Helper.getTranslation("savedAlto"), null);
            FacesContext.getCurrentInstance().addMessage("altoChanges", fm);
        } catch (JDOMException | IOException | SwapException | DAOException | InterruptedException e) {
            // TODO Auto-generated catch block
            logger.error(e);
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, Helper.getTranslation("errorSavingAlto"), null);
            FacesContext.getCurrentInstance().addMessage("altoChanges", fm);
        }
    }

    public String getOcrAddress() {
        int startseite = -1;
        int endseite = -1;
        if (this.structSeiten != null) {
            for (SelectItem si : this.structSeiten) {
                int temp = Integer.parseInt(si.getLabel().substring(0, si.getLabel().indexOf(":")));
                if (startseite == -1 || startseite > temp) {
                    startseite = temp;
                }
                if (endseite == -1 || endseite < temp) {
                    endseite = temp;
                }
            }
        }
        return getOcrBasisUrl(startseite, endseite);
    }

    private String getOcrBasisUrl(int... seiten) {
        String url = ConfigurationHelper.getInstance().getOcrUrl();
        VariableReplacer replacer = new VariableReplacer(this.document, this.myPrefs, this.myProzess, null);
        url = replacer.replace(url);
        url += "/&imgrange=" + seiten[0];
        if (seiten.length > 1) {
            url += "-" + seiten[1];
        }
        return url;
    }

    /*
     * ##################################################### ##################################################### ## ## Getter und Setter ##
     * ##################################################### ####################################################
     */

    public void setBildNummer(int inBild) {
    }

    public String getTempTyp() {
        if (this.selectedMetadatum == null) {
            getAddableMetadataTypes();
            this.selectedMetadatum = this.tempMetadatumList.get(0);
        }
        return this.selectedMetadatum.getMd().getType().getName();
    }

    public void setTempTyp(String tempTyp) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(tempTyp);
        try {
            Metadata md = new Metadata(mdt);
            this.selectedMetadatum = new MetadatumImpl(md, this.myMetadaten.size() + 1, this.myPrefs, this.myProzess, this);
            currentMetadataToPerformSearch = selectedMetadatum;
        } catch (MetadataTypeNotAllowedException e) {
            logger.error(e.getMessage());
        }
        this.tempTyp = tempTyp;
    }

    public MetadatumImpl getMetadatum() {
        if (this.selectedMetadatum == null) {
            getAddableMetadataTypes();
            this.selectedMetadatum = this.tempMetadatumList.get(0);
        }
        return this.selectedMetadatum;
    }

    public MetaCorporate getSelectedCorporate() {
        if (selectedCorporate == null && !addableCorporates.isEmpty()) {
            selectedCorporate = addableCorporates.get(0);
        }
        return selectedCorporate;
    }

    public void setMetadatum(MetadatumImpl meta) {
        this.selectedMetadatum = meta;
    }

    public String getTempMetadataGroupType() {
        if (this.selectedGroup == null) {
            getAddableMetadataGroupTypes();
            this.selectedGroup = this.tempMetadataGroups.get(0);
        }
        this.tempGroupType = selectedGroup.getMetadataGroup().getType().getName();
        return this.selectedGroup.getMetadataGroup().getType().getName();
    }

    public void setTempMetadataGroupType(String tempTyp) {
        if (!selectedGroup.getMetadataGroup().getType().getName().equals(tempTyp)) {
            MetadataGroupType mdt = this.myPrefs.getMetadataGroupTypeByName(tempTyp);
            try {
                MetadataGroup md = new MetadataGroup(mdt);
                this.selectedGroup = new MetadataGroupImpl(myPrefs, myProzess, md, this, metahelper.getMetadataGroupTypeLanguage(mdt), null, 0);
            } catch (MetadataTypeNotAllowedException e) {
                logger.error(e.getMessage());
            }
        }
        this.tempGroupType = tempTyp;
    }

    public MetadataGroupImpl getSelectedGroup() {

        if (this.selectedGroup == null) {
            getAddableMetadataGroupTypes();
            if (tempMetadataGroups != null && !tempMetadataGroups.isEmpty()) {
                this.selectedGroup = this.tempMetadataGroups.get(0);
            }
        }
        return this.selectedGroup;
    }

    public void setSelectedGroup(MetadataGroupImpl meta) {
        this.selectedGroup = meta;
    }

    public String getOutputType() {
        return this.selectedMetadatum.getOutputType();
    }

    public SelectItem[] getStructSeiten() {
        if (this.structSeiten.length > 0 && this.structSeiten[0] == null) {
            return new SelectItem[0];
        } else {
            return this.structSeiten;
        }
    }

    public void BildAnzeigen() {
        this.bildAnzeigen = !this.bildAnzeigen;
        if (!bildAnzeigen) {
            enablePageArea = false;
        } else {
            enablePageArea = myPrefs.getDocStrctTypeByName("area") != null;
        }

    }

    public String getAddDocStructType1() {
        if (addDocStructType1 == null || addDocStructType1.isEmpty()) {
            SelectItem[] list = getAddableDocStructTypenAlsNachbar();
            if (list.length > 0) {
                addDocStructType1 = ((DocStructType) list[0].getValue()).getName();
                createAddableData();
            }
        }
        return this.addDocStructType1;
    }

    public void setAddDocStructType1(String addDocStructType1) {
        this.addDocStructType1 = getSelectedStructType(getAddableDocStructTypenAlsNachbar(), addDocStructType1);

        createAddableData();
    }

    public String getAddDocStructType2() {
        if (addDocStructType2 == null || addDocStructType2.isEmpty()) {
            SelectItem[] list = getAddableDocStructTypenAlsKind();
            if (list.length > 0) {
                addDocStructType2 = ((DocStructType) list[0].getValue()).getName();
                createAddableData();
            }
        }

        return this.addDocStructType2;
    }

    public void setAddDocStructType2(String addDocStructType2) {
        this.addDocStructType2 = getSelectedStructType(getAddableDocStructTypenAlsKind(), addDocStructType2);
        createAddableData();
    }

    public String getBildNummerGeheZu() {
        return "";
    }

    public void setBildNummerGeheZu(String bildNummerGeheZu) {
        this.bildNummerGeheZu = bildNummerGeheZu;
    }

    public void setBildNummerGeheZuCompleteString(String bildNummerGeheZu) {
        try {
            this.bildNummerGeheZu = bildNummerGeheZu.substring(0, bildNummerGeheZu.indexOf(":"));
        } catch (Exception e) {
            this.bildNummerGeheZu = bildNummerGeheZu;
        }
    }

    public String getBildNummerGeheZuCompleteString() {
        return "";
    }

    public String getNeuesElementWohin() {
        if (this.neuesElementWohin == null || this.neuesElementWohin == "") {
            this.neuesElementWohin = "4";
        }
        return this.neuesElementWohin;
    }

    public void setNeuesElementWohin(String inNeuesElementWohin) {
        if (inNeuesElementWohin == null || inNeuesElementWohin.equals("")) {
            this.neuesElementWohin = "1";
        } else {
            if (!inNeuesElementWohin.equals(neuesElementWohin)) {
                if ((neuesElementWohin.equals("1") || neuesElementWohin.equals("2"))
                        && (inNeuesElementWohin.equals("3") || inNeuesElementWohin.equals("4"))) {
                    this.neuesElementWohin = inNeuesElementWohin;
                    getAddDocStructType2();
                    createAddableData();
                } else if ((neuesElementWohin.equals("3") || neuesElementWohin.equals("4"))
                        && (inNeuesElementWohin.equals("1") || inNeuesElementWohin.equals("2"))) {
                    this.neuesElementWohin = inNeuesElementWohin;
                    getAddDocStructType1();
                    createAddableData();

                } else {
                    this.neuesElementWohin = inNeuesElementWohin;
                }
            }
        }
    }

    public List<TreeNode> getStrukturBaum3() {
        if (this.tree3 != null) {
            return this.tree3.getChildrenAsList();
        } else {
            return new ArrayList<>();
        }
    }

    public List<TreeNode> getStrukturBaum3Alle() {
        if (this.tree3 != null) {
            return this.tree3.getChildrenAsListAlle();
        } else {
            return new ArrayList<>();
        }
    }

    public boolean isModusStrukturelementVerschieben() {
        return this.modusStrukturelementVerschieben;
    }

    public void setModusStrukturelementVerschieben(boolean modusStrukturelementVerschieben) {
        this.modusStrukturelementVerschieben = modusStrukturelementVerschieben;

        // wenn der Verschiebevorgang gestartet werden soll, dann in allen
        // DocStructs prüfen
        // ob das aktuelle Strukturelement dort eingefügt werden darf
        if (this.modusStrukturelementVerschieben) {
            List<DocStruct> list = new ArrayList<>();
            list.add(myDocStruct);
            TreeDurchlaufen(this.tree3, list);
        }
    }

    private void TreeDurchlaufen(TreeNodeStruct3 inTreeStruct, List<DocStruct> inDocStructList) {
        for (DocStruct docStruct : inDocStructList) {

            DocStruct temp = inTreeStruct.getStruct();
            if (inTreeStruct.getStruct() == docStruct) {
                inTreeStruct.setSelected(true);
            } else {
                inTreeStruct.setSelected(false);
            }

            // alle erlaubten Typen durchlaufen
            // for (Iterator<String> iter = temp.getType().getAllAllowedDocStructTypes().iterator(); iter.hasNext();) {
            // String dst = iter.next();
            // if (docStruct.getType().getName().equals(dst)) {
            // inTreeStruct.setEinfuegenErlaubt(false);
            // break;
            // }
            // }
            if (!temp.getType().getAllAllowedDocStructTypes().contains(docStruct.getType().getName())) {
                inTreeStruct.setEinfuegenErlaubt(false);
            }
            for (Object element : inTreeStruct.getChildren()) {
                TreeNodeStruct3 kind = (TreeNodeStruct3) element;
                List<DocStruct> list = new ArrayList<>();
                list.add(docStruct);
                TreeDurchlaufen(kind, list);
            }
        }
    }

    public MetadatumImpl getMetadata() {
        return myMetadaten.get(0);
    }

    public String search() {
        if (currentMetadataToPerformSearch != null) {
            if (currentMetadataToPerformSearch.getMetadataDisplaytype() == DisplayType.dante) {
                currentMetadataToPerformSearch.setSearchValue(danteSearchValue);
            } else if (StringUtils.isNotBlank(gndSearchValue)) {
                currentMetadataToPerformSearch.setSearchOption(searchOption);
                currentMetadataToPerformSearch.setSearchValue(gndSearchValue);
            } else if (currentMetadataToPerformSearch.getMetadataDisplaytype() == DisplayType.kulturnav
                    || StringUtils.isNotBlank(kulturnavSearchValue) ) {
                currentMetadataToPerformSearch.setSearchValue(kulturnavSearchValue);
            } else {
                currentMetadataToPerformSearch.setSearchValue(geonamesSearchValue);
            }
            currentMetadataToPerformSearch.search();
            resetSearchValues();
        }
        return "";
    }


    private void resetSearchValues() {
        gndSearchValue = "";
        geonamesSearchValue = "";
        danteSearchValue = "";
        kulturnavSearchValue = "";
    }


    public String getOpacKatalog() {
        if (StringUtils.isBlank(opacKatalog)) {
            opacKatalog = getAllOpacCatalogues().get(0);
            currentCatalogue = catalogues.get(0);
        }
        return this.opacKatalog;
    }

    public void setOpacKatalog(String opacKatalog) {
        if (!this.opacKatalog.equals(opacKatalog)) {
            this.opacKatalog = opacKatalog;
            currentCatalogue = null;
            for (ConfigOpacCatalogue catalogue : catalogues) {
                if (opacKatalog.equals(catalogue.getTitle())) {
                    currentCatalogue = catalogue;
                    break;
                }
            }

            if (currentCatalogue == null) {
                // get first catalogue in case configured catalogue doesn't exist
                currentCatalogue = catalogues.get(0);
            }
        }
    }

    public Map<String, String> getAllSearchFields() {
        if (currentCatalogue == null) {
            return null;
        }
        return currentCatalogue.getSearchFields();
    }

    public List<String> getAllOpacCatalogues() {
        if (catalogues == null) {
            catalogues = ConfigOpac.getInstance().getAllCatalogues();

            catalogueTitles = new ArrayList<>(catalogues.size());
            for (ConfigOpacCatalogue coc : catalogues) {
                catalogueTitles.add(coc.getTitle());
            }
        }
        return catalogueTitles;
    }

    public void setCurrentTifFolder(String currentTifFolder) {
        if (!this.currentTifFolder.equals(currentTifFolder)) {
            this.currentTifFolder = currentTifFolder;
        }
    }

    public List<String> autocomplete(String suggest) {
        String pref = suggest;
        List<String> result = new ArrayList<>();
        List<String> alle = new ArrayList<>();
        for (String key : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(key);
            alle.add(po.getLabel());
        }

        Iterator<String> iterator = alle.iterator();
        while (iterator.hasNext()) {
            String elem = iterator.next();
            if (elem != null && elem.contains(pref) || "".equals(pref)) {
                result.add(elem);
            }
        }
        return result;
    }

    public void autocompleteJson() throws IOException {
        String suggest = Faces.getRequestParameter("suggest");
        List<String> result = autocomplete(suggest);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        externalContext.setResponseContentType("application/json");
        externalContext.setResponseCharacterEncoding("UTF-8");
        externalContext.getResponseOutputWriter().write(new Gson().toJson(result));
        facesContext.responseComplete();
    }

    public boolean getIsNotRootElement() {
        if (this.myDocStruct != null) {
            if (this.myDocStruct.getParent() == null) {
                return false;
            }
        }
        return true;
    }

    public void updateRepresentativePage() {

        if (resetRepresentative) {
            currentRepresentativePage = "";
            resetRepresentative = false;
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                po.setRepresentative(false);
            }
        }

        if (StringUtils.isNotBlank(currentRepresentativePage) && pageMap != null) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                if (po.getPhysicalPageNo().equals(currentRepresentativePage) && po.getType().equals("div")) {
                    po.setRepresentative(true);
                } else {
                    po.setRepresentative(false);
                }
            }
        }
    }

    public void moveSeltectedPagesUp(int positions) {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = document.getPhysicalDocStruct().getAllChildren();
        List<String> pageNoList = new ArrayList<>();
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.isSelected()) {
                pageNoList.add(po.getPhysicalPageNo());
            }
        }

        for (String order : pageNoList) {
            int currentPhysicalPageNo = Integer.parseInt(order) - 1;
            if (currentPhysicalPageNo == 0) {
                break;
            }
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        List<String> newSelectionList = new ArrayList<>();
        for (Integer pageIndex : selectedPages) {
            if (pageIndex - positions < 0) {
                positions = pageIndex;
            }
            DocStruct image = allPages.get(pageIndex);
            allPages.remove(image);
            allPages.add(pageIndex - positions, image);
            newSelectionList.add(String.valueOf(pageIndex - positions + 1));
        }
        setPhysicalOrder(allPages);

        retrieveAllImages();
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if ("div".equals(po.getType()) && newSelectionList.contains(po.getPhysicalPageNo())) {
                po.setSelected(true);
            }
        }
        loadCurrentImages(false);

    }

    private void setPhysicalOrder(List<DocStruct> pages) {
        int physicalCounter = 1;
        for (DocStruct image : pages) {
            Metadata physicalImageNo = image.getAllMetadataByType(myPrefs.getMetadataTypeByName("physPageNumber")).get(0);
            physicalImageNo.setValue("" + physicalCounter++);
        }
    }

    public void moveSelectedPages(String inDirection, int inTimes) {
        long times = inTimes;
        if (times < 1) {
            times = 1;
        }
        if (inDirection.equals("up")) {
            moveSeltectedPagesUp(inTimes);
        } else {
            moveSeltectedPagesDown(inTimes);
        }
    }

    public void moveSeltectedPagesDown(int positions) {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = document.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = new ArrayList<>();
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.isSelected()) {
                pagesList.add(po.getPhysicalPageNo());
            }
        }
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order) - 1;
            if (currentPhysicalPageNo + 1 == allPages.size()) {
                break;
            }
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        List<String> newSelectionList = new ArrayList<>();
        for (Integer pageIndex : selectedPages) {
            if (pageIndex + positions > allPages.size()) {
                positions = allPages.size() - pageIndex - 1;
            }
            DocStruct image = allPages.get(pageIndex);
            allPages.remove(image);
            allPages.add(pageIndex + positions, image);
            newSelectionList.add(String.valueOf(pageIndex + positions + 1));
        }
        setPhysicalOrder(allPages);
        retrieveAllImages();

        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if ("div".equals(po.getType()) && newSelectionList.contains(po.getPhysicalPageNo())) {
                po.setSelected(true);
            }
        }

        loadCurrentImages(false);

    }

    public void deleteSeltectedPages() {
        List<Integer> selectedPages = new ArrayList<>();
        List<DocStruct> allPages = document.getPhysicalDocStruct().getAllChildren();
        List<String> pagesList = new ArrayList<>();
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.isSelected()) {
                pagesList.add(po.getPhysicalPageNo());
            }
        }
        Collections.reverse(pagesList);
        for (String order : pagesList) {
            int currentPhysicalPageNo = Integer.parseInt(order);
            selectedPages.add(currentPhysicalPageNo);
        }

        if (selectedPages.isEmpty()) {
            return;
        }
        for (Integer pageIndex : selectedPages) {

            DocStruct pageToRemove = allPages.get(pageIndex - 1);
            String imagename = pageToRemove.getImageName();

            removeImage(imagename, allPages.size());
            // try {
            document.getFileSet().removeFile(pageToRemove.getAllContentFiles().get(0));
            // pageToRemove.removeContentFile(pageToRemove.getAllContentFiles().get(0));
            // } catch (ContentFileNotLinkedException e) {
            // myLogger.error(e);
            // }

            document.getPhysicalDocStruct().removeChild(pageToRemove);
            List<Reference> refs = new ArrayList<>(pageToRemove.getAllFromReferences());
            for (ugh.dl.Reference ref : refs) {
                ref.getSource().removeReferenceTo(pageToRemove);
            }

            // save current state
            Reload();
        }

        if (document.getPhysicalDocStruct().getAllChildren() != null) {
            myBildLetztes = document.getPhysicalDocStruct().getAllChildren().size();
        } else {
            myBildLetztes = 0;
        }

        loadCurrentImages(true);
        allPages = document.getPhysicalDocStruct().getAllChildren();

        int currentPhysicalOrder = 1;
        if (allPages != null) {
            MetadataType mdt = this.myPrefs.getMetadataTypeByName("physPageNumber");
            for (DocStruct page : allPages) {
                List<? extends Metadata> pageNoMetadata = page.getAllMetadataByType(mdt);
                if (pageNoMetadata == null || pageNoMetadata.size() == 0) {
                    currentPhysicalOrder++;
                    break;
                }
                for (Metadata pageNo : pageNoMetadata) {
                    pageNo.setValue(String.valueOf(currentPhysicalOrder));
                }
                currentPhysicalOrder++;
            }
        }
        retrieveAllImages();
        // current image was deleted, load first image
        if (selectedPages.contains(bildNummer - 1)) {

            BildErsteSeiteAnzeigen();
        } else {

            setImageIndex(bildNummer - 1);

        }

        // save current state
        Reload();

    }

    public void reOrderPagination() {
    	try {
	        String imageDirectory = "";
	        try {
	            imageDirectory = myProzess.getImagesDirectory();
	        } catch (SwapException e) {
	            logger.error(e);
	        } catch (DAOException e) {
	            logger.error(e);
	        } catch (IOException e) {
	            logger.error(e);
	        } catch (InterruptedException e) {
	            logger.error(e);
	
	        }
	        if (imageDirectory.equals("")) {
	            Helper.setFehlerMeldung("ErrorMetsEditorImageRenaming");
	            return;
	        }
	
	        List<String> oldfilenames = new ArrayList<>();
	    	for (DocStruct page : document.getPhysicalDocStruct().getAllChildren()) {
	    		oldfilenames.add(page.getImageName());
	    	}
	
	        // get all folders to check and rename images
	        Map<Path, List<Path>> allFolderAndAllFiles = myProzess.getAllFolderAndFiles();
	        // check size of folders, remove them if they don't match the expected number of files
	        for (Path p : allFolderAndAllFiles.keySet()) {
	            List<Path> files = allFolderAndAllFiles.get(p);
	            if (oldfilenames.size() != files.size()) {
	                files = Collections.emptyList();
	            }
	        }
	
	        progress = 0;
	        totalImageNo = oldfilenames.size() * 2;
	        currentImageNo = 0;
	
	        boolean isWriteable = true;
	        for (Path currentFolder : allFolderAndAllFiles.keySet()) {
	            // check if folder is writeable
	            if (!StorageProvider.getInstance().isWritable(currentFolder)) {
	                isWriteable = false;
	                Helper.setFehlerMeldung(Helper.getTranslation("folderNoWriteAccess", currentFolder.getFileName().toString()));
	            }
	            List<Path> files = allFolderAndAllFiles.get(currentFolder);
	            for (Path file : files) {
	                // check if folder is writeable
	                if (!StorageProvider.getInstance().isWritable(file)) {
	                    isWriteable = false;
	                    Helper.setFehlerMeldung(Helper.getTranslation("fileNoWriteAccess", file.toString()));
	                }
	            }
	        }
	        if (!isWriteable) {
	            return;
	        }
	
	        for (String imagename : oldfilenames) {
	
	            String filenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
	            //            String filenameExtension =  Metadaten.getFileExtension(imagename);
	
	            currentImageNo++;
	
	            // check all folder
	            for (Path currentFolder : allFolderAndAllFiles.keySet()) {
	                // check files in current folder
	                List<Path> files = allFolderAndAllFiles.get(currentFolder);
	                for (Path file : files) {
	                    String filenameToCheck = file.getFileName().toString();
	                    String filenamePrefixToCheck = filenameToCheck.substring(0, filenameToCheck.lastIndexOf("."));
	                    String fileExtension = Metadaten.getFileExtension(filenameToCheck);
	                    // find the current file the folder
	                    if (filenamePrefixToCheck.equals(filenamePrefix)) {
	                        // found file to rename
	                        Path tmpFileName = Paths.get(currentFolder.toString(), filenamePrefix + fileExtension + "_bak");
	                        try {
	                            StorageProvider.getInstance().move(file, tmpFileName);
	                        } catch (IOException e) {
	                            logger.error(e);
	                        }
	                    }
	                }
	            }
	        }
	
	        System.gc();
	        int counter = 1;
	        for (String imagename : oldfilenames) {
	            currentImageNo++;
	            String oldFilenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
	            String newFilenamePrefix = generateFileName(counter);
	            String originalExtension = Metadaten.getFileExtension(imagename.replace("_bak", ""));
	            // update filename in mets file
	            document.getPhysicalDocStruct().getAllChildren().get(counter - 1).setImageName(newFilenamePrefix + originalExtension);
	
	            // check all folder
	            for (Path currentFolder : allFolderAndAllFiles.keySet()) {
	                // check files in current folder
	                List<Path> files = StorageProvider.getInstance().listFiles(currentFolder.toString(), NIOFileUtils.fileFilter);
	                for (Path file : files) {
	                    String filenameToCheck = file.getFileName().toString();
	                    if (filenameToCheck.contains(".")) {
	                        String filenamePrefixToCheck = filenameToCheck.substring(0, filenameToCheck.lastIndexOf("."));
	                        String fileExtension = Metadaten.getFileExtension(filenameToCheck.replace("_bak", ""));
	                        // found right file
	                        if (filenameToCheck.endsWith("bak") && filenamePrefixToCheck.equals(oldFilenamePrefix)) {
	                            // generate new file name
	                            Path renamedFile = Paths.get(currentFolder.toString(), newFilenamePrefix + fileExtension.toLowerCase());
	                            try {
	                                StorageProvider.getInstance().move(file, renamedFile);
	                            } catch (IOException e) {
	                                logger.error(e);
	                            }
	                        }
	                    } else {
	                        logger.debug("the file to be renamed does not contain a '.': " + currentFolder.toString() + filenameToCheck);
	                    }
	                }
	            }
	            counter++;
	        }
	
	        retrieveAllImages();
	        progress = null;
	        totalImageNo = 0;
	        currentImageNo = 0;
	
	        // load new file names
	        changeFolder();
	
	        // save current state
	        Reload();
	
	        Helper.setMeldung("finishedFileRenaming");
    	}catch(Exception e) {
    		logger.error(e);
    		Helper.setFehlerMeldung("ErrorMetsEditorImageRenaming");
    	}
    }

    private void removeImage(String fileToDelete, int totalNumberOfFiles) {
        // TODO find a solution for files in a folder with the same name, but different extensions

        // check what happens with .tar.gz
        String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf("."));

        Map<Path, List<Path>> allFolderAndAllFiles = myProzess.getAllFolderAndFiles();
        // check size of folders, remove them if they don't match the expected number of files

        // check all folder
        for (Path currentFolder : allFolderAndAllFiles.keySet()) {
            // check files in current folder
            List<Path> files = allFolderAndAllFiles.get(currentFolder);
            if (totalNumberOfFiles == files.size()) {
                for (Path file : files) {
                    String filenameToCheck = file.getFileName().toString();
                    String filenamePrefixToCheck = filenameToCheck.substring(0, filenameToCheck.lastIndexOf("."));
                    // find the current file the folder
                    if (filenamePrefixToCheck.equals(fileToDeletePrefix)) {
                        // found file to delete
                        try {
                            StorageProvider.getInstance().deleteFile(file);
                        } catch (IOException e) {
                            logger.error(e);
                            Helper.setFehlerMeldung(Helper.getTranslation("fileNoWriteAccess", file.toString()));
                        }
                    }
                }
            } else {
                Helper.setFehlerMeldung("File " + fileToDelete + " cannot be deleted from folder " + currentFolder.toString()
                + " because number of files differs (" + totalNumberOfFiles + " vs. " + files.size() + ")");
            }
        }

    }

    private static String generateFileName(int counter) {
        String filename = "";
        if (counter >= 10000000) {
            filename = "" + counter;
        } else if (counter >= 1000000) {
            filename = "0" + counter;
        } else if (counter >= 100000) {
            filename = "00" + counter;
        } else if (counter >= 10000) {
            filename = "000" + counter;
        } else if (counter >= 1000) {
            filename = "0000" + counter;
        } else if (counter >= 100) {
            filename = "00000" + counter;
        } else if (counter >= 10) {
            filename = "000000" + counter;
        } else {
            filename = "0000000" + counter;
        }
        return filename;
    }

    public FileManipulation getFileManipulation() {
        if (fileManipulation == null) {
            fileManipulation = new FileManipulation(this);
        }
        return fileManipulation;
    }

    public static String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String afterLastSlash = filename.substring(filename.lastIndexOf('/') + 1);
        // int afterLastBackslash = afterLastSlash.lastIndexOf('\\') + 1;
        int dotIndex = afterLastSlash.lastIndexOf('.');
        return (dotIndex == -1) ? "" : afterLastSlash.substring(dotIndex);
    }

    public void setModusCopyDocstructFromOtherProcess(boolean modusCopyDocstructFromOtherProcess) {
        if (modusCopyDocstructFromOtherProcess) {
            treeOfFilteredProcess = null;
            setDisplayInsertion(false);
            filteredProcess = null;
        }
        this.modusCopyDocstructFromOtherProcess = modusCopyDocstructFromOtherProcess;
    }

    public Boolean getDisplayFileManipulation() {
        return ConfigurationHelper.getInstance().isMetsEditorDisplayFileManipulation();
    }

    public void filterMyProcess() {
        filteredProcess = ProcessManager.getProcessByTitle(filterProcessTitle);
        if (filteredProcess == null) {
            Helper.setFehlerMeldung("kein Vorgang gefunden");
        } else {
            if (!filteredProcess.getRegelsatz().getId().equals(myProzess.getRegelsatz().getId())) {
                Helper.setFehlerMeldung("unterschiedlicher Regelsatz");
            } else {
                Helper.setMeldung("Vorgang gefunden und gleicher Regelsatz: " + filteredProcess.getId());
                loadTreeFromFilteredProcess();

            }
        }
    }

    private void loadTreeFromFilteredProcess() {
        if (this.modusCopyDocstructFromOtherProcess) {
            try {
                treeOfFilteredProcess =
                        buildTree(treeOfFilteredProcess, filteredProcess.readMetadataFile().getDigitalDocument().getLogicalDocStruct(), false);

            } catch (PreferencesException e) {
                logger.error("Error loading the tree for filtered processes (PreferencesException): ", e);

            } catch (ReadException e) {
                logger.error("Error loading the tree for filtered processes (ReadException): ", e);

            } catch (SwapException e) {
                logger.error("Error loading the tree for filtered processes (SwapException): ", e);

            } catch (DAOException e) {
                logger.error("Error loading the tree for filtered processes (DAOException): ", e);

            } catch (WriteException e) {
                logger.error("Error loading the tree for filtered processes (WriteException): ", e);

            } catch (IOException e) {
                logger.error("Error loading the tree for filtered processes (IOException): ", e);

            } catch (InterruptedException e) {
                logger.error("Error loading the tree for filtered processes (InterruptedException): ", e);

            }
            activateAllTreeElements(treeOfFilteredProcess);
        }
    }

    private void activateAllTreeElements(TreeNodeStruct3 inTreeStruct) {
        inTreeStruct.setEinfuegenErlaubt(true);
        for (Object element : inTreeStruct.getChildren()) {
            TreeNodeStruct3 kind = (TreeNodeStruct3) element;
            activateAllTreeElements(kind);
        }
    }

    public List<TreeNode> getStruktureTreeAsTableForFilteredProcess() {
        if (this.treeOfFilteredProcess != null) {
            return this.treeOfFilteredProcess.getChildrenAsListAlle();
        } else {
            return new ArrayList<>();
        }
    }

    public boolean getIsProcessLoaded() {
        return treeOfFilteredProcess != null;
    }

    public void rememberFilteredProcessStruct() {
        activateAllTreeElements(tree3);
        List<DocStruct> selectdElements = getSelectedElements(treeOfFilteredProcess);
        TreeDurchlaufen(this.tree3, selectdElements);

    }

    private List<DocStruct> getSelectedElements(TreeNodeStruct3 currentNode) {
        List<DocStruct> selectdElements = new ArrayList<>();
        for (TreeNode node : currentNode.getChildren()) {
            TreeNodeStruct3 tns = (TreeNodeStruct3) node;
            if (node.isSelected()) {
                selectdElements.add(tns.getStruct().copy(true, true));
            }
            if (tns.getChildren() != null && !tns.getChildren().isEmpty()) {
                selectdElements.addAll(getSelectedElements(tns));
            }
        }
        return selectdElements;
    }

    public void importFilteredProcessStruct() throws TypeNotAllowedAsChildException {

        List<DocStruct> selectdElements = getSelectedElements(treeOfFilteredProcess);

        for (DocStruct docStructFromFilteredProcess : selectdElements) {
            List<Reference> refs = docStructFromFilteredProcess.getAllToReferences();
            List<Reference> clone = new ArrayList<>(refs);
            for (Reference ref : clone) {
                docStructFromFilteredProcess.removeReferenceTo(ref.getTarget());
            }

            this.tempStrukturelement.addChild(docStructFromFilteredProcess);
        }
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        this.neuesElementWohin = "1";
    }

    public void updateAllSubNodes() {
        activateAllTreeElements(treeOfFilteredProcess);
        updateAllSubNodes(treeOfFilteredProcess);
    }

    private void updateAllSubNodes(TreeNodeStruct3 papi) {
        for (TreeNode node : papi.getChildren()) {
            TreeNodeStruct3 tns = (TreeNodeStruct3) node;
            if (papi.isSelected()) {
                tns.setEinfuegenErlaubt(false);
                disableSubelements(tns);
            } else {
                updateAllSubNodes(tns);
            }
        }

    }

    private void disableSubelements(TreeNodeStruct3 element) {
        for (TreeNode node : element.getChildren()) {
            TreeNodeStruct3 tns = (TreeNodeStruct3) node;
            tns.setEinfuegenErlaubt(false);
            tns.setSelected(false);

            disableSubelements(tns);
        }
    }

    // progress bar
    public Integer getProgress() {
        if (progress == null) {
            progress = 0;
        } else if (totalImageNo == 0) {
            progress = 100;
        } else {
            progress = (int) (currentImageNo / totalImageNo * 100);

            if (progress > 100) {
                progress = 100;
            }
        }

        return progress;
    }

    public void onComplete() {
        progress = null;
    }

    public boolean isShowProgressBar() {
        if (progress == null || progress == 100 || progress == 0) {
            return false;
        }
        return true;
    }

    private void createAddableData() {
        String docstructName = "";
        int selection = Integer.valueOf(neuesElementWohin).intValue();
        if (selection < 3) {
            docstructName = getAddDocStructType1();
        } else {
            docstructName = getAddDocStructType2();
        }
        if (StringUtils.isNotBlank(docstructName) && (oldDocstructName.isEmpty() || !oldDocstructName.equals(docstructName))) {
            oldDocstructName = docstructName;

            addableMetadata = new LinkedList<>();
            if (docstructName != null) {

                DocStructType dst = this.myPrefs.getDocStrctTypeByName(docstructName);
                try {
                    DocStruct ds = this.document.createDocStruct(dst);

                    List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(ds, Helper.getMetadataLanguage(),
                            MetadataTypes.METATDATA, this.myProzess, displayHiddenMetadata);
                    if (myTempMetadata != null) {
                        for (Metadata metadata : myTempMetadata) {
                            MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.myProzess, this);
                            // meta.getSelectedItem();
                            addableMetadata.add(meta);
                        }
                    }
                } catch (TypeNotAllowedForParentException e) {
                    logger.error(e);
                }
            }
            addablePersondata = new LinkedList<>();
            if (docstructName != null) {

                DocStructType dst = this.myPrefs.getDocStrctTypeByName(docstructName);
                try {
                    DocStruct ds = this.document.createDocStruct(dst);

                    List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(ds, Helper.getMetadataLanguage(),
                            MetadataTypes.PERSON, this.myProzess, displayHiddenMetadata);
                    if (myTempMetadata != null) {
                        for (Metadata metadata : myTempMetadata) {
                            MetaPerson meta = new MetaPerson((Person) metadata, 0, this.myPrefs, ds, myProzess, this);

                            addablePersondata.add(meta);
                        }
                    }
                } catch (TypeNotAllowedForParentException e) {
                    logger.error(e);
                }
            }

            addableCorporates = new LinkedList<>();
            if (docstructName != null) {

                DocStructType dst = this.myPrefs.getDocStrctTypeByName(docstructName);
                try {
                    DocStruct ds = this.document.createDocStruct(dst);

                    List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(ds, Helper.getMetadataLanguage(),
                            MetadataTypes.CORPORATE, this.myProzess, displayHiddenMetadata);
                    if (myTempMetadata != null) {
                        for (Metadata metadata : myTempMetadata) {
                            addableCorporates.add(new MetaCorporate((Corporate) metadata, myPrefs, ds, myProzess, this));

                        }
                    }
                } catch (TypeNotAllowedForParentException e) {
                    logger.error(e);
                }
            }
        }
    }

    public void changeTopstruct() {
        if (currentTopstruct.getType().getName().equals(logicalTopstruct.getType().getName())) {
            currentTopstruct = physicalTopstruct;
            modusAnsicht = "Metadaten";
        } else {
            currentTopstruct = logicalTopstruct;
        }
        MetadatenalsBeanSpeichern(this.currentTopstruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, true);
    }

    public boolean isPhysicalTopstruct() {
        return currentTopstruct.getType().getName().equals(physicalTopstruct.getType().getName());
    }

    public List<Image> getPaginatorList() {
        List<Image> subList = new ArrayList<>();
        for (Image currentImage : allImages) {
            if (sizeChanged) {
                currentImage.createThumbnailUrls(thumbnailSizeInPixel);
            }
        }

        if (allImages.size() > (pageNo * numberOfImagesPerPage) + numberOfImagesPerPage) {
            subList = allImages.subList(pageNo * numberOfImagesPerPage, (pageNo * numberOfImagesPerPage) + numberOfImagesPerPage);
        } else {
            subList = allImages.subList(pageNo * numberOfImagesPerPage, allImages.size());
        }

        return subList;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
        if (this.imageIndex < 0) {
            this.imageIndex = 0;
        }
        if (this.imageIndex >= getSizeOfImageList()) {
            this.imageIndex = getSizeOfImageList() - 1;
        }
        if (!allImages.isEmpty() && allImages.size() >= this.imageIndex) {
            setImage(allImages.get(this.imageIndex));
            bildNummer = this.imageIndex;
            try {
                this.loadJsonAlto();
            } catch (IOException | JDOMException | SwapException | DAOException | InterruptedException e) {
                SimpleAlto alto = new SimpleAlto("Error reading ALTO, see application log for details.");

                this.currentJsonAlto = new Gson().toJson(alto);
                logger.error(e);
            }
        }
    }

    public void checkSelectedThumbnail(int imageIndex) {
        if (pageMap != null && !pageMap.isEmpty()) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                po.setSelected(false);
            }
            PhysicalObject po = pageMap.get("" + imageIndex);
            if (po != null) {
                po.setSelected(true);
            }
        }
    }

    public String getImageUrl() {
        if (image == null) {
            return null;
        } else {
            FacesContext context = FacesContextHelper.getCurrentFacesContext();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            String currentImageURL = session.getServletContext().getContextPath() + ConfigurationHelper.getTempImagesPath() + session.getId() + "_"
                    + image.getImageName() + "_large_" + ".jpg";
            return currentImageURL.replaceAll("\\\\", "/");
        }
    }

    public int getImageWidth() {
        if (image == null) {
            logger.error("Must set image before querying image size");
            return 0;
        } else {
            return image.getSize().width;
        }
    }

    public int getImageHeight() {
        if (image == null) {
            logger.error("Must set image before querying image size");
            return 0;
        } else {
            return image.getSize().height;
        }
    }

    public String cmdMoveFirst() {

        if (this.pagesRTL) {
            if (this.pageNo != getLastPageNumber()) {
                this.pageNo = getLastPageNumber();
                getPaginatorList();
            }
        } else {
            if (this.pageNo != 0) {
                this.pageNo = 0;
                getPaginatorList();
            }
        }
        return "";
    }

    public String cmdMovePrevious() {
        if (this.pagesRTL) {
            if (!isLastPage()) {
                this.pageNo++;
                getPaginatorList();
            }
        } else {
            if (!isFirstPage()) {
                this.pageNo--;
                getPaginatorList();
            }
        }
        return "";
    }

    public String cmdMoveNext() {
        if (this.pagesRTL) {
            if (!isFirstPage()) {
                this.pageNo--;
                getPaginatorList();
            }
        } else {
            if (!isLastPage()) {
                this.pageNo++;
                getPaginatorList();
            }
        }
        return "";
    }

    public String cmdMoveLast() {
        if (this.pagesRTL) {
            if (this.pageNo != 0) {
                this.pageNo = 0;
                getPaginatorList();
            }
        } else {
            if (this.pageNo != getLastPageNumber()) {
                this.pageNo = getLastPageNumber();
                getPaginatorList();
            }
        }
        return "";
    }

    public void setTxtMoveTo(Integer neueSeite) {
        if ((this.pageNo != neueSeite - 1) && neueSeite > 0 && neueSeite <= getLastPageNumber() + 1) {
            this.pageNo = neueSeite - 1;
            getPaginatorList();
        }
    }

    public Integer getTxtMoveTo() {
        return null;
        //        return this.pageNo + 1;
    }

    public int getLastPageNumber() {
        int ret = Double.valueOf(Math.floor(this.allImages.size() / numberOfImagesPerPage)).intValue();
        if (this.allImages.size() % numberOfImagesPerPage == 0) {
            ret--;
        }
        return ret;
    }

    public boolean isFirstPage() {
        return this.pageNo == 0;
    }

    public boolean isLastPage() {
        return this.pageNo >= getLastPageNumber();
    }

    public boolean hasNextPage() {
        return this.allImages.size() > numberOfImagesPerPage;
    }

    public boolean hasPreviousPage() {
        return this.pageNo > 0;
    }

    public Long getPageNumberCurrent() {
        return Long.valueOf(this.pageNo + 1);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1);
    }

    public int getSizeOfImageList() {
        return allImages.size();
    }

    public int getThumbnailSize() {
        return thumbnailSizeInPixel;
    }

    public void setThumbnailSize(int value) {
        if (value == 0) {
            return;
        }
        if (thumbnailSizeInPixel != value) {
            sizeChanged = true;
            thumbnailSizeInPixel = value;
            getPaginatorList();
            sizeChanged = false;
        }
    }

    public void setImage(final Image image) {
        this.image = image;
    }

    public void setContainerWidth(int containerWidth) {
        if (containerWidth > 100) {
            this.containerWidth = containerWidth;
        }
    }

    public void increaseContainerWidth() {
        containerWidth = containerWidth + 100;
    }

    public void reduceContainerWidth() {
        if (containerWidth > 200) {
            containerWidth = containerWidth - 100;
        }
    }

    public void changeFolder() {
        allImages = new ArrayList<>();
        try {
            List<String> imageNames = imagehelper.getImageFiles(myProzess, currentTifFolder);
            if (imageNames != null && !imageNames.isEmpty()) {
                imageFolderName = myProzess.getImagesDirectory() + currentTifFolder + File.separator;
                int order = 1;
                for (String imagename : imageNames) {
                    Image currentImage = new Image(myProzess, imageFolderName, imagename, order++, thumbnailSizeInPixel);
                    allImages.add(currentImage);
                }
                setImageIndex(imageIndex);
            } else {
                bildNummer = -1;
            }
        } catch (InvalidImagesException | SwapException | DAOException | IOException | InterruptedException e1) {
            logger.error(e1);
        }

    }

    public void setNumberOfImagesPerPage(int numberOfImagesPerPage) {
        if (numberOfImagesPerPage == 0) {
            return;
        }
        if (this.numberOfImagesPerPage != numberOfImagesPerPage) {
            this.numberOfImagesPerPage = numberOfImagesPerPage;
            pageNo = 0;
            getPaginatorList();
        }
    }

    public List<String> getPossibleDatabases() {
        if (normdataList.isEmpty()) {
            List<NormDatabase> databaseList = NormDatabase.getAllDatabases();
            for (NormDatabase norm : databaseList) {
                normdataList.add(norm.getAbbreviation());
            }
        }
        return normdataList;
    }

    public List<String> getPossibleNamePartTypes() {
        List<String> possibleNamePartTypes = new ArrayList<>();
        possibleNamePartTypes.add("date");
        possibleNamePartTypes.add("termsOfAddress");
        return possibleNamePartTypes;
    }

    public void reloadMetadataList() {
        MetadatenalsBeanSpeichern(currentTopstruct);
    }

    /**
     * Check if {@link MetadataType} can be duplicated in current {@link DocStruct}
     *
     * @param mdt the {@link MetadataType} to check
     * @return true if duplication is allowed
     */

    public boolean isAddableMetadata(MetadataType mdt) {
        if (myDocStruct != null && myDocStruct.getAddableMetadataTypes(false) != null) {
            for (MetadataType type : myDocStruct.getAddableMetadataTypes(false)) {
                if (type.getName().equals(mdt.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if {@link Metadata} can be duplicated in current {@link DocStruct}
     *
     * @param md the {@link MetadataType} to check
     * @return true if duplication is allowed
     */

    public boolean isAddableMetadata(Metadata md) {
        if (md.getParent() != null && md.getParent().getAddableMetadataTypes(false) != null) {
            for (MetadataType type : md.getParent().getAddableMetadataTypes(false)) {
                if (type.getName().equals(md.getType().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if {@link MetadataType} can be duplicated in current {@link DocStruct}
     *
     * @param mdt the role to check
     * @return true if duplication is allowed
     */

    public boolean isAddablePerson(MetadataType mdt) {
        for (MetadataType type : myDocStruct.getAddableMetadataTypes(false)) {
            if (type.getName().equals(mdt.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<PhysicalObject> getAllPages() {
        if (pageMap != null && !pageMap.isEmpty()) {
            List<PhysicalObject> list = new ArrayList<>(pageMap.size());
            for (String key : pageMap.getKeyList()) {
                list.add(pageMap.get(key));
            }

            return list;
        }
        return null;
    }

    public void setCurrentMetadataToPerformSearch(SearchableMetadata currentMetadataToPerformSearch) {
        if (this.currentMetadataToPerformSearch == null || !this.currentMetadataToPerformSearch.equals(currentMetadataToPerformSearch)) {
            this.currentMetadataToPerformSearch = currentMetadataToPerformSearch;

            if (this.currentMetadataToPerformSearch != null) {
                this.currentMetadataToPerformSearch.clearResults();
            }
        }
    }
}
