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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PasswordResetBeanTest extends AbstractTest {

    private PasswordResetBean bean;

    @BeforeEach
    void setUp() {
        bean = new PasswordResetBean();
    }

    @Test
    void initWithNullTokenTokenInvalid() {
        bean.setToken(null);
        bean.init();
        assertFalse(bean.isTokenValid());
    }

    @Test
    void initWithBlankTokenTokenInvalid() {
        bean.setToken("   ");
        bean.init();
        assertFalse(bean.isTokenValid());
    }

    @Test
    void initWithWrongPurposeTokenInvalid() {
        DecodedJWT jwt = Mockito.mock(DecodedJWT.class);
        Claim purposeClaim = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("purpose")).thenReturn(purposeClaim);
        Mockito.when(purposeClaim.asString()).thenReturn("confirmMail");

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("some-token")).thenReturn(jwt);
            bean.setToken("some-token");
            bean.init();
        }
        assertFalse(bean.isTokenValid());
    }

    @Test
    void initWithExpiredTokenTokenInvalid() {
        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("expired-token"))
                    .thenThrow(new com.auth0.jwt.exceptions.TokenExpiredException("expired", null));
            bean.setToken("expired-token");
            bean.init();
        }
        assertFalse(bean.isTokenValid());
    }

    @Test
    void initWithValidTokenTokenValidAndUserLoaded() {
        User user = new User();
        user.setId(42);

        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");
        Claim userIdClaim = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("userId")).thenReturn(userIdClaim);
        Mockito.when(userIdClaim.asString()).thenReturn("42");

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("valid-token")).thenReturn(jwt);
            mockedUserManager.when(() -> UserManager.getUserById(42)).thenReturn(user);

            bean.setToken("valid-token");
            bean.init();
        }
        assertTrue(bean.isTokenValid());
    }

    @Test
    void initWithValidTokenButUserNotFoundTokenInvalid() {
        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");
        Claim userIdClaim = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("userId")).thenReturn(userIdClaim);
        Mockito.when(userIdClaim.asString()).thenReturn("99");

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("valid-token")).thenReturn(jwt);
            mockedUserManager.when(() -> UserManager.getUserById(99)).thenReturn(null);

            bean.setToken("valid-token");
            bean.init();
        }
        assertFalse(bean.isTokenValid());
    }

    // ---- saveNewPassword tests ----

    @Test
    void saveNewPasswordExpiredTokenDoesNotSave() {
        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims(Mockito.anyString()))
                    .thenThrow(new com.auth0.jwt.exceptions.JWTVerificationException("expired"));
            bean.setToken("expired-token");
            bean.saveNewPassword();
            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
        assertFalse(bean.isPasswordSaved());
    }

    @Test
    void saveNewPasswordPasswordTooShortDoesNotSave() {
        de.sub.goobi.config.ConfigurationHelper mockConfig =
                Mockito.mock(de.sub.goobi.config.ConfigurationHelper.class);
        Mockito.when(mockConfig.getMinimumPasswordLength()).thenReturn(8);

        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");
        bean.user = new User();

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<de.sub.goobi.config.ConfigurationHelper> mockedCfg =
                        Mockito.mockStatic(de.sub.goobi.config.ConfigurationHelper.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("t")).thenReturn(jwt);
            mockedCfg.when(de.sub.goobi.config.ConfigurationHelper::getInstance).thenReturn(mockConfig);

            bean.setToken("t");
            bean.setNewPassword("short");
            bean.setConfirmPassword("short");
            bean.saveNewPassword();

            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
        assertFalse(bean.isPasswordSaved());
    }

    @Test
    void saveNewPasswordPasswordMismatchDoesNotSave() {
        de.sub.goobi.config.ConfigurationHelper mockConfig =
                Mockito.mock(de.sub.goobi.config.ConfigurationHelper.class);
        Mockito.when(mockConfig.getMinimumPasswordLength()).thenReturn(4);

        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");
        bean.user = new User();

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<de.sub.goobi.config.ConfigurationHelper> mockedCfg =
                        Mockito.mockStatic(de.sub.goobi.config.ConfigurationHelper.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("t")).thenReturn(jwt);
            mockedCfg.when(de.sub.goobi.config.ConfigurationHelper::getInstance).thenReturn(mockConfig);

            bean.setToken("t");
            bean.setNewPassword("password1");
            bean.setConfirmPassword("password2");
            bean.saveNewPassword();

            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(Mockito.any(), Mockito.any()),
                    Mockito.never());
        }
        assertFalse(bean.isPasswordSaved());
    }

    @Test
    void saveNewPasswordValidInputDatabaseUserSavesPassword() {
        org.goobi.beans.Ldap ldap = Mockito.mock(org.goobi.beans.Ldap.class);
        Mockito.when(ldap.getAuthenticationTypeEnum())
                .thenReturn(org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType.DATABASE);

        User user = new User();
        user.setId(1);
        user.setLdapGruppe(ldap);
        bean.user = user;

        de.sub.goobi.config.ConfigurationHelper mockConfig =
                Mockito.mock(de.sub.goobi.config.ConfigurationHelper.class);
        Mockito.when(mockConfig.getMinimumPasswordLength()).thenReturn(4);

        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<de.sub.goobi.config.ConfigurationHelper> mockedCfg =
                        Mockito.mockStatic(de.sub.goobi.config.ConfigurationHelper.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("t")).thenReturn(jwt);
            mockedCfg.when(de.sub.goobi.config.ConfigurationHelper::getInstance).thenReturn(mockConfig);

            bean.setToken("t");
            bean.setNewPassword("securepass");
            bean.setConfirmPassword("securepass");
            bean.saveNewPassword();

            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(user, "securepass"),
                    Mockito.times(1));
        }
        assertTrue(bean.isPasswordSaved());
    }

    @Test
    void saveNewPasswordUserWithNullLdapGroupSavesPassword() {
        User user = new User();
        user.setId(1);
        // ldapGruppe is null — user has no LDAP group assigned
        bean.user = user;

        de.sub.goobi.config.ConfigurationHelper mockConfig =
                Mockito.mock(de.sub.goobi.config.ConfigurationHelper.class);
        Mockito.when(mockConfig.getMinimumPasswordLength()).thenReturn(4);

        DecodedJWT jwt = buildJwtWithPurpose("passwordReset");

        try (MockedStatic<JwtHelper> mockedJwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<de.sub.goobi.config.ConfigurationHelper> mockedCfg =
                        Mockito.mockStatic(de.sub.goobi.config.ConfigurationHelper.class);
                MockedStatic<UserBean> mockedUserBean = Mockito.mockStatic(UserBean.class)) {
            mockedJwt.when(() -> JwtHelper.verifyTokenAndReturnClaims("t")).thenReturn(jwt);
            mockedCfg.when(de.sub.goobi.config.ConfigurationHelper::getInstance).thenReturn(mockConfig);

            bean.setToken("t");
            bean.setNewPassword("securepass");
            bean.setConfirmPassword("securepass");
            bean.saveNewPassword();

            mockedUserBean.verify(
                    () -> UserBean.saltAndSaveUserPassword(user, "securepass"),
                    Mockito.times(1));
        }
        assertTrue(bean.isPasswordSaved());
    }

    DecodedJWT buildJwtWithPurpose(String purpose) {
        DecodedJWT jwt = Mockito.mock(DecodedJWT.class);
        Claim purposeClaim = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("purpose")).thenReturn(purposeClaim);
        Mockito.when(purposeClaim.asString()).thenReturn(purpose);
        return jwt;
    }
}
