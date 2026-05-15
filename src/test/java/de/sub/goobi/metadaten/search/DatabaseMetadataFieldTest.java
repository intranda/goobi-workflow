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
package de.sub.goobi.metadaten.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class DatabaseMetadataFieldTest {

    @Test
    public void testNoArgsConstructor() {
        DatabaseMetadataField field = new DatabaseMetadataField();
        assertNull(field.getMetadataName());
        assertNull(field.getMetadataValue());
        assertNull(field.getAuthorityName());
        assertNull(field.getAuthorityUri());
        assertNull(field.getAuthorityValue());
    }

    @Test
    public void testAllArgsConstructor() {
        DatabaseMetadataField field = new DatabaseMetadataField("TitleDocMain", "Test Title", "GND", "https://d-nb.info/gnd/", "12345");
        assertEquals("TitleDocMain", field.getMetadataName());
        assertEquals("Test Title", field.getMetadataValue());
        assertEquals("GND", field.getAuthorityName());
        assertEquals("https://d-nb.info/gnd/", field.getAuthorityUri());
        assertEquals("12345", field.getAuthorityValue());
    }

    @Test
    public void testSetters() {
        DatabaseMetadataField field = new DatabaseMetadataField();
        field.setMetadataName("Author");
        field.setMetadataValue("John Doe");
        field.setAuthorityName("GND");
        field.setAuthorityUri("https://d-nb.info/gnd/");
        field.setAuthorityValue("67890");

        assertEquals("Author", field.getMetadataName());
        assertEquals("John Doe", field.getMetadataValue());
        assertEquals("GND", field.getAuthorityName());
        assertEquals("https://d-nb.info/gnd/", field.getAuthorityUri());
        assertEquals("67890", field.getAuthorityValue());
    }
}
