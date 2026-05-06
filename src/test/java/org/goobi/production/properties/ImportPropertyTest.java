package org.goobi.production.properties;

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
 *
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ImportPropertyTest {

    private ImportProperty property;

    @BeforeEach
    public void setUp() {
        property = new ImportProperty();
    }

    @Test
    public void testConstructor() {
        assertNotNull(property);
    }

    @Test
    public void testDefaultValues() {
        assertEquals("", property.getName());
        assertEquals("0", property.getContainer());
        assertEquals("", property.getValidation());
        assertEquals(Type.TEXT, property.getType());
        assertEquals("", property.getValue());
        assertNotNull(property.getPossibleValues());
        assertTrue(property.getPossibleValues().isEmpty());
        assertNotNull(property.getProjects());
        assertTrue(property.getProjects().isEmpty());
        assertFalse(property.isRequired());
        assertEquals("dd.MM.yyyy", property.getPattern());
    }

    // --- isValid ---

    @Test
    public void testIsValidMatchingRegex() {
        property.setValidation("\\d+");
        property.setValue("123");
        assertTrue(property.isValid());
    }

    @Test
    public void testIsValidNonMatchingRegex() {
        property.setValidation("\\d+");
        property.setValue("abc");
        assertFalse(property.isValid());
    }

    @Test
    public void testIsValidEmptyValidationMatchesAnything() {
        property.setValidation(".*");
        property.setValue("anything");
        assertTrue(property.isValid());
    }

    // --- getValueList / setValueList ---

    @Test
    public void testGetValueListSingleValue() {
        property.setValue("a");
        List<String> list = property.getValueList();
        assertEquals(1, list.size());
        assertEquals("a", list.get(0));
    }

    @Test
    public void testGetValueListMultipleValues() {
        property.setValue("a; b; c");
        List<String> list = property.getValueList();
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testSetValueListJoinsWithSemicolon() {
        property.setValueList(Arrays.asList("a", "b", "c"));
        assertEquals("a; b; c; ", property.getValue());
    }

    @Test
    public void testSetValueListEmptyList() {
        property.setValueList(Arrays.asList());
        assertEquals("", property.getValue());
    }

    // --- getBooleanValue / setBooleanValue ---

    @Test
    public void testGetBooleanValueTrue() {
        property.setValue("true");
        assertTrue(property.getBooleanValue());
    }

    @Test
    public void testGetBooleanValueTrueUpperCase() {
        property.setValue("TRUE");
        assertTrue(property.getBooleanValue());
    }

    @Test
    public void testGetBooleanValueFalse() {
        property.setValue("false");
        assertFalse(property.getBooleanValue());
    }

    @Test
    public void testGetBooleanValueArbitraryStringIsFalse() {
        property.setValue("yes");
        assertFalse(property.getBooleanValue());
    }

    @Test
    public void testSetBooleanValueTrue() {
        property.setBooleanValue(true);
        assertEquals("true", property.getValue());
    }

    @Test
    public void testSetBooleanValueFalse() {
        property.setBooleanValue(false);
        assertEquals("false", property.getValue());
    }

    // --- getDateValue / setDateValue ---

    @Test
    public void testSetAndGetDateValue() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JUNE, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date input = cal.getTime();

        property.setDateValue(input);
        Date result = property.getDateValue();

        assertNotNull(result);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(2024, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, resultCal.get(Calendar.MONTH));
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testGetDateValueWithInvalidValueReturnsCurrentDate() {
        property.setValue("not-a-date");
        Date result = property.getDateValue();
        assertNotNull(result);
    }

    @Test
    public void testGetDateValueWithNullValueReturnsCurrentDate() {
        property.setValue(null);
        Date result = property.getDateValue();
        assertNotNull(result);
    }

    // --- getClone ---

    @Test
    public void testGetCloneReturnsNewInstance() {
        ImportProperty clone = property.getClone("1");
        assertNotNull(clone);
    }

    // --- container ---

    @Test
    public void testSetContainer() {
        property.setContainer("5");
        assertEquals("5", property.getContainer());
    }

    // --- UnsupportedOperationException ---

    @Test
    public void testGetShowStepConditionsThrows() {
        assertThrows(UnsupportedOperationException.class, () -> {
            property.getShowStepConditions();
        });
    }

    @Test
    public void testGetShowProcessGroupAccessConditionThrows() {
        assertThrows(UnsupportedOperationException.class, () -> {
            property.getShowProcessGroupAccessCondition();
        });
    }
}
