package de.sub.goobi.helper.servletfilter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.FacesContextHelper;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class })
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
public class SecurityCheckFilterTest {

    @Test
    public void testSecurityCheckFilter() {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        assertNotNull(filter);
    }

    @Test
    public void testInit() throws ServletException {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        filter.init(null);
    }

    @Test
    public void testDestroy() {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        filter.destroy();
    }

    @Test
    public void testDoFilterWithoutUser() throws IOException, ServletException {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        EasyMock.expect(servletRequest.getRequestURI()).andReturn("fixture");
        EasyMock.expect(servletRequest.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute("LoginForm")).andReturn(null).anyTimes();
        servletResponse.sendRedirect(EasyMock.anyString());
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);
        externalContext.redirect("index.xhtml");
        FacesContextHelper.setFacesContext(facesContext);
        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();


        EasyMock.expectLastCall();
        EasyMock.replay(servletRequest);
        EasyMock.replay(servletResponse);
        EasyMock.replay(filterChain);
        EasyMock.replay(session);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);
        filter.doFilter(servletRequest, servletResponse, filterChain);
    }

    @Test
    public void testDoFilterLoggedIn() throws IOException, ServletException {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        HttpSession session = EasyMock.createMock(HttpSession.class);
        LoginBean login = EasyMock.createMock(LoginBean.class);
        User user = new User();
        login.setMyBenutzer(user);

        EasyMock.expect(servletRequest.getRequestURI()).andReturn("index.xhtml");
        EasyMock.expect(servletRequest.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute("LoginForm")).andReturn(login).anyTimes();
        servletResponse.sendRedirect(EasyMock.anyString());
        filterChain.doFilter(servletRequest, servletResponse);

        EasyMock.expectLastCall();
        EasyMock.replay(servletRequest);
        EasyMock.replay(servletResponse);
        EasyMock.replay(filterChain);
        EasyMock.replay(session);
        filter.doFilter(servletRequest, servletResponse, filterChain);
    }
}
