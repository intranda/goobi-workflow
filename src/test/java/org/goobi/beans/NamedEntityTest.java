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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NamedEntityTest {

    @Test
    public void testNoArgsConstructorCreatesEmptyEntity() {
        NamedEntity e = new NamedEntity();
        assertNull(e.getId());
        assertNull(e.getLabel());
    }

    @Test
    public void testAllArgsConstructorSetsAllFields() {
        NamedEntity e = new NamedEntity("id1", "Label", "PER", "http://example.com");
        assertEquals("id1", e.getId());
        assertEquals("Label", e.getLabel());
        assertEquals("PER", e.getType());
        assertEquals("http://example.com", e.getUri());
    }

    @Test
    public void testSetters() {
        NamedEntity e = new NamedEntity();
        e.setId("x");
        e.setLabel("y");
        e.setType("LOC");
        e.setUri("http://loc.example");
        assertEquals("x", e.getId());
        assertEquals("y", e.getLabel());
        assertEquals("LOC", e.getType());
        assertEquals("http://loc.example", e.getUri());
    }

    @Test
    public void testEqualsBasedOnId() {
        NamedEntity a = new NamedEntity("id1", "A", "PER", "http://a");
        NamedEntity b = new NamedEntity("id1", "B", "LOC", "http://b");
        assertTrue(a.equals(b));
    }

    @Test
    public void testNotEqualForDifferentId() {
        NamedEntity a = new NamedEntity("id1", "A", "PER", "http://a");
        NamedEntity b = new NamedEntity("id2", "A", "PER", "http://a");
        assertFalse(a.equals(b));
    }

    @Test
    public void testNotEqualToNull() {
        NamedEntity e = new NamedEntity("id1", "A", "PER", "http://a");
        assertFalse(e.equals(null));
    }

    @Test
    public void testNotEqualToDifferentClass() {
        NamedEntity e = new NamedEntity("id1", "A", "PER", "http://a");
        assertFalse("id1".equals(e));
    }

    @Test
    public void testHashCodeWithNonNullId() {
        NamedEntity e = new NamedEntity("id1", "A", "PER", "http://a");
        assertEquals("id1".hashCode(), e.hashCode());
    }

    @Test
    public void testHashCodeWithNullIdIsZero() {
        NamedEntity e = new NamedEntity();
        assertEquals(0, e.hashCode());
    }

    @Test
    public void testEqualEntitiesHaveSameHashCode() {
        NamedEntity a = new NamedEntity("same", "A", "PER", "http://a");
        NamedEntity b = new NamedEntity("same", "B", "LOC", "http://b");
        assertEquals(a.hashCode(), b.hashCode());
    }
}
