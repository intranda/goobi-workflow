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

package org.goobi.managedbeans;

import java.util.Collections;

import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.UserManager;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UserBeanPasswordResetTest extends AbstractTest {

    private UserBean bean;
    private User userToReset;
    private User superAdmin;
    private ConfigurationHelper config;

    @BeforeEach
    void setUp() {
        bean = new UserBean();

        superAdmin = Mockito.mock(User.class);
        Mockito.when(superAdmin.isSuperAdmin()).thenReturn(true);
        Mockito.when(superAdmin.getAllUserRoles()).thenReturn(Collections.emptyList());

        config = Mockito.mock(ConfigurationHelper.class);
        Mockito.when(config.getMinimumPasswordLength()).thenReturn(8);

        Ldap ldapGroup = Mockito.mock(Ldap.class);
        Mockito.when(ldapGroup.getAuthenticationTypeEnum()).thenReturn(AuthenticationType.LDAP);
        Mockito.when(ldapGroup.isReadonly()).thenReturn(false);

        userToReset = new User();
        userToReset.setId(5);
        userToReset.setLogin("testuser");
        userToReset.setVorname("Test");
        userToReset.setNachname("User");
        userToReset.setLdapGruppe(ldapGroup);
    }

    @Test
    void createNewRandomPasswordForLdapUserDoesNotSaveWhenDirectoryChangeFails() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<ConfigurationHelper> mockedConfig = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<LdapAuthentication> mockedLdap = Mockito.mockConstruction(LdapAuthentication.class,
                        (mock, context) -> Mockito.when(mock.changeUserPassword(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(false))) {

            mockedHelper.when(Helper::getCurrentUser).thenReturn(superAdmin);
            mockedHelper.when(() -> Helper.getRequestParameter("ID")).thenReturn("5");
            mockedUserManager.when(() -> UserManager.getUserById(5)).thenReturn(userToReset);
            mockedConfig.when(ConfigurationHelper::getInstance).thenReturn(config);

            bean.createNewRandomPasswordForUser();

            mockedUserManager.verify(() -> UserManager.saveUser(Mockito.any()), Mockito.never());
            mockedHelper.verify(() -> Helper.setMeldung(Mockito.anyString()), Mockito.never());
            mockedHelper.verify(() -> Helper.setFehlerMeldung("ldapPasswordChangeFailed"), Mockito.times(1));
        }
    }

    @Test
    void createNewRandomPasswordForLdapUserSavesWhenDirectoryChangeSucceeds() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<ConfigurationHelper> mockedConfig = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<LdapAuthentication> mockedLdap = Mockito.mockConstruction(LdapAuthentication.class,
                        (mock, context) -> Mockito.when(mock.changeUserPassword(Mockito.any(), Mockito.any(), Mockito.any()))
                                .thenReturn(true))) {

            mockedHelper.when(Helper::getCurrentUser).thenReturn(superAdmin);
            mockedHelper.when(() -> Helper.getRequestParameter("ID")).thenReturn("5");
            mockedUserManager.when(() -> UserManager.getUserById(5)).thenReturn(userToReset);
            mockedConfig.when(ConfigurationHelper::getInstance).thenReturn(config);

            bean.createNewRandomPasswordForUser();

            mockedUserManager.verify(() -> UserManager.saveUser(userToReset), Mockito.times(1));
            mockedHelper.verify(() -> Helper.setMeldung(Mockito.anyString()), Mockito.times(1));
        }
    }
}
