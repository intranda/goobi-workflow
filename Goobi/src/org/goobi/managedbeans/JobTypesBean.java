package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import de.sub.goobi.persistence.managers.StepManager;
import lombok.Data;

@ViewScoped
@Named("JobTypesBean")
@Data
public class JobTypesBean implements Serializable {
    private static final long serialVersionUID = 4451013586976652181L;

    @Inject
    private JobTypesCache jobTypesCache;
    private List<String> stepTitles;

    @PostConstruct
    public void init() {
        this.stepTitles = StepManager.getDistinctStepTitles();
    }
}
