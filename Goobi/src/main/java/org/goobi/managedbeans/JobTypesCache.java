package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.goobi.beans.JobType;
import org.omnifaces.cdi.Startup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Startup
public class JobTypesCache implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 2425466797563220681L;
    @Getter
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

        applyAndReturnRestarted();
    }

    public boolean isStepPaused(String stepName) {
        return pausedSteps.contains(stepName);
    }

    /**
     * This takes the jobTypes and creates a new pausedSteps set. It also persists the jobTypes list. Returns the restarted jobs, that is the jobs
     * that are in the old paused steps set and not in the new paused steps set.
     * 
     * @param newJobTypes
     * @return The step names that
     * @throws DAOException
     */
    public List<String> applyAndPersist(List<JobType> newJobTypes) throws DAOException {
        this.jobTypes = ImmutableList.copyOf(newJobTypes);
        List<String> restartedSteps = applyAndReturnRestarted();
        StepManager.saveExternalQueueJobTypes(jobTypes);
        return restartedSteps;
    }

    private List<String> applyAndReturnRestarted() {
        ImmutableSet.Builder<String> newPausedStepsBuilder = ImmutableSet.<String> builder();

        for (JobType jobType : jobTypes) {
            if (jobType.isPaused()) {
                newPausedStepsBuilder.addAll(jobType.getStepNames());
            }
        }
        ImmutableSet<String> newPausedSteps = newPausedStepsBuilder.build();
        if (pausedSteps != null) {
            List<String> restartedSteps = pausedSteps.stream()
                    .filter(stepTitle -> !newPausedSteps.contains(stepTitle))
                    .collect(Collectors.toList());
            pausedSteps = newPausedSteps;
            return restartedSteps;
        }
        pausedSteps = newPausedSteps;
        return new ArrayList<String>();
    }

}
