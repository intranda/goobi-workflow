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
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateDataDefinedMultipleTimes {
    /**
     * Validate the XML structure starting from the root element to find duplicate elements in xml.
     * 
     * @param root The root XML element to be validated.
     * @return errors A list of XMLError objects containing details about duplicate elements
     */
    public List<RulesetValidationError> validate(org.jdom2.Element root) {
        List<RulesetValidationError> errors = new ArrayList<>();

        List<String> typesToCheck = List.of("DocStrctType", "MetadataType", "Group");
        for (String type : typesToCheck) {
            checkForDuplicates(root, type, errors);
        }
        return errors;

    }

    /**
     * Run through all elements of the same type and find duplicates
     * 
     * @param root
     * @param elementType
     * @param errors
     */
    private void checkForDuplicates(Element root, String elementType, List<RulesetValidationError> errors) {
        List<Element> allElements = root.getChildren(elementType);
        for (int i = 0; i < allElements.size(); i++) {
            Element currentElement = allElements.get(i);
            if (elementType.equals(currentElement.getName())) {
                Element nameElement = currentElement.getChild("Name");
                if (nameElement != null) {
                    String name = nameElement.getText();
                    // Go through all following elements and check if the name ist the same
                    for (int j = i + 1; j < allElements.size(); j++) {
                        Element nextElement = allElements.get(j);
                        if (elementType.equals(nextElement.getName())) {
                            Element nextNameElement = nextElement.getChild("Name");
                            if (nextNameElement != null && nextNameElement.getText() != null) {
                                String nextName = nextNameElement.getText();
                                // An other error will be thrown
                                if (name == null || nextName == null) {
                                    continue;
                                }
                                if (name.equals(nextName)) {
                                    String lineNumber = nextNameElement.getAttributeValue("goobi_lineNumber");
                                    String lineInfo = (lineNumber != null) ? lineNumber : "0";
                                    createError(errors, elementType, nextName, lineInfo, nextElement);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Create the errors for the duplicate values
     *
     * @param errors
     * @param elementType
     * @param attributeValue
     * @param name
     * @param lineInfo
     */
    private void createError(List<RulesetValidationError> errors, String elementType, String name, String lineInfo, Element element) {
        if ("DocStrctType".equals(elementType)) {
            errors.add(new RulesetValidationError("WARNING",
                    Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_docstruct", name), lineInfo, 2, element));
        } else if ("Group".equals(elementType)) {
            errors.add(new RulesetValidationError("WARNING",
                    Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_group", name), lineInfo, 2, element));
        } else if ("MetadataType".equals(elementType)) {
            errors.add(new RulesetValidationError("WARNING",
                    Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_metadata", name), lineInfo, 2, element));

        }

    }
}