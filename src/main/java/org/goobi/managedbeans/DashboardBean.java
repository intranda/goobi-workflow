/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import java.io.IOException;
import java.util.Objects;
import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.User;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.extern.log4j.Log4j2;

/**
 * Provides the dashboard plugin the main menu and the dashboard page work with.
 *
 * The scope is a compromise. Looking a plugin up builds a new plugin manager and scans the plugin folder, and the bean is referenced from the main
 * menu, so request scope repeated that scan for every request. A longer scope is not an option either: the plugin instance is what holds the
 * dashboard data, and plugins compute it in their constructor or cache it in lazy getters, so anything beyond a single view would show the numbers
 * of the first page load until the session ends. View scope does the lookup once per rendered page and hands out fresh data on every reload.
 */
@Named("DashboardForm")
@ViewScoped
@Log4j2
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = -8555010017712925180L;

    private IDashboardPlugin plugin = null;

    // the plugin name the current plugin was loaded for, so that a changed user setting is picked up without another lookup per view
    private String loadedPluginName = null;

    @PostConstruct
    public void initializePlugins() {
        User user = Helper.getCurrentUser();
        if (user == null) {
            return;
        }
        String pluginName = user.getDashboardPlugin();
        loadedPluginName = pluginName;
        plugin = null;

        if (isDashboardAvailable()) {
            plugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
        }
    }

    /**
     * Tells whether a dashboard is to be shown at all. The main menu and the breadcrumb of every single page need to know this, and they must not
     * pay for it: looking the plugin up scans the plugin folder, and building it runs whatever the plugin does in its constructor, which for the
     * extended dashboard means several database queries. Only the dashboard page itself asks for the plugin, through {@link #getPlugin()}.
     */
    public boolean isDashboardAvailable() {
        User user = Helper.getCurrentUser();
        if (user == null || StringUtils.isBlank(user.getDashboardPlugin())) {
            return false;
        }
        return user.getInstitution().isDashboardPluginAvailable(user.getDashboardPlugin());
    }

    /**
     * Looking a plugin up scans the plugin folder, so it is only repeated when the user configured a different dashboard plugin - or when nobody was
     * logged in yet as this bean was created.
     */
    public IDashboardPlugin getPlugin() {
        User user = Helper.getCurrentUser();
        String configuredPluginName = user == null ? null : user.getDashboardPlugin();
        if (!Objects.equals(configuredPluginName, loadedPluginName)) {
            initializePlugins();
        }
        return plugin;
    }

    public String getPluginUi() {
        IDashboardPlugin plugin = getPlugin();
        if (plugin == null) {
            return "";
        }
        ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
        // redirect to the plugin page
        if (PluginGuiType.FULL == plugin.getPluginGuiType()) {
            try {
                ec.redirect(ec.getRequestContextPath() + plugin.getGuiPath());
            } catch (IOException e) {
                log.error(e);
            }
        }
        // redirect to index.xhtml, if the request comes from a different page
        else if (PluginGuiType.PART == plugin.getPluginGuiType()
                && !"/uii/index.xhtml".equals(FacesContextHelper.getCurrentFacesContext().getViewRoot().getViewId())) {
            try {
                ec.redirect(ec.getRequestContextPath() + "/uii/index.xhtml");
            } catch (IOException e) {
                log.error(e);
            }
        }
        return plugin.getGuiPath();
    }
}
