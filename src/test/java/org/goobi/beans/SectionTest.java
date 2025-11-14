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

package org.goobi.beans;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class SectionTest {

    private Section section;

    @Before
    public void setUp() {
        section = new Section();
    }

    @Test
    public void testIsCollapsedWithDefaultValue() {
        assertTrue(section.isCollapsed("foo", true));
        assertFalse(section.isCollapsed("bar", false));
    }

    @Test
    public void testIsCollapsedWithStoredValue() throws Exception {
        // use reflection to access private map
        Map<String, Boolean> map = new HashMap<>();
        map.put("foo", Boolean.TRUE);
        setPrivateField(section, "sections", map);

        assertTrue(section.isCollapsed("foo"));
        assertTrue(section.isCollapsed("foo", false));
    }

    @Test
    public void testToggleCreatesNewEntry() {
        section.toggle("section1");
        // the new entry should now exist and be true since default null toggles to true
        assertTrue(section.isCollapsed("section1"));
    }

    @Test
    public void testToggle() {
        section.toggle("section1"); // false -> true
        boolean first = section.isCollapsed("section1");
        section.toggle("section1");
        boolean second = section.isCollapsed("section1");

        assertNotEquals(first, second);
    }

    @Test
    public void testToggleWithDefaultValue() {
        section.toggle("section", false); // creates it (true)
        boolean before = section.isCollapsed("section");
        section.toggle("section", false); // should invert again
        boolean after = section.isCollapsed("section");

        assertNotEquals(before, after);
    }

    @Test
    public void testMultipleSections() {
        section.toggle("A");
        section.toggle("B");
        section.toggle("A"); // toggle A again

        assertFalse(section.isCollapsed("A"));
        assertTrue(section.isCollapsed("B"));
    }

    /**
     * Utility to set private fields for testing.
     */
    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
