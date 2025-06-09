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

package io.goobi.workflow.ruleseteditor.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateNamesTest {

    private ValidateNames validator;

    @Before
    public void setUp() {
        validator = new ValidateNames();
    }

    @Test
    public void testMissingNameElement() {
        Element root = new Element("Root");
        Element metadataType = new Element("MetadataType");
        metadataType.setAttribute("goobi_lineNumber", "12");
        root.addContent(metadataType);

        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertTrue(error.getMessage().contains("missing_name"));
        assertEquals(12, error.getLine());
        assertEquals("ERROR", error.getSeverity());
    }

    @Test
    public void testEmptyNameElement() {
        Element root = new Element("Root");
        Element docStruct = new Element("DocStructType");
        docStruct.setAttribute("goobi_lineNumber", "42");

        Element name = new Element("Name");
        name.setText("   "); // only whitespace
        name.setAttribute("goobi_lineNumber", "43");

        docStruct.addContent(name);
        root.addContent(docStruct);

        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertTrue(error.getMessage().contains("empty_name"));
        assertEquals(43, error.getLine());
    }

    @Test
    public void testValidNameElement() {
        Element root = new Element("Root");
        Element docStruct = new Element("DocStructType");

        Element name = new Element("Name");
        name.setText("Monograph");

        docStruct.addContent(name);
        root.addContent(docStruct);

        List<RulesetValidationError> errors = validator.validate(root);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testFormatsElementIsIgnored() {
        Element root = new Element("Root");
        Element formats = new Element("Formats");

        Element name = new Element("Name");
        name.setText(""); // Should be empty but ignored
        formats.addContent(name);
        root.addContent(formats);

        List<RulesetValidationError> errors = validator.validate(root);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testMissingLineNumberFallsBackToZero() {
        Element root = new Element("Root");
        Element docStruct = new Element("DocStructType");
        Element name = new Element("Name"); // no goobi_lineNumber
        name.setText(""); // empty

        docStruct.addContent(name);
        root.addContent(docStruct);

        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertEquals(0, errors.get(0).getLine());
    }

}
