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
package org.goobi.api.display.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DisplayTypeTest {

    @Test
    public void testGetByTitleKnownValues() {
        assertEquals(DisplayType.input, DisplayType.getByTitle("input"));
        assertEquals(DisplayType.select, DisplayType.getByTitle("select"));
        assertEquals(DisplayType.select1, DisplayType.getByTitle("select1"));
        assertEquals(DisplayType.textarea, DisplayType.getByTitle("textarea"));
        assertEquals(DisplayType.readonly, DisplayType.getByTitle("readonly"));
        assertEquals(DisplayType.gnd, DisplayType.getByTitle("gnd"));
        assertEquals(DisplayType.person, DisplayType.getByTitle("person"));
        assertEquals(DisplayType.geonames, DisplayType.getByTitle("geonames"));
        assertEquals(DisplayType.htmlInput, DisplayType.getByTitle("htmlInput"));
        assertEquals(DisplayType.generate, DisplayType.getByTitle("generate"));
    }

    @Test
    public void testGetByTitleUnknownReturnsInput() {
        assertEquals(DisplayType.input, DisplayType.getByTitle("doesnotexist"));
    }

    @Test
    public void testGetByTitleNullReturnsInput() {
        assertEquals(DisplayType.input, DisplayType.getByTitle(null));
    }

    @Test
    public void testEnumValues() {
        assertEquals(19, DisplayType.values().length);
    }
}
