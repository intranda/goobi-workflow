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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class JsonFieldTest {

    @Test
    public void testNoArgsConstructorHasNullNameAndValue() {
        JsonField f = new JsonField();
        assertNull(f.getName());
        assertNull(f.getValue());
    }

    @Test
    public void testNoArgsConstructorDefaultValueTypeIsString() {
        JsonField f = new JsonField();
        assertEquals(JsonField.ValueType.STRING, f.getValueType());
    }

    @Test
    public void testAllArgsConstructor() {
        JsonField f = new JsonField("myField", "42", JsonField.ValueType.INTEGER);
        assertEquals("myField", f.getName());
        assertEquals("42", f.getValue());
        assertEquals(JsonField.ValueType.INTEGER, f.getValueType());
    }

    @Test
    public void testSettersAndGetters() {
        JsonField f = new JsonField();
        f.setName("score");
        f.setValue("3.14");
        f.setValueType(JsonField.ValueType.FLOAT);
        assertEquals("score", f.getName());
        assertEquals("3.14", f.getValue());
        assertEquals(JsonField.ValueType.FLOAT, f.getValueType());
    }

    @Test
    public void testGetValueTypesReturnsAllEnumConstants() {
        JsonField f = new JsonField();
        JsonField.ValueType[] types = f.getValueTypes();
        assertEquals(4, types.length);
    }

    @Test
    public void testValueTypeEnumValues() {
        assertEquals(JsonField.ValueType.STRING, JsonField.ValueType.valueOf("STRING"));
        assertEquals(JsonField.ValueType.INTEGER, JsonField.ValueType.valueOf("INTEGER"));
        assertEquals(JsonField.ValueType.FLOAT, JsonField.ValueType.valueOf("FLOAT"));
        assertEquals(JsonField.ValueType.BOOL, JsonField.ValueType.valueOf("BOOL"));
    }
}
