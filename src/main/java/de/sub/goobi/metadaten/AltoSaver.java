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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.AltoChange;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class AltoSaver {

    private AltoSaver() {
        // hide implicit public constructor
    }

    private static SAXBuilder sax = XmlTools.getSAXBuilder();
    private static XPathFactory xFactory = XPathFactory.instance();

    public static void saveAltoChanges(Path altoFile, AltoChange[] changes) throws JDOMException, IOException {
        if (changes == null) {
            return;
        }
        Document doc = sax.build(altoFile.toFile());
        Namespace namespace = Namespace.getNamespace("alto", doc.getRootElement().getNamespaceURI());
        boolean writeTags = Arrays.stream(changes).anyMatch(change -> "setNamedEntity".equals(change.getAction()));
        //first remove all tags and tagrefs. Create new Tags element if necessary
        XPathExpression<Element> tagsXPath = xFactory.compile("//alto:Tags", Filters.element(), null, namespace);
        Element tagsElement = tagsXPath.evaluateFirst(doc);
        if (writeTags && tagsElement == null) {
            int layoutIndex = doc.getRootElement().indexOf(doc.getRootElement().getChild("Layout", namespace));
            tagsElement = new Element("Tags", doc.getRootElement().getNamespace());
            doc.getRootElement().addContent(layoutIndex, tagsElement);
        } else if (!writeTags && tagsElement != null) {
            doc.getRootElement().removeContent(tagsElement);
            tagsElement = null;
        }
        if (tagsElement != null) {
            tagsElement.removeChildren("NamedEntityTag", namespace);
        }
        XPathExpression<Element> stringsXPath = xFactory.compile("//alto:String", Filters.element(), null, namespace);
        List<Element> stringElements = stringsXPath.evaluate(doc);
        stringElements.forEach(string -> string.removeAttribute("TAGREFS"));

        int tagCounter = 0;

        for (AltoChange change : changes) {
            Element stringEl = getWordById(doc, namespace, change.getWordId());
            if ("changeContent".equals(change.getAction())) {
                if (stringEl != null) {
                    stringEl.setAttribute("CONTENT", change.getValue());
                }
            } else if ("setNamedEntity".equals(change.getAction())) {
                String tagId = String.format("Tag%d", ++tagCounter);
                Element namedEntityTag = new Element("NamedEntityTag", doc.getRootElement().getNamespace());
                namedEntityTag.setAttribute("ID", tagId);
                if (StringUtils.isNotBlank(change.getEntity().getLabel())) {
                    namedEntityTag.setAttribute("LABEL", change.getEntity().getLabel());
                }
                if (StringUtils.isNotBlank(change.getEntity().getType())) {
                    namedEntityTag.setAttribute("TYPE", change.getEntity().getType());
                }
                if (StringUtils.isNotBlank(change.getEntity().getUri())) {
                    namedEntityTag.setAttribute("URI", change.getEntity().getUri());
                }
                tagsElement.addContent(namedEntityTag);
                change.getWords().stream().map(id -> getWordById(doc, namespace, id)).filter(Objects::nonNull).forEach(ele -> {
                    String tagRefs = Optional.ofNullable(ele.getAttributeValue("TAGREFS")).orElse("").trim();
                    tagRefs = (tagRefs + " " + tagId).trim();
                    ele.setAttribute("TAGREFS", tagRefs);
                });
            } else {
                log.error("Cannot add alto change: Unknown action {}", change.getAction());
            }
        }
        try (
                OutputStream out = StorageProvider.getInstance().newOutputStream(altoFile)) {
            XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
            xmlOut.output(doc, out);
        }
    }

    public static Element getWordById(Document doc, Namespace namespace, String id) {
        String xpath = String.format("//alto:String[@ID='%s']", id);
        XPathExpression<Element> compXpath = xFactory.compile(xpath, Filters.element(), null, namespace);
        return compXpath.evaluateFirst(doc);
    }
}
