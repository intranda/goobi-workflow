/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi-workflow
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
package de.sub.goobi.helper.ldap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LdapAuthenticationTest extends AbstractTest {

    private User buildLdapUser() {
        Ldap ldap = Mockito.mock(Ldap.class);
        Mockito.when(ldap.isReadonly()).thenReturn(false);
        Mockito.when(ldap.isUseSsl()).thenReturn(false);
        Mockito.when(ldap.getLdapUrl()).thenReturn("ldap://localhost:389");
        Mockito.when(ldap.getAdminLogin()).thenReturn("cn=admin,dc=example,dc=org");
        Mockito.when(ldap.getAdminPassword()).thenReturn("adminpassword");
        Mockito.when(ldap.getEncryptionType()).thenReturn("SHA256");
        Mockito.when(ldap.getUserDN()).thenReturn("uid={login},dc=example,dc=org");

        User user = new User();
        user.setLogin("testuser");
        user.setVorname("Test");
        user.setNachname("User");
        user.setLdapGruppe(ldap);
        return user;
    }

    @Test
    void changeUserPasswordSendsNoNullModificationItemToDirectory() throws Exception {
        ArgumentCaptor<ModificationItem[]> captor = ArgumentCaptor.forClass(ModificationItem[].class);

        try (MockedConstruction<InitialDirContext> mockedContext = Mockito.mockConstruction(InitialDirContext.class)) {
            new LdapAuthentication().changeUserPassword(buildLdapUser(), null, "newPassword");

            InitialDirContext ctx = mockedContext.constructed().get(0);
            Mockito.verify(ctx).modifyAttributes(Mockito.anyString(), captor.capture());
        }

        ModificationItem[] mods = captor.getValue();
        for (int i = 0; i < mods.length; i++) {
            assertNotNull(mods[i], "modification item at index " + i + " must not be null");
        }
    }

    @Test
    void changeUserPasswordReturnsTrueWhenDirectoryAcceptsModification() throws Exception {
        try (MockedConstruction<InitialDirContext> mockedContext = Mockito.mockConstruction(InitialDirContext.class)) {
            assertTrue(new LdapAuthentication().changeUserPassword(buildLdapUser(), null, "newPassword"));
        }
    }

    @Test
    void changeUserPasswordReturnsFalseWhenDirectoryRejectsModification() throws Exception {
        try (MockedConstruction<InitialDirContext> mockedContext = Mockito.mockConstruction(InitialDirContext.class,
                (mock, context) -> Mockito.doThrow(new NamingException("[LDAP: error code 49 - invalid credentials]"))
                        .when(mock)
                        .modifyAttributes(Mockito.anyString(), Mockito.any(ModificationItem[].class)))) {
            assertFalse(new LdapAuthentication().changeUserPassword(buildLdapUser(), null, "newPassword"));
        }
    }

    @Test
    void changeUserPasswordReturnsFalseForReadonlyLdapGroup() throws Exception {
        User user = buildLdapUser();
        Mockito.when(user.getLdapGruppe().isReadonly()).thenReturn(true);

        assertFalse(new LdapAuthentication().changeUserPassword(user, null, "newPassword"));
    }
}
