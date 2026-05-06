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
package org.goobi.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.junit.jupiter.api.Test;

public class SimpleAltoTest {

    @Test
    public void testDefaultConstructorCreatesEmptyLists() {
        SimpleAlto alto = new SimpleAlto();
        assertNotNull(alto.getLines());
        assertTrue(alto.getLines().isEmpty());
        assertNotNull(alto.getNamedEntities());
        assertTrue(alto.getNamedEntities().isEmpty());
    }

    @Test
    public void testErrorMessageConstructorCreatesSingleLine() {
        SimpleAlto alto = new SimpleAlto("An error occurred");
        assertEquals(1, alto.getLines().size());
    }

    @Test
    public void testErrorMessageConstructorSetsWordValue() {
        SimpleAlto alto = new SimpleAlto("Something went wrong");
        SimpleAltoLine line = alto.getLines().get(0);
        assertEquals(1, line.getWords().size());
        assertEquals("Something went wrong", line.getWords().get(0).getValue());
    }

    @Test
    public void testParseNamedEntityTagRefsWithEmptyTagrefsIsNoop() {
        SimpleAlto alto = new SimpleAlto();
        Element element = new Element("String");
        element.setAttribute("TAGREFS", "");
        element.setAttribute("ID", "W1");
        // Should not throw
        SimpleAlto.parseNamedEntityTagRefs(alto, element);
        assertTrue(alto.getNamedEntityMap().isEmpty());
    }

    @Test
    public void testParseNamedEntityTagRefsAddsWordIdToExistingKey() {
        SimpleAlto alto = new SimpleAlto();
        // Pre-populate the map so the key exists (otherwise the new list is discarded)
        List<String> wordList = new ArrayList<>();
        alto.getNamedEntityMap().put("TAG1", wordList);

        Element element = new Element("String");
        element.setAttribute("TAGREFS", "TAG1");
        element.setAttribute("ID", "W42");
        SimpleAlto.parseNamedEntityTagRefs(alto, element);

        assertTrue(alto.getNamedEntityMap().get("TAG1").contains("W42"));
    }
}
