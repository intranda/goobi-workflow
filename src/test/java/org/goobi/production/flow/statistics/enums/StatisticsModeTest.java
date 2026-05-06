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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StatisticsModeTest {

    @Test
    public void testValuesHasEightEntries() {
        assertEquals(8, StatisticsMode.values().length);
    }

    @Test
    public void testGetModeSimpleRuntimeSteps() {
        assertEquals("runtimeOfSteps", StatisticsMode.SIMPLE_RUNTIME_STEPS.getMode());
    }

    @Test
    public void testGetModeProjects() {
        assertEquals("projectAssociation", StatisticsMode.PROJECTS.getMode());
    }

    @Test
    public void testGetModeThroughput() {
        assertEquals("productionThroughput", StatisticsMode.THROUGHPUT.getMode());
    }

    @Test
    public void testGetModeProduction() {
        assertEquals("productionStatistics", StatisticsMode.PRODUCTION.getMode());
    }

    @Test
    public void testSimpleRuntimeStepsIsSimple() {
        assertTrue(StatisticsMode.SIMPLE_RUNTIME_STEPS.getIsSimple());
    }

    @Test
    public void testThroughputIsNotSimple() {
        assertFalse(StatisticsMode.THROUGHPUT.getIsSimple());
    }

    @Test
    public void testProductionIsNotSimple() {
        assertFalse(StatisticsMode.PRODUCTION.getIsSimple());
    }

    @Test
    public void testThroughputRendersIncludeLoops() {
        assertTrue(StatisticsMode.THROUGHPUT.isRenderIncludeLoops());
    }

    @Test
    public void testProjectsDoesNotRenderIncludeLoops() {
        assertFalse(StatisticsMode.PROJECTS.isRenderIncludeLoops());
    }

    @Test
    public void testThroughputRestrictsDate() {
        assertTrue(StatisticsMode.THROUGHPUT.getRestrictedDate());
    }

    @Test
    public void testProjectsDoesNotRestrictDate() {
        assertFalse(StatisticsMode.PROJECTS.getRestrictedDate());
    }

    @Test
    public void testSimpleRuntimeStepsHasNullQuestion() {
        assertNull(StatisticsMode.SIMPLE_RUNTIME_STEPS.getStatisticalQuestion());
    }

    @Test
    public void testProjectsHasNonNullQuestion() {
        assertNotNull(StatisticsMode.PROJECTS.getStatisticalQuestion());
    }

    @Test
    public void testProductionHasNonNullQuestion() {
        assertNotNull(StatisticsMode.PRODUCTION.getStatisticalQuestion());
    }

    @Test
    public void testGetTitleReturnsNonNull() {
        assertNotNull(StatisticsMode.PRODUCTION.getTitle());
    }
}
