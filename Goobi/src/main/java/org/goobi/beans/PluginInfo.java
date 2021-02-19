package org.goobi.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PluginInfo {
    private String filename;
    private Date buildDate;
    private String gitHash;
    private List<String> containedPlugins = new ArrayList<String>();
    private Collection<String> pluginsUsedInWorkflows;

    public void addContainedPlugin(String pluginTitle) {
        this.containedPlugins.add(pluginTitle);
    }
}
