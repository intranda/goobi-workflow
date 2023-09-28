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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
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
public class GoobiScriptAddStepAtOtherStepPositionTest extends AbstractTest {

    private Process process;

    private Step s1;
    private Step s2;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);

        User u = new User();
        u.setVorname("firstname");
        u.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(u).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setId(Integer.valueOf(1));

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.DONE);
        s1.setReihenfolge(1);
        s2 = new Step();
        s2.setTitel("step 2");
        s2.setBearbeitungsstatusEnum(StepStatus.DONE);
        s2.setReihenfolge(2);
        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        steps.add(s2);
        process.setSchritte(steps);

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptAddStepAtOtherStepPosition fixture = new GoobiScriptAddStepAtOtherStepPosition();
        assertNotNull(fixture);
        assertEquals("addStepAtOtherStepPosition", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptAddStepAtOtherStepPosition fixture = new GoobiScriptAddStepAtOtherStepPosition();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows to add a new workflow step into the workflow before or after an other step (defined by name).\\naction: addStepAtOtherStepPosition\\n\\n# The new step can be executed \"before\" or \"after\" the existing step.\\ninsertionstrategy: after\\n\\n# Title of the existing workflow step\\nexistingsteptitle: Scanning\\n\\n# Title of the new workflow step\\nnewsteptitle: Analyzing",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addStepAtOtherStepPosition";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("insertionstrategy", "before");
        parameters.put("existingsteptitle", "step 2");
        parameters.put("newsteptitle", "between");

        GoobiScriptAddStepAtOtherStepPosition fixture = new GoobiScriptAddStepAtOtherStepPosition();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("addStepAtOtherStepPosition", results.get(0).getCommand());
    }

    @Test
    public void testExecute() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addStepAtOtherStepPosition";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("insertionstrategy", "before");
        parameters.put("existingsteptitle", "step 2");
        parameters.put("newsteptitle", "between");
        GoobiScriptAddStepAtOtherStepPosition fixture = new GoobiScriptAddStepAtOtherStepPosition();
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);

        assertEquals(2, process.getSchritte().size());

        fixture.execute(results.get(0));
        assertEquals(3, process.getSchritte().size());
        assertEquals("between", process.getSchritte().get(2).getTitel());
        assertEquals(2, process.getSchritte().get(2).getReihenfolge().intValue());
    }

}
