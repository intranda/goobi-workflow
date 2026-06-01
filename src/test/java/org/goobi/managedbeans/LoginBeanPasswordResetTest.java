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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.goobi.api.mail.SendMail;
import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LoginBeanPasswordResetTest extends AbstractTest {

    private LoginBean bean;
    private SendMail mockSendMail;
    private SendMail.MailConfiguration mockMailConfig;

    @BeforeEach
    void setUp() {
        bean = new LoginBean();

        mockMailConfig = Mockito.mock(SendMail.MailConfiguration.class);
        Mockito.when(mockMailConfig.isEnableMail()).thenReturn(true);
        Mockito.when(mockMailConfig.getApiUrl())
                .thenReturn("https://goobi.example.com/goobi/api/mails/disable");
        Mockito.when(mockMailConfig.getPasswordResetSubject()).thenReturn("Reset your password");
        Mockito.when(mockMailConfig.getPasswordResetBody()).thenReturn("Link: {url}");

        mockSendMail = Mockito.mock(SendMail.class);
        Mockito.when(mockSendMail.getConfig()).thenReturn(mockMailConfig);
    }

    private User buildUser(String login, String email) {
        Ldap ldap = Mockito.mock(Ldap.class);
        Mockito.when(ldap.getAuthenticationTypeEnum()).thenReturn(AuthenticationType.DATABASE);
        User user = new User();
        user.setId(42);
        user.setLogin(login);
        user.setEmail(email);
        user.setLdapGruppe(ldap);
        return user;
    }

    @Test
    void resetPasswordMailDisabledNoTokenCreated() throws Exception {
        Mockito.when(mockMailConfig.isEnableMail()).thenReturn(false);
        bean.setLogin("testuser");

        try (MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class)) {
            mockedSendMail.when(SendMail::getInstance).thenReturn(mockSendMail);
            bean.resetPassword();
            mockedJwt.verify(
                    () -> JwtHelper.createToken(Mockito.any(Map.class), Mockito.any(Date.class)),
                    Mockito.never());
        }
    }

    @Test
    void resetPasswordJwtConfigMissingNoMailSent() throws Exception {
        User user = buildUser("testuser", "test@example.com");
        bean.setLogin("testuser");

        try (MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedSendMail.when(SendMail::getInstance).thenReturn(mockSendMail);
            mockedUserManager.when(() -> UserManager.getUsers(Mockito.isNull(), Mockito.anyString(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                    .thenReturn(Collections.singletonList(user));
            mockedJwt.when(() -> JwtHelper.createToken(Mockito.any(Map.class), Mockito.any(Date.class)))
                    .thenThrow(new javax.naming.ConfigurationException("no jwt secret"));

            bean.resetPassword();

            Mockito.verify(mockSendMail, Mockito.never())
                    .sendMailToUser(Mockito.any(), Mockito.any(), Mockito.anyString());
        }
    }

    @Test
    void resetPasswordSuccessMailBodyContainsResetUrl() throws Exception {
        User user = buildUser("testuser", "test@example.com");
        bean.setLogin("testuser");

        try (MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedSendMail.when(SendMail::getInstance).thenReturn(mockSendMail);
            mockedUserManager.when(() -> UserManager.getUsers(Mockito.isNull(), Mockito.anyString(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                    .thenReturn(Collections.singletonList(user));
            mockedJwt.when(() -> JwtHelper.createToken(Mockito.any(Map.class), Mockito.any(Date.class)))
                    .thenReturn("test-jwt-token");

            bean.resetPassword();

            ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
            Mockito.verify(mockSendMail)
                    .sendMailToUser(
                            Mockito.anyString(), bodyCaptor.capture(), Mockito.eq("test@example.com"));
            String body = bodyCaptor.getValue();
            assertTrue(body.contains("/uii/password_reset.xhtml?token=test-jwt-token"),
                    "Mail body must contain the reset URL with token. Actual body: " + body);
        }
    }

    @Test
    void resetPasswordSuccessDoesNotChangePassword() throws Exception {
        User user = buildUser("testuser", "test@example.com");
        bean.setLogin("testuser");

        try (MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class);
                MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedSendMail.when(SendMail::getInstance).thenReturn(mockSendMail);
            mockedUserManager.when(() -> UserManager.getUsers(Mockito.isNull(), Mockito.anyString(),
                    Mockito.isNull(), Mockito.isNull(), Mockito.isNull()))
                    .thenReturn(Collections.singletonList(user));
            mockedJwt.when(() -> JwtHelper.createToken(Mockito.any(Map.class), Mockito.any(Date.class)))
                    .thenReturn("test-jwt-token");

            bean.resetPassword();

            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
    }
}
