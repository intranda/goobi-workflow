package de.sub.goobi.metadaten;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.geonames.Toponym;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.Process;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.VocabRecord;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.sub.goobi.metadaten.search.EasyDBSearch;
import de.sub.goobi.metadaten.search.ViafSearch;
import lombok.Data;
import ugh.dl.Corporate;
import ugh.dl.HoldingElement;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Prefs;

@Data
public class MetaCorporate implements SearchableMetadata {

    private Corporate corporate;
    private Prefs myPrefs;
    private Metadaten bean;

    private String searchValue;
    private String searchOption;
    private List<List<NormData>> dataList;
    private List<NormData> currentData;

    private HoldingElement docStruct;
    private MetadatenHelper metadatenHelper;
    private boolean showNotHits = false;

    private boolean isSearchInViaf;

    // viaf data
    private ViafSearch viafSearch = new ViafSearch();

    // unused fields, but needed to use the same modals as regular metadata
    private List<Toponym> resultList;
    private List<NormDataRecord> normdataList;
    private int totalResults;
    private EasyDBSearch easydbSearch = new EasyDBSearch();
    private List<StringPair> vocabularySearchFields;
    private String vocabularyName;
    private List<VocabRecord> records;
    private String vocabularyUrl;
    private VocabRecord selectedVocabularyRecord;

    /**
     * constructor
     * 
     * @param corporate
     * @param inID
     * @param inPrefs
     * @param inStruct
     * @param inProcess
     * @param bean
     */

    public MetaCorporate(Corporate corporate, Prefs inPrefs, HoldingElement inStruct, Process inProcess, Metadaten bean) {
        this.myPrefs = inPrefs;
        this.corporate = corporate;
        this.docStruct = inStruct;
        this.bean = bean;
        this.metadatenHelper = new MetadatenHelper(inPrefs, null);

        List<TagDescription> mainTagList = new ArrayList<>();
        mainTagList.add(new TagDescription("100", "_", "_", "a", null));
        mainTagList.add(new TagDescription("110", "_", "_", "a", null));
        mainTagList.add(new TagDescription("150", "_", "_", "a", null));
        mainTagList.add(new TagDescription("151", "_", "_", "a", null));
        mainTagList.add(new TagDescription("200", "_", "_", "a", null));
        mainTagList.add(new TagDescription("210", "_", "_", "a", null));
        mainTagList.add(new TagDescription("400", "_", "_", "a", null));
        mainTagList.add(new TagDescription("410", "_", "_", "a", null));
        mainTagList.add(new TagDescription("450", "_", "_", "a", null));
        viafSearch.setMainTagList(mainTagList);
    }

    @Override
    public DisplayType getMetadataDisplaytype() {
        return DisplayType.corporate;
    }

    @Override
    public String search() {
        if (isSearchInViaf) {
            viafSearch.performSearchRequest();
            if (viafSearch.getRecords() == null) {
                showNotHits = true;
            } else {
                showNotHits = false;
            }
            return "";
        }

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
        dataList = NormDataImporter.importNormDataList(string, 3);

        if (dataList == null || dataList.isEmpty()) {
            showNotHits = true;
        } else {
            showNotHits = false;
        }

        return "";
    }

    public String getData() {
        String mainValue = null;
        StringBuilder partName = new StringBuilder();
        List<NamePart> subNames = new ArrayList<>();
        if (isSearchInViaf) {
            viafSearch.writeCorporateData(corporate);

        } else {

            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    corporate.setAutorityFile("gnd", "http://d-nb.info/gnd/", normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_ORGANIZATION")) {
                    mainValue = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "");
                } else if (normdata.getKey().equals("NORM_SUB_ORGANIZATION")) {
                    subNames.add(new NamePart("subname", normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "")));
                } else if (normdata.getKey().equals("NORM_PART_ORGANIZATION")) {
                    if (partName.length() > 0) {
                        partName.append("; ");
                    }
                    partName.append(normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", ""));
                }
            }
        }
        if (mainValue != null) {
            mainValue = filter(mainValue);
            // remove trailing comma on LoC records
            if (mainValue.endsWith(",")) {
                mainValue = mainValue.substring(0, mainValue.lastIndexOf(","));
            }
            if (subNames.isEmpty()) {
                subNames.add(new NamePart("subname", ""));
            }
            corporate.setMainName(mainValue);
            corporate.setSubNames(subNames);
            corporate.setPartName(partName.toString());

            dataList = new ArrayList<>();
        }
        return "";

    }

    public boolean isShowNoHitFound() {
        return showNotHits;
    }

    @Override
    public void clearResults() {
    }

    public String getRole() {
        return corporate.getRole();
    }

    public void setRole(String inRole) {
        corporate.setRole(inRole);
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(corporate.getRole());
        corporate.setType(mdt);
    }

    public List<SelectItem> getAddableRoles() {
        return this.metadatenHelper.getAddableCorporateRoles(docStruct, corporate.getRole());
    }

    public void setMainName(String name) {
        corporate.setMainName(name);
    }

    public String getMainName() {
        return corporate.getMainName();
    }

    public void setPartName(String name) {
        corporate.setPartName(name);
    }

    public String getPartName() {
        return corporate.getPartName();
    }

    public List<NamePart> getSubNames() {
        if (corporate.getSubNames().isEmpty()) {
            corporate.addSubName(new NamePart("subname", ""));
        }

        return corporate.getSubNames();
    }

    public void addSubName() {
        corporate.addSubName(new NamePart("subname", ""));
    }

    public void removeSubName(NamePart name) {
        corporate.removeSubName(name);
    }

    public List<String> getPossibleDatabases() {
        List<NormDatabase> databaseList = NormDatabase.getAllDatabases();
        List<String> abbrev = new ArrayList<>();
        for (NormDatabase norm : databaseList) {
            abbrev.add(norm.getAbbreviation());
        }
        return abbrev;
    }

    public boolean isNormdata() {
        if (corporate.getType() == null) {
            return false;
        }
        return corporate.getType().isAllowNormdata();
    }

    public String getNormdataValue() {
        return corporate.getAuthorityValue();
    }

    public void setNormdataValue(String normdata) {
        corporate.setAuthorityValue(normdata);
    }

    public void setNormDatabase(String abbrev) {
        NormDatabase database = NormDatabase.getByAbbreviation(abbrev);
        corporate.setAuthorityID(database.getAbbreviation());
        corporate.setAuthorityURI(database.getPath());
    }

    public String getNormDatabase() {
        if (corporate.getAuthorityURI() != null && corporate.getAuthorityID() != null) {
            NormDatabase ndb = NormDatabase.getByAbbreviation(corporate.getAuthorityID());
            return ndb.getAbbreviation();
        } else {
            return null;
        }
    }

    public int getSubNameSize() {
        return corporate.getSubNames().size();
    }
}
