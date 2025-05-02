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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

/**
 * Find duplicate metadata, group and allowedChildType values in <DocstrctType> Element and return those into the errors list
 * 
 * @author Paul Hankiewicz Lopez
 * @version 04.02.2025
 */
public class ValidateDuplicatesInDocStrct {

    /**
     * Validate the XML structure starting from the root element for duplicate metadata, group and allowedChildType values in <DocstrctType>
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<RulesetValidationError> validate(Element root) {
        List<RulesetValidationError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
            checkDocStructNodesForDuplicates(errors, root, element);
        }
        return errors;
    }

    /**
     * Checks a specific XML element and its children for duplicates.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for duplicates.
     */
    private void checkDocStructNodesForDuplicates(List<RulesetValidationError> errors, Element root, Element element) {
        if (!"DocStrctType".equals(element.getName())) {
            return;
        }
        Map<String, String> valueMap = new HashMap<>();
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        String nameText = (nameElement != null) ? nameElement.getText().trim() : "";

        for (Element childElement : childElements) {
            String childName = childElement.getName();
            String childText = childElement.getText().trim();
            String childSignature = childName + ":" + childText;

            String lineNumber = childElement.getAttributeValue("lineNumber");
            String lineInfo = (lineNumber != null) ? lineNumber.trim() : "0";

            // Check if the childSignature already exists in the set
            if (valueMap.containsKey(childSignature)) {
                // Check if the child element name is "metadata"
                if ("metadata".equals(childName)) {
                    // Check if the found duplicate value is a person, corporate or metadata
                    findMetadataType(errors, root, childText, nameText, valueMap.get(childSignature));

                }
                // Check if the child element name is "group"
                else if ("group".equals(childName)) {
                    errors.add(
                            new RulesetValidationError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_group_group", childText, nameText),
                                    valueMap.get(childSignature)));
                }
                // Check if the child element name is "allowedchildtype"
                else if ("allowedchildtype".equals(childName)) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_group_allowedchildtype", childText, nameText),
                            valueMap.get(childSignature)));
                }
            } else {
                // Add the signature to the Map
                valueMap.put(childSignature, lineInfo);
            }
        }
    }

    /**
     * Find out if the duplicate value is either a person, corporate or a metadata.
     * 
     * @param errors
     * @param root
     * @param text
     * @param childElementText
     * @param nameElementText
     */
    private void findMetadataType(List<RulesetValidationError> errors, Element root, String childElementText, String nameElementText, String lineInfo) {
        for (Element element : root.getChildren()) {
            if (!"MetadataType".equals(element.getName()) && !"group".equals(element.getName()) && !"metadata".equals(element.getName())) {
                continue;
            }

            String nameValue = element.getChildText("Name");
            if (nameValue != null && childElementText.equals(nameValue.trim())) {
                String typeValue = element.getAttributeValue("type");

                if ("person".equals(typeValue)) {
                    errors.add(
                            new RulesetValidationError("ERROR", Helper.getTranslation("ruleset_validation_duplicates_person", childElementText, nameElementText),
                                    lineInfo));
                    return;
                }
                if ("corporate".equals(typeValue)) {
                    errors.add(
                            new RulesetValidationError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_corporate", childElementText, nameElementText), lineInfo));
                    return;

                } else {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_duplicates_metadata", childElementText, nameElementText), lineInfo));
                    return;
                }
            }
        }
    }

}