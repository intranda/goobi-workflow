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
 * 
 */
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class MasterpieceTest extends AbstractTest {

    private static Masterpiece masterpiece1;
    private static Masterpiece masterpiece2;
    private static Masterpiece masterpiece3;

    @BeforeClass
    public static void prepareMasterpieceObjects() {
        MasterpieceTest.masterpiece1 = new Masterpiece();
        MasterpieceTest.masterpiece2 = new Masterpiece();
        MasterpieceTest.masterpiece3 = new Masterpiece();
        // masterpiece1 and masterpiece2 are equal, masterpiece3 differs
        MasterpieceTest.masterpiece1.setId(1);
        MasterpieceTest.masterpiece2.setId(1);
        MasterpieceTest.masterpiece3.setId(2);
    }

    @Test
    public void testHashCode() {
        // The hash code is only calculated with the masterpiece ID
        assertEquals(MasterpieceTest.masterpiece1.hashCode(), MasterpieceTest.masterpiece1.hashCode());
        assertEquals(MasterpieceTest.masterpiece1.hashCode(), MasterpieceTest.masterpiece2.hashCode());
        assertNotEquals(MasterpieceTest.masterpiece2.hashCode(), MasterpieceTest.masterpiece3.hashCode());
    }

    @Test
    public void testEquals() {
        // The equals method only uses the masterpiece ID:
        // equal id -> equal object
        // different id -> other object
        // masterpiece.equals() is called by junit internally
        assertEquals(MasterpieceTest.masterpiece1, MasterpieceTest.masterpiece1);
        assertEquals(MasterpieceTest.masterpiece1, MasterpieceTest.masterpiece2);
        assertNotEquals(MasterpieceTest.masterpiece2, MasterpieceTest.masterpiece3);
    }
}
