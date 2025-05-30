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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jdom2.Element;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateDataNotMappedForExport {
	/**
	 * Find all values that are defined but not used inside the formats as <InternalName> values
	 * @param root
	 * @param format
	 * @return
	 */
	public List<RulesetValidationError> validate(org.jdom2.Element root, String format) {
		List<RulesetValidationError> errors = new ArrayList<>();
		Map<String, List<Object>> allUsedValuesInFormats = new HashMap<>();
		Map<String, Element> allDefinedValues = new HashMap<>();

		for (Element element : root.getChildren()) {
			if ("Formats".equals(element.getName())) {
				Element formatElement = element.getChild(format);
				if (formatElement != null) {
					collectNamesInFormat(formatElement, allUsedValuesInFormats, format);
				}
			}
		}
		// If no value was used the format might not be used at all
		if (allUsedValuesInFormats.isEmpty()) {
		    return errors; 
		}
		
		// Go throguh all DocStrct, Groups and MetadataType Elements and get their names in the Set
		for (Element element : root.getChildren()) {
			if("DocStrctType".equals(element.getName()) || "MetadataType".equals(element.getName()) || "Group".equals(element.getName())){
				Element nameElement = element.getChild("Name");
				String elementName = element.getName();
				 if (nameElement != null && !nameElement.getTextTrim().isEmpty()) {
			            allDefinedValues.put(elementName + ":" + nameElement.getText(), element);
			        }
			}
		}
		
		for (Map.Entry<String, Element> entry : allDefinedValues.entrySet()) {
		    if (!allUsedValuesInFormats.containsKey(entry.getKey())) {
				String lineNumber = entry.getValue().getAttributeValue("goobi_lineNumber");
				String lineInfo = (lineNumber != null) ? lineNumber : "0";
		        createError(errors, entry.getKey(), lineInfo, entry.getValue());
		    }
		}
		
		return errors;
	}
	
	/**
	 * Go through the formats Name children recursivly to find all "InternalName" values and add them to the Map
	 * @param parent
	 * @param allUsedValues
	 * @param formatName
	 */
	private void collectNamesInFormat(Element parent, Map<String, List<Object>> allUsedValuesInFormats, String formatName) {
	    for (Element child : parent.getChildren()) {
	        String name = child.getName();

	        if ("Metadata".equals(name) || "Person".equals(name) || "Corporate".equals(name)
	                || "DocStruct".equals(name) || "Group".equals(name)) {

	            String lineNumber = child.getAttributeValue("goobi_lineNumber");
	            String lineInfo = (lineNumber != null) ? lineNumber : "0";

	            Element nameElement = child.getChild("InternalName");
	            if (nameElement != null && !nameElement.getTextTrim().isEmpty()) {
	                String value = nameElement.getTextTrim();
	                if(name.equals("Metadata") || name.equals("Person") || name.equals("Corporate")) {
	                	name = "MetadataType";
	                } else if(name.equals("Group")) {
	                	name = "Group";
	                } else if(name.equals("DocStruct")) {
	                	name = "DocStrctType";
	                }
	                String key = name + ":" + value;
	                List<Object> valueList = new ArrayList<>();
	                valueList.add(lineInfo);       
	                valueList.add(nameElement);    
	                allUsedValuesInFormats.put(key, valueList);
	            }
	            // In LIDO there can be <Metadata> inside a Group
	            if ("Group".equals(name) && "LIDO".equals(formatName)) {
	                collectNamesInFormat(child, allUsedValuesInFormats, formatName);
	            }
	        } else {
	            collectNamesInFormat(child, allUsedValuesInFormats, formatName);
	        }
	    }
	}

    /**
     * Create the errors for the found undefined values
     *
     * @param errors
     * @param value
     * @param lineNumber
     */
    private void createError(List<RulesetValidationError> errors, String value, String lineNumber, Element element) {
        String[] parts = value.split(":", 2);
        String key = parts[0];
        String val = parts[1];
        if ("MetadataType".equals(key)) {
            errors.add(new RulesetValidationError("INFO", Helper.getTranslation("ruleset_validation_usedButUnMappedForExport_metadata", val),
                    lineNumber, 3, element));
        } else if ("Group".equals(key)) {
            errors.add(new RulesetValidationError("INFO", Helper.getTranslation("ruleset_validation_usedButUnMappedForExport_group", val),
                    lineNumber,3, element));
        } else if ("DocStrctType".equals(key)) {
            errors.add(new RulesetValidationError("INFO", Helper.getTranslation("ruleset_validation_usedButUnMappedForExport_docstruct", val),
                    lineNumber,3,element));
        }
    }
}