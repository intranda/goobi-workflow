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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.easymock.EasyMock;
import org.goobi.production.properties.DisplayProperty;
import org.junit.jupiter.api.Test;

public class PropertyListObjectTest {

    @Test
    public void testDefaultConstructor() {
        PropertyListObject obj = new PropertyListObject();
        assertNotNull(obj);
        assertEquals("0", obj.getContainerNumber());
        assertNotNull(obj.getPropertyList());
        assertTrue(obj.getPropertyList().isEmpty());
    }

    @Test
    public void testContainerConstructor() {
        PropertyListObject obj = new PropertyListObject("5");
        assertEquals("5", obj.getContainerNumber());
        assertTrue(obj.getPropertyList().isEmpty());
    }

    @Test
    public void testAddToList() {
        PropertyListObject obj = new PropertyListObject();
        DisplayProperty prop = EasyMock.createMock(DisplayProperty.class);
        EasyMock.replay(prop);
        obj.addToList(prop);
        assertEquals(1, obj.getPropertyListSize());
    }

    @Test
    public void testGetPropertyListSize() {
        PropertyListObject obj = new PropertyListObject();
        assertEquals(0, obj.getPropertyListSize());
        DisplayProperty prop1 = EasyMock.createMock(DisplayProperty.class);
        DisplayProperty prop2 = EasyMock.createMock(DisplayProperty.class);
        EasyMock.replay(prop1, prop2);
        obj.addToList(prop1);
        obj.addToList(prop2);
        assertEquals(2, obj.getPropertyListSize());
    }

    @Test
    public void testGetPropertyListSizeString() {
        PropertyListObject obj = new PropertyListObject();
        assertEquals("0", obj.getPropertyListSizeString());
        DisplayProperty prop = EasyMock.createMock(DisplayProperty.class);
        EasyMock.replay(prop);
        obj.addToList(prop);
        assertEquals("1", obj.getPropertyListSizeString());
    }
}
