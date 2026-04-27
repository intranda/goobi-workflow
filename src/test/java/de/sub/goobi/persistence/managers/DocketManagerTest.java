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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DocketMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class DocketManagerTest extends AbstractTest {

    private Docket sampleDocket;
    private List<Docket> sampleDockets;

    @Before
    public void setUp() throws Exception {
        sampleDocket = new Docket();
        sampleDocket.setId(1);
        sampleDocket.setName("TestDocket");
        sampleDocket.setFile("docket.xsl");

        sampleDockets = new ArrayList<>();
        sampleDockets.add(sampleDocket);

        PowerMock.mockStatic(DocketMysqlHelper.class);
        EasyMock.expect(DocketMysqlHelper.getDocketById(EasyMock.anyInt())).andReturn(sampleDocket).anyTimes();
        DocketMysqlHelper.saveDocket(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        DocketMysqlHelper.deleteDocket(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        EasyMock.expect(DocketMysqlHelper.getDockets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject())).andReturn(sampleDockets).anyTimes();
        EasyMock.expect(DocketMysqlHelper.getDocketCount(EasyMock.anyString(), EasyMock.anyObject())).andReturn(1).anyTimes();
        EasyMock.expect(DocketMysqlHelper.getAllDockets()).andReturn(sampleDockets).anyTimes();
        EasyMock.expect(DocketMysqlHelper.getDocketByName(EasyMock.anyString())).andReturn(sampleDocket).anyTimes();
        PowerMock.replay(DocketMysqlHelper.class);
    }

    @Test
    public void testGetDocketById() throws Exception {
        Docket result = DocketManager.getDocketById(1);
        assertNotNull(result);
        assertEquals("TestDocket", result.getName());
    }

    @Test
    public void testSaveDocket() throws Exception {
        DocketManager.saveDocket(sampleDocket);
    }

    @Test
    public void testDeleteDocket() throws Exception {
        DocketManager.deleteDocket(sampleDocket);
    }

    @Test
    public void testGetDockets() throws Exception {
        List<Docket> result = DocketManager.getDockets("", "", 0, 10, null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetList() throws Exception {
        DocketManager manager = new DocketManager();
        List<?> result = manager.getList("", "", 0, 10, null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetHitSize() throws Exception {
        DocketManager manager = new DocketManager();
        assertEquals(1, manager.getHitSize("", "", null));
    }

    @Test
    public void testGetIdListReturnsNull() {
        DocketManager manager = new DocketManager();
        assertNull(manager.getIdList("", "", null));
    }

    @Test
    public void testGetAllDockets() {
        List<Docket> result = DocketManager.getAllDockets();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetDocketByName() throws Exception {
        Docket result = DocketManager.getDocketByName("TestDocket");
        assertNotNull(result);
        assertEquals("TestDocket", result.getName());
    }

    @Test
    public void testResultSetHandlersNotNull() {
        assertNotNull(DocketManager.RESULTSET_TO_DOCKET_HANDLER);
        assertNotNull(DocketManager.RESULTSET_TO_DOCKET_LIST_HANDLER);
    }
}
