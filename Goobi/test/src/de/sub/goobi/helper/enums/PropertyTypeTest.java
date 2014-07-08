package de.sub.goobi.helper.enums;

import static org.junit.Assert.*;

import org.junit.Test;

public class PropertyTypeTest {

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
