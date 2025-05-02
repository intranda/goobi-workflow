package io.goobi.workflow.ruleseteditor.validation;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateNamesInFormats {
	/**
	 * Go through the <Format> and find the <Name> or <InternalName> values that are empty or dont exist
	 * @param root
	 * @param formatNameToCheck
	 * @param nameElementKey
	 * @return
	 */
    public List<RulesetValidationError> validate(Element root, String formatNameToCheck, String nameElementKey) {
        List<RulesetValidationError> errors = new ArrayList<>();

        for (Element element : root.getChildren()) {
            if ("Formats".equals(element.getName())) {
                Element formatElement = element.getChild(formatNameToCheck);
                if (formatElement != null) {
                    searchInFormats(formatElement, errors, nameElementKey, formatNameToCheck);
                }
            }
        }
        return errors;
    }
    
    /**
     * Recursivly go through the <Format> and find the <Name> or <InternalName> values that are empty or dont exist
     * @param parent
     * @param errors
     * @param nameElementKey
     * @param formatName
     */
    private void searchInFormats(Element parent, List<RulesetValidationError> errors, String nameElementKey, String formatName) {
        for (Element child : parent.getChildren()) {
            String name = child.getName();

            if ("Metadata".equals(name) || "Person".equals(name) || "Corporate".equals(name) || "DocStruct".equals(name) || "Group".equals(name)) {
                String lineNumber = child.getAttributeValue("lineNumber");
                String lineInfo = (lineNumber != null) ? lineNumber : "0";

                Element nameElement = child.getChild(nameElementKey);

                if (nameElement == null) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_missing_name", name), lineInfo));
                } else if (nameElement.getTextTrim().isEmpty()) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_empty_name", name), lineInfo));
                }
                // In LIDO there can be <Metadata> inside a Group
                if ("Group".equals(name) && "LIDO".equals(formatName)) {
                    searchInFormats(child, errors, nameElementKey, formatName);
                }
            } else {
                searchInFormats(child, errors, nameElementKey, formatName);
            }
        }
    }
}
