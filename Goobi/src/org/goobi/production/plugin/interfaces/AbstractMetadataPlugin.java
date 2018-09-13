package org.goobi.production.plugin.interfaces;

import java.util.List;

import javax.faces.model.SelectItem;

import org.geonames.Toponym;
import org.goobi.production.enums.PluginType;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.sub.goobi.metadaten.Metadaten;
import lombok.Data;
import ugh.dl.Metadata;

public @Data abstract class AbstractMetadataPlugin implements IMetadataPlugin {

    protected Metadata metadata;
    protected Metadaten bean;

    protected List<SelectItem> possibleItems;
    protected List<String> defaultItems;
    protected String defaultValue;

    private String pagePath;
    private String title;
    private String data;
    private String url;
    private List<List<NormData>> dataList;
    private List<NormData> currentData;
    private String searchValue;
    private String searchOption;
    private List<String> selectedItems;
    private String selectedItem;

    @Override
    public PluginType getType() {
        return PluginType.Metadata;
    }

    @Override
    public String copy() {
        bean.setCurrentMetadata(metadata);
        bean.Copy();
        return "";
    }

    @Override
    public String delete() {
        bean.setCurrentMetadata(metadata);
        bean.delete();
        return "";
    }

    public String filter(String str) {
        StringBuilder filtered = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char current = str.charAt(i);
            // current != 0x152 && current != 0x156
            if (current != 0x98 && current != 0x9C) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }

    @Override
    public String search() {
        return "";
    }

    @Override
    public List<Toponym> getResultList() {
        return null;
    }

    @Override
    public int getTotalResults() {
        return 0;
    }

    public boolean isShowNoHitFound() {
        return false;
    }

    /**
     * this method is used to disable the edition of the identifier field, the default value is false, so it can be edited
     * but it can be overwritten by individual plugins
     */

    @Override
    public boolean isDisableIdentifierField() {
        return false;
    }

    /**
     * this method is used to disable the edition of the metadata value field, the default value is false,
     * but it can be overwritten by individual plugins
     */

    @Override
    public boolean isDisableMetadataField() {
        return false;
    }
}
