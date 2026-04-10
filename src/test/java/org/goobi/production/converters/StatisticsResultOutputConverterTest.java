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
package org.goobi.production.converters;

import static org.junit.Assert.assertEquals;

import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.junit.Test;

public class StatisticsResultOutputConverterTest {

    private final StatisticsResultOutputConverter converter = new StatisticsResultOutputConverter();

    @Test
    public void testConverterIdConstant() {
        assertEquals("StatisticsResultOutputConverter", StatisticsResultOutputConverter.CONVERTER_ID);
    }

    @Test
    public void testGetAsObjectNullReturnsTable() {
        assertEquals(ResultOutput.table, converter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsObjectChart() {
        assertEquals(ResultOutput.chart, converter.getAsObject(null, null, "1"));
    }

    @Test
    public void testGetAsObjectTable() {
        assertEquals(ResultOutput.table, converter.getAsObject(null, null, "2"));
    }

    @Test
    public void testGetAsObjectChartAndTable() {
        assertEquals(ResultOutput.chartAndTable, converter.getAsObject(null, null, "3"));
    }

    @Test
    public void testGetAsObjectUnknownIdFallsBackToTable() {
        assertEquals(ResultOutput.table, converter.getAsObject(null, null, "99"));
    }

    @Test
    public void testGetAsStringChart() {
        assertEquals("1", converter.getAsString(null, null, ResultOutput.chart));
    }

    @Test
    public void testGetAsStringTable() {
        assertEquals("2", converter.getAsString(null, null, ResultOutput.table));
    }

    @Test
    public void testGetAsStringChartAndTable() {
        assertEquals("3", converter.getAsString(null, null, ResultOutput.chartAndTable));
    }

    @Test
    public void testGetAsStringNullReturnsTableId() {
        assertEquals(ResultOutput.table.getId(), converter.getAsString(null, null, null));
    }
}
