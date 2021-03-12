package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.goobi.beans.ExternalQueueJobType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SessionScoped
@Named("JobTypesBean")
@Data
public class JobTypesBean implements Serializable {
    private static final long serialVersionUID = 4451013586976652181L;

    //    @Inject
    //    private Conversation conversation;
    @Inject
    private JobTypesCache jobTypesCache;
    private List<String> stepTitles;
    private List<String> availableStepTitles;
    private List<ExternalQueueJobType> jobTypes;
    private ExternalQueueJobType currentJobType;

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

    public void initConversation() {
        //        if (!FacesContext.getCurrentInstance().isPostback() && conversation.isTransient()) {
        //            conversation.begin();
        //        }
    }

    public void addStepToCurrentJobType(String stepTitle) {
        if (this.currentJobType != null) {
            this.currentJobType.getStepNames().add(stepTitle);
            this.availableStepTitles.remove(stepTitle);
        }
    }

    public void removeStepFromCurrentJobType(String stepTitle) {
        if (this.currentJobType != null) {
            this.currentJobType.getStepNames().remove(stepTitle);
            this.availableStepTitles.add(stepTitle);
        }
    }

    public void removeStepFromJobType(String stepTitle, ExternalQueueJobType jobType) {
        jobType.getStepNames().remove(stepTitle);
    }

    public String addNewJobType() {
        this.currentJobType = new ExternalQueueJobType();
        this.availableStepTitles = new ArrayList<>(this.stepTitles);
        return "admin_jobtypes_edit.xhtml";
    }

    public String editJobType(ExternalQueueJobType jobType) {
        this.currentJobType = jobType.clone();
        this.availableStepTitles = new ArrayList<>(this.stepTitles);
        this.availableStepTitles.removeAll(this.currentJobType.getStepNameList());
        return "admin_jobtypes_edit.xhtml";
    }

    public String saveCurrentJobType() {
        int[] indexes = IntStream.range(0, this.jobTypes.size())
                .filter(i -> this.jobTypes.get(i).getId().equals(this.currentJobType.getId()))
                .toArray();
        if (indexes.length == 0) {
            this.jobTypes.add(this.currentJobType);
        } else {
            ExternalQueueJobType oldJobType = this.jobTypes.get(indexes[0]);
            if (oldJobType.isPaused() && !this.currentJobType.isPaused()) {
                //job was paused, but now isn't anymore => restart work 
                //TODO re-run paused jobs for this jobType
            }
            this.jobTypes.set(indexes[0], this.currentJobType);
        }
        this.apply();
        return "admin_jobtypes_all.xhtml";
    }

    public String cancelJobTypeEdit() {
        this.currentJobType = new ExternalQueueJobType();
        return "admin_jobtypes_all.xhtml";
    }

    public void pauseJobType(ExternalQueueJobType jobType) {
        jobType.setPaused(true);
        this.apply();
    }

    public void unPauseJobType(ExternalQueueJobType jobType) {
        jobType.setPaused(false);
        //TODO: re-run paused jobs for this jobType
        this.apply();
    }

    public void apply() {
        try {
            this.jobTypesCache.applyAndPersist(jobTypes);
        } catch (DAOException e) {
            Helper.setFehlerMeldung(Helper.getTranslation("errorPersistingJobTypes"));
        }
    }
}
