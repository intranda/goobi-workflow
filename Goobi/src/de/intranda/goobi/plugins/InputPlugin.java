package de.intranda.goobi.plugins;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.intranda.digiverso.normdataimporter.model.NormData;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public @Data class InputPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    private String searchValue;
    private String searchOption;
    private List<List<NormData>> dataList;
    private List<NormData> currentData;
    
    @Override
    public String getTitle() {
        return "InputPlugin";
    }

    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsInput.xhtml";
    }

}
