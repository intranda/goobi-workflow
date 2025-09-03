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

import org.jdom2.Element;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixChangeCardinality {
    /**
     * Set the num attribute value of the error element depending on the "value".
     * 
     * @param root
     * @param error
     * @param value This value was given through the press of the correction button and the js
     */
    public void fix(Element root, RulesetValidationError error, int value) {
        Element element = findElementByLineNumber(root, error.getLine());
        if (element != null) {
            // TODO: Replace these numeric values with an enum
            var numValue = switch (value) {
                case 1 -> "1o";
                case 2 -> "*";
                case 3 -> "1m";
                case 4 -> "+";
                default -> throw new IllegalArgumentException("Invalid value: " + value);
            };
            element.setAttribute("num", numValue);
        }
    }

    /**
     * Recursively searches the XML tree for an element with a specific line number.
     * 
     * @param current
     * @param targetLine
     * @return
     */
    private Element findElementByLineNumber(Element current, int targetLine) {
        String lineAttr = current.getAttributeValue("goobi_lineNumber");
        if (lineAttr != null && Integer.parseInt(lineAttr) == targetLine) {
            return current;
        }

        for (Element child : current.getChildren()) {
            Element result = findElementByLineNumber(child, targetLine);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}