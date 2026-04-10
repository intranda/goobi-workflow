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
package org.goobi.production.enums;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ImportFormatTest {

    @Test
    public void testValuesCount() {
        assertEquals(4, ImportFormat.values().length);
    }

    @Test
    public void testPicaValue() {
        assertEquals("1", ImportFormat.PICA.getValue());
    }

    @Test
    public void testPicaTitle() {
        assertEquals("pica", ImportFormat.PICA.getTitle());
    }

    @Test
    public void testMarc21Value() {
        assertEquals("2", ImportFormat.MARC21.getValue());
    }

    @Test
    public void testMarcXmlValue() {
        assertEquals("3", ImportFormat.MARCXML.getValue());
    }

    @Test
    public void testDcValue() {
        assertEquals("4", ImportFormat.DC.getValue());
    }

    @Test
    public void testGetTypeFromValuePica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromValue("1"));
    }

    @Test
    public void testGetTypeFromValueMarc21() {
        assertEquals(ImportFormat.MARC21, ImportFormat.getTypeFromValue("2"));
    }

    @Test
    public void testGetTypeFromValueMarcXml() {
        assertEquals(ImportFormat.MARCXML, ImportFormat.getTypeFromValue("3"));
    }

    @Test
    public void testGetTypeFromValueDc() {
        assertEquals(ImportFormat.DC, ImportFormat.getTypeFromValue("4"));
    }

    @Test
    public void testGetTypeFromValueUnknownReturnsPica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromValue("unknown"));
    }

    @Test
    public void testGetTypeFromValueNullReturnsPica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromValue(null));
    }

    @Test
    public void testGetTypeFromTitlePica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromTitle("pica"));
    }

    @Test
    public void testGetTypeFromTitleMarc21() {
        assertEquals(ImportFormat.MARC21, ImportFormat.getTypeFromTitle("marc21"));
    }

    @Test
    public void testGetTypeFromTitleUnknownReturnsPica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromTitle("unknown"));
    }

    @Test
    public void testGetTypeFromTitleNullReturnsPica() {
        assertEquals(ImportFormat.PICA, ImportFormat.getTypeFromTitle(null));
    }
}
