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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EasydbSearchResponseTest {

    private EasydbSearchResponse response;

    @Before
    public void setUp() {
        response = new EasydbSearchResponse();
    }

    @Test
    public void testDefaultValues() {
        assertNotNull(response);
        assertNull(response.getFormat());
        assertNull(response.getType());
        assertEquals(0, response.getOffset());
        assertEquals(0, response.getLimit());
        assertEquals(0, response.getCount());
        assertNotNull(response.getConvertedObjects());
        assertTrue(response.getConvertedObjects().isEmpty());
    }

    @Test
    public void testSetFormat() {
        response.setFormat("long");
        assertEquals("long", response.getFormat());
    }

    @Test
    public void testSetCount() {
        response.setCount(42);
        assertEquals(42, response.getCount());
    }

    @Test
    public void testSetOffset() {
        response.setOffset(10);
        assertEquals(10, response.getOffset());
    }

    @Test
    public void testSetLimit() {
        response.setLimit(20);
        assertEquals(20, response.getLimit());
    }

    @Test
    public void testAddConvertedObject() {
        EasydbResponseObject obj = new EasydbResponseObject();
        response.getConvertedObjects().add(obj);
        assertEquals(1, response.getConvertedObjects().size());
    }

    @Test
    public void testSetLanguage() {
        response.setLanguage("de-DE");
        assertEquals("de-DE", response.getLanguage());
    }
}
