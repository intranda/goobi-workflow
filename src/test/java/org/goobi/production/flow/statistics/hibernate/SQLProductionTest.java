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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.jupiter.api.Test;

public class SQLProductionTest {

    @Test
    public void testGetSQLReturnsNonNull() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        assertNotNull(prod.getSQL());
    }

    @Test
    public void testGetSQLContainsSelectVolumes() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        assertTrue(prod.getSQL().contains("count(table_1.singleProcess) AS volumes"));
    }

    @Test
    public void testGetSQLContainsPagesSum() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        assertTrue(prod.getSQL().contains("sum(table_1.pages) AS pages"));
    }

    @Test
    public void testGetSQLWithIdsContainsInClause() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, Arrays.asList(1, 2, 3));
        String sql = prod.getSQL();
        assertTrue(sql.contains("prozesse.prozesseid in (1,2,3)"));
    }

    @Test
    public void testGetSQLWithoutIdsHasNoInClause() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        assertFalse(prod.getSQL().contains("prozesse.prozesseid in ("));
    }

    @Test
    public void testGetSQLWithTimeRangeContainsWhereClause() {
        Date from = new Date(0);
        Date to = new Date(System.currentTimeMillis());
        SQLProduction prod = new SQLProduction(from, to, TimeUnit.months, null);
        assertTrue(prod.getSQL().contains("WHERE"));
    }

    @Test
    public void testGetSQLWithYearsTimeUnit() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.years, null);
        assertTrue(prod.getSQL().contains("year("));
    }

    @Test
    public void testGetSQLWithDaysTimeUnit() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.days, null);
        assertTrue(prod.getSQL().contains("date_format("));
    }

    @Test
    public void testGetSQLWithNullTimeUnitUsesTotal() {
        SQLProduction prod = new SQLProduction(null, null, null, null);
        assertTrue(prod.getSQL().contains("'Total'"));
    }

    @Test
    public void testGetSQLWithStepDoneNull() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        // passing null stepDone falls back to getSQL()
        assertTrue(prod.getSQL(null).contains("count(table_1.singleProcess) AS volumes"));
    }

    @Test
    public void testGetSQLWithStepDoneNonNull() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, null);
        String sql = prod.getSQL(3);
        assertTrue(sql.contains("schritte.reihenfolge=3"));
        assertTrue(sql.contains("schritte.bearbeitungsstatus>2"));
    }

    @Test
    public void testGetSQLWithStepDoneAndIds() {
        SQLProduction prod = new SQLProduction(null, null, TimeUnit.months, Arrays.asList(10, 20));
        String sql = prod.getSQL(5);
        assertTrue(sql.contains("schritte.reihenfolge=5"));
        assertTrue(sql.contains("prozesse.prozesseid in (10,20)"));
    }
}
