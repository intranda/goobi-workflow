package org.goobi.managedbeans;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.User;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Named("DashboardForm")
@ViewScoped
@Log4j2
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
        if (user != null) {
            String pluginName = user.getDashboardPlugin();

            if (StringUtils.isNotBlank(pluginName)
                    && (user.getInstitution().isAllowAllPlugins() || user.getInstitution().isDashboardPluginAllowed(pluginName))) {
                IDashboardPlugin plugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
                if (plugin != null) {
                    this.plugin = plugin;
                }
            }
        }
    }

    public String getPluginUi() {
        if (plugin == null) {
            return "";
        } else if (PluginGuiType.FULL == plugin.getPluginGuiType()) {
            ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
            try {
                ec.redirect(ec.getRequestContextPath() + plugin.getGuiPath());
            } catch (IOException e) {
                log.error(e);
            }
        }
        return plugin.getGuiPath();
    }
}
