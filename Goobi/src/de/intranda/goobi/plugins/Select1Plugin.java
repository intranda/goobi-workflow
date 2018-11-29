package de.intranda.goobi.plugins;


import org.apache.commons.lang.StringUtils;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;

@PluginImplementation

public class Select1Plugin extends AbstractMetadataPlugin implements IMetadataPlugin {

   
    @Override
    public String getTitle() {
        return "Select1Plugin";
    }

   
    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsSingleSelect.xhtml";
    }
    
    public String getSelectedItem() {
        if (StringUtils.isBlank(metadata.getValue())) {
            
        }
        return metadata.getValue();
    }

    public void setSelectedItem(String selectedItem) {
        metadata.setValue(selectedItem);
    }
}
