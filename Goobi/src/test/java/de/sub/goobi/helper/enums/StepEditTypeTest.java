package de.sub.goobi.helper.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;

public class StepEditTypeTest extends AbstractTest {

    @Test
    public void testStepEditTypeGetValue() {
        assertTrue(StepEditType.UNNOWKN.getValue() == 0);
    }

    // TODO fails in eclipse, but not in jenkins/mvn
    // Can't find bundle for base name messages, locale en
    @Test
    public void testStepEditTypeGetTitle() {
        assertEquals(Helper.getTranslation("unbekannt"), StepEditType.UNNOWKN.getTitle());
    }

    @Test
    public void testStepEditTypeGetTypeFromValue() {
        assertEquals(StepEditType.MANUAL_SINGLE, StepEditType.getTypeFromValue(1));
        assertEquals(StepEditType.UNNOWKN, StepEditType.getTypeFromValue(666));
    }
}
