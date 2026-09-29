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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.production.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class MetadataEditorAccessTest extends AbstractTest {

    private static final Predicate<String> NO_ROLES = role -> false;

    private User user;
    private Process process;

    @BeforeEach
    public void setUp() {
        Project ownProject = new Project();
        ownProject.setId(1);
        Project foreignProject = new Project();
        foreignProject.setId(2);

        user = Mockito.mock(User.class);
        Mockito.when(user.getProjekte()).thenReturn(List.of(ownProject));

        process = Mockito.mock(Process.class);
        Mockito.when(process.getProjekt()).thenReturn(foreignProject);
    }

    private static Predicate<String> roles(UserRole... roles) {
        Set<String> names = Arrays.stream(roles).map(UserRole::name).collect(Collectors.toSet());
        return names::contains;
    }

    private void moveProcessToOwnProject() {
        Project ownProject = new Project();
        ownProject.setId(1);
        Mockito.when(process.getProjekt()).thenReturn(ownProject);
    }

    @Test
    public void testNoUserHasNoAccess() {
        assertFalse(MetadataEditorAccess.canRead(null, process, roles(UserRole.Workflow_General_Show_All_Projects)));
        assertFalse(MetadataEditorAccess.canWrite(null, process, roles(UserRole.Task_Mets_Metadata)));
    }

    @Test
    public void testForeignProjectIsDenied() {
        assertFalse(MetadataEditorAccess.canRead(user, process, NO_ROLES));
        assertFalse(MetadataEditorAccess.canWrite(user, process, NO_ROLES));
    }

    @Test
    public void testForeignProjectIsDeniedEvenWithMetsRole() {
        Predicate<String> hasRole = roles(UserRole.Task_Mets_Metadata);
        assertFalse(MetadataEditorAccess.canRead(user, process, hasRole));
        assertFalse(MetadataEditorAccess.canWrite(user, process, hasRole));
    }

    @Test
    public void testShowAllProjectsAllowsReading() {
        Predicate<String> hasRole = roles(UserRole.Workflow_General_Show_All_Projects);
        assertTrue(MetadataEditorAccess.canRead(user, process, hasRole));
        assertFalse(MetadataEditorAccess.canWrite(user, process, hasRole));
    }

    @Test
    public void testShowAllProjectsAndMetsRoleAllowsWriting() {
        assertTrue(MetadataEditorAccess.canWrite(user, process,
                roles(UserRole.Workflow_General_Show_All_Projects, UserRole.Task_Mets_Structure)));
    }

    @Test
    public void testProjectMemberWithoutMetsRoleIsReadOnly() {
        moveProcessToOwnProject();
        assertTrue(MetadataEditorAccess.canRead(user, process, NO_ROLES));
        assertFalse(MetadataEditorAccess.canWrite(user, process, NO_ROLES));
    }

    @Test
    public void testProjectMemberWithMetsRoleMayWrite() {
        moveProcessToOwnProject();
        for (UserRole role : List.of(UserRole.Task_Mets_Pagination, UserRole.Task_Mets_Structure, UserRole.Task_Mets_Metadata,
                UserRole.Task_Mets_Files)) {
            assertTrue(MetadataEditorAccess.canWrite(user, process, roles(role)), role.name());
        }
    }

    @Test
    public void testProjectIdIsUsedWhenProjectIsNotLoaded() {
        Mockito.when(process.getProjekt()).thenReturn(null);
        Mockito.when(process.getProjectId()).thenReturn(1);
        assertTrue(MetadataEditorAccess.canRead(user, process, NO_ROLES));
    }
}
