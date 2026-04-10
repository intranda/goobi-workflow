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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class RestProcessQueryResourceTest {

    @Test
    public void testNoArgConstructor() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        assertNotNull(resource);
    }

    @Test
    public void testGetConditionsWithNullFilterReturnsEmpty() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        resource.setFilter(null);
        assertEquals(0, resource.getConditions().length);
    }

    @Test
    public void testGetConditionsWithBlankFilterReturnsEmpty() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        resource.setFilter("   ");
        assertEquals(0, resource.getConditions().length);
    }

    @Test
    public void testGetConditionsWithSingleCondition() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        resource.setFilter("'step:Scan'");
        String[] conditions = resource.getConditions();
        assertEquals(1, conditions.length);
        assertEquals("\"step:Scan\"", conditions[0]);
    }

    @Test
    public void testGetConditionsWithMultipleConditions() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        resource.setFilter("'step:Scan''project:MyProject'");
        String[] conditions = resource.getConditions();
        assertEquals(2, conditions.length);
        assertEquals("\"step:Scan\"", conditions[0]);
        assertEquals("\"project:MyProject\"", conditions[1]);
    }

    @Test
    public void testGetConditionsWrapsInDoubleQuotes() {
        RestProcessQueryResource resource = new RestProcessQueryResource();
        resource.setFilter("'my filter'");
        String[] conditions = resource.getConditions();
        assertEquals(1, conditions.length);
        assertEquals("\"my filter\"", conditions[0]);
    }
}
