package de.sub.goobi.converter;

import static org.junit.Assert.*;

import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.junit.Test;

public class StatisticsTimeUnitConverterTest {

    @Test
    public void testGetAsObject() {
        StatisticsTimeUnitConverter conv = new StatisticsTimeUnitConverter();

        assertEquals(TimeUnit.days, conv.getAsObject(null, null, null));
        assertEquals(TimeUnit.days, conv.getAsObject(null, null, "1"));
        assertEquals(TimeUnit.weeks, conv.getAsObject(null, null, "2"));
        assertEquals(TimeUnit.months, conv.getAsObject(null, null, "3"));
        assertEquals(TimeUnit.quarters, conv.getAsObject(null, null, "4"));
        assertEquals(TimeUnit.years, conv.getAsObject(null, null, "5"));
        assertEquals(TimeUnit.simpleSum, conv.getAsObject(null, null, "6"));
        assertEquals(TimeUnit.days, conv.getAsObject(null, null, "42"));

    }

    @Test
    public void testGetAsString() {
        StatisticsTimeUnitConverter conv = new StatisticsTimeUnitConverter();
        assertEquals("1", conv.getAsString(null, null, null));
        assertEquals("1", conv.getAsString(null, null, 42));

        assertEquals("1", conv.getAsString(null, null, TimeUnit.days));
        assertEquals("2", conv.getAsString(null, null, TimeUnit.weeks));
        assertEquals("3", conv.getAsString(null, null, TimeUnit.months));
        assertEquals("4", conv.getAsString(null, null, TimeUnit.quarters));
        assertEquals("5", conv.getAsString(null, null, TimeUnit.years));
        assertEquals("6", conv.getAsString(null, null, TimeUnit.simpleSum));

    }

}
