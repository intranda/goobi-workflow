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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EasydbSearchRequestTest {

    private EasydbSearchRequest request;

    @BeforeEach
    public void setUp() {
        request = new EasydbSearchRequest();
    }

    @Test
    public void testDefaultValues() {
        assertNotNull(request);
        assertEquals(0, request.getOffset());
        assertEquals(10, request.getLimit());
        assertFalse(request.isGenerate_rights());
        assertNotNull(request.getSearch());
        assertTrue(request.getSearch().isEmpty());
        assertEquals("long", request.getFormat());
        assertNotNull(request.getSort());
        assertTrue(request.getSort().isEmpty());
        assertNotNull(request.getObjecttypes());
        assertTrue(request.getObjecttypes().isEmpty());
    }

    @Test
    public void testSetOffset() {
        request.setOffset(20);
        assertEquals(20, request.getOffset());
    }

    @Test
    public void testSetLimit() {
        request.setLimit(50);
        assertEquals(50, request.getLimit());
    }

    @Test
    public void testSetFormat() {
        request.setFormat("short");
        assertEquals("short", request.getFormat());
    }

    @Test
    public void testAddSearchField() {
        EasydbSearchField field = new EasydbSearchField();
        field.setType("match");
        request.getSearch().add(field);
        assertEquals(1, request.getSearch().size());
        assertEquals("match", request.getSearch().get(0).getType());
    }

    @Test
    public void testAddObjecttype() {
        request.getObjecttypes().add("artefact");
        assertEquals(1, request.getObjecttypes().size());
        assertEquals("artefact", request.getObjecttypes().get(0));
    }

    @Test
    public void testSetIncludeFields() {
        request.setInclude_fields(Arrays.asList("field1", "field2"));
        assertEquals(2, request.getInclude_fields().size());
    }

    @Test
    public void testAddSortField() {
        EasydbSortField sortField = new EasydbSortField();
        sortField.setField("name");
        request.getSort().add(sortField);
        assertEquals(1, request.getSort().size());
        assertEquals("name", request.getSort().get(0).getField());
    }
}
