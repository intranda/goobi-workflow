package de.sub.goobi.helper;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepManager.class, UserManager.class, UsergroupManager.class, ProcessManager.class, RulesetManager.class })
public class GoobiScriptTest {

    private List<Integer> processList;
    private Process process;

    @Before
    public void setUp() throws Exception {
        processList = new ArrayList<>();
        // process
        process = new Process();
        process.setTitel("process");
        process.setId(1);
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(false);

        List<Step> stepList = new ArrayList<>();
        Step first = new Step();
        first.setReihenfolge(1);
        first.setTitel("first");
        first.setBearbeitungsstatusEnum(StepStatus.DONE);

        stepList.add(first);

        Step second = new Step();
        second.setReihenfolge(2);
        second.setTitel("second");
        second.setBearbeitungsstatusEnum(StepStatus.DONE);

        stepList.add(second);
        process.setSchritte(stepList);
        processList.add(process.getId());

        prepareMocking();
    }

    private void prepareMocking() throws Exception {

        // swapSteps
        PowerMock.mockStatic(StepManager.class);
        StepManager.saveStep(EasyMock.anyObject(Step.class));
        StepManager.saveStep(EasyMock.anyObject(Step.class));

        // addUser
        User user = new User();
        user.setLogin("test");
        user.setId(0);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        PowerMock.mockStatic(UserManager.class);
        EasyMock.expect(UserManager.getUsers(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),  null))
        .andReturn(userList)
        .anyTimes();

        // addUsergroup
        Usergroup group = new Usergroup();
        group.setId(1);
        group.setTitel("title");
        group.setBerechtigung(3);
        List<Usergroup> usergroupList = new ArrayList<>();
        usergroupList.add(group);
        PowerMock.mockStatic(UsergroupManager.class);
        EasyMock.expect(UsergroupManager.getUsergroups(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),  null))
        .andReturn(usergroupList)
        .anyTimes();

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));

        Ruleset r = new Ruleset();
        r.setTitel("title");
        r.setDatei("file");
        r.setOrderMetadataByRuleset(false);
        List<Ruleset> rulesetList = new ArrayList<>();
        rulesetList.add(r);
        PowerMock.mockStatic(RulesetManager.class);
        EasyMock.expect(RulesetManager.getRulesets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),  null))
        .andReturn(rulesetList)
        .anyTimes();

        PowerMock.replay(RulesetManager.class);
        PowerMock.replay(ProcessManager.class);
        PowerMock.replay(UsergroupManager.class);
        PowerMock.replay(UserManager.class);
        PowerMock.replay(StepManager.class);
    }

    @Test
    public void testConstructor() {
        GoobiScript script = new GoobiScript();
        assertNotNull(script);
    }

    @Test
    public void testExecuteEmptyGoobiScript() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Integer>(), "");
        assertNull(script.myParameters);
    }

    @Test
    public void testExecuteWrongSyntax() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Integer>(), "action");
        assertEquals(0, script.myParameters.size());
    }

    @Test
    public void testExecuteUnknownAction() {
        GoobiScript script = new GoobiScript();
        script.execute(new ArrayList<Integer>(), "action:test");
        assertEquals(1, script.myParameters.size());
    }

    @Test
    public void testExecuteSwapStepsAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapSteps");
        script.execute(processList, "action:swapSteps swap1nr:1");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2 swap1title:first");
        script.execute(processList, "action:swapSteps swap1nr:NoNumber swap2nr:2 swap1title:first swap2title:second");
        script.execute(processList, "action:swapSteps swap1nr:1 swap2nr:2 swap1title:first swap2title:second");
    }

    @Test
    public void testExecuteSwapProzessesOutAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapProzessesOut");
    }

    @Test
    public void testExecuteSwapProzessesInAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:swapProzessesIn");
    }

    @Test
    public void testExecuteAddUserAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addUser");
        script.execute(processList, "action:addUser steptitle:first");
        script.execute(processList, "action:addUser steptitle:first username:user");
    }

    @Test
    public void testExecuteAddUserGroupAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addUserGroup");
        script.execute(processList, "action:addUserGroup steptitle:first");

        script.execute(processList, "action:addUserGroup steptitle:first group:group");
    }

    @Test
    public void testExecuteSetTaskPropertyAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:setTaskProperty");
        script.execute(processList, "action:setTaskProperty steptitle:first");
        script.execute(processList, "action:setTaskProperty steptitle:first property:test");
        script.execute(processList, "action:setTaskProperty steptitle:first property:wrong value:wrong");
        script.execute(processList, "action:setTaskProperty steptitle:first property:readimages value:wrong");

        script.execute(processList, "action:setTaskProperty steptitle:first property:readimages value:true");

        script.execute(processList, "action:setTaskProperty steptitle:first property:metadata value:false");
        script.execute(processList, "action:setTaskProperty steptitle:first property:automatic value:false");
        script.execute(processList, "action:setTaskProperty steptitle:first property:batch value:false");
        script.execute(processList, "action:setTaskProperty steptitle:first property:writeimages value:false");
        script.execute(processList, "action:setTaskProperty steptitle:first property:validate value:false");
        script.execute(processList, "action:setTaskProperty steptitle:first property:exportdms value:false");

    }

    @Test
    public void testSetStepStatusAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:setStepStatus");
        script.execute(processList, "action:setStepStatus steptitle:first");
        script.execute(processList, "action:setStepStatus steptitle:first status:NotANumber");
        script.execute(processList, "action:setStepStatus steptitle:first status:1");
    }

    @Test
    public void testSetStepNumberAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:setStepNumber");
        script.execute(processList, "action:setStepNumber steptitle:first");
        script.execute(processList, "action:setStepNumber steptitle:first number:NotANumber");
        script.execute(processList, "action:setStepNumber steptitle:first number:1");
    }

    @Test
    public void testAddStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addStep");
        script.execute(processList, "action:addStep steptitle:third");
        script.execute(processList, "action:addStep steptitle:third number:NotANumber");
        script.execute(processList, "action:addStep steptitle:third number:3");
    }

    @Test
    public void testDeleteStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:deleteStep");
        script.execute(processList, "action:deleteStep steptitle:wrong");
        script.execute(processList, "action:deleteStep steptitle:third");
    }

    @Test
    public void testAddShellScriptToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addShellScriptToStep");
        script.execute(processList, "action:addShellScriptToStep steptitle:first");
        script.execute(processList, "action:addShellScriptToStep steptitle:first label:label");
        script.execute(processList, "action:addShellScriptToStep steptitle:first label:label script:script");
    }

    @Test
    public void testAddModuleToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addModuleToStep");
        script.execute(processList, "action:addModuleToStep steptitle:first");
        script.execute(processList, "action:addModuleToStep steptitle:first module:module");
    }

    @Test
    public void testSetRulesetAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:setRuleset");
        script.execute(processList, "action:setRuleset ruleset:ruleset.xml");
    }

    @Test
    public void testDeleteProcessAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:deleteProcess");

        script.execute(processList, "action:deleteProcess contentOnly:true");
        script.execute(processList, "action:deleteProcess contentOnly:false");
    }

    @Test
    public void testAddPluginToStep() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "action:addPluginToStep");

        script.execute(processList, "action:addPluginToStep steptitle:");
        script.execute(processList, "action:addPluginToStep  steptitle:first");

        script.execute(processList, "action:addPluginToStep  steptitle:first plugin:");
        script.execute(processList, "action:addPluginToStep  steptitle:first plugin:plugin");

    }

}
