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

package io.goobi.workflow.ruleseteditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.goobi.workflow.ruleseteditor.RulesetUsage.Viewer;

/**
 * Tests which processes of a ruleset a user can open in the process list or check, and why the others are hidden.
 */
public class RulesetUsageTest {

    private static final int OWN = 1;
    private static final int OTHER = 2;

    /** a user of institution 1 without any of the roles and with the default list toggles */
    private static final Viewer LIMITED = new Viewer(OWN, false, false, false);

    /**
     * The example from the design discussion: project A (member) with 2 open and 1 closed process, project B (no member) with 1, archived project
     * C (member) with 1, and 2 templates.
     */
    private static final List<RulesetUsageGroup> EXAMPLE = Arrays.asList(
            process(false, false, OWN, true, 2),
            process(false, true, OWN, true, 1),
            process(false, false, OWN, false, 1),
            process(true, false, OWN, true, 1),
            new RulesetUsageGroup(true, false, false, OWN, true, 2));

    @Test
    public void testAdministratorSeesEverything() {
        RulesetUsage usage = RulesetUsage.of(EXAMPLE, Viewer.ALL);

        assertEquals(5, usage.getProcesses());
        assertEquals(2, usage.getTemplates());
        assertEquals(5, usage.getVisible());
        assertEquals(0, usage.getHidden());
    }

    @Test
    public void testLimitedUserSeesTheReasons() {
        RulesetUsage usage = RulesetUsage.of(EXAMPLE, LIMITED);

        // the badge stays the same for everyone
        assertEquals(5, usage.getProcesses());
        assertEquals(2, usage.getTemplates());
        assertEquals(2, usage.getVisible());
        assertEquals(0, usage.getHiddenInstitution());
        assertEquals(1, usage.getHiddenProject());
        assertEquals(1, usage.getHiddenArchived());
        assertEquals(1, usage.getHiddenClosed());
        assertEquals(3, usage.getHidden());
    }

    @Test
    public void testEachHiddenProcessCountsOnceUnderTheFirstReason() {
        // closed, in an archived project of another institution, without membership
        RulesetUsage usage = RulesetUsage.of(List.of(process(true, true, OTHER, false, 1)), LIMITED);

        assertEquals(1, usage.getHiddenInstitution());
        assertEquals(0, usage.getHiddenProject());
        assertEquals(0, usage.getHiddenArchived());
        assertEquals(0, usage.getHiddenClosed());
        assertEquals(1, usage.getHidden());
    }

    @Test
    public void testArchivedComesBeforeClosed() {
        RulesetUsage usage = RulesetUsage.of(List.of(process(true, true, OWN, true, 1)), LIMITED);

        assertEquals(1, usage.getHiddenArchived());
        assertEquals(0, usage.getHiddenClosed());
    }

    @Test
    public void testAllInstitutionsWithoutInstitutionLimit() {
        RulesetUsage usage = RulesetUsage.of(List.of(process(false, false, OTHER, true, 1)), new Viewer(null, false, false, false));

        assertEquals(1, usage.getVisible());
    }

    @Test
    public void testAllProjectsRoleShowsProjectsWithoutMembership() {
        RulesetUsage usage = RulesetUsage.of(List.of(process(false, false, OWN, false, 1)), new Viewer(OWN, true, false, false));

        assertEquals(1, usage.getVisible());
    }

    @Test
    public void testListTogglesShowClosedAndArchived() {
        List<RulesetUsageGroup> groups = Arrays.asList(process(true, false, OWN, true, 1), process(false, true, OWN, true, 1));

        assertEquals(1, RulesetUsage.of(groups, new Viewer(OWN, false, false, true)).getVisible());
        assertEquals(1, RulesetUsage.of(groups, new Viewer(OWN, false, true, false)).getVisible());
        assertEquals(2, RulesetUsage.of(groups, new Viewer(OWN, false, true, true)).getVisible());
    }

    @Test
    public void testTemplatesAreNeverVisibleOrHidden() {
        RulesetUsage usage = RulesetUsage.of(List.of(new RulesetUsageGroup(true, true, true, OTHER, false, 3)), LIMITED);

        assertEquals(0, usage.getProcesses());
        assertEquals(3, usage.getTemplates());
        assertEquals(0, usage.getVisible());
        assertEquals(0, usage.getHidden());
    }

    @Test
    public void testRulesetWithOnlyTemplatesIsInUse() {
        assertTrue(RulesetUsage.of(List.of(new RulesetUsageGroup(true, false, false, OWN, true, 1)), Viewer.ALL).isInUse());
    }

    @Test
    public void testRulesetWithoutProcessesIsNotInUse() {
        RulesetUsage usage = RulesetUsage.of(List.of(), Viewer.ALL);

        assertFalse(usage.isInUse());
        assertFalse(usage.isUnknown());
        assertEquals(0, usage.getProcesses());
    }

    @Test
    public void testUnknownUsageIsInUse() {
        // without knowing the usage the ruleset must not be deletable
        RulesetUsage usage = RulesetUsage.unknown();

        assertTrue(usage.isUnknown());
        assertTrue(usage.isInUse());
        assertEquals(0, usage.getProcesses());
        assertEquals(0, usage.getVisible());
    }

    private static RulesetUsageGroup process(boolean closed, boolean archived, int institutionId, boolean member, int count) {
        return new RulesetUsageGroup(false, archived, closed, institutionId, member, count);
    }
}
