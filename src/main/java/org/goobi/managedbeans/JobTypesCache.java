/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.goobi.beans.JobType;
import org.omnifaces.cdi.Startup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.annotation.PostConstruct;
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
        return new ArrayList<>();
    }

}
