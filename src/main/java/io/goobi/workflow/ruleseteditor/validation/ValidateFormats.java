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
import java.util.Map;
import java.util.Set;
import java.util.List;

import org.jdom2.Element;
import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateFormats {

	/**
	 * Validates the structure of an XML document by checking the export formats
	 *
	 * @param root   The root element of the XML document to validate.
	 * @param format The format of the export part which will be checked
	 * @param name   This value is either "name" or "InternalName" depends on the
	 *               format
	 * @return A list of {@link RulesetValidationError} objects containing
	 *         validation errors, if any.
	 */
	public List<RulesetValidationError> validate(Element root, String format, String nameValue) {
		List<RulesetValidationError> errors = new ArrayList<>();
		Map<String, List<Object>> allUsedValues = new HashMap<>();
		Set<String> allDefinedValues = new HashSet<>();

		checkElements(root, errors, "DocStruct", "DocStrctType", format, nameValue, allUsedValues);
		checkElements(root, errors, "Metadata", "MetadataType", format, nameValue, allUsedValues);

		// In the following cases there is a <Person> value which also has to be checked
		if ("PicaPlus".equals(format) || "Marc".equals(format)) {
			checkElements(root, errors, "Person", "MetadataType", format, nameValue, allUsedValues);
		}
		if ("PicaPlus".equals(format)) {
			checkElements(root, errors, "Corporate", "MetadataType", format, nameValue, allUsedValues);
		}
		checkElements(root, errors, "Group", "Group", format, nameValue, allUsedValues);

		// Run through the Elements and build a String containing the
		// elementName:elementChild("Name") and if it is a MetadataType also collect the
		// attribute values in between and add all of that to the map
		for (Element element : root.getChildren()) {
			if ("MetadataType".equals(element.getName()) || "Group".equals(element.getName())
					|| "DocStrctType".equals(element.getName())) {
				Element nameElement = element.getChild("Name");
				if (nameElement != null && !nameElement.getTextTrim().isEmpty()) {
					String key;
					if ("MetadataType".equals(element.getName())) {
						String typeAttr = element.getAttributeValue("type");
						String typeValue = (typeAttr != null) ? typeAttr : "null";
						key = element.getName() + ":" + typeValue + ":" + nameElement.getText();
					} else {
						key = element.getName() + ":" + nameElement.getText();
					}

					allDefinedValues.add(key);
				}
			}
		}
		// Go through the Map of allUsedValues and compare it with the definedValues List
		for (Map.Entry<String, List<Object>> entry : allUsedValues.entrySet()) {
			boolean foundMatch = false;
			List<Object> valueList = entry.getValue();
			String fullValue = (String) valueList.get(0);
		    Element element = (Element) valueList.get(1);
			String[] parts = fullValue.split(":", 3);

			String usedElementType = parts[0];
			String usedElementAttributeValue = (parts.length == 3) ? parts[1] : null;
			String usedElementNameValue = (parts.length == 3) ? parts[2] : parts[1];

			for (String definedValue : allDefinedValues) {
				String[] defParts = definedValue.split(":", 3);
				String definedElementType = defParts[0];
				String definedElementAttributeValue = (defParts.length == 3) ? defParts[1] : null;
				String definedElementNameValue = (defParts.length == 3) ? defParts[2] : defParts[1];

				boolean attributesMatch = usedElementAttributeValue == null || definedElementAttributeValue == null
						|| usedElementAttributeValue.equals(definedElementAttributeValue);

				if (usedElementType.equals(definedElementType) && usedElementNameValue.equals(definedElementNameValue)
						&& attributesMatch) {
					foundMatch = true;
					break;
				}

			}
			if (!foundMatch) {
				createError(errors, usedElementType, usedElementAttributeValue, usedElementNameValue, entry.getKey(),
						format, element);
			}
		}

		return errors;
	}

	/**
	 * Checks if specific export elements in the given XML document are defined but
	 * not used.
	 *
	 * @param root       The root element of the XML document.
	 * @param errors     A list of {@link RulesetValidationError} objects to which
	 *                   validation errors are added.
	 * @param type       The type of the XML element to check (e.g., "DocStruct",
	 *                   "Metadata", "Person").
	 * @param definition The expected definition of the element (e.g.,
	 *                   "DocStrctType", "MetadataType").
	 * @param format     The format of the XML document.
	 * @param name       This value is either "name" or "InternalName" depends on
	 *                   the format
	 * @param allUsed    Value hashmap where all used values should add into it
	 */
	private void checkElements(Element root, List<RulesetValidationError> errors, String type, String definition,
			String format, String name, Map<String, List<Object>> allUsedValues) {
		Element formats = root.getChild("Formats");
		if (formats != null) {
			Element formatElement = formats.getChild(format);
			if (formatElement != null) {
				List<Element> elements = formatElement.getChildren(type);
				for (Element elem : elements) {
					Element nameElement = elem.getChild(name);
					if (nameElement != null) {
						String value = nameElement.getTextTrim();
						String lineNumber = elem.getAttributeValue("goobi_lineNumber");
						if (type.equals("Metadata")) {
							type = "MetadataType";
						} else if (type.equals("DocStruct")) {
							type = "DocStrctType";
						} else if (type.equals("Person")) {
							type = "MetadataType:person";
						} else if (type.equals("Corporate")) {
							type = "MetadataType:corporate";
						}
						List<Object> valueList = new ArrayList<>();
						valueList.add(type + ":" + value);
						valueList.add(elem);
						allUsedValues.put(lineNumber, valueList);

					}
				}
			}
		}
	}

	/**
	 * Create the errors for the found undefined values
	 *
	 * @param errors
	 * @param value
	 * @param attributeValue
	 * @param lineNumber
	 * @param formatNameValue
	 */
	private void createError(List<RulesetValidationError> errors, String usedElementType,
			String usedElementAttributeValue, String usedElementNameValue, String lineNumber, String formatName, Element element) {
		if ("MetadataType".equals(usedElementType)) {
			if (usedElementAttributeValue == null) {
				errors.add(new RulesetValidationError("ERROR",
						Helper.getTranslation("ruleset_validation_used_but_undefined_metadata_for_export",
								usedElementNameValue, formatName),
						lineNumber,6, element));
			} else {
				if (usedElementAttributeValue.equals("person")) {
					errors.add(new RulesetValidationError("ERROR",
							Helper.getTranslation("ruleset_validation_used_but_undefined_person_for_export",
									usedElementNameValue, formatName),
							lineNumber, 6, element));
				} else if (usedElementAttributeValue.equals("corporate")) {
					errors.add(new RulesetValidationError("ERROR",
							Helper.getTranslation("ruleset_validation_used_but_undefined_corporate_for_export",
									usedElementNameValue, formatName),
							lineNumber,6, element));
				}
			}
		} else if ("Group".equals(usedElementType)) {
			errors.add(new RulesetValidationError("ERROR",
					Helper.getTranslation("ruleset_validation_used_but_undefined_group_for_export",
							usedElementNameValue, formatName),
					lineNumber,6, element));
		}
	}
}