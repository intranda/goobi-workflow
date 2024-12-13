package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginType;

public interface IFooterPlugin extends IPlugin {
    default String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    String getIcon();

    @Override
    default PluginType getType() {
        return PluginType.Footer;
    }

    void initialize() throws Exception;

    void execute() throws Exception;

    default String getModal() {
        return null;
    }
}
