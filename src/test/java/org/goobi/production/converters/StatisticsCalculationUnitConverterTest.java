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

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.junit.Test;

public class StatisticsCalculationUnitConverterTest {

    private final StatisticsCalculationUnitConverter converter = new StatisticsCalculationUnitConverter();

    @Test
    public void testConverterIdConstant() {
        assertEquals("StatisticsCalculationUnitConverter", StatisticsCalculationUnitConverter.CONVERTER_ID);
    }

    @Test
    public void testGetAsObjectNullReturnsVolumes() {
        assertEquals(CalculationUnit.volumes, converter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsObjectVolumes() {
        assertEquals(CalculationUnit.volumes, converter.getAsObject(null, null, "1"));
    }

    @Test
    public void testGetAsObjectPages() {
        assertEquals(CalculationUnit.pages, converter.getAsObject(null, null, "2"));
    }

    @Test
    public void testGetAsObjectVolumesAndPages() {
        assertEquals(CalculationUnit.volumesAndPages, converter.getAsObject(null, null, "3"));
    }

    @Test
    public void testGetAsObjectUnknownIdFallsBackToVolumes() {
        assertEquals(CalculationUnit.volumes, converter.getAsObject(null, null, "99"));
    }

    @Test
    public void testGetAsStringVolumes() {
        assertEquals("1", converter.getAsString(null, null, CalculationUnit.volumes));
    }

    @Test
    public void testGetAsStringPages() {
        assertEquals("2", converter.getAsString(null, null, CalculationUnit.pages));
    }

    @Test
    public void testGetAsStringVolumesAndPages() {
        assertEquals("3", converter.getAsString(null, null, CalculationUnit.volumesAndPages));
    }

    @Test
    public void testGetAsStringNullReturnsVolumesId() {
        assertEquals(CalculationUnit.volumes.getId(), converter.getAsString(null, null, null));
    }
}
