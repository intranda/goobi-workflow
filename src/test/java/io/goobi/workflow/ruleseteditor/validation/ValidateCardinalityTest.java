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

import java.io.StringReader;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateCardinalityTest {

    @Test
    public void testValidCardinalities() throws Exception {
        String xml = "<DocStrctType>"
                + "<Name>Monograph</Name>"
                + "<metadata num='1o'>Title</metadata>"
                + "<group num='*'>Authors</group>"
                + "</DocStrctType>";

        Element root = loadRoot(xml);
        ValidateCardinality validator = new ValidateCardinality();
        List<RulesetValidationError> errors = validator.validate(root);

        assertTrue("No errors expected for valid cardinalities", errors.isEmpty());
    }

    @Test
    public void testInvalidCardinality() throws Exception {
        String xml = "<DocStrctType>"
                + "<Name>Monograph</Name>"
                + "<metadata num='xyz'>Title</metadata>"
                + "<group num='1x'>Authors</group>"
                + "</DocStrctType>";

        Element root = loadRoot(xml);
        ValidateCardinality validator = new ValidateCardinality();
        List<RulesetValidationError> errors = validator.validate(root);

        assertEquals("Two invalid cardinalities expected", 2, errors.size());

        for (RulesetValidationError error : errors) {
            assertTrue(error.getMessage().contains("ruleset_validation_wrong_cardinality"));
        }
    }

    @Test
    public void testMissingNumAttribute() throws Exception {
        String xml = "<DocStrctType>"
                + "<Name>Monograph</Name>"
                + "<metadata>Title</metadata>" // No num attribute
                + "</DocStrctType>";

        Element root = loadRoot(xml);
        ValidateCardinality validator = new ValidateCardinality();
        List<RulesetValidationError> errors = validator.validate(root);

        assertTrue("No error expected if num attribute is missing", errors.isEmpty());
    }

    private Element loadRoot(String xml) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader("<root>" + xml + "</root>"));
        return doc.getRootElement();
    }
}
