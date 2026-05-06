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

package org.goobi.pagination;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class RomanNumberSequenceTest extends AbstractTest {

    @Test
    public void generatesSequenceOfRomanNumbers() {
        RomanNumberSequence rns = new RomanNumberSequence(1, 10);
        assertEquals("Wrong sequence", "[I, II, III, IV, V, VI, VII, VIII, IX, X]", rns.toString());
    }

    @Test
    public void generatesSequenceOfRomanNumbersWithIncrement() {
        RomanNumberSequence rns = new RomanNumberSequence(1, 50, 10);
        assertEquals("Wrong sequence", "[I, XI, XXI, XXXI, XLI]", rns.toString());
    }

    @Test
    public void singleElementSequenceWhenStartEqualsEnd() {
        RomanNumberSequence rns = new RomanNumberSequence(1, 1);
        assertEquals(1, rns.size());
        assertEquals("I", rns.get(0));
    }

    @Test
    public void sequenceFromOneToFiveHasFiveElements() {
        RomanNumberSequence rns = new RomanNumberSequence(1, 5);
        assertEquals(5, rns.size());
    }

    @Test
    public void firstElementIsCorrectRomanNumeral() {
        RomanNumberSequence rns = new RomanNumberSequence(4, 6);
        assertEquals("IV", rns.get(0));
    }

    @Test
    public void lastElementIsCorrectRomanNumeral() {
        RomanNumberSequence rns = new RomanNumberSequence(4, 6);
        assertEquals("VI", rns.get(rns.size() - 1));
    }

    @Test
    public void subtractiveNotationIV() {
        RomanNumberSequence rns = new RomanNumberSequence(4, 4);
        assertEquals("IV", rns.get(0));
    }

    @Test
    public void subtractiveNotationIX() {
        RomanNumberSequence rns = new RomanNumberSequence(9, 9);
        assertEquals("IX", rns.get(0));
    }

    @Test
    public void subtractiveNotationXL() {
        RomanNumberSequence rns = new RomanNumberSequence(40, 40);
        assertEquals("XL", rns.get(0));
    }

    @Test
    public void subtractiveNotationXC() {
        RomanNumberSequence rns = new RomanNumberSequence(90, 90);
        assertEquals("XC", rns.get(0));
    }

    @Test
    public void incrementSequenceHasCorrectSize() {
        // 2, 4, 6, 8, 10 → 5 elements
        RomanNumberSequence rns = new RomanNumberSequence(2, 10, 2);
        assertEquals(5, rns.size());
    }

}
