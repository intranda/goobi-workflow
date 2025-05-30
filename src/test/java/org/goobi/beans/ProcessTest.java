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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;

public class ProcessTest extends AbstractTest {

    /**
     * This test process is prepared in the method prepareStepsForProgressTests() and tested in the methods testFortschritt(), testFortschritt1(),
     * testFortschritt2(), testFortschrittError() and testFortschritt3().
     */
    private static Process testProcess;

    /**
     * This method prepares the process for the testFortschritt* methods. The test process contains multiple steps with different states, the states
     * can then be analyzed in the unit tests.
     */
    @BeforeClass
    public static void prepareStepsForProgressTests() {

        ProcessTest.testProcess = new Process();

        // Prepare the string representations of the enum status values, eg. "1" == OPEN
        String locked = String.valueOf(StepStatus.LOCKED.getValue());
        String open = String.valueOf(StepStatus.OPEN.getValue());
        String inwork = String.valueOf(StepStatus.INWORK.getValue());
        String done = String.valueOf(StepStatus.DONE.getValue());
        String error = String.valueOf(StepStatus.ERROR.getValue());
        String deactivated = String.valueOf(StepStatus.DEACTIVATED.getValue());

        // The list of steps contains 2x DONE, 1x INWORK, 2x OPEN, 2x LOCKED, 1x ERROR and 1x DEACTIVATED
        List<Step> steps = new ArrayList<>();

        Step step1 = new Step();
        step1.setBearbeitungsstatusAsString(done);
        steps.add(step1);
        Step step2 = new Step();
        step2.setBearbeitungsstatusAsString(done);
        steps.add(step2);

        Step step3 = new Step();
        step3.setBearbeitungsstatusAsString(inwork);
        steps.add(step3);

        Step step4 = new Step();
        step4.setBearbeitungsstatusAsString(open);
        steps.add(step4);
        Step step5 = new Step();
        step5.setBearbeitungsstatusAsString(open);
        steps.add(step5);

        Step step6 = new Step();
        step6.setBearbeitungsstatusAsString(locked);
        steps.add(step6);
        Step step7 = new Step();
        step7.setBearbeitungsstatusAsString(locked);
        steps.add(step7);

        Step step8 = new Step();
        step8.setBearbeitungsstatusAsString(error);
        steps.add(step8);

        // The deactivated step is ignored later. getSchritte() returns 9 steps, but the getFortschritt() methods ignore it
        Step step9 = new Step();
        step9.setBearbeitungsstatusAsString(deactivated);
        steps.add(step9);

        // Assign steps to process: The ratio of each status in relation to all steps is then tested for testProcess
        ProcessTest.testProcess.setSchritte(steps);
    }

    @Test
    public void testId() {
        Process process = new Process();
        assertNull(process.getId());
        process.setId(1);
        assertEquals(1, process.getId().intValue());
    }

    @Test
    public void testTitel() {
        Process process = new Process();
        assertEquals("", process.getTitel());
        process.setTitel("test title");
        assertEquals("test title", process.getTitel());
    }

    @Test
    public void testAusgabename() {
        Process process = new Process();
        assertNull(process.getAusgabename());
        process.setAusgabename("name");
        assertEquals("name", process.getAusgabename());
    }

    @Test
    public void testIstTemplate() {
        Process process = new Process();
        assertFalse(process.isIstTemplate());
        process.setIstTemplate(true);
        assertTrue(process.isIstTemplate());
        process.setIstTemplate(false);
        assertFalse(process.isIstTemplate());
    }

    @Test
    public void testInAuswahllisteAnzeigen() {
        Process process = new Process();
        assertFalse(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(true);
        assertTrue(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(false);
        assertFalse(process.isInAuswahllisteAnzeigen());
    }

    @Test
    public void testProjekt() {
        Process process = new Process();
        assertNull(process.getProjekt());

        Project project = new Project();
        project.setTitel("Project Title");
        process.setProjekt(project);

        assertNotNull(process.getProjekt());
        assertSame(project, process.getProjekt());
        assertEquals("Project Title", process.getProjekt().getTitel());
    }

    @Test
    public void testErstellungsdatum() {
        Date before = new Date();
        Process process = new Process();
        Date after = new Date();

        assertNotNull(process.getErstellungsdatum());
        assertNotNull(process.getErstellungsdatumAsString());

        Date date = process.getErstellungsdatum();
        assertTrue(before.compareTo(date) <= 0);
        assertTrue(date.compareTo(after) <= 0);
    }

    @Test
    public void testSchritte() {
        Process process = new Process();

        List<Step> steps = new ArrayList<>();
        Step step1 = new Step();
        Step step2 = new Step();
        step1.setId(1);
        step2.setId(2);
        steps.add(step1);
        steps.add(step2);
        process.setSchritte(steps);

        assertEquals(2, process.getSchritte().size());
        assertTrue(process.getSchritte().contains(step1));
        assertTrue(process.getSchritte().contains(step2));
    }

    @Test
    public void testEigenschaften() {
        Process process = new Process();

        List<GoobiProperty> properties = new ArrayList<>();
        GoobiProperty property1 = new GoobiProperty(PropertyOwnerType.PROCESS);
        GoobiProperty property2 = new GoobiProperty(PropertyOwnerType.PROCESS);
        properties.add(property1);
        properties.add(property2);
        process.setEigenschaften(properties);

        assertEquals(2, process.getEigenschaften().size());
        assertTrue(process.getEigenschaften().contains(property1));
        assertTrue(process.getEigenschaften().contains(property2));
    }

    @Test
    public void testSortHelperStatus() {
        Process process = new Process();
        assertNull(process.getSortHelperStatus());
        process.setSortHelperStatus("10");
        assertEquals("10", process.getSortHelperStatus());
    }

    @Test
    public void testSortHelperImages() {
        Process process = new Process();
        assertEquals(0, process.getSortHelperImages().intValue());
        process.setSortHelperImages(10);
        assertEquals(10, process.getSortHelperImages().intValue());
    }

    @Test
    public void testSortHelperArticles() {
        Process process = new Process();
        assertEquals(0, process.getSortHelperArticles().intValue());
        process.setSortHelperArticles(10);
        assertEquals(10, process.getSortHelperArticles().intValue());
    }

    @Test
    public void testSortHelperMetadata() {
        Process process = new Process();
        assertEquals(0, process.getSortHelperMetadata().intValue());
        process.setSortHelperMetadata(10);
        assertEquals(10, process.getSortHelperMetadata().intValue());
    }

    @Test
    public void testSortHelperDocstructs() {
        Process process = new Process();
        assertEquals(0, process.getSortHelperDocstructs().intValue());
        process.setSortHelperDocstructs(10);
        assertEquals(10, process.getSortHelperDocstructs().intValue());
    }

    @Test
    public void testRegelsatz() {
        Process process = new Process();
        assertNull(process.getRegelsatz());
        Ruleset ruleset = new Ruleset();
        process.setRegelsatz(ruleset);
        assertSame(ruleset, process.getRegelsatz());
    }

    @Test
    public void testBatch() {
        Process process = new Process();
        assertNull(process.getBatch());
        Batch batch = new Batch();
        process.setBatch(batch);
        assertSame(batch, process.getBatch());
    }

    @Test
    public void testSwappedOut() {
        Process process = new Process();

        assertFalse(process.isSwappedOutHibernate());
        process.setSwappedOutHibernate(true);
        assertTrue(process.isSwappedOutHibernate());
        process.setSwappedOutHibernate(false);
        assertFalse(process.isSwappedOutHibernate());

        assertFalse(process.isSwappedOutGui());
        process.setSwappedOutGui(true);
        assertTrue(process.isSwappedOutGui());
        process.setSwappedOutGui(false);
        assertFalse(process.isSwappedOutGui());
    }

    @Test
    public void testPanelAusgeklappt() {
        Process process = new Process();
        assertFalse(process.isPanelAusgeklappt());
        process.setPanelAusgeklappt(true);
        assertTrue(process.isPanelAusgeklappt());
        process.setPanelAusgeklappt(false);
        assertFalse(process.isPanelAusgeklappt());
    }

    @Test
    public void testSelected() {
        Process process = new Process();
        assertFalse(process.isSelected());
        process.setSelected(true);
        assertTrue(process.isSelected());
        process.setSelected(false);
        assertFalse(process.isSelected());
    }

    @Test
    public void testDocket() {
        Process process = new Process();
        assertNull(process.getDocket());
        Docket docket = new Docket();
        process.setDocket(docket);
        assertSame(docket, process.getDocket());
    }

    @Test
    public void testExportValidator() {
        Process process = new Process();
        assertNull(process.getExportValidator());
        ExportValidator validator = new ExportValidator();
        process.setExportValidator(validator);
        assertSame(validator, process.getExportValidator());
    }

    // Note: 'bhelp' can not be tested since it has no setter and getter

    @Test
    public void testProjectId() {
        Process process = new Process();
        assertNull(process.getProjectId());
        process.setProjectId(10);
        assertEquals(10, process.getProjectId().intValue());
    }

    @Test
    public void testMetadatenKonfigurationID() {
        Process process = new Process();
        assertNull(process.getMetadatenKonfigurationID());
        process.setMetadatenKonfigurationID(10);
        assertEquals(10, process.getMetadatenKonfigurationID().intValue());
    }

    @Test
    public void testDocketId() {
        Process process = new Process();
        assertNull(process.getDocketId());
        process.setDocketId(10);
        assertEquals(10, process.getDocketId().intValue());
    }

    // TODO: Can 'msp' be tested?

    // Note: 'help' can not be tested directly

    // Note: 'tempVariableMap' can not be tested directly

    @Test
    public void testMediaFolderExists() {
        Process process = new Process();
        assertFalse(process.isMediaFolderExists());
        process.setMediaFolderExists(true);
        assertTrue(process.isMediaFolderExists());
        process.setMediaFolderExists(false);
        assertFalse(process.isMediaFolderExists());
    }

    // TODO: 'metadataList' can not be tested without database

    // Note: 'representativeImage' can not be tested directly

    // Note: 'folderList' can not be tested directly

    @Test
    public void testCurrentFolder() {
        Process process = new Process();
        assertNull(process.getCurrentFolder());
        process.setCurrentFolder("folder");
        assertEquals("folder", process.getCurrentFolder());
    }

    @Test
    public void testUploadFolder() {
        Process process = new Process();
        assertEquals("intern", process.getUploadFolder());
        process.setUploadFolder("intern2");
        assertEquals("intern2", process.getUploadFolder());
    }

    // Note: 'showFileDeletionButton' can not be tested directly

    @Test
    public void testPauseAutomaticExecution() {
        Process process = new Process();
        process.setSchritte(new ArrayList<>());
        assertFalse(process.isPauseAutomaticExecution());
        process.setPauseAutomaticExecution(true);
        assertTrue(process.isPauseAutomaticExecution());
        process.setPauseAutomaticExecution(false);
        assertFalse(process.isPauseAutomaticExecution());
    }

    // Note: 'xmlWriteLock' can not be tested because it has no setters and getters

    @Test
    public void testEntryType() {
        Process process = new Process();
        assertSame(JournalEntry.EntryType.PROCESS, process.getEntryType());
    }

    @Test
    public void testConstructor() {
        Process process = new Process();
        assertFalse(process.isSwappedOutGui());
        assertEquals("", process.getTitel());
        assertFalse(process.isIstTemplate());
        assertFalse(process.isInAuswahllisteAnzeigen());
        assertNotNull(process.getEigenschaften());
        assertEquals(0, process.getEigenschaften().size());
        assertNotNull(process.getSchritte());
        assertEquals(0, process.getSchritte().size());
        assertNotNull(process.getErstellungsdatum());
    }

    @Test
    public void testContainsStepOfOrder() {
        Process process = new Process();
        List<Step> steps = new ArrayList<>();
        Step step1 = new Step();
        step1.setReihenfolge(1);
        steps.add(step1);
        Step step2 = new Step();
        step2.setReihenfolge(3);
        steps.add(step2);
        process.setSchritte(steps);
        assertFalse(process.containsStepOfOrder(0));
        assertTrue(process.containsStepOfOrder(1));
        assertFalse(process.containsStepOfOrder(2));
        assertTrue(process.containsStepOfOrder(3));
        assertFalse(process.containsStepOfOrder(4));
    }

    @Test
    public void testGetContainsExportStep() {

        Step dummyStep = new Step();
        dummyStep.setTypExportDMS(false);
        dummyStep.setTypExportRus(false);

        Step dmsStep = new Step();
        dmsStep.setTypExportDMS(true);
        dmsStep.setTypExportRus(false);

        Step rusStep = new Step();
        rusStep.setTypExportDMS(false);
        rusStep.setTypExportRus(true);

        List<Step> stepsWithoutExport = new ArrayList<>();
        stepsWithoutExport.add(dummyStep);
        Process processWithoutExport = new Process();
        processWithoutExport.setSchritte(stepsWithoutExport);
        assertFalse(processWithoutExport.getContainsExportStep());

        List<Step> stepsWithDMSExport = new ArrayList<>();
        stepsWithDMSExport.add(dummyStep);
        stepsWithDMSExport.add(dmsStep);
        Process processWithDMSExport = new Process();
        processWithDMSExport.setSchritte(stepsWithDMSExport);
        assertTrue(processWithDMSExport.getContainsExportStep());

        List<Step> stepsWithRUSExport = new ArrayList<>();
        stepsWithRUSExport.add(dummyStep);
        stepsWithRUSExport.add(rusStep);
        Process processWithRUSExport = new Process();
        processWithRUSExport.setSchritte(stepsWithRUSExport);
        assertTrue(processWithRUSExport.getContainsExportStep());
    }

    // Note: 'getBenutzerGesperrt()' can not be tested without database

    @Test
    public void testGetMinutenGesperrt() {
        Process process = new Process();
        process.setId(10);
        assertEquals(0, process.getMinutenGesperrt());
    }

    @Test
    public void testGetSekundenGesperrt() {
        Process process = new Process();
        process.setId(10);
        assertEquals(0, process.getSekundenGesperrt());
    }

    // Note: 'getImagesTifDirectory()' can not be tested without storage provider and variable replacer

    @Test
    public void testGetTifDirectoryExists() {
        Process process = new Process();
        assertFalse(process.getTifDirectoryExists());
        process.setMediaFolderExists(true);
        assertTrue(process.getTifDirectoryExists());
        process.setMediaFolderExists(false);
        assertFalse(process.getTifDirectoryExists());
    }

    @Test
    public void testGetDisplayMETSButton() {
        Process process = new Process();
        process.setSortHelperImages(1);
        assertTrue(process.getDisplayMETSButton());
    }

    @Test
    public void testGetDisplayPDFButton() {
        Process process = new Process();
        process.setSortHelperImages(1);
        assertTrue(process.getDisplayPDFButton());
    }

    @Test
    public void testGetDisplayDMSButton() {
        Process process = new Process();
        process.setSortHelperImages(1);
        assertTrue(process.getDisplayDMSButton());
    }

    // Note: 'getImagesOrigDirectory()' can not be tested without storage provider and variable replacer

    @Test
    public void testGetMatchingImageDir() {
        Process process = new Process();
        assertEquals("test", process.getMatchingImageDir("test"));
        assertEquals("test_", process.getMatchingImageDir("test_"));
        assertEquals("test_test", process.getMatchingImageDir("test_test"));
        assertEquals("test", process.getMatchingImageDir("test_1"));
        assertEquals("test", process.getMatchingImageDir("test_123"));
        assertEquals("test_test", process.getMatchingImageDir("test_test_123"));
    }

    @Test
    public void testGetSchritteSize() {
        Process process = new Process();
        process.setSchritte(new ArrayList<>());
        assertEquals(0, process.getSchritteSize());
        List<Step> list = new ArrayList<>();
        list.add(new Step());
        process.setSchritte(list);
        assertEquals(1, process.getSchritteSize());
    }

    @Test
    public void testGetSchritteList() {
        Process process = new Process();
        List<Step> list = new ArrayList<>();
        process.setSchritte(list);
        assertSame(list, process.getSchritteList());
    }

    @Test
    public void testGetEigenschaftenSize() {
        Process process = new Process();
        process.setEigenschaften(new ArrayList<>());
        assertEquals(0, process.getEigenschaftenSize());
        List<GoobiProperty> list = new ArrayList<>();
        list.add(new GoobiProperty(PropertyOwnerType.PROCESS));
        process.setEigenschaften(list);
        assertEquals(1, process.getEigenschaftenSize());
    }

    @Test
    public void testGetEigenschaftenList() {
        Process process = new Process();
        List<GoobiProperty> list = new ArrayList<>();
        process.setEigenschaften(list);
        assertSame(list, process.getEigenschaftenList());
    }

    @Test
    public void testGetAktuellerSchritt() {
        Step done = new Step();
        done.setBearbeitungsstatusEnum(StepStatus.DONE);
        Step inwork = new Step();
        inwork.setBearbeitungsstatusEnum(StepStatus.INWORK);
        Step inflight = new Step();
        inflight.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);
        Step open = new Step();
        open.setBearbeitungsstatusEnum(StepStatus.OPEN);
        Step error = new Step();
        error.setBearbeitungsstatusEnum(StepStatus.ERROR);
        Step locked = new Step();
        locked.setBearbeitungsstatusEnum(StepStatus.LOCKED);

        Process process1 = new Process();
        List<Step> steps1 = new ArrayList<>();
        steps1.add(done);
        steps1.add(error);
        steps1.add(locked);
        process1.setSchritte(steps1);
        assertNull(process1.getAktuellerSchritt());

        Process process2 = new Process();
        List<Step> steps2 = new ArrayList<>();
        steps2.add(done);
        steps2.add(inwork);
        steps2.add(locked);
        process2.setSchritte(steps2);
        assertNotNull(process2.getAktuellerSchritt());
        assertSame(inwork, process2.getAktuellerSchritt());

        Process process3 = new Process();
        List<Step> steps3 = new ArrayList<>();
        steps3.add(done);
        steps3.add(inflight);
        steps3.add(locked);
        process3.setSchritte(steps3);
        assertNotNull(process3.getAktuellerSchritt());
        assertSame(inflight, process3.getAktuellerSchritt());

        Process process4 = new Process();
        List<Step> steps4 = new ArrayList<>();
        steps4.add(done);
        steps4.add(open);
        steps4.add(locked);
        process4.setSchritte(steps4);
        assertNotNull(process4.getAktuellerSchritt());
        assertSame(open, process4.getAktuellerSchritt());
    }

    @Test
    public void testGetErstellungsdatumAsString() {
        Process process = new Process();

        // Create non-deprecated date object:
        GregorianCalendar calendar = new GregorianCalendar();
        // month 5 is 0-based: 0 = January, ..., 5 = June
        calendar.set(2023, 5, 15, 10, 0, 0);
        long timestamp = calendar.getTimeInMillis();
        Date testDate = new Date(timestamp);

        process.setErstellungsdatum(testDate);
        assertTrue(process.getErstellungsdatumAsString().contains("10:00:00"));
    }

    @Test
    public void testGetFortschritt() {
        String result = ProcessTest.testProcess.getFortschritt();
        // Explanation for this number:
        // There are 9 steps (3 DONE + DEACTIVATED, 4 OPEN + INWORK + INFLIGHT + ERROR, 2 LOCKED)
        // The ratios 3/9, 4/9, and 2/9 are used (33.333%, 44.444%, and 22.222%)
        // Decimal Format converts the number so integers with 3 digits each
        // -> 33, 44, and 22
        // The result is then "033" + "044" + "022"
        String expected = "033044022";
        assertEquals("Result of getFortschritt is wrong.", expected, result);
    }

    @Test
    public void testGetFortschritt1() {
        // 2/8 (= 25.0%) of the non-ignored steps have status LOCKED
        double result = ProcessTest.testProcess.getFortschritt1();
        double expected = 25.0;
        double delta = result - expected;
        assertEquals("Result of getFortschritt1 (StepStatus.LOCKED) is wrong.", result, expected, delta);
    }

    @Test
    public void testGetFortschritt2() {
        // 3/8 (= 37.5%) of the non-ignored steps have status OPEN, INWORK and INFLIGHT
        double result = ProcessTest.testProcess.getFortschritt2();
        double expected = 37.5;
        double delta = result - expected;
        assertEquals("Result of getFortschritt2 (StepStatus.OPEN, INWORK and INFLIGHT) is wrong.", result, expected, delta);
    }

    @Test
    public void testGetFortschrittError() {
        // 1/8 (= 12.5%) of the non-ignored steps has status ERROR
        double result = ProcessTest.testProcess.getFortschrittError();
        double expected = 12.5;
        double delta = result - expected;
        assertEquals("Result of getFortschrittError (StepStatus.ERROR) is wrong.", result, expected, delta);
    }

    @Test
    public void testGetFortschritt3() {
        // 2/8 (= 25.0%) of the non-ignored steps have status DONE
        double result = ProcessTest.testProcess.getFortschritt3();
        double expected = 25.0;
        double delta = result - expected;
        assertEquals("Result of getFortschritt3 (StepStatus.DONE) is wrong.", result, expected, delta);
    }

    @Test
    public void testReadMetadata() throws Exception {
        Process process = MockProcess.createProcess();
        Fileformat ff = process.readMetadataFile();
        assertNotNull(ff);
        DocStruct logical = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals("Monograph", logical.getType().getName());
    }

    @Test
    public void testGetContainsUnreachableSteps() {
        Process process = new Process();
        process.setSchritte(new ArrayList<>());

        assertTrue(process.getContainsUnreachableSteps());
        List<Step> steps = new ArrayList<>();
        steps.add(new Step());
        process.setSchritte(steps);
        assertTrue(process.getContainsUnreachableSteps());

        Step userStep = new Step();
        User user = new User();
        List<User> users = new ArrayList<>();
        users.add(user);
        userStep.setBenutzer(users);
        steps.add(userStep);
        assertTrue(process.getContainsUnreachableSteps());

        steps = new ArrayList<>();
        Step userGroupStep = new Step();
        Usergroup userGroup = new Usergroup();
        List<Usergroup> userGroups = new ArrayList<>();
        userGroups.add(userGroup);
        userGroupStep.setBenutzergruppen(userGroups);
        steps.add(userGroupStep);
        assertTrue(process.getContainsUnreachableSteps());
    }

    @Test
    public void testGetImageFolderInUse() {
        Process process = new Process();
        assertFalse(process.isImageFolderInUse());
        List<Step> steps = new ArrayList<>();
        Step step = new Step();
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step.setTypImagesSchreiben(true);
        steps.add(step);
        process.setSchritte(steps);
        assertTrue(process.isImageFolderInUse());
    }

    @Test
    public void testgetImageFolderInUseUser() {
        Process process = new Process();
        assertNull(process.getImageFolderInUseUser());
        List<Step> steps = new ArrayList<>();
        Step step = new Step();
        step.setBearbeitungsstatusEnum(StepStatus.INWORK);
        step.setTypImagesSchreiben(true);
        User user = new User();
        step.setBearbeitungsbenutzer(user);
        steps.add(step);
        process.setSchritte(steps);
        assertSame(user, process.getImageFolderInUseUser());
    }

    @Test
    public void testGetFirstOpenStep() {
        Process process = new Process();

        Step done = new Step();
        done.setBearbeitungsstatusEnum(StepStatus.DONE);
        Step open = new Step();
        open.setBearbeitungsstatusEnum(StepStatus.OPEN);
        Step inwork = new Step();
        inwork.setBearbeitungsstatusEnum(StepStatus.INWORK);
        Step inflight = new Step();
        inflight.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);

        List<Step> steps1 = new ArrayList<>();
        steps1.add(done);
        process.setSchritte(steps1);
        assertNull(process.getFirstOpenStep());

        List<Step> steps2 = new ArrayList<>();
        steps2.add(done);
        steps2.add(open);
        process.setSchritte(steps2);
        assertSame(open, process.getFirstOpenStep());

        List<Step> steps3 = new ArrayList<>();
        steps3.add(done);
        steps3.add(inwork);
        process.setSchritte(steps3);
        assertSame(inwork, process.getFirstOpenStep());

        List<Step> steps4 = new ArrayList<>();
        steps4.add(done);
        steps4.add(inflight);
        process.setSchritte(steps4);
        assertSame(inflight, process.getFirstOpenStep());
    }

    @Test
    public void testCompareTo() {
        Process process1 = new Process();
        process1.setId(1);
        Process process2 = new Process();
        process2.setId(1);
        Process process3 = new Process();
        process3.setId(2);
        assertEquals(0, process1.compareTo(process1));
        assertEquals(0, process1.compareTo(process2));
        assertTrue(process1.compareTo(process3) < 0);
        assertTrue(process3.compareTo(process1) > 0);
    }

    @Test
    public void testEquals() {
        Process process1 = new Process();
        process1.setId(1);
        Process process2 = new Process();
        process2.setId(1);
        Process process3 = new Process();
        process3.setId(2);
        assertNotEquals(process1, null);
        assertNotEquals(process1, process3);
        assertNotEquals(process1, new User());
        assertEquals(process1, process1);
        assertEquals(process1, process2);
    }

    @Test
    public void testHashCode() {
        Process process1 = new Process();
        Process process2 = new Process();
        assertEquals(process1.hashCode(), process1.hashCode());
        assertNotEquals(process1.hashCode(), process2.hashCode());
    }

    @Ignore("This test can not be executed without a database")
    @Test
    public void testCloneConstructor() {
        Process process = new Process();
        process.setDocket(new Docket());
        process.setExportValidator(new ExportValidator());
        process.setProjectId(10);
        process.setProjekt(new Project());
        process.setRegelsatz(new Ruleset());
        process.setSortHelperStatus("10");
        process.setTitel("Process 1");

        Process clone = new Process(process);
        assertFalse(clone.isSwappedOutGui());
        assertFalse(clone.isIstTemplate());
        assertFalse(clone.isInAuswahllisteAnzeigen());
        assertNotNull(clone.getEigenschaften());
        assertEquals(0, clone.getEigenschaftenSize());
        assertNotNull(clone.getSchritte());
        assertEquals(0, clone.getSchritteSize());
        assertNotNull(clone.getErstellungsdatum());
        assertTrue(clone.getErstellungsdatum().after(process.getErstellungsdatum()));

        assertSame(process.getDocket(), clone.getDocket());
        assertSame(process.getExportValidator(), clone.getExportValidator());
        assertEquals(process.getProjectId().intValue(), clone.getProjectId().intValue());
        assertSame(process.getProjekt(), clone.getProjekt());
        assertSame(process.getRegelsatz(), clone.getRegelsatz());
        assertEquals(process.getSortHelperStatus(), clone.getSortHelperStatus());
        assertEquals(process.getTitel() + "copy", clone.getTitel());
    }

    @Test
    public void testGetTitelLokalisiert() {
        Process process = new Process();
        process.setTitel("Titel");
        assertEquals("Titel", process.getTitelLokalisiert());
    }

    @Ignore("This test can not be executed when the translator is not loaded.")
    @Test
    public void testGetStepsAsString() {
        Process process = new Process();
        process.setSchritte(null);
        assertEquals("", process.getStepsAsString(0, ""));
        List<Step> steps = new ArrayList<>();
        process.setSchritte(steps);
        assertEquals("", process.getStepsAsString(0, ""));
        Step step1 = new Step();
        step1.setTitel("Titel 1");
        step1.setBearbeitungsstatusEnum(StepStatus.DONE);
        Step step2 = new Step();
        step2.setTitel("Titel 2");
        step2.setBearbeitungsstatusEnum(StepStatus.DONE);
        steps.add(step1);
        steps.add(step2);
        int status = StepStatus.DONE.getValue();
        assertEquals("Titel 1, Titel 2", process.getStepsAsString(status, ", "));
    }

    @Test
    public void testIsConfiguredWithExportValidator() {
        Process process = new Process();
        assertFalse(process.isConfiguredWithExportValidator());
        ExportValidator validator = new ExportValidator();
        validator.setLabel("Label");
        validator.setCommand("Command");
        process.setExportValidator(validator);
        assertTrue(process.isConfiguredWithExportValidator());
    }

    @Deprecated(since = "23.05", forRemoval = true)
    @Test
    public void testProcessLog() {
        Process process = new Process();
        List<JournalEntry> log = new ArrayList<>();
        process.setProcessLog(log);
        assertSame(log, process.getProcessLog());
    }

    @Test
    public void testAddJournalEntryForAll() {
        Process process = new Process();
        try {
            process.addJournalEntryForAll();
            fail("UnsupportedOperationException was expected");
        } catch (UnsupportedOperationException exception) {
            // This exception is expected behavior
        }
    }

}
