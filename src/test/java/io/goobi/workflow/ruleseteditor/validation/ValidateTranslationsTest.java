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

import org.easymock.EasyMock;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class })
public class ValidateTranslationsTest {

    private ValidateTranslations validator;

    @Before
    public void setUp() {
        validator = new ValidateTranslations();
        PowerMock.mockStatic(Helper.class);
    }

    @Test
    public void testNoErrorsWhenLanguageHasText() {
        Element root = new Element("root");
        Element elem = new Element("element");
        Element lang = new Element("language");
        lang.setText("English");
        elem.addContent(lang);
        Element name = new Element("Name");
        name.setText("TestName");
        elem.addContent(name);
        root.addContent(elem);

        PowerMock.replay(Helper.class);

        List<RulesetValidationError> errors = validator.validate(root);
        assertTrue(errors.isEmpty());

        PowerMock.verify(Helper.class);
    }

    @Test
    public void testErrorWhenLanguageEmpty() {
        Element root = new Element("root");
        Element elem = new Element("element");
        Element lang = new Element("language");
        lang.setText("   "); // empty text
        lang.setAttribute("goobi_lineNumber", "42");
        elem.addContent(lang);
        Element name = new Element("Name");
        name.setText("TestName");
        elem.addContent(name);
        root.addContent(elem);

        EasyMock.expect(Helper.getTranslation("ruleset_validation_emptyTranslation", "TestName"))
                .andReturn("Translated error message");

        PowerMock.replay(Helper.class);

        List<RulesetValidationError> errors = validator.validate(root);
        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertEquals("ERROR", error.getSeverity());
        assertEquals("Translated error message", error.getMessage());
        assertEquals(42, error.getLine());

        PowerMock.verify(Helper.class);
    }

    @Test
    public void testErrorWithNoLineNumber() {
        Element root = new Element("root");
        Element elem = new Element("element");
        Element lang = new Element("language");
        lang.setText(""); // empty
        // no line number set
        elem.addContent(lang);
        Element name = new Element("Name");
        name.setText("AnotherName");
        elem.addContent(name);
        root.addContent(elem);

        EasyMock.expect(Helper.getTranslation("ruleset_validation_emptyTranslation", "AnotherName"))
                .andReturn("Translated error message");

        PowerMock.replay(Helper.class);

        List<RulesetValidationError> errors = validator.validate(root);
        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertEquals(0, error.getLine()); // fallback to "0"

        PowerMock.verify(Helper.class);
    }
}
