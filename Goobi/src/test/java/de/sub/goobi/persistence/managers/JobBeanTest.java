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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.managedbeans.JobBean;
import org.goobi.production.flow.jobs.BackgroundJob;
import org.goobi.production.flow.jobs.QuartzJobDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.JobKey;

import de.sub.goobi.helper.Helper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BackgroundJobsMysqlHelper.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class JobBeanTest {

    @Before
    public void setUp() throws Exception {

        PowerMock.mockStatic(BackgroundJobsMysqlHelper.class);
        EasyMock.expect(BackgroundJobsMysqlHelper.getHitSize(EasyMock.anyString())).andReturn(1).anyTimes();

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        Helper.setMeldung(EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString());
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyObject(Exception.class));
        PowerMock.replay(Helper.class);

        BackgroundJob job = EasyMock.createMock(BackgroundJob.class);
        List<BackgroundJob> jobs = new ArrayList<>();
        jobs.add(job);

        EasyMock.expect(BackgroundJobsMysqlHelper.getList(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt()))
        .andReturn(jobs)
        .anyTimes();

        PowerMock.replay(BackgroundJobsMysqlHelper.class);
    }

    @Test
    public void testConstructor() {
        JobBean fixture = new JobBean();
        assertNotNull(fixture);
    }

    @Test
    public void testInit() throws Exception {
        JobBean fixture = new JobBean();
        assertNotNull(fixture);
        assertNull(fixture.getPaginator());
        fixture.init();
        assertEquals(1, fixture.getPaginator().getTotalResults());

    }

    @Test
    public void testPauseAndResumeAllJobs() throws Exception {
        JobBean fixture = new JobBean();
        fixture.init();
        assertFalse(fixture.isPaused());
        fixture.pauseAllJobs();
        assertTrue(fixture.isPaused());
        fixture.resumeAllJobs();
        assertFalse(fixture.isPaused());
    }

    @Test
    public void testPauseAndResumeJob() throws Exception {
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

    @Test
    public void testTriggerQuartzJob() throws Exception {
        QuartzJobDetails details = new QuartzJobDetails();
        JobKey jobKey = new JobKey("key");
        details.setJobKey(jobKey);
        JobBean fixture = new JobBean();
        fixture.init();
        fixture.setQuartzJobDetails(details);
        fixture.triggerQuartzJob();
        assertEquals("key", fixture.getQuartzJobDetails().getJobKey().getName());

    }

    @Test
    public void testGetActiveJobs() throws Exception {
        JobBean fixture = new JobBean();
        assertEquals(0, fixture.getActiveJobs().size());
    }
}
