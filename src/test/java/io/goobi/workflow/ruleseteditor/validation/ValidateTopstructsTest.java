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

public class ValidateTopstructsTest {

    private ValidateTopstructs validator;

    @Before
    public void setUp() {
        validator = new ValidateTopstructs();
    }

    @Test
    public void testNoAllowedChildTypeErrors() {
        Element root = new Element("Root");

        // DocStrctType with topStruct and Name
        Element docStrct = new Element("DocStrctType");
        docStrct.setAttribute("topStruct", "true");
        Element name = new Element("Name");
        name.setText("TopStruct1");
        name.setAttribute("goobi_lineNumber", "10");
        docStrct.addContent(name);
        root.addContent(docStrct);

        // allowedchildtype that is NOT a topstruct -> no error
        Element docStrct2 = new Element("DocStrctType");
        Element allowedChildType = new Element("allowedchildtype");
        allowedChildType.setText("SomeOtherType");
        allowedChildType.setAttribute("goobi_lineNumber", "20");
        docStrct2.addContent(allowedChildType);
        root.addContent(docStrct2);

        List<RulesetValidationError> errors = validator.validate(root);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testAllowedChildTypeMatchingTopstruct_ShouldReturnError() {
        Element root = new Element("Root");

        // DocStrctType with topStruct and Name = TopStruct1
        Element topStructElement = new Element("DocStrctType");
        topStructElement.setAttribute("topStruct", "true");
        Element name = new Element("Name");
        name.setText("TopStruct1");
        name.setAttribute("goobi_lineNumber", "10");
        topStructElement.addContent(name);
        root.addContent(topStructElement);

        // DocStrctType with allowedchildtype = TopStruct1 -> should error
        Element otherDocStrct = new Element("DocStrctType");
        Element allowedChildType = new Element("allowedchildtype");
        allowedChildType.setText("TopStruct1");
        allowedChildType.setAttribute("goobi_lineNumber", "20");
        otherDocStrct.addContent(allowedChildType);
        root.addContent(otherDocStrct);

        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertEquals("ERROR", error.getSeverity());
        assertTrue(error.getMessage().contains("ruleset_validation_unallowedUsageOfTopstruct"));
        assertEquals(20, error.getLine());
    }

    @Test
    public void testLineNumberFallback() {
        Element root = new Element("Root");

        // DocStrctType with topStruct and Name without line number
        Element topStructElement = new Element("DocStrctType");
        topStructElement.setAttribute("topStruct", "true");
        Element name = new Element("Name");
        name.setText("TopStruct1");
        // no line number set here
        topStructElement.addContent(name);
        root.addContent(topStructElement);

        // DocStrctType with allowedchildtype = TopStruct1 without line number
        Element otherDocStrct = new Element("DocStrctType");
        Element allowedChildType = new Element("allowedchildtype");
        allowedChildType.setText("TopStruct1");
        // no line number set here
        otherDocStrct.addContent(allowedChildType);
        root.addContent(otherDocStrct);

        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals(1, errors.size());
        assertEquals(0, errors.get(0).getLine());
    }
}
