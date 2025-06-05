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

import org.easymock.EasyMock;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixChangeCardinalityTest {

    private FixChangeCardinality fixer;

    @Before
    public void setUp() {
        fixer = new FixChangeCardinality();
    }

    @Test
    public void testFix_setsCorrectNumAttributeValue() {
        // Prepare XML element with attribute goobi_lineNumber = 42 and num = "old"
        Element root = new Element("Root");

        Element target = new Element("TargetElement");
        target.setAttribute("goobi_lineNumber", "42");
        target.setAttribute("num", "old");

        root.addContent(target);

        // Mock RulesetValidationError
        RulesetValidationError error = EasyMock.createMock(RulesetValidationError.class);
        EasyMock.expect(error.getLine()).andReturn(42).anyTimes();
        EasyMock.replay(error);

        // Call fix for value = 1 → should set num="1o"
        fixer.fix(root, error, 1);
        assertEquals("1o", target.getAttributeValue("num"));

        // Call fix for value = 2 → should set num="*"
        fixer.fix(root, error, 2);
        assertEquals("*", target.getAttributeValue("num"));

        // Call fix for value = 3 → should set num="1m"
        fixer.fix(root, error, 3);
        assertEquals("1m", target.getAttributeValue("num"));

        // Call fix for value = 4 → should set num="+"
        fixer.fix(root, error, 4);
        assertEquals("+", target.getAttributeValue("num"));

        EasyMock.verify(error);
    }

}
