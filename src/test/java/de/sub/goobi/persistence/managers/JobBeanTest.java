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
 */

package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.goobi.managedbeans.JobBean;
import org.goobi.production.flow.jobs.BackgroundJob;
import org.goobi.production.flow.jobs.QuartzJobDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.JobKey;

import de.sub.goobi.helper.Helper;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class JobBeanTest {

    private List<BackgroundJob> jobs;

    @BeforeEach
    public void setUp() throws Exception {



        BackgroundJob job = Mockito.mock(BackgroundJob.class);
        jobs = new ArrayList<>();
        jobs.add(job);

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            JobBean fixture = new JobBean();
            assertNotNull(fixture);
    
        }
}

    @Test
    public void testInit() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            JobBean fixture = new JobBean();
            assertNotNull(fixture);
            assertNull(fixture.getPaginator());
            fixture.init();
            assertEquals(1, fixture.getPaginator().getTotalResults());

    
        }
}

    @Test
    public void testPauseAndResumeAllJobs() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            JobBean fixture = new JobBean();
            fixture.init();
            assertFalse(fixture.isPaused());
            fixture.pauseAllJobs();
            assertTrue(fixture.isPaused());
            fixture.resumeAllJobs();
            assertFalse(fixture.isPaused());
    
        }
}

    @Test
    public void testPauseAndResumeJob() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            QuartzJobDetails details = new QuartzJobDetails();
            JobKey jobKey = new JobKey("key");
            details.setJobKey(jobKey);
            JobBean fixture = new JobBean();
            fixture.init();
            fixture.setQuartzJobDetails(details);
            assertFalse(details.isPaused());
            fixture.pauseJob();
            assertTrue(details.isPaused());
            fixture.resumeJob();
            assertFalse(details.isPaused());

    
        }
}

    @Test
    public void testTriggerQuartzJob() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            QuartzJobDetails details = new QuartzJobDetails();
            JobKey jobKey = new JobKey("key");
            details.setJobKey(jobKey);
            JobBean fixture = new JobBean();
            fixture.init();
            fixture.setQuartzJobDetails(details);
            fixture.triggerQuartzJob();
            assertEquals("key", fixture.getQuartzJobDetails().getJobKey().getName());

    
        }
}

    @Test
    public void testGetActiveJobs() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.nullable(String.class))).thenReturn(1);
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
                        mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(jobs);


            JobBean fixture = new JobBean();
            assertEquals(0, fixture.getActiveJobs().size());
    
        }
}
}
