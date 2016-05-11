package org.goobi.production.plugin.interfaces;

import java.util.List;

import javax.faces.model.SelectItem;

import de.sub.goobi.metadaten.Metadaten;
import ugh.dl.Metadata;

public interface IMetadataPlugin extends IPlugin {

    public void setMetadata(Metadata metadata);

    public Metadata getMetadata();

    public String getPagePath();
    
    public String copy();
    
    public String delete();

    public void setBean(Metadaten bean);

    public void setDefaultValue(String defaultValue);
    
    public void setPossibleItems(List<SelectItem> items);

    public void setDefaultItems(List<String> selectedItems);
    
}
