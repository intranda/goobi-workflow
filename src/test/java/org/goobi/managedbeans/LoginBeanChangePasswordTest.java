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
class LoginBeanChangePasswordTest extends AbstractTest {

    private LoginBean bean;
    private User ldapUser;
    private ConfigurationHelper config;

    @BeforeEach
    void setUp() {
        Ldap ldapGroup = Mockito.mock(Ldap.class);
        Mockito.when(ldapGroup.getAuthenticationTypeEnum()).thenReturn(AuthenticationType.LDAP);
        Mockito.when(ldapGroup.isReadonly()).thenReturn(false);

        ldapUser = new User();
        ldapUser.setId(7);
        ldapUser.setLogin("testuser");
        ldapUser.setVorname("Test");
        ldapUser.setNachname("User");
        ldapUser.setLdapGruppe(ldapGroup);

        bean = new LoginBean();
        bean.setMyBenutzer(ldapUser);
        bean.setPasswortAendernAlt("oldPassword");
        bean.setPasswortAendernNeu1("newPassword");
        bean.setPasswortAendernNeu2("newPassword");

        config = Mockito.mock(ConfigurationHelper.class);
        Mockito.when(config.getMinimumPasswordLength()).thenReturn(8);
    }

    @Test
    void changePasswordDoesNotSaveWhenDirectoryChangeFails() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<ConfigurationHelper> mockedConfig = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<LdapAuthentication> mockedLdap = Mockito.mockConstruction(LdapAuthentication.class,
                        (mock, context) -> {
                            Mockito.when(mock.isUserPasswordCorrect(Mockito.any(), Mockito.any())).thenReturn(true);
                            Mockito.when(mock.changeUserPassword(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
                        })) {

            mockedConfig.when(ConfigurationHelper::getInstance).thenReturn(config);
            mockedUserManager.when(() -> UserManager.getUserById(7)).thenReturn(ldapUser);

            bean.PasswortAendernSpeichern();

            mockedUserManager.verify(() -> UserManager.saveUser(Mockito.any()), Mockito.never());
            mockedHelper.verify(() -> Helper.setMeldung("passwortGeaendert"), Mockito.never());
            mockedHelper.verify(() -> Helper.setFehlerMeldung("ldapPasswordChangeFailed"), Mockito.times(1));
        }
    }

    @Test
    void changePasswordSavesWhenDirectoryChangeSucceeds() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<ConfigurationHelper> mockedConfig = Mockito.mockStatic(ConfigurationHelper.class);
                MockedConstruction<LdapAuthentication> mockedLdap = Mockito.mockConstruction(LdapAuthentication.class,
                        (mock, context) -> {
                            Mockito.when(mock.isUserPasswordCorrect(Mockito.any(), Mockito.any())).thenReturn(true);
                            Mockito.when(mock.changeUserPassword(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
                        })) {

            mockedConfig.when(ConfigurationHelper::getInstance).thenReturn(config);
            mockedUserManager.when(() -> UserManager.getUserById(7)).thenReturn(ldapUser);

            bean.PasswortAendernSpeichern();

            mockedUserManager.verify(() -> UserManager.saveUser(ldapUser), Mockito.times(1));
            mockedHelper.verify(() -> Helper.setMeldung("passwortGeaendert"), Mockito.times(1));
        }
    }
}
