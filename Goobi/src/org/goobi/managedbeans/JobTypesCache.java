package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.goobi.beans.ExternalQueueJobType;
import org.omnifaces.cdi.Startup;

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
    private List<ExternalQueueJobType> jobTypes;
    private ImmutableSet<String> pausedSteps;

    @PostConstruct
    public void init() {
        // get jobTypes from DB
        try {
            jobTypes = StepManager.getExternalQueueJobTypes();
        } catch (DAOException e) {
            log.error(e);
            jobTypes = new ArrayList<>();
        }

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
    public void applyAndPersist() throws DAOException {
        apply();
        StepManager.saveExternalQueueJobTypes(jobTypes);
    }

    private void apply() {
        ImmutableSet.Builder<String> newPausedSteps = ImmutableSet.<String> builder();

        for (ExternalQueueJobType jobType : jobTypes) {
            if (jobType.isPaused()) {
                newPausedSteps.addAll(jobType.getStepNames());
            }
        }
        pausedSteps = newPausedSteps.build();
    }

}
