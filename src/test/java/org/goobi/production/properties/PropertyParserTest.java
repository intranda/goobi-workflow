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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class PropertyParserTest extends AbstractTest {

    @Test
    public void testGetInstanceReturnsNotNull() {
        assertNotNull(PropertyParser.getInstance());
    }

    @Test
    public void testGetInstanceReturnsSameInstance() {
        PropertyParser first = PropertyParser.getInstance();
        PropertyParser second = PropertyParser.getInstance();
        assertSame(first, second);
    }

    @Test
    public void testGetEscapedPropertySimpleString() {
        assertEquals("'simple'", PropertyParser.getEscapedProperty("simple"));
    }

    @Test
    public void testGetEscapedPropertyEmptyString() {
        assertEquals("''", PropertyParser.getEscapedProperty(""));
    }

    @Test
    public void testGetEscapedPropertyWithSingleQuote() {
        assertEquals("concat('O',\"'\",'Brien')", PropertyParser.getEscapedProperty("O'Brien"));
    }

    @Test
    public void testGetEscapedPropertyWithMultipleSingleQuotes() {
        // "it's a test" -> concat('it',\"'\",\"s a test\")
        // = concat('it',"'",'s a test')
        assertEquals("concat('it',\"'\",'s a test')", PropertyParser.getEscapedProperty("it's a test"));
    }

    @Test
    public void testGetEscapedPropertyOnlySingleQuote() {
        assertEquals("concat('',\"'\",'')", PropertyParser.getEscapedProperty("'"));
    }

    @Test
    public void testGetEscapedPropertyWithSpecialXPathCharacters() {
        // chars other than single quote are not escaped
        assertEquals("'hello world'", PropertyParser.getEscapedProperty("hello world"));
    }
}
