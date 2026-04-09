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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class AuthorityDataTest {

    @Test
    public void testConstructorSetsValue() {
        AuthorityData data = new AuthorityData("Goethe", "http://d-nb.info/gnd/118540238");
        assertEquals("Goethe", data.getValue());
    }

    @Test
    public void testConstructorSetsUri() {
        AuthorityData data = new AuthorityData("Goethe", "http://d-nb.info/gnd/118540238");
        assertEquals("http://d-nb.info/gnd/118540238", data.getUri());
    }

    @Test
    public void testEqualsForSameContent() {
        AuthorityData a = new AuthorityData("A", "http://example.com/1");
        AuthorityData b = new AuthorityData("A", "http://example.com/1");
        assertEquals(a, b);
    }

    @Test
    public void testNotEqualForDifferentValue() {
        AuthorityData a = new AuthorityData("A", "http://example.com/1");
        AuthorityData b = new AuthorityData("B", "http://example.com/1");
        assertNotEquals(a, b);
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        AuthorityData a = new AuthorityData("X", "http://example.com");
        AuthorityData b = new AuthorityData("X", "http://example.com");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToStringContainsFields() {
        AuthorityData data = new AuthorityData("Schiller", "http://example.com/2");
        assertNotNull(data.toString());
    }
}
