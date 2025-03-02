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

package org.goobi.production.flow.jobs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.reflections.Reflections;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import lombok.extern.log4j.Log4j2;

/**
 * JobManager organizes all scheduled jobs
 * 
 * @author Steffen Hankiewicz
 * @author Igor Toker
 * @version 21.10.2009
 */
@Log4j2
public class JobManager implements ServletContextListener {

    /**
     * Restarts timed Jobs
     * 
     * @throws SchedulerException
     */
    public static void restartTimedJobs() throws SchedulerException {
        stopTimedJobs();
        startTimedJobs();
    }

    /**
     * Stops timed updates of HistoryManager
     * 
     * @throws SchedulerException
     */
    private static void stopTimedJobs() throws SchedulerException {
        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
        schedFact.getScheduler().shutdown(false);
    }

    /**
     * Starts timed updates of {@link HistoryAnalyserJob}
     * 
     * @throws SchedulerException
     */
    private static void startTimedJobs() throws SchedulerException {
        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
        Scheduler sched = schedFact.getScheduler();
        sched.start();

        Set<Class<? extends IGoobiJob>> jobs = new Reflections().getSubTypesOf(IGoobiJob.class);
        for (Class<? extends IGoobiJob> jobClass : jobs) {
            if (!Modifier.isAbstract(jobClass.getModifiers())) {
                try {
                    IGoobiJob job = jobClass.getDeclaredConstructor().newInstance();
                    initializeJob(job, job.getJobName(), sched);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    log.warn(jobClass.getName() + " cannot be instantiated");
                }
            }
        }
    }

    /**
     * initializes given SimpleGoobiJob at given time
     * 
     * @throws SchedulerException
     */
    private static void initializeJob(IGoobiJob goobiJob, String configuredStartTimeProperty, Scheduler sched) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(goobiJob.getClass()).withIdentity(goobiJob.getJobName(), goobiJob.getJobName()).build();
        String cronTimer = ConfigurationHelper.getInstance().getJobStartTime(configuredStartTimeProperty);
        if (StringUtils.isNotBlank(cronTimer)) {
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(goobiJob.getJobName(), goobiJob.getJobName())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronTimer))
                    .build();
            sched.scheduleJob(jobDetail, trigger);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        log.debug("Stop daily JobManager scheduler");
        try {
            stopTimedJobs();
        } catch (SchedulerException e) {
            log.error("Daily JobManager could not be stopped", e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        log.debug("Start daily JobManager scheduler");
        try {
            startTimedJobs();
        } catch (SchedulerException e) {
            log.error("daily JobManager could not be started", e);
        }
    }
}
