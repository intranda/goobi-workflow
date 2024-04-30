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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.faces.model.SelectItem;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.geonames.Toponym;
import org.goobi.api.display.DisplayCase;
import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.Process;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.VocabRecord;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.NormDataRecord;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.metadaten.search.EasyDBSearch;
import de.sub.goobi.metadaten.search.KulturNavImporter;
import de.sub.goobi.metadaten.search.ViafSearch;
import lombok.Data;
import ugh.dl.DocStruct;
import ugh.dl.HoldingElement;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
@Data
public class MetaPerson implements SearchableMetadata {
    private Person p;
    private int identifier;
    private Prefs myPrefs;
    private HoldingElement myDocStruct;
    private MetadatenHelper mdh;
    private DisplayCase myValues;
    private Metadaten bean;

    private String searchValue;
    private String searchOption;
    private List<List<NormData>> dataList;
    private List<NormData> currentData;

    private DocStruct docStruct;
    private MetadatenHelper metadatenHelper;
    private boolean showNotHits = false;

    private boolean isSearchInViaf;
    private boolean isSearchInKulturnav;

    // viaf data
    private ViafSearch viafSearch = new ViafSearch();

    // unused fields, but needed to use the same modals as regular metadata
    private NormDataRecord selectedRecord;
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
     * Allgemeiner Konstruktor ()
     */
    public MetaPerson(Person p, int inID, Prefs inPrefs, HoldingElement inStruct, Process inProcess, Metadaten bean) {
        this.myPrefs = inPrefs;
        this.p = p;
        this.identifier = inID;
        this.myDocStruct = inStruct;
        this.bean = bean;
        this.mdh = new MetadatenHelper(inPrefs, null);
        myValues = new DisplayCase(inProcess, p.getType());

        List<TagDescription> mainTagList = new ArrayList<>();
        mainTagList.add(new TagDescription("200", "_", "|", "a", null));
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

    /*#####################################################
     #####################################################
     ##
     ##																Getter und Setter
     ##
     #####################################################
     ####################################################*/

    public int getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

    public Person getP() {
        return this.p;
    }

    public void setP(Person p) {
        this.p = p;
    }

    public String getVorname() {
        if (this.p.getFirstname() == null) {
            return "";
        }
        return this.p.getFirstname();
    }

    public void setVorname(String inVorname) {
        if (inVorname == null) {
            inVorname = "";
        }
        this.p.setFirstname(inVorname);
        this.p.setDisplayname(getNachname() + ", " + getVorname());
    }

    public String getNachname() {
        if (this.p.getLastname() == null) {
            return "";
        }
        return this.p.getLastname();
    }

    public void setNachname(String inNachname) {
        if (inNachname == null) {
            inNachname = "";
        }
        this.p.setLastname(inNachname);
        this.p.setDisplayname(getNachname() + ", " + getVorname());
    }

    public String getRolle() {
        return this.p.getRole();
    }

    public void setRolle(String inRolle) {
        this.p.setRole(inRolle);
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(this.p.getRole());
        this.p.setType(mdt);

    }

    public List<SelectItem> getAddableRollen() {
        return this.mdh.getAddablePersonRoles(this.myDocStruct, this.p.getRole());
    }

    public List<NamePart> getAdditionalNameParts() {
        return p.getAdditionalNameParts();
    }

    public void setAdditionalNameParts(List<NamePart> nameParts) {
        p.setAdditionalNameParts(nameParts);
    }

    public void addNamePart() {
        List<NamePart> parts = p.getAdditionalNameParts();
        if (parts == null) {
            parts = new ArrayList<>();
        }
        NamePart part = new NamePart();
        part.setType("date");
        parts.add(part);
        p.setAdditionalNameParts(parts);
    }

    public List<String> getPossibleDatabases() {
        List<NormDatabase> databaseList = NormDatabase.getAllDatabases();
        List<String> abbrev = new ArrayList<>();
        for (NormDatabase norm : databaseList) {
            abbrev.add(norm.getAbbreviation());
        }
        return abbrev;
    }

    public List<String> getPossibleNamePartTypes() {
        // TODO configurable?
        List<String> possibleNamePartTypes = new ArrayList<>();
        possibleNamePartTypes.add("date");
        possibleNamePartTypes.add("termsOfAddress");
        return possibleNamePartTypes;
    }

    public String getNormdataValue() {
        return p.getAuthorityValue();
    }

    public void setNormdataValue(String normdata) {
        p.setAuthorityValue(normdata);
    }

    public void setNormDatabase(String abbrev) {
        NormDatabase database = NormDatabase.getByAbbreviation(abbrev);
        p.setAuthorityID(database.getAbbreviation());
        p.setAuthorityURI(database.getPath());
    }

    public String getNormDatabase() {
        if (p.getAuthorityURI() != null && p.getAuthorityID() != null) {
            NormDatabase ndb = NormDatabase.getByAbbreviation(p.getAuthorityID());
            return ndb.getAbbreviation();
        } else {
            return null;
        }
    }

    public boolean isShowAdditionalParts() {
        if (p.getType() == null) {
            return false;
        }
        return p.getType().isAllowNameParts();
    }

    public boolean isNormdata() {
        if (p.getType() == null) {
            return false;
        }
        return p.getType().isAllowNormdata();
    }

    @Override
    public String search() {
        if (!isSearchInViaf && StringUtils.isBlank(searchOption) && StringUtils.isBlank(searchValue)) {
            showNotHits = true;
            return "";
        } else if (isSearchInViaf) {
            viafSearch.performSearchRequest();
            showNotHits = viafSearch.getRecords() == null || viafSearch.getRecords().isEmpty();
        } else if (isSearchInKulturnav) {
            String knUrl = KulturNavImporter.constructSearchUrl(getSearchValue(), KulturNavImporter.getSourceForPerson());
            normdataList = KulturNavImporter.importNormData(knUrl);
            showNotHits = normdataList.isEmpty();
        } else { // default is GND
            String val = "";
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
            showNotHits = dataList == null || dataList.isEmpty();
        }
        searchValue = "";
        return "";
    }

    public String getData() {
        String mainValue = null;

        if (isSearchInViaf) {
            mainValue = viafSearch.getPersonData(p);

        } else if (isSearchInKulturnav) {
            if (Objects.nonNull(selectedRecord)) {
                for (NormData normdata : selectedRecord.getNormdataList()) {
                    if ("URI".equals(normdata.getKey())) {
                        String uriValue = normdata.getValues().get(0).getText();
                        p.setAutorityFile(DisplayType.kulturnav.name(), KulturNavImporter.BASE_URL, uriValue);
                        break;
                    }
                }
                // Use preferred value, otherwise use the first element in the list
                if (StringUtils.isNotBlank(selectedRecord.getPreferredValue())) {
                    mainValue = selectedRecord.getPreferredValue();
                } else if (CollectionUtils.isNotEmpty(selectedRecord.getValueList())) {
                    mainValue = selectedRecord.getValueList().get(0);
                }
            }
            normdataList = new ArrayList<>();
        } else {
            for (NormData normdata : currentData) {
                if ("NORM_IDENTIFIER".equals(normdata.getKey())) {
                    p.setAutorityFile("gnd", "http://d-nb.info/gnd/", normdata.getValues().get(0).getText());
                } else if ("NORM_NAME".equals(normdata.getKey())) {
                    mainValue = normdata.getValues().get(0).getText().replace("\\x152", "").replace("\\x156", "");
                }
            }
        }
        if (mainValue != null) {
            mainValue = filter(mainValue);
            // remove trailing comma on LoC records
            if (mainValue.endsWith(",")) {
                mainValue = mainValue.substring(0, mainValue.lastIndexOf(","));
            }
            if (mainValue.contains(",")) {
                p.setLastname(mainValue.substring(0, mainValue.lastIndexOf(",")).trim());
                p.setFirstname(mainValue.substring(mainValue.lastIndexOf(",") + 1).trim());
            } else if (mainValue.contains(" ")) {
                String[] nameParts = mainValue.split(" ");
                String first = "";
                String last = "";
                if (nameParts.length == 0) {
                    // do nothing
                } else if (nameParts.length == 1) {
                    last = nameParts[0];
                } else if (nameParts.length == 2) {
                    first = nameParts[0];
                    last = nameParts[1];
                } else { // if nameParts.length > 2
                    StringBuilder firstBuilder = new StringBuilder(nameParts[0]);
                    int counter = nameParts.length;
                    for (int i = 1; i < counter - 1; i++) {
                        firstBuilder.append(" ").append(nameParts[i]);
                    }
                    first = firstBuilder.toString();
                    last = nameParts[counter - 1];
                }
                p.setLastname(last);
                p.setFirstname(first);
            } else {
                p.setLastname(mainValue);
            }

            dataList = new ArrayList<>();

        }
        return "";

    }

    public boolean isShowNoHitFound() {
        return showNotHits;
    }

    @Override
    public DisplayType getMetadataDisplaytype() {
        return DisplayType.person;
    }

    @Override
    public void clearResults() {
    }
}
