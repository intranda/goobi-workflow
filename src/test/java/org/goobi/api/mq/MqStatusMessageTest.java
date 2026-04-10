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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.goobi.api.mq.MqStatusMessage.MessageStatus;
import org.junit.Test;

public class MqStatusMessageTest {

    @Test
    public void testConstructor() {
        Date now = new Date();
        MqStatusMessage msg = new MqStatusMessage("ticket-123", now, MessageStatus.DONE,
                "success", "{}", 1, "command", 42, 7, "myTicket");
        assertNotNull(msg);
        assertEquals("ticket-123", msg.getTicketId());
        assertEquals(now, msg.getTime());
        assertEquals(MessageStatus.DONE, msg.getStatus());
        assertEquals("success", msg.getStatusMessage());
        assertEquals("{}", msg.getOriginalMessage());
        assertEquals(1, msg.getNumberOfObjects());
        assertEquals("command", msg.getTicketType());
        assertEquals(42, msg.getProcessid());
        assertEquals(7, msg.getStepId());
        assertEquals("myTicket", msg.getTicketName());
    }

    @Test
    public void testMessageStatusDone() {
        assertEquals("DONE", MessageStatus.DONE.getName());
        assertEquals("DONE", MessageStatus.DONE.toString());
    }

    @Test
    public void testMessageStatusError() {
        assertEquals("ERROR", MessageStatus.ERROR.getName());
        assertEquals("ERROR", MessageStatus.ERROR.toString());
    }

    @Test
    public void testMessageStatusErrorDlq() {
        assertEquals("ERROR_DLQ", MessageStatus.ERROR_DLQ.getName());
        assertEquals("ERROR_DLQ", MessageStatus.ERROR_DLQ.toString());
    }

    @Test
    public void testLazyLoadDoesNothing() {
        Date now = new Date();
        MqStatusMessage msg = new MqStatusMessage("id", now, MessageStatus.DONE, "", "", 0, "", 0, 0, "");
        msg.lazyLoad(); // must not throw
    }
}
