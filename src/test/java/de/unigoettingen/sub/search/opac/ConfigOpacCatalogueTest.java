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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.sub.goobi.AbstractTest;

public class ConfigOpacCatalogueTest extends AbstractTest {

    private ConfigOpacCatalogue catalogue;
    private Map<String, String> searchFields;

    @Before
    public void setUp() {
        searchFields = new HashMap<>();
        searchFields.put("ISBN", "7");
        searchFields.put("ISSN", "8");

        catalogue = new ConfigOpacCatalogue("TestCat", "A test catalogue", "catalogue.example.com",
                "db1", "iktlist.xml", 8080, new ArrayList<>(), "PICA", searchFields);
    }

    @Test
    public void testShortConstructorGetters() {
        assertNotNull(catalogue);
        assertEquals("TestCat", catalogue.getTitle());
        assertEquals("A test catalogue", catalogue.getDescription());
        assertEquals("catalogue.example.com", catalogue.getAddress());
        assertEquals("db1", catalogue.getDatabase());
        assertEquals("iktlist.xml", catalogue.getIktlist());
        assertEquals(8080, catalogue.getPort());
        assertEquals("PICA", catalogue.getOpacType());
        assertEquals(searchFields, catalogue.getSearchFields());
        assertNull(catalogue.getCbs());
        assertEquals("iso-8859-1", catalogue.getCharset());
        assertEquals("http://", catalogue.getProtocol());
    }

    @Test
    public void testFullConstructorGetters() {
        ConfigOpacCatalogue full = new ConfigOpacCatalogue("KXP", "K10plus", "kxp.k10plus.de",
                "2.1", "IKTLIST-GBV.xml", 80, "utf-8", "&UCNF=NFC",
                new ArrayList<>(), "PICA", "https://", searchFields);

        assertEquals("utf-8", full.getCharset());
        assertEquals("&UCNF=NFC", full.getCbs());
        assertEquals("https://", full.getProtocol());
        assertEquals("kxp.k10plus.de", full.getAddress());
        assertEquals(80, full.getPort());
    }

    @Test
    public void testSetTitle() {
        catalogue.setTitle("NewTitle");
        assertEquals("NewTitle", catalogue.getTitle());
    }

    @Test
    public void testSetOpacPlugin() {
        assertNull(catalogue.getOpacPlugin());
        catalogue.setOpacPlugin(null);
        assertNull(catalogue.getOpacPlugin());
    }

    @Test
    public void testExecuteBeautifierWithEmptyBeautifyList() throws Exception {
        // With an empty beautifySetList the node passes through unchanged
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);

        Node result = catalogue.executeBeautifier(root);
        assertNotNull(result);
    }

}
