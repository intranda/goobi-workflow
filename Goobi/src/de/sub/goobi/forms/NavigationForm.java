package de.sub.goobi.forms;

import java.util.Collections;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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

import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.goobi.api.mail.SendMail;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IWorkflowPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;

@ManagedBean(name = "NavigationForm")
@SessionScoped
public class NavigationForm {

    private List<String> possibleWorkflowPluginNames;

    private String currentWorkflowPluginName;

    private IWorkflowPlugin workflowPlugin;

    public NavigationForm() {
        possibleWorkflowPluginNames = PluginLoader.getListOfPlugins(PluginType.Workflow);
        Collections.sort(possibleWorkflowPluginNames);
        uiStatus.put("process_log_level_debug", "true");
    }

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

    };

    private String aktuell = "0";
    private boolean showHelp = false;
    private boolean showEasyRead = false;
    private boolean showExpertView = false;
    private boolean showSidebar = true;
    private String activeTab = "productionStatistics";
    private String activeImportTab = "recordImport";
    private HashMap<String, String> uiStatus = new HashMap<>();
    private String currentTheme = "/uii";

    public String getAktuell() {
        return this.aktuell;
    }

    public void setAktuell(String aktuell) {
        this.aktuell = aktuell;
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

    public boolean isShowHelp() {
        return showHelp;
    }

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    public boolean isShowEasyRead() {
        return showEasyRead;
    }

    public void setShowEasyRead(boolean showEasyRead) {
        this.showEasyRead = showEasyRead;
    }

    public boolean isShowExpertView() {
        return showExpertView;
    }

    public void setShowExpertView(boolean showExpertView) {
        this.showExpertView = showExpertView;
    }

    public boolean isShowSidebar() {
        return showSidebar;
    }

    public void setShowSidebar(boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    public String getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(String activeTab) {
        this.activeTab = activeTab;
    }

    public String getActiveImportTab() {
        return activeImportTab;
    }

    public void setActiveImportTab(String activeImportTab) {
        this.activeImportTab = activeImportTab;
    }

    public HashMap<String, String> getUiStatus() {
        return uiStatus;
    }

    public String changeTheme() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        String completePath = context.getExternalContext().getRequestServletPath();
        if (StringUtils.isNotBlank(completePath)) {
            completePath = completePath.replace("/uii", "");
            completePath = completePath.replace("/ui", "");
        }
        if (currentTheme.equals("/uii")) {
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
            if (parts[1].equalsIgnoreCase("ui")) {
                return Theme.ui;
            } else {
                return Theme.uii;
            }
        }
        return Theme.ui;
    }

    public boolean isShowSecondLogField() {
        return ConfigurationHelper.getInstance().isShowSecondLogField();
    }

    public boolean isShowThirdLogField() {
        return ConfigurationHelper.getInstance().isShowThirdLogField();
    }

    public String getCurrentWorkflowPluginName() {
        return currentWorkflowPluginName;
    }

    public List<String> getPossibleWorkflowPluginNames() {
        return possibleWorkflowPluginNames;
    }

    public IWorkflowPlugin getWorkflowPlugin() {
        return workflowPlugin;
    }

    public void setCurrentWorkflowPluginName(String currentWorkflowPluginName) {
        this.currentWorkflowPluginName = currentWorkflowPluginName;
    }

    public void setPossibleWorkflowPluginNames(List<String> possibleWorkflowPluginNames) {
        this.possibleWorkflowPluginNames = possibleWorkflowPluginNames;
    }

    public void setWorkflowPlugin(IWorkflowPlugin workflowPlugin) {
        this.workflowPlugin = workflowPlugin;
    }

    public String setPlugin(String pluginName) {
        currentWorkflowPluginName = pluginName;
        workflowPlugin = (IWorkflowPlugin) PluginLoader.getPluginByTitle(PluginType.Workflow, currentWorkflowPluginName);
        return "workflow";
    }

    public boolean isEmailActive() {
        return SendMail.getInstance().getConfig().isEnableMail();
    }

}
