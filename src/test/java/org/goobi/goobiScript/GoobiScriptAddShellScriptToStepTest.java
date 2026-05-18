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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;

@ExtendWith(MockitoExtension.class)
public class GoobiScriptAddShellScriptToStepTest extends AbstractTest {

    private Process process;

    private Step s1;

    private User u;

    @BeforeEach
    public void setUp() throws Exception {

        u = new User();
        u.setVorname("firstname");
        u.setNachname("lastname");

        process = new Process();
        process.setId(Integer.valueOf(1));

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.DONE);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            GoobiScriptAddShellScriptToStep fixture = new GoobiScriptAddShellScriptToStep();
            assertNotNull(fixture);
            assertEquals("addShellScriptToStep", fixture.getAction());

        }
    }

    @Test
    public void testSampleCall() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            GoobiScriptAddShellScriptToStep fixture = new GoobiScriptAddShellScriptToStep();
            assertNotNull(fixture);
            assertEquals(
                    "---\\n# This GoobiScript allows to add a shell script to an existing workflow step.\\naction: "
                            + "addShellScriptToStep\\n\\n# This is the title of the workflow step to be used.\\n"
                            + "steptitle: Generate MD5 Hashes\\n\\n# Define a label for the script that shall be "
                            + "visible for that script inside of an accepted task.\\nlabel: Hash generation\\n\\n# "
                            + "Define the script that shall be added here.\\nscript: /bin/bash /path/to/script.sh \"parameter with blanks\"",
                    fixture.getSampleCall());

        }
    }

    @Test
    public void testPrepare() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            String command = "addShellScriptToStep";
            Map<String, String> parameters = new HashMap<>();
            parameters.put("steptitle", "step 1");
            parameters.put("label", "abc");
            parameters.put("script", "xyz");
            GoobiScriptAddShellScriptToStep fixture = new GoobiScriptAddShellScriptToStep();
            assertNotNull(fixture);
            List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
            assertEquals(1, results.size());
            assertEquals("addShellScriptToStep", results.get(0).getCommand());

        }
    }

    @Test
    public void testExecute() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(u);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            String command = "addShellScriptToStep";
            Map<String, String> parameters = new HashMap<>();
            parameters.put("steptitle", "step 1");
            parameters.put("label", "abc");
            parameters.put("script", "xyz");
            GoobiScriptAddShellScriptToStep fixture = new GoobiScriptAddShellScriptToStep();
            List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);

            assertNull(s1.getScriptname1());

            fixture.execute(results.get(0));

            assertEquals("abc", s1.getScriptname1());

        }
    }

}
