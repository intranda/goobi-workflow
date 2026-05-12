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
import org.goobi.beans.Usergroup;
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
import de.sub.goobi.persistence.managers.UsergroupManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, StepManager.class, UsergroupManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptDeleteUserGroupTest extends AbstractTest {

    private Process process;
    private Step s1;
    private Usergroup usergroup;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(UsergroupManager.class);

        User user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();

        usergroup = new Usergroup();
        usergroup.setTitel("Photographers");

        List<Usergroup> groups = new ArrayList<>();
        groups.add(usergroup);

        EasyMock.expect(UsergroupManager.getUsergroupByName(EasyMock.anyString())).andReturn(usergroup).anyTimes();

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
        StepManager.removeUsergroupFromStep(EasyMock.anyObject(), EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        assertNotNull(fixture);
        assertEquals("deleteUserGroup", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        assertNotNull(fixture.getSampleCall());
        assertTrue(fixture.getSampleCall().contains("deleteUserGroup"));
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("group", "Photographers");
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        List<GoobiScriptResult> results = fixture.prepare(processes, "deleteUserGroup", parameters);
        assertEquals(1, results.size());
        assertEquals("deleteUserGroup", results.get(0).getCommand());
    }

    @Test
    public void testPrepareWithMissingSteptitleReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("group", "Photographers");
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        List<GoobiScriptResult> results = fixture.prepare(processes, "deleteUserGroup", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testExecuteStepFoundUsergroupNotAssigned() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "step 1");
        parameters.put("group", "Photographers");
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        List<GoobiScriptResult> results = fixture.prepare(processes, "deleteUserGroup", parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
    }

    @Test
    public void testExecuteStepNotFound() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("steptitle", "nonexistent");
        parameters.put("group", "Photographers");
        GoobiScriptDeleteUserGroup fixture = new GoobiScriptDeleteUserGroup();
        List<GoobiScriptResult> results = fixture.prepare(processes, "deleteUserGroup", parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.ERROR, results.get(0).getResultType());
        assertTrue(results.get(0).getResultMessage().contains("nonexistent"));
    }
}
