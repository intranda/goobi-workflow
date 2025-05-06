package io.goobi.workflow.ruleseteditor.validation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.html.HTML.Attribute;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateDataDefinedMultipleTimes {
	/**
	 * Validate the XML structure starting from the root element to find duplicate
	 * elements in xml
	 * 
	 * @param root The root XML element to be validated.
	 * @return errors A list of XMLError objects containing details about duplicate
	 *         elements
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
							String nextName = nextNameElement.getText();
							// An other error will be thrown
							if (name == null || nextName == null) {
								continue;
							}
							if (name.equals(nextName)) {
								String lineNumber = nextNameElement.getAttributeValue("lineNumber");
								String lineInfo = (lineNumber != null) ? lineNumber : "0";
								createError(errors, elementType, nextName, lineInfo);
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
	private void createError(List<RulesetValidationError> errors, String elementType, String name, String lineInfo) {
		if (elementType.equals("DocStrctType")) {
			errors.add(new RulesetValidationError("WARNING",
					Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_docstruct", name), lineInfo));
		} else if (elementType.equals("Group")) {
			errors.add(new RulesetValidationError("WARNING",
					Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_group", name), lineInfo));
		} else if (elementType.equals("MetadataType")) {
			errors.add(new RulesetValidationError("WARNING",
					Helper.getTranslation("ruleset_validation_valueDefinedMultipleTimes_metadata", name), lineInfo));

		}

	}
}