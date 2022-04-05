package org.goobi.production.plugin.interfaces;

import org.goobi.production.enums.PluginGuiType;

public interface IWorkflowPlugin extends IPlugin {

    public String getGui();

    /**
     * This method is used to define, if the plugin has no UI, a part UI or a full UI
     * 
     * @return the {@link PluginGuiType} of the plugin
     */

    default PluginGuiType getPluginGuiType() {
        return PluginGuiType.PART;
    }

}
