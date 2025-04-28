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

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.xml.XMLError;

/**
 * Find Cardinality values in the <DocStrctType> elements which are not equal to "1o", "*", "1m" or "+" and return those into the errors list
 * 
 * @author Paul Hankiewicz Lopez
 * @version 04.02.2025
 */
public class ValidateCardinality {

    /**
     * Validate the XML structure starting from the root element for not allowed values in the "num" field
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<XMLError> validate(org.jdom2.Element root) {
        List<XMLError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
            checkForInvalidCardinatliy(errors, element);
        }
        return errors;
    }

    /**
     * Checks the child elements of a given XML element for invalid cardinality values.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for invalid cardinality.
     */
    private void checkForInvalidCardinatliy(List<XMLError> errors, Element element) {
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        for (Element childElement : childElements) {
            // Skip elements that are not "group" or "metadata"
            if (!"group".equals(childElement.getName()) && !"metadata".equals(childElement.getName())) {
                continue;
            }
            String attributeValue = childElement.getAttributeValue("num");
            if (attributeValue != null && !"1o".equals(attributeValue) && !"*".equals(attributeValue) && !"1m".equals(attributeValue)
                    && !"+".equals(attributeValue)) {
                errors.add(new XMLError("ERROR",
                        Helper.getTranslation("ruleset_validation_wrong_cardinality", childElement.getText(), nameElement.getText(),
                                attributeValue),
                        childElement.getAttributeValue("lineNumber")));
            }
        }
    }
}