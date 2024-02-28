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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ProjectTest extends AbstractTest {

    @Test
    public void testId() {
        Project project = new Project();
        project.setId(42);
        assertEquals(42, project.getId().intValue());
    }

    @Test
    public void testTitel() {
        Project project = new Project();
        project.setTitel("Biography");
        assertEquals("Biography", project.getTitel());
    }

    @Test
    public void testProjectIdentifier() {
        Project project = new Project();
        project.setProjectIdentifier("Biography");
        assertEquals("Biography", project.getProjectIdentifier());
    }

    @Test
    public void testBenutzer() {
        Project project = new Project();
        User user1 = new User();
        User user2 = new User();
        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        project.setBenutzer(users);
        assertEquals(2, project.getBenutzer().size());
    }

    @Test
    public void testProzesse() {
        Project project = new Project();
        Process process1 = new Process();
        Process process2 = new Process();
        List<Process> processes = new ArrayList<>();
        processes.add(process1);
        processes.add(process2);
        project.setProzesse(processes);
        assertEquals(2, project.getProzesse().size());
    }

    @Test
    public void testFilegroups() {
        Project project = new Project();
        ProjectFileGroup group1 = new ProjectFileGroup();
        ProjectFileGroup group2 = new ProjectFileGroup();
        List<ProjectFileGroup> groups = new ArrayList<>();
        groups.add(group1);
        groups.add(group2);
        project.setFilegroups(groups);
        assertEquals(2, project.getFilegroups().size());
    }

    @Test
    public void testUseDmsImport() {
        Project project = new Project();
        project.setUseDmsImport(true);
        assertTrue(project.isUseDmsImport());
    }

    @Test
    public void testDmsImportTimeOut() {
        Project project = new Project();
        project.setDmsImportTimeOut(10000);
        assertEquals(10000, project.getDmsImportTimeOut().intValue());
    }

    @Test
    public void testDmsImportRootPath() {
        Project project = new Project();
        project.setDmsImportRootPath("test/");
        assertEquals("test/", project.getDmsImportRootPath());
    }

    @Test
    public void testDmsImportImagesPath() {
        Project project = new Project();
        project.setDmsImportImagesPath("test/");
        assertEquals("test/", project.getDmsImportImagesPath());
    }

    @Test
    public void testDmsImportSuccessPath() {
        Project project = new Project();
        project.setDmsImportSuccessPath("test/");
        assertEquals("test/", project.getDmsImportSuccessPath());
    }

    @Test
    public void testDmsImportErrorPath() {
        Project project = new Project();
        project.setDmsImportErrorPath("test/");
        assertEquals("test/", project.getDmsImportErrorPath());
    }

    @Test
    public void testDmsImportCreateProcessFolder() {
        Project project = new Project();
        project.setDmsImportCreateProcessFolder(true);
        assertTrue(project.isDmsImportCreateProcessFolder());
        project.setDmsImportCreateProcessFolderHibernate(false);
        assertFalse(project.isDmsImportCreateProcessFolderHibernate());
    }

    @Test
    public void testFileFormatInternal() {
        Project project = new Project();
        project.setFileFormatInternal("format");
        assertEquals("format", project.getFileFormatInternal());
    }

    @Test
    public void testFileFormatDmsExport() {
        Project project = new Project();
        project.setFileFormatDmsExport("format");
        assertEquals("format", project.getFileFormatDmsExport());
    }

    @Test
    public void testMetsRightsOwner() {
        Project project = new Project();
        project.setMetsRightsOwner("test");
        assertEquals("test", project.getMetsRightsOwner());
    }

    @Test
    public void testMetsRightsOwnerLogo() {
        Project project = new Project();
        project.setMetsRightsOwnerLogo("test");
        assertEquals("test", project.getMetsRightsOwnerLogo());
    }

    @Test
    public void testMetsRightsOwnerSite() {
        Project project = new Project();
        project.setMetsRightsOwnerSite("test");
        assertEquals("test", project.getMetsRightsOwnerSite());
    }

    @Test
    public void testMetsRightsOwnerMail() {
        Project project = new Project();
        project.setMetsRightsOwnerMail("test");
        assertEquals("test", project.getMetsRightsOwnerMail());
    }

    @Test
    public void testMetsDigiprovReference() {
        Project project = new Project();
        project.setMetsDigiprovReference("test");
        assertEquals("test", project.getMetsDigiprovReference());
    }

    @Test
    public void testMetsDigiprovPresentation() {
        Project project = new Project();
        project.setMetsDigiprovPresentation("test");
        assertEquals("test", project.getMetsDigiprovPresentation());
    }

    @Test
    public void testMetsDigiprovReferenceAnchor() {
        Project project = new Project();
        project.setMetsDigiprovReferenceAnchor("test");
        assertEquals("test", project.getMetsDigiprovReferenceAnchor());
    }

    @Test
    public void testMetsDigiprovPresentationAnchor() {
        Project project = new Project();
        project.setMetsDigiprovPresentationAnchor("test");
        assertEquals("test", project.getMetsDigiprovPresentationAnchor());
    }

    @Test
    public void testMetsPointerPath() {
        Project project = new Project();
        project.setMetsPointerPath("test");
        assertEquals("test", project.getMetsPointerPath());
    }

    @Test
    public void testMetsPointerPathAnchor() {
        Project project = new Project();
        project.setMetsPointerPathAnchor("test");
        assertEquals("test", project.getMetsPointerPathAnchor());
    }

    @Test
    public void testMetsPurl() {
        Project project = new Project();
        project.setMetsPurl("test");
        assertEquals("test", project.getMetsPurl());
    }

    @Test
    public void testMetsContentIDs() {
        Project project = new Project();
        project.setMetsContentIDs("test");
        assertEquals("test", project.getMetsContentIDs());
    }

    // TODO: commonWorkFlow can not be tested without database

    @Test
    public void testStartDate() {
        Project project = new Project();
        Date now = new Date();
        project.setStartDate(now);
        assertSame(now, project.getStartDate());
    }

    @Test
    public void testEndDate() {
        Project project = new Project();
        Date now = new Date();
        project.setEndDate(now);
        assertSame(now, project.getEndDate());
    }

    @Test
    public void testNumberOfPages() {
        Project project = new Project();
        project.setNumberOfPages(42);
        assertSame(42, project.getNumberOfPages().intValue());
    }

    @Test
    public void testNumberOfVolumes() {
        Project project = new Project();
        project.setNumberOfVolumes(42);
        assertSame(42, project.getNumberOfVolumes().intValue());
    }

    @Test
    public void testProjectIsArchived() {
        Project project = new Project();
        assertFalse(project.getProjectIsArchived());
        project.setProjectIsArchived(true);
        assertTrue(project.getProjectIsArchived());
    }

    @Test
    public void testMetsRightsSponsor() {
        Project project = new Project();
        project.setMetsRightsSponsor("test");
        assertEquals("test", project.getMetsRightsSponsor());
    }

    @Test
    public void testMetsRightsSponsorLogo() {
        Project project = new Project();
        project.setMetsRightsSponsorLogo("test");
        assertEquals("test", project.getMetsRightsSponsorLogo());
    }

    @Test
    public void testMetsRightsSponsorSiteURL() {
        Project project = new Project();
        project.setMetsRightsSponsorSiteURL("test");
        assertEquals("test", project.getMetsRightsSponsorSiteURL());
    }

    @Test
    public void testMetsRightsLicense() {
        Project project = new Project();
        project.setMetsRightsLicense("test");
        assertEquals("test", project.getMetsRightsLicense());
    }

    @Test
    public void testInstitution() {
        Project project = new Project();
        Institution institution = new Institution();
        project.setInstitution(institution);
        assertSame(institution, project.getInstitution());
    }

    @Test
    public void testMetsSruUrl() {
        Project project = new Project();
        project.setMetsSruUrl("test");
        assertEquals("test", project.getMetsSruUrl());
    }

    @Test
    public void testMetsIIIFUrl() {
        Project project = new Project();
        project.setMetsIIIFUrl("test");
        assertEquals("test", project.getMetsIIIFUrl());
    }

    @Test
    public void testDfgViewerUrl() {
        Project project = new Project();
        project.setDfgViewerUrl("test");
        assertEquals("test", project.getDfgViewerUrl());
    }

    @Test
    public void testIsDeleteAble() {
        Project project = new Project();
        assertTrue(project.isDeleteAble());
        Process process = new Process();
        List<Process> processes = new ArrayList<>();
        processes.add(process);
        project.setProzesse(processes);
        assertFalse(project.isDeleteAble());
    }

    // TODO: getFileGroups() can not be tested without database

    // TODO: getWorkFlow() can not be tested without database

    /**
     * Note: This method tests "compareTo()" and "equals()" to avoid some redundancy
     */
    @Test
    public void testCompareToAndEquals() {

        // Prepare some institutions:
        Institution institution1 = new Institution();
        institution1.setShortName("ABC");

        Institution institution2 = new Institution();
        institution2.setShortName("XYZ");

        // Prepare some projects with titles and institutions
        Project project1 = new Project();
        project1.setTitel("Project A");
        project1.setInstitution(institution1);

        Project project2 = new Project();
        project2.setTitel("Project A");
        project2.setInstitution(institution2);

        Project project3 = new Project();
        project3.setTitel("Project B");
        project3.setInstitution(institution1);

        // Test whether projects are compared correctly using compareTo():
        assertEquals(0, project1.compareTo(project1));
        assertNotEquals(0, project1.compareTo(project2));
        assertNotEquals(0, project1.compareTo(project3));

        // Test whether projects are equal or different using equals():
        assertEquals(project1, project1);
        assertNotEquals(project1, project2);
        assertNotEquals(project1, project3);
    }

    @Test
    public void testHashCode() {// NOSONAR This method needs > 25 asserts to be complete
        Project project1 = new Project();
        // Here, the hash code method should be called twice:
        assertEquals(project1.hashCode(), project1.hashCode());
        int hash = project1.hashCode();

        Project project2 = new Project();
        List<User> users = new ArrayList<>();
        users.add(new User());
        project2.setBenutzer(users);
        assertNotEquals(hash, project2.hashCode());

        // commonWorkFlow has no setter
        //Project project3 = new Project();
        //project3.setCommonWorkFlow();
        //assertNotEquals(hash, project3.hashCode());

        Project project4 = new Project();
        project4.setDmsImportCreateProcessFolder(true);
        assertNotEquals(hash, project4.hashCode());

        Project project5 = new Project();
        project5.setDmsImportErrorPath("test");
        assertNotEquals(hash, project5.hashCode());

        Project project6 = new Project();
        project6.setDmsImportImagesPath("test");
        assertNotEquals(hash, project6.hashCode());

        Project project7 = new Project();
        project7.setDmsImportRootPath("test");
        assertNotEquals(hash, project7.hashCode());

        Project project8 = new Project();
        project8.setDmsImportSuccessPath("test");
        assertNotEquals(hash, project8.hashCode());

        Project project9 = new Project();
        project9.setDmsImportTimeOut(1000);
        assertNotEquals(hash, project9.hashCode());

        Project project10 = new Project();
        project10.setEndDate(new Date());
        assertNotEquals(hash, project10.hashCode());

        Project project11 = new Project();
        project11.setFileFormatDmsExport("test");
        assertNotEquals(hash, project11.hashCode());

        Project project12 = new Project();
        project12.setFileFormatInternal("test");
        assertNotEquals(hash, project12.hashCode());

        Project project13 = new Project();
        List<ProjectFileGroup> groups = new ArrayList<>();
        groups.add(new ProjectFileGroup());
        project13.setFilegroups(groups);
        assertNotEquals(hash, project13.hashCode());

        Project project14 = new Project();
        project14.setId(1);
        assertNotEquals(hash, project14.hashCode());

        Project project15 = new Project();
        project15.setMetsContentIDs("test");
        assertNotEquals(hash, project15.hashCode());

        Project project16 = new Project();
        project16.setMetsDigiprovPresentation("test");
        assertNotEquals(hash, project16.hashCode());

        Project project17 = new Project();
        project17.setMetsDigiprovPresentationAnchor("test");
        assertNotEquals(hash, project17.hashCode());

        Project project18 = new Project();
        project18.setMetsDigiprovReference("test");
        assertNotEquals(hash, project18.hashCode());

        Project project19 = new Project();
        project19.setMetsDigiprovReferenceAnchor("test");
        assertNotEquals(hash, project19.hashCode());

        Project project20 = new Project();
        project20.setMetsPointerPath("test");
        assertNotEquals(hash, project20.hashCode());

        Project project21 = new Project();
        project21.setMetsPointerPathAnchor("test");
        assertNotEquals(hash, project21.hashCode());

        Project project22 = new Project();
        project22.setMetsPurl("test");
        assertNotEquals(hash, project22.hashCode());

        Project project23 = new Project();
        project23.setMetsRightsLicense("test");
        assertNotEquals(hash, project23.hashCode());

        Project project24 = new Project();
        project24.setMetsRightsOwner("test");
        assertNotEquals(hash, project24.hashCode());

        Project project25 = new Project();
        project25.setMetsRightsOwnerLogo("test");
        assertNotEquals(hash, project25.hashCode());

        Project project26 = new Project();
        project26.setMetsRightsOwnerMail("test");
        assertNotEquals(hash, project26.hashCode());

        Project project27 = new Project();
        project27.setMetsRightsOwnerSite("test");
        assertNotEquals(hash, project27.hashCode());

        Project project28 = new Project();
        project28.setMetsRightsSponsor("test");
        assertNotEquals(hash, project28.hashCode());

        Project project29 = new Project();
        project29.setMetsRightsSponsorLogo("test");
        assertNotEquals(hash, project29.hashCode());

        Project project30 = new Project();
        project30.setMetsRightsSponsorSiteURL("test");
        assertNotEquals(hash, project30.hashCode());

        Project project31 = new Project();
        project31.setNumberOfPages(1000);
        assertNotEquals(hash, project31.hashCode());

        Project project32 = new Project();
        project32.setNumberOfVolumes(100);
        assertNotEquals(hash, project32.hashCode());

        Project project33 = new Project();
        project33.setProjectIsArchived(true);
        assertNotEquals(hash, project33.hashCode());

        Project project34 = new Project();
        List<Process> processes = new ArrayList<>();
        processes.add(new Process());
        project34.setProzesse(processes);
        assertNotEquals(hash, project34.hashCode());

        Project project35 = new Project();
        project35.setStartDate(new Date());
        assertNotEquals(hash, project35.hashCode());

        Project project36 = new Project();
        project36.setTitel("test");
        assertNotEquals(hash, project36.hashCode());

        Project project37 = new Project();
        project37.setUseDmsImport(true);
        assertNotEquals(hash, project37.hashCode());
    }

    public void testCloneConstructor() {
        // TODO: The clone constructor can not be tested without database access
    }

    @Test
    public void testGetEntryType() {
        assertEquals(JournalEntry.EntryType.PROJECT, new Project().getEntryType());
    }

    public void testCloneProjectTitleWithoutNameConflict() {
        // TODO: This method can not be tested without accessing the database
    }

}
