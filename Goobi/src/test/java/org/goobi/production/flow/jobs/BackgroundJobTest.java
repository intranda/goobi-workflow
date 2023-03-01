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
