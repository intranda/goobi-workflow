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

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class ProcessStepSetErrorTest extends AbstractTest {

    private MockedStatic<StepManager> mockedStepManager;

    @AfterEach
    public void tearDown() {
        if (mockedStepManager != null) {
            mockedStepManager.close();
        }
    }

    @Test
    public void testSetErrorStepNotFound() {
        mockedStepManager = Mockito.mockStatic(StepManager.class);
        mockedStepManager.when(() -> StepManager.getStepById(999)).thenReturn(null);

        Response response = new ProcessStepResource().setStepToError("1", "999");

        assertEquals(404, response.getStatus());
    }

    @Test
    public void testSetErrorNonNumericProcessIdReturns400() {
        Response response = new ProcessStepResource().setStepToError("abc", "999");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSetErrorNonNumericStepIdReturns400() {
        Response response = new ProcessStepResource().setStepToError("1", "abc");

        assertEquals(400, response.getStatus());
    }

    @Test
    public void testSetErrorStepBelongsToDifferentProcessReturns409() {
        int procId = 1;
        int otherProcId = 2;
        int stepId = 2;

        Step step = new Step();
        step.setId(stepId);
        step.setProcessId(otherProcId);

        mockedStepManager = Mockito.mockStatic(StepManager.class);
        mockedStepManager.when(() -> StepManager.getStepById(stepId)).thenReturn(step);

        Response response = new ProcessStepResource().setStepToError(String.valueOf(procId), String.valueOf(stepId));

        assertEquals(409, response.getStatus());
    }

    @Test
    public void testSetErrorSuccess() {
        int procId = 1;
        int stepId = 2;

        Process process = new Process();
        process.setId(procId);
        process.setTitel("testprocess");

        Step step = new Step();
        step.setId(stepId);
        step.setProcessId(procId);

        try (MockedStatic<StepManager> mockedStepManagerStatic = Mockito.mockStatic(StepManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {

            mockedStepManagerStatic.when(() -> StepManager.getStepById(stepId)).thenReturn(step);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(procId)).thenReturn(process);
            mockedStepManagerStatic.when(() -> StepManager.saveStep(Mockito.any())).thenAnswer(invocation -> null);

            ProcessStepResource resource = new ProcessStepResource();
            Response response = resource.setStepToError(String.valueOf(procId), String.valueOf(stepId));

            assertEquals(200, response.getStatus());
            assertEquals(StepStatus.ERROR, step.getBearbeitungsstatusEnum());

            mockedStepManagerStatic.verify(() -> StepManager.saveStep(step));
        }
    }

}
