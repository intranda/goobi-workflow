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
public class GoobiScriptSetPriorityTest extends AbstractTest {

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
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
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
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        assertNotNull(fixture);
        assertEquals("setPriority", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        assertNotNull(fixture.getSampleCall());
        assertTrue(fixture.getSampleCall().contains("setPriority"));
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("priority", "high");
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setPriority", parameters);
        assertEquals(1, results.size());
        assertEquals("setPriority", results.get(0).getCommand());
    }

    @Test
    public void testPrepareWithMissingPriorityReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setPriority", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testPrepareWithInvalidPriorityReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("priority", "ultramax");
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setPriority", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testExecuteSetsPriorityHigh() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("priority", "high");
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setPriority", parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
        assertEquals(1, s1.getPrioritaet().intValue());
    }

    @Test
    public void testExecuteSetsPriorityCorrection() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("priority", "correction");
        GoobiScriptSetPriority fixture = new GoobiScriptSetPriority();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setPriority", parameters);
        fixture.execute(results.get(0));
        assertEquals(10, s1.getPrioritaet().intValue());
    }
}
