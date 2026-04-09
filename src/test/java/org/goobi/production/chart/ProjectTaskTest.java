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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProjectTaskTest {

    @Test
    public void testGetTitle() {
        ProjectTask task = new ProjectTask("MyTask", 5, 10, 10);
        assertEquals("MyTask", task.getTitle());
    }

    @Test
    public void testGetStepsCompleted() {
        ProjectTask task = new ProjectTask("T", 3, 10, 10);
        assertEquals(Integer.valueOf(3), task.getStepsCompleted());
    }

    @Test
    public void testGetStepsMax() {
        ProjectTask task = new ProjectTask("T", 3, 10, 10);
        assertEquals(Integer.valueOf(10), task.getStepsMax());
    }

    @Test
    public void testGetConfiguredMax() {
        ProjectTask task = new ProjectTask("T", 3, 10, 7);
        assertEquals(Integer.valueOf(7), task.getConfiguredMax());
    }

    @Test
    public void testSetStepsCompleted() {
        ProjectTask task = new ProjectTask("T", 3, 10, 10);
        task.setStepsCompleted(8);
        assertEquals(Integer.valueOf(8), task.getStepsCompleted());
    }

    @Test
    public void testSetStepsMax() {
        ProjectTask task = new ProjectTask("T", 3, 10, 10);
        task.setStepsMax(20);
        assertEquals(Integer.valueOf(20), task.getStepsMax());
    }

    @Test
    public void testCheckSizesAdjustsMaxWhenCompletedExceedsMax() {
        // stepsCompleted (15) > stepsMax (10) → stepsMax is raised to stepsCompleted
        ProjectTask task = new ProjectTask("T", 15, 10, 10);
        assertEquals(Integer.valueOf(15), task.getStepsMax());
        assertEquals(Integer.valueOf(15), task.getStepsCompleted());
    }

    @Test
    public void testCheckSizesDoesNotAdjustWhenCompletedEqualsMax() {
        ProjectTask task = new ProjectTask("T", 10, 10, 10);
        assertEquals(Integer.valueOf(10), task.getStepsMax());
    }

    @Test
    public void testCheckSizesDoesNotAdjustWhenCompletedLessThanMax() {
        ProjectTask task = new ProjectTask("T", 5, 10, 10);
        assertEquals(Integer.valueOf(10), task.getStepsMax());
    }
}
