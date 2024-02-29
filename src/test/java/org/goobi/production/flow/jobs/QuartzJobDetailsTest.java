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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Test;
import org.quartz.JobKey;

import de.sub.goobi.AbstractTest;

public class QuartzJobDetailsTest extends AbstractTest {

    @Test
    public void testConstructor() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNotNull(fixture);
    }

    @Test
    public void testJobName() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getJobName());
        fixture.setJobName("fixture");
        assertEquals("fixture", fixture.getJobName());
    }

    @Test
    public void testJobGroup() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getJobGroup());
        fixture.setJobGroup("fixture");
        assertEquals("fixture", fixture.getJobGroup());
    }

    @Test
    public void testJobKey() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getJobKey());
        JobKey jobKey = new JobKey("key");
        fixture.setJobKey(jobKey);
        assertEquals(jobKey, fixture.getJobKey());
    }

    @Test
    public void testPreviousFireTime() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getPreviousFireTime());
        Date date = new Date();
        fixture.setPreviousFireTime(date);
        assertEquals(date, fixture.getPreviousFireTime());
    }

    @Test
    public void testNextFireTime() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getNextFireTime());
        Date date = new Date();
        fixture.setNextFireTime(date);
        assertEquals(date, fixture.getNextFireTime());
    }

    @Test
    public void testPaused() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertFalse(fixture.isPaused());
        fixture.setPaused(true);
        assertTrue(fixture.isPaused());
        fixture.setPaused(false);
        assertFalse(fixture.isPaused());
    }

    @Test
    public void testJob() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getJob());
        IGoobiJob job = EasyMock.createMock(IGoobiJob.class);
        fixture.setJob(job);
        assertNotNull(fixture.getJob());
    }

    @Test
    public void testCronExpression() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        assertNull(fixture.getCronExpression());
        fixture.setCronExpression("fixture");
        assertEquals("fixture", fixture.getCronExpression());
    }

    @Test
    public void testHumanReadableCronTime() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        fixture.setCronExpression(null);
        assertEquals("", fixture.getHumanReadableCronTime());
        fixture.setCronExpression("");
        assertEquals("", fixture.getHumanReadableCronTime());

        fixture.setCronExpression("0 0 0 * * ?");
        assertTrue(fixture.getHumanReadableCronTime().contains("0:00"));
    }

    @Test
    public void testCompareTo() throws Exception {
        QuartzJobDetails fixture = new QuartzJobDetails();
        fixture.setJobName("");
        QuartzJobDetails other = new QuartzJobDetails();
        other.setJobName("");
        assertEquals(0, fixture.compareTo(other));

        fixture.setJobName("a");
        other.setJobName("b");
        assertEquals(-1, fixture.compareTo(other));

        fixture.setJobName("b");
        other.setJobName("a");
        assertEquals(1, fixture.compareTo(other));
    }

    //
    //    @Override
    //    public int compareTo(QuartzJobDetails o) {
    //        return jobName.compareTo(o.getJobName());
    //    }

}
