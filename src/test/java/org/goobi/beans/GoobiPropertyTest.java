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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.helper.enums.PropertyType;

public class GoobiPropertyTest {

    private GoobiProperty property;

    @Before
    public void setUp() {
        property = new GoobiProperty(PropertyOwnerType.PROCESS);
    }

    @Test
    public void testPropertyOwnerTypeValues() {
        assertEquals(5, PropertyOwnerType.values().length);
    }

    @Test
    public void testPropertyOwnerTypeGetTitle() {
        assertEquals("process", PropertyOwnerType.PROCESS.getTitle());
        assertEquals("user", PropertyOwnerType.USER.getTitle());
        assertEquals("error", PropertyOwnerType.ERROR.getTitle());
        assertEquals("project", PropertyOwnerType.PROJECT.getTitle());
        assertEquals("batch", PropertyOwnerType.BATCH.getTitle());
    }

    @Test
    public void testGetByTitleReturnsCorrectType() {
        assertEquals(PropertyOwnerType.PROCESS, PropertyOwnerType.getByTitle("process"));
        assertEquals(PropertyOwnerType.USER, PropertyOwnerType.getByTitle("user"));
        assertEquals(PropertyOwnerType.BATCH, PropertyOwnerType.getByTitle("batch"));
    }

    @Test
    public void testGetByTitleUnknownFallsBackToProcess() {
        assertEquals(PropertyOwnerType.PROCESS, PropertyOwnerType.getByTitle("unknown"));
    }

    @Test
    public void testGetContainerReturnsZeroWhenNull() {
        // container is null by default
        assertEquals("0", property.getContainer());
    }

    @Test
    public void testSetContainerWithNull() {
        property.setContainer(null);
        assertEquals("0", property.getContainer());
    }

    @Test
    public void testSetAndGetContainer() {
        property.setContainer("5");
        assertEquals("5", property.getContainer());
    }

    @Test
    public void testGetNormalizedTitle() {
        property.setPropertyName("My Title");
        assertEquals("My_Title", property.getNormalizedTitle());
    }

    @Test
    public void testGetNormalizedValue() {
        property.setPropertyValue("some value");
        assertEquals("some_value", property.getNormalizedValue());
    }

    @Test
    public void testGetValueListLazyInitReturnsEmptyList() {
        // getValueList() initializes valueList to an empty ArrayList when null
        java.util.List<String> list = property.getValueList();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testSetAndGetType() {
        property.setType(PropertyType.GENERAL);
        assertEquals(PropertyType.GENERAL, property.getType());
    }

    @Test
    public void testGetTypeDefaultsToStringWhenDataTypeIsNull() {
        // dataType is null → getType() sets dataType to PropertyType.STRING.getId() and returns it
        PropertyType t = property.getType();
        assertNotNull(t);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testLegacyTitelMethods() {
        property.setTitel("LegacyName");
        assertEquals("LegacyName", property.getTitel());
        assertEquals("LegacyName", property.getPropertyName());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testLegacyWertMethods() {
        property.setWert("LegacyValue");
        assertEquals("LegacyValue", property.getWert());
        assertEquals("LegacyValue", property.getPropertyValue());
    }

    @Test
    public void testGetObjectIdWithNullObjectIdAndNullOwner() {
        assertNull(property.getObjectId());
    }
}
