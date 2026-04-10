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
package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthenticationMethodDescriptionTest {

    @Test
    public void testConstructorWithRequiredFields() {
        AuthenticationMethodDescription desc = new AuthenticationMethodDescription("ldap", "LDAP Login", "/api/auth/ldap");
        assertNotNull(desc);
        assertEquals("ldap", desc.getMethodType());
        assertEquals("LDAP Login", desc.getDescription());
        assertEquals("/api/auth/ldap", desc.getUrl());
    }

    @Test
    public void testOptionalFieldsDefaultToNull() {
        AuthenticationMethodDescription desc = new AuthenticationMethodDescription("openid", "OpenID", "/api/auth/openid");
        assertNull(desc.getMethodID());
        assertNull(desc.getApiTokenId());
        assertFalse(desc.isSelected());
    }

    @Test
    public void testSetOptionalFields() {
        AuthenticationMethodDescription desc = new AuthenticationMethodDescription("token", "Token Auth", "/api/auth/token");
        desc.setMethodID(3);
        desc.setApiTokenId(7);
        desc.setSelected(true);

        assertEquals(Integer.valueOf(3), desc.getMethodID());
        assertEquals(Integer.valueOf(7), desc.getApiTokenId());
        assertTrue(desc.isSelected());
    }

    @Test
    public void testSettersOverrideConstructorValues() {
        AuthenticationMethodDescription desc = new AuthenticationMethodDescription("basic", "Basic Auth", "/api/auth/basic");
        desc.setMethodType("digest");
        desc.setDescription("Digest Auth");
        desc.setUrl("/api/auth/digest");

        assertEquals("digest", desc.getMethodType());
        assertEquals("Digest Auth", desc.getDescription());
        assertEquals("/api/auth/digest", desc.getUrl());
    }
}
