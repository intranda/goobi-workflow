package de.intranda.goobi.plugins;

import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

public class HtmlInputPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    @Override
    public String getTitle() {
        return "HtmlInputPlugin";
    }


}
