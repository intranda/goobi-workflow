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
package org.goobi.production.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class RecordTest {

    private Record record;

    @Before
    public void setUp() {
        record = new Record();
    }

    @Test
    public void testDefaultDataIsEmpty() {
        assertEquals("", record.getData());
    }

    @Test
    public void testDefaultIdIsEmpty() {
        assertEquals("", record.getId());
    }

    @Test
    public void testDefaultCollectionsIsEmptyList() {
        assertNotNull(record.getCollections());
        assertTrue(record.getCollections().isEmpty());
    }

    @Test
    public void testDefaultObjectIsNull() {
        assertNull(record.getObject());
    }

    @Test
    public void testSetData() {
        record.setData("<xml>content</xml>");
        assertEquals("<xml>content</xml>", record.getData());
    }

    @Test
    public void testSetId() {
        record.setId("ABC-123");
        assertEquals("ABC-123", record.getId());
    }

    @Test
    public void testSetCollections() {
        record.setCollections(Arrays.asList("col1", "col2"));
        assertEquals(2, record.getCollections().size());
        assertEquals("col1", record.getCollections().get(0));
        assertEquals("col2", record.getCollections().get(1));
    }

    @Test
    public void testSetObject() {
        Object obj = new Object();
        record.setObject(obj);
        assertEquals(obj, record.getObject());
    }
}
