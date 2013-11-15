package de.sub.goobi.converter;

import static org.junit.Assert.*;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.junit.Test;

public class StatisticsCalculationUnitConverterTest {

    @Test
    public void testGetAsObject() {
        StatisticsCalculationUnitConverter conv = new StatisticsCalculationUnitConverter();
        assertEquals(CalculationUnit.volumes, conv.getAsObject(null, null, null));

        assertEquals(CalculationUnit.volumes, conv.getAsObject(null, null, "1"));
        assertEquals(CalculationUnit.pages, conv.getAsObject(null, null, "2"));
        assertEquals(CalculationUnit.volumesAndPages, conv.getAsObject(null, null, "3"));
        assertEquals(CalculationUnit.volumes, conv.getAsObject(null, null, "4"));
    }

    @Test
    public void testGetAsString() {
        StatisticsCalculationUnitConverter conv = new StatisticsCalculationUnitConverter();
        assertEquals(CalculationUnit.volumes.getId(), conv.getAsString(null, null, null));
        assertEquals(CalculationUnit.volumes.getId(), conv.getAsString(null, null, 42));
        assertEquals(CalculationUnit.volumes.getId(), conv.getAsString(null, null, CalculationUnit.volumes));
        assertEquals(CalculationUnit.pages.getId(), conv.getAsString(null, null, CalculationUnit.pages));
        assertEquals(CalculationUnit.volumesAndPages.getId(), conv.getAsString(null, null, CalculationUnit.volumesAndPages));
        
    }

}
