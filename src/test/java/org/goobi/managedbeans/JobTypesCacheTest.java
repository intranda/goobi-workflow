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
 */
package org.goobi.managedbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.JobType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.persistence.managers.StepManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class JobTypesCacheTest {

    private JobTypesCache cache;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getExternalQueueJobTypes()).andReturn(new ArrayList<>()).anyTimes();
        StepManager.saveExternalQueueJobTypes(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(StepManager.class);

        cache = new JobTypesCache();
        cache.init();
    }

    @Test
    public void testInitialJobTypesIsEmpty() {
        assertNotNull(cache.getJobTypes());
        assertTrue(cache.getJobTypes().isEmpty());
    }

    @Test
    public void testIsStepPausedReturnsFalseForUnknownStep() {
        assertFalse(cache.isStepPaused("unknownStep"));
    }

    @Test
    public void testApplyAndPersistWithPausedJobTypePausesItsSteps() throws Exception {
        JobType jt = new JobType();
        jt.setName("type1");
        jt.setPaused(true);
        jt.getStepNames().add("step1");
        jt.getStepNames().add("step2");

        cache.applyAndPersist(Arrays.asList(jt));

        assertTrue(cache.isStepPaused("step1"));
        assertTrue(cache.isStepPaused("step2"));
    }

    @Test
    public void testApplyAndPersistWithUnpausedJobTypeDoesNotPauseSteps() throws Exception {
        JobType jt = new JobType();
        jt.setName("type1");
        jt.setPaused(false);
        jt.getStepNames().add("step1");

        cache.applyAndPersist(Arrays.asList(jt));

        assertFalse(cache.isStepPaused("step1"));
    }

    @Test
    public void testApplyAndPersistUpdatesJobTypesList() throws Exception {
        JobType jt = new JobType();
        jt.setName("type1");
        jt.setPaused(false);

        cache.applyAndPersist(Arrays.asList(jt));

        assertEquals(1, cache.getJobTypes().size());
        assertEquals("type1", cache.getJobTypes().get(0).getName());
    }

    @Test
    public void testApplyAndPersistReturnsRestartedSteps() throws Exception {
        // First: pause a step
        JobType jt = new JobType();
        jt.setName("type1");
        jt.setPaused(true);
        jt.getStepNames().add("step1");
        cache.applyAndPersist(Arrays.asList(jt));

        // Second: unpause — step1 should appear in restarted list
        JobType jt2 = new JobType();
        jt2.setName("type1");
        jt2.setPaused(false);
        jt2.getStepNames().add("step1");
        List<String> restarted = cache.applyAndPersist(Arrays.asList(jt2));

        assertTrue(restarted.contains("step1"));
        assertFalse(cache.isStepPaused("step1"));
    }

    @Test
    public void testApplyAndPersistReturnsEmptyListWhenNoStepsRestarted() throws Exception {
        JobType jt = new JobType();
        jt.setName("type1");
        jt.setPaused(false);
        jt.getStepNames().add("step1");

        List<String> restarted = cache.applyAndPersist(Arrays.asList(jt));
        assertTrue(restarted.isEmpty());
    }

    @Test
    public void testStepFromUnpausedJobTypeIsNotConsideredPaused() throws Exception {
        JobType pausedType = new JobType();
        pausedType.setName("paused");
        pausedType.setPaused(true);
        pausedType.getStepNames().add("pausedStep");

        JobType activeType = new JobType();
        activeType.setName("active");
        activeType.setPaused(false);
        activeType.getStepNames().add("activeStep");

        cache.applyAndPersist(Arrays.asList(pausedType, activeType));

        assertTrue(cache.isStepPaused("pausedStep"));
        assertFalse(cache.isStepPaused("activeStep"));
    }
}
