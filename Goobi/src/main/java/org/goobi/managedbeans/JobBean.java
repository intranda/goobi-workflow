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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.flow.jobs.AbstractGoobiJob;
import org.goobi.production.flow.jobs.IGoobiJob;
import org.goobi.production.flow.jobs.QuartzJobDetails;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@WindowScoped
@Named
public class JobBean implements Serializable {

    private static final long serialVersionUID = 7490719476585282366L;

    private transient Scheduler scheduler = null;

    private transient Set<Class<? extends AbstractGoobiJob>> allJobTypes;

    @Getter
    private boolean paused;

    @Getter
    @Setter
    private transient QuartzJobDetails quartzJobDetails;

    @Getter
    private transient List<QuartzJobDetails> activeJobs = new ArrayList<>();

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = new StdSchedulerFactory().getScheduler();
        allJobTypes = new Reflections().getSubTypesOf(AbstractGoobiJob.class);

        // find all existing jobs
        for (Class<? extends IGoobiJob> jobClass : allJobTypes) {
            try {
                IGoobiJob job = jobClass.getDeclaredConstructor().newInstance();
                QuartzJobDetails details = new QuartzJobDetails();
                activeJobs.add(details);
                details.setJob(job);
                details.setJobName(job.getJobName());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                log.error(e);
            }
        }

        // order jobs by name
        Collections.sort(activeJobs);

        // find active jobs
        for (String groupName : scheduler.getJobGroupNames()) {

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                for (QuartzJobDetails details : activeJobs) {
                    if (jobName.equals(details.getJobName())) {
                        details.setJobKey(jobKey);

                        details.setJobName(jobName);

                        String jobGroup = jobKey.getGroup();
                        details.setJobGroup(jobGroup);

                        //get job's trigger
                        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                        Trigger trigger = triggers.get(0);
                        if (trigger instanceof CronTrigger) {
                            CronTrigger cronTrigger = (CronTrigger) trigger;
                            String cronExpr = cronTrigger.getCronExpression();
                            details.setCronExpression(cronExpr);
                        }
                        if (TriggerState.PAUSED.equals(scheduler.getTriggerState(trigger.getKey()))) {
                            details.setPaused(true);
                        }

                        Date nextFireTime = trigger.getNextFireTime();
                        Date lastFireTime = trigger.getPreviousFireTime();
                        details.setNextFireTime(nextFireTime);
                        details.setPreviousFireTime(lastFireTime);
                    }
                }
            }
        }
    }

    public void pauseAllJobs() {
        try {
            scheduler.pauseAll();
            for (QuartzJobDetails details : activeJobs) {
                details.setPaused(true);
            }
        } catch (SchedulerException e) {
            log.error(e);
        }
        paused = true;
    }

    public void resumeAllJobs() {
        try {
            scheduler.resumeAll();
            for (QuartzJobDetails details : activeJobs) {
                details.setPaused(false);
            }
        } catch (SchedulerException e) {
            log.error(e);
        }
        paused = false;
    }

    public void pauseJob() {
        try {
            scheduler.pauseJob(quartzJobDetails.getJobKey());
            quartzJobDetails.setPaused(true);
        } catch (SchedulerException e) {
            log.error(e);
        }
    }

    public void resumeJob() {
        try {
            scheduler.resumeJob(quartzJobDetails.getJobKey());
            quartzJobDetails.setPaused(false);
        } catch (SchedulerException e) {
            log.error(e);
        }
    }

}
