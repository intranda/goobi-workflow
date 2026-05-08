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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;

@ExtendWith(MockitoExtension.class)
public class SearchQueryTest extends AbstractTest {

    @Test
    public void testAllArgsConstructorAndGetters() {
        SearchQuery q = new SearchQuery("field1", "value1", RelationalOperator.EQUAL);

        assertEquals("field1", q.getField());
        assertEquals("value1", q.getValue());
        assertEquals(RelationalOperator.EQUAL, q.getRelation());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        SearchQuery q = new SearchQuery();
        q.setField("f");
        q.setValue("v");
        q.setRelation(RelationalOperator.LIKE);

        assertEquals("f", q.getField());
        assertEquals("v", q.getValue());
        assertEquals(RelationalOperator.LIKE, q.getRelation());
    }

    @Test
    public void testCreateLegacySqlClause() {
        SearchQuery q = new SearchQuery("name", "val", RelationalOperator.NEQUAL);

        StringBuilder b = new StringBuilder();
        q.createLegacySqlClause(b);

        assertEquals("(name=? and value!=?)", b.toString());
    }

    @Test
    public void testGetOperators() {
        SearchQuery q = new SearchQuery();

        RelationalOperator[] ops = q.getOperators();

        assertNotNull(ops);
        assertEquals(4, ops.length);
        assertArrayEquals(RelationalOperator.values(), ops);
    }

    @Test
    public void testAddLegacyParams() {
        SearchQuery q = new SearchQuery("field", "value", RelationalOperator.EQUAL);

        List<Object> params = new ArrayList<>();
        q.addLegacyParams(params);

        assertEquals(2, params.size());
        assertEquals("field", params.get(0));
        assertEquals("value", params.get(1));
    }

    @Test
    public void testCreateSqlClauseEQUAL() {
        testSqlClause(RelationalOperator.EQUAL,
                " EXISTS (SELECT 1 FROM metadata m0 WHERE m0.processid = prozesse.ProzesseID  AND m0.name = ?  AND m0.value = ? ) ");
    }

    @Test
    public void testCreateSqlClauseNEQUAL() {
        testSqlClause(RelationalOperator.NEQUAL,
                " NOT EXISTS (SELECT 1 FROM metadata m0 WHERE m0.processid = prozesse.ProzesseID  AND m0.name = ?  AND m0.value != ? ) ");
    }

    @Test
    public void testCreateSqlClauseLIKE() {
        testSqlClause(RelationalOperator.LIKE,
                " EXISTS (SELECT 1 FROM metadata m0 WHERE m0.processid = prozesse.ProzesseID  AND m0.name = ?  AND m0.value  LIKE  ? ) ");
    }

    @Test
    public void testCreateSqlClauseNLIKE() {
        testSqlClause(RelationalOperator.NLIKE,
                " NOT EXISTS (SELECT 1 FROM metadata m0 WHERE m0.processid = prozesse.ProzesseID  AND m0.name = ?  AND m0.value  NOT LIKE  ? ) ");
    }

    private void testSqlClause(RelationalOperator op, String expected) {
        SearchQuery q = new SearchQuery("f", "v", op);

        StringBuilder b = new StringBuilder();
        q.createSqlClause(b, 0);

        assertEquals(expected, b.toString());
    }

    @Test
    public void testAddParamsEQUAL() {
        SearchQuery q = new SearchQuery("field", "value", RelationalOperator.EQUAL);

        List<Object> params = new ArrayList<>();
        q.addParams(params);

        assertEquals(2, params.size());
        assertEquals("field", params.get(0));
        assertEquals("value", params.get(1));
    }

    @Test
    public void testAddParamsNEQUAL() {
        SearchQuery q = new SearchQuery("field", "value", RelationalOperator.NEQUAL);

        List<Object> params = new ArrayList<>();
        q.addParams(params);

        assertEquals("field", params.get(0));
        assertEquals("value", params.get(1));
    }

    @Test
    public void testAddParamsLIKE() {
        SearchQuery q = new SearchQuery("field", "value", RelationalOperator.LIKE);

        List<Object> params = new ArrayList<>();
        q.addParams(params);

        assertEquals("field", params.get(0));
        assertEquals("%value%", params.get(1));
    }

    @Test
    public void testAddParamsNLIKE() {
        SearchQuery q = new SearchQuery("field", "value", RelationalOperator.NLIKE);

        List<Object> params = new ArrayList<>();
        q.addParams(params);

        assertEquals("field", params.get(0));
        assertEquals("%value%", params.get(1));
    }
}