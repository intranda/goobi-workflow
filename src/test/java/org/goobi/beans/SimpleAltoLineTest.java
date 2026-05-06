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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleAltoLineTest {

    private SimpleAltoLine line;

    @BeforeEach
    public void setUp() {
        line = new SimpleAltoLine();
    }

    @Test
    public void testDefaultIdIsNull() {
        assertNull(line.getId());
    }

    @Test
    public void testSetAndGetId() {
        line.setId("L001");
        assertEquals("L001", line.getId());
    }

    @Test
    public void testSetAndGetWords() {
        SimpleAltoWord w1 = new SimpleAltoWord();
        w1.setValue("foo");
        SimpleAltoWord w2 = new SimpleAltoWord();
        w2.setValue("bar");
        List<SimpleAltoWord> words = Arrays.asList(w1, w2);
        line.setWords(words);
        assertEquals(2, line.getWords().size());
        assertEquals("foo", line.getWords().get(0).getValue());
    }

    @Test
    public void testSetAndGetCoordinates() {
        line.setX(5);
        line.setY(15);
        line.setWidth(200);
        line.setHeight(25);
        assertEquals(5, line.getX());
        assertEquals(15, line.getY());
        assertEquals(200, line.getWidth());
        assertEquals(25, line.getHeight());
    }
}
