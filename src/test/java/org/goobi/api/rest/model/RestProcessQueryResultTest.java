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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.Process;
import org.junit.jupiter.api.Test;

public class RestProcessQueryResultTest {

    @Test
    public void testConstructorWithNullList() {
        RestProcessQueryResult result = new RestProcessQueryResult(null);
        assertNotNull(result);
        assertEquals(0, result.getResults());
        assertNotNull(result.getIds());
        assertEquals(0, result.getIds().length);
    }

    @Test
    public void testConstructorWithEmptyList() {
        RestProcessQueryResult result = new RestProcessQueryResult(new ArrayList<>());
        assertEquals(0, result.getResults());
        assertEquals(0, result.getIds().length);
    }

    @Test
    public void testConstructorWithProcesses() {
        Process p1 = new Process();
        p1.setId(10);
        Process p2 = new Process();
        p2.setId(20);
        Process p3 = new Process();
        p3.setId(30);

        List<Process> processes = Arrays.asList(p1, p2, p3);
        RestProcessQueryResult result = new RestProcessQueryResult(processes);

        assertEquals(3, result.getResults());
        assertEquals(3, result.getIds().length);
        assertEquals(Integer.valueOf(10), result.getIds()[0]);
        assertEquals(Integer.valueOf(20), result.getIds()[1]);
        assertEquals(Integer.valueOf(30), result.getIds()[2]);
    }
}
