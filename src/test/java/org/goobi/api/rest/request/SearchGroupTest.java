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

package org.goobi.api.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class SearchGroupTest extends AbstractTest {

    @Test
    public void testLegacySqlOrCombination() {

        SearchGroup group = new SearchGroup();
        group.setConjunctive(false); // OR

        group.addFilter(new SearchQuery("f1", "v1", RelationalOperator.EQUAL));
        group.addFilter(new SearchQuery("f2", "v2", RelationalOperator.NEQUAL));

        StringBuilder b = new StringBuilder();
        group.createLegacySqlClause(b);

        assertEquals(
                "((name=? and value=?)OR (name=? and value!=?))",
                b.toString());

        List<Object> params = new ArrayList<>();
        group.addLegacyParams(params);

        assertEquals(4, params.size());
        assertEquals("f1", params.get(0));
        assertEquals("v1", params.get(1));
        assertEquals("f2", params.get(2));
        assertEquals("v2", params.get(3));
    }

    @Test
    public void testLegacySqlAndCombination() {

        SearchGroup group = new SearchGroup();
        group.setConjunctive(true); // AND

        group.addFilter(new SearchQuery("a", "1", RelationalOperator.LIKE));
        group.addFilter(new SearchQuery("b", "2", RelationalOperator.NLIKE));

        StringBuilder b = new StringBuilder();
        group.createLegacySqlClause(b);

        assertEquals(
                "((name=? and value LIKE ?)AND (name=? and value NOT LIKE ?))",
                b.toString());
    }

    @Test
    public void testCreateSqlClauseAndParams() {

        SearchGroup group = new SearchGroup();
        group.setConjunctive(true); // AND

        group.addFilter(new SearchQuery("title", "goobi", RelationalOperator.LIKE));
        group.addFilter(new SearchQuery("status", "done", RelationalOperator.EQUAL));

        StringBuilder b = new StringBuilder();
        group.createSqlClause(b);

        assertEquals(
                "((JSON_EXTRACT(value, ?) LIKE ?)AND (JSON_CONTAINS(value, ?, ?)))",
                b.toString());

        List<Object> params = new ArrayList<>();
        group.addParams(params);

        assertEquals(4, params.size());

        // LIKE params
        assertEquals("$.title", params.get(0));
        assertEquals("%goobi%", params.get(1));

        // EQUAL params
        assertEquals("\"done\"", params.get(2));
        assertEquals("$.status", params.get(3));
    }

    @Test
    public void testEmptyGroupProducesEmptyBrackets() {

        SearchGroup group = new SearchGroup();

        StringBuilder b1 = new StringBuilder();
        group.createLegacySqlClause(b1);
        assertEquals("()", b1.toString());

        StringBuilder b2 = new StringBuilder();
        group.createSqlClause(b2);
        assertEquals("()", b2.toString());

        List<Object> params = new ArrayList<>();
        group.addParams(params);
        assertTrue(params.isEmpty());
    }

    @Test
    public void testNewFilterAddsDefaultQuery() {

        SearchGroup group = new SearchGroup();

        group.newFilter();

        assertEquals(1, group.getNumFilters());

        SearchQuery q = group.getFilters().get(0);

        assertEquals("", q.getField());
        assertEquals("", q.getValue());
        assertEquals(RelationalOperator.EQUAL, q.getRelation());
    }

    @Test
    public void testDeleteFilter() {

        SearchGroup group = new SearchGroup();

        group.addFilter(new SearchQuery("a", "1", RelationalOperator.EQUAL));
        group.addFilter(new SearchQuery("b", "2", RelationalOperator.EQUAL));

        group.deleteFilter(0);

        assertEquals(1, group.getNumFilters());
        assertEquals("b", group.getFilters().get(0).getField());
    }
}