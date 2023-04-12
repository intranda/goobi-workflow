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

package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.ws.rs.core.Response;

import org.easymock.EasyMock;
import org.goobi.api.rest.model.RestProcessResource;
import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.ProcessManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessManager.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class ProcessServiceTest extends AbstractTest {

    private ProcessService service;
    private RestProcessResource resource;

    @Before
    public void setUp() throws Exception {
        service = new ProcessService();

        resource = new RestProcessResource();
        resource.setId(1);

        Process process = MockProcess.createProcess();
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();

        ProcessManager.saveProcessInformation(EasyMock.anyObject());

        EasyMock.expectLastCall();
        PowerMock.replayAll();

    }

    @Test
    public void testGetProcessData() {
        service = new ProcessService();
        Response response = service.getProcessData("");
        assertEquals(400, response.getStatus());
        response = service.getProcessData("abc");
        assertEquals(400, response.getStatus());

        response = service.getProcessData("1");
        assertNotNull(response);

        RestProcessResource res = (RestProcessResource) response.getEntity();
        assertEquals(1, res.getId());
        assertEquals("testprocess", res.getTitle());
        assertEquals("project", res.getProjectName());
    }

    @Test
    public void testUpdateProcess() {
        service = new ProcessService();
        Response response = service.updateProcess(resource);
        assertNotNull(response);
        RestProcessResource res = (RestProcessResource) response.getEntity();
        assertEquals(1, res.getId());
        assertEquals("testprocess", res.getTitle());
    }

    @Test
    public void testCreateProcess() {
        service = new ProcessService();
        Response response = service.createProcess( resource);
        assertNull(response);
    }

    @Test
    public void testDeleteProcess() {
        service = new ProcessService();
        Response response = service.deleteProcess(resource);
        assertNull(response);
    }
}
