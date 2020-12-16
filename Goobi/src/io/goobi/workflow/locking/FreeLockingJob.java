package io.goobi.workflow.locking;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class FreeLockingJob implements Job {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {

        LockingBean.removeOldSessions();

    }

}
