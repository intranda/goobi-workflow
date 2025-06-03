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
	    Attribute numAttribute = findElementByLineNumber(root, error.getLine()).getAttribute("num");
	    if(numAttribute != null) {
	    	if(value == 1) {
	    		numAttribute.setValue("1o");
	    	} else if(value == 2) {
	    		numAttribute.setValue("*");
	    	} else if(value == 3) {
	    		numAttribute.setValue("1m");
	    	} else if(value == 4) {
	    		numAttribute.setValue("+");
	    	}
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