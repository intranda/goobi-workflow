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
package de.sub.goobi.helper;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Runs the automatic steps of the workflow, limiting how many of them are running at the same time.
 *
 */
@Log4j2
public class AutomaticStepExecutor {

    private static final int IDLE_TIMEOUT_IN_SECONDS = 60;

    private static AutomaticStepExecutor instance;

    @Getter
    private final int maxParallelSteps;

    private final ThreadPoolExecutor executor;

    public AutomaticStepExecutor(int maxParallelSteps) {
        this.maxParallelSteps = maxParallelSteps;

        AtomicInteger threadNumber = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            // a telling name, the threads of an installation under load are read in thread dumps
            Thread thread = new Thread(runnable, "automaticStep-" + threadNumber.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };

        this.executor = new ThreadPoolExecutor(maxParallelSteps, maxParallelSteps, IDLE_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), threadFactory);
        this.executor.allowCoreThreadTimeOut(true);
    }

    /**
     * @return the executor used for all automatic steps of this installation, its size is configurable
     */
    public static synchronized AutomaticStepExecutor getInstance() {
        if (instance == null) {
            instance = new AutomaticStepExecutor(ConfigurationHelper.getInstance().getMaxParallelAutomaticSteps());
        }
        return instance;
    }

    /**
     * Runs the given step as soon as a slot is free. Returns immediately, the caller is not blocked.
     *
     * @param step the work to be done
     */
    public void execute(Runnable step) {
        try {
            executor.execute(() -> runAndLogErrors(step));
        } catch (RejectedExecutionException e) {
            log.error("An automatic step could not be started because Goobi is shutting down.", e);
        }
    }

    /**
     * Stops the executor. Steps that are already running are finished, steps still waiting in the queue are not started any more.
     */
    public void shutdown() {
        executor.shutdown();
    }

    private void runAndLogErrors(Runnable step) {
        try {
            step.run();
        } catch (RuntimeException e) { //NOSONAR, a single failing step must never take the executor down
            log.error("An exception occurred while running an automatic step.", e);
        }
    }
}
