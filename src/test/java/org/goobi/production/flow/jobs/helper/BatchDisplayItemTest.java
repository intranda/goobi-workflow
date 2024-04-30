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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.goobi.beans.Step;
import org.goobi.production.flow.helper.BatchDisplayItem;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.helper.enums.StepStatus;

public class BatchDisplayItemTest {

    private Step step;

    @Before
    public void setUp() {
        step = new Step();
        step.setId(1);
        step.setTitel("title");
        step.setReihenfolge(5);
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);

        step.setScriptname1("script");
        step.setTypAutomatischScriptpfad("/bin/false");
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
        fixture.setStepOrder(2);;
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
        assertEquals("script", fixture.getScriptnames().get(0));
    }
}
