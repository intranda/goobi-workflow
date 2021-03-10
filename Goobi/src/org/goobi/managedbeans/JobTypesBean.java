package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.goobi.beans.ExternalQueueJobType;
import org.omnifaces.cdi.ViewScoped;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ViewScoped
@Named("JobTypesBean")
@Data
public class JobTypesBean implements Serializable {
    private static final long serialVersionUID = 4451013586976652181L;

    @Inject
    private JobTypesCache jobTypesCache;
    private List<String> stepTitles;
    private List<ExternalQueueJobType> jobTypes;
    private ExternalQueueJobType currentJobType;
    private String newJobTypeName = "";

    @PostConstruct
    public void init() {
        this.stepTitles = StepManager.getDistinctStepTitles();
        try {
            this.jobTypes = StepManager.getExternalQueueJobTypes();
        } catch (DAOException e) {
            log.error(e);
        }
        if (!jobTypes.isEmpty()) {
            this.currentJobType = jobTypes.get(0);
        }
    }

    public void addStepToCurrentJobType(String stepTitle) {
        if (this.currentJobType != null) {
            this.currentJobType.getStepNames().add(stepTitle);
        }
    }

    public void removeStepFromJobType(String stepTitle, ExternalQueueJobType jobType) {
        jobType.getStepNames().remove(stepTitle);
    }

    public void addNewJobType() {
        ExternalQueueJobType newJobType = new ExternalQueueJobType();
        newJobType.setName(newJobTypeName);
        this.jobTypes.add(newJobType);
        this.currentJobType = newJobType;
        this.newJobTypeName = "";
    }

    public void apply() {
        try {
            this.jobTypesCache.applyAndPersist(jobTypes);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(Helper.getTranslation("errorPersistingJobTypes"));
        }
    }
}
