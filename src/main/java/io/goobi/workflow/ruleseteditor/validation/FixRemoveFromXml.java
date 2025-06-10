package io.goobi.workflow.ruleseteditor.validation;

import org.jdom2.Element;


import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixRemoveFromXml {
	/**
	 * Remove the Element from the xml
	 * 
	 * @param root
	 * @param error
	 */
	public void fix(Element root, RulesetValidationError error, boolean deleteParent) {
	    Element toRemove = findElementByLineNumber(root, error);
	    
	    if (toRemove != null) {
	        if (deleteParent) {
	            Element parent = toRemove.getParentElement();
	            if (parent != null) {
	                Element grandParent = parent.getParentElement();
	                if (grandParent != null) {
	                	grandParent.removeContent(parent);
	                }
	            }
	        } else {
	            Element parent = toRemove.getParentElement();
	            if (parent != null) {
	            	parent.removeContent(toRemove);
	            }
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
	private Element findElementByLineNumber(Element current, RulesetValidationError error) {
		String lineAttr = current.getAttributeValue("goobi_lineNumber");
		if (lineAttr != null && Integer.parseInt(lineAttr) == error.getLine()) {

			if ((("Name".equals(current.getName()) || "InternalName".equals(current.getName())) == false) && error.getErrorType() == RulesetValidationError.ErrorType.VALIDATE_FORMATS ) {
				Element nameChild = findFirstNameChild(current);
				if (nameChild != null) {
					return nameChild;
				}
			}
			return current;
		}

		for (Element child : current.getChildren()) {
			Element result = findElementByLineNumber(child, error);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	/**
	 * Recursively searches the child Elements for "name" or "InternalName" value
	 * 
	 * @param element
	 * @return
	 */
	private Element findFirstNameChild(Element element) {
		if ("Name".equals(element.getName()) || "InternalName".equals(element.getName())) {
			return element;
		}

		for (Element child : element.getChildren()) {
			Element result = findFirstNameChild(child);
			if (result != null) {
				return result;
			}
		}

		return null;
	}
}