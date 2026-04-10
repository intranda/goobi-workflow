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
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class DocketTest {

    private Docket docket;

    @Before
    public void setUp() {
        docket = new Docket();
    }

    @Test
    public void testDefaultIdIsNull() {
        assertNull(docket.getId());
    }

    @Test
    public void testSetAndGetId() {
        docket.setId(5);
        assertEquals(Integer.valueOf(5), docket.getId());
    }

    @Test
    public void testSetAndGetName() {
        docket.setName("Standard Docket");
        assertEquals("Standard Docket", docket.getName());
    }

    @Test
    public void testSetAndGetFile() {
        docket.setFile("docket.xsl");
        assertEquals("docket.xsl", docket.getFile());
    }

    @Test
    public void testLazyLoadDoesNotThrow() {
        docket.lazyLoad(); // must not throw
    }
}
