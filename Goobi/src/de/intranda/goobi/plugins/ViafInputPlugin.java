package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.MarcRecord;
import de.intranda.digiverso.normdataimporter.model.MarcRecord.DatabaseUrl;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.intranda.digiverso.normdataimporter.model.ViafSearchParameter;
import de.intranda.digiverso.normdataimporter.model.ViafSearchRequest;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public @Data class ViafInputPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    private boolean showNotHits = false;

    private List<SelectItem> searchSources;
    private List<SelectItem> searchFields;
    private List<String> relations;

    private static final String VIAF_URL = "http://www.viaf.org/viaf/search?query=";

    private ViafSearchRequest searchRequest = new ViafSearchRequest();

    private DatabaseUrl currentDatabase;

    private List<MarcRecord> records;

    private List<TagDescription> mainTagList = new ArrayList<>();
    private List<TagDescription> visibleTagList = new ArrayList<>();

    public ViafInputPlugin() {
        createDatabaseList();
        createSearchFieldList();
        Collections.sort(searchSources, selectItemComparator);
        Collections.sort(searchFields, selectItemComparator);

        relations = Arrays.asList(ViafSearchParameter.getPossibleOperands());

    }

    private void createSearchFieldList() {
        searchFields = new ArrayList<>();
        for (String field : ViafSearchParameter.getPossibleSearchFields()) {
            searchFields.add(new SelectItem(field, Helper.getTranslation("mets_viaf_field_" + field.substring(field.indexOf(".") + 1))));
        }
    }

    private void createDatabaseList() {
        searchSources = new ArrayList<>();
        for (String field : ViafSearchParameter.getPossibleDatabases()) {
            searchSources.add(new SelectItem(field, Helper.getTranslation("mets_viaf_catalogue_" + field)));
        }
    }

    @Override
    public String getTitle() {
        return "viaf";
    }

    @Override
    public String getPagePath() {
        return "";
    }

    @Override
    public String search() {
        searchRequest.setDisplayableTags(visibleTagList);

        List<MarcRecord> clusterRecords = NormDataImporter.importNormdataFromAuthorityDatabase(VIAF_URL, searchRequest, "&sortKeys=holdingscount", "&httpAccept=application/xml",
                "&recordSchema=info:srw/schema/1/marcxml-v1.1");
        if (clusterRecords == null || clusterRecords.isEmpty()) {
            records = null;
            showNotHits = true;
        } else {
            showNotHits = false;
        }
        records = clusterRecords;
        return "";
    }

    @Override
    public String getData() {
        if (currentDatabase != null) {
            MarcRecord recordToImport =  NormDataImporter.getSingleMarcRecord(currentDatabase.getMarcRecordUrl());

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
                metadata.setAutorityFile("viaf", "http://www.viaf.org/viaf/", currentDatabase.getMarcRecordUrl());
                metadata.setValue(names.get(0));
            }
        }
        return "";
    }

    @Override
    public boolean isShowNoHitFound() {
        return showNotHits;
    }

    @Override
    public void clearResults() {
        //        dataList = null;
        records = null;
        searchRequest = new ViafSearchRequest();
    }

    @Override
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

    @Override
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

    private Comparator<SelectItem> selectItemComparator = new Comparator<SelectItem>() {
        @Override
        public int compare(SelectItem s1, SelectItem s2) {
            return s1.getLabel().compareTo(s2.getLabel());
        }
    };

}
