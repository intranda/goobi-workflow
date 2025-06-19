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

public class FixRemoveFromXml {
    /**
     * Remove the Element from the xml
     * 
     * @param root
     * @param error
     */
    public void fix(Element root, RulesetValidationError error, boolean deleteParent) {
        Element toRemove = findElementByLineNumber(root, error);

        if (toRemove != null) {
            if (deleteParent) {
                Element parent = toRemove.getParentElement();
                if (parent != null) {
                    Element grandParent = parent.getParentElement();
                    if (grandParent != null) {
                        grandParent.removeContent(parent);
                    }
                }
            } else {
                Element parent = toRemove.getParentElement();
                if (parent != null) {
                    parent.removeContent(toRemove);
                }
            }
        }
    }

    /**
     * Recursively searches the XML tree for an element with a specific line number.
     * 
     * @param current
     * @param targetLine
     * @return
     */
    private Element findElementByLineNumber(Element current, RulesetValidationError error) {
        String lineAttr = current.getAttributeValue("goobi_lineNumber");
        if (lineAttr != null && Integer.parseInt(lineAttr) == error.getLine()) {

            if ((("Name".equals(current.getName()) || "InternalName".equals(current.getName())) == false)
                    && error.getErrorType() == RulesetValidationError.ErrorType.VALIDATE_FORMATS) {
                Element nameChild = findFirstNameChild(current);
                if (nameChild != null) {
                    return nameChild;
                }
            }
            return current;
        }

        for (Element child : current.getChildren()) {
            Element result = findElementByLineNumber(child, error);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    /**
     * Recursively searches the child Elements for "name" or "InternalName" value
     * 
     * @param element
     * @return
     */
    private Element findFirstNameChild(Element element) {
        if ("Name".equals(element.getName()) || "InternalName".equals(element.getName())) {
            return element;
        }

        for (Element child : element.getChildren()) {
            Element result = findFirstNameChild(child);
            if (result != null) {
                return result;
            }
        }

        return null;
    }
}