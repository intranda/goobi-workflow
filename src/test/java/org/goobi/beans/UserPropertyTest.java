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

public class UserPropertyTest extends AbstractTest {

    @Test
    public void testIstObligatorisch() {
        UserProperty property = new UserProperty();

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
        UserProperty property = new UserProperty();

        // test that property is set and returned correctly
        property.setType(PropertyType.BOOLEAN);
        assertEquals(property.getType(), PropertyType.BOOLEAN);
    }

    @Test
    public void testValueList() {
        UserProperty property = new UserProperty();

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
    public void testGetNormalizedTitle() {
        UserProperty property = new UserProperty();
        property.setTitel("\tMy Title\t");
        assertEquals("My_Title", property.getNormalizedTitle());
    }

    @Test
    public void testGetNormalizedValue() {
        UserProperty property = new UserProperty();
        property.setTitel("\tMy Value\t");
        assertEquals("My_Value", property.getNormalizedTitle());
    }
}
