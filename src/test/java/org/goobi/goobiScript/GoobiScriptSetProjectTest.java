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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.production.enums.GoobiScriptResultType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;

@ExtendWith(MockitoExtension.class)
public class GoobiScriptSetProjectTest extends AbstractTest {

    private Process process;
    private Project project;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");

        project = new Project();
        project.setId(1);
        project.setTitel("Newspaper_2021");

        process = new Process();
        process.setId(1);
        process.setTitel("Test Process");
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            GoobiScriptSetProject fixture = new GoobiScriptSetProject();
            assertNotNull(fixture);
            assertEquals("setProject", fixture.getAction());
        }
    }

    @Test
    public void testSampleCall() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            GoobiScriptSetProject fixture = new GoobiScriptSetProject();
            assertNotNull(fixture.getSampleCall());
            assertTrue(fixture.getSampleCall().contains("setProject"));
        }
    }

    @Test
    public void testPrepare() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("project", "Newspaper_2021");
            GoobiScriptSetProject fixture = new GoobiScriptSetProject();
            List<GoobiScriptResult> results = fixture.prepare(processes, "setProject", parameters);
            assertEquals(1, results.size());
            assertEquals("setProject", results.get(0).getCommand());
        }
    }

    @Test
    public void testPrepareWithMissingProjectReturnsEmpty() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            GoobiScriptSetProject fixture = new GoobiScriptSetProject();
            List<GoobiScriptResult> results = fixture.prepare(processes, "setProject", parameters);
            assertTrue(results.isEmpty());
        }
    }

    @Test
    public void testExecuteAssignsProject() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<ProjectManager> mockedProjectManager = Mockito.mockStatic(ProjectManager.class)) {
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedProjectManager.when(() -> ProjectManager.getProjectByName(Mockito.any())).thenReturn(project);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            List<Integer> processes = new ArrayList<>();
            processes.add(1);
            Map<String, String> parameters = new HashMap<>();
            parameters.put("project", "Newspaper_2021");
            GoobiScriptSetProject fixture = new GoobiScriptSetProject();
            List<GoobiScriptResult> results = fixture.prepare(processes, "setProject", parameters);
            fixture.execute(results.get(0));
            assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
            assertTrue(results.get(0).getResultMessage().contains("Project"));
            assertSame(project, process.getProjekt());
        }
    }
}
