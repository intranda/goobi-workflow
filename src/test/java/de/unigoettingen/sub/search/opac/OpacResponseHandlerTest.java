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
package de.unigoettingen.sub.search.opac;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class OpacResponseHandlerTest {

    private static final String FULL_RESPONSE_XML =
            "<?xml version=\"1.0\"?>"
                    + "<OPAC>"
                    + "<SESSIONVAR name=\"SID\">session123</SESSIONVAR>"
                    + "<SESSIONVAR name=\"SET\">myResultSet</SESSIONVAR>"
                    + "<SET hits=\"3\"/>"
                    + "<SHORTTITLE PPN=\"ppn001\">Title One</SHORTTITLE>"
                    + "<SHORTTITLE PPN=\"ppn002\">Title Two</SHORTTITLE>"
                    + "</OPAC>";
    private static final String COOKIE_RESPONSE_XML = "<?xml version=\"1.0\"?>"
            + "<OPAC>"
            + "<SESSIONVAR name=\"SID\">sessionABC</SESSIONVAR>"
            + "<SESSIONVAR name=\"COOKIE\">cookieXYZ</SESSIONVAR>"
            + "</OPAC>";

    private static final String ILLEGAL_QUERY_XML =
            "<?xml version=\"1.0\"?><OPAC><RESULT error=\"ILLEGAL\"/></OPAC>";

    private OpacResponseHandler parse(String xml) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        OpacResponseHandler handler = new OpacResponseHandler();
        reader.setContentHandler(handler);
        reader.parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        return handler;
    }

    @Test
    public void testConstructorDefaults() {
        OpacResponseHandler handler = new OpacResponseHandler();
        assertNotNull(handler);
        assertEquals(0, handler.getNumberOfHits());
        assertEquals("", handler.getSet());
        assertTrue(handler.getOpacResponseItemPpns().isEmpty());
        assertTrue(handler.getOpacResponseItemTitles().isEmpty());
    }

    @Test
    public void testParseNumberOfHits() throws Exception {
        OpacResponseHandler handler = parse(FULL_RESPONSE_XML);
        assertEquals(3, handler.getNumberOfHits());
    }

    @Test
    public void testParseSetSessionVariable() throws Exception {
        OpacResponseHandler handler = parse(FULL_RESPONSE_XML);
        assertEquals("myResultSet", handler.getSet());
    }

    @Test
    public void testParsePpns() throws Exception {
        OpacResponseHandler handler = parse(FULL_RESPONSE_XML);
        assertEquals(2, handler.getOpacResponseItemPpns().size());
        assertEquals("ppn001", handler.getOpacResponseItemPpns().get(0));
        assertEquals("ppn002", handler.getOpacResponseItemPpns().get(1));
    }

    @Test
    public void testParseTitles() throws Exception {
        OpacResponseHandler handler = parse(FULL_RESPONSE_XML);
        assertEquals(2, handler.getOpacResponseItemTitles().size());
        assertEquals("Title One", handler.getOpacResponseItemTitles().get(0));
        assertEquals("Title Two", handler.getOpacResponseItemTitles().get(1));
    }

    @Test
    public void testGetSessionIdWithoutCookie() throws Exception {
        OpacResponseHandler handler = parse(FULL_RESPONSE_XML);
        String sessionId = handler.getSessionId("UTF-8");
        assertEquals("session123", sessionId);
    }

    @Test
    public void testGetSessionIdWithCookie() throws Exception {
        OpacResponseHandler handler = parse(COOKIE_RESPONSE_XML);
        String sessionId = handler.getSessionId("UTF-8");
        assertTrue(sessionId.contains("sessionABC"));
        assertTrue(sessionId.contains("COOKIE="));
        assertTrue(sessionId.contains("cookieXYZ"));
    }

    @Test
    public void testIllegalQueryThrowsSaxException() {
        try {
            parse(ILLEGAL_QUERY_XML);
        } catch (SAXException e) {
            assertTrue(e.getException() instanceof IllegalQueryException);
            return;
            //CHECKSTYLE:OFF
        } catch (Exception e) {
            // unexpected exception type
        }
        //CHECKSTYLE:ON
        // If we reach here without catching SAXException, fail
        assertTrue(false, "Expected SAXException with IllegalQueryException cause");
    }

}
