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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

public class ReportErrorsErrorHandlerTest {

    private ReportErrorsErrorHandler handler;

    @Before
    public void setUp() {
        handler = new ReportErrorsErrorHandler();
    }

    @Test
    public void testErrorHandling_error() throws Exception {
        SAXParseException exception = new SAXParseException("Invalid element", null, null, 12, 34);
        handler.error(exception);

        List<RulesetValidationError> errors = handler.getErrors();
        assertEquals(1, errors.size());

        RulesetValidationError error = errors.get(0);
        assertEquals("ERROR", error.getSeverity());
        assertEquals("Invalid element", error.getMessage());
        assertEquals(12, error.getLine());
        assertEquals(34, error.getColumn());
        assertNull(error.getElement());
        assertNull(error.getErrorType());
    }

    @Test
    public void testErrorHandling_fatalError() throws Exception {
        SAXParseException exception = new SAXParseException("Broken root tag", null, null, 7, 15);
        handler.fatalError(exception);

        List<RulesetValidationError> errors = handler.getErrors();
        assertEquals(1, errors.size());

        RulesetValidationError error = errors.get(0);
        assertEquals("FATAL", error.getSeverity());
        assertEquals("Broken root tag", error.getMessage());
        assertEquals(7, error.getLine());
        assertEquals(15, error.getColumn());
    }

    @Test
    public void testErrorHandling_warning() throws Exception {
        SAXParseException exception = new SAXParseException("Deprecated attribute", null, null, 3, 8);
        handler.warning(exception);

        List<RulesetValidationError> errors = handler.getErrors();
        assertEquals(1, errors.size());

        RulesetValidationError error = errors.get(0);
        assertEquals("WARNING", error.getSeverity());
        assertEquals("Deprecated attribute", error.getMessage());
        assertEquals(3, error.getLine());
        assertEquals(8, error.getColumn());
    }

    @Test
    public void testMultipleErrors() throws Exception {
        handler.error(new SAXParseException("First error", null, null, 1, 1));
        handler.warning(new SAXParseException("A warning", null, null, 2, 2));
        handler.fatalError(new SAXParseException("Fatal issue", null, null, 3, 3));

        List<RulesetValidationError> errors = handler.getErrors();
        assertEquals(3, errors.size());

        assertEquals("ERROR", errors.get(0).getSeverity());
        assertEquals("WARNING", errors.get(1).getSeverity());
        assertEquals("FATAL", errors.get(2).getSeverity());
    }
}
