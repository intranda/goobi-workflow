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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

public class LineNumberHandlerTest {

    private LineNumberHandler handler;

    @Before
    public void setUp() {
        handler = new LineNumberHandler();
    }

    @Test
    public void testStartDocument() {
        handler.startDocument();
        assertNull(handler.getDocument());
    }

    @Test
    public void testStartAndEndElement_singleElementWithLineNumber() {
        Locator locator = createMock(Locator.class);
        expect(locator.getLineNumber()).andReturn(42).anyTimes();
        replay(locator);

        handler.setDocumentLocator(locator);
        handler.startDocument();

        Attributes attributes = createMock(Attributes.class);
        expect(attributes.getLength()).andReturn(0).anyTimes();
        replay(attributes);

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
        Locator locator = createMock(Locator.class);
        expect(locator.getLineNumber()).andReturn(3).once();
        replay(locator);

        handler.setDocumentLocator(locator);
        handler.startDocument();

        Attributes attrs = createMock(Attributes.class);
        expect(attrs.getLength()).andReturn(0).anyTimes();
        replay(attrs);

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
