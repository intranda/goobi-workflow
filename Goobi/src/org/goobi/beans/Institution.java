package org.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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


    private List<InstitutionConfigurationObject> allowedRulesets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedDockets = new ArrayList<>();

    private List<InstitutionConfigurationObject> allowedAuthentications = new ArrayList<>();

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
}
