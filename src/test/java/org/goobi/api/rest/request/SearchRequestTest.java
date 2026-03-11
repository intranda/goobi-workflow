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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class SearchRequestTest extends AbstractTest {

    @Test
    public void testCreateSqlWithMetadataFilter() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.setConjunctive(true);
        group.addFilter(new SearchQuery("title", "goobi", RelationalOperator.LIKE));

        req.addSearchGroup(group);
        req.setMetadataConjunctive(true);

        String sql = req.createSql();

        assertTrue(sql.contains("SELECT prozesse.ProzesseID"));
        assertTrue(sql.contains("FROM metadata_json"));
        assertTrue(sql.contains("WHERE"));
        assertTrue(sql.contains("JSON_EXTRACT(value, ?) LIKE ?"));
        assertTrue(sql.contains("ORDER BY metadata_json.processid ASC"));
    }

    @Test
    public void testCreateSqlParams() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("field", "value", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        Object[] params = req.createSqlParams();

        assertEquals(2, params.length);
        assertEquals("\"value\"", params[0]);
        assertEquals("$.field", params[1]);
    }

    @Test
    public void testSqlWithAdditionalFilters() {

        SearchRequest req = new SearchRequest();

        req.setFilterProjects(Arrays.asList("ProjectA", "ProjectB"));
        req.setFilterTemplateIDs(Arrays.asList(1, 2));
        req.setProperty("propName", "propValue");
        req.setStepStatus("Scan", "DONE");

        String sql = req.createSql();

        assertTrue(sql.contains("LEFT JOIN projekte"));
        assertTrue(sql.contains("LEFT JOIN properties"));
        assertTrue(sql.contains("LEFT JOIN schritte"));

        assertTrue(sql.contains("projekte.Titel IN"));
        assertTrue(sql.contains("properties.property_name=\"templateID\""));
        assertTrue(sql.contains("schritte.Titel IN"));
    }

    @Test
    public void testSortingAndLimit() {

        SearchRequest req = new SearchRequest();

        req.setSortField("title");
        req.setSortDescending(true);
        req.setLimit(10);
        req.setOffset(5);

        String sql = req.createSql();

        assertTrue(sql.contains("ORDER BY JSON_EXTRACT(value, ?) DESC"));
        assertTrue(sql.contains("LIMIT ? OFFSET ?"));

        Object[] params = req.createSqlParams();

        assertEquals(3, params.length);
        assertEquals("$.title", params[0]);
        assertEquals(10, params[1]);
        assertEquals(5, params[2]);
    }

    @Test
    public void testCreateLegacySql() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("f", "v", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        // first case, without any additional data
        String sql = req.createLegacySql();

        assertTrue(sql.contains("FROM metadata"));
        assertTrue(sql.contains("prozesse.ProzesseID IN"));
        assertTrue(sql.contains("name=? and value=?"));
        assertFalse(sql.contains("projekte.Titel IN (?)"));
        assertFalse(sql.contains("properties.property_name=\"templateID\""));

        // project filter
        List<String> filterList = new ArrayList<>();
        filterList.add("A");

        req.setFilterProjects(filterList);
        sql = req.createLegacySql();
        assertTrue(sql.contains("projekte.Titel IN (?)"));

        // template filter
        List<Integer> idList = new ArrayList<>();
        idList.add(1);
        req.setFilterTemplateIDs(idList);

        sql = req.createLegacySql();
        assertTrue(sql.contains("properties.property_name=\"templateID\""));

    }

    @Test
    public void testCreateLegacySqlParams() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("field", "value", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        Object[] params = req.createLegacySqlParams();

        assertEquals(2, params.length);
        assertEquals("field", params[0]);
        assertEquals("value", params[1]);
    }

    @Test
    public void testStepStatusNameResolution() {
        SearchRequest req = new SearchRequest();
        req.setStepStatus("Scan", "DONE");

        Object[] params = req.createSqlParams();
        // stepName at index 0, resolved stepStatus at index 1
        assertEquals("Scan", params[0]);
        assertEquals("3", params[1]);
    }

    @Test
    public void testStepStatusSearchStringResolution() {
        SearchRequest req = new SearchRequest();
        req.setStepStatus("Scan", "stepopen");

        Object[] params = req.createSqlParams();
        assertEquals("Scan", params[0]);
        assertEquals("1", params[1]);
    }

    @Test
    public void testStepStatusNumericPassthrough() {
        SearchRequest req = new SearchRequest();
        req.setStepStatus("Scan", "3");

        Object[] params = req.createSqlParams();
        assertEquals("Scan", params[0]);
        assertEquals("3", params[1]);
    }

    @Test
    public void testGroupManagement() {

        SearchRequest req = new SearchRequest();

        req.newGroup();

        assertEquals(1, req.getNumGroups());

        req.deleteSearchGroup(0);

        assertEquals(0, req.getNumGroups());
    }
}
