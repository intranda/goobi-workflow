package de.sub.goobi.converter;

import static org.junit.Assert.*;

import org.goobi.production.flow.statistics.enums.ResultOutput;
import org.junit.Test;

public class StatisticsResultOutputConverterTest {

    @Test
    public void testGetAsObject() {
        StatisticsResultOutputConverter conv = new StatisticsResultOutputConverter();
        assertEquals(ResultOutput.table, conv.getAsObject(null, null, null));
        assertEquals(ResultOutput.chart, conv.getAsObject(null, null, "1"));
        assertEquals(ResultOutput.table, conv.getAsObject(null, null, "2"));
        assertEquals(ResultOutput.chartAndTable, conv.getAsObject(null, null, "3"));
        assertEquals(ResultOutput.table, conv.getAsObject(null, null, "42"));

    }

    @Test
    public void testGetAsString() {
        StatisticsResultOutputConverter conv = new StatisticsResultOutputConverter();
        assertEquals("2", conv.getAsString(null, null, null));
        assertEquals("2", conv.getAsString(null, null, 42));
        assertEquals("1", conv.getAsString(null, null, ResultOutput.chart));
        assertEquals("2", conv.getAsString(null, null, ResultOutput.table));
        assertEquals("3", conv.getAsString(null, null, ResultOutput.chartAndTable));

    }

}
