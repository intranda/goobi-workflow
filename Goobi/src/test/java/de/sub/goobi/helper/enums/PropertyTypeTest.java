package de.sub.goobi.helper.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class PropertyTypeTest extends AbstractTest {

    @Test
    public void testGetPropertyTypeByName() {
        assertEquals(PropertyType.unknown, PropertyType.getByName("unknown"));
        assertEquals(PropertyType.String, PropertyType.getByName("something"));

    }

    @Test
    public void testGetPropertyTypeShowInDisplay() {
        assertTrue(PropertyType.String.getShowInDisplay());
        assertFalse(PropertyType.unknown.getShowInDisplay());
    }

    @Test
    public void testGetPropertyTypeGetId() {
        assertEquals(0, PropertyType.unknown.getId());
        assertEquals(1, PropertyType.general.getId());
    }

    @Test
    public void testGetPropertyTypeById() {
        assertEquals(PropertyType.unknown, PropertyType.getById(0));
        assertEquals(PropertyType.general, PropertyType.getById(1));

        assertEquals(PropertyType.String, PropertyType.getById(666));
    }

    @Test
    public void testGetPropertyTypeToString() {
        assertEquals(PropertyType.unknown.getName(), PropertyType.unknown.toString());
    }

}
