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
package org.goobi.production.flow.statistics.hibernate;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FilterStringTest {

    // --- English constants ---

    @Test
    public void testProcessproperty() {
        assertEquals("processproperty:", FilterString.PROCESSPROPERTY);
    }

    @Test
    public void testStepproperty() {
        assertEquals("stepproperty:", FilterString.STEPPROPERTY);
    }

    @Test
    public void testStep() {
        assertEquals("step:", FilterString.STEP);
    }

    @Test
    public void testStepinwork() {
        assertEquals("stepinwork:", FilterString.STEPINWORK);
    }

    @Test
    public void testStepdone() {
        assertEquals("stepdone:", FilterString.STEPDONE);
    }

    @Test
    public void testProject() {
        assertEquals("project:", FilterString.PROJECT);
    }

    @Test
    public void testId() {
        assertEquals("id:", FilterString.ID);
    }

    @Test
    public void testProcess() {
        assertEquals("process:", FilterString.PROCESS);
    }

    @Test
    public void testBatch() {
        assertEquals("batch:", FilterString.BATCH);
    }

    @Test
    public void testMetadata() {
        assertEquals("meta:", FilterString.METADATA);
    }

    @Test
    public void testJournal() {
        assertEquals("journal:", FilterString.JOURNAL);
    }

    @Test
    public void testInstitution() {
        assertEquals("institution", FilterString.INSTITUTION);
    }

    @Test
    public void testProcessDate() {
        assertEquals("processdate", FilterString.PROCESS_DATE);
    }

    @Test
    public void testStepStartDate() {
        assertEquals("stepstartdate", FilterString.STEP_START_DATE);
    }

    @Test
    public void testStepFinishDate() {
        assertEquals("stepfinishdate", FilterString.STEP_FINISH_DATE);
    }

    // --- German constants ---

    @Test
    public void testSchritt() {
        assertEquals("schritt:", FilterString.SCHRITT);
    }

    @Test
    public void testProjekt() {
        assertEquals("projekt:", FilterString.PROJEKT);
    }

    @Test
    public void testProzess() {
        assertEquals("prozess:", FilterString.PROZESS);
    }

    @Test
    public void testGruppe() {
        assertEquals("gruppe:", FilterString.GRUPPE);
    }

    @Test
    public void testProzesslog() {
        assertEquals("log:", FilterString.PROZESSLOG);
    }
}
