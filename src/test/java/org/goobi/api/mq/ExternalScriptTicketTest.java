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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ExternalScriptTicketTest {

    @Test
    public void testDefaultValues() {
        ExternalScriptTicket ticket = new ExternalScriptTicket();
        assertNotNull(ticket);
        assertNull(ticket.getProcessId());
        assertNull(ticket.getProcessName());
        assertNull(ticket.getStepId());
        assertNull(ticket.getStepName());
        assertNull(ticket.getJwt());
        assertNull(ticket.getRestJwt());
        assertNull(ticket.getScriptNames());
        assertNull(ticket.getScripts());
        assertEquals(0, ticket.getNumberOfObjects());
    }

    @Test
    public void testSettersAndGetters() {
        ExternalScriptTicket ticket = new ExternalScriptTicket();
        ticket.setProcessId(10);
        ticket.setProcessName("TestProcess");
        ticket.setStepId(20);
        ticket.setStepName("RunScript");
        ticket.setJwt("jwt-token");
        ticket.setRestJwt("rest-jwt-token");
        ticket.setNumberOfObjects(3);

        List<String> scriptNames = Arrays.asList("script1", "script2");
        ticket.setScriptNames(scriptNames);

        assertEquals(Integer.valueOf(10), ticket.getProcessId());
        assertEquals("TestProcess", ticket.getProcessName());
        assertEquals(Integer.valueOf(20), ticket.getStepId());
        assertEquals("RunScript", ticket.getStepName());
        assertEquals("jwt-token", ticket.getJwt());
        assertEquals("rest-jwt-token", ticket.getRestJwt());
        assertEquals(3, ticket.getNumberOfObjects());
        assertEquals(2, ticket.getScriptNames().size());
    }
}
