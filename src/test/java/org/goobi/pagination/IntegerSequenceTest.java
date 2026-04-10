/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 *
 * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

//
package org.goobi.pagination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class IntegerSequenceTest extends AbstractTest {

    @Test
    public void sequenceWithoutRangeIsEmpty() {

        IntegerSequence seq = new IntegerSequence();
        assertTrue(seq.isEmpty());

    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIllegalArgumentExceptionWhenStartIsBelowEnd() {

        new IntegerSequence(5, 1);

    }

    @Test
    public void sequenceFromOneToFiveHasFiveElements() {

        IntegerSequence seq = new IntegerSequence(1, 5);
        assertEquals(5, seq.size());

    }

    @Test
    public void sequenceIsStrictlyIncreasing() {

        IntegerSequence seq = new IntegerSequence(1, 5);

        int last = 0;
        for (int i : seq) {
            assertTrue(i > last);
            last = i;
        }

    }

    @Test
    public void differenceBetweenElementsEqualsIncrement() {

        IntegerSequence seq = new IntegerSequence(1, 5, 2);

        int last = -1;
        for (int i : seq) {
            assertTrue("Difference between elements should be equal to increment (2)", (last + 2) == i);
            last = i;
        }

    }

    @Test
    public void sequenceWithStartEqualsEndHasSingleElement() {
        IntegerSequence seq = new IntegerSequence(7, 7);
        assertEquals(1, seq.size());
        assertEquals(Integer.valueOf(7), seq.get(0));
    }

    @Test
    public void firstElementEqualsStart() {
        IntegerSequence seq = new IntegerSequence(3, 10);
        assertEquals(Integer.valueOf(3), seq.get(0));
    }

    @Test
    public void lastElementEqualsEnd() {
        IntegerSequence seq = new IntegerSequence(1, 8);
        assertEquals(Integer.valueOf(8), seq.get(seq.size() - 1));
    }

    @Test
    public void sequenceWithIncrementHasCorrectSize() {
        // 1,3,5,7,9 → 5 elements
        IntegerSequence seq = new IntegerSequence(1, 9, 2);
        assertEquals(5, seq.size());
    }

    @Test
    public void sequenceWithIncrementLargerThanRangeContainsOnlyStart() {
        IntegerSequence seq = new IntegerSequence(1, 5, 10);
        assertEquals(1, seq.size());
        assertEquals(Integer.valueOf(1), seq.get(0));
    }

    @Test
    public void sequenceContainsExpectedValues() {
        IntegerSequence seq = new IntegerSequence(2, 6);
        assertEquals(Integer.valueOf(2), seq.get(0));
        assertEquals(Integer.valueOf(3), seq.get(1));
        assertEquals(Integer.valueOf(4), seq.get(2));
        assertEquals(Integer.valueOf(5), seq.get(3));
        assertEquals(Integer.valueOf(6), seq.get(4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwIllegalArgumentExceptionWithIncrementWhenStartIsBelowEnd() {
        new IntegerSequence(10, 1, 2);
    }

}
