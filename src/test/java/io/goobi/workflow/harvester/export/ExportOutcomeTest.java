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
package io.goobi.workflow.harvester.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;

public class ExportOutcomeTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testConstructor() {
        ExportOutcome fixture = new ExportOutcome();
        assertEquals(ExportOutcomeStatus.OK, fixture.status);

    }

    @Test
    public void testExportOutcomeStatus() {
        ExportOutcome fixture = new ExportOutcome();
        assertEquals(ExportOutcomeStatus.OK, fixture.status);

        fixture.status = ExportOutcomeStatus.NOT_FOUND;
        assertEquals(ExportOutcomeStatus.NOT_FOUND, fixture.status);

        fixture.status = ExportOutcomeStatus.ERROR;
        assertEquals(ExportOutcomeStatus.ERROR, fixture.status);

        fixture.status = ExportOutcomeStatus.SKIP;
        assertEquals(ExportOutcomeStatus.SKIP, fixture.status);
    }

    @Test
    public void testMessage() {
        ExportOutcome fixture = new ExportOutcome();
        assertNull(fixture.message);

        fixture.message = "fixture";
        assertEquals("fixture", fixture.message);

    }
}
