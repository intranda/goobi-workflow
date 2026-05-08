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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.goobi.api.mq.MqStatusMessage;
import org.goobi.beans.DatabaseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class MQResultManagerTest extends AbstractTest {

    private MqStatusMessage sampleMessage;

    @BeforeEach
    public void setUp() throws Exception {
        sampleMessage = new MqStatusMessage("ticket-1", new Date(), MqStatusMessage.MessageStatus.DONE, "Done", "original", 0, "testQueue", 1, 1,
                "ticketName");

    }

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<MQResultMysqlHelper> mockedMQResultMysqlHelper = Mockito.mockStatic(MQResultMysqlHelper.class)) {
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessagesCount(Mockito.anyString())).thenReturn(5);
                        mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessageList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
                ;
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getAllTicketNames()).thenReturn(Arrays.asList("ticket-1", "ticket-2"));


            MQResultManager manager = new MQResultManager();
            assertEquals(5, manager.getHitSize("", "", null));
    
        }
}

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<MQResultMysqlHelper> mockedMQResultMysqlHelper = Mockito.mockStatic(MQResultMysqlHelper.class)) {
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessagesCount(Mockito.anyString())).thenReturn(5);
                        mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessageList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
                ;
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getAllTicketNames()).thenReturn(Arrays.asList("ticket-1", "ticket-2"));


            MQResultManager manager = new MQResultManager();
            List<? extends DatabaseObject> result = manager.getList("", "", 0, 10, null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
    
        }
}

    @Test
    public void testGetIdListReturnsNull() {
        try (MockedStatic<MQResultMysqlHelper> mockedMQResultMysqlHelper = Mockito.mockStatic(MQResultMysqlHelper.class)) {
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessagesCount(Mockito.anyString())).thenReturn(5);
                        mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessageList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
                ;
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getAllTicketNames()).thenReturn(Arrays.asList("ticket-1", "ticket-2"));


            MQResultManager manager = new MQResultManager();
            assertNull(manager.getIdList("", "", null));
    
        }
}

    @Test
    public void testInsertResult() {
        try (MockedStatic<MQResultMysqlHelper> mockedMQResultMysqlHelper = Mockito.mockStatic(MQResultMysqlHelper.class)) {
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessagesCount(Mockito.anyString())).thenReturn(5);
                        mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessageList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
                ;
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getAllTicketNames()).thenReturn(Arrays.asList("ticket-1", "ticket-2"));


            MQResultManager.insertResult(sampleMessage);
    
        }
}

    @Test
    public void testGetAllTicketNames() {
        try (MockedStatic<MQResultMysqlHelper> mockedMQResultMysqlHelper = Mockito.mockStatic(MQResultMysqlHelper.class)) {
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessagesCount(Mockito.anyString())).thenReturn(5);
                        mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getMessageList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(java.util.Collections.emptyList());
                ;
            mockedMQResultMysqlHelper.when(() -> MQResultMysqlHelper.getAllTicketNames()).thenReturn(Arrays.asList("ticket-1", "ticket-2"));


            List<String> result = MQResultManager.getAllTicketNames();
            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains("ticket-1"));
    
        }
}
}
