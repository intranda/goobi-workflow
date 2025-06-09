package io.goobi.workflow.ruleseteditor.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;


public class ValidateTranslations {
	/**
	 * Validate the XML structure starting from the root element to find empty <language> fields in xml
	 * 
	 * @param root The root XML element to be validated.
	 * @return errors A list of XMLError objects containing details about unvalid Translations
	 */
	public List<RulesetValidationError> validate(org.jdom2.Element root) {
		List<RulesetValidationError> errors = new ArrayList<>();
		
		for (Element element : root.getChildren()) {
			for (Element language : element.getChildren("language")) {
				if(language.getText().trim().isEmpty()) {
                    String lineNumber = language.getAttributeValue("goobi_lineNumber");
                    String lineInfo = (lineNumber != null) ? lineNumber : "0";
                	createError(errors, element.getChild("Name").getText(), lineInfo, element);
                }
            }
		}
		
		return errors;
	}
	private void createError(List<RulesetValidationError> errors, String name, String lineInfo, Element element) {
        errors.add(new RulesetValidationError("ERROR", Helper.getTranslation("ruleset_validation_emptyTranslation", name), lineInfo, 10,element));
    }
}