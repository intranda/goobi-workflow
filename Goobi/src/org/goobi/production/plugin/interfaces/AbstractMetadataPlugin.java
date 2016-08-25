package org.goobi.production.plugin.interfaces;

import java.util.List;

import javax.faces.model.SelectItem;

import lombok.Data;

import org.geonames.Toponym;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.sub.goobi.metadaten.Metadaten;
import ugh.dl.Metadata;

public @Data abstract class AbstractMetadataPlugin implements IMetadataPlugin {

    protected Metadata metadata;
    protected Metadaten bean;

    protected List<SelectItem> possibleItems;
    protected List<String> defaultItems;
    protected String defaultValue;
    
    public PluginType getType() {
        return PluginType.Metadata;
    }

    public String copy() {
        bean.setCurrentMetadata(metadata);
        bean.Copy();
        return "";
    }

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
            if (current != 0x98  && current != 0x9C ) {
                filtered.append(current);
            }
        }
        return filtered.toString();
    }

    @Override
    public String getPagePath() {
        return "";
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String search() {
        return "";
    }

    @Override
    public String getData() {
        return "";
    }

    @Override
    public String getUrl() {
        return "";
    }

    @Override
    public List<List<NormData>> getDataList() {
        return null;
    }

    @Override
    public List<NormData> getCurrentData() {
        return null;
    }

    @Override
    public String getSearchValue() {
        return "";
    }

    @Override
    public String getSearchOption() {
        return "";
    }

    @Override
    public void setSelectedItems(List<String> selectedItems) {
        
    }

    @Override
    public List<String> getSelectedItems() {
        return null;
    }

    @Override
    public void setSelectedItem(String selectedItem) {
        
    }

    @Override
    public String getSelectedItem() {
        return null;
    }
    
    public List<Toponym> getResultList(){
        return null;
    }
    
    public int getTotalResults() {
        return 0;
    }
}
