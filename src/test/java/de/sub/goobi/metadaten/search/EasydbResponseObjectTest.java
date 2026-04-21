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
package de.sub.goobi.metadaten.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class EasydbResponseObjectTest {

    @Test
    public void testDefaultConstructor() {
        EasydbResponseObject obj = new EasydbResponseObject();
        assertNotNull(obj);
        assertNotNull(obj.getMetadata());
        assertTrue(obj.getMetadata().isEmpty());
        assertEquals(0, obj.get_system_object_id());
        assertFalse(obj.is_has_children());
        assertEquals(0.0, obj.get_score(), 0.001);
    }

    @Test
    public void testMapConstructorBasicFields() {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.put("_created", "2023-01-01T00:00:00");
        objectMap.put("_mask", "standard");
        objectMap.put("_last_modified", "2023-06-01T00:00:00");
        objectMap.put("_system_object_id", 42);
        objectMap.put("_objecttype", "artefact");
        objectMap.put("_uuid", "uuid-1234");
        objectMap.put("_global_object_id", "global-1");
        objectMap.put("_format", "long");
        objectMap.put("_has_children", false);
        objectMap.put("_score", 1.5);

        // Required: the objecttype-keyed sub-map
        Map<String, Object> artefactData = new LinkedHashMap<>();
        artefactData.put("title", "Test Title");
        artefactData.put("inventory_number", "INV-001");
        objectMap.put("artefact", artefactData);

        EasydbResponseObject obj = new EasydbResponseObject(objectMap);

        assertEquals("2023-01-01T00:00:00", obj.get_created());
        assertEquals("standard", obj.get_mask());
        assertEquals("2023-06-01T00:00:00", obj.get_last_modified());
        assertEquals(42, obj.get_system_object_id());
        assertEquals("artefact", obj.get_objecttype());
        assertEquals("uuid-1234", obj.get_uuid());
        assertEquals("global-1", obj.get_global_object_id());
        assertEquals("long", obj.get_format());
        assertFalse(obj.is_has_children());
        assertEquals(1.5, obj.get_score(), 0.001);

        // metadata map gets populated
        assertNotNull(obj.getMetadata());
        assertEquals("uuid-1234", obj.getMetadata().get("_uuid"));
        assertEquals("global-1", obj.getMetadata().get("_global_object_id"));
        assertEquals("artefact", obj.getMetadata().get("_objecttype"));
        assertEquals("42", obj.getMetadata().get("_system_object_id"));
        // field from objecttype sub-map
        assertEquals("Test Title", obj.getMetadata().get("artefact.title"));
        assertEquals("INV-001", obj.getMetadata().get("artefact.inventory_number"));
    }

    @Test
    public void testMapConstructorWithNestedSubMap() {
        Map<String, Object> objectMap = new LinkedHashMap<>();
        objectMap.put("_objecttype", "item");
        objectMap.put("_uuid", "uuid-999");
        objectMap.put("_global_object_id", "gid-999");
        objectMap.put("_system_object_id", 99);

        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("label", "My Label");
        nested.put("count", 5);

        Map<String, Object> itemData = new LinkedHashMap<>();
        itemData.put("name", "Item Name");
        itemData.put("nested_obj", nested);
        objectMap.put("item", itemData);

        EasydbResponseObject obj = new EasydbResponseObject(objectMap);

        // flat string field gets put in metadata
        assertEquals("Item Name", obj.getMetadata().get("item.name"));
        // nested map is traversed recursively: "nested_obj.label"
        assertEquals("My Label", obj.getMetadata().get("item.nested_obj.label"));
        assertEquals("5", obj.getMetadata().get("item.nested_obj.count"));
    }

    @Test
    public void testSetMetadata() {
        EasydbResponseObject obj = new EasydbResponseObject();
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("key1", "value1");
        obj.setMetadata(meta);
        assertEquals("value1", obj.getMetadata().get("key1"));
    }
}
