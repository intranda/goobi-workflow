package de.sub.goobi.forms;

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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.goobi.beans.Process;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.tasks.LongRunningTask;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;

public class LongRunningTasksFormTest extends AbstractTest {

    @Test
    public void testConstructor() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);
    }

    @Test
    public void testAddNewMasterTask() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);
        lrtf.addNewMasterTask();
        assertNotNull(LongRunningTaskManager.getInstance().getTasks());
    }

    @Test
    public void testTask() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        assertEquals(task, lrtf.getTask());
    }

    @Test
    public void testExecuteTask() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.executeTask();

    }

    @Test
    public void testClearFinishedTasks() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.executeTask();
        lrtf.clearFinishedTasks();
    }

    @Test
    public void testClearAllTasks() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.executeTask();
        lrtf.clearAllTasks();
    }

    @Test
    public void testMoveTaskUp() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.addNewMasterTask();
        //        lrtf.executeTask();
        lrtf.moveTaskUp();
    }

    @Test
    public void testMoveTaskDown() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.addNewMasterTask();
        //        lrtf.executeTask();
        lrtf.moveTaskDown();
    }

    @Test
    public void testCancelTask() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.executeTask();
        lrtf.cancelTask();
    }

    @Test
    public void testRemoveTask() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);

        LongRunningTask task = new LongRunningTask();
        lrtf.setTask(task);
        lrtf.executeTask();
        lrtf.removeTask();
    }

    @Test
    public void testProcess() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);
        Process p = new Process();
        lrtf.setProzess(p);
        assertEquals(p, lrtf.getProzess());
    }

    //@Test
    public void testIsRunning() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);
        assertTrue(lrtf.isRunning());
    }

    @Test
    public void testToggleRunning() {
        LongRunningTasksForm lrtf = new LongRunningTasksForm();
        assertNotNull(lrtf);
        lrtf.toggleRunning();
    }
}
