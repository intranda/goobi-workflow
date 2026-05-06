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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;

public class XmlToolsTest {

    @Test
    public void testGetSAXBuilderReturnsNonNull() {
        SAXBuilder builder = XmlTools.getSAXBuilder();
        assertNotNull(builder);
    }

    @Test
    public void testReadDocumentFromStringValidXml() {
        String xml = "<root><child>value</child></root>";
        Document doc = XmlTools.readDocumentFromString(xml);
        assertNotNull(doc);
        assertEquals("root", doc.getRootElement().getName());
        assertEquals("value", doc.getRootElement().getChildText("child"));
    }

    @Test
    public void testReadDocumentFromStringInvalidXmlReturnsNull() {
        String xml = "this is not xml <at all";
        Document doc = XmlTools.readDocumentFromString(xml);
        assertNull(doc);
    }

    @Test
    public void testReadDocumentFromStream() {
        String xml = "<data><item id=\"1\">hello</item></data>";
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        Document doc = XmlTools.readDocumentFromStream(stream);
        assertNotNull(doc);
        assertEquals("data", doc.getRootElement().getName());
        assertEquals("hello", doc.getRootElement().getChildText("item"));
    }

    @Test
    public void testReadDocumentFromStringWithAttributes() {
        String xml = "<root attr=\"val\"><child>text</child></root>";
        Document doc = XmlTools.readDocumentFromString(xml);
        assertNotNull(doc);
        assertEquals("val", doc.getRootElement().getAttributeValue("attr"));
    }

    @Test
    public void testReadDocumentFromStreamInvalidReturnsNull() {
        byte[] badData = "<<bad xml>>".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream stream = new ByteArrayInputStream(badData);
        Document doc = XmlTools.readDocumentFromStream(stream);
        assertNull(doc);
    }
}
