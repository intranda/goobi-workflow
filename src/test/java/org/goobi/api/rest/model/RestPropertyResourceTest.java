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
package org.goobi.api.rest.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.junit.jupiter.api.Test;

public class RestPropertyResourceTest {

    @Test
    public void testNoArgConstructor() {
        RestPropertyResource resource = new RestPropertyResource();
        assertNotNull(resource);
        assertNull(resource.getId());
        assertNull(resource.getName());
        assertNull(resource.getValue());
    }

    @Test
    public void testConstructorFromGoobiProperty() {
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setId(5);
        property.setPropertyName("Opening angle");
        property.setPropertyValue("90°");
        Date now = new Date();
        property.setCreationDate(now);

        RestPropertyResource resource = new RestPropertyResource(property);

        assertEquals(Integer.valueOf(5), resource.getId());
        assertEquals("Opening angle", resource.getName());
        assertEquals("90°", resource.getValue());
        assertEquals(now, resource.getCreationDate());
    }

    @Test
    public void testSettersAndGetters() {
        RestPropertyResource resource = new RestPropertyResource();
        Date now = new Date();
        resource.setId(1);
        resource.setName("color");
        resource.setValue("red");
        resource.setCreationDate(now);

        assertEquals(Integer.valueOf(1), resource.getId());
        assertEquals("color", resource.getName());
        assertEquals("red", resource.getValue());
        assertEquals(now, resource.getCreationDate());
    }
}
