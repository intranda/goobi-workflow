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
package org.goobi.production.cli;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class WebInterfaceConfigTest {

    @Test
    public void testGetCredencialsReturnsEmptyListWhenConfigFileMissing() {
        // When goobi_webapi.xml does not exist, a ConfigurationException is caught
        // and an empty list is returned.
        List<String> result = WebInterfaceConfig.getCredencials("127.0.0.1", "secret");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetCredencialsWithNonMatchingIpReturnsEmptyList() {
        List<String> result = WebInterfaceConfig.getCredencials("192.168.1.100", "wrongpassword");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
