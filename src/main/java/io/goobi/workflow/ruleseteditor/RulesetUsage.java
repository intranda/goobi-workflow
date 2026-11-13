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

import java.util.List;

import lombok.Getter;

/**
 * How many processes use a ruleset and how many of them a user can open in the process list.
 */
@Getter
public final class RulesetUsage {

    /**
     * What a user can see in the process list.
     *
     * @param institutionId only projects of this institution, null for all institutions
     * @param allProjects all projects, not only the ones the user is a member of
     * @param showArchived processes in archived projects are shown
     * @param showClosed closed processes are shown
     */
    public record Viewer(Integer institutionId, boolean allProjects, boolean showArchived, boolean showClosed) {
        public static final Viewer ALL = new Viewer(null, true, true, true);
    }

    private final int processes;
    private final int templates;
    private final int visible;
    private final int hiddenInstitution;
    private final int hiddenProject;
    private final int hiddenArchived;
    private final int hiddenClosed;
    private final boolean unknown;

    private RulesetUsage(int processes, int templates, int visible, int[] hidden, boolean unknown) {
        this.processes = processes;
        this.templates = templates;
        this.visible = visible;
        this.hiddenInstitution = hidden[0];
        this.hiddenProject = hidden[1];
        this.hiddenArchived = hidden[2];
        this.hiddenClosed = hidden[3];
        this.unknown = unknown;
    }

    /**
     * Sorts the groups into what the viewer can open, and what is hidden from the viewer (each process under the first matching reason).
     *
     * @param groups the usage groups of one ruleset
     * @param viewer what the current user can see
     * @return the usage
     */
    public static RulesetUsage of(List<RulesetUsageGroup> groups, Viewer viewer) {
        int processes = 0;
        int templates = 0;
        int visible = 0;
        // order: institution, project, archived, closed
        int[] hidden = new int[4];
        for (RulesetUsageGroup g : groups) {
            if (g.template()) {
                // templates are not listed, they only block deleting
                templates += g.count();
                continue;
            }
            processes += g.count();
            boolean otherInstitution = viewer.institutionId() != null && g.institutionId() != viewer.institutionId();
            boolean noMember = !viewer.allProjects() && !g.member();
            if (otherInstitution) {
                hidden[0] += g.count();
            } else if (noMember) {
                hidden[1] += g.count();
            } else {
                if (!viewer.showArchived() && g.archived()) {
                    hidden[2] += g.count();
                } else if (!viewer.showClosed() && g.closed()) {
                    hidden[3] += g.count();
                } else {
                    visible += g.count();
                }
            }
        }
        return new RulesetUsage(processes, templates, visible, hidden, false);
    }

    /**
     * The usage could not be determined. It counts as in use so that the ruleset cannot be deleted.
     *
     * @return an usage with all counts 0
     */
    public static RulesetUsage unknown() {
        return new RulesetUsage(0, 0, 0, new int[4], true);
    }

    public int getHidden() {
        return hiddenInstitution + hiddenProject + hiddenArchived + hiddenClosed;
    }

    public boolean isInUse() {
        return unknown || processes + templates > 0;
    }
}
