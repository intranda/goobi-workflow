package de.sub.goobi.helper.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;

public class StepStatusTest extends AbstractTest {

    @Test
    public void testStepStatusGetValue() {
        assertTrue(StepStatus.LOCKED.getValue() == 0);
    }

    // TODO fails in eclipse, but not in jenkins/mvn
    // Can't find bundle for base name messages, locale en
    @Test
    public void testStepStatusGetTitle() {
        assertEquals(Helper.getTranslation("statusGesperrt"), StepStatus.LOCKED.getTitle());
        assertEquals(Helper.getTranslation("statusOffen"), StepStatus.OPEN.getTitle());
        assertEquals(Helper.getTranslation("statusInBearbeitung"), StepStatus.INWORK.getTitle());
        assertEquals(Helper.getTranslation("statusAbgeschlossen"), StepStatus.DONE.getTitle());
    }

    @Test
    public void testStepStatusGetSmallImagePath() {
        assertEquals("images/status/red_10.gif", StepStatus.LOCKED.getSmallImagePath());
        assertEquals("images/status/orange_10.gif", StepStatus.OPEN.getSmallImagePath());
        assertEquals("images/status/yellow_10.gif", StepStatus.INWORK.getSmallImagePath());
        assertEquals("images/status/green_10.gif", StepStatus.DONE.getSmallImagePath());
    }

    @Test
    public void testStepStatusGetBigImagePath() {
        assertEquals("images/status/red_15a.gif", StepStatus.LOCKED.getBigImagePath());
        assertEquals("images/status/orange_15a.gif", StepStatus.OPEN.getBigImagePath());
        assertEquals("images/status/yellow_15a.gif", StepStatus.INWORK.getBigImagePath());
        assertEquals("images/status/green_15a.gif", StepStatus.DONE.getBigImagePath());
    }

    @Test
    public void testStepStatusGetStatusFromValue() {
        assertEquals(StepStatus.LOCKED, StepStatus.getStatusFromValue(0));
        assertEquals(StepStatus.OPEN, StepStatus.getStatusFromValue(1));
        assertEquals(StepStatus.INWORK, StepStatus.getStatusFromValue(2));
        assertEquals(StepStatus.DONE, StepStatus.getStatusFromValue(3));
        assertEquals(StepStatus.LOCKED, StepStatus.getStatusFromValue(666));
    }

    @Test
    public void testStepStatusGetSearchString() {
        assertEquals("steplocked", StepStatus.LOCKED.getSearchString());
        assertEquals("stepopen", StepStatus.OPEN.getSearchString());
        assertEquals("stepinwork", StepStatus.INWORK.getSearchString());
        assertEquals("stepdone", StepStatus.DONE.getSearchString());

    }
}
