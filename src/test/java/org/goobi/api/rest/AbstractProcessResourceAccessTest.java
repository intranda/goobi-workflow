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

import static org.junit.jupiter.api.Assertions.assertNull;

import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import jakarta.ws.rs.core.Response;

public class AbstractProcessResourceAccessTest extends AbstractProcessResourceTest {

    private static final class Concrete extends AbstractProcessResource {
        Response call(Process p) {
            return checkProcessAccess(p);
        }
    }

    @Test
    public void testNoTokenAllowsAccess() {
        Concrete c = new Concrete();
        c.setRequest(mockRequestWithoutToken());
        assertNull(c.call(Mockito.mock(Process.class)));
    }
}
