package de.sub.goobi.metadaten.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.MarcRecord;
import de.intranda.digiverso.normdataimporter.model.MarcRecord.DatabaseUrl;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.intranda.digiverso.normdataimporter.model.ViafSearchParameter;
import de.intranda.digiverso.normdataimporter.model.ViafSearchRequest;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import ugh.dl.Metadata;
import ugh.dl.Person;

@Data
public class ViafSearch {

    // contains the list of available databases
    private List<SelectItem> searchSources;
    // contains the list of all field names
    private List<SelectItem> searchFields;

    private List<String> relations;

    private static final String VIAF_URL = "http://www.viaf.org/viaf/search?query=";
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

    private static Comparator<SelectItem> selectItemComparator = new Comparator<SelectItem>() {
        @Override
        public int compare(SelectItem s1, SelectItem s2) {
            return s1.getLabel().compareTo(s2.getLabel());
        }
    };

    public void performSearchRequest() {

        viafSearchRequest.setDisplayableTags(visibleTagList);

        List<MarcRecord> clusterRecords = NormDataImporter.importNormdataFromAuthorityDatabase(VIAF_URL, viafSearchRequest,
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
                md.setAutorityFile("viaf", "http://www.viaf.org/viaf/", currentDatabase.getMarcRecordUrl());
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
                p.setAutorityFile("viaf", "http://www.viaf.org/viaf/", currentDatabase.getMarcRecordUrl());
                p.addAuthorityUriToMap("viaf-cluster", currentCluster);
                p.addAuthorityUriToMap("viaf", currentDatabase.getMarcRecordUrl());
                mainValue = names.get(0);
            }
        }
        return mainValue;
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
