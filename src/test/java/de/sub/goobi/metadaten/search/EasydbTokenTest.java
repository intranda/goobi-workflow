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
package de.sub.goobi.metadaten.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EasydbTokenTest {

    private EasydbToken token;

    @BeforeEach
    public void setUp() {
        token = new EasydbToken();
    }

    @Test
    public void testDefaultValues() {
        assertNotNull(token);
        assertNotNull(token.getCreationDate());
        assertNull(token.getAccess_token());
        assertEquals(0L, token.getExpires_in());
        assertNull(token.getRefresh_token());
        assertNull(token.getScope());
        assertNull(token.getToken_type());
    }

    @Test
    public void testGetTokenReturnsAccessToken() {
        token.setAccess_token("my-access-token");
        assertEquals("my-access-token", token.getToken());
    }

    @Test
    public void testSetTokenSetsAccessToken() {
        token.setToken("my-access-token");
        assertEquals("my-access-token", token.getAccess_token());
    }

    @Test
    public void testSetExpiresIn() {
        token.setExpires_in(3600L);
        assertEquals(3600L, token.getExpires_in());
    }

    @Test
    public void testSetRefreshToken() {
        token.setRefresh_token("refresh-token-123");
        assertEquals("refresh-token-123", token.getRefresh_token());
    }

    @Test
    public void testSetScope() {
        token.setScope("read write");
        assertEquals("read write", token.getScope());
    }

    @Test
    public void testSetTokenType() {
        token.setToken_type("Bearer");
        assertEquals("Bearer", token.getToken_type());
    }
}
