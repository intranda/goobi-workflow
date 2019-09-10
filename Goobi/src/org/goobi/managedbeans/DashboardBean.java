package org.goobi.managedbeans;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.User;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;

@ManagedBean(name = "DashboardForm")
@ViewScoped
public class DashboardBean {

    private IDashboardPlugin plugin = null;

    public DashboardBean() {

    }

    @PostConstruct
    public void initializePlugins() {

        User user = Helper.getCurrentUser();
        String pluginName = ConfigurationHelper.getInstance().getDashboardPlugin();

        if (StringUtils.isNotBlank(pluginName) && user != null
                && (user.getInstitution().isAllowAllPlugins() || user.getInstitution().isDashboardPluginAllowed(pluginName))) {

            IDashboardPlugin plugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
            if (plugin != null) {
                this.plugin = plugin;
            }
        }
    }

    public IDashboardPlugin getPlugin() {
        return plugin;
    }

}
