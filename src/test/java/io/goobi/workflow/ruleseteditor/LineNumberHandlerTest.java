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

package io.goobi.workflow.ruleseteditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

@ExtendWith(MockitoExtension.class)
public class LineNumberHandlerTest {

    private LineNumberHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new LineNumberHandler();
    }

    @Test
    public void testStartDocument() {
        handler.startDocument();
        assertNull(handler.getDocument());
    }

    @Test
    public void testStartAndEndElementSingleElementWithLineNumber() {
        Locator locator = Mockito.mock(Locator.class);
        Mockito.when(locator.getLineNumber()).thenReturn(42);

        handler.setDocumentLocator(locator);
        handler.startDocument();

        Attributes attributes = Mockito.mock(Attributes.class);
        Mockito.when(attributes.getLength()).thenReturn(0);

        handler.startElement("", "", "root", attributes);
        handler.endElement("", "", "root");

        Document doc = handler.getDocument();
        assertNotNull(doc);
        Element root = doc.getRootElement();
        assertEquals("root", root.getName());
        assertEquals("42", root.getAttributeValue("goobi_lineNumber"));
    }

    @Test
    public void testFlushTextAppendsToCurrentElement() {
        Locator locator = Mockito.mock(Locator.class);
        Mockito.when(locator.getLineNumber()).thenReturn(3);

        handler.setDocumentLocator(locator);
        handler.startDocument();

        Attributes attrs = Mockito.mock(Attributes.class);
        Mockito.when(attrs.getLength()).thenReturn(0);

        handler.startElement("", "", "test", attrs);
        handler.characters("some text".toCharArray(), 0, "some text".length());
        handler.endElement("", "", "test");

        Element root = handler.getDocument().getRootElement();
        assertEquals("some text", root.getText());
    }

    @Test
    public void testEmptyDocument() {
        handler.startDocument();
        assertNull(handler.getDocument());
    }
}
