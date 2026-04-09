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

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.Test;

public class StatisticsTimeUnitConverterTest {

    private final StatisticsTimeUnitConverter converter = new StatisticsTimeUnitConverter();

    @Test
    public void testConverterIdConstant() {
        assertEquals("StatisticsTimeUnitConverter", StatisticsTimeUnitConverter.CONVERTER_ID);
    }

    @Test
    public void testGetAsObjectNullReturnsDays() {
        assertEquals(TimeUnit.days, converter.getAsObject(null, null, null));
    }

    @Test
    public void testGetAsObjectDays() {
        assertEquals(TimeUnit.days, converter.getAsObject(null, null, "1"));
    }

    @Test
    public void testGetAsObjectWeeks() {
        assertEquals(TimeUnit.weeks, converter.getAsObject(null, null, "2"));
    }

    @Test
    public void testGetAsObjectMonths() {
        assertEquals(TimeUnit.months, converter.getAsObject(null, null, "3"));
    }

    @Test
    public void testGetAsObjectQuarters() {
        assertEquals(TimeUnit.quarters, converter.getAsObject(null, null, "4"));
    }

    @Test
    public void testGetAsObjectYears() {
        assertEquals(TimeUnit.years, converter.getAsObject(null, null, "5"));
    }

    @Test
    public void testGetAsObjectSimpleSum() {
        assertEquals(TimeUnit.simpleSum, converter.getAsObject(null, null, "6"));
    }

    @Test
    public void testGetAsObjectUnknownIdFallsBackToDays() {
        assertEquals(TimeUnit.days, converter.getAsObject(null, null, "99"));
    }

    @Test
    public void testGetAsStringDays() {
        assertEquals("1", converter.getAsString(null, null, TimeUnit.days));
    }

    @Test
    public void testGetAsStringMonths() {
        assertEquals("3", converter.getAsString(null, null, TimeUnit.months));
    }

    @Test
    public void testGetAsStringYears() {
        assertEquals("5", converter.getAsString(null, null, TimeUnit.years));
    }

    @Test
    public void testGetAsStringNullReturnsDaysId() {
        assertEquals(TimeUnit.days.getId(), converter.getAsString(null, null, null));
    }
}
