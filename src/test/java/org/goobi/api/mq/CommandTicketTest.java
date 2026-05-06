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
package org.goobi.api.mq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CommandTicketTest {

    @Test
    public void testDefaultValues() {
        CommandTicket ticket = new CommandTicket();
        assertNotNull(ticket);
        assertNull(ticket.getProcessId());
        assertNull(ticket.getProcessName());
        assertNull(ticket.getStepId());
        assertNull(ticket.getStepName());
        assertNull(ticket.getCommand());
        assertNull(ticket.getJwt());
        assertNull(ticket.getNewStatus());
        assertNull(ticket.getScriptNames());
        assertNull(ticket.getLogType());
        assertNull(ticket.getIssuer());
        assertNull(ticket.getContent());
        assertEquals(0, ticket.getNumberOfObjects());
        assertNull(ticket.getTicketType());
    }

    @Test
    public void testSettersAndGetters() {
        CommandTicket ticket = new CommandTicket();
        ticket.setProcessId(1);
        ticket.setProcessName("MyProcess");
        ticket.setStepId(2);
        ticket.setStepName("Validation");
        ticket.setCommand("closeStep");
        ticket.setJwt("my-jwt");
        ticket.setNewStatus("done");
        ticket.setLogType("info");
        ticket.setIssuer("admin");
        ticket.setContent("all good");
        ticket.setNumberOfObjects(5);
        ticket.setTicketType("command");

        assertEquals(Integer.valueOf(1), ticket.getProcessId());
        assertEquals("MyProcess", ticket.getProcessName());
        assertEquals(Integer.valueOf(2), ticket.getStepId());
        assertEquals("Validation", ticket.getStepName());
        assertEquals("closeStep", ticket.getCommand());
        assertEquals("my-jwt", ticket.getJwt());
        assertEquals("done", ticket.getNewStatus());
        assertEquals("info", ticket.getLogType());
        assertEquals("admin", ticket.getIssuer());
        assertEquals("all good", ticket.getContent());
        assertEquals(5, ticket.getNumberOfObjects());
        assertEquals("command", ticket.getTicketType());
    }
}
