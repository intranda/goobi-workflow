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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class RestEndpointConfigTest {

    @Test
    public void testConstructor() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNotNull(fixture);
    }

    @Test
    public void testPath() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getPath());
        fixture.setPath("fixture");
        assertEquals("fixture", fixture.getPath());
    }

    @Test
    public void testJwtAuth() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertFalse(fixture.isJwtAuth());
        fixture.setJwtAuth(true);
        assertTrue(fixture.isJwtAuth());
        fixture.setJwtAuth(false);
        assertFalse(fixture.isJwtAuth());
    }

    @Test
    public void testMethodConfigs() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getMethodConfigs());
        RestMethodConfig rmc = new RestMethodConfig();
        List<RestMethodConfig> list = new ArrayList<>();
        fixture.setMethodConfigs(list);
        assertEquals(0, fixture.getMethodConfigs().size());
        list.add(rmc);
        assertEquals(1, fixture.getMethodConfigs().size());
    }

    @Test
    public void testCorsOrigins() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getCorsOrigins());
        List<String> list = new ArrayList<>();
        fixture.setCorsOrigins(list);
        assertEquals(0, fixture.getCorsOrigins().size());
        list.add("xyz");
        assertEquals(1, fixture.getCorsOrigins().size());
    }

    @Test
    public void testCorsMethods() {
        RestEndpointConfig fixture = new RestEndpointConfig();
        assertNull(fixture.getCorsMethods());
        List<String> list = new ArrayList<>();
        fixture.setCorsMethods(list);
        assertEquals(0, fixture.getCorsMethods().size());
        list.add("xyz");
        assertEquals(1, fixture.getCorsMethods().size());
    }

}
