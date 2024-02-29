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
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.JobType;
import org.goobi.beans.Step;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@WindowScoped
@Named("JobTypesBean")
@Data
public class JobTypesBean implements Serializable {

    private static final long serialVersionUID = 4451013586976652181L;

    private static final String RETURN_PAGE_ALL = "admin_jobtypes_all.xhtml";
    private static final String RETURN_PAGE_EDIT = "admin_jobtypes_edit.xhtml";

    @Inject
    private JobTypesCache jobTypesCache;
    private List<String> stepTitles;
    private List<String> availableStepTitles;
    private List<JobType> jobTypes;
    private JobType currentJobType;

    @PostConstruct
    public void init() {
        this.stepTitles = StepManager.getDistinctStepTitles("schritte.titel", "schritte.typAutomatisch=1");
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
            this.availableStepTitles.remove(stepTitle);
        }
    }

    public void removeStepFromCurrentJobType(String stepTitle) {
        if (this.currentJobType != null) {
            this.currentJobType.getStepNames().remove(stepTitle);
            this.availableStepTitles.add(stepTitle);
        }
    }

    public void removeStepFromJobType(String stepTitle, JobType jobType) {
        jobType.getStepNames().remove(stepTitle);
    }

    public String addNewJobType() {
        this.currentJobType = new JobType();
        this.availableStepTitles = new ArrayList<>(this.stepTitles);
        return RETURN_PAGE_EDIT;
    }

    public String editJobType(JobType jobType) {
        this.currentJobType = new JobType(jobType);
        this.availableStepTitles = new ArrayList<>(this.stepTitles);
        this.availableStepTitles.removeAll(this.currentJobType.getStepNameList());
        return RETURN_PAGE_EDIT;
    }

    public String saveCurrentJobType() {
        int[] indexes = IntStream.range(0, this.jobTypes.size())
                .filter(i -> this.jobTypes.get(i).getId().equals(this.currentJobType.getId()))
                .toArray();
        if (indexes.length == 0) {
            this.jobTypes.add(this.currentJobType);
        } else {
            JobType oldJobType = this.jobTypes.get(indexes[0]);
            if (oldJobType.isPaused() && !this.currentJobType.isPaused()) {
                //job was paused, but now isn't anymore => restart work
                //TODO re-run paused jobs for this jobType
            }
            this.jobTypes.set(indexes[0], this.currentJobType);
        }
        this.apply();
        return RETURN_PAGE_ALL;
    }

    public String cancelJobTypeEdit() {
        this.currentJobType = new JobType();
        return RETURN_PAGE_ALL;
    }

    public void toggleJobType(JobType jobType) {
        if (jobType.isPaused()) {
            this.unPauseJobType(jobType);
        } else {
            this.pauseJobType(jobType);
        }
    }

    public void pauseJobType(JobType jobType) {
        jobType.setPaused(true);
        this.apply();
    }

    public void unPauseJobType(JobType jobType) {
        jobType.setPaused(false);
        this.apply();
    }

    public boolean isCurrentJobTypeNew() {
        return jobTypesCache.getJobTypes()
                .stream()
                .noneMatch(jt -> jt.getId().equals(this.currentJobType.getId()));
    }

    public String deleteCurrentJobType() {
        this.jobTypes = this.jobTypes
                .stream()
                .filter(jt -> !jt.getId().equals(this.currentJobType.getId()))
                .collect(Collectors.toList());
        this.apply();
        return RETURN_PAGE_ALL;
    }

    public void apply() {
        try {
            List<String> restartStepnames = this.jobTypesCache.applyAndPersist(jobTypes);
            // Restart the steps.
            if (!restartStepnames.isEmpty()) {
                List<Step> restartSteps = StepManager.getPausedSteps(restartStepnames);
                for (Step step : restartSteps) {
                    ScriptThreadWithoutHibernate scriptThread = new ScriptThreadWithoutHibernate(step);
                    scriptThread.startOrPutToQueue();
                    StepManager.setStepPaused(step.getId(), false);
                }
            }
        } catch (DAOException e) {
            log.error("error persisting jobTypes", e);
            Helper.setFehlerMeldung(Helper.getTranslation("errorPersistingJobTypes"));
        }
    }
}
