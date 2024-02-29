/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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

package org.goobi.api.rest.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class RestProcessTest extends AbstractTest {

    @Test
    public void testConstructor() {
        RestProcess fixture = new RestProcess(1);
        assertNotNull(fixture);
        assertEquals(1, fixture.getId());
    }

    @Test
    public void testName() {
        RestProcess fixture = new RestProcess(1);
        assertNotNull(fixture);
        fixture.setName("name");
        assertEquals("name", fixture.getName());
    }

    @Test
    public void testRuleset() {
        RestProcess fixture = new RestProcess(1);
        assertNotNull(fixture);
        fixture.setRuleset("ruleset");
        assertEquals("ruleset", fixture.getRuleset());
    }

    @Test
    public void testMetadata() {
        RestProcess fixture = new RestProcess(1);
        assertNotNull(fixture);
        fixture.setRuleset("ruleset");
        assertTrue(fixture.getMetadata().isEmpty());

        RestMetadata rm = new RestMetadata();
        fixture.addMetadata("name", rm);

        assertEquals(1, fixture.getMetadata().size());

    }
}
