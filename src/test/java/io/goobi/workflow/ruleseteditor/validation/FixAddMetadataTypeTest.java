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
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;
import io.goobi.workflow.ruleseteditor.RulesetValidationError.ErrorType;

public class FixAddMetadataTypeTest {

    @Test
    public void testFixAddsNewMetadataType() {
        // Arrange
        Element root = new Element("Root");

        // Existing MetadataType
        Element metadata1 = new Element("MetadataType");
        metadata1.addContent(new Element("Name").setText("existing"));
        root.addContent(metadata1);

        // Simulate error: message = "Biografisches - fehlt", refers to <Person> in <PicaPlus>
        Element person = new Element("Person");
        Element picaPlus = new Element("PicaPlus");
        picaPlus.addContent(person); // <PicaPlus><Person/></PicaPlus>

        RulesetValidationError error = new RulesetValidationError(
                1, 1, "WARNING", "Biografisches - fehlt", person, ErrorType.VALIDATE_FORMATS);

        FixAddMetadataType fixer = new FixAddMetadataType();

        // Act
        fixer.fix(root, error);

        // Assert
        List<Element> children = root.getChildren();
        assertEquals(2, children.size());

        Element newMetadata = children.get(1);
        assertEquals("MetadataType", newMetadata.getName());

        assertEquals("person", newMetadata.getAttributeValue("type"));

        List<Element> contents = newMetadata.getChildren();
        assertEquals(3, contents.size());

        assertEquals("Name", contents.get(0).getName());
        assertEquals("Biografisches", contents.get(0).getText());

        assertEquals("language", contents.get(1).getName());
        assertEquals("de", contents.get(1).getAttributeValue("name"));
        assertEquals("Biografisches", contents.get(1).getText());

        assertEquals("language", contents.get(2).getName());
        assertEquals("en", contents.get(2).getAttributeValue("name"));
        assertEquals("Biografisches", contents.get(2).getText());
    }

    @Test
    public void testFixDoesNothingIfNoMetadataTypeExists() {
        Element root = new Element("Root");

        // No MetadataType child

        Element element = new Element("Corporate");
        Element marc = new Element("Marc");
        marc.addContent(element);

        RulesetValidationError error = new RulesetValidationError(
                1, 1, "WARNING", "XYZ - Fehler", element, ErrorType.VALIDATE_FORMATS);

        FixAddMetadataType fixer = new FixAddMetadataType();
        fixer.fix(root, error);

        assertTrue(root.getChildren().isEmpty());
    }
}