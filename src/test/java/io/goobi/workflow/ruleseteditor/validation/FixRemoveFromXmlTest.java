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

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixRemoveFromXmlTest {

    private Element root;
    private FixRemoveFromXml fixer;

    @Before
    public void setUp() throws Exception {
        String xml = "<root>"
                + "  <parent>"
                + "    <child goobi_lineNumber='10'>Test</child>"
                + "  </parent>"
                + "</root>";

        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(xml));
        root = document.getRootElement();

        fixer = new FixRemoveFromXml();
    }

    @Test
    public void testFix_NoMatchingLine() {
        RulesetValidationError error = new RulesetValidationError("dummy", "message", "line", 999, root);

        fixer.fix(root, error, false);

        Element parent = root.getChild("parent");
        assertNotNull("parent should still exist", parent);
        assertNotNull("child should still exist", parent.getChild("child"));
    }
}
