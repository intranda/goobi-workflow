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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.managedbeans.LoginBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
public class GoobiScriptTest extends AbstractTest {

    private List<Integer> processList;
    private Process process;
    private User user;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
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

        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<UsergroupManager> mockedUsergroupManager = Mockito.mockStatic(UsergroupManager.class);
                MockedStatic<RulesetManager> mockedRulesetManager = Mockito.mockStatic(RulesetManager.class)) {
            // addUser
            user = new User();
            user.setLogin("test");
            user.setId(0);
            user.setVorname("firstname");
            user.setNachname("lastname");

            List<User> userList = new ArrayList<>();
            userList.add(user);
            mockedUserManager.when(() -> UserManager.getUsers(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.any(Institution.class))).thenReturn(userList);

            // addUsergroup
            Usergroup group = new Usergroup();
            group.setId(1);
            group.setTitel("title");
            group.setBerechtigung(3);
            List<Usergroup> usergroupList = new ArrayList<>();
            usergroupList.add(group);
            mockedUsergroupManager
                    .when(() -> UsergroupManager.getUsergroups(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                            Mockito.any(Institution.class)))
                    .thenReturn(usergroupList);

            Ruleset r = new Ruleset();
            r.setTitel("title");
            r.setDatei("file");
            r.setOrderMetadataByRuleset(false);
            List<Ruleset> rulesetList = new ArrayList<>();
            rulesetList.add(r);
            mockedRulesetManager.when(() -> RulesetManager.getRulesets(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.any(Institution.class))).thenReturn(rulesetList);

            //        Mockito.mockStatic(ProcessManager.class);
            //        Mockito.when(ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
            //        ProcessManager.saveProcess(Mockito.any());
            //        ProcessManager.deleteProcess(Mockito.any());
            //        //
            //        Mockito.mockStatic(Helper.class);
            //
            //        SessionForm sessionForm = new SessionForm();
            //        LoginBean loginBean = new LoginBean();
            //
            //        loginBean.setMyBenutzer(user);
            //        mockedHelper.when(() -> Helper.getManagedBeanValue("#{SessionForm}")).thenReturn(sessionForm);
            //        mockedHelper.when(() -> Helper.getManagedBeanValue("#{LoginForm}")).thenReturn(loginBean);
            //
            //        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
            //        Helper.addMessageToProcessJournal(Mockito.anyInt(), Mockito.any(), Mockito.anyString());
            //
            //
        }
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
    @SuppressWarnings("removal")
    public void testExecuteSwapStepsAction() throws Exception {
        Mockito.mockStatic(ProcessManager.class);
        Mockito.when(ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
        ProcessManager.saveProcess(Mockito.any());
        ProcessManager.deleteProcess(Mockito.any());
        JournalManager.saveJournalEntry(Mockito.any());

        Mockito.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapSteps");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2 \\nswap1title: first");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: NoNumber \\nswap2nr: 2 swap1title: first \\nswap2title: second");
        script.execute(processList, "---\\naction: swapSteps \\nswap1nr: 1 \\nswap2nr: 2 \\nswap1title: first \\nswap2title: second");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testExecuteSwapProzessesOutAction() throws Exception {
        Mockito.mockStatic(ProcessManager.class);
        Mockito.when(ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
        ProcessManager.saveProcess(Mockito.any());
        ProcessManager.deleteProcess(Mockito.any());
        JournalManager.saveJournalEntry(Mockito.any());

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapProzessesOut");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testExecuteSwapProzessesInAction() throws Exception {
        Mockito.mockStatic(ProcessManager.class);
        Mockito.when(ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
        ProcessManager.saveProcess(Mockito.any());
        ProcessManager.deleteProcess(Mockito.any());
        JournalManager.saveJournalEntry(Mockito.any());

        Mockito.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        Helper.addMessageToProcessJournal(Mockito.anyInt(), Mockito.any(), Mockito.anyString());

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: swapProzessesIn");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testExecuteAddUserAction() throws Exception {

        Mockito.mockStatic(ProcessManager.class);
        Mockito.when(ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process);
        ProcessManager.saveProcess(Mockito.any());
        ProcessManager.deleteProcess(Mockito.any());
        JournalManager.saveJournalEntry(Mockito.any());

        Mockito.mockStatic(Helper.class);
        LoginBean loginBean = new LoginBean();
        loginBean.setMyBenutzer(user);

        Helper.setFehlerMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Helper.setMeldung(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        Helper.addMessageToProcessJournal(Mockito.anyInt(), Mockito.any(), Mockito.anyString());

        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addUser");
        script.execute(processList, "---\\naction: addUser \\nsteptitle: first");
        script.execute(processList, "---\\naction: addUser \\nsteptitle: first \\nusername: user");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testExecuteAddUserGroupAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addUserGroup");
        script.execute(processList, "---\\naction: addUserGroup \\nsteptitle: first");

        script.execute(processList, "---\\naction: addUserGroup \\nsteptitle: first \\ngroup: group");
    }

    //@Test
    @SuppressWarnings("removal")
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
    @SuppressWarnings("removal")
    public void testSetStepStatusAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setStepStatus");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first \\nstatus: NotANumber");
        script.execute(processList, "---\\naction: setStepStatus \\nsteptitle: first \\nstatus: 1");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testSetStepNumberAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setStepNumber");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first \\nnumber: NotANumber");
        script.execute(processList, "---\\naction: setStepNumber \\nsteptitle: first \\nnumber: 1");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testAddStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addStep");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third \\nnumber: NotANumber");
        script.execute(processList, "---\\naction: addStep \\nsteptitle: third \\nnumber: 3");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testDeleteStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: deleteStep");
        script.execute(processList, "---\\naction: deleteStep \\nsteptitle: wrong");
        script.execute(processList, "---\\naction: deleteStep \\nsteptitle: third");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testAddShellScriptToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addShellScriptToStep");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first \\nlabel: label");
        script.execute(processList, "---\\naction: addShellScriptToStep \\nsteptitle: first \\nlabel: label \\nscript: script");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testAddModuleToStepAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addModuleToStep");
        script.execute(processList, "---\\naction: addModuleToStep \\nsteptitle: first");
        script.execute(processList, "---\\naction: addModuleToStep \\nsteptitle: first \\nmodule: module");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testSetRulesetAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: setRuleset");
        script.execute(processList, "---\\naction: setRuleset \\nruleset: ruleset.xml");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testDeleteProcessAction() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: deleteProcess");

        script.execute(processList, "---\\naction: deleteProcess \\ncontentOnly: true");
        script.execute(processList, "---\\naction: deleteProcess \\ncontentOnly: false");
    }

    //@Test
    @SuppressWarnings("removal")
    public void testAddPluginToStep() {
        GoobiScript script = new GoobiScript();
        script.execute(processList, "---\\naction: addPluginToStep");

        script.execute(processList, "---\\naction: addPluginToStep \\nsteptitle: ");
        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first");

        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first \\nplugin: ");
        script.execute(processList, "---\\naction: addPluginToStep  \\nsteptitle: first \\nplugin: plugin");

    }

}
