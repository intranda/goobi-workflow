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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class RestMetadataTest extends AbstractTest {

    @Test
    public void testConstructor() {
        RestMetadata fixture = new RestMetadata();
        assertNotNull(fixture);
    }

    @Test
    public void testValue() {
        RestMetadata fixture = new RestMetadata();
        assertNull(fixture.getValue());
        fixture.setValue("fixture");
        assertEquals("fixture", fixture.getValue());
    }

    @Test
    public void testLabels() {
        RestMetadata fixture = new RestMetadata();
        assertNull(fixture.getLabels());
        Map<String, String> labelMap = new HashMap<>();
        labelMap.put("fixture", "fixture");

        fixture.setLabels(labelMap);
        assertNotNull(fixture.getLabels());
        assertEquals("fixture", fixture.getLabels().get("fixture"));
    }

    @Test
    public void testAuthorityID() {
        RestMetadata fixture = new RestMetadata();
        assertNotNull(fixture);

        assertNull(fixture.getAuthorityID());
        fixture.setAuthorityID("fixture");
        assertEquals("fixture", fixture.getAuthorityID());
    }

    @Test
    public void testAuthorityValue() {
        RestMetadata fixture = new RestMetadata();
        assertNotNull(fixture);
        assertNull(fixture.getAuthorityValue());
        fixture.setAuthorityValue("fixture");
        assertEquals("fixture", fixture.getAuthorityValue());
    }

    @Test
    public void testAuthorityURI() {
        RestMetadata fixture = new RestMetadata();
        assertNotNull(fixture);

        assertNull(fixture.getAuthorityURI());
        fixture.setAuthorityURI("fixture");
        assertEquals("fixture", fixture.getAuthorityURI());
    }

    @Test
    public void testAnyValue() {
        RestMetadata fixture = new RestMetadata();
        assertFalse(fixture.anyValue());

        fixture.setValue("");
        assertTrue(fixture.anyValue());

        fixture.setValue(null);
        assertFalse(fixture.anyValue());

        fixture.setAuthorityID("fixture");
        assertTrue(fixture.anyValue());
        fixture.setAuthorityID(null);
        assertFalse(fixture.anyValue());

        fixture.setAuthorityValue("fixture");
        assertTrue(fixture.anyValue());
        fixture.setAuthorityValue(null);
        assertFalse(fixture.anyValue());

        fixture.setAuthorityURI("fixture");
        assertTrue(fixture.anyValue());

    }

}
