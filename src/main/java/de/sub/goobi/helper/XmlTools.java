package de.sub.goobi.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Path;

import org.jdom2.Document;
import org.jdom2.JDOMException;

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

import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class XmlTools {

    private XmlTools() {
        // private constructor to overwrite implicit one
    }

    public static SAXBuilder getSAXBuilder() {
        SAXBuilder builder = new SAXBuilder();
        // Disable access to external entities
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return builder;
    }

    public static Document readDocumentFromFile(Path path) {
        SAXBuilder builder = getSAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(path.toFile());
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
        return doc;
    }

    public static Document readDocumentFromString(String content) {
        SAXBuilder builder = getSAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(new StringReader(content));
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
        return doc;
    }

    public static Document readDocumentFromStream(InputStream stream) {
        SAXBuilder builder = getSAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(stream);
        } catch (JDOMException | IOException e) {
            log.error(e);
        }
        return doc;
    }

    public static void saveDocument(Document document, Path path) {
        try (OutputStream out = StorageProvider.getInstance().newOutputStream(path)) {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            xmlOutputter.output(document, out);
        } catch (IOException e) {
            log.error(e);
        }
    }

}
