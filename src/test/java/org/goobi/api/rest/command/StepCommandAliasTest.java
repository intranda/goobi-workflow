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
package org.goobi.api.rest.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.core.Response;

@SuppressWarnings("deprecation")
public class StepCommandAliasTest extends AbstractTest {

    @Test
    public void testSetStepToErrorByNameWithUnknownStepReturnsNotFound() {
        Process process = new Process();
        process.setId(42);
        process.setTitel("testprocess");

        Step existing = new Step();
        existing.setTitel("scanning");
        List<Step> steps = new ArrayList<>();
        steps.add(existing);

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {

            mockedProcessManager.when(() -> ProcessManager.getProcessByExactTitle("testprocess")).thenReturn(process);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(42)).thenReturn(steps);

            CommandSetStepToError command = new CommandSetStepToError();
            Response response = command.setStepToErrorByName("testprocess", "unknown-step");

            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        }
    }

    @Test
    public void testSetStepToErrorWithValidStepSavesErrorStatus() throws Exception {
        Step source = new Step();
        source.setId(7);
        source.setTitel("scanning");

        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {

            mockedStepManager.when(() -> StepManager.getStepById(7)).thenReturn(source);
            mockedStepManager.when(() -> StepManager.saveStep(Mockito.any(Step.class))).thenAnswer(invocation -> null);

            CommandSetStepToError command = new CommandSetStepToError();
            Response response = command.setStepToError(7);

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(StepStatus.ERROR, source.getBearbeitungsstatusEnum());
            mockedStepManager.verify(() -> StepManager.saveStep(source));
        }
    }

    @Test
    public void testAddToProcessLogByProcessIdWritesJournalEntry() {
        int processId = 99;

        Process process = new Process();
        process.setId(processId);
        process.setTitel("testprocess");

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(process);
            mockedHelper.when(() -> Helper.addMessageToProcessJournal(Mockito.anyInt(), Mockito.any(LogType.class), Mockito.anyString(),
                    Mockito.anyString())).thenAnswer(invocation -> null);

            CommandAddToProcessLog command = new CommandAddToProcessLog();
            Response response = command.addToLogByProcessId(processId, "info", "test log message");

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            mockedHelper.verify(() -> Helper.addMessageToProcessJournal(processId, LogType.INFO, "test log message", "webapi"));
        }
    }

    @Test
    public void testAddToProcessLogByProcessIdWithUnknownProcessReturnsError() {
        int processId = 12345;

        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            mockedProcessManager.when(() -> ProcessManager.getProcessById(processId)).thenReturn(null);

            CommandAddToProcessLog command = new CommandAddToProcessLog();
            Response response = command.addToLogByProcessId(processId, "info", "test log message");

            assertEquals(500, response.getStatus());
            mockedHelper.verifyNoInteractions();
        }
    }
}
