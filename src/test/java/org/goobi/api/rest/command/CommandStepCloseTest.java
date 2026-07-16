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

import org.goobi.beans.Process;
import org.goobi.beans.Step;
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
public class CommandStepCloseTest extends AbstractTest {

    @Test
    public void testCloseStepUnknownProcessReturnsNotFound() {
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(999)).thenReturn(null);

            @SuppressWarnings("deprecation")
            Response response = new CommandStepClose().closeStepByProcessIdAndName(999, "anyStep");

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testCloseStepNameNotFoundReturnsNotFound() {
        Process process = new Process();
        process.setId(5);
        try (MockedStatic<ProcessManager> pm = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<StepManager> sm = Mockito.mockStatic(StepManager.class)) {
            pm.when(() -> ProcessManager.getProcessById(5)).thenReturn(process);
            sm.when(() -> StepManager.getStepsForProcess(5)).thenReturn(new ArrayList<>());

            @SuppressWarnings("deprecation")
            Response response = new CommandStepClose().closeStepByProcessIdAndName(5, "missing");

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testCloseStepByIdStepNotFoundReturnsNotFound() {
        try (MockedStatic<StepManager> sm = Mockito.mockStatic(StepManager.class)) {
            sm.when(() -> StepManager.getStepById(999)).thenReturn(null);

            @SuppressWarnings("deprecation")
            Response response = new CommandStepClose().closeStepAndRemoveLink(null, 999);

            assertEquals(404, response.getStatus());
        }
    }

    @Test
    public void testCloseStepInvalidUsernameReturnsBadRequest() {
        Step step = new Step();
        step.setId(2);
        step.setTitel("scan");
        try (MockedStatic<StepManager> sm = Mockito.mockStatic(StepManager.class)) {
            sm.when(() -> StepManager.getStepById(2)).thenReturn(step);

            @SuppressWarnings("deprecation")
            Response response = new CommandStepClose().closeStepAndRemoveLink("invalid name!", 2);

            assertEquals(400, response.getStatus());
        }
    }

    @Test
    public void testCloseStepAlreadyDoneReturnsBadRequest() {
        Step step = Mockito.spy(new Step());
        step.setId(2);
        step.setTitel("scan");
        step.setBearbeitungsstatusEnum(StepStatus.DONE);
        try (MockedStatic<StepManager> sm = Mockito.mockStatic(StepManager.class)) {
            sm.when(() -> StepManager.getStepById(2)).thenReturn(step);

            @SuppressWarnings("deprecation")
            Response response = new CommandStepClose().closeStepAndRemoveLink(null, 2);

            assertEquals(400, response.getStatus());
        }
    }
}
