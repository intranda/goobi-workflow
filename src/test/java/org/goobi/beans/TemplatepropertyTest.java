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

package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.PropertyType;

public class TemplatepropertyTest extends AbstractTest {

    @Test
    public void testIstObligatorisch() {
        Templateproperty property = new Templateproperty();

        // tests whether 'false' is returned correctly
        property.setRequired(false);
        assertEquals(property.isRequired(), false);

        // tests whether 'true' is returned correctly
        property.setRequired(true);
        assertEquals(property.isRequired(), true);
    }

    @Test
    public void testType() {
        Templateproperty property = new Templateproperty();

        // test that property is set and returned correctly
        property.setType(PropertyType.BOOLEAN);
        assertEquals(property.getType(), PropertyType.BOOLEAN);
    }

    @Test
    public void testValueList() {
        Templateproperty property = new Templateproperty();

        // null should be replaced with an empty list (by the getter)
        property.setValueList(null);
        assertNotNull(property.getValueList());

        // test that example list is returned as the same object
        List<String> exampleList = new ArrayList<>();
        exampleList.add("Test property");
        property.setValueList(exampleList);
        assertNotNull(property.getValueList());
        assertSame(property.getValueList(), exampleList);

    }

    @Test
    public void testContainer() {
        Templateproperty property = new Templateproperty();

        assertEquals(property.getContainer(), "0");

        // Test whether null is replaced with 0
        property.setContainer(null);
        assertEquals(property.getContainer(), "0");

        // Test whether normal numbers work
        property.setContainer("42");
        assertEquals("42", property.getContainer());
    }

    @Test
    public void testGetNormalizedTitle() {
        Templateproperty property = new Templateproperty();
        property.setPropertyName("\tMy Title\t");
        assertEquals("My_Title", property.getNormalizedTitle());
    }

    @Test
    public void testGetNormalizedValue() {
        Templateproperty property = new Templateproperty();
        property.setPropertyName("\tMy Value\t");
        assertEquals("My_Value", property.getNormalizedTitle());
    }
}
