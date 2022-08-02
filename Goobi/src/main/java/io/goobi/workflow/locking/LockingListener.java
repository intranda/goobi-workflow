package io.goobi.workflow.locking;

import java.util.Date;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;

import lombok.extern.log4j.Log4j2;

@WebListener
@Log4j2
public class LockingListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        SchedulerFactory schedFact = new StdSchedulerFactory();
        Scheduler sched;
        try {
            sched = schedFact.getScheduler();
            sched.start();

            JobDetail jobDetail = new JobDetail("locking", null, FreeLockingJob.class);
            Trigger trigger = TriggerUtils.makeMinutelyTrigger(5); // TODO set to 30 after tests
            trigger.setStartTime(new Date());
            trigger.setName("MinuteTrigger");
            sched.scheduleJob(jobDetail, trigger);


        } catch (SchedulerException e) {
            log.error(e);

        }
    }

}
