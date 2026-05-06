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
package org.goobi.production.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class StepReturnValueTest {

    @Test
    public void testValuesCount() {
        assertEquals(5, StepReturnValue.values().length);
    }

    @Test
    public void testFinishedId() {
        assertEquals(0, StepReturnValue.Finished.getId());
    }

    @Test
    public void testFinishedValue() {
        assertEquals("Step finished", StepReturnValue.Finished.getValue());
    }

    @Test
    public void testWriteErrorId() {
        assertEquals(4, StepReturnValue.WriteError.getId());
    }

    @Test
    public void testWriteErrorValue() {
        assertEquals("Data could not be written", StepReturnValue.WriteError.getValue());
    }

    @Test
    public void testGetByIdFinished() {
        assertEquals(StepReturnValue.Finished, StepReturnValue.getById(0));
    }

    @Test
    public void testGetByIdInvalidData() {
        assertEquals(StepReturnValue.InvalidData, StepReturnValue.getById(1));
    }

    @Test
    public void testGetByIdNoData() {
        assertEquals(StepReturnValue.NoData, StepReturnValue.getById(2));
    }

    @Test
    public void testGetByIdDataAlreadyExists() {
        assertEquals(StepReturnValue.DataAllreadyExists, StepReturnValue.getById(3));
    }

    @Test
    public void testGetByIdWriteError() {
        assertEquals(StepReturnValue.WriteError, StepReturnValue.getById(4));
    }

    @Test
    public void testGetByIdUnknownReturnsNull() {
        assertNull(StepReturnValue.getById(99));
    }

    @Test
    public void testGetByValueFinished() {
        assertEquals(StepReturnValue.Finished, StepReturnValue.getByValue("Step finished"));
    }

    @Test
    public void testGetByValueWriteError() {
        assertEquals(StepReturnValue.WriteError, StepReturnValue.getByValue("Data could not be written"));
    }

    @Test
    public void testGetByValueUnknownReturnsNull() {
        assertNull(StepReturnValue.getByValue("unknown"));
    }
}
