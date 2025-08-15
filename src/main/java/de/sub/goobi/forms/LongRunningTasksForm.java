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
package de.sub.goobi.forms;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.goobi.beans.Process;

import de.sub.goobi.helper.tasks.LongRunningTask;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LongRunningTasksForm {
    @Getter
    @Setter
    private Process prozess;
    @Getter
    @Setter
    private LongRunningTask task;

    public List<LongRunningTask> getTasks() {
        return LongRunningTaskManager.getInstance().getTasks();
    }

    public void addNewMasterTask() {
        Process p = new Process();
        p.setTitel("hallo Titel " + System.currentTimeMillis());
        this.task = new LongRunningTask();
        this.task.initialize(p);
        LongRunningTaskManager.getInstance().addTask(this.task);
    }

    /**
     * Thread entweder starten oder restarten. ================================================================
     */
    public void executeTask() {
        if (this.task.getStatusProgress() == 0) {
            LongRunningTaskManager.getInstance().executeTask(this.task);
        } else {
            /* Thread lief schon und wurde abgebrochen */
            try {
                LongRunningTask lrt = this.task.getClass().getDeclaredConstructor().newInstance();
                lrt.initialize(this.task.getProzess());
                LongRunningTaskManager.getInstance().replaceTask(this.task, lrt);
                LongRunningTaskManager.getInstance().executeTask(lrt);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                log.error(e);
            }
        }
    }

    public void clearFinishedTasks() {
        LongRunningTaskManager.getInstance().clearFinishedTasks();
    }

    public void clearAllTasks() {
        LongRunningTaskManager.getInstance().clearAllTasks();
    }

    public void moveTaskUp() {
        LongRunningTaskManager.getInstance().moveTaskUp(this.task);
    }

    public void moveTaskDown() {
        LongRunningTaskManager.getInstance().moveTaskDown(this.task);
    }

    public void cancelTask() {
        LongRunningTaskManager.getInstance().cancelTask(this.task);
    }

    public void removeTask() {
        LongRunningTaskManager.getInstance().removeTask(this.task);
    }

    public boolean isRunning() {
        return LongRunningTaskManager.isRunning();
    }

    public void toggleRunning() {
        LongRunningTaskManager.setRunning(!LongRunningTaskManager.isRunning());
    }
}