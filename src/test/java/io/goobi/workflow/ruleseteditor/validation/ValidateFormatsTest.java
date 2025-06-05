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

public class ValidateFormatsTest {

    private ValidateFormats validator;

    @Before
    public void setUp() {
        validator = new ValidateFormats();
    }

    @Test
    public void testUndefinedMetadataType_shouldReturnError() {
        Element root = new Element("Ruleset");

        // Define <Formats><PicaPlus><Metadata><name>Title</name></Metadata></PicaPlus></Formats>
        Element formats = new Element("Formats");
        Element picaPlus = new Element("PicaPlus");
        Element metadata = new Element("Metadata");
        metadata.setAttribute("goobi_lineNumber", "5");
        Element name = new Element("name");
        name.setText("Title");
        metadata.addContent(name);
        picaPlus.addContent(metadata);
        formats.addContent(picaPlus);
        root.addContent(formats);

        // No <MetadataType> defined with Name=Title
        List<RulesetValidationError> errors = validator.validate(root, "PicaPlus", "name");

        assertEquals(1, errors.size());
        RulesetValidationError error = errors.get(0);
        assertEquals("ERROR", error.getSeverity());
        assertTrue(error.getMessage().contains("ruleset_validation_used_but_undefined_metadata_for_export"));
        assertEquals(5, error.getLine());
    }

    @Test
    public void testDefinedMetadataType_shouldPassValidation() {
        Element root = new Element("Ruleset");

        // Used
        Element formats = new Element("Formats");
        Element picaPlus = new Element("PicaPlus");
        Element metadata = new Element("Metadata");
        metadata.setAttribute("goobi_lineNumber", "7");
        Element name = new Element("name");
        name.setText("ValidTitle");
        metadata.addContent(name);
        picaPlus.addContent(metadata);
        formats.addContent(picaPlus);
        root.addContent(formats);

        // Defined
        Element metadataType = new Element("MetadataType");
        Element definedName = new Element("Name");
        definedName.setText("ValidTitle");
        metadataType.addContent(definedName);
        root.addContent(metadataType);

        List<RulesetValidationError> errors = validator.validate(root, "PicaPlus", "name");

        assertTrue(errors.isEmpty());
    }

    @Test
    public void testGroupRecursion_shouldDetectUndefinedGroup() {
        Element root = new Element("Ruleset");

        // Used in nested group
        Element formats = new Element("Formats");
        Element lido = new Element("LIDO");

        Element outerGroup = new Element("Group");
        outerGroup.setAttribute("goobi_lineNumber", "15");

        Element innerGroup = new Element("Group");
        innerGroup.setAttribute("goobi_lineNumber", "16");

        Element name = new Element("InternalName");
        name.setText("UnknownGroup");
        innerGroup.addContent(name);
        outerGroup.addContent(innerGroup);
        lido.addContent(outerGroup);
        formats.addContent(lido);
        root.addContent(formats);

        // No Group definition with Name = UnknownGroup
        List<RulesetValidationError> errors = validator.validate(root, "LIDO", "InternalName");

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().contains("ruleset_validation_used_but_undefined_group_for_export"));
        assertEquals(16, errors.get(0).getLine());
    }

    @Test
    public void testDefinedGroup_shouldNotRaiseError() {
        Element root = new Element("Ruleset");

        // Used
        Element formats = new Element("Formats");
        Element lido = new Element("LIDO");
        Element group = new Element("Group");
        group.setAttribute("goobi_lineNumber", "18");
        Element name = new Element("InternalName");
        name.setText("MyGroup");
        group.addContent(name);
        lido.addContent(group);
        formats.addContent(lido);
        root.addContent(formats);

        // Defined
        Element definedGroup = new Element("Group");
        Element definedName = new Element("Name");
        definedName.setText("MyGroup");
        definedGroup.addContent(definedName);
        root.addContent(definedGroup);

        List<RulesetValidationError> errors = validator.validate(root, "LIDO", "InternalName");

        assertTrue(errors.isEmpty());
    }
}
