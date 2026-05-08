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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
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
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.when(servletRequest.getRequestURI()).thenReturn("fixture");
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("LoginForm")).thenReturn(null);
        servletResponse.sendRedirect(Mockito.anyString());

        try (MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
                MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {

            FacesContext facesContext = Mockito.mock(FacesContext.class);
            ExternalContext externalContext = Mockito.mock(ExternalContext.class);
            externalContext.redirect("index.xhtml");
            FacesContextHelper.setFacesContext(facesContext);
            Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);

            filter.doFilter(servletRequest, servletResponse, filterChain);
        }
    }

    @Test
    public void testDoFilterLoggedIn() throws IOException, ServletException {
        SecurityCheckFilter filter = new SecurityCheckFilter();
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        LoginBean login = Mockito.mock(LoginBean.class);
        User user = new User();
        login.setMyBenutzer(user);

        Mockito.when(servletRequest.getRequestURI()).thenReturn("index.xhtml");
        Mockito.when(servletRequest.getSession()).thenReturn(session);
        Mockito.when(session.getAttribute("LoginForm")).thenReturn(login);
        servletResponse.sendRedirect(Mockito.anyString());
        filterChain.doFilter(servletRequest, servletResponse);

        filter.doFilter(servletRequest, servletResponse, filterChain);
    }
}
