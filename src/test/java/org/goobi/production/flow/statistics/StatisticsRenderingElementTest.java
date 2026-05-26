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
package org.goobi.production.flow.statistics;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.jupiter.api.Test;

import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;

public class StatisticsRenderingElementTest {

    private static final IStatisticalQuestion DUMMY_QUESTION = new IStatisticalQuestion() {
        private static final long serialVersionUID = 1L;

        @Override
        public List<DataTable> getDataTables(String f, String o, boolean c, boolean a) {
            return List.of();
        }

        @Override
        public void setTimeUnit(TimeUnit t) {
        }

        @Override
        public void setCalculationUnit(CalculationUnit cu) {
        }

        @Override
        public Boolean isRendererInverted(IRenderer r) {
            return Boolean.FALSE;
        }

        @Override
        public String getNumberFormatPattern() {
            return "#";
        }
    };

    private StatisticsRenderingElement elementWithRenderer(DataTable table) throws Exception {
        HtmlTableRenderer renderer = new HtmlTableRenderer();
        renderer.setDataTable(table);

        StatisticsRenderingElement element = new StatisticsRenderingElement(table, DUMMY_QUESTION);

        Field field = StatisticsRenderingElement.class.getDeclaredField("htmlTableRenderer");
        field.setAccessible(true);
        field.set(element, renderer);

        return element;
    }

    @Test
    public void testSanitizedRenderingStripsScriptInRowLabel() throws Exception {
        DataTable table = new DataTable("Statistics");
        DataRow row = new DataRow("count");
        row.addValue("<script>alert('xss')</script>", 1.0);
        table.addDataRow(row);

        String result = elementWithRenderer(table).getSanitizedRendering();

        assertFalse(result.contains("<script>"), "script tag must be stripped from statistics rendering");
    }

    @Test
    public void testSanitizedRenderingStripsScriptInRowName() throws Exception {
        DataTable table = new DataTable("Statistics");
        DataRow row = new DataRow("<script>alert(1)</script>");
        row.addValue("col", 1.0);
        table.addDataRow(row);

        String result = elementWithRenderer(table).getSanitizedRendering();

        assertFalse(result.contains("<script>"), "script tag in row name must be stripped");
    }

    @Test
    public void testSanitizedRenderingStripsEventHandlers() throws Exception {
        DataTable table = new DataTable("Statistics");
        DataRow row = new DataRow("<img src=x onerror=alert(1)>");
        row.addValue("col", 2.0);
        table.addDataRow(row);

        String result = elementWithRenderer(table).getSanitizedRendering();

        assertFalse(result.contains("onerror"), "event handler attributes must be stripped from statistics rendering");
    }

    @Test
    public void testSanitizedRenderingPreservesTableStructure() throws Exception {
        DataTable table = new DataTable("My Statistics");
        DataRow row = new DataRow("Row Name");
        row.addValue("Column", 42.0);
        table.addDataRow(row);

        String result = elementWithRenderer(table).getSanitizedRendering();

        assertTrue(result.contains("<table"), "table tag must be preserved in sanitized rendering");
        assertTrue(result.contains("Row Name"), "row name text must be preserved in sanitized rendering");
    }

    @Test
    public void testSanitizedRenderingWithNullRendererReturnsEmpty() {
        DataTable table = new DataTable("Test");
        StatisticsRenderingElement element = new StatisticsRenderingElement(table, DUMMY_QUESTION);

        String result = element.getSanitizedRendering();

        assertNotNull(result);
        assertTrue(result.isEmpty(), "null renderer must produce empty string");
    }
}
