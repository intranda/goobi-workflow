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
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParametersDataTest {

    @Test
    public void testDefaultFlagCriticalQueryIsFalse() {
        ParametersData data = new ParametersData();
        assertFalse(data.getFlagCriticalQuery());
    }

    @Test
    public void testDefaultExactStepDoneIsNull() {
        ParametersData data = new ParametersData();
        assertNull(data.getExactStepDone());
    }

    @Test
    public void testParameterizedConstructor() {
        ParametersData data = new ParametersData(true, 5);
        assertTrue(data.getFlagCriticalQuery());
        assertEquals(Integer.valueOf(5), data.getExactStepDone());
    }

    @Test
    public void testParameterizedConstructorWithFalse() {
        ParametersData data = new ParametersData(false, null);
        assertFalse(data.getFlagCriticalQuery());
        assertNull(data.getExactStepDone());
    }

    @Test
    public void testSetCriticalQuerySetsFlag() {
        ParametersData data = new ParametersData();
        data.setCriticalQuery();
        assertTrue(data.getFlagCriticalQuery());
    }

    @Test
    public void testSetCriticalQueryIsIdempotent() {
        ParametersData data = new ParametersData();
        data.setCriticalQuery();
        data.setCriticalQuery();
        assertTrue(data.getFlagCriticalQuery());
    }

    @Test
    public void testSetStepDone() {
        ParametersData data = new ParametersData();
        data.setStepDone(3);
        assertEquals(Integer.valueOf(3), data.getExactStepDone());
    }

    @Test
    public void testSetStepDoneOverwritesPrevious() {
        ParametersData data = new ParametersData(false, 1);
        data.setStepDone(10);
        assertEquals(Integer.valueOf(10), data.getExactStepDone());
    }
}
