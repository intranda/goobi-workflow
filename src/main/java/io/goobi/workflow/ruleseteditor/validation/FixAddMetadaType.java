package io.goobi.workflow.ruleseteditor.validation;

import org.jdom2.Element;
import java.util.List;

import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class FixAddMetadaType {
	public void fix(Element root, RulesetValidationError error) {
		// get the part of the message value before the -
		String partBeforeDash = error.getMessage().split("-")[0].trim();
		
		System.out.println("test");

		List<Element> allChildren = root.getChildren();
		Element lastMetadataType = null;

		for (Element child : allChildren) {
			if ("MetadataType".equals(child.getName())) {
				lastMetadataType = child;
			}
		}
		if (lastMetadataType != null) { 
			Element newElement = new Element("MetadataType");
			Element name = new Element("Name").setText(partBeforeDash);
			newElement.addContent(name);
			
			int index = allChildren.indexOf(lastMetadataType);
			allChildren.add(index + 1, newElement);
		}
	}
}