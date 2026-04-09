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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ImportReturnValueTest {

    @Test
    public void testValuesCount() {
        assertEquals(5, ImportReturnValue.values().length);
    }

    @Test
    public void testExportFinishedId() {
        assertEquals(0, ImportReturnValue.ExportFinished.getId());
    }

    @Test
    public void testExportFinishedValue() {
        assertEquals("Export finished", ImportReturnValue.ExportFinished.getValue());
    }

    @Test
    public void testWriteErrorId() {
        assertEquals(4, ImportReturnValue.WriteError.getId());
    }

    @Test
    public void testGetByIdExportFinished() {
        assertEquals(ImportReturnValue.ExportFinished, ImportReturnValue.getById(0));
    }

    @Test
    public void testGetByIdInvalidData() {
        assertEquals(ImportReturnValue.InvalidData, ImportReturnValue.getById(1));
    }

    @Test
    public void testGetByIdNoData() {
        assertEquals(ImportReturnValue.NoData, ImportReturnValue.getById(2));
    }

    @Test
    public void testGetByIdDataAlreadyExists() {
        assertEquals(ImportReturnValue.DataAllreadyExists, ImportReturnValue.getById(3));
    }

    @Test
    public void testGetByIdWriteError() {
        assertEquals(ImportReturnValue.WriteError, ImportReturnValue.getById(4));
    }

    @Test
    public void testGetByIdUnknownReturnsNull() {
        assertNull(ImportReturnValue.getById(99));
    }

    @Test
    public void testGetByValueExportFinished() {
        assertEquals(ImportReturnValue.ExportFinished, ImportReturnValue.getByValue("Export finished"));
    }

    @Test
    public void testGetByValueWriteError() {
        assertEquals(ImportReturnValue.WriteError, ImportReturnValue.getByValue("Data could not be written"));
    }

    @Test
    public void testGetByValueUnknownReturnsNull() {
        assertNull(ImportReturnValue.getByValue("unknown"));
    }
}
