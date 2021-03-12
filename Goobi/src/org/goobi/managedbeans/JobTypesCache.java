package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import org.goobi.beans.JobType;
import org.omnifaces.cdi.Startup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Startup
public class JobTypesCache implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 2425466797563220681L;
    private ImmutableList<JobType> jobTypes;
    private ImmutableSet<String> pausedSteps;

    @PostConstruct
    public void init() {
        // get jobTypes from DB
        ImmutableList.Builder<JobType> newList = ImmutableList.<JobType> builder();
        try {
            newList.addAll(StepManager.getExternalQueueJobTypes()).build();
        } catch (DAOException e) {
            log.error(e);
        }
        jobTypes = newList.build();

        apply();
    }

    public boolean isStepPaused(String stepName) {
        return pausedSteps.contains(stepName);
    }

    /**
     * This takes the jobTypes and creates a new pausedSteps set. It also persists the jobTypes list.
     * 
     * @throws DAOException
     */
    public void applyAndPersist(List<JobType> newJobTypes) throws DAOException {
        this.jobTypes = ImmutableList.copyOf(newJobTypes);
        apply();
        StepManager.saveExternalQueueJobTypes(jobTypes);
    }

    private void apply() {
        ImmutableSet.Builder<String> newPausedSteps = ImmutableSet.<String> builder();

        for (JobType jobType : jobTypes) {
            if (jobType.isPaused()) {
                newPausedSteps.addAll(jobType.getStepNames());
            }
        }
        pausedSteps = newPausedSteps.build();
    }

}
