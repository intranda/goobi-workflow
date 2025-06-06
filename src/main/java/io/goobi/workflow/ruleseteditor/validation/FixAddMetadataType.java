package io.goobi.workflow.ruleseteditor.validation;

import org.jdom2.Element;
import java.util.List;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixAddMetadataType {
	public void fix(Element root, RulesetValidationError error) {
		// get the part of the message value before the -
		String partBeforeDash = error.getMessage().split("-")[0].trim();

		List<Element> allChildren = root.getChildren();
		Element lastMetadataType = null;

		for (Element child : allChildren) {
			if ("MetadataType".equals(child.getName())) {
				lastMetadataType = child;
			}
		}
		if (lastMetadataType != null) { 
			Element newElement = new Element("MetadataType");
			if(error.getErrorType().equals(RulesetValidationError.ErrorType.VALIDATE_FORMATS) && (error.getElement().getParentElement().getName().equals("PicaPlus") || error.getElement().getParentElement().getName().equals("Marc")) && (error.getElement().getName().equals("Person") ||error.getElement().getName().equals("Corporate"))) {
				newElement.setAttribute("type", error.getElement().getName().toLowerCase());
			}
			Element name = new Element("Name").setText(partBeforeDash);
			Element languageDe = new Element("language").setText(partBeforeDash);
			Element languageEn = new Element("language").setText(partBeforeDash);
			languageDe.setAttribute("name", "de");
			languageEn.setAttribute("name", "en");
			newElement.addContent(name);
			newElement.addContent(languageDe);
			newElement.addContent(languageEn);
			
			int index = allChildren.indexOf(lastMetadataType);
			allChildren.add(index + 1, newElement);
		}
	}
}