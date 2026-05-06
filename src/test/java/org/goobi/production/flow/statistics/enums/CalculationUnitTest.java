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
package org.goobi.production.flow.statistics.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CalculationUnitTest {

    @Test
    public void testVolumesId() {
        assertEquals("1", CalculationUnit.volumes.getId());
    }

    @Test
    public void testPagesId() {
        assertEquals("2", CalculationUnit.pages.getId());
    }

    @Test
    public void testVolumesAndPagesId() {
        assertEquals("3", CalculationUnit.volumesAndPages.getId());
    }

    @Test
    public void testGetByIdVolumes() {
        assertEquals(CalculationUnit.volumes, CalculationUnit.getById("1"));
    }

    @Test
    public void testGetByIdPages() {
        assertEquals(CalculationUnit.pages, CalculationUnit.getById("2"));
    }

    @Test
    public void testGetByIdVolumesAndPages() {
        assertEquals(CalculationUnit.volumesAndPages, CalculationUnit.getById("3"));
    }

    @Test
    public void testGetByIdUnknownReturnsVolumes() {
        assertEquals(CalculationUnit.volumes, CalculationUnit.getById("unknown"));
    }

    @Test
    public void testValuesHasThreeEntries() {
        assertEquals(3, CalculationUnit.values().length);
    }

    @Test
    public void testGetTitleReturnsNonNull() {
        assertNotNull(CalculationUnit.volumes.getTitle());
        assertNotNull(CalculationUnit.pages.getTitle());
        assertNotNull(CalculationUnit.volumesAndPages.getTitle());
    }
}
