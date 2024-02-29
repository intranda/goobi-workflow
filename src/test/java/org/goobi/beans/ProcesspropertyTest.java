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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.enums.PropertyType;

public class ProcesspropertyTest extends AbstractTest {

    @Test
    public void testIstObligatorisch() {
        Processproperty property = new Processproperty();

        // tests whether 'null' is replaced by 'false'
        property.setIstObligatorisch(null);
        assertEquals(property.isIstObligatorisch(), false);

        // tests whether 'false' is returned correctly
        property.setIstObligatorisch(false);
        assertEquals(property.isIstObligatorisch(), false);

        // tests whether 'true' is returned correctly
        property.setIstObligatorisch(true);
        assertEquals(property.isIstObligatorisch(), true);
    }

    @Test
    public void testType() {
        Processproperty property = new Processproperty();

        // test that property is set and returned correctly
        property.setType(PropertyType.BOOLEAN);
        assertEquals(property.getType(), PropertyType.BOOLEAN);
    }

    @Test
    public void testValueList() {
        Processproperty property = new Processproperty();

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
        Processproperty property = new Processproperty();

        // This integer is outsourced because assertEquals() requires object type safety
        Integer zero = Integer.valueOf(0);
        assertEquals(property.getContainer(), zero);

        // Test whether null is replaced with 0
        property.setContainer(null);
        assertEquals(property.getContainer(), zero);

        // Test whether normal numbers work
        property.setContainer(42);
        assertEquals(property.getContainer(), Integer.valueOf(42));
    }

    @Test
    public void testGetNormalizedTitle() {
        Processproperty property = new Processproperty();
        property.setTitel("\tMy Title\t");
        assertEquals("My_Title", property.getNormalizedTitle());
    }

    @Test
    public void testGetNormalizedValue() {
        Processproperty property = new Processproperty();
        property.setTitel("\tMy Value\t");
        assertEquals("My_Value", property.getNormalizedTitle());
    }

    @Test
    public void testHashCode() {
        Processproperty property1 = new Processproperty();
        property1.setId(1);
        Processproperty property2 = new Processproperty();
        property2.setId(1);
        Processproperty property3 = new Processproperty();
        property3.setId(2);
        // test that property 1 has the same hash code as itself
        assertEquals(property1.hashCode(), property1.hashCode());
        // test that property 1 has the same hash code as property 2
        assertEquals(property1.hashCode(), property2.hashCode());
        // test that property 1 has not the same hash code as property 3
        assertNotEquals(property1.hashCode(), property3.hashCode());
    }

    @Test
    public void testEquals() {
        Processproperty property1 = new Processproperty();
        property1.setId(1);
        property1.setContainer(1);
        property1.setCreationDate(new Date());
        property1.setType(PropertyType.BOOLEAN);
        property1.setProcessId(1);
        property1.setTitel("Title");
        property1.setWert("Value");
        Processproperty property2 = new Processproperty();
        property2.setId(2);
        Processproperty property3 = new Processproperty();
        property3.setContainer(2);
        Processproperty property4 = new Processproperty();
        property4.setCreationDate(null);
        Processproperty property5 = new Processproperty();
        property5.setType(PropertyType.NUMBER);
        Processproperty property6 = new Processproperty();
        property6.setProcessId(2);
        Processproperty property7 = new Processproperty();
        property7.setTitel("Title 2");
        Processproperty property8 = new Processproperty();
        property8.setWert("Value 2");
        // test that property1 has the same hash code as itself
        assertEquals(property1.hashCode(), property1.hashCode());
        // test that property1 has a different hash code to all other properties
        assertNotEquals(property1.hashCode(), property2.hashCode());// different id
        assertNotEquals(property1.hashCode(), property3.hashCode());// different container
        assertNotEquals(property1.hashCode(), property4.hashCode());// different creation date
        assertNotEquals(property1.hashCode(), property5.hashCode());// different type
        assertNotEquals(property1.hashCode(), property6.hashCode());// different process id
        assertNotEquals(property1.hashCode(), property7.hashCode());// different title
        assertNotEquals(property1.hashCode(), property8.hashCode());// different value
    }
}
