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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public class GoobiPathFileComparatorTest {

    private final GoobiPathFileComparator comparator = new GoobiPathFileComparator();

    @Test
    public void testEqualPathsReturnZero() {
        Path p1 = Path.of("/images/001.jpg");
        Path p2 = Path.of("/images/001.jpg");
        assertEquals(0, comparator.compare(p1, p2));
    }

    @Test
    public void testNumericOrderingSmaller() {
        Path p1 = Path.of("/images/1.jpg");
        Path p2 = Path.of("/images/10.jpg");
        assertTrue(comparator.compare(p1, p2) < 0);
    }

    @Test
    public void testNumericOrderingLarger() {
        Path p1 = Path.of("/images/10.jpg");
        Path p2 = Path.of("/images/2.jpg");
        assertTrue(comparator.compare(p1, p2) > 0);
    }

    @Test
    public void testSemanticEqualityIgnoresExtension() {
        Path p1 = Path.of("/images/001.jpg");
        Path p2 = Path.of("/images/1.tif");
        assertEquals(0, comparator.compare(p1, p2));
    }

    @Test
    public void testMixedAlphaNumeric() {
        Path p1 = Path.of("/images/img001.tif");
        Path p2 = Path.of("/images/img010.tif");
        assertTrue(comparator.compare(p1, p2) < 0);
    }
}
