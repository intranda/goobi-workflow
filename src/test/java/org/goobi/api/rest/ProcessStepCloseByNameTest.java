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
import java.util.List;

import org.goobi.api.rest.model.RestStepQueryResource;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.CloseStepHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessStepCloseByNameTest extends AbstractProcessResourceTest {

    private static final String STEP_NAME = "scanning";

    @Test
    public void testCloseStepNonNumericIdReturnsBadRequest() {
        Response response = new ProcessStepResource().closeStepGivenName("abc", new RestStepQueryResource(STEP_NAME));
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testCloseStepUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            Response response = new ProcessStepResource().closeStepGivenName("999", new RestStepQueryResource(STEP_NAME));

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testCloseStepAccessDeniedReturnsForbidden() {
        Process process = buildProcess(1);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedProjectManager.when(() -> ProjectManager.isUserMemberOfProject(42, 1)).thenReturn(false);

            ProcessStepResource resource = new ProcessStepResource();
            resource.setRequest(mockRequestWithToken(buildToken(42)));

            Response response = resource.closeStepGivenName("5", new RestStepQueryResource(STEP_NAME));

            assertEquals(403, response.getStatus());
        }
    }

    @Test
    public void testCloseStepNameNotFoundReturnsNotFound() {
        Process process = buildProcess(1);

        Step other = new Step();
        other.setTitel("other step");
        List<Step> steps = new ArrayList<>();
        steps.add(other);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(5)).thenReturn(steps);

            Response response = new ProcessStepResource().closeStepGivenName("5", new RestStepQueryResource(STEP_NAME));

            assertEquals(404, response.getStatus());
        }
    }

    private Response closeStepWithStatus(StepStatus status) {
        Process process = buildProcess(1);

        Step step = new Step();
        step.setTitel(STEP_NAME);
        step.setProcessId(5);
        step.setBearbeitungsstatusEnum(status);
        List<Step> steps = new ArrayList<>();
        steps.add(step);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(5)).thenReturn(steps);

            return new ProcessStepResource().closeStepGivenName("5", new RestStepQueryResource(STEP_NAME));
        }
    }

    @Test
    public void testCloseStepDeactivatedReturnsConflict() {
        assertEquals(409, closeStepWithStatus(StepStatus.DEACTIVATED).getStatus());
    }

    @Test
    public void testCloseStepDoneReturnsConflict() {
        assertEquals(409, closeStepWithStatus(StepStatus.DONE).getStatus());
    }

    @Test
    public void testCloseStepLockedReturnsConflict() {
        assertEquals(409, closeStepWithStatus(StepStatus.LOCKED).getStatus());
    }

    @Test
    public void testCloseStepOpenReturnsOk() {
        Process process = buildProcess(1);

        Step step = new Step();
        step.setTitel(STEP_NAME);
        step.setProcessId(5);
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        List<Step> steps = new ArrayList<>();
        steps.add(step);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<CloseStepHelper> mockedCloseStepHelper = Mockito.mockStatic(CloseStepHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            mockedProcessManager.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(5)).thenReturn(steps);
            mockedCloseStepHelper.when(() -> CloseStepHelper.closeStep(step, null)).thenReturn(true);

            Response response = new ProcessStepResource().closeStepGivenName("5", new RestStepQueryResource(STEP_NAME));

            assertEquals(200, response.getStatus());
            mockedCloseStepHelper.verify(() -> CloseStepHelper.closeStep(step, null));
        }
    }

}
