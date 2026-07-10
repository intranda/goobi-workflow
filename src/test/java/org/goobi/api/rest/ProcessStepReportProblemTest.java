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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.rest.model.RestReportProblem;
import org.goobi.api.rest.model.RestReportProblemResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessStepReportProblemTest extends AbstractTest {

    private MockedStatic<StepManager> mockedStepManager;

    @AfterEach
    public void tearDown() {
        if (mockedStepManager != null) {
            mockedStepManager.close();
        }
    }

    @Test
    public void testReportProblemStepNotFound() {
        mockedStepManager = Mockito.mockStatic(StepManager.class);
        mockedStepManager.when(() -> StepManager.getStepById(999)).thenReturn(null);

        Response response = new ProcessStepResource().reportProblem("1", "999", new RestReportProblem());

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testReportProblemSuccess() {
        int procId = 1;
        int sourceStepId = 2;
        int destinationStepId = 1;

        Process process = new Process();
        process.setId(procId);
        process.setTitel("testprocess");

        Step source = Mockito.spy(new Step());
        source.setId(sourceStepId);
        source.setTitel("source step");
        source.setReihenfolge(2);
        source.setProcessId(procId);

        Step destination = Mockito.spy(new Step());
        destination.setId(destinationStepId);
        destination.setTitel("destination step");
        destination.setReihenfolge(1);
        destination.setProcessId(procId);

        List<Step> steps = new ArrayList<>();
        steps.add(source);
        steps.add(destination);
        process.setSchritte(steps);

        Mockito.doReturn(process).when(source).getProzess();
        Mockito.doReturn(process).when(destination).getProzess();

        try (MockedStatic<StepManager> mockedStepManagerStatic = Mockito.mockStatic(StepManager.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class)) {

            mockedStepManagerStatic.when(() -> StepManager.getStepById(sourceStepId)).thenReturn(source);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(procId)).thenReturn(process);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenAnswer(invocation -> invocation.getArgument(0));

            RestReportProblem body = new RestReportProblem();
            body.setDestinationStepName("destination step");
            body.setErrorText("test");

            ProcessStepResource resource = new ProcessStepResource();
            Response response = resource.reportProblem(String.valueOf(procId), String.valueOf(sourceStepId), body);

            assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());

            RestReportProblemResponse entity = (RestReportProblemResponse) response.getEntity();
            assertEquals(sourceStepId, entity.getErrorStepId());
            assertEquals(destinationStepId, entity.getDestinationStepId());

            assertEquals(StepStatus.LOCKED, source.getBearbeitungsstatusEnum());
            assertEquals(StepStatus.OPEN, destination.getBearbeitungsstatusEnum());

            // saving must have been attempted for the intermediate step(s) and the process itself
            mockedStepManagerStatic.verify(() -> StepManager.saveStep(Mockito.any()), Mockito.atLeastOnce());
            mockedProcessManager.verify(() -> ProcessManager.saveProcess(Mockito.any()));
        }
    }

    @Test
    public void testReportProblemDestinationNotFound() {
        int procId = 1;
        int sourceStepId = 2;

        Process process = new Process();
        process.setId(procId);
        process.setTitel("testprocess");

        Step source = Mockito.spy(new Step());
        source.setId(sourceStepId);
        source.setTitel("source step");
        source.setReihenfolge(2);
        source.setProcessId(procId);

        List<Step> steps = new ArrayList<>();
        steps.add(source);
        process.setSchritte(steps);

        Mockito.doReturn(process).when(source).getProzess();

        try (MockedStatic<StepManager> mockedStepManagerStatic = Mockito.mockStatic(StepManager.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {

            mockedStepManagerStatic.when(() -> StepManager.getStepById(sourceStepId)).thenReturn(source);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(procId)).thenReturn(process);

            RestReportProblem body = new RestReportProblem();
            body.setDestinationStepName("does not exist");
            body.setErrorText("test");

            ProcessStepResource resource = new ProcessStepResource();
            Response response = resource.reportProblem(String.valueOf(procId), String.valueOf(sourceStepId), body);

            assertEquals(400, response.getStatus());
        }
    }

}
