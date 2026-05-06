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
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ConverterTest {

    @Test
    public void testConstructorWithNullThrowsNPE() {
        assertThrows(NullPointerException.class, () -> {
            new Converter(null);
        });
    }

    @Test
    public void testConstructorWithValidObjectSucceeds() {
        assertNotNull(new Converter(42));
    }

    @Test
    public void testGetIntegerFromInteger() {
        assertEquals(Integer.valueOf(7), new Converter(7).getInteger());
    }

    @Test
    public void testGetIntegerFromDouble() {
        assertEquals(Integer.valueOf(3), new Converter(3.9).getInteger());
    }

    @Test
    public void testGetIntegerFromString() {
        assertEquals(Integer.valueOf(123), new Converter("123").getInteger());
    }

    @Test
    public void testGetIntegerFromLong() {
        assertEquals(Integer.valueOf(99), new Converter(99L).getInteger());
    }

    @Test
    public void testGetIntegerFromUnknownTypeThrows() {
        assertThrows(NumberFormatException.class, () -> {
            new Converter(new Object()).getInteger();
        });
    }

    @Test
    public void testGetDoubleFromInteger() {
        assertEquals(Double.valueOf(5.0), new Converter(5).getDouble());
    }

    @Test
    public void testGetDoubleFromDouble() {
        assertEquals(Double.valueOf(2.5), new Converter(2.5).getDouble());
    }

    @Test
    public void testGetDoubleFromString() {
        assertEquals(Double.valueOf(1.5), new Converter("1.5").getDouble());
    }

    @Test
    public void testGetDoubleFromLong() {
        assertEquals(Double.valueOf(1000.0), new Converter(1000L).getDouble());
    }

    @Test
    public void testGetDoubleFromUnknownTypeThrows() {
        assertThrows(NumberFormatException.class, () -> {
            new Converter(new Object()).getDouble();
        });
    }

    @Test
    public void testGetStringFromString() {
        assertEquals("hello", new Converter("hello").getString());
    }

    @Test
    public void testGetStringFromInteger() {
        assertEquals("42", new Converter(42).getString());
    }

    @Test
    public void testGetGBFromLong() {
        long oneGB = 1024L * 1024 * 1024;
        assertEquals(Double.valueOf(1.0), new Converter(oneGB).getGB());
    }

    @Test
    public void testGetGBFromInteger() {
        int bytes = 1024 * 1024 * 1024;
        assertEquals(Double.valueOf(1.0), new Converter(bytes).getGB());
    }
}
