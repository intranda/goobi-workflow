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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TimeUnitTest {

    @Test
    public void testDaysId() {
        assertEquals("1", TimeUnit.days.getId());
    }

    @Test
    public void testWeeksId() {
        assertEquals("2", TimeUnit.weeks.getId());
    }

    @Test
    public void testMonthsId() {
        assertEquals("3", TimeUnit.months.getId());
    }

    @Test
    public void testQuartersId() {
        assertEquals("4", TimeUnit.quarters.getId());
    }

    @Test
    public void testYearsId() {
        assertEquals("5", TimeUnit.years.getId());
    }

    @Test
    public void testSimpleSumId() {
        assertEquals("6", TimeUnit.simpleSum.getId());
    }

    @Test
    public void testGetByIdDays() {
        assertEquals(TimeUnit.days, TimeUnit.getById("1"));
    }

    @Test
    public void testGetByIdWeeks() {
        assertEquals(TimeUnit.weeks, TimeUnit.getById("2"));
    }

    @Test
    public void testGetByIdMonths() {
        assertEquals(TimeUnit.months, TimeUnit.getById("3"));
    }

    @Test
    public void testGetByIdYears() {
        assertEquals(TimeUnit.years, TimeUnit.getById("5"));
    }

    @Test
    public void testGetByIdUnknownReturnsDays() {
        assertEquals(TimeUnit.days, TimeUnit.getById("unknown"));
    }

    @Test
    public void testDaysDayFactor() {
        assertEquals(Double.valueOf(1.0), TimeUnit.days.getDayFactor());
    }

    @Test
    public void testWeeksDayFactor() {
        assertEquals(Double.valueOf(5.0), TimeUnit.weeks.getDayFactor());
    }

    @Test
    public void testMonthsDayFactor() {
        assertEquals(Double.valueOf(21.3), TimeUnit.months.getDayFactor());
    }

    @Test
    public void testSimpleSumDayFactor() {
        assertEquals(Double.valueOf(-1.0), TimeUnit.simpleSum.getDayFactor());
    }

    @Test
    public void testDaysSqlKeyword() {
        assertEquals("day", TimeUnit.days.getSqlKeyword());
    }

    @Test
    public void testMonthsSqlKeyword() {
        assertEquals("month", TimeUnit.months.getSqlKeyword());
    }

    @Test
    public void testToStringDays() {
        assertEquals("days", TimeUnit.days.toString());
    }

    @Test
    public void testToStringMonths() {
        assertEquals("months", TimeUnit.months.toString());
    }

    @Test
    public void testGetAllVisibleValuesExcludesSimpleSum() {
        List<TimeUnit> visible = TimeUnit.getAllVisibleValues();
        assertFalse(visible.contains(TimeUnit.simpleSum));
    }

    @Test
    public void testGetAllVisibleValuesContainsDays() {
        assertTrue(TimeUnit.getAllVisibleValues().contains(TimeUnit.days));
    }

    @Test
    public void testGetAllVisibleValuesHasFiveEntries() {
        assertEquals(5, TimeUnit.getAllVisibleValues().size());
    }

    @Test
    public void testGetDateRowDaysReturnsDayList() {
        // range of 3 days: 2024-01-01 to 2024-01-04
        Date start = new Date(124, 0, 1); // 2024-01-01
        Date end = new Date(124, 0, 4); // 2024-01-04
        List<String> row = TimeUnit.days.getDateRow(start, end);
        assertEquals(3, row.size());
        assertEquals("2024-01-01", row.get(0));
    }

    @Test
    public void testGetDateRowMonthsReturnsMonthList() {
        Date start = new Date(124, 0, 1); // 2024-01
        Date end = new Date(124, 2, 1); // 2024-03
        List<String> row = TimeUnit.months.getDateRow(start, end);
        assertEquals(2, row.size());
        assertEquals("2024/01", row.get(0));
        assertEquals("2024/02", row.get(1));
    }

    @Test
    public void testGetDateRowYearsReturnsYearList() {
        Date start = new Date(122, 0, 1); // 2022
        Date end = new Date(124, 0, 1); // 2024
        List<String> row = TimeUnit.years.getDateRow(start, end);
        assertEquals(2, row.size());
        assertEquals("2022", row.get(0));
    }

    @Test
    public void testGetTitleReturnsNonNull() {
        assertNotNull(TimeUnit.days.getTitle());
        assertNotNull(TimeUnit.months.getTitle());
    }
}
