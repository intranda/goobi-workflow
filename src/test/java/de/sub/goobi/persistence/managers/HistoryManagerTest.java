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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.HistoryEventType;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HistoryMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class HistoryManagerTest extends AbstractTest {

    private List<HistoryEvent> sampleEvents;
    private HistoryEvent sampleEvent;

    @Before
    public void setUp() throws Exception {
        sampleEvents = new ArrayList<>();
        sampleEvent = new HistoryEvent();
        sampleEvent.setDate(new Date());
        sampleEvent.setNumericValue(1.0);
        sampleEvent.setStringValue("testValue");
        sampleEvent.setHistoryType(HistoryEventType.storageDifference);
        Process process = new Process();
        process.setId(1);
        sampleEvent.setProcess(process);
        sampleEvents.add(sampleEvent);

        PowerMock.mockStatic(HistoryMysqlHelper.class);
        EasyMock.expect(HistoryMysqlHelper.getHistoryEvents(EasyMock.anyInt())).andReturn(sampleEvents).anyTimes();
        EasyMock.expect(HistoryMysqlHelper.getNumberOfImages(EasyMock.anyInt())).andReturn(42).anyTimes();
        HistoryMysqlHelper.addHistory(EasyMock.anyObject(), EasyMock.anyDouble(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        EasyMock.expectLastCall().anyTimes();
        HistoryMysqlHelper.updateHistoryEvent(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        HistoryMysqlHelper.deleteHistoryEvent(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        HistoryMysqlHelper.addAllHistoryEvents(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(HistoryMysqlHelper.class);
    }

    @Test
    public void testGetHistoryEvents() {
        List<HistoryEvent> result = HistoryManager.getHistoryEvents(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetNumberOfImages() {
        int result = HistoryManager.getNumberOfImages(1);
        assertEquals(42, result);
    }

    @Test
    public void testAddHistory() {
        HistoryManager.addHistory(new Date(), 1.0, "value", 1, 1);
    }

    @Test
    public void testAddHistoryEvent() {
        HistoryManager.addHistoryEvent(sampleEvent);
    }

    @Test
    public void testUpdateHistoryEvent() {
        HistoryManager.updateHistoryEvent(sampleEvent);
    }

    @Test
    public void testDeleteHistoryEvent() {
        HistoryManager.deleteHistoryEvent(sampleEvent);
    }

    @Test
    public void testAddAllEvents() {
        HistoryManager.addAllEvents(sampleEvents);
        assertTrue(true);
    }
}
