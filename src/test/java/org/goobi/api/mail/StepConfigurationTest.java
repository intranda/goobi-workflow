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
package org.goobi.api.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StepConfigurationTest {

    @Test
    public void testDefaultValues() {
        StepConfiguration sc = new StepConfiguration();
        assertNotNull(sc);
        assertNull(sc.getId());
        assertNull(sc.getProjectId());
        assertNull(sc.getStepName());
        assertFalse(sc.isOpen());
        assertFalse(sc.isInWork());
        assertFalse(sc.isDone());
        assertFalse(sc.isError());
    }

    @Test
    public void testSettersAndGetters() {
        StepConfiguration sc = new StepConfiguration();
        sc.setId(42);
        sc.setProjectId(7);
        sc.setStepName("Scanning");
        sc.setOpen(true);
        sc.setInWork(true);
        sc.setDone(true);
        sc.setError(true);

        assertEquals(Integer.valueOf(42), sc.getId());
        assertEquals(Integer.valueOf(7), sc.getProjectId());
        assertEquals("Scanning", sc.getStepName());
        assertTrue(sc.isOpen());
        assertTrue(sc.isInWork());
        assertTrue(sc.isDone());
        assertTrue(sc.isError());
    }

    @Test
    public void testEquality() {
        StepConfiguration sc1 = new StepConfiguration();
        sc1.setId(1);
        sc1.setStepName("OCR");

        StepConfiguration sc2 = new StepConfiguration();
        sc2.setId(1);
        sc2.setStepName("OCR");

        assertEquals(sc1, sc2);
    }
}
