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
package org.goobi.production.flow.statistics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StepInformationTest {

    @Test
    public void testDefaultConstructorTitle() {
        StepInformation info = new StepInformation();
        assertEquals("", info.getTitle());
    }

    @Test
    public void testDefaultConstructorAverageStepOrder() {
        StepInformation info = new StepInformation();
        assertEquals(Double.valueOf(0), info.getAverageStepOrder());
    }

    @Test
    public void testDefaultConstructorCounts() {
        StepInformation info = new StepInformation();
        assertEquals(0, info.getNumberOfTotalSteps());
        assertEquals(0, info.getNumberOfTotalImages());
        assertEquals(0, info.getTotalProcessCount());
        assertEquals(0, info.getNumberOfStepsDone());
        assertEquals(0, info.getNumberOfImagesDone());
        assertEquals(0, info.getProcessCountDone());
    }

    @Test
    public void testTitleConstructor() {
        StepInformation info = new StepInformation("Scanning");
        assertEquals("Scanning", info.getTitle());
        assertEquals(Double.valueOf(0), info.getAverageStepOrder());
    }

    @Test
    public void testTitleAndOrderConstructor() {
        StepInformation info = new StepInformation("QA", 3.5);
        assertEquals("QA", info.getTitle());
        assertEquals(Double.valueOf(3.5), info.getAverageStepOrder());
    }

    @Test
    public void testSetTitle() {
        StepInformation info = new StepInformation();
        info.setTitle("Export");
        assertEquals("Export", info.getTitle());
    }

    @Test
    public void testSetAverageStepOrder() {
        StepInformation info = new StepInformation();
        info.setAverageStepOrder(7.2);
        assertEquals(Double.valueOf(7.2), info.getAverageStepOrder());
    }

    @Test
    public void testSetNumberOfTotalSteps() {
        StepInformation info = new StepInformation();
        info.setNumberOfTotalSteps(42);
        assertEquals(42, info.getNumberOfTotalSteps());
    }

    @Test
    public void testSetNumberOfTotalImages() {
        StepInformation info = new StepInformation();
        info.setNumberOfTotalImages(1000);
        assertEquals(1000, info.getNumberOfTotalImages());
    }

    @Test
    public void testSetTotalProcessCount() {
        StepInformation info = new StepInformation();
        info.setTotalProcessCount(50);
        assertEquals(50, info.getTotalProcessCount());
    }

    @Test
    public void testSetNumberOfStepsDone() {
        StepInformation info = new StepInformation();
        info.setNumberOfStepsDone(10);
        assertEquals(10, info.getNumberOfStepsDone());
    }

    @Test
    public void testSetNumberOfImagesDone() {
        StepInformation info = new StepInformation();
        info.setNumberOfImagesDone(500);
        assertEquals(500, info.getNumberOfImagesDone());
    }

    @Test
    public void testSetProcessCountDone() {
        StepInformation info = new StepInformation();
        info.setProcessCountDone(25);
        assertEquals(25, info.getProcessCountDone());
    }
}
