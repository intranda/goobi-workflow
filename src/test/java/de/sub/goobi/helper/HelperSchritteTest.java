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

package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Response;
import org.easymock.EasyMock;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
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
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ StepManager.class, Helper.class, SendMail.class, HistoryManager.class, ProcessManager.class, MetadataManager.class,
        Executor.class, JwtHelper.class, JournalManager.class })

public class HelperSchritteTest extends AbstractTest {

    private Process process;
    private Step step1;
    private Step step2;
    private User user;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties");
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }

        // prepare test data
        process = MockProcess.createProcess();

        List<Step> stepList = new ArrayList<>();
        step1 = new Step();
        step1.setTitel("step 1");
        step1.setId(1);
        step1.setReihenfolge(1);
        step1.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step1.setProzess(process);
        step1.setProcessId(process.getId());
        stepList.add(step1);

        step2 = new Step();
        step2.setTitel("step 2");
        step2.setId(2);
        step2.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        step2.setProzess(process);
        step2.setProcessId(process.getId());
        step2.setReihenfolge(2);
        stepList.add(step2);

        process.setSchritte(stepList);

        user = new User();
        user.setId(1);
        user.setLogin("login");
        user.setEncryptedPassword("password");

        // mock database connection
        PowerMock.mockStatic(StepManager.class);
        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(stepList).anyTimes();
        StepManager.saveStep(EasyMock.anyObject());
        StepManager.saveStep(EasyMock.anyObject());

        PowerMock.mockStatic(HistoryManager.class);
        HistoryManager.addHistory(EasyMock.anyObject(), EasyMock.anyDouble(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        HistoryManager.addHistory(EasyMock.anyObject(), EasyMock.anyDouble(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        HistoryManager.addHistory(EasyMock.anyObject(), EasyMock.anyDouble(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());
        HistoryManager.addHistory(EasyMock.anyObject(), EasyMock.anyDouble(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt());

        EasyMock.expect(HistoryManager.getHistoryEvents(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getCurrentUser()).andReturn(user).anyTimes();
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());
        Helper.addMessageToProcessJournal(EasyMock.anyInt(), EasyMock.anyObject(LogType.class), EasyMock.anyString());

        PowerMock.mockStatic(SendMail.class);
        SendMail mail = EasyMock.createMock(SendMail.class);
        EasyMock.expect(SendMail.getInstance()).andReturn(mail).anyTimes();
        mail.sendMailToAssignedUser(EasyMock.anyObject(), EasyMock.anyObject());

        // http step
        PowerMock.mockStatic(Executor.class);
        Executor http = EasyMock.createMock(Executor.class);
        Response response = EasyMock.createMock(Response.class);
        EasyMock.expect(Executor.newInstance(EasyMock.anyObject())).andReturn(http).anyTimes();
        EasyMock.expect(http.execute(EasyMock.anyObject())).andReturn(response).anyTimes();
        PowerMock.mockStatic(JwtHelper.class);
        EasyMock.expect(JwtHelper.createApiToken(EasyMock.anyString(), EasyMock.anyObject())).andReturn("12356").anyTimes();
        PowerMock.replay(JwtHelper.class);

        PowerMock.mockStatic(JournalManager.class);
        JournalManager.saveJournalEntry(EasyMock.anyObject());

        PowerMock.mockStatic(MetadataManager.class);
        MetadataManager.updateMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));
        MetadataManager.updateJSONMetadata(EasyMock.anyInt(), EasyMock.anyObject(Map.class));

        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process).anyTimes();
        ProcessManager.updateImages(EasyMock.anyInt(), EasyMock.anyInt());
        ProcessManager.updateProcessStatus(EasyMock.anyString(), EasyMock.anyInt());
        ProcessManager.updateLastChangeDate(EasyMock.anyObject(), EasyMock.anyInt());
        ProcessManager.saveProcessInformation(EasyMock.anyObject());
        EasyMock.expectLastCall();
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
    }

    @Test
    public void testCloseStepObjectAutomatic() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
        fixture.CloseStepObjectAutomatic(step1);
        assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());
        assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());

    }

    @Test
    public void testCloseStepAndFollowingSteps() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
        fixture.closeStepAndFollowingSteps(step1, user);
        assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());
        assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());
    }

    @Test
    public void testSaveStepStatus() {
        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        HelperSchritte.saveStepStatus(step1, user);
        assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());
    }

    @Test
    public void testCloseStepObject() {
        assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
        HelperSchritte.closeStepObject(step1, 1);
        assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());
    }

    @Test
    public void testUpdateMetadataIndex() {
        assertFalse(process.isMediaFolderExists());
        HelperSchritte.updateMetadataIndex(step1);
        assertTrue(process.isMediaFolderExists());
    }

    @Test
    public void testExecuteAllScriptsForStep() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        // automatic task is paused
        process.setPauseAutomaticExecution(true);
        ShellScriptReturnValue val = fixture.executeAllScriptsForStep(step1, true);
        assertEquals(1, val.getReturnCode());

        // no script configured
        process.setPauseAutomaticExecution(false);
        val = fixture.executeAllScriptsForStep(step1, true);
        assertNull(val);

        // script is empty
        step1.setScriptname1("");
        step1.setTypAutomatischScriptpfad("");
        val = fixture.executeAllScriptsForStep(step1, true);
        assertNull(val);
    }

    @Test
    public void testRunHttpStep() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        step1.setHttpStep(true);
        step1.setTypAutomatisch(true);
        step1.setHttpEscapeBodyJson(true);
        step1.setHttpJsonBody("{\"test\": true}");
        step1.setHttpMethod("xxx");
        //        step1.setHttpUrl("http://example.com");
        step1.setHttpCloseStep(true);
        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        fixture.runHttpStep(step1);
    }

    @Test
    public void testExecuteScriptForStepObject() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        step1.setTypAutomatisch(true);
        // null script
        ShellScriptReturnValue val = fixture.executeScriptForStepObject(step1, null, false);
        assertEquals(-1, val.getReturnCode());
        // empty script
        val = fixture.executeScriptForStepObject(step1, "", false);
        assertEquals(-1, val.getReturnCode());

        val = fixture.executeScriptForStepObject(step1, " ", false);
        assertEquals(0, val.getReturnCode());

        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        val = fixture.executeScriptForStepObject(step1, " ", true);
        assertEquals(0, val.getReturnCode());
        assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());

    }

    @Test
    public void testCreateShellParamsForBashScript() throws Exception {
        List<String> values = HelperSchritte.createShellParamsForBashScript(step1, "/bin/echo {projectname}");
        assertEquals(2, values.size());
        assertEquals("/bin/echo", values.get(0));
        assertEquals("project", values.get(1));
    }

    @Test
    public void testErrorStep() {
        HelperSchritte fixture = new HelperSchritte();
        assertNotNull(fixture);
        assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
        fixture.errorStep(step1);
        assertEquals(StepStatus.ERROR, step1.getBearbeitungsstatusEnum());

    }

    @Test
    public void testExtractAuthorityMetadata() throws Exception {
        Map<String, List<String>> metadataPairs = new HashMap<>();
        HelperSchritte.extractAuthorityMetadata(Paths.get(process.getMetadataFilePath()), metadataPairs);
        assertTrue(metadataPairs.isEmpty());
    }

    @Test
    public void testExtractMetadata() throws Exception {

        Map<String, List<String>> metadataPairs = new HashMap<>();
        HelperSchritte.extractMetadata(Paths.get(process.getMetadataFilePath()), metadataPairs);
        assertEquals(7, metadataPairs.size());
        assertEquals("main title", metadataPairs.get("TitleDocMain").get(0));

    }
}
