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
package de.unigoettingen.sub.search.opac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class QueryTest {

    @Test
    public void testDefaultConstructor() {
        Query query = new Query();
        assertNotNull(query);
        assertNull(query.getQueryUrl());
    }

    @Test
    public void testConstants() {
        assertEquals("*", Query.AND);
        assertEquals("%2B", Query.OR);
        assertEquals("-", Query.NOT);
    }

    @Test
    public void testTwoArgConstructorBuildsFirstTermUrl() {
        Query query = new Query("testvalue", "12");
        String url = query.getQueryUrl();
        assertNotNull(url);
        assertTrue(url.contains("SRCH"));
        assertTrue(url.contains("IKT0=12"));
        assertTrue(url.contains("TRM0=testvalue"));
    }

    @Test
    public void testAddQueryFirstTermUsesSrch() {
        Query query = new Query();
        query.addQuery(null, "hello", "4");
        String url = query.getQueryUrl();
        assertTrue(url.contains("ACT0=SRCH"));
        assertTrue(url.contains("IKT0=4"));
        assertTrue(url.contains("TRM0=hello"));
    }

    @Test
    public void testAddQuerySubsequentTermUsesOperation() {
        Query query = new Query("first", "12");
        query.addQuery(Query.AND, "second", "8");
        String url = query.getQueryUrl();
        // first term
        assertTrue(url.contains("ACT0=SRCH"));
        assertTrue(url.contains("IKT0=12"));
        assertTrue(url.contains("TRM0=first"));
        // second term
        assertTrue(url.contains("ACT1=" + Query.AND));
        assertTrue(url.contains("IKT1=8"));
        assertTrue(url.contains("TRM1=second"));
    }

    @Test
    public void testAddQueryWithOrOperation() {
        Query query = new Query("first", "12");
        query.addQuery(Query.OR, "second", "8");
        String url = query.getQueryUrl();
        assertTrue(url.contains("ACT1=" + Query.OR));
    }

    @Test
    public void testAddQueryWithNotOperation() {
        Query query = new Query("first", "12");
        query.addQuery(Query.NOT, "second", "8");
        String url = query.getQueryUrl();
        assertTrue(url.contains("ACT1=" + Query.NOT));
    }

}
