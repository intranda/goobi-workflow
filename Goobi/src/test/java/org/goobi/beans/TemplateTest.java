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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class TemplateTest extends AbstractTest {

    private static Template template1;
    private static Template template2;
    private static Template template3;

    @BeforeClass
    public static void prepareTemplates() {
        // template1 and template2 are equal, template3 differs
        TemplateTest.template1 = new Template();
        TemplateTest.template1.setId(1);
        TemplateTest.template1.setHerkunft("Source 1");
        TemplateTest.template2 = new Template();
        TemplateTest.template2.setId(1);
        TemplateTest.template2.setHerkunft("Source 1");
        TemplateTest.template3 = new Template();
        TemplateTest.template3.setId(2);
        TemplateTest.template3.setHerkunft("Source 2");
    }

    @Test
    public void testEigenschaften() {

        // Create some test properties
        Templateproperty property1 = new Templateproperty();
        Templateproperty property2 = new Templateproperty();
        List<Templateproperty> propertyList = new ArrayList<>();
        propertyList.add(property1);
        propertyList.add(property2);

        // Create a template with property list
        Template template = new Template();
        template.setEigenschaften(propertyList);

        // Check that the returned list contains the same elements
        List<Templateproperty> returnedList = template.getEigenschaftenList();
        assertNotNull(returnedList);
        assertEquals(template.getEigenschaftenSize(), 2);
        assertTrue(returnedList.contains(property1));
        assertTrue(returnedList.contains(property2));
    }

    @Test
    public void testHashCode() {
        // hashCode() tests all properties
        assertEquals(TemplateTest.template1.hashCode(), TemplateTest.template1.hashCode());
        assertEquals(TemplateTest.template1.hashCode(), TemplateTest.template2.hashCode());
        assertNotEquals(TemplateTest.template1.hashCode(), TemplateTest.template3.hashCode());
    }

    @Test
    public void testEquals() {
        // The equals() method only checks the id and the 'herkunft' value
        // Check that template1 and template2 are equal, but template3 differs
        assertEquals(TemplateTest.template1, TemplateTest.template1);
        assertEquals(TemplateTest.template1, TemplateTest.template2);
        assertNotEquals(TemplateTest.template1, TemplateTest.template3);
    }

}
