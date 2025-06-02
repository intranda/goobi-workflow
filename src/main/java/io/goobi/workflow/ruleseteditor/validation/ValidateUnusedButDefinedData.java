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

public class ValidateUnusedButDefinedData {

    /**
     * Validate the XML structure starting from the root element to find unused values
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<RulesetValidationError> validate(org.jdom2.Element root) {
        List<RulesetValidationError> errors = new ArrayList<>();
        List<String> allMetadataTypeNameValues = new ArrayList<>();
        List<String> allAllowedchildtypeValues = new ArrayList<>();
        List<String> allUnusedAllowedchildtypeValues = new ArrayList<>();

        for (Element element : root.getChildren()) {
            // Check the children of this element and add them to the list
            getAllMetadataTypeNameValues(errors, element, allMetadataTypeNameValues, allAllowedchildtypeValues);
        }

        for (Element element : root.getChildren()) {
            // Go through all children of the element and search for unused values
            searchInDocstrctTypesForUnusedValues(errors, element, allMetadataTypeNameValues, allAllowedchildtypeValues);
        }
        // Add all unused values to the errors list
        for (String unusedValue : allMetadataTypeNameValues) {
            findMetadataType(errors, root, unusedValue);
        }
        for (String unusedValue : allAllowedchildtypeValues) {
            for (Element element : root.getChildren()) {
            	if(element.getChild("Name") == null) {
            		continue;
            	}
                String lineNumber = element.getChild("Name").getAttributeValue("goobi_lineNumber");
                String lineInfo = (lineNumber != null) ? lineNumber.trim() : "0";
                // Check if the element is a DocStrctType and if the Name value is inside the allAllowedchildtypeValues.
                // If so it will be added to allUnusedAllowedchildtypeValues and a message will be displayed
                if ("DocStrctType".equals(element.getName())
                    && unusedValue.equals("DocStrctType:" + element.getChildText("Name").trim())
                    && !allUnusedAllowedchildtypeValues.contains(unusedValue)) {
                    allUnusedAllowedchildtypeValues.add(unusedValue.trim());
                    errors.add(new RulesetValidationError("WARNING", Helper.getTranslation("ruleset_validation_unused_values_allowedchildtype",
                            unusedValue.substring(unusedValue.lastIndexOf(":") + 1)), lineInfo,11, element.getChild("Name")));
                   
                }
            }
        }

        return errors;
    }

    /**
     * Add the elements name to allMetadataTypeNameValues
     * 
     * @param errors
     * @param element
     * @param allMetadataTypeNameValues
     */
    private void getAllMetadataTypeNameValues(List<RulesetValidationError> errors, Element element, List<String> allMetadataTypeNameValues,
            List<String> allAllowedchildtypeValues) {
        if ("MetadataType".equals(element.getName()) || "Group".equals(element.getName())) {
            // Add the Name text to the list
        	Element nameElement = element.getChild("Name");
            if (nameElement != null && nameElement.getText() != null && !nameElement.getText().trim().isEmpty()) {
                allMetadataTypeNameValues.add(element.getName() + ":" + nameElement.getText().trim());
            }
        } else if ("DocStrctType".equals(element.getName())) {
            List<Element> allowedChildTypeElements = element.getChildren("Name");
            for (Element child : allowedChildTypeElements) {
                String topStructValue = element.getAttributeValue("topStruct");
                if (topStructValue != null && "true".equals(topStructValue)) {
                    continue;
                }

                if (child != null && child.getText() != null && !child.getText().trim().isEmpty()) {
                    allAllowedchildtypeValues.add(element.getName() + ":" + child.getText().trim());
                }
            }
        }
    }

    /**
     * Go through all DocStrctType and Group Elements and if a value of allMetadataTypeNameValues was found it will be removed
     * 
     * @param errors
     * @param element
     * @param allMetadataTypeNameValues
     */
    private void searchInDocstrctTypesForUnusedValues(List<RulesetValidationError> errors, Element element, List<String> allMetadataTypeNameValues,
            List<String> allAllowedchildtypeValues) {

        if (!"DocStrctType".equals(element.getName()) && !"Group".equals(element.getName())) {
            return;
        }

        List<Element> childElements = element.getChildren();

        for (Element childElement : childElements) {
            if (!"group".equals(childElement.getName()) && !"metadata".equals(childElement.getName().trim())
                    && !"allowedchildtype".equals(childElement.getName())) {
                continue;
            }
            // If a value was found it is being used therefore it will be removed from the list
            if ("metadata".equals(childElement.getName())) {

                if (allMetadataTypeNameValues.contains("MetadataType:" + childElement.getText().trim())) {
                    allMetadataTypeNameValues.remove("MetadataType:" + childElement.getText().trim());
                }
            }

            if ("DocStrctType".equals(element.getName())) {
                if ("allowedchildtype".equals(childElement.getName())) {
                    if (allAllowedchildtypeValues.contains("DocStrctType:" + childElement.getText().trim())) {

                        allAllowedchildtypeValues.remove("DocStrctType:" + childElement.getText().trim());
                    }
                }
            }

            // Group used in same group
            if ("Group".equals(element.getName())) {
                if ("group".equals(childElement.getName()) && allMetadataTypeNameValues.contains("Group:" + childElement.getText().trim())) {
                    allMetadataTypeNameValues.remove("Group:" + childElement.getText().trim());
                }
            }
            // Groups in DocstrcyTypes
            if ("DocStrctType".equals(element.getName())) {
                if ("group".equals(childElement.getName())) {
                    allMetadataTypeNameValues.remove("Group:" + childElement.getText().trim());
                }
            }

        }
    }

    /**
     * Find out if the unused value is either a person, corporate or a metadata.
     * 
     * @param errors
     * @param root
     * @param text
     */
    private void findMetadataType(List<RulesetValidationError> errors, Element root, String text) {
        for (Element element : root.getChildren()) {
            if ("Group".equals(element.getName()) || "MetadataType".equals(element.getName())) {

                Element nameChild = element.getChild("Name");

                if (nameChild != null && nameChild.getText() != null && !nameChild.getText().trim().isEmpty()) {
                    String type = element.getAttributeValue("type");
                    String nameText = nameChild.getText().trim();

                    if ("person".equals(type) && ("MetadataType:" + nameText).equals(text)) {
                        errors.add(new RulesetValidationError("WARNING",
                                Helper.getTranslation("ruleset_validation_unused_values_person", text.substring(text.lastIndexOf(":") + 1)),
                                element.getChild("Name").getAttributeValue("goobi_lineNumber"),11, nameChild));
                        continue;
                    }

                    if ("corporate".equals(type) && ("MetadataType:" + nameText).equals(text)) {
                        errors.add(new RulesetValidationError("WARNING",
                                Helper.getTranslation("ruleset_validation_unused_values_corporate", text.substring(text.lastIndexOf(":") + 1)),
                                element.getChild("Name").getAttributeValue("goobi_lineNumber"),11, nameChild));
                        continue;
                    }

                    if ("Group".equals("Group:" + nameText)) {
                        errors.add(new RulesetValidationError("WARNING",
                                Helper.getTranslation("ruleset_validation_unused_values_groups", text.substring(text.lastIndexOf(":") + 1)),
                                element.getChild("Name").getAttributeValue("goobi_lineNumber"),11, nameChild));
                        continue;
                    } else if (("MetadataType:" + nameText).equals(text)) {
                        errors.add(new RulesetValidationError("WARNING",
                                Helper.getTranslation("ruleset_validation_unused_values_metadata", text.substring(text.lastIndexOf(":") + 1)),
                                element.getChild("Name").getAttributeValue("goobi_lineNumber"),11, nameChild));
                        continue;
                    }
                }
            }
        }
    }
}