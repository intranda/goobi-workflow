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

public class ValidateNames {
	public List<RulesetValidationError> validate(org.jdom2.Element root) {
		List<RulesetValidationError> errors = new ArrayList<>();
		for (Element element : root.getChildren()) {
			// Check every element for the <name> element (the Formats are in an own class)
			if (!"Formats".equals(element.getName())) {
				Element nameElement = element.getChild("Name");
				if (nameElement == null) {
					String lineNumber = element.getAttributeValue("lineNumber");
					String lineInfo = (lineNumber != null) ? lineNumber : "0";

					errors.add(new RulesetValidationError("ERROR",
							Helper.getTranslation("ruleset_validation_missing_name", element.getName()), lineInfo));
					continue;
				}
				if (nameElement.getTextTrim().isEmpty()) {
					String lineNumber = nameElement.getAttributeValue("lineNumber");
					String lineInfo = (lineNumber != null) ? lineNumber : "0";
					errors.add(new RulesetValidationError("ERROR",
							Helper.getTranslation("ruleset_validation_empty_name", element.getName()), lineInfo));
				}
			}
		}

		return errors;
	}
}