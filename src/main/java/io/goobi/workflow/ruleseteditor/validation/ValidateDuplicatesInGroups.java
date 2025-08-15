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
 * Find duplicate metadata and group values in <Groups> and return those into the error list.
 * 
 * @author Paul Hankiewicz Lopez
 * @version 04.02.2025
 */
public class ValidateDuplicatesInGroups {

    /**
     * Validate the XML structure starting from the root element for duplicate metadata and group values in Groups.
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<RulesetValidationError> validate(Element root) {
        List<RulesetValidationError> errors = new ArrayList<>();
        for (Element element : root.getChildren()) {
            checkForUnvalidGroups(errors, root, element);
        }
        return errors;
    }

    /**
     * Checks a specific XML element and its children for duplicates.
     *
     * @param errors A list of XMLError objects to collect validation errors.
     * @param element The XML element to be checked for duplicates.
     */
    private void checkForUnvalidGroups(List<RulesetValidationError> errors, Element root, Element element) {
        if (!"Group".equals(element.getName())) {
            return;
        }
        Map<String, String> valueMap = new HashMap<>();
        List<Element> childElements = element.getChildren();
        Element nameElement = element.getChild("Name");
        String nameText = (nameElement != null) ? nameElement.getText().trim() : "";

        for (Element childElement : childElements) {
            String childName = childElement.getName().trim();
            String childText = childElement.getText().trim();
            String childSignature = childName + ":" + childText;

            if (valueMap.containsKey(childSignature)) {

                // Check if the child element name is "metadata"
                if ("metadata".equals(childName)) {
                    findMetadataType(errors, root, childText, nameText, valueMap.get(childSignature));
                }
                // Check if the child element name is "group"
                else if ("group".equals(childName)) {
                    errors.add(
                            new RulesetValidationError("ERROR",
                                    Helper.getTranslation("ruleset_validation_duplicates_group_metadata", childText,
                                            nameText),
                                    valueMap.get(childSignature), 5, childElement));
                }
            } else {
                // Add the signature to the map
                valueMap.put(childSignature, childElement.getAttributeValue("goobi_lineNumber"));
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
    private void findMetadataType(List<RulesetValidationError> errors, Element root, String childElementText, String nameElementText,
            String lineInfo) {
        for (Element element : root.getChildren()) {

            if (!"MetadataType".equals(element.getName()) || element.getChild("Name") == null) {
                continue;
            }

            Element nameChild = element.getChild("Name");

            // Check if the element is a person with the same name
            if ("person".equals(element.getAttributeValue("type")) && childElementText.equals(nameChild.getText().trim())) {
                errors.add(new RulesetValidationError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_person", childElementText, nameElementText), lineInfo, 5,
                        element));
                return;
            }

            if ("corporate".equals(element.getAttributeValue("type")) && childElementText.equals(nameChild.getText().trim())) {
                errors.add(new RulesetValidationError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_corporate", childElementText, nameElementText), lineInfo, 5,
                        element));
                return;

            } else if (childElementText.equals(nameChild.getText().trim())) {
                errors.add(new RulesetValidationError("ERROR",
                        Helper.getTranslation("ruleset_validation_duplicates_group_metadata", childElementText, nameElementText), lineInfo, 5,
                        element));
                return;
            }

        }
    }
}