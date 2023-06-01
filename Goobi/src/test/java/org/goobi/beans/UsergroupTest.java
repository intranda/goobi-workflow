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
 * 
 */
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class UsergroupTest extends AbstractTest {

    @Test
    public void testId() {
        Usergroup group = new Usergroup();
        assertNull(group.getId());
        group.setId(42);
        assertEquals(42, group.getId().intValue());
    }

    @Test
    public void testTitel() {
        Usergroup group = new Usergroup();
        assertEquals("", group.getTitel());
        group.setTitel("Titel");
        assertEquals("Titel", group.getTitel());
    }

    @Test
    public void testBerechtigung() {
        Usergroup group = new Usergroup();
        assertEquals(4, group.getBerechtigung().intValue());
        group.setBerechtigung(1);
        assertEquals(1, group.getBerechtigung().intValue());
    }

    @Test
    public void testBerechtigungAsString() {
        Usergroup group = new Usergroup();
        assertEquals("4", group.getBerechtigungAsString());
        group.setBerechtigungAsString("1");
        assertEquals("1", group.getBerechtigungAsString());
    }

    public void testBenutzer() {
        // getBenutzer() can not be tested without database
    }

    @Test
    public void testIsDeletable() {
        Usergroup group = new Usergroup();
        assertTrue(group.isDeletable());
    }

    @Test
    public void testSchritte() {
        Usergroup group = new Usergroup();
        assertNull(group.getSchritte());
        Step step = new Step();
        List<Step> steps = new ArrayList<>();
        steps.add(step);
        group.setSchritte(steps);
        assertNotNull(group.getSchritte());
        assertEquals(1, group.getSchritte().size());
    }

    @Test
    public void testGetAddRemoveUserRoles() {
        Usergroup group = new Usergroup();
        List<String> roles = group.getUserRoles();
        assertNotNull(roles);
        assertEquals(0, roles.size());
        group.addUserRole("write");
        assertEquals(1, roles.size());
        group.addUserRole("read");
        assertEquals(2, roles.size());
        // "read" should not be added twice, so size() is still 2
        group.addUserRole("read");
        assertEquals(2, roles.size());
        // This is not contained, list is not changed:
        group.removeUserRole("execute");
        assertEquals(2, roles.size());
        group.removeUserRole("read");
        assertEquals(1, roles.size());
        group.removeUserRole("write");
        assertEquals(0, roles.size());

    }

    @Test
    public void testInstitution() {
        Usergroup group = new Usergroup();
        Institution institution = new Institution();
        group.setInstitution(institution);
        assertNotNull(group.getInstitution());
        assertSame(institution, group.getInstitution());
    }

    @Test
    public void testInstitutionId() {
        Usergroup group = new Usergroup();
        assertNull(group.getInstitutionId());
        group.setInstitutionId(42);
        assertEquals(42, group.getInstitutionId().intValue());
    }

    @Test
    public void testPanelAusgeklappt() {
        Usergroup group = new Usergroup();
        assertFalse(group.isPanelAusgeklappt());
        group.setPanelAusgeklappt(true);
        assertTrue(group.isPanelAusgeklappt());
    }

    @Test
    public void testCompareTo() {
        Institution institutionA = new Institution();
        institutionA.setShortName("ABC");
        Institution institutionB = new Institution();
        institutionB.setShortName("XYZ");
        Usergroup group1 = new Usergroup();
        group1.setTitel("Admin");
        group1.setInstitution(institutionA);
        Usergroup group2 = new Usergroup();
        group2.setTitel("Admin");
        group2.setInstitution(institutionA);
        Usergroup group3 = new Usergroup();
        group3.setTitel("Admin");
        group3.setInstitution(institutionB);
        Usergroup group4 = new Usergroup();
        group4.setTitel("Users");
        group4.setInstitution(institutionA);
        // Here, the method Usergroup.equals() is called implicitly by assertEquals() and assertNotEquals():
        assertEquals(0, group1.compareTo(group1));
        assertEquals(0, group1.compareTo(group2));
        assertEquals(0, group2.compareTo(group1));
        assertNotEquals(-1, group1.compareTo(group3));// "institution" is different
        assertNotEquals(1, group1.compareTo(group4));// "titel" is different
    }

    @Test
    public void testEquals() {
        Usergroup group1 = new Usergroup();
        group1.setTitel("Admin");
        group1.setInstitutionId(1);
        Usergroup group2 = new Usergroup();
        group2.setTitel("Admin");
        group2.setInstitutionId(1);
        Usergroup group3 = new Usergroup();
        group3.setTitel("Admin");
        group3.setInstitutionId(2);
        Usergroup group4 = new Usergroup();
        group4.setTitel("Users");
        group4.setInstitutionId(1);
        // Here, the method Usergroup.equals() is called implicitly by assertEquals() and assertNotEquals():
        assertEquals(group1, group1);
        assertEquals(group1, group2);
        assertEquals(group2, group1);
        assertNotEquals(group1, group3);// "institutionId" is different
        assertNotEquals(group1, group4);// "titel" is different
    }

    @Test
    public void testGetTitelLokalisiert() {
        Usergroup group = new Usergroup();
        group.setTitel("Titel");
        assertEquals("Titel", group.getTitelLokalisiert());
    }

    @Test
    public void testHashCode() {
        Usergroup group0 = new Usergroup();
        assertEquals(group0.hashCode(), group0.hashCode());
        Usergroup group1 = new Usergroup();
        group1.setId(4);
        assertNotEquals(group0.hashCode(), group1.hashCode());
        Usergroup group2 = new Usergroup();
        group2.setTitel("Titel");
        assertNotEquals(group0.hashCode(), group2.hashCode());
        Usergroup group3 = new Usergroup();
        group3.setBerechtigung(1);
        assertNotEquals(group0.hashCode(), group3.hashCode());
        Usergroup group4 = new Usergroup();
        group4.setBenutzer(new ArrayList<>());
        assertNotEquals(group0.hashCode(), group4.hashCode());
        Usergroup group5 = new Usergroup();
        group5.setSchritte(new ArrayList<>());
        assertNotEquals(group0.hashCode(), group5.hashCode());
        Usergroup group6 = new Usergroup();
        group6.setUserRoles(new ArrayList<>());
        assertNotEquals(group0.hashCode(), group6.hashCode());
        Usergroup group7 = new Usergroup();
        group7.setPanelAusgeklappt(true);
        assertNotEquals(group0.hashCode(), group7.hashCode());
    }

}
