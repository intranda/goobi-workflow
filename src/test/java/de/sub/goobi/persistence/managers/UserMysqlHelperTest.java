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
package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class UserMysqlHelperTest extends AbstractTest {

    @Test
    public void testBuildUserSortFieldRejectsSqlInjection() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("'; DROP TABLE benutzer;--", sql);
        assertEquals("", result);
        assertFalse(sql.toString().contains("DROP"));
    }

    @Test
    public void testBuildUserSortFieldRejectsUnknownField() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("unknownField", sql);
        assertEquals("", result);
    }

    @Test
    public void testBuildUserSortFieldAcceptsLoginField() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("benutzer.login", sql);
        assertEquals("benutzer.login", result);
        assertEquals("", sql.toString());
    }

    @Test
    public void testBuildUserSortFieldAcceptsNameField() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("benutzer.nachname, benutzer.vorname", sql);
        assertEquals("benutzer.nachname, benutzer.vorname", result);
    }

    @Test
    public void testBuildUserSortFieldAcceptsStandortField() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("benutzer.standort", sql);
        assertEquals("benutzer.standort", result);
    }

    @Test
    public void testBuildUserSortFieldAcceptsInstitutionField() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("institution.shortName", sql);
        assertEquals("institution.shortName", result);
    }

    @Test
    public void testBuildUserSortFieldGroupsAddsLeftJoin() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("groups", sql);
        assertTrue(sql.toString().contains("LEFT JOIN"));
        assertTrue(sql.toString().contains("benutzergruppenmitgliedschaft"));
        assertTrue(result.contains("groups"));
        assertFalse(result.isEmpty());
    }

    @Test
    public void testBuildUserSortFieldProjectsAddsLeftJoin() {
        StringBuilder sql = new StringBuilder();
        String result = UserMysqlHelper.buildUserSortField("projects", sql);
        assertTrue(sql.toString().contains("LEFT JOIN"));
        assertTrue(sql.toString().contains("projektbenutzer"));
        assertTrue(result.contains("projects"));
        assertFalse(result.isEmpty());
    }
}
