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
package org.goobi.production.cli.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderedKeyMapTest {

    private OrderedKeyMap<String, Integer> map;

    @BeforeEach
    public void setUp() {
        map = new OrderedKeyMap<>();
    }

    @Test
    public void testInitiallyEmpty() {
        assertTrue(map.isEmpty());
        assertTrue(map.getKeyList().isEmpty());
    }

    @Test
    public void testPutAddsToKeyList() {
        map.put("a", 1);
        map.put("b", 2);
        List<String> keyList = map.getKeyList();
        assertEquals(2, keyList.size());
        assertEquals("a", keyList.get(0));
        assertEquals("b", keyList.get(1));
    }

    @Test
    public void testPutSameKeyDoesNotDuplicateInKeyList() {
        map.put("a", 1);
        map.put("a", 2);
        assertEquals(1, map.getKeyList().size());
        assertEquals(Integer.valueOf(2), map.get("a"));
    }

    @Test
    public void testGetIndexPositionReturnsInsertionOrder() {
        map.put("first", 1);
        map.put("second", 2);
        map.put("third", 3);
        assertEquals(0, map.getIndexPosition("first"));
        assertEquals(1, map.getIndexPosition("second"));
        assertEquals(2, map.getIndexPosition("third"));
    }

    @Test
    public void testGetIndexPositionForMissingKeyReturnsMinusOne() {
        map.put("a", 1);
        assertEquals(-1, map.getIndexPosition("missing"));
    }

    @Test
    public void testKeySetPreservesInsertionOrder() {
        map.put("z", 1);
        map.put("a", 2);
        map.put("m", 3);
        Set<String> keys = map.keySet();
        List<String> keyList = new ArrayList<>(keys);
        assertEquals("z", keyList.get(0));
        assertEquals("a", keyList.get(1));
        assertEquals("m", keyList.get(2));
    }

    @Test
    public void testRemoveDeletesFromKeyListAndMap() {
        map.put("a", 1);
        map.put("b", 2);
        map.remove("a");
        assertFalse(map.containsKey("a"));
        assertFalse(map.getKeyList().contains("a"));
        assertEquals(1, map.getKeyList().size());
    }

    @Test
    public void testRemoveNonexistentKeyIsNoop() {
        map.put("a", 1);
        assertNull(map.remove("nonexistent"));
        assertEquals(1, map.size());
    }

    @Test
    public void testClearEmptiesMapAndKeyList() {
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        assertTrue(map.isEmpty());
        assertTrue(map.getKeyList().isEmpty());
    }

    @Test
    public void testHashCodeDependsOnKeyList() {
        OrderedKeyMap<String, Integer> map1 = new OrderedKeyMap<>();
        map1.put("a", 1);
        map1.put("b", 2);

        OrderedKeyMap<String, Integer> map2 = new OrderedKeyMap<>();
        map2.put("b", 2);
        map2.put("a", 1);

        // Same entries, different insertion order: different hash
        assertNotEquals(map1.hashCode(), map2.hashCode());
    }

    @Test
    public void testEqualsWithSelfReturnsTrue() {
        map.put("a", 1);
        // Same reference: the object == this branch returns true
        assertTrue(map.equals(map));
    }

    @Test
    public void testEqualsWithNonOrderedKeyMapReturnsFalse() {
        map.put("a", 1);
        // Class mismatch detected before key-list comparison → false
        assertFalse(map.equals(new java.util.HashMap<>()));
    }

    @Test
    public void testEqualsWithSameInsertionOrderReturnsTrue() {
        OrderedKeyMap<String, Integer> other = new OrderedKeyMap<>();
        map.put("a", 1);
        other.put("a", 1);
        assertTrue(map.equals(other));
    }

    @Test
    public void testEqualsWithDifferentInsertionOrderReturnsFalse() {
        OrderedKeyMap<String, Integer> map1 = new OrderedKeyMap<>();
        map1.put("a", 1);
        map1.put("b", 2);

        OrderedKeyMap<String, Integer> map2 = new OrderedKeyMap<>();
        map2.put("b", 2);
        map2.put("a", 1);

        assertFalse(map1.equals(map2));
    }
}
