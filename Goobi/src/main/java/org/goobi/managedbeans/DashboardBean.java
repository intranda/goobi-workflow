package org.goobi.managedbeans;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.User;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.helper.Helper;
import lombok.Getter;

@Named("DashboardForm")
@ViewScoped
public class DashboardBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8555010017712925180L;
    @Getter
    private IDashboardPlugin plugin = null;

    public DashboardBean() {

    }

    @PostConstruct
    public void initializePlugins() {

        User user = Helper.getCurrentUser();
        String pluginName = user.getDashboardPlugin();

        if (StringUtils.isNotBlank(pluginName) && user != null
                && (user.getInstitution().isAllowAllPlugins() || user.getInstitution().isDashboardPluginAllowed(pluginName))) {

            IDashboardPlugin plugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
            if (plugin != null) {
                this.plugin = plugin;
            }
        }
    }
}
