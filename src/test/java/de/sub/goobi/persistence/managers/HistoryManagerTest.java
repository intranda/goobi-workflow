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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.HistoryEventType;

@ExtendWith(MockitoExtension.class)
public class HistoryManagerTest extends AbstractTest {

    private List<HistoryEvent> sampleEvents;
    private HistoryEvent sampleEvent;

    @BeforeEach
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

    }

    @Test
    public void testGetHistoryEvents() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            List<HistoryEvent> result = HistoryManager.getHistoryEvents(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetNumberOfImages() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            int result = HistoryManager.getNumberOfImages(1);
            assertEquals(42, result);

        }
    }

    @Test
    public void testAddHistory() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            HistoryManager.addHistory(new Date(), 1.0, "value", 1, 1);

        }
    }

    @Test
    public void testAddHistoryEvent() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            HistoryManager.addHistoryEvent(sampleEvent);

        }
    }

    @Test
    public void testUpdateHistoryEvent() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            HistoryManager.updateHistoryEvent(sampleEvent);

        }
    }

    @Test
    public void testDeleteHistoryEvent() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            HistoryManager.deleteHistoryEvent(sampleEvent);

        }
    }

    @Test
    public void testAddAllEvents() {
        try (MockedStatic<HistoryMysqlHelper> mockedHistoryMysqlHelper = Mockito.mockStatic(HistoryMysqlHelper.class)) {
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getHistoryEvents(Mockito.anyInt())).thenReturn(sampleEvents);
            mockedHistoryMysqlHelper.when(() -> HistoryMysqlHelper.getNumberOfImages(Mockito.anyInt())).thenReturn(42);

            HistoryManager.addAllEvents(sampleEvents);
            assertTrue(true);

        }
    }
}
