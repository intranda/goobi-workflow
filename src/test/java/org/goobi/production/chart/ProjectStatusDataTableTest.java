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
package org.goobi.production.chart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProjectStatusDataTableTest {

    private static final Date BEGIN = new Date(0);
    private static final Date END = new Date(1_000_000_000L);

    private ProjectStatusDataTable table;

    @BeforeEach
    public void setUp() {
        table = new ProjectStatusDataTable("TestProject", BEGIN, END);
    }

    @Test
    public void testGetName() {
        assertEquals("TestProject", table.getName());
    }

    @Test
    public void testSetName() {
        table.setName("NewName");
        assertEquals("NewName", table.getName());
    }

    @Test
    public void testGetProjectBegin() {
        assertEquals(BEGIN, table.getProjectBegin());
    }

    @Test
    public void testGetProjectEnd() {
        assertEquals(END, table.getProjectEnd());
    }

    @Test
    public void testInitiallyHasNoTasks() {
        assertEquals(0, table.getNumberOfTasks());
    }

    @Test
    public void testGetTasksIsNotNull() {
        assertNotNull(table.getTasks());
    }

    @Test
    public void testAddTaskIncreasesCount() {
        table.addTask(new ProjectTask("Step1", 2, 5, 5));
        assertEquals(1, table.getNumberOfTasks());
    }

    @Test
    public void testAddTaskWithSameTitleUpdatesValues() {
        table.addTask(new ProjectTask("Step1", 2, 5, 5));
        table.addTask(new ProjectTask("Step1", 4, 8, 5));
        // Duplicate title: count stays 1, but values are updated
        assertEquals(1, table.getNumberOfTasks());
        assertEquals(Integer.valueOf(4), table.getTasks().get(0).getStepsCompleted());
        assertEquals(Integer.valueOf(8), table.getTasks().get(0).getStepsMax());
    }

    @Test
    public void testAddMultipleDistinctTasks() {
        table.addTask(new ProjectTask("A", 1, 5, 5));
        table.addTask(new ProjectTask("B", 2, 5, 5));
        table.addTask(new ProjectTask("C", 3, 5, 5));
        assertEquals(3, table.getNumberOfTasks());
    }

    @Test
    public void testRemoveExistingTask() {
        table.addTask(new ProjectTask("Step1", 1, 5, 5));
        table.addTask(new ProjectTask("Step2", 2, 5, 5));
        table.removeTask("Step1");
        assertEquals(1, table.getNumberOfTasks());
        assertEquals("Step2", table.getTasks().get(0).getTitle());
    }

    @Test
    public void testRemoveNonExistentTaskIsNoop() {
        table.addTask(new ProjectTask("Step1", 1, 5, 5));
        table.removeTask("NotHere");
        assertEquals(1, table.getNumberOfTasks());
    }

    @Test
    public void testGetTaskIndexForExistingTask() {
        table.addTask(new ProjectTask("A", 1, 5, 5));
        table.addTask(new ProjectTask("B", 2, 5, 5));
        assertEquals(0, table.getTaskIndex("A"));
        assertEquals(1, table.getTaskIndex("B"));
    }

    @Test
    public void testGetTaskIndexForMissingTaskReturnsMinusOne() {
        table.addTask(new ProjectTask("A", 1, 5, 5));
        assertEquals(-1, table.getTaskIndex("NotHere"));
    }
}
