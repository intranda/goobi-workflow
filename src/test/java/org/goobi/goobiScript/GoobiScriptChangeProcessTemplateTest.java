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
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
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
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, ProcessManager.class, StepManager.class, PropertyManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class GoobiScriptChangeProcessTemplateTest extends AbstractTest {

    private Process process;
    private Step s1;

    private User user;

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(PropertyManager.class);

        user = new User();
        user.setVorname("firstname");
        user.setNachname("lastname");
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject(),
                EasyMock.anyString());

        process = new Process();
        process.setTitel("fixture");
        process.setId(Integer.valueOf(1));

        s1 = new Step();
        s1.setTitel("step 1");
        s1.setBearbeitungsstatusEnum(StepStatus.OPEN);
        s1.setReihenfolge(1);

        List<Step> steps = new ArrayList<>();
        steps.add(s1);
        process.setSchritte(steps);

        Process template = new Process();
        template.setId(2);
        template.setTitel("template");

        Step s2 = new Step();
        s2.setTitel("step 1");
        s2.setBearbeitungsstatusEnum(StepStatus.DONE);
        s2.setReihenfolge(1);

        List<Step> templatesteps = new ArrayList<>();
        steps.add(s2);
        template.setSchritte(templatesteps);

        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        EasyMock.expect(ProcessManager.getProcessByExactTitle(EasyMock.anyString())).andReturn(template).anyTimes();

        List<GoobiProperty> props = new ArrayList<>();

        GoobiProperty p1 = new GoobiProperty(PropertyOwnerType.PROCESS);
        p1.setPropertyName("Template");
        p1.setPropertyValue("abc");
        GoobiProperty p2 = new GoobiProperty(PropertyOwnerType.PROCESS);
        p2.setPropertyName("TemplateID");
        p2.setPropertyValue("111");
        props.add(p1);
        props.add(p2);

        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(props).anyTimes();
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(templatesteps).anyTimes();
        StepManager.deleteStep(EasyMock.anyObject());
        StepManager.deleteStep(EasyMock.anyObject());

        StepManager.saveStep(EasyMock.anyObject());
        ProcessManager.saveProcess(EasyMock.anyObject());
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GoobiScriptChangeProcessTemplate fixture = new GoobiScriptChangeProcessTemplate();
        assertNotNull(fixture);
        assertEquals("changeProcessTemplate", fixture.getAction());
    }

    @Test
    public void testSampleCall() {
        GoobiScriptChangeProcessTemplate fixture = new GoobiScriptChangeProcessTemplate();
        assertNotNull(fixture);
        assertEquals(
                "---\\n# This GoobiScript allow to adapt the workflow for a process by switching to another process template.\\naction: changeProcessTemplate\\n\\n# Use the name of the process template to use for the Goobi processes.\\ntemplateName: Manuscript_workflow",
                fixture.getSampleCall());
    }

    @Test
    public void testPrepare() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "changeProcessTemplate";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("templateName", "templateName");
        GoobiScriptChangeProcessTemplate fixture = new GoobiScriptChangeProcessTemplate();
        assertNotNull(fixture);
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);
        assertEquals(1, results.size());
        assertEquals("changeProcessTemplate", results.get(0).getCommand());
    }

    @Test
    public void testExecute() {
        List<Integer> processes = new ArrayList<>();
        processes.add(1);
        String command = "changeProcessTemplate";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("templateName", "templateName");
        GoobiScriptChangeProcessTemplate fixture = new GoobiScriptChangeProcessTemplate();
        List<GoobiScriptResult> results = fixture.prepare(processes, command, parameters);

        fixture.execute(results.get(0));
        assertEquals("Changed template for process fixture", results.get(0).getResultMessage());
        assertEquals(GoobiScriptResultType.OK, results.get(0).getResultType());

    }

}
