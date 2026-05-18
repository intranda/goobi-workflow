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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Response;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.metadaten.search.DatabaseMetadataField;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

@ExtendWith(MockitoExtension.class)
public class HelperSchritteTest extends AbstractTest {

    private Process process;
    private Step step1;
    private Step step2;
    private User user;

    private Executor http;
    private SendMail mail;
    private List<Step> stepList;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties");
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }

        // prepare test data
        process = MockProcess.createProcess();

        stepList = new ArrayList<>();
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

        mail = Mockito.mock(SendMail.class);

        // http step
        http = Mockito.mock(Executor.class);
        Response response = Mockito.mock(Response.class);
        Mockito.lenient().when(http.execute(Mockito.any())).thenReturn(response);

    }

    @Test
    public void testConstructor() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testCloseStepObjectAutomatic() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);
            assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
            assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
            fixture.CloseStepObjectAutomatic(step1);
            assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());
            assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testCloseStepAndFollowingSteps() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);
            assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
            assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
            fixture.closeStepAndFollowingSteps(step1, user);
            assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());
            assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testSaveStepStatus() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
            HelperSchritte.saveStepStatus(step1, user);
            assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testCloseStepObject() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            assertEquals(StepStatus.LOCKED, step2.getBearbeitungsstatusEnum());
            HelperSchritte.closeStepObject(step1, 1);
            assertEquals(StepStatus.OPEN, step2.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testUpdateMetadataIndex() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            assertFalse(process.isMediaFolderExists());
            HelperSchritte.updateMetadataIndex(step1);
            assertTrue(process.isMediaFolderExists());

        }
    }

    @Test
    public void testExecuteAllScriptsForStep() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);
            // automatic task is paused
            process.setPauseAutomaticExecution(true);
            ShellScriptReturnValue val = fixture.executeAllScriptsForStep(step1, true);
            assertEquals(1, val.getReturnCode());

        }
    }

    @Test
    public void testRunHttpStep() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

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
    }

    @Test
    public void testExecuteScriptForStepObject() {

        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);
            step1.setTypAutomatisch(true);

            // empty script
            ShellScriptReturnValue val = fixture.executeScriptForStepObject(step1, "", false);
            assertEquals(-1, val.getReturnCode());

            val = fixture.executeScriptForStepObject(step1, " ", false);
            assertEquals(0, val.getReturnCode());

            assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
            val = fixture.executeScriptForStepObject(step1, " ", true);
            assertEquals(0, val.getReturnCode());
            assertEquals(StepStatus.DONE, step1.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testCreateShellParamsForBashScript() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            List<String> values = HelperSchritte.createShellParamsForBashScript(step1, "/bin/echo {projectname}");
            assertEquals(2, values.size());
            assertEquals("/bin/echo", values.get(0));
            assertEquals("project", values.get(1));

        }
    }

    @Test
    public void testErrorStep() {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            HelperSchritte fixture = new HelperSchritte();
            assertNotNull(fixture);
            assertEquals(StepStatus.INWORK, step1.getBearbeitungsstatusEnum());
            fixture.errorStep(step1);
            assertEquals(StepStatus.ERROR, step1.getBearbeitungsstatusEnum());

        }
    }

    @Test
    public void testExtractMetadata() throws Exception {
        try (MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
                MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<Executor> mockedExecutor = Mockito.mockStatic(Executor.class);
                MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class)) {
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(stepList);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(user);
            mockedSendMail.when(() -> SendMail.getInstance()).thenReturn(mail);
            mockedExecutor.when(() -> Executor.newInstance(Mockito.any())).thenReturn(http);
            mockedJwtHelper.when(() -> JwtHelper.createApiToken(Mockito.anyString(), Mockito.any())).thenReturn("12356");
            mockedProcessManager.when(() -> ProcessManager.getProcessById(1)).thenReturn(process);

            Map<String, List<DatabaseMetadataField>> metadataPairs = new HashMap<>();
            HelperSchritte.extractMetadata(Paths.get(process.getMetadataFilePath()), metadataPairs);
            assertEquals(7, metadataPairs.size());
            assertEquals("main title", metadataPairs.get("TitleDocMain").get(0).getMetadataValue());

        }
    }
}
