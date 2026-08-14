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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class AutomaticStepExecutorTest extends AbstractTest {

    private static final int TIMEOUT_IN_SECONDS = 30;

    @Test
    public void testTaskIsExecuted() throws InterruptedException {
        AutomaticStepExecutor executor = new AutomaticStepExecutor(2);
        CountDownLatch executed = new CountDownLatch(1);

        executor.execute(executed::countDown);

        assertTrue(executed.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        executor.shutdown();
    }

    @Test
    public void testNoMoreTasksRunAtTheSameTimeThanConfigured() throws InterruptedException {
        int maxParallelSteps = 2;
        int numberOfTasks = 20;
        AutomaticStepExecutor executor = new AutomaticStepExecutor(maxParallelSteps);
        AtomicInteger running = new AtomicInteger();
        AtomicInteger highWaterMark = new AtomicInteger();
        CountDownLatch finished = new CountDownLatch(numberOfTasks);

        for (int i = 0; i < numberOfTasks; i++) {
            executor.execute(() -> {
                highWaterMark.accumulateAndGet(running.incrementAndGet(), Math::max);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    running.decrementAndGet();
                    finished.countDown();
                }
            });
        }

        assertTrue(finished.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        assertTrue(highWaterMark.get() <= maxParallelSteps, "at most " + maxParallelSteps + " tasks may run at once, but " + highWaterMark.get()
                + " did");
        executor.shutdown();
    }

    @Test
    public void testAllTasksAreExecutedEvenWhenTheyDoNotFitIntoThePool() throws InterruptedException {
        int numberOfTasks = 20;
        AutomaticStepExecutor executor = new AutomaticStepExecutor(2);
        CountDownLatch finished = new CountDownLatch(numberOfTasks);
        List<Integer> executedTasks = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < numberOfTasks; i++) {
            final int task = i;
            executor.execute(() -> {
                executedTasks.add(task);
                finished.countDown();
            });
        }

        assertTrue(finished.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        assertEquals(numberOfTasks, executedTasks.size());
        executor.shutdown();
    }

    @Test
    public void testFailingTaskDoesNotStopTheExecutor() throws InterruptedException {
        AutomaticStepExecutor executor = new AutomaticStepExecutor(1);
        CountDownLatch finished = new CountDownLatch(1);

        executor.execute(() -> {
            throw new IllegalStateException("this step fails");
        });
        executor.execute(finished::countDown);

        assertTrue(finished.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        executor.shutdown();
    }

    @Test
    public void testThreadsAreNamedAndRunAsDaemons() throws InterruptedException {
        AutomaticStepExecutor executor = new AutomaticStepExecutor(1);
        CountDownLatch finished = new CountDownLatch(1);
        List<String> names = Collections.synchronizedList(new ArrayList<>());
        List<Boolean> daemons = Collections.synchronizedList(new ArrayList<>());

        executor.execute(() -> {
            names.add(Thread.currentThread().getName());
            daemons.add(Thread.currentThread().isDaemon());
            finished.countDown();
        });

        assertTrue(finished.await(TIMEOUT_IN_SECONDS, TimeUnit.SECONDS));
        assertTrue(names.get(0).startsWith("automaticStep-"), "unexpected thread name " + names.get(0));
        assertTrue(daemons.get(0));
        executor.shutdown();
    }

    @Test
    public void testTasksSubmittedAfterShutdownAreNotExecuted() throws InterruptedException {
        AutomaticStepExecutor executor = new AutomaticStepExecutor(1);
        executor.shutdown();
        CountDownLatch executed = new CountDownLatch(1);

        executor.execute(executed::countDown);

        assertEquals(1, executed.getCount());
    }

    @Test
    public void testDefaultExecutorUsesTheConfiguredNumberOfParallelSteps() {
        assertEquals(10, AutomaticStepExecutor.getInstance().getMaxParallelSteps());
    }
}
