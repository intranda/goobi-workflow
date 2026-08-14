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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.goobi.api.mq.QueueType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.managedbeans.JobTypesCache;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class ScriptThreadWithoutHibernateTest extends AbstractTest {

    private static final int TIMEOUT_IN_SECONDS = 30;

    @Test
    public void testStepWithoutQueueRunsOnTheAutomaticStepExecutor() throws InterruptedException {
        List<String> threadNames = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch executed = new CountDownLatch(1);

        Process process = Mockito.mock(Process.class);
        Mockito.when(process.isPauseAutomaticExecution()).thenReturn(false);

        Step step = Mockito.mock(Step.class);
        Mockito.when(step.getTitel()).thenReturn("automatic step");
        Mockito.when(step.getProzess()).thenReturn(process);
        Mockito.when(step.getMessageQueue()).thenReturn(QueueType.NONE);
        Mockito.when(step.isTypScriptStep()).thenAnswer(invocation -> {
            threadNames.add(Thread.currentThread().getName());
            executed.countDown();
            return false;
        });

        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            JobTypesCache jobTypesCache = Mockito.mock(JobTypesCache.class);
            Mockito.when(jobTypesCache.isStepPaused(Mockito.anyString())).thenReturn(false);
            mockedHelper.when(() -> Helper.getBeanByClass(JobTypesCache.class)).thenReturn(jobTypesCache);

            new ScriptThreadWithoutHibernate(step).startOrPutToQueue();

            assertTrue(executed.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        }
        assertEquals(1, threadNames.size());
        assertTrue(threadNames.get(0).startsWith("automaticStep-"), "the step ran on thread " + threadNames.get(0));
    }
}
