package org.goobi.managedbeans;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.config.ConfigurationHelper;



@Named("DashboardForm")
@SessionScoped
public class DashboardBean {

    private IDashboardPlugin plugin = null;

    public DashboardBean() {

    }

    @PostConstruct
    public void initializePlugins() {
        String pluginName = ConfigurationHelper.getInstance().getDashboardPlugin();

        IDashboardPlugin plugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
        if (plugin != null) {
            this.plugin = plugin;
        }

    }

    public IDashboardPlugin getPlugin() {
        return plugin;
    }

}
