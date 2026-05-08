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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.persistence.managers.StepManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class JobTypesCacheTest {

    private JobTypesCache cache;

    @BeforeEach
    public void setUp() throws Exception {
        cache = new JobTypesCache();
    }

    @Test
    public void testInitialJobTypesIsEmpty() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());
            cache.init();

            assertNotNull(cache.getJobTypes());
            assertTrue(cache.getJobTypes().isEmpty());
    
        }
}

    @Test
    public void testIsStepPausedReturnsFalseForUnknownStep() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());
            cache.init();

            assertFalse(cache.isStepPaused("unknownStep"));
    
        }
}

    @Test
    public void testApplyAndPersistWithPausedJobTypePausesItsSteps() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


            JobType jt = new JobType();
            jt.setName("type1");
            jt.setPaused(true);
            jt.getStepNames().add("step1");
            jt.getStepNames().add("step2");

            cache.applyAndPersist(Arrays.asList(jt));

            assertTrue(cache.isStepPaused("step1"));
            assertTrue(cache.isStepPaused("step2"));
    
        }
}

    @Test
    public void testApplyAndPersistWithUnpausedJobTypeDoesNotPauseSteps() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


            JobType jt = new JobType();
            jt.setName("type1");
            jt.setPaused(false);
            jt.getStepNames().add("step1");

            cache.applyAndPersist(Arrays.asList(jt));

            assertFalse(cache.isStepPaused("step1"));
    
        }
}

    @Test
    public void testApplyAndPersistUpdatesJobTypesList() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


            JobType jt = new JobType();
            jt.setName("type1");
            jt.setPaused(false);

            cache.applyAndPersist(Arrays.asList(jt));

            assertEquals(1, cache.getJobTypes().size());
            assertEquals("type1", cache.getJobTypes().get(0).getName());
    
        }
}

    @Test
    public void testApplyAndPersistReturnsRestartedSteps() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


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
}

    @Test
    public void testApplyAndPersistReturnsEmptyListWhenNoStepsRestarted() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


            JobType jt = new JobType();
            jt.setName("type1");
            jt.setPaused(false);
            jt.getStepNames().add("step1");

            List<String> restarted = cache.applyAndPersist(Arrays.asList(jt));
            assertTrue(restarted.isEmpty());
    
        }
}

    @Test
    public void testStepFromUnpausedJobTypeIsNotConsideredPaused() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedStepManager.when(() -> StepManager.getExternalQueueJobTypes()).thenReturn(new ArrayList<>());


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
}
