package org.goobi.production.plugin.interfaces;

import java.util.List;

import javax.faces.model.SelectItem;

import lombok.Data;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

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
}
