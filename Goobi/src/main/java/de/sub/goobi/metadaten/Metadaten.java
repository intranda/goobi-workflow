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

package de.sub.goobi.metadaten;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.AltoChange;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.SimpleAlto;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.cli.helper.OrderedKeyMap;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IMetadataEditorExtension;
import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.jdom2.JDOMException;
import org.omnifaces.util.Faces;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.HelperForm;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperComparator;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.TreeNode;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.InvalidImagesException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.metadaten.MetaConvertibleDate.DateType;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import io.goobi.workflow.api.connection.HttpUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class Metadaten implements Serializable {

    private static final long serialVersionUID = 2361148967408139027L;

    private static final String REDIRECT_TO_METSEDITOR = "metseditor";
    private static final String REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT = "metseditor_timeout";

    @Getter
    @Setter
    private boolean displayInsertion = false;
    @Getter
    @Setter
    private String filterProcessTitle = "";
    @Getter
    private Process filteredProcess = null;
    private String oldDocstructName = "";
    @Setter // needed to mock it in junit test
    private transient MetadatenImagesHelper imagehelper;
    private transient MetadatenHelper metahelper;
    @Getter
    @Setter
    private boolean treeReloaden = false;
    private transient Fileformat gdzfile;
    @Getter
    private DocStruct myDocStruct;
    @Setter
    private DocStruct tempStrukturelement;
    @Getter
    @Setter
    private transient List<MetadatumImpl> myMetadaten = new LinkedList<>();
    @Getter
    @Setter
    private transient List<MetaPerson> myPersonen = new LinkedList<>();
    @Getter
    @Setter
    private transient List<MetaCorporate> corporates = new LinkedList<>();

    @Getter
    private transient List<MetadataGroupImpl> groups = new LinkedList<>();
    @Getter
    @Setter
    private transient MetadataGroupImpl currentGroup;
    private transient MetadataGroupImpl selectedGroup;
    @Getter
    @Setter
    private transient List<MetadataGroupImpl> tempMetadataGroups = new ArrayList<>();
    private String tempGroupType;
    @Getter
    @Setter
    private transient MetadatumImpl curMetadatum;
    @Getter
    @Setter
    private Metadata currentMetadata;
    @Getter
    @Setter
    private Corporate currentCorporate;
    @Getter
    @Setter
    private transient MetaPerson curPerson;
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
    private String pageSelectionFirstPage;
    @Getter
    @Setter
    private String pageSelectionLastPage;
    @Getter
    @Setter
    private String[] structSeitenAuswahl;
    @Getter
    @Setter
    private String[] alleSeitenAuswahl;
    @Getter
    private transient OrderedKeyMap<String, PhysicalObject> pageMap;
    private transient MetadatumImpl[] logicalPageNumForPages;
    @Getter
    @Setter
    private transient ArrayList<MetadatumImpl> tempMetadatumList = new ArrayList<>();
    @Getter
    @Setter
    private transient MetadatumImpl selectedMetadatum;

    @Setter
    private transient MetaCorporate selectedCorporate;

    @Setter
    private transient PhysicalObject currentPage;
    @Getter
    @Setter
    private String currentRepresentativePage = "";
    @Getter
    @Setter
    private boolean resetRepresentative = false;

    private transient PhysicalObject lastAddedObject = null;
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

    private SelectItem[] structSeiten;
    private transient MetadatumImpl[] structSeitenNeu;
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
    private String modusAnsicht = "Metadaten"; //NOSONAR
    @Getter
    private boolean modusCopyDocstructFromOtherProcess = false;

    private transient TreeNodeStruct3 treeOfFilteredProcess;
    private transient TreeNodeStruct3 tree3;
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
    private transient FileManipulation fileManipulation;

    private double currentImageNo = 0;
    private double totalImageNo = 0;
    @Setter
    private Integer progress;

    private boolean tiffFolderHasChanged = true;

    private List<String> dataList = new ArrayList<>();
    @Getter
    private transient List<MetadatumImpl> addableMetadata = new LinkedList<>();

    @Getter
    private transient List<MetaCorporate> addableCorporates = new LinkedList<>();

    @Getter
    private transient List<MetaPerson> addablePersondata = new LinkedList<>();
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
    private transient List<Image> allImages;
    @Getter
    private transient Image image = null;
    @Getter
    private int containerWidth = 600;
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
    private transient SearchableMetadata currentMetadataToPerformSearch;
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

    private transient List<ConfigOpacCatalogue> catalogues = null;
    private List<String> catalogueTitles;
    private transient ConfigOpacCatalogue currentCatalogue;

    @Getter
    @Setter
    private boolean doublePage;

    @Getter
    private String currentJsonAlto;

    private transient PageAreaManager pageAreaManager;

    private transient ImageCommentPropertyHelper commentPropertyHelper;

    //this is set whenever setImage() is called.
    @Getter
    private boolean showImageComments = false;

    private boolean useThumbsDir = false;

    @Getter
    private List<IMetadataEditorExtension> extensions;

    @Getter
    @Setter
    private IMetadataEditorExtension extension;

    public enum MetadataTypes {
        PERSON,
        CORPORATE,
        METADATA
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
            this.treeProperties.put("fullexpanded", Boolean.valueOf(true)); //NOSONAR
            this.treeProperties.put("showpagesasajax", Boolean.valueOf(false)); //NOSONAR
            this.treeProperties.put("showThumbnails", Boolean.valueOf(false)); //NOSONAR
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return "";
    }

    public String Hinzufuegen() {
        this.modusHinzufuegen = true;
        currentMetadataToPerformSearch = null;
        getMetadatum().setValue("");
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return "";
    }

    public String HinzufuegenPerson() {
        this.modusHinzufuegenPerson = true;
        this.tempPersonNachname = "";
        this.tempPersonVorname = "";
        currentMetadataToPerformSearch = null;
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return "";
    }

    public String Reload() {

        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        } else {
            try {
                processHasNewTemporaryMetadataFiles = false;
                setRepresentativeMetadata();
                this.myProzess.writeMetadataFile(this.gdzfile);

            } catch (Exception e) {
                Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e); //NOSONAR
                log.error(e);
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
                if ("_directionRTL".equals(md.getType().getName())) { //NOSONAR
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
                    } catch (MetadataTypeNotAllowedException exception) {
                        log.error(exception);
                    }
                }
            }
        }
        boolean match = false;
        if (this.document.getPhysicalDocStruct() != null && this.document.getPhysicalDocStruct().getAllMetadata() != null
                && !this.document.getPhysicalDocStruct().getAllMetadata().isEmpty()) {
            for (Metadata md : this.document.getPhysicalDocStruct().getAllMetadata()) {
                if ("_representative".equals(md.getType().getName())) { //NOSONAR
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
            } catch (MetadataTypeNotAllowedException exception) {
                // ignore this
            }

        }
    }

    public String toggleImageView() {
        pageNo = ((imageIndex) / numberOfImagesPerPage);
        return "";
    }

    public String automaticSave() {

        try {
            processHasNewTemporaryMetadataFiles = true;
            updateRepresentativePage();
            this.myProzess.saveTemporaryMetsFile(this.gdzfile);

        } catch (Exception e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e);
            log.error(e);
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
            log.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e);
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            log.error("Fehler beim Kopieren von Metadaten (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        currentMetadata = null;
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            log.error(e);
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        currentCorporate = null;
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            log.error("Fehler beim Kopieren von Personen (IncompletePersonObjectException): " + e.getMessage());
        } catch (MetadataTypeNotAllowedException e) {
            log.error("Fehler beim Kopieren von Personen (MetadataTypeNotAllowedException): " + e.getMessage());
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
                log.error("Error while changing DocStructTypes: " + e);
            }
        }
        return REDIRECT_TO_METSEDITOR;
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
                if ("TitleDocMain".equals(this.tempTyp) && this.myPrefs.getMetadataTypeByName("TitleDocMainShort") != null) {
                    try {
                        Metadata md2 = new Metadata(this.myPrefs.getMetadataTypeByName("TitleDocMainShort"));
                        md2.setValue(this.selectedMetadatum.getValue());
                        this.myDocStruct.addMetadata(md2);
                    } catch (MetadataTypeNotAllowedException e) {
                        log.error("Error while adding title (MetadataTypeNotAllowedException): " + e.getMessage());
                    }
                }
            }
        } catch (MetadataTypeNotAllowedException e) {
            log.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        this.modusHinzufuegen = false;
        this.selectedMetadatum.setValue("");
        this.tempWert = "";
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
                md.addCorporate(corporate);
            }
            if (currentGroup != null) {
                currentGroup.getMetadataGroup().addMetadataGroup(md);
            } else {
                this.myDocStruct.addMetadataGroup(md);
            }
        } catch (MetadataTypeNotAllowedException e) {
            log.error("Error while adding metadata (MetadataTypeNotAllowedException): " + e.getMessage());
        }

        this.modeAddGroup = false;
        for (MetadatumImpl mdi : selectedGroup.getMetadataList()) {
            mdi.setValue("");
        }
        for (MetaPerson mp : selectedGroup.getPersonList()) {
            mp.setVorname("");
            mp.setNachname("");
        }
        for (MetaCorporate mc : selectedGroup.getCorporateList()) {
            mc.setMainName("");
            mc.getSubNames().clear();
            mc.addSubName();
            mc.setPartName("");

        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        return "";
    }

    public String loadRightFrame() {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        if ("3".equals(neuesElementWohin) || "4".equals(neuesElementWohin)) {
            if (!docStructIsAllowed(getAddableDocStructTypenAlsKind(), getAddDocStructType2())) {
                setAddDocStructType2("");
            }

        } else if (!docStructIsAllowed(getAddableDocStructTypenAlsNachbar(), getAddDocStructType1())) {
            setAddDocStructType1("");
        }
        if (!tempMetadatumList.isEmpty()) {
            tempTyp = tempMetadatumList.get(0).getMd().getType().getName();
            selectedMetadatum = tempMetadatumList.get(0);
        }
        return REDIRECT_TO_METSEDITOR;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return "";
    }

    public String deleteGroup() {
        MetadataGroup mg = currentGroup.getMetadataGroup();
        mg.getParent().removeMetadataGroup(mg, true);
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return "";
    }

    /**
     * This method is called when the "convert date" button is clicked on the website. It will brute-force convert the currently present date (if it
     * is present) into the Gregorian calendar, assuming the given calendar corresponds to the parameter given.
     * 
     * @param dateType Type of the date given (valid are BRITISH, JULIAN and GREGORIAN)
     * @return An error message if the saving times out.
     */
    public String convertDate(DateType dateType) {
        HoldingElement he = curMetadatum.getMd().getParent();
        if (he != null) {
            MetaConvertibleDate currentDate = new MetaConvertibleDate(curMetadatum.getValue(), dateType);
            if (currentDate.isValid()) {
                MetaConvertibleDate gregorianDate = currentDate.convert(DateType.GREGORIAN);
                String gregorianDateString = gregorianDate.getDate();
                curMetadatum.setValue(gregorianDateString);
                curMetadatum.setNormdataValue(gregorianDateString);
                curMetadatum.setNormDatabase(gregorianDateString);
            }
        } else {
            // we have a default metadata field, clear it
            curMetadatum.setValue("");
            curMetadatum.setNormdataValue("");
            curMetadatum.setNormDatabase("");
        }
        MetadatenalsBeanSpeichern(this.myDocStruct);
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
        c.setSortierart("MetadatenTypen"); //NOSONAR
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
                log.error("Fehler beim sortieren der Metadaten: " + e.getMessage()); //NOSONAR
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
                log.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
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

            } catch (MetadataTypeNotAllowedException e) {
                log.error("Fehler beim sortieren der Metadaten: " + e.getMessage());
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
        SelectItem[] myTypen = new SelectItem[zaehler];

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
        SelectItem[] myTypenOhneUnterstrich = new SelectItem[zaehler];
        System.arraycopy(myTypen, 0, myTypenOhneUnterstrich, 0, zaehler);
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
        SelectItem[] myTypen = new SelectItem[zaehler];

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
        SelectItem[] myTypenOhneUnterstrich = new SelectItem[zaehler];
        System.arraycopy(myTypen, 0, myTypenOhneUnterstrich, 0, zaehler);
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

        String result = "";

        result = readXmlAndBuildTree();

        return result;
    }

    private String readXmlAndBuildTree() {

        // Refreshing the configuration because the display rules may have changed
        ConfigDisplayRules.getInstance().refresh();

        // Parameter names for request parameters
        String parameterBack = "zurueck";
        String parameterReadOnly = "nurLesen";
        String parameterDiscardChanges = "discardChanges";
        String parameterOverwriteChanges = "overwriteChanges";
        String parameterProcessId = "ProzesseID";
        String parameterUserId = "BenutzerID";

        try {
            Integer id = Integer.valueOf(Helper.getRequestParameter(parameterProcessId));
            this.myProzess = ProcessManager.getProcessById(id);
        } catch (NumberFormatException e1) {
            Helper.setFehlerMeldung("error while loading process data " + e1.getMessage());
            log.error(e1);
            return Helper.getRequestParameter(parameterBack);
        }
        processHasNewTemporaryMetadataFiles = false;
        this.myBenutzerID = Helper.getRequestParameter(parameterUserId);
        this.pageSelectionFirstPage = "";
        this.pageSelectionLastPage = "";
        this.zurueck = Helper.getRequestParameter(parameterBack);
        this.nurLesenModus = "true".equals(Helper.getRequestParameter(parameterReadOnly));
        this.neuesElementWohin = "4";
        this.tree3 = null;
        image = null;
        myBild = null;
        dataList = null;
        treeProperties.put("showThumbnails", false);
        treeProperties.put("showOcr", false);
        if ("true".equals(Helper.getRequestParameter(parameterDiscardChanges))) {
            myProzess.removeTemporaryMetadataFiles();
        } else if ("true".equals(Helper.getRequestParameter(parameterOverwriteChanges))) {
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
            return Helper.getRequestParameter(parameterBack);
        } catch (ReadException | PreferencesException | IOException | DAOException e) {
            Helper.setFehlerMeldung("Error while loading metadata", e);
            return Helper.getRequestParameter(parameterBack);
        }
        getAddDocStructType2();
        createAddableData();

        TreeExpand();
        this.sperrung.setLocked(this.myProzess.getId().intValue(), this.myBenutzerID);
        return REDIRECT_TO_METSEDITOR;
    }

    /**
     * Metadaten Einlesen
     *
     * @throws ReadException
     * @throws InterruptedException
     * @throws IOException
     * @throws PreferencesException
     * @throws DAOException
     * @throws SwapException
     * @throws WriteException
     */

    public String XMLlesenStart() throws ReadException, IOException, PreferencesException, SwapException, DAOException {
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

        //initialize page area editor
        this.pageAreaManager = new PageAreaManager(this.myPrefs, this.document);

        checkImageNames();
        retrieveAllImages();
        // check filenames, correct them

        // initialize image list
        numberOfImagesPerPage = ConfigurationHelper.getInstance().getMetsEditorNumberOfImagesPerPage();
        thumbnailSizeInPixel = ConfigurationHelper.getInstance().getMetsEditorThumbnailSize();

        pageNo = 0;
        imageIndex = 0;
        loadCurrentImages(true);

        if (this.document.getPhysicalDocStruct().getAllMetadata() != null && !this.document.getPhysicalDocStruct().getAllMetadata().isEmpty()) {

            List<Metadata> lstMetadata = this.document.getPhysicalDocStruct().getAllMetadata();
            for (Metadata md : lstMetadata) {
                if ("_representative".equals(md.getType().getName())) {
                    try {
                        Integer value = Integer.valueOf(md.getValue());
                        currentRepresentativePage = String.valueOf(value);
                        updateRepresentativePage();
                    } catch (Exception exception) {
                        log.warn(exception);
                    }
                }
            }
            DocStruct docstruct = logicalTopstruct;
            if (docstruct.getType().isAnchor()) {
                docstruct = docstruct.getAllChildren().get(0);
            }
            lstMetadata = docstruct.getAllMetadata();
            if (lstMetadata != null) {
                for (Metadata md : lstMetadata) {
                    if ("_directionRTL".equals(md.getType().getName())) {
                        try {
                            Boolean value = Boolean.valueOf(md.getValue());
                            this.pagesRTL = value;
                        } catch (Exception exception) {
                            log.warn(exception);
                        }
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

        readMetadataEditorExtensions();
        return REDIRECT_TO_METSEDITOR;
    }

    private void loadCurrentImages(boolean jumpToFirstPage) {
        allImages = new ArrayList<>();
        try {
            dataList = imagehelper.getImageFiles(myProzess, currentTifFolder, useThumbsDir);
            if (dataList != null && !dataList.isEmpty()) {
                imageFolderName = myProzess.getImagesDirectory() + currentTifFolder + File.separator;
                imageFolderName = imageFolderName.replace("\\\\", "/");
                int order = 1;

                for (String imageName : dataList) {
                    Image currentImage = new Image(myProzess, imageFolderName, imageName, order++, thumbnailSizeInPixel);
                    allImages.add(currentImage);
                }
                if (jumpToFirstPage) {
                    setImageIndex(0);
                }
            }
        } catch (InvalidImagesException | SwapException | DAOException | IOException e1) {
            log.error(e1);
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
            if (element.getAllChildren() != null && !element.getAllChildren().isEmpty()) {
                for (DocStruct ds : element.getAllChildren()) {
                    createDefaultValues(ds);
                }
            }
        }
    }

    public boolean isCheckForRepresentative() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_representative");
        return mdt != null;
    }

    public boolean isCheckForReadingDirection() {
        MetadataType mdt = myPrefs.getMetadataTypeByName("_directionRTL");
        return mdt != null;
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
            log.error(e);
        } catch (Exception e) {
            Helper.setFehlerMeldung("error while counting current images", e);
            log.error(e);
        }
        /* xml-Datei speichern */
        /*
         * --------------------- vor dem Speichern alle ungenutzen Docstructs rauswerfen -------------------
         */
        this.metahelper.deleteAllUnusedElements(this.document.getLogicalDocStruct());
        updateRepresentativePage();

        //reading direction
        setRepresentativeMetadata();
        boolean writeMetadata = false;
        try {
            writeMetadata = this.myProzess.writeMetadataFile(this.gdzfile);
        } catch (Exception e) {
            Helper.setFehlerMeldung("Metafile is not writable.", e);
            log.error(e);
            return "Metadaten";
        }
        myProzess.removeTemporaryMetadataFiles();

        SperrungAufheben();
        return writeMetadata ? this.zurueck : "";
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
                MetadataTypes.METADATA, this.myProzess, displayHiddenMetadata);
        if (myTempMetadata != null) {
            for (Metadata metadata : myTempMetadata) {
                MetadatumImpl meta = new MetadatumImpl(metadata, 0, this.myPrefs, this.myProzess, this);
                meta.getSelectedItem();
                meta.setValidationErrorPresent(metadata.isValidationErrorPresent());
                meta.setValidationMessage(metadata.getValidationMessage());
                //the validation error has been moved to the UI class (MetadatumImpl) and can be deleted from the UGH object
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

        List<MetadataGroup> metadataGroups =
                this.metahelper.getMetadataGroupsInclDefaultDisplay(inStrukturelement, Helper.getMetadataLanguage(), this.myProzess);
        if (metadataGroups != null) {
            int counter = 1;
            for (MetadataGroup mg : metadataGroups) {
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return REDIRECT_TO_METSEDITOR;
    }

    private TreeNodeStruct3 buildTree(TreeNodeStruct3 inTree, DocStruct inLogicalTopStruct, boolean expandAll) {
        TreeNodeStruct3 knoten;
        List<DocStruct> status = new ArrayList<>();

        /*
         * -------------------------------- den Ausklapp-Zustand aller Knoten erfassen --------------------------------
         */
        if (inTree != null) {
            for (HashMap<String, Object> hashMap : inTree.getChildrenAsList()) {
                knoten = (TreeNodeStruct3) hashMap.get("node");
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
        for (HashMap<String, Object> hashMap : inTree.getChildrenAsListAlle()) {
            knoten = (TreeNodeStruct3) hashMap.get("node");
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
    private void MetadatenalsTree3Einlesen2(DocStruct inStrukturelement, TreeNodeStruct3 oberKnoten) {

        if (currentTopstruct != null && "BoundBook".equals(currentTopstruct.getType().getName())) {
            if (inStrukturelement.getAllMetadata() != null) {
                String phys = "";
                String log = "";
                for (Metadata md : inStrukturelement.getAllMetadata()) {
                    oberKnoten.addMetadata(md.getType().getLanguage(Helper.getMetadataLanguage()), md.getValue());
                    if ("logicalPageNumber".equals(md.getType().getName())) { //NOSONAR
                        log = md.getValue();
                    }
                    if ("physPageNumber".equals(md.getType().getName())) { //NOSONAR
                        phys = md.getValue();
                    }
                }
                if (phys != null && phys.length() > 0) {
                    oberKnoten.setFirstImage(new MutablePair<>(phys, log));
                }
            }
        } else {
            String mainTitle = MetadatenErmitteln(inStrukturelement, "TitleDocMain");
            oberKnoten.setMainTitle(mainTitle);
            oberKnoten.addMetadata(Helper.getTranslation("haupttitel"), mainTitle);
            oberKnoten.addMetadata(Helper.getTranslation("identifier"), MetadatenErmitteln(inStrukturelement, "IdentifierDigital"));
            MutablePair<String, String> first = this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_FIRST);
            if (first != null) {
                oberKnoten.setFirstImage(first);
                oberKnoten.addMetadata(Helper.getTranslation("firstImage"),
                        oberKnoten.getFirstImage().getLeft() + ":" + oberKnoten.getFirstImage().getRight());
            }
            MutablePair<String, String> last = this.metahelper.getImageNumber(inStrukturelement, MetadatenHelper.PAGENUMBER_LAST);
            if (last != null) {
                oberKnoten.setLastImage(last);
                oberKnoten.addMetadata(Helper.getTranslation("lastImage"),
                        oberKnoten.getLastImage().getLeft() + ":" + oberKnoten.getLastImage().getRight());
            }
            oberKnoten.addMetadata(Helper.getTranslation("partNumber"), MetadatenErmitteln(inStrukturelement, "PartNumber"));
            oberKnoten.addMetadata(Helper.getTranslation("dateIssued"), MetadatenErmitteln(inStrukturelement, "DateIssued"));
        }
        // wenn es ein Periodical oder PeriodicalVolume ist, dann ausklappen
        if ("Periodical".equals(inStrukturelement.getType().getName()) || "PeriodicalVolume".equals(inStrukturelement.getType().getName())) {
            oberKnoten.setExpanded(true);
        }
        if (inStrukturelement != null) {
            if (oberKnoten != null) {
                oberKnoten.setValidationErrorPresent(inStrukturelement.isValidationErrorPresent());
                oberKnoten.setValidationMessage(inStrukturelement.getValidationMessage());
            }
            //we moved the validation information to the UI class (TreeNodeStruct3), so we don't need it on the docStruct anymore
            //because of that, we reset the validation error on the docStruct, so the next validation run won't have false positives
            inStrukturelement.setValidationErrorPresent(false);
            inStrukturelement.setValidationMessage(null);
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
                oberKnoten.addChild(tns);
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
        StringBuilder bld = new StringBuilder();
        List<Metadata> allMDs = inStrukturelement.getAllMetadata();
        if (allMDs != null) {
            for (Metadata md : allMDs) {
                if (md.getType().getName().equals(inTyp) && md.getValue() != null) {
                    bld.append(md.getValue());
                    bld.append(" ");
                }
            }
        }
        return bld.toString().trim();
    }

    public void setMyStrukturelement(DocStruct inStruct) {
        this.modusHinzufuegen = false;
        this.modusHinzufuegenPerson = false;
        MetadatenalsBeanSpeichern(inStruct);

        /*
         * -------------------------------- die Selektion kenntlich machen --------------------------------
         */
        for (HashMap<String, Object> hashMap : this.tree3.getChildrenAsListAlle()) {

            TreeNodeStruct3 knoten = (TreeNodeStruct3) hashMap.get("node");
            // Selection wiederherstellen
            knoten.setSelected(this.myDocStruct == knoten.getStruct());
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
            if (log.isDebugEnabled()) {
                log.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Fehler beim Verschieben des Knotens: " + e.getMessage());
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
        if (this.myDocStruct != null && this.myDocStruct.getParent() != null) {
            this.myDocStruct.getParent().removeChild(this.myDocStruct);
            this.tempStrukturelement.addChild(this.myDocStruct);
            MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
            this.neuesElementWohin = "1";
            return REDIRECT_TO_METSEDITOR;
        } else {
            return "";
        }
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
            if (this.myDocStruct.getAllToReferences() != null) {
                List<DocStruct> pageAreas = this.myDocStruct.getAllToReferences()
                        .stream()
                        .map(Reference::getTarget)
                        .filter(Objects::nonNull)
                        .filter(t -> StringUtils.isNotBlank(MetadatenErmitteln(t, "_COORDS")))
                        .collect(Collectors.toList());
                for (DocStruct area : pageAreas) {
                    if (area.getAllFromReferences() != null) {
                        List<DocStruct> referencedLogDs =
                                area.getAllFromReferences().stream().map(Reference::getSource).filter(Objects::nonNull).collect(Collectors.toList());
                        if (referencedLogDs.isEmpty() || (referencedLogDs.size() == 1 && referencedLogDs.get(0).equals(this.myDocStruct))) {
                            area.getParent().removeChild(area);
                        }
                    }
                }
            }
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
                    ds.addReferenceTo(last.getTarget(), "logical_physical"); //NOSONAR
                }
                siblings.add(index + 1, ds);
                myDocStruct = ds;
            } catch (TypeNotAllowedForParentException e) {
                log.error(e);
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
        if ("1".equals(this.neuesElementWohin)) {
            if (getAddDocStructType1() == null || "".equals(getAddDocStructType1())) {
                return REDIRECT_TO_METSEDITOR;
            }
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType1());
            ds = this.document.createDocStruct(dst);
            if (this.myDocStruct == null) {
                return REDIRECT_TO_METSEDITOR;
            }
            DocStruct parent = this.myDocStruct.getParent();
            if (parent == null) {
                if (log.isDebugEnabled()) {
                    log.debug("the current element has no parent element"); //NOSONAR
                }
                return REDIRECT_TO_METSEDITOR;
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
        if ("2".equals(this.neuesElementWohin)) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType1());
            ds = this.document.createDocStruct(dst);
            DocStruct parent = this.myDocStruct.getParent();
            if (parent == null) {
                if (log.isDebugEnabled()) {
                    log.debug("the current element has no parent element");
                }
                return REDIRECT_TO_METSEDITOR;
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
        if ("3".equals(this.neuesElementWohin)) {
            DocStructType dst = this.myPrefs.getDocStrctTypeByName(getAddDocStructType2());
            ds = this.document.createDocStruct(dst);
            DocStruct parent = this.myDocStruct;
            if (parent == null) {
                if (log.isDebugEnabled()) {
                    log.debug("the current element has no parent element");
                }
                return REDIRECT_TO_METSEDITOR;
            }
            List<DocStruct> alleDS = new ArrayList<>();
            alleDS.add(ds);

            if (parent.getAllChildren() != null && !parent.getAllChildren().isEmpty()) {
                alleDS.addAll(parent.getAllChildren());
                parent.getAllChildren().retainAll(new ArrayList<>());
            }

            // add all children again
            for (DocStruct docStruct : alleDS) {
                parent.addChild(docStruct);
            }
        }

        // add element as first child element
        if ("4".equals(this.neuesElementWohin)) {
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
                    log.error(e);
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
                    log.error(e);
                }
            }
        }
        if (!addableCorporates.isEmpty()) {
            for (MetaCorporate corp : addableCorporates) {
                Corporate corporate = corp.getCorporate();
                try {
                    ds.addCorporate(corporate);
                } catch (MetadataTypeNotAllowedException e) {
                    log.error(e);
                }
            }
        }

        if (!"".equals(this.pagesStart) && !"".equals(this.pagesEnd)) {
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
        }

        //if page area was set, assign to docStruct
        if (this.pageAreaManager.hasNewPageArea()) {
            this.pageAreaManager.assignToPhysicalDocStruct(this.pageAreaManager.getNewPageArea(), getCurrentPage());
            this.pageAreaManager.assignToLogicalDocStruct(this.pageAreaManager.getNewPageArea(), ds);
            this.pageAreaManager.resetNewPageArea();
            retrieveAllImages();
        }

        // if easy pagination is switched on, use the last page as first page for next structure element
        if (enableFastPagination) {
            pagesStart = pagesEnd;
        } else {
            pagesStart = "";
        }
        pagesEnd = "";

        oldDocstructName = "";
        createAddableData();
        return MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
    }

    /**
     * mögliche Docstructs als Kind zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsKind() {
        return this.metahelper.getAddableDocStructTypen(this.myDocStruct, false); // list of items
    }

    /**
     * mögliche Docstructs als Nachbar zurückgeben ================================================================
     */
    public SelectItem[] getAddableDocStructTypenAlsNachbar() {
        return this.metahelper.getAddableDocStructTypen(this.myDocStruct, true); // list of items
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
            dataList = imagehelper.checkImageNames(this.myProzess, currentTifFolder);
        } catch (TypeNotAllowedForParentException | SwapException | DAOException | IOException e) {
            log.error(e);
        }
    }

    /**
     * Markus baut eine Seitenstruktur aus den vorhandenen Images ================================================================
     *
     * @throws DAOException
     * @throws SwapException
     */
    public String createPagination() throws TypeNotAllowedForParentException, IOException, SwapException, DAOException {
        this.imagehelper.createPagination(this.myProzess, this.currentTifFolder);
        retrieveAllImages();

        // added new
        if (ConfigurationHelper.getInstance().isMetsEditorEnableImageAssignment()) {
            DocStruct logical = this.document.getLogicalDocStruct();
            if (logical.getType().isAnchor()) {
                if (logical.getAllChildren() != null && !logical.getAllChildren().isEmpty()) {
                    logical = logical.getAllChildren().get(0);
                } else {
                    return "";
                }
            }

            if (logical.getAllChildren() != null) {
                for (DocStruct child : logical.getAllChildren()) {
                    List<Reference> childRefs = child.getAllReferences("to");
                    for (Reference toAdd : childRefs) {
                        boolean match = false;
                        for (Reference ref : logical.getAllReferences("to")) {
                            if (ref.getTarget().equals(toAdd.getTarget())) {
                                match = true;
                                break;
                            }
                        }
                        if (!match) {
                            logical.getAllReferences("to").add(toAdd);
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
            while (physical.getAllChildren() != null && !physical.getAllChildren().isEmpty()) {
                physical.removeChild(physical.getAllChildren().get(0));
            }
        }

        createPagination();
        loadCurrentImages(true);
        return "";
    }

    /**
     * alle Seiten ermitteln ================================================================
     */
    public void retrieveAllImages() {
        DigitalDocument digitalDocument = null;
        try {
            digitalDocument = this.gdzfile.getDigitalDocument();
        } catch (PreferencesException e) {
            Helper.setMeldung(null, "Can not get DigitalDocument: ", e.getMessage());
        }

        List<DocStruct> meineListe = digitalDocument.getPhysicalDocStruct().getAllChildrenAsFlatList();

        if (meineListe == null) {
            pageMap = null;
            return;
        }
        int numberOfPages = 0;
        if (digitalDocument.getPhysicalDocStruct() != null && digitalDocument.getPhysicalDocStruct().getAllChildren() != null) {
            numberOfPages = digitalDocument.getPhysicalDocStruct().getAllChildren().size();
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
                if ("logicalPageNumber".equals(md.getType().getName())) {
                    logPageNo = md.getValue();
                    logPageNoMd = md;
                } else if ("physPageNumber".equals(md.getType().getName())) {
                    physPageNo = md.getValue();
                } else if ("_COORDS".equals(md.getType().getName())) {
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
                String strDoublePage = pageStruct.getAdditionalValue(); // a boolean field named doublePage is already declared at line 465
                pi.setDoublePage(StringUtils.isNotBlank(strDoublePage) && "double page".equals(strDoublePage));
                pi.setLogicalPageNo(lastLogPageNo);
                counter++;
                pageMap.put(lastPhysPageNo, pi);
            } else {
                // add placeholder for areas
                pi = this.pageAreaManager.createPhysicalObject(pageStruct);
                pageMap.put(pi.getPhysicalPageNo(), pi);
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

    private String getRequestParameter(String name) {
        Map<String, String> params = FacesContextHelper.getCurrentFacesContext().getExternalContext().getRequestParameterMap();
        return params.get(name);
    }

    /**
     * Add page area via commandScript
     */
    public void addPageAreaCommand() {
        String addTo = getRequestParameter("addTo");
        Integer x = Integer.parseInt(getRequestParameter("x"));
        Integer y = Integer.parseInt(getRequestParameter("y"));
        Integer w = Integer.parseInt(getRequestParameter("w"));
        Integer h = Integer.parseInt(getRequestParameter("h"));

        DocStruct page = getCurrentPage();
        DocStruct logicalDocStruct = "current".equalsIgnoreCase(addTo) ? this.myDocStruct : null;
        try {
            DocStruct pageArea = this.pageAreaManager.createPageArea(page, x, y, w, h);
            if (logicalDocStruct != null) {
                this.pageAreaManager.assignToPhysicalDocStruct(pageArea, page);
                this.pageAreaManager.assignToLogicalDocStruct(pageArea, logicalDocStruct);
                retrieveAllImages();
            } else {
                this.pageAreaManager.setNewPageArea(pageArea);
            }
        } catch (TypeNotAllowedAsChildException | TypeNotAllowedForParentException | MetadataTypeNotAllowedException exception) {
            log.error(exception);
        }
    }

    /**
     * Set coordinates for existing page area rectangles
     * 
     * @param json
     */
    public void setPageAreaCommand() {
        String id = getRequestParameter("areaId");
        Integer x = Integer.parseInt(getRequestParameter("x"));
        Integer y = Integer.parseInt(getRequestParameter("y"));
        Integer w = Integer.parseInt(getRequestParameter("w"));
        Integer h = Integer.parseInt(getRequestParameter("h"));
        this.pageAreaManager.setRectangle(id, x, y, w, h, getCurrentPage());

    }

    public void deletePageAreaCommand() {
        DocStruct page = getCurrentPage();
        String areaId = getRequestParameter("areaId");
        if (this.pageAreaManager.hasNewPageArea() && Objects.equals(areaId, this.pageAreaManager.getNewPageArea().getIdentifier())) {
            this.pageAreaManager.setNewPageArea(null);
        } else if (page != null && StringUtils.isNotBlank(areaId) && page.getAllChildren() != null) {
            DocStruct pageArea = page.getAllChildren().stream().filter(c -> areaId.equals(c.getIdentifier())).findAny().orElse(null);
            if (pageArea != null) {
                deletePageArea(pageArea);
            }
        }
    }

    public void deletePageArea(DocStruct pageArea) {
        DocStruct page = pageArea.getParent();
        if (page != null) {
            page.removeChild(pageArea);
        }
        List<Reference> fromReferences = pageArea.getAllFromReferences();
        List<DocStruct> linkedDocstructs = new ArrayList<>();
        for (Reference ref : fromReferences) {
            linkedDocstructs.add(ref.getSource());
        }
        for (DocStruct ds : linkedDocstructs) {
            ds.removeReferenceTo(pageArea);
            if (page != null && ds.getAllToReferences() == null || ds.getAllToReferences().isEmpty()) {
                ds.addReferenceTo(page, "logical_physical");
            }
        }
        retrieveAllImages();
    }

    public void cancelPageAreaEdition() {
        this.pageAreaManager.resetNewPageArea();
    }

    /**
     * Get all page areas of current page
     * 
     * @return
     */
    public String getPageAreas() {

        return this.pageAreaManager.getRectangles(getCurrentPage(), myDocStruct);

    }

    public String getPageArea() {
        return this.pageAreaManager.getNewPageAreaLabel();
    }

    public void setPageArea(String label) {
        log.warn("Attempting to set page area to ", label);
    }

    private DocStruct getCurrentPage() {
        List<DocStruct> pages = document.getPhysicalDocStruct().getAllChildren();
        if (pages == null || pages.isEmpty() || pages.size() <= imageIndex) {
            return null;
        }
        return pages.get(imageIndex); // page
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
            Collections.sort(listReferenzen, (o1, o2) -> {
                final Reference r1 = o1;
                final Reference r2 = o2;
                Integer page1 = 0;
                Integer page2 = 0;

                final MetadataType mdt = Metadaten.this.myPrefs.getMetadataTypeByName("physPageNumber");
                List<? extends Metadata> listMetadaten = r1.getTarget().getAllMetadataByType(mdt);
                if (listMetadaten != null && !listMetadaten.isEmpty()) {
                    final Metadata meineSeite = listMetadaten.get(0);
                    page1 = Integer.parseInt(meineSeite.getValue());
                }
                listMetadaten = r2.getTarget().getAllMetadataByType(mdt);
                if (listMetadaten != null && !listMetadaten.isEmpty()) {
                    final Metadata meineSeite = listMetadaten.get(0);
                    page2 = Integer.parseInt(meineSeite.getValue());
                }
                if (page1.equals(page2) && "div".equals(r1.getTarget().getDocstructType())) {
                    page1 = 0;
                }

                return page1.compareTo(page2);
            });

            /* die Größe der Arrays festlegen */
            this.structSeiten = new SelectItem[listReferenzen.size()];
            this.structSeitenNeu = new MetadatumImpl[listReferenzen.size()];
            /* alle Referenzen durchlaufen und deren Metadaten ermitteln */
            for (Reference ref : listReferenzen) {
                DocStruct target = ref.getTarget();
                StructSeitenErmitteln2(target, zaehler);
                if (imageNr == 0) {
                    imageNr = StructSeitenErmitteln3(target);
                }
                zaehler++;
            }

        }

        /*
         * Wenn eine Verknuepfung zwischen Strukturelement und Bildern sein soll, das richtige Bild anzeigen
         */
        if (this.bildZuStrukturelement && !this.noUpdateImageIndex) {
            if (currentTopstruct != null && "BoundBook".equals(currentTopstruct.getType().getName())) {
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
    private void StructSeitenErmitteln2(DocStruct inStrukturelement, int inZaehler) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName("logicalPageNumber");
        List<? extends Metadata> listMetadaten = inStrukturelement.getAllMetadataByType(mdt);
        if (listMetadaten == null || listMetadaten.isEmpty()) {
            return;
        }
        String pageIdentifier = null;
        if ("div".equals(inStrukturelement.getDocstructType())) {
            pageIdentifier = MetadatenErmitteln(inStrukturelement, "physPageNumber");
        } else {
            pageIdentifier = this.pageAreaManager.createPhysicalPageNumberForArea(inStrukturelement, inStrukturelement.getParent());
        }
        for (Metadata meineSeite : listMetadaten) {
            this.structSeitenNeu[inZaehler] = new MetadatumImpl(meineSeite, inZaehler, this.myPrefs, this.myProzess, this);
            DocStruct ds = (DocStruct) meineSeite.getParent();
            if ("div".equals(inStrukturelement.getDocstructType())) {
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
        if (listMetadaten == null || listMetadaten.isEmpty()) {
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
                if ((scope == Paginator.Scope.SELECTED && po.isSelected()) || (scope == Paginator.Scope.FROMFIRST && firstMatch)) {
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        doublePage = false;
        return null;
    }

    /**
     * alle Knoten des Baums expanden oder collapsen ================================================================
     */
    public String TreeExpand() {
        this.tree3.expandNodes(this.treeProperties.get("fullexpanded"));
        return REDIRECT_TO_METSEDITOR;
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

    public void readAllTifFolders() throws IOException, SwapException {
        this.allTifFolders = new ArrayList<>();
        Path dir = Paths.get(this.myProzess.getImagesDirectory());

        List<String> verzeichnisse = StorageProvider.getInstance().listDirNames(dir.toString());
        for (int i = 0; i < verzeichnisse.size(); i++) {
            this.allTifFolders.add(verzeichnisse.get(i));
        }

        Path thumbsDir = Paths.get(myProzess.getThumbsDirectory());
        useThumbsDir = false;
        if (ConfigurationHelper.getInstance().useS3() || StorageProvider.getInstance().isDirectory(thumbsDir)) {
            List<String> thumbDirs = StorageProvider.getInstance().listDirNames(thumbsDir.toString());
            for (String thumbDirName : thumbDirs) {
                useThumbsDir = true;
                String matchingImageDir = myProzess.getMatchingImageDir(thumbDirName);
                if (!allTifFolders.contains(matchingImageDir)) {
                    allTifFolders.add(matchingImageDir);
                }
            }
        }

        if (!"".equals(ConfigurationHelper.getInstance().getProcessImagesFallbackDirectoryName())) {
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

        if (dataList == null || dataList.isEmpty() || tiffFolderHasChanged) {
            tiffFolderHasChanged = false;

            if (this.currentTifFolder != null) {
                try {
                    dataList = this.imagehelper.getImageFiles(this.myProzess, this.currentTifFolder, useThumbsDir);
                    if (dataList == null) {
                        myBild = null;
                        bildNummer = -1;
                        return;
                    }
                } catch (InvalidImagesException e1) {
                    log.error("Images could not be read", e1);
                    Helper.setFehlerMeldung("images could not be read", e1);
                }
            } else {
                try {
                    createPagination();
                    dataList = this.imagehelper.getImageFiles(document.getPhysicalDocStruct());
                } catch (TypeNotAllowedForParentException | SwapException | DAOException | IOException e) {
                    log.error(e);
                }
            }
        }

        if (dataList != null && !dataList.isEmpty()) {
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
                        log.error(e);
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
                //TODO
                exists = StorageProvider.getInstance()
                        .isFileExists(Paths.get(
                                this.myProzess.getImagesDirectory() + this.currentTifFolder + FileSystems.getDefault().getSeparator() + this.myBild));
            }
        } catch (Exception e) {
            this.bildNummer = -1;
            log.error(e);
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
        int processId = this.myProzess.getId().intValue();
        if (MetadatenSperrung.isLocked(processId) && !this.sperrung.getLockBenutzer(processId).equals(this.myBenutzerID)) {
            // locked by someone else
            return false;
        }
        this.sperrung.setLocked(processId, this.myBenutzerID);
        return true;
    }

    private void SperrungAufheben() {
        int processId = this.myProzess.getId().intValue();
        if (MetadatenSperrung.isLocked(processId) && this.sperrung.getLockBenutzer(processId).equals(this.myBenutzerID)) {
            this.sperrung.setFree(processId);
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
                log.error("Error while importing from catalogue: " + e.getMessage());
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
                            if (StringUtils.isBlank(md.getValue())) {
                                this.myDocStruct.removeMetadata(md, true);
                            }
                        }
                    }
                    List<Person> personList = myDocStruct.getAllPersons();
                    if (personList != null) {
                        List<Person> personListClone = new ArrayList<>(personList);
                        for (Person p : personListClone) {
                            if (StringUtils.isBlank(p.getFirstname()) && StringUtils.isBlank(p.getLastname())) {
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
                    if (addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadataGroups() != null) {
                        for (MetadataGroup m : addrdf.getDigitalDocument().getLogicalDocStruct().getAllMetadataGroups()) {
                            if (myDocStruct.getAddableMetadataGroupTypes().contains(m.getType())) {
                                myDocStruct.addMetadataGroup(m);
                            }
                        }
                    }

                    MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
                } else {
                    Helper.setMeldung(null, "Opac abgefragt: ", "kein Ergebnis");
                }
            } catch (Exception e) {
                log.error("Error while importing from catalogue: " + e.getMessage());
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

        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
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
    }

    public void CurrentEndpage() {
        for (String pageObject : pageMap.getKeyList()) {
            PhysicalObject po = pageMap.get(pageObject);
            if (po.getPhysicalPageNo().equals(String.valueOf(this.pageNumber + 1))) {
                this.pagesEnd = po.getLabel();
            }
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Ajax-Liste abgefragt");
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
                this.pageSelectionFirstPage = po.getPhysicalPageNo();
            }
            if (po.getLabel().equals(this.ajaxSeiteEnde)) {
                endseiteOk = true;
                this.pageSelectionLastPage = po.getPhysicalPageNo();
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        int startPage = pageMap.getIndexPosition(pageSelectionFirstPage);
        int endPage = pageMap.getIndexPosition(pageSelectionLastPage);
        int anzahlAuswahl = endPage - startPage + 1;
        if (anzahlAuswahl > 0) {
            /* alle bisher zugewiesenen Seiten entfernen */
            this.myDocStruct.getAllToReferences().clear();
            List<String> pageIdentifierList = pageMap.getKeyList().subList(startPage, endPage + 1);

            for (String pageIdentifier : pageIdentifierList) {
                DocStruct page = pageMap.get(pageIdentifier).getDocStruct();
                //only add physical docStructs of type "page"/"div" to logical docstruct. Ignore page areas
                if ("div".equalsIgnoreCase(page.getDocstructType())) {
                    myDocStruct.addReferenceTo(page, "logical_physical");
                }
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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
        if (Boolean.TRUE.equals(this.treeProperties.get("showpagesasajax"))) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                if (po.getLabel().equals(this.ajaxSeiteStart)) {
                    this.pageSelectionFirstPage = po.getPhysicalPageNo();
                    break;
                }
            }
        }
        try {
            int localPageNumber = Integer.parseInt(this.pageSelectionFirstPage) - this.bildNummer + 1; // a field named "pageNumber" is already declared at line 403
            setImageIndex(localPageNumber - 1);

        } catch (Exception e) {
            log.error(e);
        }
        return "";
    }

    /**
     * die erste und die letzte Seite festlegen und alle dazwischen zuweisen ================================================================
     */
    public String BildLetzteSeiteAnzeigen() {
        this.bildAnzeigen = true;
        if (Boolean.TRUE.equals(this.treeProperties.get("showpagesasajax"))) {
            for (String pageObject : pageMap.getKeyList()) {
                PhysicalObject po = pageMap.get(pageObject);
                if (po.getLabel().equals(this.ajaxSeiteEnde)) {
                    this.pageSelectionLastPage = po.getPhysicalPageNo();
                    break;
                }
            }
        }
        try {
            int localPageNumber = Integer.parseInt(this.pageSelectionLastPage) - this.bildNummer + 1; // a field named "pageNumber" is already declared at line 403
            setImageIndex(localPageNumber - 1);
        } catch (Exception exception) {
            log.warn(exception);
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
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
        }
        return null;
    }

    /**
     * ausgewählte Seiten aus dem Strukturelement entfernen ================================================================
     */
    public String SeitenWeg() {
        for (String key : this.structSeitenAuswahl) {
            DocStruct ds = Optional.ofNullable(pageMap.get(key)).map(PhysicalObject::getDocStruct).orElse(null);
            if (ds != null) {
                this.myDocStruct.removeReferenceTo(ds);
            }
        }
        StructSeitenErmitteln(this.myDocStruct);
        MetadatenalsTree3Einlesen1(this.tree3, this.currentTopstruct, false);
        this.structSeitenAuswahl = null;
        if (!SperrungAktualisieren()) {
            return REDIRECT_TO_METSEDITOR_AFTER_TIMEOUT;
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

    public int getMaxParallelThumbnailRequests() {
        return ConfigurationHelper.getInstance().getMaxParallelThumbnailRequests();
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

                stream = client.execute(method, HttpUtils.streamResponseHandler);
                if (stream != null) {
                    ocrResult = IOUtils.toString(stream, StandardCharsets.UTF_8);
                }

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

    public void loadJsonAlto() throws IOException, JDOMException, SwapException, DAOException {
        Path altoFile = getCurrentAltoPath();
        SimpleAlto alto = new SimpleAlto();
        if (StorageProvider.getInstance().isFileExists(altoFile)) {
            alto = SimpleAlto.readAlto(altoFile);
        }

        this.currentJsonAlto = new Gson().toJson(alto);
    }

    public String getJsonAlto() {
        return currentJsonAlto;
    }

    private Path getCurrentAltoPath() throws SwapException, IOException {
        String ocrFileNew = image.getTooltip().substring(0, image.getTooltip().lastIndexOf("."));
        Path altoFile = Paths.get(myProzess.getOcrAltoDirectory(), ocrFileNew + ".xml");
        if (!StorageProvider.getInstance().isFileExists(altoFile)) {
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
        } catch (JDOMException | IOException | SwapException | DAOException e) {
            log.error(e);
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
        // setter is needed for jsf
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
            log.error(e.getMessage());
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
                log.error(e.getMessage());
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
        return ""; //NOSONAR
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
        if (StringUtils.isBlank(neuesElementWohin)) {
            this.neuesElementWohin = "4";
        }
        return this.neuesElementWohin;
    }

    public void setNeuesElementWohin(String inNeuesElementWohin) {
        if (inNeuesElementWohin == null || "".equals(inNeuesElementWohin)) {
            this.neuesElementWohin = "1";
        } else if (!inNeuesElementWohin.equals(neuesElementWohin)) {
            if (("1".equals(neuesElementWohin) || "2".equals(neuesElementWohin))
                    && ("3".equals(inNeuesElementWohin) || "4".equals(inNeuesElementWohin))) {
                this.neuesElementWohin = inNeuesElementWohin;
                getAddDocStructType2();
                createAddableData();
            } else if (("3".equals(neuesElementWohin) || "4".equals(neuesElementWohin))
                    && ("1".equals(inNeuesElementWohin) || "2".equals(inNeuesElementWohin))) {
                this.neuesElementWohin = inNeuesElementWohin;
                getAddDocStructType1();
                createAddableData();

            } else {
                this.neuesElementWohin = inNeuesElementWohin;
            }
        }
    }

    public List<HashMap<String, Object>> getStrukturBaum3() {
        if (this.tree3 != null) {
            return this.tree3.getChildrenAsList();
        } else {
            return new ArrayList<>();
        }
    }

    public List<HashMap<String, Object>> getStrukturBaum3Alle() {
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

            inTreeStruct.setSelected(inTreeStruct.getStruct() == docStruct);

            if (!temp.getType().getAllAllowedDocStructTypes().contains(docStruct.getType().getName())) {
                inTreeStruct.setEinfuegenErlaubt(false);
            }
            for (TreeNode element : inTreeStruct.getChildren()) {
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
                    || StringUtils.isNotBlank(kulturnavSearchValue)) {
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
        if (StringUtils.isBlank(opacKatalog) && !getAllOpacCatalogues().isEmpty() && !catalogues.isEmpty()) {
            opacKatalog = getAllOpacCatalogues().get(0);
            currentCatalogue = catalogues.get(0);
        }
        return this.opacKatalog;
    }

    public void setOpacKatalog(String opacKatalog) {
        if (this.opacKatalog == null || !this.opacKatalog.equals(opacKatalog)) {
            this.opacKatalog = opacKatalog;
            currentCatalogue = null;
            for (ConfigOpacCatalogue catalogue : catalogues) {
                if (opacKatalog.equals(catalogue.getTitle())) {
                    currentCatalogue = catalogue;
                    break;
                }
            }

            if (!catalogues.isEmpty() && currentCatalogue == null) {
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
            String processTemplateName = "";
            List<Processproperty> properties = myProzess.getEigenschaften();
            if (properties != null) {
                for (Processproperty pp : properties) {
                    if ("Template".equals(pp.getTitel())) {
                        processTemplateName = pp.getWert();
                    }
                }
            }
            catalogues = ConfigOpac.getInstance().getAllCatalogues(processTemplateName);

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
            if ("page".equals(po.getDocStruct().getType().getName())) {
                alle.add(po.getLabel());
            }
        }

        for (String elem : alle) {
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
        return this.myDocStruct == null || this.myDocStruct.getParent() != null;
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
                po.setRepresentative(po.getPhysicalPageNo().equals(currentRepresentativePage) && "div".equals(po.getType()));
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
            DocStruct imageDocStruct = allPages.get(pageIndex);
            allPages.remove(imageDocStruct);
            allPages.add(pageIndex - positions, imageDocStruct);
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
        for (DocStruct imageDocStruct : pages) {
            Metadata physicalImageNo = imageDocStruct.getAllMetadataByType(myPrefs.getMetadataTypeByName("physPageNumber")).get(0);
            physicalImageNo.setValue("" + physicalCounter++);
        }
    }

    public void moveSelectedPages(String direction, int times) {
        if (times < 1) {
            times = 1;
        }
        if ("up".equals(direction)) {
            moveSeltectedPagesUp(times);
        } else {
            moveSeltectedPagesDown(times);
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
            DocStruct imageDocStruct = allPages.get(pageIndex);
            allPages.remove(imageDocStruct);
            allPages.add(pageIndex + positions, imageDocStruct);
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

            document.getFileSet().removeFile(pageToRemove.getAllContentFiles().get(0));

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
                if (pageNoMetadata == null || pageNoMetadata.isEmpty()) {
                    break;
                }
                for (Metadata pageNoMD : pageNoMetadata) {
                    pageNoMD.setValue(String.valueOf(currentPhysicalOrder));
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
            } catch (SwapException | IOException e) {
                log.error(e);
            }
            if ("".equals(imageDirectory)) {
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
            for (Map.Entry<Path, List<Path>> entry : allFolderAndAllFiles.entrySet()) {
                List<Path> files = allFolderAndAllFiles.get(entry.getKey());
                if (oldfilenames.size() != files.size()) {
                    files.clear();
                }
            }

            progress = 0;
            totalImageNo = oldfilenames.size() * 2d;
            currentImageNo = 0;

            boolean isWriteable = true;
            for (Map.Entry<Path, List<Path>> entry : allFolderAndAllFiles.entrySet()) {
                // check if folder is writeable
                if (!StorageProvider.getInstance().isWritable(entry.getKey())) {
                    isWriteable = false;
                    Helper.setFehlerMeldung(Helper.getTranslation("folderNoWriteAccess", entry.getKey().getFileName().toString()));
                }
                List<Path> files = entry.getValue();
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

                currentImageNo++;

                // check all folder
                for (Map.Entry<Path, List<Path>> entry : allFolderAndAllFiles.entrySet()) {
                    // check files in current folder
                    List<Path> files = entry.getValue();
                    for (Path file : files) {
                        String filenameToCheck = file.getFileName().toString();
                        String filenamePrefixToCheck = filenameToCheck.substring(0, filenameToCheck.lastIndexOf("."));
                        String fileExtension = Metadaten.getFileExtension(filenameToCheck);
                        // find the current file the folder
                        if (filenamePrefixToCheck.equals(filenamePrefix)) {
                            // found file to rename
                            Path tmpFileName = Paths.get(entry.getKey().toString(), filenamePrefix + fileExtension + "_bak");
                            try {
                                StorageProvider.getInstance().move(file, tmpFileName);
                            } catch (IOException e) {
                                log.error(e);
                            }
                        }
                    }
                }
            }

            System.gc(); //NOSONAR, its needed to unlock the files on windows environments
            int counter = 1;
            for (String imagename : oldfilenames) {
                currentImageNo++;
                String oldFilenamePrefix = imagename.substring(0, imagename.lastIndexOf("."));
                String newFilenamePrefix = generateFileName(counter);
                String originalExtension = Metadaten.getFileExtension(imagename.replace("_bak", ""));
                // update filename in mets file
                document.getPhysicalDocStruct().getAllChildren().get(counter - 1).setImageName(newFilenamePrefix + originalExtension);

                // check all folder
                for (Map.Entry<Path, List<Path>> entry : allFolderAndAllFiles.entrySet()) {
                    // check files in current folder
                    List<Path> files = StorageProvider.getInstance().listFiles(entry.getKey().toString(), NIOFileUtils.fileFilter);
                    for (Path file : files) {
                        String filenameToCheck = file.getFileName().toString();
                        if (filenameToCheck.contains(".")) {
                            String filenamePrefixToCheck = filenameToCheck.substring(0, filenameToCheck.lastIndexOf("."));
                            String fileExtension = Metadaten.getFileExtension(filenameToCheck.replace("_bak", ""));
                            // found right file
                            if (filenameToCheck.endsWith("bak") && filenamePrefixToCheck.equals(oldFilenamePrefix)) {
                                // generate new file name
                                Path renamedFile = Paths.get(entry.getKey().toString(), newFilenamePrefix + fileExtension.toLowerCase());
                                try {
                                    StorageProvider.getInstance().move(file, renamedFile);
                                } catch (IOException e) {
                                    log.error(e);
                                }
                            }
                        } else {
                            log.debug("the file to be renamed does not contain a '.': " + entry.getKey().toString() + filenameToCheck);
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
        } catch (Exception e) {
            log.error(e);
            Helper.setFehlerMeldung("ErrorMetsEditorImageRenaming");
        }
    }

    private void removeImage(String fileToDelete, int totalNumberOfFiles) {

        // check what happens with .tar.gz
        String fileToDeletePrefix = fileToDelete.substring(0, fileToDelete.lastIndexOf("."));

        Map<Path, List<Path>> allFolderAndAllFiles = myProzess.getAllFolderAndFiles();
        // check size of folders, remove them if they don't match the expected number of files

        // check all folder
        for (Map.Entry<Path, List<Path>> entry : allFolderAndAllFiles.entrySet()) {
            // check files in current folder
            List<Path> files = entry.getValue();
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
                            log.error(e);
                            Helper.setFehlerMeldung(Helper.getTranslation("fileNoWriteAccess", file.toString()));
                        }
                    }
                }
            } else {
                Helper.setFehlerMeldung("File " + fileToDelete + " cannot be deleted from folder " + entry.getKey().toString()
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
        } else if (!filteredProcess.getRegelsatz().getId().equals(myProzess.getRegelsatz().getId())) {
            Helper.setFehlerMeldung("unterschiedlicher Regelsatz");
        } else {
            Helper.setMeldung("Vorgang gefunden und gleicher Regelsatz: " + filteredProcess.getId());
            loadTreeFromFilteredProcess();

        }
    }

    private void loadTreeFromFilteredProcess() {
        if (this.modusCopyDocstructFromOtherProcess) {
            try {
                treeOfFilteredProcess =
                        buildTree(treeOfFilteredProcess, filteredProcess.readMetadataFile().getDigitalDocument().getLogicalDocStruct(), false);
            } catch (PreferencesException e) {
                log.error("Error loading the tree for filtered processes (PreferencesException): ", e);

            } catch (ReadException e) {
                log.error("Error loading the tree for filtered processes (ReadException): ", e);

            } catch (SwapException e) {
                log.error("Error loading the tree for filtered processes (SwapException): ", e);
            } catch (IOException e) {
                log.error("Error loading the tree for filtered processes (IOException): ", e);
            }
            if (treeOfFilteredProcess != null) {
                activateAllTreeElements(treeOfFilteredProcess);
            }
        }
    }

    private void activateAllTreeElements(TreeNodeStruct3 inTreeStruct) {
        inTreeStruct.setEinfuegenErlaubt(true);
        for (TreeNode element : inTreeStruct.getChildren()) {
            TreeNodeStruct3 kind = (TreeNodeStruct3) element;
            activateAllTreeElements(kind);
        }
    }

    public List<HashMap<String, Object>> getStruktureTreeAsTableForFilteredProcess() {
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
        return progress != null && progress != 100 && progress != 0;
    }

    private void createAddableData() {
        String docstructName = "";
        int selection = Integer.parseInt(neuesElementWohin);
        if (selection < 3) {
            docstructName = getAddDocStructType1();
        } else {
            docstructName = getAddDocStructType2();
        }
        if (StringUtils.isNotBlank(docstructName) && (oldDocstructName.isEmpty() || !oldDocstructName.equals(docstructName))) {
            oldDocstructName = docstructName;

            DocStructType dst = this.myPrefs.getDocStrctTypeByName(docstructName);

            addableMetadata = new LinkedList<>();
            try {
                DocStruct ds = this.document.createDocStruct(dst);

                List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(ds, Helper.getMetadataLanguage(),
                        MetadataTypes.METADATA, this.myProzess, displayHiddenMetadata);
                if (myTempMetadata != null) {
                    for (Metadata metadata : myTempMetadata) {
                        addableMetadata.add(new MetadatumImpl(metadata, 0, this.myPrefs, this.myProzess, this));
                    }
                }
            } catch (TypeNotAllowedForParentException e) {
                log.error(e);
            }

            addablePersondata = new LinkedList<>();
            try {
                DocStruct ds = this.document.createDocStruct(dst);

                List<? extends Metadata> myTempMetadata = this.metahelper.getMetadataInclDefaultDisplay(ds, Helper.getMetadataLanguage(),
                        MetadataTypes.PERSON, this.myProzess, displayHiddenMetadata);
                if (myTempMetadata != null) {
                    for (Metadata metadata : myTempMetadata) {
                        addablePersondata.add(new MetaPerson((Person) metadata, 0, this.myPrefs, ds, myProzess, this));
                    }
                }
            } catch (TypeNotAllowedForParentException e) {
                log.error(e);
            }

            addableCorporates = new LinkedList<>();
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
                log.error(e);
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
        for (Image currentImage : allImages) {
            if (sizeChanged) {
                currentImage.createThumbnailUrls(thumbnailSizeInPixel);
            }
        }

        List<Image> subList;
        if (allImages.size() > (pageNo * numberOfImagesPerPage) + numberOfImagesPerPage) {
            subList = allImages.subList(pageNo * numberOfImagesPerPage, (pageNo * numberOfImagesPerPage) + numberOfImagesPerPage);
        } else {
            // Sometimes pageNo is not zero here, although we only have 20 images or so.
            // This is a quick fix and we should find out why pageNo is not zero in some cases
            int startIdx = pageNo * numberOfImagesPerPage;
            if (startIdx > allImages.size()) {
                startIdx = Math.max(0, allImages.size() - numberOfImagesPerPage);
            }
            subList = allImages.subList(startIdx, allImages.size());
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
            } catch (IOException | JDOMException | SwapException | DAOException e) {
                SimpleAlto alto = new SimpleAlto("Error reading ALTO, see application log for details.");

                this.currentJsonAlto = new Gson().toJson(alto);
                log.error(e);
            }
        }
        cancelPageAreaEdition();
    }

    public void checkSelectedThumbnail(int imageIndex) {
        // The selection should only happen in "Paginierung"-mode
        if (!"Paginierung".equals(this.modusAnsicht)) {
            return;
        }
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
            return currentImageURL.replace('\\', '/'); //NOSONAR
        }
    }

    public int getImageWidth() {
        if (image == null) {
            log.error("Must set image before querying image size");
            return 0;
        } else {
            return image.getSize().width;
        }
    }

    public int getImageHeight() {
        if (image == null) {
            log.error("Must set image before querying image size");
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
        } else if (this.pageNo != 0) {
            this.pageNo = 0;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMovePrevious() {
        if (this.pagesRTL) {
            if (!isLastPage()) {
                this.pageNo++;
                getPaginatorList();
            }
        } else if (!isFirstPage()) {
            this.pageNo--;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMoveNext() {
        if (this.pagesRTL) {
            if (!isFirstPage()) {
                this.pageNo--;
                getPaginatorList();
            }
        } else if (!isLastPage()) {
            this.pageNo++;
            getPaginatorList();
        }
        return "";
    }

    public String cmdMoveLast() {
        if (this.pagesRTL) {
            if (this.pageNo != 0) {
                this.pageNo = 0;
                getPaginatorList();
            }
        } else if (this.pageNo != getLastPageNumber()) {
            this.pageNo = getLastPageNumber();
            getPaginatorList();
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
    }

    public int getLastPageNumber() {
        int ret = this.allImages.size() / numberOfImagesPerPage;
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
        return Long.valueOf(this.pageNo + 1l);
    }

    public Long getPageNumberLast() {
        return Long.valueOf(getLastPageNumber() + 1l);
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

        showImageComments = false;
        boolean commentsEnabledInConfig = ConfigurationHelper.getInstance().getMetsEditorShowImageComments();
        if (commentsEnabledInConfig && myProzess != null && image != null) {
            showImageComments = true;
        }
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
            dataList = imagehelper.getImageFiles(myProzess, currentTifFolder, useThumbsDir);
            if (dataList != null && !dataList.isEmpty()) {
                imageFolderName = myProzess.getImagesDirectory() + currentTifFolder + File.separator;
                int order = 1;
                for (String imagename : dataList) {
                    Image currentImage = new Image(myProzess, imageFolderName, imagename, order++, thumbnailSizeInPixel);
                    allImages.add(currentImage);
                }
                setImageIndex(imageIndex);
            } else {
                bildNummer = -1;
            }
        } catch (InvalidImagesException | SwapException | DAOException | IOException e1) {
            log.error(e1);
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

    // =========================== Use ImageCommentPropertyHelper To Save Comments =========================== //

    private ImageCommentPropertyHelper getCommentPropertyHelper() {
        if (commentPropertyHelper == null) {
            commentPropertyHelper = new ImageCommentPropertyHelper(myProzess);
        }

        return commentPropertyHelper;
    }

    public String getCommentPropertyForImage() {
        if (myProzess == null || getImage() == null) {
            return null;
        }

        return getCommentPropertyHelper().getComment(currentTifFolder, getImage().getImageName());
    }

    public void setCommentPropertyForImage(String comment) {
        if (myProzess == null || getImage() == null) {
            return;
        }

        // only save new log entry if the comment has changed
        String oldComment = getCommentPropertyForImage();
        if (comment == null || (oldComment != null && comment.contentEquals(oldComment)) || (oldComment == null && comment.isBlank())) {
            return;
        }

        getCommentPropertyHelper().setComment(currentTifFolder, getImage().getImageName(), comment);
    }

    // =========================== Use ImageCommentPropertyHelper To Save Comments =========================== //

    public void refresh() {
        // do nothing, this is needed for jsf calls
    }

    public void downloadCurrentFolderAsPdf() throws IOException {
        if (allImages.isEmpty()) {
            return;
        }

        Path imagesPath = Paths.get(imageFolderName);
        // put all selected images into a URL
        String imagesParameter = allImages.stream().map(Image::getImageName).collect(Collectors.joining("$"));

        URI goobiContentServerUrl = UriBuilder.fromUri(new HelperForm().getServletPathWithHostAsUrl())
                .path("api")
                .path("process")
                .path("image")
                .path(Integer.toString(myProzess.getId()))
                .path("media") //dummy, replaced by images parameter
                .path("00000001.tif") //dummy, replaced by images parameter
                .path(myProzess.getTitel() + ".pdf")
                .queryParam("imageSource", imagesPath.toUri())
                .queryParam("images", imagesParameter)
                .build();

        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        // generate the pdf and deliver it as download
        if (!context.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
            String fileName = myProzess.getTitel() + ".pdf";
            ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
            String contentType = servletContext.getMimeType(fileName);
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
            response.sendRedirect(goobiContentServerUrl.toString());
            context.responseComplete();
        }
    }

    private void readMetadataEditorExtensions() {
        extensions = new ArrayList<>();
        List<IPlugin> plugins = PluginLoader.getPluginList(PluginType.MetadataEditor);
        for (IPlugin p : plugins) {
            IMetadataEditorExtension ext = (IMetadataEditorExtension) p;
            try {
                ext.initializePlugin(this);
                extensions.add(ext);
            } catch (Exception e) {
                // The error  is handled by the plugin, don't log it again.
                // Do not add this plugin to the list of available plugins
            }
        }
        if (!extensions.isEmpty()) {
            extensions.sort((IMetadataEditorExtension o1, IMetadataEditorExtension o2) -> o1.getTitle().compareTo(o2.getTitle()));

            extension = extensions.get(0);
        }
    }
}
