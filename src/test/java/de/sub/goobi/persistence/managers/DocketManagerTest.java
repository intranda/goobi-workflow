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

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Docket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class DocketManagerTest extends AbstractTest {

    private Docket sampleDocket;
    private List<Docket> sampleDockets;

    @BeforeEach
    public void setUp() throws Exception {
        sampleDocket = new Docket();
        sampleDocket.setId(1);
        sampleDocket.setName("TestDocket");
        sampleDocket.setFile("docket.xsl");

        sampleDockets = new ArrayList<>();
        sampleDockets.add(sampleDocket);

    }

    @Test
    public void testGetDocketById() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            Docket result = DocketManager.getDocketById(1);
            assertNotNull(result);
            assertEquals("TestDocket", result.getName());
    
        }
}

    @Test
    public void testSaveDocket() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            DocketManager.saveDocket(sampleDocket);
    
        }
}

    @Test
    public void testDeleteDocket() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            DocketManager.deleteDocket(sampleDocket);
    
        }
}

    @Test
    public void testGetDockets() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            List<Docket> result = DocketManager.getDockets("", "", 0, 10, null);
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            DocketManager manager = new DocketManager();
            List<?> result = manager.getList("", "", 0, 10, null);
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            DocketManager manager = new DocketManager();
            assertEquals(1, manager.getHitSize("", "", null));
    
        }
}

    @Test
    public void testGetIdListReturnsNull() {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            DocketManager manager = new DocketManager();
            assertNull(manager.getIdList("", "", null));
    
        }
}

    @Test
    public void testGetAllDockets() {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            List<Docket> result = DocketManager.getAllDockets();
            assertNotNull(result);
            assertEquals(1, result.size());
    
        }
}

    @Test
    public void testGetDocketByName() throws Exception {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            Docket result = DocketManager.getDocketByName("TestDocket");
            assertNotNull(result);
            assertEquals("TestDocket", result.getName());
    
        }
}

    @Test
    public void testResultSetHandlersNotNull() {
        try (MockedStatic<DocketMysqlHelper> mockedDocketMysqlHelper = Mockito.mockStatic(DocketMysqlHelper.class)) {
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketById(Mockito.anyInt())).thenReturn(sampleDocket);
                        mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDockets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.any())).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketCount(Mockito.anyString(), Mockito.any())).thenReturn(1);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getAllDockets()).thenReturn(sampleDockets);
            mockedDocketMysqlHelper.when(() -> DocketMysqlHelper.getDocketByName(Mockito.anyString())).thenReturn(sampleDocket);


            assertNotNull(DocketManager.RESULTSET_TO_DOCKET_HANDLER);
            assertNotNull(DocketManager.RESULTSET_TO_DOCKET_LIST_HANDLER);
    
        }
}
}
