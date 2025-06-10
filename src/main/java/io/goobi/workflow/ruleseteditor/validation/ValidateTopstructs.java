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
 * Find unallowed values in values in the "allowedchildtype" elements
 * 
 * @author Paul Hankiewicz Lopez
 * @version 25.04.2025
 */
public class ValidateTopstructs {
	/**
	 * Validate the XML structure starting from the root element to find unallowed
	 * values in the "allowedchildtype" elements
	 *
	 * @param root The root XML element to be validated.
	 * @return A list of XMLError objects containing details about any duplicate
	 *         entries found during validation.
	 */
	public List<RulesetValidationError> validate(org.jdom2.Element root) {
		List<RulesetValidationError> errors = new ArrayList<>();
		Map<String, String> allTopstructs = new HashMap<>();

		// Go through all elements and get the names of the topstructs
		for (Element element : root.getChildren()) {
			String topStruct = element.getAttributeValue("topStruct");
			if ("DocStrctType".equals(element.getName()) && topStruct != null) {
				Element nameElement = element.getChild("Name");
				if (nameElement != null) {
					String name = nameElement.getText();
					String lineNumber = nameElement.getAttributeValue("goobi_lineNumber");
					String lineInfo = (lineNumber != null) ? lineNumber : "0";
					allTopstructs.put(name, lineInfo);
				}
			}
		}
		// Go through all DocStrctType elements
		for (Element element : root.getChildren()) {
			List<Element> allowedChildTypeList = element.getChildren("allowedchildtype");
			if ("DocStrctType".equals(element.getName())) {
				if (element.getAttribute("anchor") != null && "true".equals(element.getAttributeValue("anchor"))){
					continue;
				}
				for (Element allowedChildType : allowedChildTypeList) {
					String allowedChildTypeText = allowedChildType.getText();
					// if an allowedchildtype's name equals one name of the all Topstrcs map it is
					// not allowed there
					if (allTopstructs.containsKey(allowedChildTypeText)) {
						String lineNumber = allowedChildType.getAttributeValue("goobi_lineNumber");
						String lineInfo = (lineNumber != null) ? lineNumber : "0";
						createError(errors, allowedChildTypeText, lineInfo, element);
					}
				}
			}
		}
		return errors;
	}

	/**
	 * Create the errors for the allowedchildtype values which are not supposed to
	 * be there
	 *
	 * @param errors
	 * @param name
	 * @param lineInfo
	 */
	private void createError(List<RulesetValidationError> errors, String name, String lineInfo, Element element) {
		errors.add(new RulesetValidationError("ERROR",
				Helper.getTranslation("ruleset_validation_unallowedUsageOfTopstruct", name), lineInfo, 9,element));
	}
}