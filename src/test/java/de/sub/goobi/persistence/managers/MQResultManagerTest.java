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
package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.api.mq.MqStatusMessage;
import org.goobi.beans.DatabaseObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MQResultMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class MQResultManagerTest extends AbstractTest {

    private MqStatusMessage sampleMessage;

    @Before
    public void setUp() throws Exception {
        sampleMessage = new MqStatusMessage("ticket-1", new Date(), MqStatusMessage.MessageStatus.DONE, "Done", "original", 0, "testQueue", 1, 1,
                "ticketName");

        PowerMock.mockStatic(MQResultMysqlHelper.class);
        EasyMock.expect(MQResultMysqlHelper.getMessagesCount(EasyMock.anyString())).andReturn(5).anyTimes();
        EasyMock.expect(MQResultMysqlHelper.getMessageList(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt()))
                .andReturn(java.util.Collections.emptyList())
                .anyTimes();
        MQResultMysqlHelper.insertMessage(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(MQResultMysqlHelper.getAllTicketNames()).andReturn(Arrays.asList("ticket-1", "ticket-2")).anyTimes();
        PowerMock.replay(MQResultMysqlHelper.class);
    }

    @Test
    public void testGetHitSize() throws Exception {
        MQResultManager manager = new MQResultManager();
        assertEquals(5, manager.getHitSize("", "", null));
    }

    @Test
    public void testGetList() throws Exception {
        MQResultManager manager = new MQResultManager();
        List<? extends DatabaseObject> result = manager.getList("", "", 0, 10, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetIdListReturnsNull() {
        MQResultManager manager = new MQResultManager();
        assertNull(manager.getIdList("", "", null));
    }

    @Test
    public void testInsertResult() {
        MQResultManager.insertResult(sampleMessage);
    }

    @Test
    public void testGetAllTicketNames() {
        List<String> result = MQResultManager.getAllTicketNames();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("ticket-1"));
    }
}
