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
package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ErrorResponseTest {

    @Test
    public void testDefaultValues() {
        ErrorResponse response = new ErrorResponse();
        assertNotNull(response);
        assertNull(response.getResult());
        assertNull(response.getErrorText());
    }

    @Test
    public void testSettersAndGetters() {
        ErrorResponse response = new ErrorResponse();
        response.setResult("error");
        response.setErrorText("Something went wrong");

        assertEquals("error", response.getResult());
        assertEquals("Something went wrong", response.getErrorText());
    }

    @Test
    public void testEquality() {
        ErrorResponse r1 = new ErrorResponse();
        r1.setResult("error");
        r1.setErrorText("fail");

        ErrorResponse r2 = new ErrorResponse();
        r2.setResult("error");
        r2.setErrorText("fail");

        assertEquals(r1, r2);
    }
}
