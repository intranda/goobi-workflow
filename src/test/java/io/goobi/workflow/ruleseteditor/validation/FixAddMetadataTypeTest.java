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
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.easymock.EasyMock;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixAddMetadataTypeTest {

    private FixAddMetadataType fixer;

    @Before
    public void setUp() {
        fixer = new FixAddMetadataType();
    }

    @Test
    public void testFix_addsNewMetadataType_afterLastExisting() {
        // Setup XML structure
        Element root = new Element("Root");

        Element metadata1 = new Element("MetadataType");
        metadata1.addContent(new Element("Name").setText("Author"));

        Element metadata2 = new Element("MetadataType");
        metadata2.addContent(new Element("Name").setText("Title"));

        root.addContent(metadata1);
        root.addContent(metadata2);

        // Mock RulesetValidationError
        RulesetValidationError mockError = EasyMock.createMock(RulesetValidationError.class);
        EasyMock.expect(mockError.getMessage()).andReturn("NewType - missing metadata").anyTimes();
        EasyMock.replay(mockError);

        // Call method
        fixer.fix(root, mockError);

        // Assert
        List<Element> children = root.getChildren();
        assertEquals(3, children.size());

        Element inserted = children.get(2); // Should be right after metadata2
        assertEquals("MetadataType", inserted.getName());

        Element nameChild = inserted.getChild("Name");
        assertNotNull(nameChild);
        assertEquals("NewType", nameChild.getText());

        EasyMock.verify(mockError);
    }
}
