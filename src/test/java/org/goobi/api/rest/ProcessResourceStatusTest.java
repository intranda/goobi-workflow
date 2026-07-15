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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.goobi.api.db.RestDbHelper;
import org.goobi.api.rest.model.RestProcessStatus;
import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import jakarta.ws.rs.core.Response;

public class ProcessResourceStatusTest extends AbstractTest {

    @Test
    public void testUnknownIdentifierReturns404() throws Exception {
        try (MockedStatic<RestDbHelper> db = Mockito.mockStatic(RestDbHelper.class)) {
            db.when(() -> RestDbHelper.getProcessIdsForIdentifier("XYZ")).thenReturn(Collections.emptyList());
            ProcessResource r = new ProcessResource();
            Response resp = r.getProcessStatusForIdentifier("XYZ");
            assertEquals(404, resp.getStatus());
        }
    }

    @Test
    public void testMultipleProcessesForIdentifierReturns409() throws Exception {
        try (MockedStatic<RestDbHelper> db = Mockito.mockStatic(RestDbHelper.class)) {
            db.when(() -> RestDbHelper.getProcessIdsForIdentifier("XYZ")).thenReturn(Arrays.asList(1, 2));
            ProcessResource r = new ProcessResource();
            Response resp = r.getProcessStatusForIdentifier("XYZ");
            assertEquals(409, resp.getStatus());
        }
    }

    @Test
    public void testSingleProcessForIdentifierReturns200() throws Exception {
        int processId = 42;

        Process process = Mockito.spy(new Process());
        process.setId(processId);
        process.setTitel("testprocess");

        List<org.goobi.beans.Step> steps = new ArrayList<>();
        Mockito.doReturn(steps).when(process).getSchritte();
        Mockito.doReturn(new Date()).when(process).getErstellungsdatum();
        Mockito.doReturn("100000000").when(process).getSortHelperStatus();

        try (MockedStatic<RestDbHelper> db = Mockito.mockStatic(RestDbHelper.class);
                MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            db.when(() -> RestDbHelper.getProcessIdsForIdentifier("XYZ")).thenReturn(Collections.singletonList(processId));
            pm.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);

            ProcessResource r = new ProcessResource();
            Response resp = r.getProcessStatusForIdentifier("XYZ");

            assertEquals(200, resp.getStatus());
            RestProcessStatus entity = (RestProcessStatus) resp.getEntity();
            assertEquals("testprocess", entity.getTitle());
            assertEquals(processId, entity.getId());
        }
    }
}
