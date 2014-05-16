package de.sub.goobi.forms;

import static org.junit.Assert.*;

import org.junit.Test;
import org.goobi.beans.Process;
import de.sub.goobi.helper.tasks.LongRunningTask;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;

public class LongRunningTasksFormTest {

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

    @Test
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
