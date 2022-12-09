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
