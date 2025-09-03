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
package de.sub.goobi.metadaten.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.MarcRecord;
import de.intranda.digiverso.normdataimporter.model.MarcRecord.DatabaseUrl;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.intranda.digiverso.normdataimporter.model.ViafSearchParameter;
import de.intranda.digiverso.normdataimporter.model.ViafSearchRequest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import jakarta.faces.model.SelectItem;
import lombok.Data;
import ugh.dl.Corporate;
import ugh.dl.Metadata;
import ugh.dl.NamePart;
import ugh.dl.Person;

@Data
public class ViafSearch {

    // contains the list of available databases
    private List<SelectItem> searchSources;
    // contains the list of all field names
    private List<SelectItem> searchFields;

    private List<String> relations;

    private static final String VIAF_URL = "http://www.viaf.org/viaf/";
    private static final String VIAF_SEARCH_URL = VIAF_URL + "search?query=";
    // search request object, contains the search parameter, displayable fields,
    private ViafSearchRequest viafSearchRequest = new ViafSearchRequest();
    // selected authority record
    protected DatabaseUrl currentDatabase;

    // url to selected viaf cluster
    protected String currentCluster;

    // list of all records
    private List<MarcRecord> records;

    // main tag(s) to import as metadata value
    protected List<TagDescription> mainTagList = new ArrayList<>();
    // tags to display on the ui
    private List<TagDescription> visibleTagList = new ArrayList<>();
    // sort by default or holding count
    private boolean sorting = true;

    public ViafSearch() {
        // initialize viaf search
        createDatabaseList();
        createSearchFieldList();
        Collections.sort(searchSources, selectItemComparator);
        Collections.sort(searchFields, selectItemComparator);

        relations = Arrays.asList(ViafSearchParameter.getPossibleOperands());
    }

    /*
     * generates a SelectItem list of all possible fields. Value is internal field name, label is translated field name
     */

    private void createSearchFieldList() {
        searchFields = new ArrayList<>();
        for (String field : ViafSearchParameter.getPossibleSearchFields()) {
            searchFields.add(new SelectItem(field, Helper.getTranslation("NORM_viaf_field_" + field.substring(field.indexOf(".") + 1))));
        }
    }

    /*
     * generates a SelectItem list of all authority files. Value is internal name, label is a tranlated key
     */

    private void createDatabaseList() {
        searchSources = new ArrayList<>();
        for (String field : ViafSearchParameter.getPossibleDatabases()) {
            searchSources.add(new SelectItem(field, Helper.getTranslation("mets_viaf_catalogue_" + field)));
        }
    }

    private static Comparator<SelectItem> selectItemComparator = (item1, item2) -> {
        return item1.getLabel().compareTo(item2.getLabel());
    };

    public void performSearchRequest() {

        viafSearchRequest.setDisplayableTags(visibleTagList);
        List<MarcRecord> clusterRecords = NormDataImporter.importNormdataFromAuthorityDatabase(VIAF_SEARCH_URL,
                ConfigurationHelper.getInstance().getProxyUrl(), ConfigurationHelper.getInstance().getProxyPort(), viafSearchRequest,
                sorting ? "" : "&sortKeys=holdingscount", "&httpAccept=application/xml", "&recordSchema=info:srw/schema/1/marcxml-v1.1");
        if (clusterRecords == null || clusterRecords.isEmpty()) {
            records = null;
        }
        records = clusterRecords;

    }

    public void getMetadata(Metadata md) {
        if (currentDatabase != null) {
            MarcRecord recordToImport = NormDataImporter.getSingleMarcRecord(currentDatabase.getMarcRecordUrl());

            List<String> names = new ArrayList<>();
            for (TagDescription tag : mainTagList) {
                if (tag.getSubfieldCode() == null) {
                    String value = recordToImport.getControlfieldValue(tag.getDatafieldTag());
                    if (StringUtils.isNotBlank(value)) {
                        names.add(value);
                    }
                } else {
                    List<String> list = recordToImport.getFieldValues(tag.getDatafieldTag(), tag.getInd1(), tag.getInd2(), tag.getSubfieldCode());
                    if (list != null) {
                        names.addAll(list);
                    }
                }
            }

            if (!names.isEmpty()) {
                md.setAuthorityFile("viaf", VIAF_URL, currentDatabase.getMarcRecordUrl());
                md.setValue(names.get(0));
            }
        }
    }

    public String getPersonData(Person p) {
        String mainValue = null;
        if (currentDatabase != null) {
            MarcRecord recordToImport = NormDataImporter.getSingleMarcRecord(currentDatabase.getMarcRecordUrl());

            List<String> names = new ArrayList<>();
            for (TagDescription tag : mainTagList) {
                if (tag.getSubfieldCode() == null) {
                    String value = recordToImport.getControlfieldValue(tag.getDatafieldTag());
                    if (StringUtils.isNotBlank(value)) {
                        names.add(value);
                    }
                } else {
                    List<String> list = recordToImport.getFieldValues(tag.getDatafieldTag(), tag.getInd1(), tag.getInd2(), tag.getSubfieldCode());
                    if (list != null) {
                        names.addAll(list);
                    }
                }
            }

            if (!names.isEmpty()) {
                p.setAuthorityFile("viaf", VIAF_URL, currentDatabase.getMarcRecordUrl());
                p.addAuthorityUriToMap("viaf-cluster", currentCluster);
                p.addAuthorityUriToMap("viaf", currentDatabase.getMarcRecordUrl());
                mainValue = names.get(0);
            }
        }
        return mainValue;
    }

    /**
     * Get metadata from viaf record and write it into the corporate object.
     * 
     * @param corporate
     */

    public void writeCorporateData(Corporate corporate) {
        if (currentDatabase != null) {
            MarcRecord recordToImport = NormDataImporter.getSingleMarcRecord(currentDatabase.getMarcRecordUrl());

            // get mainName from 110 a
            String mainTag = "110";
            List<String> mainNames = recordToImport.getSubFieldValues(mainTag, null, null, "a");
            // or 210 a
            if (mainNames == null || mainNames.isEmpty()) {
                mainTag = "210";
                mainNames = recordToImport.getSubFieldValues(mainTag, null, null, "a");
            }
            // or 151 a
            if (mainNames == null || mainNames.isEmpty()) {
                mainTag = "151";
                mainNames = recordToImport.getSubFieldValues(mainTag, null, null, "a");
            }
            // or 111 a
            if (mainNames == null || mainNames.isEmpty()) {
                mainTag = "111";
                mainNames = recordToImport.getSubFieldValues(mainTag, null, null, "a");
            }
            // or 100 a
            if (mainNames == null || mainNames.isEmpty()) {
                mainTag = "210";
                mainNames = recordToImport.getSubFieldValues(mainTag, null, null, "a");
            }

            if (mainNames != null && !mainNames.isEmpty()) {
                corporate.setMainName(mainNames.get(0));
                corporate.getSubNames().clear();

                // get subNames from subfield b
                List<String> subNames = recordToImport.getFieldValues(mainTag, null, null, "b");
                if (subNames != null && !subNames.isEmpty()) {
                    for (String subName : subNames) {
                        corporate.addSubName(new NamePart("subname", subName));
                    }
                } else {
                    corporate.addSubName(new NamePart("subname", ""));
                }
                //get partName subfield $c$d$n
                List<String> partNames = recordToImport.getSubFieldValues(mainTag, null, null, "c", "g", "d", "n");
                if (partNames != null && !partNames.isEmpty()) {
                    corporate.setPartName(partNames.get(0));
                } else {
                    corporate.setPartName(null);
                }
                corporate.setAuthorityFile("viaf", VIAF_URL, currentDatabase.getMarcRecordUrl());
                corporate.addAuthorityUriToMap("viaf-cluster", currentCluster);
                corporate.addAuthorityUriToMap("viaf", currentDatabase.getMarcRecordUrl());
            }
        }

    }

    public void setSource(String source) {
        if (source.contains(";")) {
            String[] parts = source.split(";");
            for (String part : parts) {
                TagDescription td = parseTag(part.trim());
                if (td != null) {
                    mainTagList.add(td);
                }
            }
        } else {
            TagDescription td = parseTag(source);
            if (td != null) {
                mainTagList.add(td);
            }
        }
    }

    public void setField(String field) {
        if (field.contains(";")) {
            String[] parts = field.split(";");
            for (String part : parts) {
                TagDescription td = parseTag(part.trim());
                if (td != null) {
                    visibleTagList.add(td);
                }
            }

        } else {
            TagDescription td = parseTag(field);
            if (td != null) {
                visibleTagList.add(td);
            }
        }

    }

    public void clearResults() {

        records = null;
        if (viafSearchRequest == null) {
            viafSearchRequest = new ViafSearchRequest();
        }
        viafSearchRequest.cleanValues();

    }

    private TagDescription parseTag(String text) {
        String tag;
        String label = null;
        if (text.contains("=")) {
            tag = text.substring(0, text.indexOf("="));
            label = text.substring(text.indexOf("=") + 1);
        } else {
            tag = text;
        }

        if (tag.length() == 3) {
            return new TagDescription(tag, null, null, null, label);
        } else if (tag.length() > 5) {
            String mainTag = tag.substring(0, 3);
            String ind1 = tag.substring(3, 4);
            String ind2 = tag.substring(4, 5);
            String subCode = tag.substring(5);
            return new TagDescription(mainTag, ind1, ind2, subCode, label);
        }
        return null;
    }

}
