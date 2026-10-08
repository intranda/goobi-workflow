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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.junit.jupiter.api.Test;

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
        assertTrue(sql.contains("FROM metadata"));
        assertTrue(sql.contains("WHERE"));
    }

    @Test
    public void testCreateSqlParams() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("field", "value", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        Object[] params = req.createSqlParams();

        assertEquals(2, params.length);
        assertEquals("field", params[0]);
        assertEquals("value", params[1]);
    }

    @Test
    public void testSqlWithAdditionalFilters() {

        SearchRequest req = new SearchRequest();

        req.setFilterProjects(Arrays.asList("ProjectA", "ProjectB"));
        req.setFilterTemplateIDs(Arrays.asList(1, 2));
        req.setProperty("propName", "propValue");
        req.setStepStatus("Scan", "DONE");

        String sql = req.createSql();

        // filters on properties and steps must not join the rows, a process would be returned once per matching row
        assertFalse(sql.contains("JOIN properties"), sql);
        assertFalse(sql.contains("JOIN schritte"), sql);

        assertTrue(sql.contains("prozesse.ProjekteID IN (SELECT ProjekteID FROM projekte WHERE Titel IN (?, ?))"), sql);
        assertTrue(sql.contains("EXISTS (SELECT 1 FROM properties tp WHERE tp.object_type = 'process' AND tp.property_name = 'templateID' "
                + "AND tp.object_id = prozesse.ProzesseID AND tp.property_value IN (?, ?))"), sql);
        assertTrue(sql.contains("EXISTS (SELECT 1 FROM properties pp WHERE pp.object_type = 'process' AND pp.property_name = ? "
                + "AND pp.object_id = prozesse.ProzesseID AND pp.property_value = ?)"), sql);
        assertTrue(sql.contains("EXISTS (SELECT 1 FROM schritte s WHERE s.ProzesseID = prozesse.ProzesseID AND s.Titel = ? "
                + "AND s.Bearbeitungsstatus = ?)"), sql);
        assertEquals(sql.chars().filter(c -> c == '?').count(), req.createSqlParams().length, sql);
    }

    @Test
    public void testSortingAndLimit() {

        SearchRequest req = new SearchRequest();

        req.setSortField("title");
        req.setSortDescending(true);
        req.setLimit(10);
        req.setOffset(5);

        String sql = req.createSql();

        assertFalse(sql.contains("JOIN metadata"), sql);
        assertTrue(sql.contains("ORDER BY (SELECT MAX(value) FROM metadata WHERE metadata.name = ? "
                + "AND metadata.processid = prozesse.ProzesseID) DESC, prozesse.ProzesseID DESC"), sql);
        assertTrue(sql.contains("LIMIT ? OFFSET ?"));

        Object[] params = req.createSqlParams();

        assertEquals(3, params.length);
        assertEquals("title", params[0]);
        assertEquals(10, params[1]);
        assertEquals(5, params[2]);
    }

    @Test
    public void testSortAscending() {

        SearchRequest req = new SearchRequest();
        req.setSortField("author");
        req.setSortDescending(false);

        String sql = req.createSql();

        assertTrue(sql.contains("metadata.processid = prozesse.ProzesseID) ASC, prozesse.ProzesseID ASC"), sql);
        assertFalse(sql.contains("ORDER BY prozesse.ProzesseID ASC"));
    }

    @Test
    public void testNoSortFieldUsesDefaultOrder() {

        SearchRequest req = new SearchRequest();

        String sql = req.createSql();

        assertTrue(sql.contains("ORDER BY prozesse.ProzesseID ASC"));
        assertFalse(sql.contains("sort_meta"));
    }

    @Test
    public void testCachedMetadata() {

        SearchRequest req = new SearchRequest();
        assertFalse(req.isCachedMetadata());

        req.setCachedMetadata(true);
        assertTrue(req.isCachedMetadata());
    }

    @Test
    public void testCreateLegacySql() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("f", "v", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        // first case, without any additional data
        String sql = req.createSql();

        assertTrue(sql.contains("FROM metadata"));
        assertTrue(sql.contains("FROM prozesse"));
        assertFalse(sql.contains("pr.Titel IN (?"));
        assertFalse(sql.contains("properties.property_name=\"templateID\""));

        // project filter
        List<String> filterList = new ArrayList<>();
        filterList.add("A");

        req.setFilterProjects(filterList);
        sql = req.createSql();
        assertTrue(sql.contains("prozesse.ProjekteID IN (SELECT ProjekteID FROM projekte WHERE Titel IN (?))"), sql);

        // template filter
        List<Integer> idList = new ArrayList<>();
        idList.add(1);
        req.setFilterTemplateIDs(idList);

        sql = req.createSql();
        assertTrue(sql.contains("tp.property_name = 'templateID'"), sql);

    }

    @Test
    public void testCreateLegacySqlParams() {

        SearchRequest req = new SearchRequest();

        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("field", "value", RelationalOperator.EQUAL));

        req.addSearchGroup(group);

        Object[] params = req.createSqlParams();

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

    @Test
    public void testCreateSqlWithoutFiltersHasNoEmptyWhere() {
        SearchRequest req = new SearchRequest();

        String sql = req.createSql();

        assertFalse(sql.contains("WHERE"), sql);
        assertTrue(sql.contains("ORDER BY prozesse.ProzesseID ASC"), sql);
    }

    @Test
    public void testCreateSqlWithProjectFilterOnlyHasNoDanglingAnd() {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("ProjectA"));

        String sql = req.createSql();

        assertTrue(sql.contains("WHERE prozesse.ProjekteID IN (SELECT ProjekteID FROM projekte WHERE Titel IN (?))"), sql);
        assertFalse(sql.matches("(?s).*AND\\s+ORDER BY.*"), sql);
    }

    @Test
    public void testTemplateFilterBindsAllPlaceholders() {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("ProjectA"));
        req.setFilterTemplateIDs(Arrays.asList(1, 2));

        String sql = req.createSql();
        Object[] params = req.createSqlParams();

        assertEquals(sql.chars().filter(c -> c == '?').count(), params.length, sql);
        assertEquals("ProjectA", params[0]);
        assertEquals("1", params[1]);
        assertEquals("2", params[2]);
    }

    @Test
    public void testMetadataGroupsDoNotBindToOtherFilters() {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("ProjectA"));
        req.setMetadataConjunctive(false);
        SearchGroup first = new SearchGroup();
        first.addFilter(new SearchQuery("title", "a", RelationalOperator.LIKE));
        SearchGroup second = new SearchGroup();
        second.addFilter(new SearchQuery("title", "b", RelationalOperator.LIKE));
        req.addSearchGroup(first);
        req.addSearchGroup(second);

        String sql = req.createSql();

        // "project AND (group1 OR group2)", not "(project AND group1) OR group2"
        assertTrue(sql.contains("prozesse.ProjekteID IN (SELECT ProjekteID FROM projekte WHERE Titel IN (?)) AND (("), sql);
    }

    @Test
    public void testEmptyMetadataGroupIsIgnored() {
        SearchRequest req = new SearchRequest();
        req.addSearchGroup(new SearchGroup());

        String sql = req.createSql();

        assertFalse(sql.contains("()"), sql);
        assertFalse(sql.contains("WHERE"), sql);
    }

    @Test
    public void testResolvedProjectIdsAreUsedDirectly() {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("ProjectA", "ProjectB"));
        req.setFilterProjectIds(Arrays.asList(2, 5));

        String sql = req.createSql();
        Object[] params = req.createSqlParams();

        // constant ids let MariaDB use the index on prozesse.ProjekteID
        assertTrue(sql.contains("WHERE prozesse.ProjekteID IN (?, ?)"), sql);
        assertFalse(sql.contains("projekte"), sql);
        assertEquals(2, params.length);
        assertEquals(2, params[0]);
        assertEquals(5, params[1]);
    }

    @Test
    public void testSortParameterFollowsFilterParameters() {
        SearchRequest req = new SearchRequest();
        req.setFilterProjectIds(Arrays.asList(2));
        SearchGroup group = new SearchGroup();
        group.addFilter(new SearchQuery("CatalogIDDigital", "PPN1", RelationalOperator.EQUAL));
        req.addSearchGroup(group);
        req.setSortField("TitleDocMain");
        req.setLimit(10);
        req.setOffset(20);

        String sql = req.createSql();
        Object[] params = req.createSqlParams();

        assertEquals(sql.chars().filter(c -> c == '?').count(), params.length, sql);
        assertEquals(Arrays.asList(2, "CatalogIDDigital", "PPN1", "TitleDocMain", 10, 20), Arrays.asList(params));
    }

    @Test
    public void testStepFilterNeedsNameAndStatus() {
        SearchRequest req = new SearchRequest();
        req.setStepName("Scanning");

        String sql = req.createSql();

        assertFalse(sql.contains("schritte"), sql);
        assertEquals(0, req.createSqlParams().length);
    }

    @Test
    public void testPropertyFilterNeedsNameAndValue() {
        SearchRequest req = new SearchRequest();
        req.setPropName("DocType");

        String sql = req.createSql();

        assertFalse(sql.contains("properties"), sql);
        assertEquals(0, req.createSqlParams().length);
    }
}
