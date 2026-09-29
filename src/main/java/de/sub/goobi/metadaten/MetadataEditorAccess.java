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

package de.sub.goobi.metadaten;

import java.util.List;
import java.util.function.Predicate;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.production.enums.UserRole;

/**
 * Server side access checks for the metadata editor. The decision is based on the logged in user only, never on request parameters.
 */
public final class MetadataEditorAccess {

    private static final List<String> METS_EDITOR_ROLES = List.of(UserRole.Task_Mets_Pagination.name(), UserRole.Task_Mets_Structure.name(),
            UserRole.Task_Mets_Metadata.name(), UserRole.Task_Mets_Files.name());

    private MetadataEditorAccess() {
    }

    /**
     * Check if the user may open the metadata of the process at all.
     *
     * @param user the logged in user
     * @param process the process to open
     * @param hasRole checks if the logged in user has a given role
     * @return true, if the process belongs to a project of the user or the user may see all projects
     */
    public static boolean canRead(User user, Process process, Predicate<String> hasRole) {
        if (user == null || process == null) {
            return false;
        }
        return hasRole.test(UserRole.Workflow_General_Show_All_Projects.name()) || isProjectMember(user, process);
    }

    /**
     * Check if the user may change the metadata of the process.
     *
     * @param user the logged in user
     * @param process the process to open
     * @param hasRole checks if the logged in user has a given role
     * @return true, if the user may see the process and has one of the metadata editor roles
     */
    public static boolean canWrite(User user, Process process, Predicate<String> hasRole) {
        return canRead(user, process, hasRole) && METS_EDITOR_ROLES.stream().anyMatch(hasRole);
    }

    private static boolean isProjectMember(User user, Process process) {
        Integer projectId = process.getProjekt() != null ? process.getProjekt().getId() : process.getProjectId();
        if (projectId == null || user.getProjekte() == null) {
            return false;
        }
        return user.getProjekte().stream().map(Project::getId).anyMatch(projectId::equals);
    }
}
