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

import org.junit.jupiter.api.Test;

public class ConfigOpacCatalogueBeautifierElementTest {

    @Test
    public void testConstructor() {
        ConfigOpacCatalogueBeautifierElement el = new ConfigOpacCatalogueBeautifierElement("100", "a", "myvalue");
        assertNotNull(el);
        assertEquals("100", el.getTag());
        assertEquals("a", el.getSubtag());
        assertEquals("myvalue", el.getValue());
    }

    @Test
    public void testSetters() {
        ConfigOpacCatalogueBeautifierElement el = new ConfigOpacCatalogueBeautifierElement("", "", "");
        el.setTag("200");
        el.setSubtag("b");
        el.setValue("updated");
        assertEquals("200", el.getTag());
        assertEquals("b", el.getSubtag());
        assertEquals("updated", el.getValue());
    }

    @Test
    public void testToString() {
        ConfigOpacCatalogueBeautifierElement el = new ConfigOpacCatalogueBeautifierElement("100", "a", "myvalue");
        assertEquals("100 - a : myvalue", el.toString());
    }

}
