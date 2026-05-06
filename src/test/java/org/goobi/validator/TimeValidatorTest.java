package org.goobi.validator;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import jakarta.faces.validator.ValidatorException;

public class TimeValidatorTest extends AbstractTest {

    private TimeValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new TimeValidator();
    }

    @Test
    public void testConstructor() {
        assertNotNull(validator);
    }

    @Test
    public void testValidateZeroIsValid() {
        // 0 is a special value meaning "no auto-save" and must always be accepted
        validator.validate(null, null, 0);
    }

    @Test
    public void testValidateMinBoundaryIsValid() {
        // 10 is the lower valid boundary
        validator.validate(null, null, 10);
    }

    @Test
    public void testValidateMaxBoundaryIsValid() {
        // 30 is the upper valid boundary
        validator.validate(null, null, 30);
    }

    @Test
    public void testValidateMiddleValueIsValid() {
        validator.validate(null, null, 20);
    }

    @Test
    public void testValidateBelowMinThrowsException() {
        assertThrows(ValidatorException.class, () -> {
            // 9 is just below the minimum allowed value
            validator.validate(null, null, 9);
        });
    }

    @Test
    public void testValidateOneThrowsException() {
        assertThrows(ValidatorException.class, () -> {
            validator.validate(null, null, 1);
        });
    }

    @Test
    public void testValidateNegativeThrowsException() {
        assertThrows(ValidatorException.class, () -> {
            validator.validate(null, null, -1);
        });
    }

    @Test
    public void testValidateAboveMaxThrowsException() {
        assertThrows(ValidatorException.class, () -> {
            // 31 is just above the maximum allowed value
            validator.validate(null, null, 31);
        });
    }

    @Test
    public void testValidateExceptionContainsMessage() {
        try {
            validator.validate(null, null, 5);
        } catch (ValidatorException e) {
            assertNotNull(e.getFacesMessage());
            assertEquals(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, e.getFacesMessage().getSeverity());
        }
    }
}
