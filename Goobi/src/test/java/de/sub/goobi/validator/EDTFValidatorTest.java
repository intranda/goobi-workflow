package de.sub.goobi.validator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import de.sub.goobi.validator.EDTFValidator;

public class EDTFValidatorTest {
    
    @Test
    public void failToParseRandomNonCompliantStringTest() {
        assertFalse(EDTFValidator.isValid("This String is quite random."));
    }
    
    @Test
    public void correctlyParseEDTFCompliantString() {
        assertTrue(EDTFValidator.isValid("1984")); 
    }
    
}
