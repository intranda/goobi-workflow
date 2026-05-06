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
package org.goobi.production.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class UserRoleTest extends AbstractTest {

    @Test
    public void testValuesAreNonEmpty() {
        assertTrue(UserRole.values().length > 0);
    }

    @Test
    public void testGetAllRolesIsNonNull() {
        List<String> roles = UserRole.getAllRoles();
        assertNotNull(roles);
    }

    @Test
    public void testGetAllRolesContainsCoreRoles() {
        List<String> roles = UserRole.getAllRoles();
        assertTrue(roles.contains("Admin_Menu"));
        assertTrue(roles.contains("Admin_Users"));
        assertTrue(roles.contains("Workflow_Processes"));
        assertTrue(roles.contains("Task_List"));
    }

    @Test
    public void testGetAllRolesIsSorted() {
        List<String> roles = UserRole.getAllRoles();
        for (int i = 0; i < roles.size() - 1; i++) {
            assertFalse(roles.get(i).compareTo(roles.get(i + 1)) > 0,
                    "List not sorted at index " + i + ": " + roles.get(i) + " > " + roles.get(i + 1));
        }
    }

    @Test
    public void testValueOfTaskList() {
        assertNotNull(UserRole.valueOf("Task_List"));
    }

    @Test
    public void testValueOfAdminUsers() {
        assertNotNull(UserRole.valueOf("Admin_Users"));
    }
}
