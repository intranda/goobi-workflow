package de.sub.goobi.converter;

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
 * 
 */
import static org.junit.Assert.assertEquals;

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class StatisticsTimeUnitConverterTest extends AbstractTest {

    @Test
    public void testGetAsObject() {
        StatisticsTimeUnitConverter conv = new StatisticsTimeUnitConverter();

        assertEquals(TimeUnit.days, conv.getAsObject(null, null, null));
        assertEquals(TimeUnit.days, conv.getAsObject(null, null, "1"));
        assertEquals(TimeUnit.weeks, conv.getAsObject(null, null, "2"));
        assertEquals(TimeUnit.months, conv.getAsObject(null, null, "3"));
        assertEquals(TimeUnit.quarters, conv.getAsObject(null, null, "4"));
        assertEquals(TimeUnit.years, conv.getAsObject(null, null, "5"));
        assertEquals(TimeUnit.simpleSum, conv.getAsObject(null, null, "6"));
        assertEquals(TimeUnit.days, conv.getAsObject(null, null, "42"));

    }

    @Test
    public void testGetAsString() {
        StatisticsTimeUnitConverter conv = new StatisticsTimeUnitConverter();
        assertEquals("1", conv.getAsString(null, null, null));

        assertEquals("1", conv.getAsString(null, null, TimeUnit.days));
        assertEquals("2", conv.getAsString(null, null, TimeUnit.weeks));
        assertEquals("3", conv.getAsString(null, null, TimeUnit.months));
        assertEquals("4", conv.getAsString(null, null, TimeUnit.quarters));
        assertEquals("5", conv.getAsString(null, null, TimeUnit.years));
        assertEquals("6", conv.getAsString(null, null, TimeUnit.simpleSum));

    }

}
