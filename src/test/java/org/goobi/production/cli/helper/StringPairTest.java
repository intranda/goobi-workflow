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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringPairTest {

    @Test
    public void testNoArgsConstructorCreatesEmptyPair() {
        StringPair pair = new StringPair();
        assertNull(pair.getOne());
        assertNull(pair.getTwo());
    }

    @Test
    public void testAllArgsConstructorSetsFields() {
        StringPair pair = new StringPair("key", "value");
        assertEquals("key", pair.getOne());
        assertEquals("value", pair.getTwo());
    }

    @Test
    public void testSetOne() {
        StringPair pair = new StringPair();
        pair.setOne("alpha");
        assertEquals("alpha", pair.getOne());
    }

    @Test
    public void testSetTwo() {
        StringPair pair = new StringPair();
        pair.setTwo("beta");
        assertEquals("beta", pair.getTwo());
    }

    @Test
    public void testEqualsAndHashCode() {
        StringPair a = new StringPair("k", "v");
        StringPair b = new StringPair("k", "v");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testNotEqualWhenDifferentOne() {
        StringPair a = new StringPair("k1", "v");
        StringPair b = new StringPair("k2", "v");
        assertNotEquals(a, b);
    }

    @Test
    public void testToStringContainsFields() {
        StringPair pair = new StringPair("myKey", "myValue");
        String s = pair.toString();
        assertNotNull(s);
        assertTrue(s.contains("myKey"));
        assertTrue(s.contains("myValue"));
    }

    @Test
    public void testOneComparatorAscending() {
        StringPair a = new StringPair("apple", "z");
        StringPair b = new StringPair("banana", "a");
        StringPair.OneComparator comparator = new StringPair.OneComparator();
        assertTrue(comparator.compare(a, b) < 0);
        assertTrue(comparator.compare(b, a) > 0);
        assertEquals(0, comparator.compare(a, a));
    }

    @Test
    public void testOneComparatorSortsList() {
        List<StringPair> list = Arrays.asList(
                new StringPair("cherry", "1"),
                new StringPair("apple", "2"),
                new StringPair("banana", "3"));
        list.sort(new StringPair.OneComparator());
        assertEquals("apple", list.get(0).getOne());
        assertEquals("banana", list.get(1).getOne());
        assertEquals("cherry", list.get(2).getOne());
    }

    @Test
    public void testTwoComparatorAscending() {
        StringPair a = new StringPair("z", "apple");
        StringPair b = new StringPair("a", "banana");
        StringPair.TwoComparator comparator = new StringPair.TwoComparator();
        assertTrue(comparator.compare(a, b) < 0);
        assertTrue(comparator.compare(b, a) > 0);
        assertEquals(0, comparator.compare(a, a));
    }

    @Test
    public void testTwoComparatorSortsList() {
        List<StringPair> list = Arrays.asList(
                new StringPair("1", "cherry"),
                new StringPair("2", "apple"),
                new StringPair("3", "banana"));
        list.sort(new StringPair.TwoComparator());
        assertEquals("apple", list.get(0).getTwo());
        assertEquals("banana", list.get(1).getTwo());
        assertEquals("cherry", list.get(2).getTwo());
    }
}
