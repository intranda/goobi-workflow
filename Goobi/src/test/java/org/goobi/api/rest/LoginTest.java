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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, UserManager.class, JwtHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*", "javax.crypto.*" })
public class LoginTest extends AbstractTest {

    private Login login;

    @Before
    public void setUp() {
        login = new Login();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("EnableHeaderLogin", "true");
        ConfigurationHelper.getInstance().setParameter("OIDCClientID", "OIDCClientID");

        PowerMock.mockStatic(JwtHelper.class);

        DecodedJWT jwt = EasyMock.createNiceMock(DecodedJWT.class);
        Claim claim1 = EasyMock.createNiceMock(Claim.class);
        EasyMock.expect(jwt.getClaim("nonce")).andReturn(claim1).anyTimes();
        EasyMock.expect(claim1.asString()).andReturn("openIDNonce").anyTimes();

        Claim claim2 = EasyMock.createNiceMock(Claim.class);
        EasyMock.expect(jwt.getClaim("aud")).andReturn(claim2).anyTimes();
        EasyMock.expect(claim2.asString()).andReturn("OIDCClientID").anyTimes();

        Claim claim3 = EasyMock.createNiceMock(Claim.class);
        EasyMock.expect(jwt.getClaim("email")).andReturn(claim3).anyTimes();
        EasyMock.expect(claim3.asString()).andReturn("fixture@example.com").anyTimes();

        EasyMock.expect(JwtHelper.verifyOpenIdToken("id")).andReturn(jwt).anyTimes();

        HttpServletRequest servletRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        EasyMock.expect(servletRequest.getAttribute(EasyMock.anyString())).andReturn("fixture").anyTimes();

        HttpSession session = EasyMock.createNiceMock(HttpSession.class);
        EasyMock.expect(servletRequest.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute("openIDNonce")).andReturn("openIDNonce").anyTimes();

        LoginBean lb = EasyMock.createNiceMock(LoginBean.class);
        User user = EasyMock.createNiceMock(User.class);
        user.lazyLoad();
        EasyMock.replay(user);
        PowerMock.mockStatic(Helper.class);

        EasyMock.expect(Helper.getLoginBeanFromSession(EasyMock.anyObject())).andReturn(lb).anyTimes();

        PowerMock.mockStatic(UserManager.class);
        EasyMock.expect(UserManager.getUserBySsoId(EasyMock.anyString())).andReturn(user).anyTimes();

        EasyMock.replay(jwt);
        EasyMock.replay(claim1);
        EasyMock.replay(claim2);
        EasyMock.replay(claim3);

        PowerMock.replay(Helper.class);
        PowerMock.replay(JwtHelper.class);
        PowerMock.replay(UserManager.class);
        EasyMock.replay(session);
        EasyMock.replay(servletRequest);

        login.setServletRequest(servletRequest);
        SessionForm sessionForm = EasyMock.createNiceMock(SessionForm.class);
        login.setSessionForm(sessionForm);

        HttpServletResponse servletResponse = EasyMock.createNiceMock(HttpServletResponse.class);
        try {
            servletResponse.sendRedirect(EasyMock.anyString());
        } catch (IOException e) {
        }
        EasyMock.replay(servletResponse);
        login.setServletResponse(servletResponse);
    }

    @Test
    public void testConstructor() {
        Login login = new Login();
        assertNotNull(login);
    }

    @Test
    public void testApacheHeaderLogin() throws Exception {
        assertEquals("", login.apacheHeaderLogin());
    }

    @Test
    public void testOpenIdErrorLogin() throws Exception {
        login.openIdLogin("error", "id");
    }

    @Test
    public void testOpenIdSuccessLogin() throws Exception {
        login.openIdLogin(null, "id");
    }
}
