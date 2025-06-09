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

package io.goobi.workflow.ruleseteditor;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.jdom2.Element;
import org.junit.Test;

public class RulesetValidationErrorTest {

    @Test
    public void testConstructorWithParsedLine() {
        Element mockElement = EasyMock.mock(Element.class);
        RulesetValidationError error = new RulesetValidationError(
                "ERROR",
                "Test message",
                "42",
                12,
                mockElement);

        assertEquals("ERROR", error.getSeverity());
        assertEquals("Test message", error.getMessage());
        assertEquals(42, error.getLine());
        assertEquals(0, error.getColumn());
        assertEquals(RulesetValidationError.ErrorType.USED_BUT_UNDEFINED, error.getErrorType());
        assertEquals(mockElement, error.getElement());
    }

    @Test
    public void testConstructorWithInvalidLine() {
        Element dummyElement = EasyMock.mock(Element.class);
        RulesetValidationError error = new RulesetValidationError(
                "WARN",
                "Invalid line test",
                "notANumber",
                4,
                dummyElement);

        assertEquals(0, error.getLine());
        assertEquals(RulesetValidationError.ErrorType.DUPLICATES_IN_DOCSTRCT, error.getErrorType());
    }

    @Test
    public void testErrorTypeFromValidCode() {
        assertEquals(RulesetValidationError.ErrorType.INVALID_CARDINALITY, RulesetValidationError.ErrorType.fromCode(1));
        assertEquals(RulesetValidationError.ErrorType.USED_BUT_UNDEFINED, RulesetValidationError.ErrorType.fromCode(12));
    }

    @Test
    public void testErrorTypeFromInvalidCode() {
        assertEquals(RulesetValidationError.ErrorType.UNKNOWN, RulesetValidationError.ErrorType.fromCode(-1));
        assertEquals(RulesetValidationError.ErrorType.UNKNOWN, RulesetValidationError.ErrorType.fromCode(999));
    }

    @Test
    public void testAllArgsConstructor() {
        Element mockElement = EasyMock.mock(Element.class);
        RulesetValidationError error = new RulesetValidationError(
                5, 10, "INFO", "AllArgs message", mockElement,
                RulesetValidationError.ErrorType.EMPTY_NAME);

        assertEquals(5, error.getLine());
        assertEquals(10, error.getColumn());
        assertEquals("INFO", error.getSeverity());
        assertEquals("AllArgs message", error.getMessage());
        assertEquals(RulesetValidationError.ErrorType.EMPTY_NAME, error.getErrorType());
    }
}
