package org.goobi.production.flow.jobs;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
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
            try {
                IGoobiJob job = jobClass.getDeclaredConstructor().newInstance();
                initializeJob(job, job.getJobName(), sched);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                log.error(e);
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
