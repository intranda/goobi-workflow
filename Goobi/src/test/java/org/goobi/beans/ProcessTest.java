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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
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

    @Before
    public void setUp() {

    }

    @Test
    public void testConstructor() {
        Process process = new Process();
        assertNotNull(process);
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
    public void testId() throws Exception {
        Process process = new Process();
        assertNull(process.getId());
        process.setId(1);
        assertEquals(1, process.getId().intValue());
    }

    @Test
    public void testTitle() throws Exception {
        Process process = new Process();
        assertEquals("", process.getTitel());
        process.setTitel("fixture");
        assertEquals("fixture", process.getTitel());
    }

    @Test
    public void testIssueName() throws Exception {
        Process process = new Process();
        assertNull(process.getAusgabename());
        process.setAusgabename("fixture");
        assertEquals("fixture", process.getAusgabename());
    }

    @Test
    public void testTemplate() throws Exception {
        Process process = new Process();
        assertFalse(process.isIstTemplate());
        process.setIstTemplate(true);
        assertTrue(process.isIstTemplate());
        process.setIstTemplate(false);
        assertFalse(process.isIstTemplate());
    }

    @Test
    public void testDisplayInSelection() throws Exception {
        Process process = new Process();
        assertFalse(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(true);
        assertTrue(process.isInAuswahllisteAnzeigen());
        process.setInAuswahllisteAnzeigen(false);
        assertFalse(process.isInAuswahllisteAnzeigen());
    }

    @Test
    public void testProject() throws Exception {
        Process process = new Process();
        Project project = new Project();
        project.setTitel("fixture");
        assertNull(process.getProjekt());
        process.setProjekt(project);
        assertEquals("fixture", process.getProjekt().getTitel());
    }

    @Test
    public void testCreationDate() throws Exception {
        Process process = new Process();
        assertNotNull(process.getErstellungsdatum());
        assertNotNull(process.getErstellungsdatumAsString());
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

}
