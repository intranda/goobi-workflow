package de.sub.goobi.metadaten;

import java.net.URL;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.model.SelectItem;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.goobi.api.display.DisplayCase;
import org.goobi.api.display.Item;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.MetadataGeneration;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.api.rest.model.RestMetadata;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.dante.DanteImport;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.metadaten.search.EasyDBSearch;
import de.sub.goobi.metadaten.search.KulturNavImporter;
import de.sub.goobi.metadaten.search.ViafSearch;
import de.sub.goobi.persistence.managers.MetadataManager;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.fileformats.mets.ModsHelper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi-workflow
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

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 *
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */

@Data
@Log4j2
public class MetadatumImpl implements Metadatum, SearchableMetadata {

    private static final String DANTE = "dante";

    private static final String PROCESS_PLUGIN = "ProcessPlugin";

    private Metadata md;
    private int identifier;
    private Prefs myPrefs;
    private Process myProcess;
    private DisplayCase myValues;
    private List<SelectItem> items;
    private List<String> selectedItems;
    private Metadaten bean;
    private DisplayType metadataDisplaytype;

    // include search values

    private String searchValue;
    private String searchOption;

    // name of database
    private String vocabulary; // source
    private String field; // should be unsused

    /**
     * The table of available normdata objects
     */
    private List<List<NormData>> dataList;

    /**
     * The list of current normdata objects
     */
    private List<NormData> currentData;

    private List<NormDataRecord> normdataList;
    private NormDataRecord selectedRecord;

    private boolean showNotHits = false;

    private List<String> labelList = new ArrayList<>();
    protected List<SelectItem> possibleItems;
    protected List<String> defaultItems;
    protected String defaultValue;

    private String pagePath;
    private String title;

    protected List<RestProcess> results;

    // geonames
    private List<Toponym> resultList;
    private int totalResults;

    private Toponym currentToponym;

    // process search
    private SearchRequest searchRequest = new SearchRequest();
    private List<String> possibleFields;

    // viaf data
    private ViafSearch viafSearch = new ViafSearch();
    private EasyDBSearch easydbSearch = new EasyDBSearch();

    // search in vocabulary
    private VocabularyAPIManager vocabularyAPI = VocabularyAPIManager.getInstance();
    private List<SelectItem> vocabularySearchFields;
    private long currentVocabularySearchField;
    private String vocabularySearchQuery;
    private String vocabularyName;
    private List<ExtendedVocabularyRecord> records;
    private List<FieldDefinition> definitions;
    private ExtendedVocabularyRecord selectedVocabularyRecord;

    private boolean validationErrorPresent;
    private String validationMessage;

    private List<MetadataGeneration> generationRules = new ArrayList<>();

    /**
     * Allgemeiner Konstruktor ()
     */
    public MetadatumImpl(Metadata m, int inID, Prefs inPrefs, Process inProcess, Metadaten bean) {
        this.md = m;
        this.identifier = inID;
        this.myPrefs = inPrefs;
        this.myProcess = inProcess;
        this.bean = bean;
        myValues = new DisplayCase(this.myProcess, this.md.getType());

        metadataDisplaytype = myValues.getDisplayType();
        initializeValues();

    }

    public void searchVocabulary() {
        Vocabulary vocab = vocabularyAPI.vocabularies().findByName(this.vocabulary);
        VocabularySchema schema = vocabularyAPI.vocabularySchemas().get(vocab.getSchemaId());
        definitions = schema.getDefinitions()
                .stream()
                .filter(d -> Boolean.TRUE.equals(d.getTitleField()))
                .sorted(Comparator.comparingLong(FieldDefinition::getId))
                .collect(Collectors.toList());
        records = vocabularyAPI.vocabularyRecords()
                .list(vocab.getId())
                .search(currentVocabularySearchField + ":" + vocabularySearchQuery)
                .request()
                .getContent();
        showNotHits = records.isEmpty();
    }

    private void initializeValues() {
        if (myValues.getDisplayType() == DisplayType.select || myValues.getDisplayType() == DisplayType.select1) {
            List<String> selectedItemList = new ArrayList<>();
            List<SelectItem> itemList = new ArrayList<>();
            for (Item i : this.myValues.getItemList()) {
                itemList.add(new SelectItem(i.getLabel(), i.getValue()));
                if (i.isSelected()) {
                    selectedItemList.add(i.getValue());
                }
            }
            setPossibleItems(itemList);
            setDefaultItems(selectedItemList);
            if (selectedItemList.size() == 1) {
                setDefaultValue(selectedItemList.get(0));
            }
            setSource(myValues.getItemList().get(0).getSource());
            setField(myValues.getItemList().get(0).getField());

        } else if (myValues.getItemList().size() == 1) {
            Item item = myValues.getItemList().get(0);
            if (item.isSelected()) {
                setDefaultValue(item.getValue());
            }
            setSource(item.getSource());
            setField(item.getField());
        }

        // initialize process search
        if (metadataDisplaytype == DisplayType.easydb) {
            easydbSearch.prepare();
        } else if (metadataDisplaytype == DisplayType.process) {
            searchRequest.newGroup();
        } else if (metadataDisplaytype == DisplayType.vocabularySearch) {
            vocabularyName = myValues.getItemList().get(0).getSource();
            Set<String> fieldsNames = Stream.of(myValues.getItemList().get(0).getField().split(";"))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            Vocabulary currentVocabulary = vocabularyAPI.vocabularies().findByName(vocabularyName);
            VocabularySchema vocabularySchema = vocabularyAPI.vocabularySchemas().get(currentVocabulary.getSchemaId());

            vocabularySearchFields = vocabularySchema.getDefinitions()
                    .stream()
                    .filter(d -> fieldsNames.contains(d.getName()))
                    .map(d -> new SelectItem(d.getId(), d.getName()))
                    .collect(Collectors.toList());
        } else if (metadataDisplaytype == DisplayType.vocabularyList) {
            try {
                String vocabularyTitle = myValues.getItemList().get(0).getSource();
                String fields = myValues.getItemList().get(0).getField();
                Vocabulary currentVocabulary = vocabularyAPI.vocabularies().findByName(vocabularyTitle);
                VocabularySchema schema = vocabularyAPI.vocabularySchemas().get(currentVocabulary.getSchemaId());

                if (Boolean.TRUE.equals(schema.getHierarchicalRecords())) {
                    Helper.setFehlerMeldung(
                            Helper.getTranslation("mets_error_configuredVocabularyListHierarchical", md.getType().getName(), vocabularyTitle));
                    return;
                }

                if (StringUtils.isBlank(fields)) {
                    searchInVocabulary(vocabularyTitle, currentVocabulary);
                } else {
                    if (fields.contains(";")) {
                        Helper.setFehlerMeldung("vocabularyList with multiple fields is not supported right now");
                        return;
                    }

                    String searchFilter = fields.trim();
                    Optional<String> sorting = Optional.empty();
                    if (searchFilter.contains("@")) {
                        String[] parts = searchFilter.split("@");
                        searchFilter = parts[0];
                        sorting = Optional.of(parts[1]);
                    }

                    String fieldName = searchFilter;
                    Optional<String> fieldValueFilter = Optional.empty();

                    if (fieldName.contains("=")) {
                        String[] parts = searchFilter.split("=");
                        fieldName = parts[0];
                        fieldValueFilter = Optional.of(parts[1]);
                    }

                    String finalFieldName = fieldName;
                    Optional<FieldDefinition> searchField = schema.getDefinitions()
                            .stream()
                            .filter(d -> d.getName().equals(finalFieldName))
                            .findFirst();

                    if (searchField.isEmpty()) {
                        Helper.setFehlerMeldung("Field " + fieldName + " not found in vocabulary " + currentVocabulary.getName());
                        return;
                    }

                    Optional<String> sortingQuery = Optional.empty();
                    if (sorting.isPresent()) {
                        sortingQuery = Optional.of(searchField.get().getId() + "," + sorting.get());
                    }
                    Optional<String> searchQuery = Optional.empty();
                    if (fieldValueFilter.isPresent()) {
                        searchQuery = Optional.of(searchField.get().getId() + ":" + fieldValueFilter.get());
                    }
                    List<ExtendedVocabularyRecord> recordList = vocabularyAPI.vocabularyRecords()
                            .list(currentVocabulary.getId())
                            .search(searchQuery)
                            .sorting(sortingQuery)
                            .request()
                            .getContent();

                    ArrayList<Item> itemList = new ArrayList<>(recordList.size() + 1);
                    List<SelectItem> selectItems = new ArrayList<>(recordList.size() + 1);

                    String defaultLabel = myValues.getItemList().get(0).getLabel();
                    if (StringUtils.isNotBlank(defaultLabel)) {
                        List<String> defaultitems = new ArrayList<>();
                        defaultitems.add(defaultLabel);
                        setDefaultItems(defaultitems);
                    }
                    itemList.add(new Item(Helper.getTranslation("bitteAuswaehlen"), "", false, "", ""));
                    selectItems.add(new SelectItem("", Helper.getTranslation("bitteAuswaehlen")));

                    for (ExtendedVocabularyRecord vr : recordList) {
                        selectItems.add(new SelectItem(vr.getMainValue(), vr.getMainValue()));
                        Item item = new Item(vr.getMainValue(), vr.getMainValue(), false, "", "");
                        if (StringUtils.isNotBlank(defaultLabel) && defaultLabel.equals(vr.getMainValue())) {
                            item.setSelected(true);
                        }
                        itemList.add(item);
                    }
                    setPossibleItems(selectItems);
                    myValues.setItemList(itemList);
                }
            } catch (APIException e) {
                Helper.setFehlerMeldung(e);
            }
        } else if (metadataDisplaytype == DisplayType.generate) {
            for (Item item : myValues.getItemList()) {
                MetadataGeneration mg = (MetadataGeneration) item.getAdditionalData();
                generationRules.add(mg);
            }

        }
    }

    private void searchInVocabulary(String vocabularyTitle, Vocabulary currentVocabulary) {
        try {
            List<ExtendedVocabularyRecord> recordList = vocabularyAPI.vocabularyRecords()
                    .list(currentVocabulary.getId())
                    .all()
                    .request()
                    .getContent();
            ArrayList<Item> itemList = new ArrayList<>(recordList.size() + 1);
            List<SelectItem> selectItems = new ArrayList<>(recordList.size() + 1);

            String defaultLabel = myValues.getItemList().get(0).getLabel();
            if (StringUtils.isNotBlank(defaultLabel)) {
                List<String> defaultitems = new ArrayList<>();
                defaultitems.add(defaultLabel);
                setDefaultItems(defaultitems);
            }
            itemList.add(new Item(Helper.getTranslation("bitteAuswaehlen"), "", false, "", ""));
            selectItems.add(new SelectItem("", Helper.getTranslation("bitteAuswaehlen")));

            for (ExtendedVocabularyRecord vr : recordList) {
                selectItems.add(new SelectItem(vr.getMainValue(), vr.getMainValue()));
                Item item = new Item(vr.getMainValue(), vr.getMainValue(), false, "", "");
                if (StringUtils.isNotBlank(defaultLabel) && defaultLabel.equals(vr.getMainValue())) {
                    item.setSelected(true);
                }
                itemList.add(item);
            }
            setPossibleItems(selectItems);
            myValues.setItemList(itemList);
        } catch (APIException e) {
            Helper.setFehlerMeldung(
                    Helper.getTranslation("mets_error_configuredVocabularyInvalid", md.getType().getName(), vocabularyTitle));
            metadataDisplaytype = DisplayType.input;
            myValues.overwriteConfiguredElement(myProcess, md.getType());
        }
    }

    @Override
    public List<Item> getWert() {
        String value = this.md.getValue();
        if (value != null) {
            for (Item i : myValues.getItemList()) {
                i.setSelected(i.getValue().equals(value));
            }
        }
        return this.myValues.getItemList();
    }

    @Override
    public void setWert(String inWert) {
        this.md.setValue(inWert.trim());
    }

    @Override
    public String getTyp() {
        String label = this.md.getType().getLanguage(Helper.getMetadataLanguage());
        if (label == null) {
            label = this.md.getType().getName();
        }
        return label;
    }

    @Override
    public void setTyp(String inTyp) {
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(inTyp);
        this.md.setType(mdt);
    }

    /*
     * ##################################################### ##################################################### ## ## Getter und Setter ##
     * ##################################################### ####################################################
     */

    /******************************************************
     *
     * new functions for use of display configuration within xml files
     *
     *****************************************************/

    @Override
    public String getOutputType() {
        String type = this.myValues.getDisplayType().name();
        if (type.toLowerCase().startsWith(DANTE)) {
            return DANTE;
        }
        return this.myValues.getDisplayType().name();
    }

    @Override
    public List<SelectItem> getItems() {
        this.items = new ArrayList<>();
        this.selectedItems = new ArrayList<>();
        for (Item i : this.myValues.getItemList()) {
            this.items.add(new SelectItem(i.getLabel()));
            if (i.isSelected()) {
                this.selectedItems.add(i.getLabel());
            }
        }
        return this.items;
    }

    @Override
    public void setItems(List<SelectItem> items) {
        for (Item i : this.myValues.getItemList()) {
            i.setSelected(false);
        }
        StringBuilder bld = new StringBuilder();
        for (SelectItem sel : items) {
            for (Item i : this.myValues.getItemList()) {
                if (i.getLabel().equals(sel.getValue())) {
                    i.setSelected(true);
                    bld.append(i.getValue());
                }
            }
        }
        setWert(bld.toString());
    }

    @Override
    public List<String> getSelectedItems() {
        this.selectedItems = new ArrayList<>();
        String values = this.md.getValue();
        if (values != null && values.length() != 0) {
            while (values != null && values.length() != 0) {
                int semicolon = values.indexOf(";");
                if (semicolon != -1) {
                    String value = values.substring(0, semicolon);
                    for (Item i : this.myValues.getItemList()) {
                        if (i.getValue().equals(value)) {
                            this.selectedItems.add(i.getLabel());
                        }
                    }
                    int length = values.length();
                    values = values.substring(semicolon + 1, length);
                } else {
                    for (Item i : this.myValues.getItemList()) {
                        if (i.getValue().equals(values)) {
                            this.selectedItems.add(i.getLabel());
                        }
                    }
                    values = "";
                }
            }
        } else { // if values == null || values.length() == 0
            StringBuilder bld = new StringBuilder();
            for (Item i : this.myValues.getItemList()) {
                if (i.isSelected()) {
                    bld.append(";");
                    bld.append(i.getValue());
                    this.selectedItems.add(i.getLabel());
                }
            }
            values = bld.toString();
            if (values != null) {
                setWert(values);
            }
        }
        return this.selectedItems;
    }

    @Override
    public void setSelectedItems(List<String> selectedItems) {

        StringBuilder bld = new StringBuilder();
        for (String sel : selectedItems) {
            for (Item i : this.myValues.getItemList()) {
                if (i.getLabel().equals(sel)) {
                    bld.append(i.getValue());
                    bld.append(";");
                }
            }
        }

        setWert(bld.toString());
    }

    @Override
    public String getSelectedItem() {
        String value = this.md.getValue();
        if (value != null && value.length() != 0) {
            for (Item i : this.myValues.getItemList()) {
                if (value.equals(i.getValue())) {
                    return i.getLabel();
                }
            }
        } else {
            for (Item i : this.myValues.getItemList()) {
                if (i.isSelected()) {
                    value = i.getValue();
                    setWert(value);
                    return i.getLabel();
                }
            }
        }
        return "";
    }

    @Override
    public void setSelectedItem(String selectedItem) {

        for (Item i : this.myValues.getItemList()) {
            if (i.getLabel().equals(selectedItem)) {
                setWert(i.getValue());
            }
        }
    }

    @Override
    public void setValue(String value) {
        setWert(value);
    }

    @Override
    public String getValue() {
        return this.md.getValue();
    }

    public List<String> getPossibleDatabases() {
        List<NormDatabase> databaseList = NormDatabase.getAllDatabases();
        List<String> abbrev = new ArrayList<>();
        for (NormDatabase norm : databaseList) {
            abbrev.add(norm.getAbbreviation());
        }
        return abbrev;
    }

    public String getNormdataValue() {
        return md.getAuthorityValue();
    }

    public void setNormdataValue(String normdata) {
        md.setAuthorityValue(normdata);
    }

    public void setNormDatabase(String abbrev) {
        NormDatabase database = NormDatabase.getByAbbreviation(abbrev);
        md.setAuthorityID(database.getAbbreviation());
        md.setAuthorityURI(database.getPath());
    }

    public String getNormDatabase() {
        if (md.getAuthorityURI() != null && md.getAuthorityID() != null) {
            NormDatabase ndb = NormDatabase.getByAbbreviation(md.getAuthorityID());
            return ndb.getAbbreviation();
        } else {
            return null;
        }
    }

    public boolean isNormdata() {
        return md.getType().isAllowNormdata();
    }

    @Override
    public String search() {
        switch (metadataDisplaytype) {
            case dante:
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append("http://api.dante.gbv.de/search?");
                urlBuilder.append("voc=");
                urlBuilder.append(getVocabulary());
                urlBuilder.append("&limit=20");
                urlBuilder.append("&cache=0");
                urlBuilder.append("&type=person,place,concept,groupconcept,corporate");
                urlBuilder.append("&query=");
                urlBuilder.append(getSearchValue());
                normdataList = DanteImport.importNormDataList(urlBuilder.toString(), getLabelList());
                showNotHits = normdataList.isEmpty();
                break;

            case kulturnav:
                String knUrl = KulturNavImporter.constructSearchUrl(getSearchValue(), getVocabulary());
                normdataList = KulturNavImporter.importNormData(knUrl);
                showNotHits = normdataList.isEmpty();
                break;

            case geonames:
                String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
                if (credentials != null) {
                    WebService.setUserName(credentials);
                    ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
                    searchCriteria.setNameEquals(searchValue);
                    searchCriteria.setStyle(Style.FULL);
                    if (StringUtils.isNotBlank(vocabulary)) {
                        Set<String> languageCodes = new HashSet<>();
                        String[] lang = vocabulary.split(";");
                        for (String l : lang) {
                            languageCodes.add(l.trim());
                        }
                        searchCriteria.setCountryCodes(languageCodes);
                    }
                    try {
                        ToponymSearchResult searchResult = WebService.search(searchCriteria);
                        resultList = searchResult.getToponyms();
                        totalResults = searchResult.getTotalResultsCount();
                    } catch (Exception e) {
                        log.error(e);
                    }

                    if (totalResults == 0) {
                        showNotHits = true;
                    } else {
                        showNotHits = false;
                    }
                } else {
                    // deaktiviert
                    Helper.setFehlerMeldung("geonamesList", "Missing data", "mets_geoname_account_inactive");
                }
                break;

            case gnd:
                String val = "";
                if (StringUtils.isBlank(searchOption) && StringUtils.isBlank(searchValue)) {
                    showNotHits = true;
                    return "";
                }
                if (StringUtils.isBlank(searchOption)) {
                    val = "dnb.nid=" + searchValue;
                } else {
                    val = searchValue + " and BBG=" + searchOption;
                }
                URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
                String string = url.toString()
                        .replace("Ä", "%C3%84")
                        .replace("Ö", "%C3%96")
                        .replace("Ü", "%C3%9C")
                        .replace("ä", "%C3%A4")
                        .replace("ö", "%C3%B6")
                        .replace("ü", "%C3%BC")
                        .replace("ß", "%C3%9F");
                if (ConfigurationHelper.getInstance().isUseProxy()) {
                    dataList = NormDataImporter.importNormDataList(string, 3, ConfigurationHelper.getInstance().getProxyUrl(),
                            ConfigurationHelper.getInstance().getProxyPort());
                } else {
                    dataList = NormDataImporter.importNormDataList(string, 3, null, 0);
                }
                if (dataList.isEmpty()) {
                    showNotHits = true;
                } else {
                    showNotHits = false;
                }
                break;
            case viaf:
                viafSearch.performSearchRequest();
                if (viafSearch.getRecords() == null) {
                    showNotHits = true;
                } else {
                    showNotHits = false;
                }

                break;
            case easydb:
                easydbSearch.search();
                break;
            case process:
                // set our wanted fields
                configureRequest(searchRequest);
                try {
                    this.results = searchRequest.search();
                } catch (SQLException e) {
                    log.error(e);
                }
                break;

            default:
                break;
        }

        return "";
    }

    public String getData() {
        switch (metadataDisplaytype) {
            case dante:
                if (StringUtils.isNotBlank(selectedRecord.getPreferredValue())) {
                    md.setValue(selectedRecord.getPreferredValue());
                }
                for (NormData normdata : selectedRecord.getNormdataList()) {
                    if ("URI".equals(normdata.getKey())) {
                        md.setAuthorityFile(DANTE, "https://uri.gbv.de/terminology/dante/", normdata.getValues().get(0).getText());
                    } else if (StringUtils.isBlank(selectedRecord.getPreferredValue()) && CollectionUtils.isEmpty(getLabelList())
                            && normdata.getKey().equals(field)) {
                        String value = normdata.getValues().get(0).getText();
                        md.setValue(filter(value));
                    }
                }

                if (StringUtils.isBlank(selectedRecord.getPreferredValue()) && CollectionUtils.isNotEmpty(getLabelList())) {
                    for (String fieldName : getLabelList()) {
                        for (NormData normdata : currentData) {
                            if (normdata.getKey().equals(fieldName)) {
                                String value = normdata.getValues().get(0).getText();
                                md.setValue(filter(value));
                                normdataList = new ArrayList<>();
                                return "";
                            }
                        }
                    }
                }
                normdataList = new ArrayList<>();
                break;
            case kulturnav:
                // Set authority ID
                if (CollectionUtils.isNotEmpty(selectedRecord.getNormdataList())) {
                    for (NormData normdata : selectedRecord.getNormdataList()) {
                        if ("URI".equals(normdata.getKey())) {
                            String uriValue = normdata.getValues().get(0).getText();
                            md.setAuthorityFile(DisplayType.kulturnav.name(), KulturNavImporter.BASE_URL, uriValue);
                            break;
                        }
                    }
                }
                // Set value based on the preferred value, otherwise
                // use the first element in the list
                if (StringUtils.isNotBlank(selectedRecord.getPreferredValue())) {
                    md.setValue(selectedRecord.getPreferredValue());
                } else if (CollectionUtils.isNotEmpty(selectedRecord.getValueList())) {
                    md.setValue(selectedRecord.getValueList().get(0));
                }
                normdataList = new ArrayList<>();
                break;
            case geonames:

                md.setValue(currentToponym.getName());
                md.setAuthorityFile("geonames", "http://www.geonames.org/", "" + currentToponym.getGeoNameId());
                resultList = new ArrayList<>();
                break;
            case gnd:
                for (NormData normdata : currentData) {
                    if ("NORM_IDENTIFIER".equals(normdata.getKey())) {
                        md.setAuthorityFile("gnd", "http://d-nb.info/gnd/", normdata.getValues().get(0).getText());
                    } else if ("NORM_NAME".equals(normdata.getKey())) {
                        String value = normdata.getValues().get(0).getText();
                        md.setValue(filter(value));
                    }
                }
                dataList = new ArrayList<>();
                break;
            case process:
                break;
            case viaf:
                viafSearch.getMetadata(md);
                break;
            case easydb:
                easydbSearch.getMetadata(md);
                break;
            case vocabularySearch:
                selectedVocabularyRecord.writeReferenceMetadata(md);
                break;
            default:
                break;
        }

        return "";
    }

    public String getUrl() {

        if (StringUtils.isBlank(md.getAuthorityValue())) {
            return null;
        } else {
            return md.getAuthorityURI() + md.getAuthorityValue();
        }
    }

    public boolean isShowNoHitFound() {
        return showNotHits;
    }

    public void setSource(String source) {
        vocabulary = source;

        viafSearch.setSource(source);
        easydbSearch.setSearchInstance(source);
    }

    public void setField(String field) {
        if (field.contains(",")) {
            String[] parts = field.split(",");
            for (String part : parts) {
                labelList.add(part.trim());
            }
        } else {
            this.field = field;
        }

        viafSearch.setField(field);
        easydbSearch.setSearchBlock(field);

    }

    public String copy() {
        bean.setCurrentMetadata(md);
        bean.Copy();
        return "";
    }

    public String delete() {
        bean.setCurrentMetadata(md);
        bean.delete();
        return "";
    }

    /**
     * This method is used to disable the edition of the identifier field, the default value is false, so it can be edited
     */

    public boolean isDisableIdentifierField() {
        return metadataDisplaytype == DisplayType.dante || metadataDisplaytype == DisplayType.kulturnav || metadataDisplaytype == DisplayType.easydb;
    }

    /**
     * this method is used to disable the edition of the metadata value field, the default value is false
     */

    public boolean isDisableMetadataField() {
        return metadataDisplaytype == DisplayType.dante || metadataDisplaytype == DisplayType.easydb;
    }

    @Override
    public void clearResults() {
        dataList = new ArrayList<>();
        normdataList = new ArrayList<>();
        viafSearch.clearResults();
        easydbSearch.clearResults();
    }

    public void linkProcess(RestProcess rp) {
        Project p = this.getBean().getMyProzess().getProjekt();
        XMLConfiguration xmlConf = ConfigPlugins.getPluginConfig(PROCESS_PLUGIN);
        if (xmlConf == null) {
            return;
        }
        HierarchicalConfiguration use = getConfigForProject(p, xmlConf);
        Map<String, List<RestMetadata>> addMetadata = new HashMap<>();
        if (use != null) {
            List<HierarchicalConfiguration> mappings = use.configurationsAt("mapping");
            for (HierarchicalConfiguration mapping : mappings) {
                String from = mapping.getString("[@from]");
                String to = mapping.getString("[@to]");
                List<RestMetadata> fromMeta = rp.getMetadata().get(from);
                if (fromMeta != null) {
                    addMetadata.put(to, fromMeta);
                }
            }
        }
        Prefs prefs = this.getBean().getMyPrefs();
        DocStruct ds = this.getBean().getMyDocStruct();
        for (Map.Entry<String, List<RestMetadata>> entry : addMetadata.entrySet()) {
            String name = entry.getKey();
            try {
                for (RestMetadata rmd : entry.getValue()) {
                    if (!rmd.anyValue()) {
                        continue;
                    }
                    if (name.contains("/")) {
                        String[] split = name.split("/");
                        String group = split[0];
                        String metaName = split[1];
                        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName(group);
                        MetadataGroup addGroup = null;
                        addGroup = new MetadataGroup(mgt);
                        List<Metadata> metaList = addGroup.getMetadataByType(metaName);
                        Metadata currentMetadata;
                        if (metaList.isEmpty()) {
                            currentMetadata = new Metadata(prefs.getMetadataTypeByName(metaName));
                            addGroup.addMetadata(currentMetadata);
                        } else {
                            currentMetadata = metaList.get(0);
                        }
                        if (rmd.getValue() != null) {
                            currentMetadata.setValue(rmd.getValue());
                        }
                        if (rmd.getAuthorityID() != null) {
                            currentMetadata.setAuthorityID(rmd.getAuthorityID());
                        }
                        if (rmd.getAuthorityURI() != null) {
                            currentMetadata.setAuthorityURI(rmd.getAuthorityURI());
                        }
                        if (rmd.getAuthorityValue() != null) {
                            currentMetadata.setAuthorityValue(rmd.getAuthorityValue());
                        }
                        ds.addMetadataGroup(addGroup);
                    } else {
                        Metadata currentMetadata = new Metadata(prefs.getMetadataTypeByName(name));
                        if (rmd.getValue() != null) {
                            currentMetadata.setValue(rmd.getValue());
                        }
                        if (rmd.getAuthorityID() != null) {
                            currentMetadata.setAuthorityID(rmd.getAuthorityID());
                        }
                        if (rmd.getAuthorityURI() != null) {
                            currentMetadata.setAuthorityURI(rmd.getAuthorityURI());
                        }
                        if (rmd.getAuthorityValue() != null) {
                            currentMetadata.setAuthorityValue(rmd.getAuthorityValue());
                        }
                        ds.addMetadata(currentMetadata);
                    }
                }
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("Metadata " + name + " not allowed in preferences.", e);
            }
        }
        this.getBean().reloadMetadataList();
    }

    private HierarchicalConfiguration getConfigForProject(Project p, XMLConfiguration xmlConf) {
        try {
            HierarchicalConfiguration use = xmlConf.configurationAt("globalConfig");
            List<HierarchicalConfiguration> projectConfs = xmlConf.configurationsAt("projectConfig");
            for (HierarchicalConfiguration projectConf : projectConfs) {
                if (p.getTitel().equals(projectConf.getString("[@project]"))) {
                    use = projectConf;
                    break;
                }
            }
            return use;
        } catch (IllegalArgumentException exception) {
            log.warn(exception);
        }
        return null;
    }

    private void configureRequest(SearchRequest req) {

        if (this.getBean() == null) {
            log.error("Metadaten bean null");
            return;
        }
        Project p = this.getBean().getMyProzess().getProjekt();
        XMLConfiguration xmlConf = ConfigPlugins.getPluginConfig(PROCESS_PLUGIN);
        if (xmlConf == null) {
            return;
        }
        List<HierarchicalConfiguration> projectConfs = xmlConf.configurationsAt("projectConfig");
        for (HierarchicalConfiguration projectConf : projectConfs) {
            if (p.getTitel().equals(projectConf.getString("[@project]"))) {
                req.setWantedFields(loadWantedFields(projectConf));
                req.setFilterProjects(loadFilterProjects(projectConf));
                return;
            }
        }
        HierarchicalConfiguration global = xmlConf.configurationAt("globalConfig");
        req.setWantedFields(loadWantedFields(global));
        req.setFilterProjects(loadFilterProjects(global));
    }

    private Set<String> loadWantedFields(HierarchicalConfiguration projectConf) {
        return Set.of(projectConf.getStringArray("wantedField"));
    }

    private List<String> loadFilterProjects(HierarchicalConfiguration projectConf) {
        return Arrays.asList(projectConf.getStringArray("searchableProject"));
    }

    public List<String> getPossibleFields() {
        if (this.possibleFields == null) {
            Metadaten mdBean = this.getBean();
            if (mdBean == null) {
                return MetadataManager.getDistinctMetadataNames();
            }
            Project p = mdBean.getMyProzess().getProjekt();
            this.possibleFields = MetadataManager.getDistinctMetadataNames();
            if (StorageProvider.getInstance()
                    .isFileExists(Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder(), "plugin_ProcessPlugin.xml"))) {
                XMLConfiguration xmlConf = ConfigPlugins.getPluginConfig(PROCESS_PLUGIN);
                if (xmlConf != null) {
                    HierarchicalConfiguration use = getConfigForProject(p, xmlConf);
                    if (use != null) {
                        List<String> whitelist = Arrays.asList(use.getStringArray("searchableField"));
                        if (!whitelist.isEmpty()) {
                            this.possibleFields = this.possibleFields.stream().filter(whitelist::contains).collect(Collectors.toList());
                        }
                    }
                }
            }
        }
        return this.possibleFields;
    }

    @Override
    public void setSearchInViaf(boolean serachInViaf) {
        // do nothing
    }

    public void generateValue() {

        XPathFactory xpfac = XPathFactory.instance();

        // convert the current docstruct into a jdom2 document
        Document doc = ModsHelper.generateModsSection(bean.getMyDocStruct(), myPrefs);
        if (doc == null) {
            Helper.setFehlerMeldung("mets_generation_error");
            return;
        }
        Element mods = doc.getRootElement();
        Element metadataSection = mods.getChild("extension", ModsHelper.MODS_NAMESPACE).getChild("goobi", ModsHelper.GOOBI_NAMESPACE);
        MetadataGeneration rule = null;

        for (MetadataGeneration mg : generationRules) {
            // check if condition is set
            if (rule != null) {
                break;
            }
            if (StringUtils.isNotBlank(mg.getCondition())) {
                // check if condition is fulfilled
                XPathExpression<Element> xp =
                        xpfac.compile(mg.getCondition(), Filters.element(), null, ModsHelper.MODS_NAMESPACE, ModsHelper.GOOBI_NAMESPACE);
                Element test = xp.evaluateFirst(metadataSection);
                if (test != null) {
                    // condition does match
                    rule = mg;
                }
            } else {
                // no condition is set, use first rule
                rule = mg;
            }
        }

        if (rule == null) {
            // no matching rule found, abort
            return;
        }

        String currentValue = rule.generateValue(myProcess, myPrefs, bean.getDocument(), xpfac, metadataSection);
        md.setValue(currentValue);
    }

    @Override
    public boolean isDisplayRestrictions() {
        return md.getType().isAllowAccessRestriction();
    }

    @Override
    public boolean isRestricted() {
        return md.isAccessRestrict();
    }

    @Override
    public void setRestricted(boolean restricted) {
        md.setAccessRestrict(restricted);
    }
}
