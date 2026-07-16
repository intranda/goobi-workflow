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

import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;

/**
 * Tests for {@link ProcessResource#startOpenAutomaticStepsOfTheProcess(String)}.
 *
 * Guard order verified in production code (ProcessResource.java): non-numeric/blank id -> 400, unknown process -> 404, checkProcessAccess() -> 403,
 * otherwise startOpenAutomaticTasks() runs and the endpoint returns 200.
 */
@ExtendWith(MockitoExtension.class)
public class ProcessStartStepsTest extends AbstractProcessResourceTest {

    @Test
    public void testStartStepsNonNumericIdReturnsBadRequest() {
        Response response = new ProcessResource().startOpenAutomaticStepsOfTheProcess("abc");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testStartStepsBlankIdReturnsBadRequest() {
        Response response = new ProcessResource().startOpenAutomaticStepsOfTheProcess("");
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testStartStepsUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            Response response = new ProcessResource().startOpenAutomaticStepsOfTheProcess("999");

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testStartStepsAccessDeniedReturnsForbidden() throws Exception {
        Process process = buildProcess(1);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(42, 1)).thenReturn(false);

            ProcessResource resource = new ProcessResource();
            resource.setRequest(mockRequestWithToken(buildToken(42)));

            Response response = resource.startOpenAutomaticStepsOfTheProcess("5");

            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void testStartStepsSuccessReturnsOk() {
        Process process = buildProcess(1);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(5)).thenReturn(new ArrayList<>());
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any()))
                    .thenReturn(new ArrayList<>());
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(new ArrayList<>());

            Response response = new ProcessResource().startOpenAutomaticStepsOfTheProcess("5");

            assertEquals(200, response.getStatus());
            mockedHelper.verify(() -> Helper.addMessageToProcessJournal(5, org.goobi.production.enums.LogType.DEBUG,
                    "open automatic steps are started using REST-API."));
        }
    }

}
