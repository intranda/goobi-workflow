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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.goobi.production.flow.jobs.BackgroundJob.JobStatus;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class BackgroundJobTest extends AbstractTest {

    @Test
    public void testConstructor() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertNotNull(job);
    }

    @Test
    public void testId() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertNull(job.getId());
        job.setId(1);
        assertEquals(1, job.getId().intValue());
    }

    @Test
    public void testJobName() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertNull(job.getJobName());
        job.setJobName("fixture");
        assertEquals("fixture", job.getJobName());
    }

    @Test
    public void testJobType() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertNull(job.getJobType());
        job.setJobType("fixture");
        assertEquals("fixture", job.getJobType());
    }

    @Test
    public void testJobStatus() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertEquals(JobStatus.NEW, job.getJobStatus());
        job.setJobStatus(JobStatus.WAIT);
        assertEquals(JobStatus.WAIT, job.getJobStatus());
    }

    @Test
    public void testRetryCount() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertEquals(1, job.getRetryCount());
        job.setRetryCount(2);
        assertEquals(2, job.getRetryCount());
    }

    @Test
    public void testLastUpdateTime() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertNotNull(job.getLastUpdateTime());
        LocalDateTime other = LocalDateTime.now();
        job.setLastUpdateTime(other);
        assertEquals(other, job.getLastUpdateTime());
    }

    @Test
    public void testProperties() throws Exception {
        BackgroundJob job = new BackgroundJob();
        assertTrue(job.getProperties().isEmpty());
        BackgroundJobProperty prop = new BackgroundJobProperty();
        job.getProperties().add(prop);
        assertEquals(1, job.getProperties().size());
    }

    @Test
    public void testJobStatusEnum() throws Exception {
        JobStatus fixture = JobStatus.getById(0);
        assertNull(fixture);

        fixture = JobStatus.getById(1);
        assertEquals(1, fixture.getId());
        assertEquals("jobs_status_new", fixture.getLabel());

        fixture = JobStatus.getById(2);
        assertEquals(2, fixture.getId());
        assertEquals("jobs_status_wait", fixture.getLabel());

        fixture = JobStatus.getById(3);
        assertEquals(3, fixture.getId());
        assertEquals("jobs_status_processing", fixture.getLabel());

        fixture = JobStatus.getById(4);
        assertEquals(4, fixture.getId());
        assertEquals("jobs_status_finished", fixture.getLabel());

        fixture = JobStatus.getById(5);
        assertEquals(5, fixture.getId());
        assertEquals("jobs_status_error", fixture.getLabel());
    }


}
