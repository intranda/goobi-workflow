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

import java.util.List;

import org.jdom2.Element;

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
            if (error.getErrorType() == RulesetValidationError.ErrorType.VALIDATE_FORMATS
                    && ("PicaPlus".equals(error.getElement().getParentElement().getName())
                            || "Marc".equals(error.getElement().getParentElement().getName()))
                    && ("Person".equals(error.getElement().getName()) || "Corporate".equals(error.getElement().getName()))) {
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