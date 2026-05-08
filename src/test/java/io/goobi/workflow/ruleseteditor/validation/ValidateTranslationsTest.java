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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.jdom2.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

@ExtendWith(MockitoExtension.class)
public class ValidateTranslationsTest {

    private ValidateTranslations validator;

    @BeforeEach
    public void setUp() {
        validator = new ValidateTranslations();
    }

    @Test
    public void testNoErrorsWhenLanguageHasText() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

            Element root = new Element("root");
            Element elem = new Element("element");
            Element lang = new Element("language");
            lang.setText("English");
            elem.addContent(lang);
            Element name = new Element("Name");
            name.setText("TestName");
            elem.addContent(name);
            root.addContent(elem);

            List<RulesetValidationError> errors = validator.validate(root);
            assertTrue(errors.isEmpty());

        }
    }

    @Test
    public void testErrorWhenLanguageEmpty() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

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

            mockedHelper.when(() -> Helper.getTranslation("ruleset_validation_emptyTranslation", "TestName")).thenReturn("Translated error message");

            List<RulesetValidationError> errors = validator.validate(root);
            assertEquals(1, errors.size());
            RulesetValidationError error = errors.get(0);
            assertEquals("ERROR", error.getSeverity());
            assertEquals("Translated error message", error.getMessage());
            assertEquals(42, error.getLine());

        }
    }

    @Test
    public void testErrorWithNoLineNumber() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {

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

            mockedHelper.when(() -> Helper.getTranslation("ruleset_validation_emptyTranslation", "AnotherName"))
                    .thenReturn("Translated error message");

            List<RulesetValidationError> errors = validator.validate(root);
            assertEquals(1, errors.size());
            RulesetValidationError error = errors.get(0);
            assertEquals(0, error.getLine()); // fallback to "0"

        }
    }
}
