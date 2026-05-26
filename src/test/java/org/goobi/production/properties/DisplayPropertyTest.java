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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class DisplayPropertyTest extends AbstractTest {

    private DisplayProperty property;

    @BeforeEach
    public void setUp() {
        property = new DisplayProperty();
    }

    @Test
    public void testConstructor() {
        assertNotNull(property);
    }

    @Test
    public void testConstructorInitializesCollections() {
        assertNotNull(property.getPossibleValues());
        assertTrue(property.getPossibleValues().isEmpty());
        assertNotNull(property.getProjects());
        assertTrue(property.getProjects().isEmpty());
        assertNotNull(property.getWorkflows());
        assertTrue(property.getWorkflows().isEmpty());
        assertNotNull(property.getShowStepConditions());
        assertTrue(property.getShowStepConditions().isEmpty());
    }

    @Test
    public void testConstructorInitializesMultiSelectBeans() {
        assertNotNull(property.getNormalSelectionBean());
        assertNotNull(property.getVocabularySelectionBean());
    }

    @Test
    public void testSetValueAlsoSetsReadValue() {
        property.setValue("testValue");
        assertEquals("testValue", property.getValue());
        assertEquals("testValue", property.getReadValue());
    }

    @Test
    public void testSetValueWithNullType() {
        property.setType(Type.TEXT);
        property.setValue("hello");
        assertEquals("hello", property.getValue());
        assertEquals("hello", property.getReadValue());
    }

    @Test
    public void testIsValidWithNullValidationReturnsTrue() {
        property.setValidation(null);
        property.setValue("anything");
        assertTrue(property.isValid());
    }

    @Test
    public void testIsValidWithEmptyValidationReturnsTrue() {
        property.setValidation("");
        property.setValue("anything");
        assertTrue(property.isValid());
    }

    @Test
    public void testIsValidWithNullValueReturnsTrue() {
        property.setValidation("\\d+");
        property.setValue(null);
        assertTrue(property.isValid());
    }

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
    public void testGetValueListWithNullValueReturnsEmpty() {
        property.setValue(null);
        List<String> list = property.getValueList();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetValueListWithoutSeparatorReturnsEmpty() {
        property.setValue("singleValue");
        List<String> list = property.getValueList();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetValueListWithSeparator() {
        property.setValue("a; b; c");
        List<String> list = property.getValueList();
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testSetValueListJoinsWithSemicolon() {
        property.setValueList(Arrays.asList("x", "y"));
        assertEquals("x; y; ", property.getValue());
        assertEquals("x; y; ", property.getReadValue());
    }

    @Test
    public void testSetValueListEmpty() {
        property.setValueList(Arrays.asList());
        assertEquals("", property.getValue());
    }

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
    public void testSetBooleanValueTrue() {
        property.setBooleanValue(true);
        assertEquals("true", property.getValue());
        assertEquals("true", property.getReadValue());
    }

    @Test
    public void testSetBooleanValueFalse() {
        property.setBooleanValue(false);
        assertEquals("false", property.getValue());
        assertEquals("false", property.getReadValue());
    }

    @Test
    public void testSetAndGetDateValue() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.MARCH, 20, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        property.setDateValue(cal.getTime());
        Date result = property.getDateValue();

        assertNotNull(result);
        Calendar resultCal = Calendar.getInstance();
        resultCal.setTime(result);
        assertEquals(2024, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.MARCH, resultCal.get(Calendar.MONTH));
        assertEquals(20, resultCal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testSetDateValueAlsoSetsReadValue() {
        property.setDateValue(new Date());
        assertNotNull(property.getReadValue());
        assertEquals(property.getValue(), property.getReadValue());
    }

    @Test
    public void testGetDateValueWithInvalidValueReturnsCurrentDate() {
        property.setValue("not-a-date");
        Date result = property.getDateValue();
        assertNotNull(result);
    }

    @Test
    public void testGetCloneReturnsNewInstance() {
        property.setName("TestProp");
        property.setValidation(".*");
        property.setType(Type.TEXT);
        property.setValue("testValue");

        DisplayProperty clone = property.getClone("99");
        assertNotNull(clone);
        assertEquals("99", clone.getContainer());
        assertEquals("TestProp", clone.getName());
        assertEquals(".*", clone.getValidation());
        assertEquals(Type.TEXT, clone.getType());
        assertEquals("testValue", clone.getValue());
    }

    @Test
    public void testGetCloneHasIndependentCollections() {
        property.getProjects().add("Project1");
        DisplayProperty clone = property.getClone("1");
        clone.getProjects().add("Project2");
        assertEquals(1, property.getProjects().size());
    }

    @Test
    public void testGetIsNewWithNullNameReturnsTrue() {
        property.setName(null);
        assertTrue(property.getIsNew());
    }

    @Test
    public void testGetIsNewWithEmptyNameReturnsTrue() {
        property.setName("");
        assertTrue(property.getIsNew());
    }

    @Test
    public void testGetIsNewWithNameReturnsFalse() {
        property.setName("SomeName");
        assertFalse(property.getIsNew());
    }

    @Test
    public void testComparePropertiesByContainer() {
        DisplayProperty p1 = new DisplayProperty();
        p1.setContainer("1");
        DisplayProperty p2 = new DisplayProperty();
        p2.setContainer("2");

        DisplayProperty.CompareProperties comparator = new DisplayProperty.CompareProperties();
        assertTrue(comparator.compare(p1, p2) < 0);
        assertTrue(comparator.compare(p2, p1) > 0);
        assertEquals(0, comparator.compare(p1, p1));
    }

    @Test
    public void testSetValueHtmlTypeStripsScriptTag() {
        property.setType(Type.HTML);
        property.setValue("<p>safe</p><script>alert('xss')</script>");
        assertFalse(property.getValue().contains("<script>"), "script tag must be removed from html property value");
        assertFalse(property.getReadValue().contains("<script>"), "script tag must be removed from html property readValue");
    }

    @Test
    public void testSetValueHtmlTypeStripsEventHandlers() {
        property.setType(Type.HTML);
        property.setValue("<p onmouseover=\"alert(1)\">text</p>");
        assertFalse(property.getValue().contains("onmouseover"), "event handler attributes must be removed from html property");
        assertFalse(property.getReadValue().contains("onmouseover"), "event handler attributes must be removed from html property readValue");
    }

    @Test
    public void testSetValueHtmlTypeStripsJavascriptHref() {
        property.setType(Type.HTML);
        property.setValue("<a href=\"javascript:alert(1)\">click</a>");
        assertFalse(property.getValue().contains("javascript:"), "javascript: href must be removed from html property");
        assertFalse(property.getReadValue().contains("javascript:"), "javascript: href must be removed from html property readValue");
    }

    @Test
    public void testSetValueHtmlTypePreservesAllowedFormatting() {
        property.setType(Type.HTML);
        property.setValue("<p><b>bold</b> and <i>italic</i> and <em>emphasis</em></p>");
        assertTrue(property.getValue().contains("<b>") || property.getValue().contains("bold"), "safe formatting must be preserved");
    }

    @Test
    public void testSetValueNonHtmlTypeDoesNotSanitize() {
        property.setType(Type.TEXT);
        String raw = "<script>alert(1)</script>";
        property.setValue(raw);
        assertEquals(raw, property.getValue(), "non-html property values must not be modified");
    }

    @Test
    public void testSetValueHtmlTypeWithNullDoesNotThrow() {
        property.setType(Type.HTML);
        property.setValue(null);
        assertNull(property.getValue());
    }
}
