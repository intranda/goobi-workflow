package org.goobi.production.properties;

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
 */
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TypeTest {

    @Test
    public void testGetTypeByNameText() {
        assertEquals(Type.TEXT, Type.getTypeByName("text"));
    }

    @Test
    public void testGetTypeByNameTextUpperCase() {
        assertEquals(Type.TEXT, Type.getTypeByName("TEXT"));
    }

    @Test
    public void testGetTypeByNameTextarea() {
        assertEquals(Type.TEXTAREA, Type.getTypeByName("textarea"));
    }

    @Test
    public void testGetTypeByNameList() {
        assertEquals(Type.LIST, Type.getTypeByName("list"));
    }

    @Test
    public void testGetTypeByNameListMultiselect() {
        assertEquals(Type.LISTMULTISELECT, Type.getTypeByName("listmultiselect"));
    }

    @Test
    public void testGetTypeByNameBoolean() {
        assertEquals(Type.BOOLEAN, Type.getTypeByName("boolean"));
    }

    @Test
    public void testGetTypeByNameDate() {
        assertEquals(Type.DATE, Type.getTypeByName("date"));
    }

    @Test
    public void testGetTypeByNameNumber() {
        assertEquals(Type.NUMBER, Type.getTypeByName("number"));
    }

    @Test
    public void testGetTypeByNameLink() {
        assertEquals(Type.LINK, Type.getTypeByName("link"));
    }

    @Test
    public void testGetTypeByNameMetadata() {
        assertEquals(Type.METADATA, Type.getTypeByName("metadata"));
    }

    @Test
    public void testGetTypeByNameHtml() {
        assertEquals(Type.HTML, Type.getTypeByName("html"));
    }

    @Test
    public void testGetTypeByNameVocabularyReference() {
        assertEquals(Type.VOCABULARYREFERENCE, Type.getTypeByName("vocabularyreference"));
    }

    @Test
    public void testGetTypeByNameVocabularyMultiReference() {
        assertEquals(Type.VOCABULARYMULTIREFERENCE, Type.getTypeByName("vocabularymultireference"));
    }

    @Test
    public void testGetTypeByNameUnknownDefaultsToText() {
        assertEquals(Type.TEXT, Type.getTypeByName("unknown"));
    }

    @Test
    public void testGetTypeByNameEmptyDefaultsToText() {
        assertEquals(Type.TEXT, Type.getTypeByName(""));
    }

    @Test
    public void testGetTypeByNameCaseInsensitive() {
        assertEquals(Type.TEXTAREA, Type.getTypeByName("TEXTAREA"));
        assertEquals(Type.BOOLEAN, Type.getTypeByName("Boolean"));
    }

    @Test
    public void testGetNameText() {
        assertEquals("text", Type.TEXT.getName());
    }

    @Test
    public void testGetNameTextarea() {
        assertEquals("textarea", Type.TEXTAREA.getName());
    }

    @Test
    public void testGetNameList() {
        assertEquals("list", Type.LIST.getName());
    }

    @Test
    public void testGetNameBoolean() {
        assertEquals("boolean", Type.BOOLEAN.getName());
    }

    @Test
    public void testEnumValuesCount() {
        assertEquals(12, Type.values().length);
    }
}
