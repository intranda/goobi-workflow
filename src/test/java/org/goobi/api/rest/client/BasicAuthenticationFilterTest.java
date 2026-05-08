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

package org.goobi.api.rest.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

@ExtendWith(MockitoExtension.class)
public class BasicAuthenticationFilterTest extends AbstractTest {

    @Test
    public void testConstructor() {
        BasicAuthenticationFilter baf = new BasicAuthenticationFilter("user", "password");
        assertNotNull(baf);
    }

    @Test
    public void testAddFilter() throws IOException {

        MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();

        ClientRequestContext mock = Mockito.mock(ClientRequestContext.class);
        Mockito.when(mock.getHeaders()).thenReturn(map);

        BasicAuthenticationFilter baf = new BasicAuthenticationFilter("user", "password");
        assertNotNull(baf);
        baf.filter(mock);
        assertEquals(1, map.keySet().size());
        assertNotNull(map.get("Authorization"));
    }

}
