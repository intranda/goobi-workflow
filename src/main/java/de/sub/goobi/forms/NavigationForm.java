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
package de.sub.goobi.forms;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.api.mail.SendMail;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IPushPlugin;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import lombok.Getter;
import lombok.Setter;

@Named("NavigationForm")
@WindowScoped
public class NavigationForm implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1781002534164254710L;

    @Getter
    @Setter
    private List<String> possibleWorkflowPluginNames;

    @Getter
    @Setter
    private String currentWorkflowPluginName;

    @Getter
    @Setter
    private IWorkflowPlugin workflowPlugin;

    /**
     * Replaced by NavigationForm.parentMenu. Is still needed by some test classes, may not be removed until now.
     */
    @Getter
    @Setter
    private String aktuell = "0";
    @Getter
    @Setter
    private String parentMenu = "start_menu";
    @Getter
    @Setter
    private boolean showHelp = false;

    @Getter
    @Setter
    private boolean showExpertView = false;
    @Getter
    @Setter
    private boolean showSidebar = true;
    @Getter
    @Setter
    private String activeTab = "productionStatistics";
    @Getter
    @Setter
    private String activeImportTab = "recordImport";
    private HashMap<String, String> uiStatus = new HashMap<>();
    private String currentTheme = "/uii";

    @Inject
    @Push
    PushContext workflowPluginPush;

    public enum Theme {
        ui("ui"),
        uii("uii");

        private String id;

        private Theme(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

    }

    public NavigationForm() {
        possibleWorkflowPluginNames = PluginLoader.getListOfPlugins(PluginType.Workflow);
        Collections.sort(possibleWorkflowPluginNames);
        uiStatus.put("process_log_level_debug", "true");
    }

    public String Reload() {
        return "";
    }

    public String JeniaPopupCloseAction() {
        return "jeniaClosePopupFrameWithAction";
    }

    public String BenutzerBearbeiten() {
        return "BenutzerBearbeiten";
    }

    public Map<String, String> getUiStatus() {
        return uiStatus;
    }

    public String changeTheme() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String completePath = context.getExternalContext().getRequestServletPath();
        if (StringUtils.isNotBlank(completePath)) {
            completePath = completePath.replace("/uii", "");
            completePath = completePath.replace("/ui", "");
        }
        if ("/uii".equals(currentTheme)) {
            currentTheme = "/ui";
        } else {
            currentTheme = "/uii";
        }
        return currentTheme + completePath + "?faces-redirect=true";
    }

    public Theme getTheme() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String completePath = context.getExternalContext().getRequestServletPath();
        if (StringUtils.isNotBlank(completePath)) {
            String[] parts = completePath.split("/");
            if ("ui".equalsIgnoreCase(parts[1])) {
                return Theme.ui;
            } else {
                return Theme.uii;
            }
        }
        return Theme.ui;
    }

    public String setPlugin(String pluginName) {
        currentWorkflowPluginName = pluginName;
        workflowPlugin = (IWorkflowPlugin) PluginLoader.getPluginByTitle(PluginType.Workflow, currentWorkflowPluginName);
        if (workflowPlugin instanceof IPushPlugin) {
            ((IPushPlugin) workflowPlugin).setPushContext(workflowPluginPush);
        }

        if (PluginGuiType.FULL == workflowPlugin.getPluginGuiType()) {
            return workflowPlugin.getGui();
        }

        return "workflow";
    }

    public boolean isEmailActive() {
        return SendMail.getInstance().getConfig().isEnableStatusChangeMail();
    }

    public boolean isShowConfigEditor() {
        return ConfigurationHelper.getInstance().isEnableConfigEditor();
    }

}
