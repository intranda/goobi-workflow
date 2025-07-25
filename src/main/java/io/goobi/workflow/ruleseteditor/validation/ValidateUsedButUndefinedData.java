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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

/**
 * Find used but undefined data
 * 
 * @author Paul Hankiewicz Lopez
 * @version 25.04.2025
 */
public class ValidateUsedButUndefinedData {
    /**
     * Validate the XML structure starting from the root element to find used but undefined values
     *
     * @param root The root XML element to be validated.
     * @return A list of XMLError objects containing details about any duplicate entries found during validation.
     */
    public List<RulesetValidationError> validate(org.jdom2.Element root) {
        List<RulesetValidationError> errors = new ArrayList<>();
        Map<String, List<List<Object>>> allUsedValues = new HashMap<>();
        Set<String> allDefinedValues = new HashSet<>();
        List<String> allUndefinedValues = new ArrayList<>();

        getAllUsedValues(root, allUsedValues);
        getAllDefinedValues(root, allDefinedValues);

        // Add values to the Undefined Values list if they are defined but not used
        for (String value : allUsedValues.keySet()) {
            if (!allDefinedValues.contains(value)) {
                allUndefinedValues.add(value);
            }
        }
        for (String value : allUndefinedValues) {
            List<List<Object>> dataList = allUsedValues.get(value);
            for (List<Object> data : dataList) {
                String lineNumber = (String) data.get(0);
                Element element = (Element) data.get(1);
                createError(errors, value, lineNumber, element);
            }
        }
        return errors;
    }

    /**
     * Collect all used values (groups, metadata, allowedchildtype) and their line Numbers
     *
     * @param root
     * @param allUsedValues
     */
    private void getAllUsedValues(Element root, Map<String, List<List<Object>>> allUsedValues) {
        // Run through all root Children
        for (Element element : root.getChildren()) {
            // Only go into DocStrcts and Groups
            if ("DocStrctType".equals(element.getName()) || "Group".equals(element.getName())) {
                List<Element> childElements = element.getChildren();

                // Go through all children of the Element
                for (Element childElement : childElements) {
                    // Add all group, metadata and allowedchildtype to the M
                    if ("group".equals(childElement.getName())) {
                        String value = "Group:" + childElement.getText().trim();
                        String lineNumber = childElement.getAttributeValue("goobi_lineNumber");
                        String lineInfo = (lineNumber != null) ? lineNumber : "0";
                        List<Object> data = new ArrayList<>();
                        data.add(lineInfo);
                        data.add(childElement);
                        allUsedValues.computeIfAbsent(value, k -> new ArrayList<>()).add(data);

                    } else if ("metadata".equals(childElement.getName().trim())) {
                        String value = "MetadataType:" + childElement.getText().trim();
                        String lineNumber = childElement.getAttributeValue("goobi_lineNumber");
                        String lineInfo = (lineNumber != null) ? lineNumber : "0";
                        List<Object> data = new ArrayList<>();
                        data.add(lineInfo);
                        data.add(childElement);
                        allUsedValues.computeIfAbsent(value, k -> new ArrayList<>()).add(data);

                    } else if ("allowedchildtype".equals(childElement.getName())) {
                        String value = "DocStrctType:" + childElement.getText().trim();
                        String lineNumber = childElement.getAttributeValue("goobi_lineNumber");
                        String lineInfo = (lineNumber != null) ? lineNumber : "0";
                        List<Object> data = new ArrayList<>();
                        data.add(lineInfo);
                        data.add(childElement);
                        allUsedValues.computeIfAbsent(value, k -> new ArrayList<>()).add(data);
                    }
                }
            }
        }
    }

    /**
     * Collect all defined values in the xml structure
     *
     * @param root
     * @param allDefinedValues
     */
    private void getAllDefinedValues(Element root, Set<String> allDefinedValues) {
        for (Element element : root.getChildren()) {
            Element nameElement = element.getChild("Name");
            if (nameElement != null) {
                String name = nameElement.getTextTrim();
                allDefinedValues.add(element.getName() + ":" + name);
            }
        }
    }

    /**
     * Create the errors for the found undefined values
     *
     * @param errors
     * @param value
     * @param lineNumber
     */
    private void createError(List<RulesetValidationError> errors, String value, String lineNumber, Element element) {
        String[] parts = value.split(":", 2);
        String key = parts[0];
        String val = parts[1];

        if ("MetadataType".equals(key)) {
            errors.add(new RulesetValidationError("ERROR", Helper.getTranslation("ruleset_validation_usedButUndefined_metadata", val),
                    lineNumber, 12, element));
        } else if ("Group".equals(key)) {
            errors.add(new RulesetValidationError("ERROR", Helper.getTranslation("ruleset_validation_usedButUndefined_group", val),
                    lineNumber, 12, element));
        } else if ("DocStrctType".equals(key)) {
            errors.add(new RulesetValidationError("ERROR", Helper.getTranslation("ruleset_validation_usedButUndefined_docstruct", val),
                    lineNumber, 12, element));
        }
    }

}