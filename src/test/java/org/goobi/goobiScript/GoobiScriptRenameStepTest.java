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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.enums.GoobiScriptResultType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, StepManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptRenameStepTest extends AbstractTest {

    private Process process;
    private Step s1;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(StepManager.class);

        User user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();
        Helper.setFehlerMeldungUntranslated(EasyMock.anyString(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();

        process = new Process();
        process.setId(1);

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.OPEN);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        StepManager.saveStep(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
        assertNotNull(fixture);
        assertEquals("renameStep", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
        assertNotNull(fixture.getSampleCall());
        assertTrue(fixture.getSampleCall().contains("renameStep"));
    }

    @Test
    public void testPrepare() {
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

    @Test
    public void testPrepareWithMissingOldStepNameReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("newStepName", "renamed step");
        GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
        List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testPrepareWithMissingNewStepNameReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("oldStepName", "step 1");
        GoobiScriptRenameStep fixture = new GoobiScriptRenameStep();
        List<GoobiScriptResult> results = fixture.prepare(processes, "renameStep", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testExecuteRenamesStep() {
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
