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
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Named("DashboardForm")
@RequestScoped
@Log4j2
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = -8555010017712925180L;

    @Getter
    private IDashboardPlugin plugin = null;

    private boolean notInitializedYet = true;

    @PostConstruct
    public void initializePlugins() {
        User user = Helper.getCurrentUser();
        if (user != null) {
            String pluginName = user.getDashboardPlugin();

            if (StringUtils.isNotBlank(pluginName)
                    && (user.getInstitution().isAllowAllPlugins() || user.getInstitution().isDashboardPluginAllowed(pluginName))) {
                IDashboardPlugin newPlugin = (IDashboardPlugin) PluginLoader.getPluginByTitle(PluginType.Dashboard, pluginName);
                if (newPlugin != null) {
                    this.plugin = newPlugin;
                }
            }
            notInitializedYet = false;
        }
    }

    public String getPluginUi() {
        if (notInitializedYet) {
            initializePlugins();
        }

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
