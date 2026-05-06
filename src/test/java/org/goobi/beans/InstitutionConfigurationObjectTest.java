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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InstitutionConfigurationObjectTest {

    private InstitutionConfigurationObject obj;

    @BeforeEach
    public void setUp() {
        obj = new InstitutionConfigurationObject();
    }

    @Test
    public void testDefaultIdIsNull() {
        assertNull(obj.getId());
    }

    @Test
    public void testSetAndGetId() {
        obj.setId(42);
        assertEquals(Integer.valueOf(42), obj.getId());
    }

    @Test
    public void testSetAndGetInstitutionId() {
        obj.setInstitution_id(7);
        assertEquals(Integer.valueOf(7), obj.getInstitution_id());
    }

    @Test
    public void testSetAndGetObjectId() {
        obj.setObject_id(99);
        assertEquals(Integer.valueOf(99), obj.getObject_id());
    }

    @Test
    public void testSetAndGetObjectType() {
        obj.setObject_type("ruleset");
        assertEquals("ruleset", obj.getObject_type());
    }

    @Test
    public void testSetAndGetObjectName() {
        obj.setObject_name("My Ruleset");
        assertEquals("My Ruleset", obj.getObject_name());
    }

    @Test
    public void testDefaultSelectedIsFalse() {
        assertFalse(obj.isSelected());
    }

    @Test
    public void testSetSelected() {
        obj.setSelected(true);
        assertTrue(obj.isSelected());
    }
}
