package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertEquals(1,  user.getId().intValue());
    }

    @Test
    public void testVorname() throws Exception {

    }

    @Test
    public void testNachname() throws Exception {

    }

    @Test
    public void testLogin() throws Exception {

    }

    @Test
    public void testLdaplogin() throws Exception {

    }

    @Test
    public void testPasswort() throws Exception {

    }

    @Test
    public void testStandort() throws Exception {

    }

    @Test
    public void testTabellengroesse() throws Exception {

    }

    @Test
    public void testSessiontimeout() throws Exception {

    }

    @Test
    public void testMetadatenSprache() throws Exception {

    }

    @Test
    public void testBenutzergruppen() throws Exception {

    }

    @Test
    public void testSchritte() throws Exception {

    }

    @Test
    public void testBearbeitungsschritte() throws Exception {

    }

    @Test
    public void testProjekte() throws Exception {

    }

    @Test
    public void testEigenschaften() throws Exception {

    }

    @Test
    public void testMitMassendownload() throws Exception {

    }

    @Test
    public void testLdapGruppe() throws Exception {

    }

    @Test
    public void testCss() throws Exception {

    }

    @Test
    public void testEmail() throws Exception {

    }

    @Test
    public void testShortcutPrefix() throws Exception {

    }

    @Test
    public void testEncryptedPassword() throws Exception {

    }

    @Test
    public void testPasswordSalt() throws Exception {

    }

    @Test
    public void testDisplayDeactivatedProjects() throws Exception {

    }

    @Test
    public void testDisplayFinishedProcesses() throws Exception {

    }

    @Test
    public void testDisplaySelectBoxes() throws Exception {

    }

    @Test
    public void testDisplayIdColumn() throws Exception {

    }

    @Test
    public void testDisplayBatchColumn() throws Exception {

    }

    @Test
    public void testDisplayProcessDateColumn() throws Exception {

    }

    @Test
    public void testDisplayLocksColumn() throws Exception {

    }

    @Test
    public void testDisplaySwappingColumn() throws Exception {

    }

    @Test
    public void testDisplayModulesColumn() throws Exception {

    }

    @Test
    public void testDisplayMetadataColumn() throws Exception {

    }

    @Test
    public void testDisplayThumbColumn() throws Exception {

    }

    @Test
    public void testDisplayGridView() throws Exception {

    }

    @Test
    public void testDisplayRulesetColumn() throws Exception {

    }

    @Test
    public void testDisplayAutomaticTasks() throws Exception {

    }

    @Test
    public void testHideCorrectionTasks() throws Exception {

    }

    @Test
    public void testDisplayOnlySelectedTasks() throws Exception {

    }

    @Test
    public void testDisplayOnlyOpenTasks() throws Exception {

    }

    @Test
    public void testDisplayOtherTasks() throws Exception {

    }

    @Test
    public void testMetsDisplayTitle() throws Exception {

    }

    @Test
    public void testMetsLinkImage() throws Exception {

    }

    @Test
    public void testMetsDisplayPageAssignments() throws Exception {

    }

    @Test
    public void testMetsDisplayHierarchy() throws Exception {

    }

    @Test
    public void testMetsDisplayProcessID() throws Exception {

    }

    @Test
    public void testMetsEditorTime() throws Exception {

    }

    @Test
    public void testIMAGE_SIZE() throws Exception {

    }

    @Test
    public void testCustomColumns() throws Exception {

    }

    @Test
    public void testCustomCss() throws Exception {

    }

    @Test
    public void testMailNotificationLanguage() throws Exception {

    }

    @Test
    public void testEmailConfiguration() throws Exception {

    }

    @Test
    public void testInstitution() throws Exception {

    }

    @Test
    public void testInstitutionId() throws Exception {

    }

    @Test
    public void testSuperAdmin() throws Exception {

    }

    @Test
    public void testDisplayInstitutionColumn() throws Exception {

    }

    @Test
    public void testDashboardPlugin() throws Exception {

    }

    @Test
    public void testSsoId() throws Exception {

    }

    @Test
    public void testProcessListDefaultSortField() throws Exception {

    }

    @Test
    public void testProcessListDefaultSortOrder() throws Exception {

    }

    @Test
    public void testTaskListDefaultSortingField() throws Exception {

    }

    public void testTaskListDefaultSortOrder() throws Exception {

    }

    @Test
    public void testDisplayLastEditionDate() throws Exception {

    }

    @Test
    public void testDisplayLastEditionUser() throws Exception {

    }

    @Test
    public void testDisplayLastEditionTask() throws Exception {

    }

    @Test
    public void testDashboardConfiguration() throws Exception {

    }

    @Test
    public void testUiMode() throws Exception {

    }

    @Test
    public void testStatus() throws Exception {

    }

    @Test
    public void testAvailableUiModes() throws Exception {

    }

    @Test
    public void testAdditionalData() throws Exception {

    }

}
