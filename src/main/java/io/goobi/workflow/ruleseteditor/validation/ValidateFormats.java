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
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.ruleseteditor.RulesetValidationError;

public class ValidateFormats {

    /**
     * Validates the structure of an XML document by checking the export formats
     *
     * @param root The root element of the XML document to validate.
     * @param format The format of the export part which will be checked
     * @param name This value is either "name" or "InternalName" depends on the format
     * @return A list of {@link RulesetValidationError} objects containing validation errors, if any.
     */
    public List<RulesetValidationError> validate(Element root, String format) {
        List<RulesetValidationError> errors = new ArrayList<>();
        String name = "Name";
        if ("METS".equals(format) || "LIDO".equals(format)) {
            name = "InternalName";
        }
        checkElements(root, errors, "DocStruct", "DocStrctType", format, name);
        checkElements(root, errors, "Metadata", "MetadataType", format, name);

        // In the following cases there is a <Person> value which also has to be checked
        if ("PicaPlus".equals(format) || "Marc".equals(format)) {
            checkElements(root, errors, "Person", "MetadataType", format, name);
        }
        if ("PicaPlus".equals(format)) {
            checkElements(root, errors, "Corporate", "MetadataType", format, name);
        }
        checkElements(root, errors, "Group", "Group", format, name);
        return errors;
    }

    /**
     * Checks if specific export elements in the given XML document are defined but not used.
     *
     * @param root The root element of the XML document.
     * @param errors A list of {@link RulesetValidationError} objects to which validation errors are added.
     * @param type The type of the XML element to check (e.g., "DocStruct", "Metadata", "Person").
     * @param definition The expected definition of the element (e.g., "DocStrctType", "MetadataType").
     * @param format The format of the XML document.
     * @param name This value is either "name" or "InternalName" depends on the format
     */
    private void checkElements(Element root, List<RulesetValidationError> errors, String type, String definition, String format, String name) {
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp = xpfac.compile("//Formats//" + format + "//" + type, Filters.element());
        List<Element> Elements = xp.evaluate(root);

        // run through all elements in formats section
        for (Element element : Elements) {
            String formatName = element.getChild(name).getText().trim();
            XPathExpression<Element> xp1 = xpfac.compile("//" + definition + "[Name='" + formatName + "']", Filters.element());

            // If the type is a Person, check if the MetadataType has a type attribute valued with "person"
            if ("Person".equals(type)) {
                XPathExpression<Element> xp2 = xpfac.compile("//MetadataType[@type='person' and Name='" + formatName + "']", Filters.element());
                if (xp1.evaluate(root).size() < 1 && xp2.evaluate(root).size() < 1) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
                                    formatName, format),
                            element.getAttributeValue("lineNumber")));
                }
            }

            // If the type is a Corporate, check if the MetadataType has a type attribute valued with "corporate"
            if ("Corporate".equals(type)) {
                XPathExpression<Element> xp3 = xpfac.compile("//MetadataType[@type='corporate' and Name='" + formatName + "']", Filters.element());
                if (xp1.evaluate(root).size() < 1 && xp3.evaluate(root).size() < 1) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
                                    formatName, format),
                            element.getAttributeValue("lineNumber")));
                }
            }

            // If a value of a Metadata, Group or DocStrct is not defined above throw out an error
            if (!"Person".equals(type) && !"Corporate".equals(type)) {
                if (xp1.evaluate(root).size() < 1) {
                    errors.add(new RulesetValidationError("ERROR",
                            Helper.getTranslation("ruleset_validation_used_but_undefined_" + type.toLowerCase() + "_for_export",
                                    formatName, format),
                            element.getAttributeValue("lineNumber")));
                }
            }
        }
    }
}