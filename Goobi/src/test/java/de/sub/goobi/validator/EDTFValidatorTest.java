package de.sub.goobi.validator;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EDTFValidatorTest {
    
    private EDTFValidator validator = new EDTFValidator();
    
    @Test
    public void failToParseRandomNonCompliantStringTest() {

        assertFalse(validator.isValid("This String is quite random."));
    }
    
    @Test
    public void correctlyParseEDTFCompliantString() {
        assertTrue(validator.isValid("1984")); 
    }
    
}
