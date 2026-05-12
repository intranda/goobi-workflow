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
import org.goobi.beans.Ruleset;
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
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, RulesetManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptSetRulesetTest extends AbstractTest {

    private Process process;
    private Ruleset ruleset;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(RulesetManager.class);

        User user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();

        ruleset = new Ruleset();
        ruleset.setId(1);
        ruleset.setTitel("Newspapers");

        List<Ruleset> rulesets = new ArrayList<>();
        rulesets.add(ruleset);

        process = new Process();
        process.setId(1);
        process.setTitel("Test Process");

        EasyMock.expect(RulesetManager.getRulesetByName(EasyMock.anyString())).andReturn(ruleset).anyTimes();
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptSetRuleset fixture = new GoobiScriptSetRuleset();
        assertNotNull(fixture);
        assertEquals("setRuleset", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptSetRuleset fixture = new GoobiScriptSetRuleset();
        assertNotNull(fixture.getSampleCall());
        assertTrue(fixture.getSampleCall().contains("setRuleset"));
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ruleset", "Newspapers");
        GoobiScriptSetRuleset fixture = new GoobiScriptSetRuleset();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setRuleset", parameters);
        assertEquals(1, results.size());
        assertEquals("setRuleset", results.get(0).getCommand());
    }

    @Test
    public void testPrepareWithMissingRulesetReturnsEmpty() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        GoobiScriptSetRuleset fixture = new GoobiScriptSetRuleset();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setRuleset", parameters);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testExecuteAssignsRuleset() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ruleset", "Newspapers");
        GoobiScriptSetRuleset fixture = new GoobiScriptSetRuleset();
        List<GoobiScriptResult> results = fixture.prepare(processes, "setRuleset", parameters);
        fixture.execute(results.get(0));
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());
        assertTrue(results.get(0).getResultMessage().contains("Ruleset"));
        assertEquals(ruleset, process.getRegelsatz());
    }
}
