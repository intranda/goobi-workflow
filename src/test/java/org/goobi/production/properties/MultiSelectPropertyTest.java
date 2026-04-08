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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class MultiSelectPropertyTest {

    private MultiSelectProperty<String> multiSelect;
    private List<String> selectedValues;

    @Before
    public void setUp() {
        selectedValues = new ArrayList<>();
        multiSelect = new MultiSelectProperty<String>() {
            @Override
            public List<String> getSelectValues() {
                return Arrays.asList("Alpha", "Beta", "Gamma");
            }

            @Override
            public List<String> getAllSelectedValues() {
                return selectedValues;
            }
        };
    }

    @Test
    public void testGetCurrentValueReturnsEmpty() {
        assertEquals("", multiSelect.getCurrentValue());
    }

    @Test
    public void testSetCurrentValueAddsToSelected() {
        multiSelect.setCurrentValue("Alpha");
        assertTrue(selectedValues.contains("Alpha"));
    }

    @Test
    public void testSetCurrentValueBlankDoesNotAdd() {
        multiSelect.setCurrentValue("");
        assertTrue(selectedValues.isEmpty());
    }

    @Test
    public void testSetCurrentValueNullDoesNotAdd() {
        multiSelect.setCurrentValue(null);
        assertTrue(selectedValues.isEmpty());
    }

    @Test
    public void testGetPossibleValuesExcludesSelected() {
        selectedValues.add("Alpha");
        List<String> possible = multiSelect.getPossibleValues();
        assertFalse(possible.contains("Alpha"));
        assertTrue(possible.contains("Beta"));
        assertTrue(possible.contains("Gamma"));
    }

    @Test
    public void testGetPossibleValuesAllAvailableWhenNothingSelected() {
        List<String> possible = multiSelect.getPossibleValues();
        assertEquals(3, possible.size());
    }

    @Test
    public void testGetPossibleValuesEmptyWhenAllSelected() {
        selectedValues.add("Alpha");
        selectedValues.add("Beta");
        selectedValues.add("Gamma");
        List<String> possible = multiSelect.getPossibleValues();
        assertTrue(possible.isEmpty());
    }

    @Test
    public void testRemoveSelectedValue() {
        selectedValues.add("Alpha");
        selectedValues.add("Beta");
        multiSelect.removeSelectedValue("Alpha");
        assertFalse(selectedValues.contains("Alpha"));
        assertTrue(selectedValues.contains("Beta"));
    }

    @Test
    public void testRemoveSelectedValueNotPresentDoesNotFail() {
        multiSelect.removeSelectedValue("Unknown");
        assertTrue(selectedValues.isEmpty());
    }
}
