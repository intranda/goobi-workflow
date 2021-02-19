package org.goobi.production.flow.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.log4j.Log4j2;

/**
 * SimpleGoobiJob as basis class for all big jobs
 * 
 * @author Steffen Hankiewicz
 * @version 21.10.2009
 */
@Log4j2
public abstract class AbstractGoobiJob implements Job, IGoobiJob {
    private static Boolean isRunning = false;

    protected AbstractGoobiJob() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#execute(org.quartz.
     * JobExecutionContext)
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (getIsRunning() == false) {
            log.trace("Start scheduled Job: " + getJobName());
            if (isRunning == false) {
                log.trace("start history updating for all processes");
                setIsRunning(true);
                execute();
                setIsRunning(false);
            }
            log.trace("End scheduled Job: " + getJobName());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.jobs.IGoobiJob#setIsRunning(java.lang.Boolean)
     */
    public void setIsRunning(Boolean inisRunning) {
        isRunning = inisRunning;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#getIsRunning()
     */
    public Boolean getIsRunning() {
        return isRunning;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#getJobName()
     */
    public String getJobName() {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#execute()
     */
    public void execute() {
    }

}
