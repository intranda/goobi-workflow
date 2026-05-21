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

package org.goobi.production.flow.jobs.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.beans.Step;
import org.goobi.production.flow.helper.BatchDisplayItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayItemTest extends AbstractTest {

    private Step step;

    @BeforeEach
    public void setUp() {
        try {
            java.lang.reflect.Field f = de.sub.goobi.config.ConfigScripts.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        step = new Step();
        step.setId(1);
        step.setTitel("title");
        step.setReihenfolge(5);
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);

        step.setScriptname1("Script Alpha");
        step.setTypExportDMS(true);
    }

    @Test
    public void testConstructor() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
    }

    @Test
    public void testStepTitle() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertEquals("title", fixture.getStepTitle());
        fixture.setStepTitle("new title");
        assertEquals("new title", fixture.getStepTitle());
    }

    @Test
    public void testStepOrder() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertEquals(5, fixture.getStepOrder().intValue());
        fixture.setStepOrder(2);
        assertEquals(2, fixture.getStepOrder().intValue());
    }

    @Test
    public void testStepStatus() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertEquals(StepStatus.OPEN, fixture.getStepStatus());
        fixture.setStepStatus(StepStatus.DONE);
        assertEquals(StepStatus.DONE, fixture.getStepStatus());
    }

    @Test
    public void testExport() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertTrue(fixture.isExportDMS());
        fixture.setExportDMS(false);
        assertFalse(fixture.isExportDMS());
    }

    @Test
    public void testScripts() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertEquals(1, fixture.getScripts().size());
        assertEquals(1, fixture.getScriptSize());
    }

    @Test
    public void testScriptNames() {
        BatchDisplayItem fixture = new BatchDisplayItem(step);
        assertNotNull(fixture);
        assertEquals("Script Alpha", fixture.getScriptnames().get(0));
    }

    @Test
    public void testEqualsWithNullStepOrderDoesNotThrowNPE() {
        Step noScriptStep = new Step();
        noScriptStep.setTitel("title");
        noScriptStep.setBearbeitungsstatusEnum(StepStatus.OPEN);
        noScriptStep.setTypExportDMS(true);

        BatchDisplayItem item1 = new BatchDisplayItem(noScriptStep);
        item1.setStepOrder(null);
        BatchDisplayItem item2 = new BatchDisplayItem(noScriptStep);
        item2.setStepOrder(null);
        BatchDisplayItem item3 = new BatchDisplayItem(noScriptStep);
        item3.setStepOrder(5);

        assertEquals(item1, item1);
        assertEquals(item1, item2);
        assertNotEquals(item1, item3);
        assertNotEquals(item3, item1);
        assertNotEquals(item1, null);
    }

    @Test
    public void testHashCodeWithNullStepOrderDoesNotThrowNPE() {
        BatchDisplayItem item = new BatchDisplayItem(step);
        item.setStepOrder(null);
        assertEquals(item.hashCode(), item.hashCode());
    }
}
