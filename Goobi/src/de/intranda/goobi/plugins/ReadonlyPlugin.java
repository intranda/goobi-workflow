package de.intranda.goobi.plugins;

import org.goobi.production.plugin.interfaces.IMetadataPlugin;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;

import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class ReadonlyPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    @Override
    public String getTitle() {
        return "ReadonlyPlugin";
    }

    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsReadOnly.xhtml";
    }

}
