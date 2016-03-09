package org.goobi.production.plugin.interfaces;

import java.util.List;

import org.goobi.api.display.enums.DisplayType;

import ugh.dl.Metadata;

public interface IMetadataPlugin extends IPlugin {

    public void setMetadata(List<Metadata> md);

    public void setMetadataPluginType(DisplayType displayType);

    public DisplayType getMetadataPluginType();

}
