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
public class GoobiScriptDeleteProcessTest extends AbstractTest {

    private Process process;

    private Step s1;

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
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setId(Integer.valueOf(5));

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.DONE);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.deleteProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptDeleteProcess fixture = new GoobiScriptDeleteProcess();
        assertNotNull(fixture);
        assertEquals("deleteProcess", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptDeleteProcess fixture = new GoobiScriptDeleteProcess();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows you to delete existing processes.\\naction: deleteProcess\\n\\n# Define here if just the content shall be deleted (true) or the the entire process from the database as well (false).\\ncontentOnly: false",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "deleteProcess";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("contentOnly", "true");
        GoobiScriptDeleteProcess fixture = new GoobiScriptDeleteProcess();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("deleteProcess", results.get(0).getCommand());
    }

    @Test
    public void testExecute() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "deleteProcess";
        // case 1: contentOnly and keep unknown false
        Map<String, String> parameters = new HashMap<>();
        parameters.put("contentOnly", "true");
        parameters.put("removeUnknownFiles", "false");
        GoobiScriptDeleteProcess fixture = new GoobiScriptDeleteProcess();
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
        assertEquals("Content for process could be deleted successfully using GoobiScript.", results.get(0).getResultMessage());

        // case 2: contentOnly and remove unknown false
        parameters.put("removeUnknownFiles", "true");
        results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
        assertEquals("Content for process could be deleted successfully using GoobiScript.", results.get(0).getResultMessage());

        // case 3: remove everything
        parameters.put("contentOnly", "false");
        results = fixture.prepare(processes, command, parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
        assertEquals("Process could be deleted successfully using GoobiScript.", results.get(0).getResultMessage());
    }

}
