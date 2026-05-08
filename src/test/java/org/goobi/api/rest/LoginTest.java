/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
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
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class LoginTest extends AbstractTest {

    private Login login;

    private DecodedJWT jwt;
    private LoginBean lb;
    private User user;

    @BeforeEach
    public void setUp() {
        login = new Login(new SessionForm());
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("EnableHeaderLogin", "true");
        ConfigurationHelper.getInstance().setParameter("OIDCClientID", "OIDCClientID");

        jwt = Mockito.mock(DecodedJWT.class);
        Claim claim1 = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("nonce")).thenReturn(claim1);
        Mockito.when(claim1.asString()).thenReturn("openIDNonce");

        Claim claim2 = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("aud")).thenReturn(claim2);
        Mockito.when(claim2.asString()).thenReturn("OIDCClientID");

        Claim claim3 = Mockito.mock(Claim.class);
        Mockito.when(jwt.getClaim("email")).thenReturn(claim3);
        Mockito.when(claim3.asString()).thenReturn("fixture@example.com");

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getAttribute(Mockito.anyString())).thenReturn("fixture");

        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("openIDNonce")).thenReturn("openIDNonce");

        lb = Mockito.mock(LoginBean.class);
        user = Mockito.mock(User.class);
        user.lazyLoad();

        login.setServletRequest(servletRequest);
        SessionForm sessionForm = Mockito.mock(SessionForm.class);
        login.setSessionForm(sessionForm);

        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
        try {
            servletResponse.sendRedirect(Mockito.anyString());
        } catch (IOException e) {
        }
        login.setServletResponse(servletResponse);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwtHelper.when(() -> JwtHelper.verifyOpenIdToken("id")).thenReturn(jwt);
            mockedHelper.when(() -> Helper.getLoginBeanFromSession(Mockito.any())).thenReturn(lb);
            mockedUserManager.when(() -> UserManager.getUserBySsoId(Mockito.anyString())).thenReturn(user);

            Login login = new Login(new SessionForm());
            assertNotNull(login);

        }
    }

    @Test
    public void testApacheHeaderLogin() throws Exception {
        try (MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwtHelper.when(() -> JwtHelper.verifyOpenIdToken("id")).thenReturn(jwt);
            mockedHelper.when(() -> Helper.getLoginBeanFromSession(Mockito.any())).thenReturn(lb);
            mockedUserManager.when(() -> UserManager.getUserBySsoId(Mockito.anyString())).thenReturn(user);

            assertEquals("", login.apacheHeaderLogin());

        }
    }

    @Test
    public void testOpenIdErrorLogin() throws Exception {
        try (MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwtHelper.when(() -> JwtHelper.verifyOpenIdToken("id")).thenReturn(jwt);
            mockedHelper.when(() -> Helper.getLoginBeanFromSession(Mockito.any())).thenReturn(lb);
            mockedUserManager.when(() -> UserManager.getUserBySsoId(Mockito.anyString())).thenReturn(user);

            login.openIdLogin("error", "id");

        }
    }

    @Test
    public void testOpenIdSuccessLogin() throws Exception {
        try (MockedStatic<JwtHelper> mockedJwtHelper = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class)) {
            mockedJwtHelper.when(() -> JwtHelper.verifyOpenIdToken("id")).thenReturn(jwt);
            mockedHelper.when(() -> Helper.getLoginBeanFromSession(Mockito.any())).thenReturn(lb);
            mockedUserManager.when(() -> UserManager.getUserBySsoId(Mockito.anyString())).thenReturn(user);

            login.openIdLogin(null, "id");

        }
    }
}
