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
package de.sub.goobi.helper.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ManipulationTypeTest {

    @Test
    public void testValuesCount() {
        assertEquals(5, ManipulationType.values().length);
    }

    @Test
    public void testValueOfNormal() {
        assertNotNull(ManipulationType.valueOf("NORMAL"));
    }

    @Test
    public void testValueOfCamelCase() {
        assertNotNull(ManipulationType.valueOf("CAMEL_CASE"));
    }

    @Test
    public void testValueOfCamelCaseLengthLimited() {
        assertNotNull(ManipulationType.valueOf("CAMEL_CASE_LENGTH_LIMITED"));
    }

    @Test
    public void testValueOfAfterLastSeparator() {
        assertNotNull(ManipulationType.valueOf("AFTER_LAST_SEPARATOR"));
    }

    @Test
    public void testValueOfBeforeFirstSeparator() {
        assertNotNull(ManipulationType.valueOf("BEFORE_FIRST_SEPARATOR"));
    }

    @Test
    public void testNameNormal() {
        assertEquals("NORMAL", ManipulationType.NORMAL.name());
    }

    @Test
    public void testNameCamelCase() {
        assertEquals("CAMEL_CASE", ManipulationType.CAMEL_CASE.name());
    }

    @Test
    public void testNameCamelCaseLengthLimited() {
        assertEquals("CAMEL_CASE_LENGTH_LIMITED", ManipulationType.CAMEL_CASE_LENGTH_LIMITED.name());
    }

    @Test
    public void testNameAfterLastSeparator() {
        assertEquals("AFTER_LAST_SEPARATOR", ManipulationType.AFTER_LAST_SEPARATOR.name());
    }

    @Test
    public void testNameBeforeFirstSeparator() {
        assertEquals("BEFORE_FIRST_SEPARATOR", ManipulationType.BEFORE_FIRST_SEPARATOR.name());
    }
}
