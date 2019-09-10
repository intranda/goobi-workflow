package org.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;

import de.sub.goobi.persistence.managers.InstitutionManager;
import lombok.Getter;
import lombok.Setter;

public class Institution implements Serializable, DatabaseObject, Comparable<Institution> {

    /**
     * 
     */
    private static final long serialVersionUID = -2608701994741239302L;

    @Getter
    @Setter
    private Integer id;

    @Getter
    @Setter
    private String shortName; // TODO unique name

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
    private List<InstitutionConfigurationObject> allowedDashboardlugins = new ArrayList<>();
    private List<InstitutionConfigurationObject> allowedWorkflowPlugins = new ArrayList<>();

    @Override
    public int compareTo(Institution o) {
        return this.shortName.compareTo(o.getShortName());
    }

    @Override
    public void lazyLoad() {

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
            allowedAdministrationPlugins = InstitutionManager.getConfiguredAdministrationPlugins(id, pluginNames);
        }
        return allowedAdministrationPlugins;
    }


    public List<InstitutionConfigurationObject> getAllowedStatisticsPlugins() {
        if (allowedStatisticsPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Statistics);
            allowedStatisticsPlugins = InstitutionManager.getConfiguredStatisticsPlugins(id, pluginNames);
        }
        return allowedStatisticsPlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedDashboardlugins() {
        if (allowedDashboardlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Dashboard);
            allowedDashboardlugins = InstitutionManager.getConfiguredDashboardPlugins(id, pluginNames);
        }
        return allowedDashboardlugins;
    }

    public List<InstitutionConfigurationObject> getAllowedWorkflowPlugins() {
        if (allowedWorkflowPlugins.isEmpty()) {
            List<String> pluginNames = PluginLoader.getListOfPlugins(PluginType.Workflow);
            allowedWorkflowPlugins = InstitutionManager.getConfiguredWorkflowPlugins(id, pluginNames);
        }
        return allowedWorkflowPlugins;
    }
}
