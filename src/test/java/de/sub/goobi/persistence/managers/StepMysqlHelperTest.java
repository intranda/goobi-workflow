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
package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class StepMysqlHelperTest extends AbstractTest {

    private static final String JOIN_BATCHES = "JOIN batches ON prozesse.batchID = batches.id";

    /**
     * A process without a batch has batchID = NULL, so inner joining batches drops every such process. That would hide their step titles from the
     * project workflow overview in ProjectHelper, and blank the overview entirely for a project in which no process is batched.
     */
    @Test
    public void testDistinctStepTitlesSqlLeftJoinsBatches() {
        assertTrue(StepMysqlHelper.DISTINCT_STEP_TITLES_AND_ORDER_SQL.contains("LEFT " + JOIN_BATCHES));
    }

    @Test
    public void testDistinctStepTitlesSqlHasNoInnerJoinOnBatches() {
        String sql = StepMysqlHelper.DISTINCT_STEP_TITLES_AND_ORDER_SQL;
        int index = sql.indexOf(JOIN_BATCHES);
        assertTrue(index >= "LEFT ".length(), "query does not join batches at all");
        assertEquals("LEFT ", sql.substring(index - "LEFT ".length(), index));
    }

    /**
     * The filter is appended afterwards and opens the WHERE clause itself, so the base query must not contain one.
     */
    @Test
    public void testDistinctStepTitlesSqlContainsNoWhereClause() {
        assertFalse(StepMysqlHelper.DISTINCT_STEP_TITLES_AND_ORDER_SQL.toLowerCase().contains("where"));
    }
}
