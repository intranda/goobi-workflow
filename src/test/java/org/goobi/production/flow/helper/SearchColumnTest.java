package org.goobi.production.flow.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SearchColumnTest {

    @Test
    public void testGetTableName() {
        SearchColumn col = new SearchColumn(1);

        col.setValue("prozesse.title");
        assertEquals("prozesse", col.getTableName());

        col.setValue("projekte.name");
        assertEquals("projekte", col.getTableName());

        col.setValue("property.custom");
        assertEquals("properties1", col.getTableName());

        col.setValue("metadata.author");
        assertEquals("metadata1", col.getTableName());

        col.setValue("log.action");
        assertEquals("log", col.getTableName());

        col.setValue("unknown.value");
        assertEquals("", col.getTableName());
    }

    @Test
    public void testGetTableType() {
        SearchColumn col = new SearchColumn(2);

        col.setValue("prozesse.title");
        assertEquals("", col.getTableType());

        col.setValue("projekte.name");
        assertEquals("projekte ", col.getTableType());

        col.setValue("property.custom");
        assertEquals("properties ", col.getTableType());

        col.setValue("metadata.author");
        assertEquals("metadata ", col.getTableType());

        col.setValue("log.action");
        assertEquals("journal ", col.getTableType());

        col.setValue("other.field");
        assertEquals("", col.getTableType());
    }

    @Test
    public void testGetColumnName() {
        SearchColumn col = new SearchColumn(3);

        col.setValue("prozesse.title");
        assertEquals("title", col.getColumnName());

        col.setValue("projekte.name");
        assertEquals("name", col.getColumnName());

        col.setValue("metadata.author");
        assertEquals("print", col.getColumnName());

        col.setValue("log.action");
        assertEquals("content", col.getColumnName());

        col.setValue("property.customField");
        assertEquals("property_value", col.getColumnName());

        col.setValue("invalidfield");
        assertEquals("", col.getColumnName());

        col.setValue("");
        assertEquals("", col.getColumnName());
    }

    @Test
    public void testGetJoinClause() {
        SearchColumn col = new SearchColumn(5);

        col.setValue("property.customProperty");
        String expectedPropJoin =
                " properties properties5 ON prozesse.ProzesseID = properties5.object_id AND properties5.object_type = 'process' AND properties5.property_name = \"customProperty\"";
        assertEquals(expectedPropJoin, col.getJoinClause());

        col.setValue("metadata.title");
        String expectedMetaJoin = " metadata metadata5 ON prozesse.ProzesseID = metadata5.processid AND metadata5.name = \"title\"";
        assertEquals(expectedMetaJoin, col.getJoinClause());

        col.setValue("projekte.name");
        String expectedProjJoin = " projekte projekte ON prozesse.ProjekteID = projekte.ProjekteID";
        assertEquals(expectedProjJoin, col.getJoinClause());

        col.setValue("prozesse.title");
        assertEquals("", col.getJoinClause());

        col.setValue("unknown.field");
        assertEquals("", col.getJoinClause());
    }

    @Test
    public void testOrderAndValueAccessors() {
        SearchColumn col = new SearchColumn(7);
        assertEquals(7, col.getOrder());

        col.setValue("metadata.foo");
        assertEquals("metadata.foo", col.getValue());
    }
}
