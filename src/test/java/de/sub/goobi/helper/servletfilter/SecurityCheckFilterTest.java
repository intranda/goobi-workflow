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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.helper.servletfilter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.easymock.EasyMock;
import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class, ExternalContext.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class SecurityCheckFilterTest extends AbstractTest {

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
