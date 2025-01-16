/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.beans;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.InstitutionManager;
import lombok.Getter;
import lombok.Setter;

public class Institution extends AbstractJournal implements Serializable, DatabaseObject, Comparable<Institution> {

    private static final long serialVersionUID = -2608701994741239302L;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String shortName;

    @Getter
    @Setter
    private String longName;
    @Getter
    @Setter
    private boolean allowAllRulesets;
    @Getter
    @Setter
    private boolean allowAllDockets;
    @Getter
    @Setter
    private boolean allowAllAuthentications;

    @Getter
    @Setter
    private boolean allowAllPlugins;

    private List<InstitutionConfigurationObject> allowedRulesets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedDockets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedAuthentications = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedAdministrationPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedStatisticsPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedDashboardPlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedWorkflowPlugins = new ArrayList<>();

    @Getter
    @Setter
    // any additional data is hold in a map and gets stored in an xml column, it is searchable using xpath
    // individual values can be extracted: 'select ExtractValue(additional_data, '/root/key') from benutzer'
    protected transient Map<String, String> additionalData = new HashMap<>();

    @Override
    public int compareTo(Institution o) {
        return this.shortName.compareTo(o.getShortName());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object.getClass().equals(this.getClass()))) {
            return false;
        } else if (object == this) {
            return true;
        }
        Institution institution = (Institution) (object);
        return institution.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        // The numbers here are prime numbers (from 19 to 97)
        int hashCode = super.hashCode();
        hashCode = hashCode + 19 * this.id.hashCode();
        hashCode = hashCode + 23 * this.shortName.hashCode();
        hashCode = hashCode + 29 * this.longName.hashCode();
        hashCode = hashCode + (this.allowAllRulesets ? 31 : 37);
        hashCode = hashCode + (this.allowAllDockets ? 41 : 43);
        hashCode = hashCode + (this.allowAllAuthentications ? 47 : 53);
        hashCode = hashCode + (this.allowAllPlugins ? 59 : 61);
        hashCode = hashCode + 67 * this.allowedRulesets.hashCode();
        hashCode = hashCode + 71 * this.allowedDockets.hashCode();
        hashCode = hashCode + 73 * this.allowedAuthentications.hashCode();
        hashCode = hashCode + 79 * this.allowedAdministrationPlugins.hashCode();
        hashCode = hashCode + 83 * this.allowedStatisticsPlugins.hashCode();
        hashCode = hashCode + 89 * this.allowedDashboardPlugins.hashCode();
        hashCode = hashCode + 97 * this.allowedWorkflowPlugins.hashCode();
        return hashCode;
    }

    @Override
    public void lazyLoad() {
        // do nothing

    }

    public List<InstitutionConfigurationObject> getAllowedRulesets() {
        if (allowedRulesets.isEmpty()) {
            allowedRulesets = InstitutionManager.getConfiguredRulesets(id);
        }
        return allowedRulesets;
    }

    public List<InstitutionConfigurationObject> getAllowedDockets() {
        if (allowedDockets.isEmpty()) {
            allowedDockets = InstitutionManager.getConfiguredDockets(id);
        }
        return allowedDockets;
    }

    public List<InstitutionConfigurationObject> getAllowedAuthentications() {
        if (allowedAuthentications.isEmpty()) {
            allowedAuthentications = InstitutionManager.getConfiguredAuthentications(id);
        }
        return allowedAuthentications;
    }

    public List<InstitutionConfigurationObject> getAllowedAdministrationPlugins() {
        if (allowedAdministrationPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Administration);
            if (!pluginNames.isEmpty()) {
                allowedAdministrationPlugins = InstitutionManager.getConfiguredAdministrationPlugins(id, pluginNames);
            }
        }
        return allowedAdministrationPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedStatisticsPlugins() {
        if (allowedStatisticsPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Statistics);
            if (!pluginNames.isEmpty()) {
                allowedStatisticsPlugins = InstitutionManager.getConfiguredStatisticsPlugins(id, pluginNames);
            }
        }
        return allowedStatisticsPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedDashboardPlugins() {
        if (allowedDashboardPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Dashboard);
            if (!pluginNames.isEmpty()) {
                allowedDashboardPlugins = InstitutionManager.getConfiguredDashboardPlugins(id, pluginNames);
            }
        }
        return allowedDashboardPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedWorkflowPlugins() {
        if (allowedWorkflowPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Workflow);
            if (!pluginNames.isEmpty()) {
                allowedWorkflowPlugins = InstitutionManager.getConfiguredWorkflowPlugins(id, pluginNames);
            }
        }
        return allowedWorkflowPlugins;
    }

    public boolean isAdministrationPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedAdministrationPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isWorkflowPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedWorkflowPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isStatisticsPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedStatisticsPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    public boolean isDashboardPluginAllowed(String pluginName) {
        if (isAllowAllPlugins()) {
            return true;
        }
        for (InstitutionConfigurationObject ico : getAllowedDashboardPlugins()) {
            if (ico.getObject_name().equals(pluginName)) {
                return ico.isSelected();
            }
        }
        return false;
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.INSTITUTION;
    }

    @Override
    public Path getDownloadFolder() {
        return Paths.get(ConfigurationHelper.getInstance().getGoobiFolder(), "uploads", "institution", shortName);
    }

    @Override
    public String getUploadFolder() {
        return Paths.get(ConfigurationHelper.getInstance().getGoobiFolder(), "uploads", "institution", shortName).toString();
    }
}
