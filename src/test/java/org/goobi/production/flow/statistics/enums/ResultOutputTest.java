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

public class ResultOutputTest {

    @Test
    public void testChartId() {
        assertEquals("1", ResultOutput.chart.getId());
    }

    @Test
    public void testTableId() {
        assertEquals("2", ResultOutput.table.getId());
    }

    @Test
    public void testChartAndTableId() {
        assertEquals("3", ResultOutput.chartAndTable.getId());
    }

    @Test
    public void testGetByIdChart() {
        assertEquals(ResultOutput.chart, ResultOutput.getById("1"));
    }

    @Test
    public void testGetByIdTable() {
        assertEquals(ResultOutput.table, ResultOutput.getById("2"));
    }

    @Test
    public void testGetByIdChartAndTable() {
        assertEquals(ResultOutput.chartAndTable, ResultOutput.getById("3"));
    }

    @Test
    public void testGetByIdUnknownReturnsTable() {
        assertEquals(ResultOutput.table, ResultOutput.getById("unknown"));
    }

    @Test
    public void testValuesHasThreeEntries() {
        assertEquals(3, ResultOutput.values().length);
    }

    @Test
    public void testGetTitleReturnsNonNull() {
        assertNotNull(ResultOutput.chart.getTitle());
        assertNotNull(ResultOutput.table.getTitle());
        assertNotNull(ResultOutput.chartAndTable.getTitle());
    }
}
