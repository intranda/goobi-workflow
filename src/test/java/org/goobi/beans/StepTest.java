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

package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.goobi.api.mq.QueueType;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;

public class StepTest extends AbstractTest {

    @Test
    public void testId() {
        Step step = new Step();
        step.setId(42);
        assertEquals(42, step.getId().intValue());
    }

    @Test
    public void testTitel() {
        Step step = new Step();
        step.setTitel("Test Step");
        assertEquals("Test Step", step.getTitel());
    }

    @Test
    public void testPrioritaet() {
        Step step = new Step();
        step.setPrioritaet(10);
        assertEquals(10, step.getPrioritaet().intValue());
    }

    @Test
    public void testReihenfolge() {
        Step step = new Step();
        step.setReihenfolge(42);
        assertEquals(42, step.getReihenfolge().intValue());
    }

    @Test
    public void testBearbeitungsstatus() {
        Step step = new Step();
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        assertSame(StepStatus.OPEN, step.getBearbeitungsstatusEnum());
        step.setBearbeitungsstatusAsString("5");// = StepStatus.DEACTIVATED
        assertEquals("5", step.getBearbeitungsstatusAsString());
    }

    @Test
    public void testBearbeitungszeitpunkt() {
        Step step = new Step();
        Date now = new Date();
        step.setBearbeitungszeitpunkt(now);
        assertSame(now, step.getBearbeitungszeitpunkt());
    }

    @Test
    public void testBearbeitungsbeginn() {
        Step step = new Step();
        Date now = new Date();
        step.setBearbeitungsbeginn(now);
        assertSame(now, step.getBearbeitungsbeginn());
    }

    @Test
    public void testBearbeitungsende() {
        Step step = new Step();
        Date now = new Date();
        step.setBearbeitungsende(now);
        assertSame(now, step.getBearbeitungsende());
    }

    @Test
    public void testEditType() {
        Step step = new Step();
        step.setEditTypeEnum(StepEditType.ADMIN);
        assertSame(StepEditType.ADMIN, step.getEditTypeEnum());
    }

    // Note: This test method tests 'bearbeitungsbenutzer' and 'userId' because they have the same setter and getter.
    @Test
    public void testBearbeitungsbenutzerAndUserId() {
        Step step = new Step();

        // Test the userId directly:
        step.setUserId(42);
        assertEquals(42, step.getUserId().intValue());

        // Test with a user with an own id:
        User testUser = new User();
        testUser.setId(32);
        step.setBearbeitungsbenutzer(testUser);
        assertSame(testUser, step.getBearbeitungsbenutzer());
        assertEquals(32, step.getUserId().intValue());

        // Test with 'null' user:
        step.setBearbeitungsbenutzer(null);
        assertNull(step.getBearbeitungsbenutzer());
        assertNull(step.getUserId());
    }

    @Test
    public void testHomeverzeichnisNutzen() {
        Step step = new Step();
        step.setHomeverzeichnisNutzen((short) (42));
        assertEquals((short) (42), step.getHomeverzeichnisNutzen());
    }

    @Test
    public void testTypMetadaten() {
        Step step = new Step();
        step.setTypMetadaten(false);
        assertFalse(step.isTypMetadaten());
        step.setTypMetadaten(true);
        assertTrue(step.isTypMetadaten());
    }

    @Test
    public void testTypAutomatisch() {
        Step step = new Step();
        step.setTypAutomatisch(false);
        assertFalse(step.isTypAutomatisch());
        step.setTypAutomatisch(true);
        assertTrue(step.isTypAutomatisch());
    }

    @Test
    public void testTypAutomaticThumbnail() {
        Step step = new Step();
        step.setTypAutomaticThumbnail(false);
        assertFalse(step.isTypAutomaticThumbnail());
        step.setTypAutomaticThumbnail(true);
        assertTrue(step.isTypAutomaticThumbnail());
    }

    @Test
    public void testAutomaticThumbnailSettingsYaml() {
        Step step = new Step();
        step.setAutomaticThumbnailSettingsYaml("test setting");
        assertEquals("test setting", step.getAutomaticThumbnailSettingsYaml());
    }

    @Test
    public void testTypImportFileUpload() {
        Step step = new Step();
        step.setTypImportFileUpload(false);
        assertFalse(step.isTypImportFileUpload());
        step.setTypImportFileUpload(true);
        assertTrue(step.isTypImportFileUpload());
    }

    @Test
    public void testTypExportRus() {
        Step step = new Step();
        step.setTypExportRus(false);
        assertFalse(step.isTypExportRus());
        step.setTypExportRus(true);
        assertTrue(step.isTypExportRus());
    }

    @Test
    public void testTypImagesLesen() {
        Step step = new Step();
        step.setTypImagesLesen(false);
        assertFalse(step.isTypImagesLesen());
        step.setTypImagesLesen(true);
        assertTrue(step.isTypImagesLesen());
    }

    @Test
    public void testTypImagesSchreiben() {
        Step step = new Step();
        step.setTypImagesSchreiben(false);
        assertFalse(step.isTypImagesSchreiben());
        step.setTypImagesSchreiben(true);
        assertTrue(step.isTypImagesSchreiben());
    }

    @Test
    public void testTypExportDMS() {
        Step step = new Step();
        step.setTypExportDMS(false);
        assertFalse(step.isTypExportDMS());
        step.setTypExportDMS(true);
        assertTrue(step.isTypExportDMS());
    }

    @Test
    public void testTypBeimAnnehmenModul() {
        Step step = new Step();
        step.setTypBeimAnnehmenModul(false);
        assertFalse(step.isTypBeimAnnehmenModul());
        step.setTypBeimAnnehmenModul(true);
        assertTrue(step.isTypBeimAnnehmenModul());
    }

    @Test
    public void testTypBeimAnnehmenAbschliessen() {
        Step step = new Step();
        step.setTypBeimAnnehmenAbschliessen(false);
        assertFalse(step.isTypBeimAnnehmenAbschliessen());
        step.setTypBeimAnnehmenAbschliessen(true);
        assertTrue(step.isTypBeimAnnehmenAbschliessen());
    }

    @Test
    public void testTypBeimAnnehmenModulUndAbschliessen() {
        Step step = new Step();
        step.setTypBeimAnnehmenModulUndAbschliessen(false);
        assertFalse(step.isTypBeimAnnehmenModulUndAbschliessen());
        step.setTypBeimAnnehmenModulUndAbschliessen(true);
        assertTrue(step.isTypBeimAnnehmenModulUndAbschliessen());
    }

    @Test
    public void testTypScriptStep() {
        Step step = new Step();
        step.setTypScriptStep(false);
        assertFalse(step.isTypScriptStep());
        step.setTypScriptStep(true);
        assertTrue(step.isTypScriptStep());
    }

    @Test
    public void testScriptname1() {
        Step step = new Step();
        step.setScriptname1("script 1");
        assertEquals("script 1", step.getScriptname1());
    }

    @Test
    public void testTypAutomatischScriptpfad() {
        Step step = new Step();
        step.setTypAutomatischScriptpfad("typ 1");
        assertEquals("typ 1", step.getTypAutomatischScriptpfad());
    }

    @Test
    public void testScriptname2() {
        Step step = new Step();
        step.setScriptname2("script 2");
        assertEquals("script 2", step.getScriptname2());
    }

    @Test
    public void testTypAutomatischScriptpfad2() {
        Step step = new Step();
        step.setTypAutomatischScriptpfad2("typ 2");
        assertEquals("typ 2", step.getTypAutomatischScriptpfad2());
    }

    @Test
    public void testScriptname3() {
        Step step = new Step();
        step.setScriptname3("script 3");
        assertEquals("script 3", step.getScriptname3());
    }

    @Test
    public void testTypAutomatischScriptpfad3() {
        Step step = new Step();
        step.setTypAutomatischScriptpfad3("typ 3");
        assertEquals("typ 3", step.getTypAutomatischScriptpfad3());
    }

    @Test
    public void testScriptname4() {
        Step step = new Step();
        step.setScriptname4("script 4");
        assertEquals("script 4", step.getScriptname4());
    }

    @Test
    public void testTypAutomatischScriptpfad4() {
        Step step = new Step();
        step.setTypAutomatischScriptpfad4("typ 4");
        assertEquals("typ 4", step.getTypAutomatischScriptpfad4());
    }

    @Test
    public void testScriptname5() {
        Step step = new Step();
        step.setScriptname5("script 5");
        assertEquals("script 5", step.getScriptname5());
    }

    @Test
    public void testTypAutomatischScriptpfad5() {
        Step step = new Step();
        step.setTypAutomatischScriptpfad5("typ 5");
        assertEquals("typ 5", step.getTypAutomatischScriptpfad5());
    }

    @Test
    public void testTypBeimAbschliessenVerifizieren() {
        Step step = new Step();
        step.setTypBeimAbschliessenVerifizieren(false);
        assertFalse(step.isTypBeimAbschliessenVerifizieren());
        step.setTypBeimAbschliessenVerifizieren(true);
        assertTrue(step.isTypBeimAbschliessenVerifizieren());
    }

    @Test
    public void testBatchStep() {
        Step step = new Step();

        // Test with null:
        step.setBatchStep(null);
        assertFalse(step.isBatchStep());
        assertFalse(step.getBatchStep());

        // Test with false:
        step.setBatchStep(false);
        assertFalse(step.isBatchStep());
        assertFalse(step.getBatchStep());

        // Test with true:
        step.setBatchStep(true);
        assertTrue(step.isBatchStep());
        assertTrue(step.getBatchStep());
    }

    @Test
    public void testBatchSize() {
        Step step = new Step();
        step.setBatchSize(false);
        assertFalse(step.isBatchSize());
        step.setBatchSize(true);
        assertTrue(step.isBatchSize());
    }

    @Test
    public void testHttpStep() {
        Step step = new Step();
        step.setHttpStep(false);
        assertFalse(step.isHttpStep());
        step.setHttpStep(true);
        assertTrue(step.isHttpStep());
    }

    @Test
    public void testHttpUrl() {
        Step step = new Step();
        step.setHttpUrl("www.example.com");
        assertEquals("www.example.com", step.getHttpUrl());
    }

    @Test
    public void testHttpMethod() {
        Step step = new Step();
        step.setHttpMethod("GET");
        assertEquals("GET", step.getHttpMethod());
    }

    @Test
    public void testPossibleHttpMethods() {
        Step step = new Step();
        // Test default value:
        String[] defaultMethods = step.getPossibleHttpMethods();
        assertEquals(4, defaultMethods.length);
        assertEquals("POST", defaultMethods[0]);
        assertEquals("PUT", defaultMethods[1]);
        assertEquals("PATCH", defaultMethods[2]);
        assertEquals("GET", defaultMethods[3]);

        // Test custom value:
        String[] exampleMethods = new String[] { "GET", "PUT", "POST", "DELETE", "OPTIONS" };
        step.setPossibleHttpMethods(exampleMethods);
        String[] customMethods = step.getPossibleHttpMethods();
        assertEquals(5, customMethods.length);
        assertEquals("GET", customMethods[0]);
        assertEquals("PUT", customMethods[1]);
        assertEquals("POST", customMethods[2]);
        assertEquals("DELETE", customMethods[3]);
        assertEquals("OPTIONS", customMethods[4]);
    }

    @Test
    public void testHttpJsonBody() {
        Step step = new Step();
        String json = "{\"key\":\"value\"}";
        step.setHttpJsonBody(json);
        assertEquals(json, step.getHttpJsonBody());
    }

    @Test
    public void testHttpCloseStep() {
        Step step = new Step();
        step.setHttpCloseStep(false);
        assertFalse(step.isHttpCloseStep());
        step.setHttpCloseStep(true);
        assertTrue(step.isHttpCloseStep());
    }

    @Test
    public void testHttpEscapeBodyJson() {
        Step step = new Step();
        step.setHttpEscapeBodyJson(false);
        assertFalse(step.isHttpEscapeBodyJson());
        step.setHttpEscapeBodyJson(true);
        assertTrue(step.isHttpEscapeBodyJson());
    }

    @Test
    public void testProzess() {
        Step step = new Step();
        Process process = new Process();
        step.setProzess(process);
        assertSame(process, step.getProzess());
    }

    @Test
    public void testProcessId() {
        Step step = new Step();
        step.setProcessId(42);
        assertEquals(42, step.getProcessId().intValue());
    }

    @Test
    public void testEigenschaften() {
        Step step = new Step();

        // Prepare the list:
        List<GoobiProperty> list = new ArrayList<>();
        GoobiProperty item1 = new GoobiProperty(PropertyOwnerType.ERROR);
        GoobiProperty item2 = new GoobiProperty(PropertyOwnerType.ERROR);
        list.add(item1);
        list.add(item2);

        // Test the returned list:
        step.setEigenschaften(list);
        assertEquals(2, step.getEigenschaften().size());
        assertTrue(step.getEigenschaften().contains(item1));
        assertTrue(step.getEigenschaften().contains(item2));

        // Test some custom methods:
        assertEquals(2, step.getEigenschaftenSize());
        assertSame(list, step.getEigenschaftenList());
    }

    @Test
    public void testBenutzer() {
        Step step = new Step();

        // Prepare the list:
        List<User> list = new ArrayList<>();
        User item1 = new User();
        User item2 = new User();
        list.add(item1);
        list.add(item2);

        // Test the returned list:
        step.setBenutzer(list);
        assertEquals(2, step.getBenutzer().size());
        assertTrue(step.getBenutzer().contains(item1));
        assertTrue(step.getBenutzer().contains(item2));

        // Test some custom methods:
        assertEquals(2, step.getBenutzerSize());
        assertSame(list, step.getBenutzerList());
    }

    @Test
    public void testBenutzergruppen() {
        Step step = new Step();

        // Prepare the list:
        List<Usergroup> list = new ArrayList<>();
        Usergroup item1 = new Usergroup();
        Usergroup item2 = new Usergroup();
        list.add(item1);
        list.add(item2);

        // Because "usergroup.equals(otherUsergroup) is called internally, some values must be non-null:
        item1.setTitel("Group 1");
        item1.setInstitutionId(0);
        item2.setTitel("Group 2");
        item2.setInstitutionId(1);

        // Test the returned list:
        step.setBenutzergruppen(list);
        assertEquals(2, step.getBenutzergruppen().size());
        assertTrue(step.getBenutzergruppen().contains(item1));
        assertTrue(step.getBenutzergruppen().contains(item2));

        // Test some custom methods:
        assertEquals(2, step.getBenutzergruppenSize());
        assertSame(list, step.getBenutzergruppenList());
    }

    @Test
    public void testPanelAusgeklappt() {
        Step step = new Step();
        step.setPanelAusgeklappt(false);
        assertFalse(step.isPanelAusgeklappt());
        step.setPanelAusgeklappt(true);
        assertTrue(step.isPanelAusgeklappt());
    }

    @Test
    public void testSelected() {
        Step step = new Step();
        step.setSelected(false);
        assertFalse(step.isSelected());
        step.setSelected(true);
        assertTrue(step.isSelected());
    }

    /**
     * This method is explicitly constructed with java.util.GregorianCalendar because java.util.Date is deprecated
     */
    @Test
    public void testGetBearbeitungsbeginnUndEndeAsFormattedString() {
        Step step = new Step();

        // Create non-deprecated date object:
        GregorianCalendar calendar = new GregorianCalendar();
        // month 5 is 0-based: 0 = January, ..., 5 = June
        calendar.set(2023, 5, 12, 10, 0, 0);
        long timestamp = calendar.getTimeInMillis();
        Date testDate = new Date(timestamp);

        step.setBearbeitungsbeginn(testDate);
        step.setBearbeitungsende(testDate);

        assertTrue(step.getBearbeitungsbeginnAsFormattedString().contains("10:00:00"));
        assertTrue(step.getBearbeitungsendeAsFormattedString().contains("10:00:00"));
    }

    @Test
    public void testStepPlugin() {
        Step step = new Step();
        step.setStepPlugin("plugin-name");
        assertEquals("plugin-name", step.getStepPlugin());
    }

    @Test
    public void testValidationPlugin() {
        Step step = new Step();
        step.setValidationPlugin("plugin-name");
        assertEquals("plugin-name", step.getValidationPlugin());
    }

    @Test
    public void testDelayStep() {
        Step step = new Step();
        step.setDelayStep(false);
        assertFalse(step.isDelayStep());
        step.setDelayStep(true);
        assertTrue(step.isDelayStep());
    }

    @Test
    public void testUpdateMetadataIndex() {
        Step step = new Step();
        step.setUpdateMetadataIndex(false);
        assertFalse(step.isUpdateMetadataIndex());
        step.setUpdateMetadataIndex(true);
        assertTrue(step.isUpdateMetadataIndex());
    }

    @Test
    public void testGenerateDocket() {
        Step step = new Step();
        step.setGenerateDocket(false);
        assertFalse(step.isGenerateDocket());
        step.setGenerateDocket(true);
        assertTrue(step.isGenerateDocket());
    }

    @Test
    public void testMessageQueue() {
        Step step = new Step();

        // Test with null:
        step.setMessageQueue(null);
        assertSame(QueueType.NONE, step.getMessageQueue());

        // Test with NONE:
        step.setMessageQueue(QueueType.NONE);
        assertSame(QueueType.NONE, step.getMessageQueue());

        // Test with other type:
        step.setMessageQueue(QueueType.COMMAND_QUEUE);
        assertSame(QueueType.COMMAND_QUEUE, step.getMessageQueue());
    }

    @Test
    public void testConstructor() {
        Step step = new Step();
        assertEquals("", step.getTitel());
        assertNotNull(step.getEigenschaften());
        assertNotNull(step.getBenutzer());
        assertNotNull(step.getBenutzergruppen());
        assertEquals(0, step.getPrioritaet().intValue());
        assertEquals(0, step.getReihenfolge().intValue());
        assertEquals("", step.getHttpJsonBody());
        assertSame(StepStatus.LOCKED, step.getBearbeitungsstatusEnum());
    }

    @Test
    public void testProcessConstructor() {

        // For an empty process, the step order is automatically set to 1:
        Process emptyProcess = new Process();
        Step step = new Step(emptyProcess);
        assertEquals(1, step.getReihenfolge().intValue());

        // Process with some steps (with order 3 and 5), new step should get 6:
        List<Step> steps = new ArrayList<>();
        Step step1 = new Step();
        step1.setReihenfolge(3);
        steps.add(step1);
        Step step2 = new Step();
        step2.setReihenfolge(5);
        steps.add(step2);
        Process testProcess = new Process();
        testProcess.setSchritte(steps);

        // Here, the new step is created and should get order 6:
        Step testStep = new Step(testProcess);
        assertEquals(6, testStep.getReihenfolge().intValue());
    }

    @Test
    public void testGetAutoThumbnailSettingsJSON() {
        Step step = new Step();
        step.setAutomaticThumbnailSettingsYaml("key1: value1\nkey2: value2");
        JSONObject json = step.getAutoThumbnailSettingsJSON();
        Object value1 = json.get("key1");
        Object value2 = json.get("key2");
        assertEquals("value1", value1);
        assertEquals("value2", value2);
    }

    @Ignore("A test on this method would require database and message queue access.")
    @Test
    public void testSubmitAutomaticThumbnailTicket() {
        // TODO: Can the method 'submitAutomaticThumbnailTicket()' be tested?
        //Step step = new Step();
        //step.submitAutomaticThumbnailTicket();
        assertTrue(true);
    }

    @Test
    public void testBearbeitungszeitpunktNow() {
        Step step = new Step();

        // Prepare some time data, setBearbeitungszeitpunktNow uses also 'new Date()':
        Date before = new Date();
        step.setBearbeitungszeitpunktNow(0);
        Date after = new Date();

        // Test whether 'before' <= 'now' <= 'after'
        assertTrue(before.compareTo(step.getBearbeitungszeitpunkt()) <= 0);
        assertTrue(step.getBearbeitungszeitpunkt().compareTo(after) <= 0);

        assertEquals(1, step.getBearbeitungszeitpunktNow());
    }

    @Test
    public void testCorrectionStep() {
        Step step = new Step();

        // 'prioritaet' for correction step would be 10:
        step.setPrioritaet(0);
        assertFalse(step.isCorrectionStep());

        step.setCorrectionStep();
        assertTrue(step.isCorrectionStep());
    }

    // TODO: This test is ignored because the java.util.ResourceBundle is not loaded in unit tests
    @Ignore("Resource Bundle is not loaded during unit tests")
    @Test
    public void testGetTitelLokalisiert() {
        Step step = new Step();
        step.setTitel("Test step title");
        String translated = step.getTitelLokalisiert();
        // This should be equal because the test title is definitely not translated:
        assertEquals("Test step title", translated);
    }

    @Test
    public void testGetNormalizedTitle() {
        Step step = new Step();
        step.setTitel("Test step title");
        String normalized = step.getNormalizedTitle();
        assertEquals("Test_step_title", normalized);
    }

    // TODO: To test this method, the "send mail algorithm" must be separated from the "set status logic"
    @Ignore("This method can not be tested because logic depends on database access")
    @Test
    public void testSetBearbeitungsstatusUp() {
        Step step = new Step();

        // Following properties must be non-null (because e-mails are sent to empty user lists):
        step.setTitel("");
        Project project = new Project();
        project.setId(-1);
        Process process = new Process();
        process.setProjekt(project);
        step.setProzess(process);

        step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.OPEN, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.INWORK, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.DONE, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.DONE);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.DONE, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.ERROR);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.DONE, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.DEACTIVATED);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.DEACTIVATED, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);
        step.setBearbeitungsstatusUp();
        assertSame(StepStatus.DONE, step.getBearbeitungsstatusEnum());
    }

    // TODO: To test this method, the "send mail algorithm" must be separated from the "set status logic"
    @Ignore("This method can not be tested because logic depends on database access")
    @Test
    public void testSetBearbeitungsstatusDown() {
        Step step = new Step();

        // Following properties must be non-null (because e-mails are sent to empty user lists):
        step.setTitel("");
        Project project = new Project();
        project.setId(-1);
        Process process = new Process();
        process.setProjekt(project);
        step.setProzess(process);

        step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.LOCKED, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.LOCKED, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.OPEN, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.DONE);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.INWORK, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.ERROR);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.OPEN, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.DEACTIVATED);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.DEACTIVATED, step.getBearbeitungsstatusEnum());

        step.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);
        step.setBearbeitungsstatusDown();
        assertSame(StepStatus.OPEN, step.getBearbeitungsstatusEnum());
    }

    @Test
    public void testGetTitelMitBenutzername() {

        // Test title without user name:
        Step step = new Step();
        step.setTitel("Test Title");
        step.setBearbeitungsbenutzer(null);
        assertEquals("Test Title", step.getTitelMitBenutzername());

        // Test title with user name:
        User user = new User();
        user.setVorname("John");
        user.setNachname("Doe");
        user.setId(4);
        step.setBearbeitungsbenutzer(user);
        assertEquals("Test Title (Doe, John)", step.getTitelMitBenutzername());
    }

    @Test
    public void testGetAllScriptPaths() {
        Step step1 = new Step();
        assertEquals(0, step1.getAllScriptPaths().size());

        Step step2 = new Step();
        step2.setTypAutomatischScriptpfad("A");
        step2.setTypAutomatischScriptpfad2("B");
        step2.setTypAutomatischScriptpfad3("C");
        step2.setTypAutomatischScriptpfad4("D");
        step2.setTypAutomatischScriptpfad5("E");
        List<String> scripts = step2.getAllScriptPaths();
        assertEquals(5, scripts.size());
        assertTrue(scripts.contains("A"));
        assertTrue(scripts.contains("B"));
        assertTrue(scripts.contains("C"));
        assertTrue(scripts.contains("D"));
        assertTrue(scripts.contains("E"));
    }

    @Test
    public void testAllScripts() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name A", "script A");
        map.put("name B", "script B");
        map.put("name C", "script C");
        map.put("name D", "script D");
        map.put("name E", "script E");

        Step step = new Step();
        step.setAllScripts(map);
        Map<String, String> returnedMap = step.getAllScripts();
        Set<String> set = returnedMap.keySet();
        Object[] keyObjects = set.toArray();
        assertEquals(5, keyObjects.length);

        assertEquals("name A", keyObjects[0]);
        assertEquals("name B", keyObjects[1]);
        assertEquals("name C", keyObjects[2]);
        assertEquals("name D", keyObjects[3]);
        assertEquals("name E", keyObjects[4]);

        assertEquals("script A", returnedMap.get(keyObjects[0]));
        assertEquals("script B", returnedMap.get(keyObjects[1]));
        assertEquals("script C", returnedMap.get(keyObjects[2]));
        assertEquals("script D", returnedMap.get(keyObjects[3]));
        assertEquals("script E", returnedMap.get(keyObjects[4]));
    }

    @Test
    public void testGetListOfPaths() {
        // Test with null scriptnames
        Step step1 = new Step();
        step1.setScriptname1(null);
        step1.setScriptname2(null);
        step1.setScriptname3(null);
        step1.setScriptname4(null);
        step1.setScriptname5(null);
        assertEquals("", step1.getListOfPaths());

        // Test with partial null scriptnames
        Step step2 = new Step();
        step2.setScriptname1("1");
        step2.setScriptname2(null);
        step2.setScriptname3("2");
        step2.setScriptname4(null);
        step2.setScriptname5("3");
        assertEquals("1; 2; 3", step2.getListOfPaths());

        // Test with non-null scriptnames
        Step step3 = new Step();
        step3.setScriptname1("A");
        step3.setScriptname2("B");
        step3.setScriptname3("C");
        step3.setScriptname4("D");
        step3.setScriptname5("E");
        assertEquals("A; B; C; D; E", step3.getListOfPaths());
    }

    @Test
    public void testCompareTo() {
        Step step1A = new Step();
        step1A.setId(1);
        Step step1B = new Step();
        step1B.setId(1);
        Step step2 = new Step();
        step2.setId(2);
        assertEquals(0, step1A.compareTo(step1A));
        assertEquals(0, step1A.compareTo(step1B));
        assertEquals(0, step2.compareTo(step2));
        assertTrue(step1A.compareTo(step2) < 0);
        assertTrue(step2.compareTo(step1B) > 0);
    }

    @Test
    public void testEquals() {

        // Prepare some steps:
        Step step = new Step();
        step.setId(0);
        Step equalStep = new Step();
        equalStep.setId(0);
        Step differentStep = new Step();
        differentStep.setId(1);

        // Test whether null and object are different:
        assertNotEquals(step, null);
        assertNotEquals(step, new Object());

        // Compare steps:
        assertEquals(step, step);
        assertEquals(step, equalStep);
        assertNotEquals(step, differentStep);
    }

    @Test
    public void testHashCode() {
        // TODO: Implement the method step.hashCode() first
        // The current test behavior tests whether object is the same or not
        Step step1 = new Step();
        Step step2 = new Step();
        assertEquals(step1.hashCode(), step1.hashCode());
        assertNotEquals(step1.hashCode(), step2.hashCode());
    }

    @Test
    public void testIsTypeSpecified() {
        Step step = new Step();
        step.setMessageQueue(QueueType.NONE);
        assertFalse(step.isTypeSpecified());

        Step metadataStep = new Step();
        metadataStep.setTypMetadaten(true);
        assertTrue(metadataStep.isTypeSpecified());

        Step importFileUploadStep = new Step();
        importFileUploadStep.setTypImportFileUpload(true);
        assertTrue(importFileUploadStep.isTypeSpecified());

        Step exportDMSStep = new Step();
        exportDMSStep.setTypExportDMS(true);
        assertTrue(exportDMSStep.isTypeSpecified());

        Step closeOnAcceptStep = new Step();
        closeOnAcceptStep.setTypBeimAnnehmenAbschliessen(true);
        assertTrue(closeOnAcceptStep.isTypeSpecified());

        Step onAcceptModuleStep = new Step();
        onAcceptModuleStep.setTypBeimAnnehmenModul(true);
        assertTrue(onAcceptModuleStep.isTypeSpecified());

        Step closeOnAcceptModuleStep = new Step();
        closeOnAcceptModuleStep.setTypBeimAnnehmenModulUndAbschliessen(true);
        assertTrue(closeOnAcceptModuleStep.isTypeSpecified());

        Step readImagesStep = new Step();
        readImagesStep.setTypImagesLesen(true);
        assertTrue(readImagesStep.isTypeSpecified());

        Step writeImagesStep = new Step();
        writeImagesStep.setTypImagesSchreiben(true);
        assertTrue(writeImagesStep.isTypeSpecified());

        Step verifyOnCloseStep = new Step();
        verifyOnCloseStep.setTypBeimAbschliessenVerifizieren(true);
        assertTrue(verifyOnCloseStep.isTypeSpecified());

        Step automaticStep = new Step();
        automaticStep.setTypAutomatisch(true);
        assertTrue(automaticStep.isTypeSpecified());

        Step scriptStep = new Step();
        scriptStep.setTypScriptStep(true);
        assertTrue(scriptStep.isTypeSpecified());

        Step moduleNameStep = new Step();
        moduleNameStep.setTypModulName("non-empty-module-name");
        assertTrue(moduleNameStep.isTypeSpecified());

        Step pluginStep = new Step();
        pluginStep.setStepPlugin("non-empty-plugin");
        assertTrue(pluginStep.isTypeSpecified());

        Step automaticThumbnailStep = new Step();
        automaticThumbnailStep.setTypAutomaticThumbnail(true);
        assertTrue(automaticThumbnailStep.isTypeSpecified());

        Step validationStep = new Step();
        validationStep.setValidationPlugin("validation-plugin");
        assertTrue(validationStep.isTypeSpecified());

        Step delayStep = new Step();
        delayStep.setDelayStep(true);
        assertTrue(delayStep.isTypeSpecified());

        Step batchStep = new Step();
        batchStep.setBatchStep(true);
        assertTrue(batchStep.isTypeSpecified());

        Step updateMetadataIndexStep = new Step();
        updateMetadataIndexStep.setUpdateMetadataIndex(true);
        assertTrue(updateMetadataIndexStep.isTypeSpecified());

        Step generateDocketStep = new Step();
        generateDocketStep.setGenerateDocket(true);
        assertTrue(generateDocketStep.isTypeSpecified());

        Step httpStep = new Step();
        httpStep.setHttpStep(true);
        assertTrue(httpStep.isTypeSpecified());

        Step messageQueueStep = new Step();
        messageQueueStep.setMessageQueue(QueueType.COMMAND_QUEUE);
        assertTrue(messageQueueStep.isTypeSpecified());
    }

}
