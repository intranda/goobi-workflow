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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.api.rest.AuthenticationToken;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.User.UserStatus;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class UserTest extends AbstractTest {

    @Test
    public void testConstructor() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    public void testId() throws Exception {
        User user = new User();
        assertNull(user.getId());
        user.setId(1);
        assertEquals(1, user.getId().intValue());
    }

    @Test
    public void testVorname() throws Exception {
        User user = new User();
        assertNull(user.getVorname());
        user.setVorname("fixture");
        assertEquals("fixture", user.getVorname());

    }

    @Test
    public void testNachname() throws Exception {
        User user = new User();
        assertNull(user.getNachname());
        user.setNachname("fixture");
        assertEquals("fixture", user.getNachname());
    }

    @Test
    public void testLogin() throws Exception {
        User user = new User();
        assertNull(user.getLogin());
        user.setLogin("fixture");
        assertEquals("fixture", user.getLogin());
    }

    @Test
    public void testLdaplogin() throws Exception {
        User user = new User();
        assertNull(user.getLdaplogin());
        user.setLdaplogin("fixture");
        assertEquals("fixture", user.getLdaplogin());
    }

    @Test
    public void testPasswort() throws Exception {
        User user = new User();
        assertNull(user.getPasswort());
        user.setPasswort("fixture");
        assertEquals("fixture", user.getPasswort());
    }

    @Test
    public void testStandort() throws Exception {
        User user = new User();
        assertNull(user.getStandort());
        user.setStandort("fixture");
        assertEquals("fixture", user.getStandort());
    }

    @Test
    public void testTabellengroesse() throws Exception {
        User user = new User();
        assertEquals(10, user.getTabellengroesse().intValue());

        user.setTabellengroesse(null);
        assertEquals(10, user.getTabellengroesse().intValue());

        user.setTabellengroesse(20);
        assertEquals(20, user.getTabellengroesse().intValue());
    }

    @Test
    public void testSessiontimeout() throws Exception {
        User user = new User();
        assertEquals(14400, user.getSessiontimeout().intValue());

        user.setSessiontimeout(null);
        assertEquals(14400, user.getSessiontimeout().intValue());

        user.setSessiontimeout(20);
        assertEquals(20, user.getSessiontimeout().intValue());
    }

    @Test
    public void testMetadatenSprache() throws Exception {
        User user = new User();
        assertNull(user.getMetadatenSprache());
        user.setMetadatenSprache("fixture");
        assertEquals("fixture", user.getMetadatenSprache());
    }

    @Test
    public void testBenutzergruppen() throws Exception {
        User user = new User();
        List<Usergroup> ugList = new ArrayList<>();
        Usergroup grp = new Usergroup();
        ugList.add(grp);
        user.setBenutzergruppen(ugList);
        assertEquals(1, user.getBenutzergruppen().size());
        assertEquals(1, user.getBenutzergruppenSize());
    }

    @Test
    public void testSchritte() throws Exception {
        User user = new User();
        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        stepList.add(step);
        user.setSchritte(stepList);
        assertEquals(1, user.getSchritte().size());
        assertEquals(1, user.getSchritteSize());
    }

    @Test
    public void testBearbeitungsschritte() throws Exception {
        User user = new User();
        List<Step> stepList = new ArrayList<>();
        Step step = new Step();
        stepList.add(step);
        user.setBearbeitungsschritte(stepList);
        assertEquals(1, user.getBearbeitungsschritte().size());
        assertEquals(1, user.getBearbeitungsschritteSize());
    }

    @Test
    public void testProjekte() throws Exception {
        User user = new User();
        List<Project> list = new ArrayList<>();
        Project p = new Project();
        list.add(p);
        user.setProjekte(list);
        assertEquals(1, user.getProjekte().size());
        assertEquals(1, user.getProjekteSize());
    }

    @Test
    public void testEigenschaften() throws Exception {
        User user = new User();
        List<UserProperty> list = new ArrayList<>();
        UserProperty p = new UserProperty();
        list.add(p);
        user.setEigenschaften(list);
        assertEquals(1, user.getEigenschaften().size());
        assertEquals(1, user.getEigenschaftenSize());
    }

    @Test
    public void testMitMassendownload() throws Exception {
        User user = new User();
        assertFalse(user.isMitMassendownload());
        user.setMitMassendownload(true);
        assertTrue(user.isMitMassendownload());
    }

    @Test
    public void testLdapGruppe() throws Exception {
        User user = new User();
        assertNull(user.getLdapGruppe());
        Ldap ldap = new Ldap();
        user.setLdapGruppe(ldap);
        assertEquals(ldap, user.getLdapGruppe());
    }

    @Test
    public void testCss() throws Exception {
        User user = new User();
        assertEquals("/css/default.css", user.getCss());
        user.setCss("fixture");
        assertEquals("fixture", user.getCss());
    }

    @Test
    public void testEmail() throws Exception {
        User user = new User();
        assertNull(user.getEmail());
        user.setEmail("fixture@example.com");
        assertEquals("fixture@example.com", user.getEmail());
    }

    @Test
    public void testShortcutPrefix() throws Exception {
        User user = new User();
        assertEquals("ctrl+shift", user.getShortcutPrefix());
        user.setShortcutPrefix("fixture");
        assertEquals("fixture", user.getShortcutPrefix());
    }

    @Test
    public void testEncryptedPassword() throws Exception {
        User user = new User();
        assertNull(user.getEncryptedPassword());
        user.setEncryptedPassword("fixture");
        assertEquals("fixture", user.getEncryptedPassword());
    }

    @Test
    public void testPasswordSalt() throws Exception {
        User user = new User();
        assertNull(user.getPasswordSalt());
        user.setPasswordSalt("fixture");
        assertEquals("fixture", user.getPasswordSalt());
    }

    @Test
    public void testDisplayDeactivatedProjects() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayDeactivatedProjects());
        user.setDisplayDeactivatedProjects(true);
        assertTrue(user.isDisplayDeactivatedProjects());
    }

    @Test
    public void testDisplayFinishedProcesses() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayFinishedProcesses());
        user.setDisplayFinishedProcesses(true);
        assertTrue(user.isDisplayFinishedProcesses());
    }

    @Test
    public void testDisplaySelectBoxes() throws Exception {
        User user = new User();
        assertFalse(user.isDisplaySelectBoxes());
        user.setDisplaySelectBoxes(true);
        assertTrue(user.isDisplaySelectBoxes());
    }

    @Test
    public void testDisplayIdColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayIdColumn());
        user.setDisplayIdColumn(true);
        assertTrue(user.isDisplayIdColumn());
    }

    @Test
    public void testDisplayBatchColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayBatchColumn());
        user.setDisplayBatchColumn(true);
        assertTrue(user.isDisplayBatchColumn());
    }

    @Test
    public void testDisplayProcessDateColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayProcessDateColumn());
        user.setDisplayProcessDateColumn(true);
        assertTrue(user.isDisplayProcessDateColumn());
    }

    @Test
    public void testDisplayLocksColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayLocksColumn());
        user.setDisplayLocksColumn(true);
        assertTrue(user.isDisplayLocksColumn());
    }

    @Test
    public void testDisplaySwappingColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplaySwappingColumn());
        user.setDisplaySwappingColumn(true);
        assertTrue(user.isDisplaySwappingColumn());
    }

    @Test
    public void testDisplayModulesColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayModulesColumn());
        user.setDisplayModulesColumn(true);
        assertTrue(user.isDisplayModulesColumn());
    }

    @Test
    public void testDisplayMetadataColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayMetadataColumn());
        user.setDisplayMetadataColumn(true);
        assertTrue(user.isDisplayMetadataColumn());
    }

    @Test
    public void testDisplayThumbColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayThumbColumn());
        user.setDisplayThumbColumn(true);
        assertTrue(user.isDisplayThumbColumn());
    }

    @Test
    public void testDisplayGridView() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayGridView());
        user.setDisplayGridView(true);
        assertTrue(user.isDisplayGridView());
    }

    @Test
    public void testDisplayRulesetColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayRulesetColumn());
        user.setDisplayRulesetColumn(true);
        assertTrue(user.isDisplayRulesetColumn());
    }

    @Test
    public void testDisplayAutomaticTasks() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayAutomaticTasks());
        user.setDisplayAutomaticTasks(true);
        assertTrue(user.isDisplayAutomaticTasks());
    }

    @Test
    public void testHideCorrectionTasks() throws Exception {
        User user = new User();
        assertFalse(user.isHideCorrectionTasks());
        user.setHideCorrectionTasks(true);
        assertTrue(user.isHideCorrectionTasks());
    }

    @Test
    public void testDisplayOnlySelectedTasks() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayOnlySelectedTasks());
        user.setDisplayOnlySelectedTasks(true);
        assertTrue(user.isDisplayOnlySelectedTasks());
    }

    @Test
    public void testDisplayOnlyOpenTasks() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayOnlyOpenTasks());
        user.setDisplayOnlyOpenTasks(true);
        assertTrue(user.isDisplayOnlyOpenTasks());
    }

    @Test
    public void testDisplayOtherTasks() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayOtherTasks());
        user.setDisplayOtherTasks(true);
        assertTrue(user.isDisplayOtherTasks());
    }

    @Test
    public void testMetsDisplayTitle() throws Exception {
        User user = new User();
        assertFalse(user.isMetsDisplayTitle());
        user.setMetsDisplayTitle(true);
        assertTrue(user.isMetsDisplayTitle());
    }

    @Test
    public void testMetsLinkImage() throws Exception {
        User user = new User();
        assertFalse(user.isMetsLinkImage());
        user.setMetsLinkImage(true);
        assertTrue(user.isMetsLinkImage());
    }

    @Test
    public void testMetsDisplayPageAssignments() throws Exception {
        User user = new User();
        assertFalse(user.isMetsDisplayPageAssignments());
        user.setMetsDisplayPageAssignments(true);
        assertTrue(user.isMetsDisplayPageAssignments());
    }

    @Test
    public void testMetsDisplayHierarchy() throws Exception {
        User user = new User();
        assertFalse(user.isMetsDisplayHierarchy());
        user.setMetsDisplayHierarchy(true);
        assertTrue(user.isMetsDisplayHierarchy());
    }

    @Test
    public void testMetsDisplayProcessID() throws Exception {
        User user = new User();
        assertFalse(user.isMetsDisplayProcessID());
        user.setMetsDisplayProcessID(true);
        assertTrue(user.isMetsDisplayProcessID());
    }

    @Test
    public void testMetsEditorTime() throws Exception {
        User user = new User();
        assertNull(user.getMetsEditorTime());
        user.setMetsEditorTime(10);
        assertEquals(10, user.getMetsEditorTime().intValue());
    }

    @Test
    public void testIMAGE_SIZE() throws Exception {
        assertEquals(27, User.getIMAGE_SIZE());
    }

    @Test
    public void testCustomColumns() throws Exception {
        User user = new User();
        assertNull(user.getCustomColumns());
        user.setCustomColumns("fixture");
        assertEquals("fixture", user.getCustomColumns());
    }

    @Test
    public void testCustomCss() throws Exception {
        User user = new User();
        assertNull(user.getCustomCss());
        user.setCustomCss("fixture");
        assertEquals("fixture", user.getCustomCss());
    }

    @Test
    public void testMailNotificationLanguage() throws Exception {
        User user = new User();
        assertNull(user.getMailNotificationLanguage());
        user.setMailNotificationLanguage("fixture");
        assertEquals("fixture", user.getMailNotificationLanguage());
    }

    @Test
    public void testEmailConfiguration() throws Exception {
        User user = new User();
        List<UserProjectConfiguration> list = new ArrayList<>();
        UserProjectConfiguration p = new UserProjectConfiguration();
        list.add(p);
        user.setEmailConfiguration(list);
        assertEquals(1, user.getEmailConfiguration().size());
    }

    @Test
    public void testInstitution() throws Exception {
        User user = new User();
        Institution in = new Institution();
        user.setInstitution(in);
        assertEquals(in, user.getInstitution());
    }

    @Test
    public void testInstitutionId() throws Exception {
        User user = new User();
        user.setInstitutionId(1);
        assertEquals(1, user.getInstitutionId().intValue());
    }

    @Test
    public void testSuperAdmin() throws Exception {
        User user = new User();
        assertFalse(user.isSuperAdmin());
        user.setSuperAdmin(true);
        assertTrue(user.isSuperAdmin());
    }

    @Test
    public void testDisplayInstitutionColumn() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayInstitutionColumn());
        user.setDisplayInstitutionColumn(true);
        assertTrue(user.isDisplayInstitutionColumn());
    }

    @Test
    public void testDashboardPlugin() throws Exception {
        User user = new User();
        assertNull(user.getDashboardPlugin());
        user.setDashboardPlugin("fixture");
        assertEquals("fixture", user.getDashboardPlugin());
    }

    @Test
    public void testSsoId() throws Exception {
        User user = new User();
        assertNull(user.getSsoId());
        user.setSsoId("fixture");
        assertEquals("fixture", user.getSsoId());
    }

    @Test
    public void testProcessListDefaultSortField() throws Exception {
        User user = new User();
        assertEquals("titel", user.getProcessListDefaultSortField());
        user.setProcessListDefaultSortField("fixture");
        assertEquals("fixture", user.getProcessListDefaultSortField());
    }

    @Test
    public void testProcessListDefaultSortOrder() throws Exception {
        User user = new User();
        assertEquals(" asc", user.getProcessListDefaultSortOrder());
        user.setProcessListDefaultSortOrder(" desc");
        assertEquals(" desc", user.getProcessListDefaultSortOrder());
    }

    @Test
    public void testTaskListDefaultSortingField() throws Exception {
        User user = new User();
        assertEquals("prioritaet", user.getTaskListDefaultSortingField());
        user.setTaskListDefaultSortingField("fixture");
        assertEquals("fixture", user.getTaskListDefaultSortingField());
    }

    @Test
    public void testTaskListDefaultSortOrder() throws Exception {
        User user = new User();
        assertEquals(" desc", user.getTaskListDefaultSortOrder());
        user.setTaskListDefaultSortOrder("Asc");
        assertEquals("Asc", user.getTaskListDefaultSortOrder());
    }

    @Test
    public void testDisplayLastEditionDate() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayLastEditionDate());
        user.setDisplayLastEditionDate(true);
        assertTrue(user.isDisplayLastEditionDate());
    }

    @Test
    public void testDisplayLastEditionUser() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayLastEditionUser());
        user.setDisplayLastEditionUser(true);
        assertTrue(user.isDisplayLastEditionUser());
    }

    @Test
    public void testDisplayLastEditionTask() throws Exception {
        User user = new User();
        assertFalse(user.isDisplayLastEditionTask());
        user.setDisplayLastEditionTask(true);
        assertTrue(user.isDisplayLastEditionTask());
    }

    @Test
    public void testDashboardConfiguration() throws Exception {
        User user = new User();
        assertNull(user.getDashboardConfiguration());
        user.setDashboardConfiguration("fixture");
        assertEquals("fixture", user.getDashboardConfiguration());
    }

    @Test
    public void testUiMode() throws Exception {
        User user = new User();
        assertNull(user.getUiMode());
        user.setUiMode("fixture");
        assertEquals("fixture", user.getUiMode());
    }

    @Test
    public void testAdditionalSearchFields() throws Exception {
        User user = new User();
        assertNull(user.getAdditionalSearchFields());
        user.setAdditionalSearchFields("fixture");
        assertEquals("fixture", user.getAdditionalSearchFields());
    }

    @Test
    public void testStatus() throws Exception {
        User user = new User();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        user.setStatus(UserStatus.DELETED);
        assertEquals(UserStatus.DELETED, user.getStatus());
    }

    @Test
    public void testApiToken() throws Exception {
        User user = new User();
        assertTrue(user.getApiToken().isEmpty());
        List<AuthenticationToken> apiToken = new ArrayList<>();
        apiToken.add(new AuthenticationToken("", -1));
        user.setApiToken(apiToken);
        assertFalse(user.getApiToken().isEmpty());
    }

    @Test
    public void testToken() {
        User user = new User();
        assertNull(user.getToken());
        AuthenticationToken token = new AuthenticationToken();
        user.setToken(token);
        assertNotNull(user.getToken());
        assertSame(user.getToken(), token);
    }

    @Test
    public void testAdditionalData() throws Exception {
        User user = new User();
        assertTrue(user.getAdditionalData().isEmpty());
        Map<String, String> additionalData = new HashMap<>();
        additionalData.put("fixture", "fixture");
        user.setAdditionalData(additionalData);
        assertFalse(user.getAdditionalData().isEmpty());
    }

    @Test
    public void testPasswortCrypt() throws Exception {
        String password = "fixture";
        User user = new User();
        user.setPasswortCrypt(password);
        String crypt = user.getPasswort();
        assertEquals("z4DxcITxhsc=", crypt);
        assertEquals(password, user.getPasswortCrypt());
    }

    @Test
    public void testPasswortKorrekt() throws Exception {
        User user = new User();
        Ldap ldap = new Ldap();
        ldap.setAuthenticationType("database");
        user.setLdapGruppe(ldap);
        user.setPasswordSalt("salt");
        user.setEncryptedPassword("MymTz74KfogfA3Uymyp+l+MRZvF4nJgeM/4qPZWMwsc=");
        assertFalse(user.istPasswortKorrekt(null));
        assertFalse(user.istPasswortKorrekt(""));
        assertFalse(user.istPasswortKorrekt("wrong"));
        assertTrue(user.istPasswortKorrekt("fixture"));
    }

    @Test
    public void testPasswordHash() throws Exception {
        User user = new User();
        user.setPasswordSalt("salt");
        assertEquals("MymTz74KfogfA3Uymyp+l+MRZvF4nJgeM/4qPZWMwsc=", user.getPasswordHash("fixture"));
    }

    @Test
    public void testGetNachVorname() {
        User user = new User();
        assertEquals("null, null", user.getNachVorname());
        user.setVorname("John");
        user.setNachname("Doe");
        assertEquals("Doe, John", user.getNachVorname());
        user.setVorname("");
        user.setNachname("");
        assertEquals(", ", user.getNachVorname());
    }

    @Test
    public void testFirstProjectTitle() throws Exception {
        User user = new User();
        assertEquals("", user.getFirstProjectTitle());
        List<Project> list = new ArrayList<>();
        user.setProjekte(list);
        assertEquals("", user.getFirstProjectTitle());
        Project p = new Project();
        p.setTitel("fixture");
        list.add(p);
        assertEquals("fixture", user.getFirstProjectTitle());
    }

    @Test
    public void testFirstUserGroupTitle() throws Exception {
        User user = new User();
        assertEquals("", user.getFirstUserGroupTitle());
        List<Usergroup> list = new ArrayList<>();
        user.setBenutzergruppen(list);
        assertEquals("", user.getFirstUserGroupTitle());
        Usergroup p = new Usergroup();
        p.setTitel("fixture");
        list.add(p);
        assertEquals("fixture", user.getFirstUserGroupTitle());
    }

    @Test
    public void testInstitutionName() throws Exception {
        User user = new User();
        Institution in = new Institution();
        user.setInstitution(in);
        in.setShortName("fixture");
        assertEquals("fixture", user.getInstitutionName());
    }

    @Test
    public void testSessiontimeoutInMinutes() throws Exception {
        User user = new User();
        assertEquals(240, user.getSessiontimeoutInMinutes().intValue());
        // 1 is not allowed, 5 is the lowest value
        user.setSessiontimeoutInMinutes(1);
        assertEquals(5, user.getSessiontimeoutInMinutes().intValue());

        user.setSessiontimeoutInMinutes(10);
        assertEquals(10, user.getSessiontimeoutInMinutes().intValue());
    }

    @Test
    public void testGetEigenschaftenSize() {
        User user = new User();
        assertEquals(0, user.getEigenschaftenSize());
        List<UserProperty> list = new ArrayList<>();
        UserProperty p1 = new UserProperty();
        UserProperty p2 = new UserProperty();
        list.add(p1);
        list.add(p2);
        user.setEigenschaften(list);
        assertEquals(2, user.getEigenschaften().size());
        assertEquals(2, user.getEigenschaftenSize());
    }

    @Test
    public void testSelfDestruct() throws Exception {
        User user = new User();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        user.selfDestruct();
        assertEquals(UserStatus.DELETED, user.getStatus());
        // These values are the most important values that should be "removed":
        assertNull(user.getLogin());
        assertNull(user.getVorname());
        assertNull(user.getNachname());
        assertNull(user.getStandort());
    }

    @Test
    public void testHashCode() {
        User user1 = new User();
        user1.setId(0);
        user1.setLogin("goobi");
        user1.setNachname("Lastname");
        user1.setVorname("Firstname");
        User user2 = new User();
        user2.setId(1);
        User user3 = new User();
        user3.setLogin("login");
        User user4 = new User();
        user4.setNachname("lastname");
        User user5 = new User();
        user5.setVorname("firstname");
        // test whether user1 is equal to itself, but all others are different
        assertEquals(user1.hashCode(), user1.hashCode());
        assertNotEquals(user1.hashCode(), user2.hashCode());// different id
        assertNotEquals(user1.hashCode(), user3.hashCode());// different login
        assertNotEquals(user1.hashCode(), user4.hashCode());// different last name
        assertNotEquals(user1.hashCode(), user5.hashCode());// different first name
    }

    @Test
    public void testEquals() {
        User user1 = new User();
        user1.setId(0);
        user1.setLogin("goobi");
        user1.setNachname("Lastname");
        user1.setVorname("Firstname");
        User user2 = new User();
        user2.setId(1);
        User user3 = new User();
        user3.setLogin("login");
        User user4 = new User();
        user4.setNachname("lastname");
        User user5 = new User();
        user5.setVorname("firstname");
        // test whether user1 is equal to itself, but all others are different
        assertEquals(user1, user1);
        assertNotEquals(user1, user2);// different id
        assertNotEquals(user1, user3);// different login
        assertNotEquals(user1, user4);// different last name
        assertNotEquals(user1, user5);// different first name
    }

    @Test
    public void testImageUrl() throws Exception {
        User user = new User();
        assertNull(user.getImageUrl());
        user.setEmail("user@example.com");

        assertEquals(
                "https://www.gravatar.com/avatar/b58996c504c5638798eb6b511e6f49af.jpg?s=27&d=https://www.gravatar.com/avatar/92bb3cacd091cbee44637e73f2ea1f7c.jpg?s=27",
                user.getImageUrl());

    }

    @Test
    public void testAllUserRoles() throws Exception {
        User user = new User();
        List<Usergroup> ugList = new ArrayList<>();
        user.setBenutzergruppen(ugList);
        Usergroup grp = new Usergroup();
        grp.addUserRole("role");
        ugList.add(grp);
        assertEquals(1, user.getAllUserRoles().size());
        assertEquals("role", user.getAllUserRoles().get(0));
    }

    @Test
    public void testActive() throws Exception {
        User user = new User();
        assertTrue(user.isActive());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        user.setActive(false);
        assertEquals(UserStatus.INACTIVE, user.getStatus());

        user.setActive(true);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }

    @Test
    public void testUserStatus() throws Exception {
        assertEquals(UserStatus.REGISTERED, UserStatus.getStatusByName("registered"));
        assertEquals(UserStatus.ACTIVE, UserStatus.getStatusByName("active"));
        assertEquals(UserStatus.INACTIVE, UserStatus.getStatusByName("inactive"));
        assertEquals(UserStatus.REJECTED, UserStatus.getStatusByName("rejected"));
        assertEquals(UserStatus.DELETED, UserStatus.getStatusByName("deleted"));
        assertEquals(UserStatus.ACTIVE, UserStatus.getStatusByName("wrong"));
    }

    @Test
    public void testDownloadFolder() throws Exception {
        User user = new User();
        user.setLogin("fixture");
        assertTrue(user.getDownloadFolder().toString().endsWith("/fixture"));
    }

    @Test
    public void testEntryType() throws Exception {
        User user = new User();
        assertEquals(EntryType.USER, user.getEntryType());
    }

}
