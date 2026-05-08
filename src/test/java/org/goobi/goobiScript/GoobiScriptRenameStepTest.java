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
package org.goobi.goobiScript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.GoobiScriptResultType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class GoobiScriptRenameStepTest extends AbstractTest {

    private Process process;
    private Step s1;

    private User user;

    @BeforeEach
    public void setUp() throws Exception {

        user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");

        process = new Process();
        process.setId(1);

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.OPEN);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);


    }

    @Test
    public void testConstructor() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            assertNotNull(fixture);
            assertEquals("renameStep", fixture.getAction());
    
        }
}

    @Test
    public void testSampleCall() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            assertNotNull(fixture.getSampleCall());
            assertTrue(fixture.getSampleCall().contains("renameStep"));
    
        }
}

    @Test
    public void testPrepare() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("oldStepName", "step 1");
            parameters.put("newStepName", "renamed step");
            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
            assertEquals(1, results.size());
            assertEquals("renameStep", results.get(0).getCommand());
    
        }
}

    @Test
    public void testPrepareWithMissingOldStepNameReturnsEmpty() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("newStepName", "renamed step");
            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
            assertTrue(results.isEmpty());
    
        }
}

    @Test
    public void testPrepareWithMissingNewStepNameReturnsEmpty() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("oldStepName", "step 1");
            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
            assertTrue(results.isEmpty());
    
        }
}

    @Test
    public void testExecuteRenamesStep() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);


            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("oldStepName", "step 1");
            parameters.put("newStepName", "renamed step");
            GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
            List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
            fixture.execute(results.get(0));
            assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
            assertEquals("renamed step", s1.getTitel());
    
        }
}
}
