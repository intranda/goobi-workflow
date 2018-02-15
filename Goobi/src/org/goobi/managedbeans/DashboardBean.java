package org.goobi.managedbeans;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.config.ConfigurationHelper;

@ManagedBean(name = "DashboardForm")
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
