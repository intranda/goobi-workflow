package org.goobi.api.display;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ItemTest {

    @Test
    public void testConstructor() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
    }

    @Test
    public void testLabel() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
        String fixture = "fixture";
        item.setLabel(fixture);
        assertEquals(fixture, item.getLabel());
    }

    @Test
    public void testValue() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
        String fixture = "fixture";
        item.setValue(fixture);
        assertEquals(fixture, item.getValue());
    }

    @Test
    public void testSource() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
        String fixture = "fixture";
        item.setSource(fixture);
        assertEquals(fixture, item.getSource());
    }

    @Test
    public void testField() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
        String fixture = "fixture";
        item.setField(fixture);
        assertEquals(fixture, item.getField());
    }

    @Test
    public void testSelection() {
        Item item = new Item("", "", false, "", "");
        assertNotNull(item);
        assertFalse(item.isSelected());
        item.setSelected(true);
        assertTrue(item.isSelected());
    }

}
