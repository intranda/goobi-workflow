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
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, JournalManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptAddToJournalTest extends AbstractTest {

    private Process process;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(JournalManager.class);

        User u = new User();
        u.setVorname("firstname");
        u.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(u).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setId(Integer.valueOf(1));

        JournalManager.saveJournalEntry(EasyMock.anyObject());
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptAddToJournal fixture = new GoobiScriptAddToJournal();
        assertNotNull(fixture);
        assertEquals("addToJournal", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptAddToJournal fixture = new GoobiScriptAddToJournal();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allows to add messages to the Goobi process journal.\\naction: addToJournal\\n\\n# Define the type for the message here. Possible values are: `error` `warn` `info` `debug` and `user`\\ntype: info\\n\\n# This parameter allows to define the message itself that shall be added to the process log. To write special characters like # put it into quotes.\\nmessage: \"This is my message\"",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addToJournal";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", "message");
        parameters.put("type", "debug");
        GoobiScriptAddToJournal fixture = new GoobiScriptAddToJournal();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("addToJournal", results.get(0).getCommand());
    }

    @Test
    public void testExecute() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "addToJournal";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("message", "message");
        parameters.put("type", "debug");
        GoobiScriptAddToJournal fixture = new GoobiScriptAddToJournal();
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);

        assertEquals(0, process.getJournal().size());
        fixture.execute(results.get(0));
        assertEquals("Process log updated.", results.get(0).getResultMessage());
    }

}
