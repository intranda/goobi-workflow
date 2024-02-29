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
import de.sub.goobi.persistence.managers.UserManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, StepManager.class, UserManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptAddUserTest extends AbstractTest {

    private Process process;
    private Step s1;

    private User user;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(UserManager.class);

        user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setId(Integer.valueOf(1));

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.DONE);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);

        List<User> users = new ArrayList<>();
        users.add(user);

        EasyMock.expect(UserManager.getUsers(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(), EasyMock.anyObject()))
                .andReturn(users)
                .anyTimes();

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        StepManager.saveStep(EasyMock.anyObject());
        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptAddUser fixture = new GoobiScriptAddUser();
        assertNotNull(fixture);
        assertEquals("addUser", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptAddUser fixture = new GoobiScriptAddUser();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows to assign a user to an existing workflow step.\\naction: addUser\\n\\n# Title of the workflow step to be edited\\nsteptitle: Scanning\\n\\n# Login name of the user to assign to the workflow step.\\nusername: steffen",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addUser";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "steptitle");
        parameters.put("username", "username");
        GoobiScriptAddUser fixture = new GoobiScriptAddUser();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("addUser", results.get(0).getCommand());
    }

    @Test
    public void testExecute() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addUser";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "steptitle");
        parameters.put("username", "username");
        GoobiScriptAddUser fixture = new GoobiScriptAddUser();
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);

        fixture.execute(results.get(0));
        assertEquals("Step not found: steptitle", results.get(0).getResultMessage());

        parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("username", "username");
        results = fixture.prepare(processes, command, parameters);
        assertEquals(0, s1.getBenutzer().size());
        fixture.execute(results.get(0));
        assertEquals(1, s1.getBenutzer().size());

    }

}
