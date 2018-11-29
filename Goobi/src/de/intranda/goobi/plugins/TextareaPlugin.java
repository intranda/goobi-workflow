package de.intranda.goobi.plugins;

import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;

@PluginImplementation
public class TextareaPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    @Override
    public String getTitle() {
        return "TextareaPlugin";
    }

    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsTextArea.xhtml";
    }

}
