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

import org.goobi.production.flow.jobs.BackgroundJob.JobStatus;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import de.sub.goobi.persistence.managers.BackgroundJobManager;
import lombok.extern.log4j.Log4j2;

/**
 * SimpleGoobiJob as basis class for all big jobs.
 * 
 * @author Steffen Hankiewicz
 * @version 21.10.2009
 */
@Log4j2
public abstract class AbstractGoobiJob implements Job, IGoobiJob {

    private static boolean running = false;

    protected AbstractGoobiJob() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.IGoobiJob#execute(org.quartz.
     * JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!isRunning()) {
            log.trace("Start scheduled Job: " + getJobName());
            if (!running) {
                setRunning(true);
                BackgroundJob details = new BackgroundJob();
                details.setJobName(getJobName());
                details.setJobType("quartz");
                try {
                    execute();
                    details.setJobStatus(JobStatus.FINISH);
                    BackgroundJobManager.saveBackgroundJob(details);
                    setRunning(false);
                    //CHECKSTYLE:OFF
                    // generic exception is thrown
                } catch (Exception e) {
                    //CHECKSTYLE:ON
                    details.setJobStatus(JobStatus.ERROR);
                    BackgroundJobManager.saveBackgroundJob(details);
                    setRunning(false);
                    log.error(e);
                    JobExecutionException jee = new JobExecutionException(e);
                    jee.setRefireImmediately(false);
                    jee.setUnscheduleFiringTrigger(true);
                    throw jee;
                }
            }
            log.trace("End scheduled Job: " + getJobName());
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void setRunning(boolean inisRunning) {
        running = inisRunning;
    }
}
