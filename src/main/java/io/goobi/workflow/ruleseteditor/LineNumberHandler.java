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

import java.util.Stack;

import org.jdom2.Document;
import org.jdom2.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

public class LineNumberHandler extends DefaultHandler {

    private Locator locator;
    private Document document;
    private Stack<Element> elementStack = new Stack<>();
    private StringBuilder textBuffer = new StringBuilder();

    @Override
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    public void startDocument() {
        document = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        flushText();

        Element element = new Element(qName);

        if (locator != null) {
            element.setAttribute("goobi_lineNumber", String.valueOf(locator.getLineNumber()));
        }

        for (int i = 0; i < attributes.getLength(); i++) {
            element.setAttribute(attributes.getQName(i), attributes.getValue(i));
        }

        if (elementStack.isEmpty()) {
            document = new Document(element);
        } else {
            elementStack.peek().addContent(element);
        }

        elementStack.push(element);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        flushText();
        elementStack.pop();
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        textBuffer.append(ch, start, length);
    }

    public Document getDocument() {
        return document;
    }

    private void flushText() {
        if (textBuffer.length() > 0) {
            Element current = elementStack.peek();
            current.addContent(textBuffer.toString());
            textBuffer.setLength(0);
        }
    }
}
