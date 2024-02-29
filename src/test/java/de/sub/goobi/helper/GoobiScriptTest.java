package de.sub.goobi.helper;

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
 * 
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.enums.LogType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepManager.class, UserManager.class, UsergroupManager.class, ProcessManager.class, RulesetManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })

@SuppressWarnings("deprecation")
public class GoobiScriptTest extends AbstractTest {

    private List<Integer> processList;
    private Process process;
    private User user;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        processList = new ArrayList<>();
        // process
        process = MockProcess.createProcess();
        process.setTitel("process");
        process.setId(1);

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
        user = new User();
        user.setLogin("test");
        user.setId(0);
        user.setVorname("firstname");
        user.setNachname("lastname");

        List<User> userList = new ArrayList<>();
        userList.add(user);
        PowerMock.mockStatic(UserManager.class);
        EasyMock.expect(UserManager.getUsers(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject(Institution.class))).andReturn(userList).anyTimes();

        // addUsergroup
        Usergroup group = new Usergroup();
        group.setId(1);
        group.setTitel("title");
        group.setBerechtigung(3);
        List<Usergroup> usergroupList = new ArrayList<>();
        usergroupList.add(group);
        PowerMock.mockStatic(UsergroupManager.class);
        EasyMock.expect(UsergroupManager.getUsergroups(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject(Institution.class))).andReturn(usergroupList).anyTimes();

        Ruleset r = new Ruleset();
        r.setTitel("title");
        r.setDatei("file");
        r.setOrderMetadataByRuleset(false);
        List<Ruleset> rulesetList = new ArrayList<>();
        rulesetList.add(r);
        PowerMock.mockStatic(RulesetManager.class);
        EasyMock.expect(RulesetManager.getRulesets(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.anyObject(Institution.class))).andReturn(rulesetList).anyTimes();

        //        PowerMock.mockStatic(ProcessManager.class);
        //        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        //        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        //        PowerMock.expectLastCall().times(0, 9);
        //        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        //        PowerMock.expectLastCall().times(0, 9);
        //        PowerMock.replay(ProcessManager.class);
        //
        //        PowerMock.mockStatic(Helper.class);
        //
        //        SessionForm sessionForm = new SessionForm();
        //        LoginBean loginBean = new LoginBean();
        //
        //        loginBean.setMyBenutzer(user);
        //        EasyMock.expect(Helper.getManagedBeanValue("#{SessionForm}")).andReturn(sessionForm).anyTimes();
        //        EasyMock.expect(Helper.getManagedBeanValue("#{LoginForm}")).andReturn(loginBean).anyTimes();
        //
        //        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        //        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());
        //
        //        PowerMock.replay(Helper.class);

        PowerMock.replay(RulesetManager.class);

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
    public void testParseEmptyGoobiScript() {
        assertNull(GoobiScript.parseGoobiscripts("---\\n"));
    }

    //@Test
    public void testParseWrongSyntax() {
        assertEquals(0, GoobiScript.parseGoobiscripts("---\\naction").size());
    }

    @Test
    public void testParseUnknownAction() {
        assertEquals(1, GoobiScript.parseGoobiscripts("---\\naction: test").size());
    }

    //@Test
    public void testExecuteSwapStepsAction() throws Exception {
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        JournalManager.saveJournalEntry(EasyMock.anyObject(JournalEntry.class));
        PowerMock.replay(ProcessManager.class);

        PowerMock.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        PowerMock.expectLastCall().anyTimes();
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        PowerMock.replay(Helper.class);

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapSteps");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2 \\nswap1title: first");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: NoNumber \\nswap2nr: 2 swap1title: first \\nswap2title: second");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2 \\nswap1title: first \\nswap2title: second");
    }

    //@Test
    public void testExecuteSwapProzessesOutAction() throws Exception {
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        JournalManager.saveJournalEntry(EasyMock.anyObject(JournalEntry.class));
        PowerMock.replay(ProcessManager.class);

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapProzessesOut");
    }

    //@Test
    public void testExecuteSwapProzessesInAction() throws Exception {
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().times(0, 9);
        JournalManager.saveJournalEntry(EasyMock.anyObject(JournalEntry.class));
        PowerMock.replay(ProcessManager.class);

        PowerMock.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());

        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());

        PowerMock.expectLastCall().anyTimes();

        PowerMock.replay(Helper.class);
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapProzessesIn");
    }

    //@Test
    public void testExecuteAddUserAction() throws Exception {

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process).anyTimes();
        ProcessManager.saveProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().anyTimes();
        ProcessManager.deleteProcess(EasyMock.anyObject(Process.class));
        PowerMock.expectLastCall().anyTimes();
        JournalManager.saveJournalEntry(EasyMock.anyObject(JournalEntry.class));
        PowerMock.replay(ProcessManager.class);

        PowerMock.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setFehlerMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());

        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());

        PowerMock.expectLastCall().anyTimes();

        PowerMock.replay(Helper.class);

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addUser");
        script.execute(processList, "---\\naction: addUser \\nsteptitle: first");
        script.execute(processList, "---\\naction: addUser \\nsteptitle: first \\nusername: user");
    }

    //@Test
    public void testExecuteAddUserGroupAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addUserGroup");
        script.execute(processList, "---\\naction: addUserGroup \\nsteptitle: first");

        script.execute(processList, "---\\naction: addUserGroup \\nsteptitle: first \\ngroup: group");
    }

    //@Test
    public void testExecuteSetStepPropertyAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setStepProperty");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: test");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: wrong \\nvalue: wrong");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: readimages \\nvalue: wrong");

        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: readimages \\nvalue: true");

        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: metadata \\nvalue: false");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: automatic \\nvalue: false");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: batch \\nvalue: false");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: writeimages \\nvalue: false");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: validate \\nvalue: false");
        script.execute(processList, "---\\naction: setStepProperty \\nsteptitle: first \\nproperty: exportdms \\nvalue: false");

    }

    //@Test
    public void testSetStepStatusAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setStepStatus");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first \\nstatus: NotANumber");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first \\nstatus: 1");
    }

    //@Test
    public void testSetStepNumberAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setStepNumber");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first \\nnumber: NotANumber");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first \\nnumber: 1");
    }

    //@Test
    public void testAddStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addStep");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third \\nnumber: NotANumber");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third \\nnumber: 3");
    }

    //@Test
    public void testDeleteStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: deleteStep");
        script.execute(processList, "---\\naction: deleteStep \\nsteptitle: wrong");
        script.execute(processList, "---\\naction: deleteStep \\nsteptitle: third");
    }

    //@Test
    public void testAddShellScriptToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addShellScriptToStep");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first \\nlabel: label");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first \\nlabel: label \\nscript: script");
    }

    //@Test
    public void testAddModuleToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addModuleToStep");
        script.execute(processList, "---\\naction: addModuleToStep \\nsteptitle: first");
        script.execute(processList, "---\\naction: addModuleToStep \\nsteptitle: first \\nmodule: module");
    }

    //@Test
    public void testSetRulesetAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setRuleset");
        script.execute(processList, "---\\naction: setRuleset \\nruleset: ruleset.xml");
    }

    //@Test
    public void testDeleteProcessAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: deleteProcess");

        script.execute(processList, "---\\naction: deleteProcess \\ncontentOnly: true");
        script.execute(processList, "---\\naction: deleteProcess \\ncontentOnly: false");
    }

    //@Test
    public void testAddPluginToStep() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addPluginToStep");

        script.execute(processList, "---\\naction: addPluginToStep \\nsteptitle: ");
        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first");

        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first \\nplugin: ");
        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first \\nplugin: plugin");

    }

}
