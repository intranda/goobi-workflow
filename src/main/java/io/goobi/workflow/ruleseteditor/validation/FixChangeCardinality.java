package io.goobi.workflow.ruleseteditor.validation;

import org.jdom2.Element;
import org.jdom2.Attribute;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixChangeCardinality {
	/**
	 * Set the num attribute value of the error element depending on the "value"
	 * 
	 * @param root
	 * @param error
	 * @param value This value was given through the press of the correction button and the js
	 */
	public void fix(Element root, RulesetValidationError error, int value) {
		Element element = findElementByLineNumber(root, error.getLine());
		if(element != null) {
			// TODO: Replace these numeric values with an enum
			var numValue = switch (value) {
				case 1 -> "1o";
				case 2 -> "*";
				case 3 -> "1m";
				case 4 -> "+";
				default -> throw new IllegalArgumentException("Invalid value: " + value);
			};
			element.setAttribute("num", numValue);
		}
	}


	/**
	 * Recursively searches the XML tree for an element with a specific line number.
	 * 
	 * @param current
	 * @param targetLine
	 * @return
	 */
	private Element findElementByLineNumber(Element current, int targetLine) {
		String lineAttr = current.getAttributeValue("goobi_lineNumber");
		if (lineAttr != null && Integer.parseInt(lineAttr) == targetLine) {
			return current;
		}

		for (Element child : current.getChildren()) {
			Element result = findElementByLineNumber(child, targetLine);
			if (result != null) {
				return result;
			}
		}

		return null;
	}
}