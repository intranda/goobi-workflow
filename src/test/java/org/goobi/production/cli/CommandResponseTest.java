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
package org.goobi.production.cli;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CommandResponseTest {

    @Test
    public void testTwoArgConstructorSetsStatus200() {
        CommandResponse cr = new CommandResponse("Title", "Message");
        assertEquals(200, cr.getStatus());
    }

    @Test
    public void testTwoArgConstructorSetsTitle() {
        CommandResponse cr = new CommandResponse("MyTitle", "MyMessage");
        assertEquals("MyTitle", cr.getTitle());
    }

    @Test
    public void testTwoArgConstructorSetsMessage() {
        CommandResponse cr = new CommandResponse("MyTitle", "MyMessage");
        assertEquals("MyMessage", cr.getMessage());
    }

    @Test
    public void testThreeArgConstructorSetsStatus() {
        CommandResponse cr = new CommandResponse(404, "Not Found", "Resource not found");
        assertEquals(404, cr.getStatus());
    }

    @Test
    public void testThreeArgConstructorSetsTitle() {
        CommandResponse cr = new CommandResponse(404, "Not Found", "Resource not found");
        assertEquals("Not Found", cr.getTitle());
    }

    @Test
    public void testThreeArgConstructorSetsMessage() {
        CommandResponse cr = new CommandResponse(404, "Not Found", "Resource not found");
        assertEquals("Resource not found", cr.getMessage());
    }

    @Test
    public void testThreeArgConstructorStatus500() {
        CommandResponse cr = new CommandResponse(500, "Error", "Internal error");
        assertEquals(500, cr.getStatus());
    }

    @Test
    public void testThreeArgConstructorStatus401() {
        CommandResponse cr = new CommandResponse(401, "Unauthorized", "No credentials");
        assertEquals(401, cr.getStatus());
        assertEquals("Unauthorized", cr.getTitle());
        assertEquals("No credentials", cr.getMessage());
    }
}
