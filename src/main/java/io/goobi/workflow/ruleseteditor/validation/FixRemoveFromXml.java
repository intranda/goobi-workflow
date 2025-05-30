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
	    Element toRemove = findElementByLineNumber(root, error.getLine());
	    
	    if (toRemove != null) {
	        if (deleteParent) {
	            Element parent = toRemove.getParentElement();
	            if (parent != null) {
	                Element grandParent = parent.getParentElement();
	                if (grandParent != null) {
	                    removeElementWithWhitespace(grandParent, parent);
	                }
	            }
	        } else {
	            Element parent = toRemove.getParentElement();
	            if (parent != null) {
	                removeElementWithWhitespace(parent, toRemove);
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
	
	private void removeElementWithWhitespace(Element parent, Element toRemove) {
	    int index = parent.indexOf(toRemove);

	    if (index > 0) {
	        org.jdom2.Content previous = parent.getContent(index - 1);
	        if (previous instanceof org.jdom2.Text) {
	            org.jdom2.Text text = (org.jdom2.Text) previous;
	            if (text.getTextTrim().isEmpty()) {
	                parent.removeContent(previous);
	            }
	        }
	    }

	    parent.removeContent(toRemove);
	}
}