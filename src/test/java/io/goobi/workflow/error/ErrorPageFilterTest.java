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
package io.goobi.workflow.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.goobi.beans.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class ErrorPageFilterTest {

    private static final String CONTEXT = "/goobi";

    private Map<String, Object> attributes;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;
    private StringWriter body;
    private boolean committed;

    @BeforeEach
    void setUp() throws IOException {
        attributes = new HashMap<>();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        body = new StringWriter();
        committed = false;
        when(request.getAttribute(anyString())).thenAnswer(i -> attributes.get(i.getArgument(0, String.class)));
        doAnswer(i -> attributes.put(i.getArgument(0, String.class), i.getArgument(1))).when(request).setAttribute(anyString(), any());
        when(request.getContextPath()).thenReturn(CONTEXT);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(CONTEXT + "/uii/error.xhtml");
        when(request.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml");
        attributes.put(RequestDispatcher.ERROR_REQUEST_URI, CONTEXT + "/uii/process_edit.xhtml");
        when(response.getWriter()).thenReturn(new PrintWriter(body));
        when(response.isCommitted()).thenAnswer(i -> committed);
    }

    private ErrorPageFilter filter(User user) {
        ErrorPageFilter filter = new ErrorPageFilter();
        Function<HttpServletRequest, User> lookup = r -> user;
        filter.setUserLookup(lookup);
        return filter;
    }

    private static User user() {
        User user = new User();
        user.setLogin("tester");
        return user;
    }

    private void error(Throwable exception, Integer status) {
        attributes.put(RequestDispatcher.ERROR_EXCEPTION, exception);
        attributes.put(RequestDispatcher.ERROR_STATUS_CODE, status);
    }

    @Test
    void databaseUnavailableServesFallbackWithoutJsf() throws IOException, ServletException {
        error(new SQLException("x", "08S01"), 500);
        filter(user()).doFilter(request, response, chain);
        verify(chain, never()).doFilter(any(), any());
        verify(response).setStatus(503);
        ErrorReport report = (ErrorReport) attributes.get(ErrorReporting.REQUEST_ATTRIBUTE);
        assertNotNull(report.incidentId());
        assertTrue(body.toString().contains(report.incidentId()));
    }

    @Test
    void failingErrorPageFallsBack() throws IOException, ServletException {
        error(new NullPointerException("x"), 500);
        doThrow(new IllegalStateException("error page failed")).when(chain).doFilter(any(), any());
        filter(user()).doFilter(request, response, chain);
        verify(response).resetBuffer();
        verify(response, never()).reset();
        verify(response, times(2)).setStatus(500);
        ErrorReport report = (ErrorReport) attributes.get(ErrorReporting.REQUEST_ATTRIBUTE);
        assertTrue(body.toString().contains(report.incidentId()));
    }

    @Test
    void committedResponseIsNotRewritten() throws IOException, ServletException {
        error(new NullPointerException("x"), 500);
        committed = true;
        doThrow(new IllegalStateException("error page failed")).when(chain).doFilter(any(), any());
        filter(user()).doFilter(request, response, chain);
        verify(response, never()).resetBuffer();
        verify(response, never()).reset();
        assertEquals("", body.toString());
    }

    @Test
    void loggedOutForbiddenRedirectsToLogin() throws IOException, ServletException {
        error(null, 403);
        filter(null).doFilter(request, response, chain);
        verify(response).sendRedirect("/goobi/uii/index.xhtml");
        verify(request, never()).getSession(true);
        verify(request, never()).getSession();
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void loggedOutViewExpiredRedirectsWithNotice() throws IOException, ServletException {
        error(new jakarta.faces.application.ViewExpiredException("gone", "/uii/x.xhtml"), 500);
        filter(null).doFilter(request, response, chain);
        verify(response).sendRedirect("/goobi/uii/index.xhtml?loginExpired=true");
    }

    @Test
    void restApiGetsPlainText() throws IOException, ServletException {
        attributes.put(RequestDispatcher.ERROR_REQUEST_URI, CONTEXT + "/api/processes/1");
        error(null, 404);
        filter(null).doFilter(request, response, chain);
        verify(response).setContentType("text/plain;charset=UTF-8");
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).sendRedirect(anyString());
        verify(chain, never()).doFilter(any(), any());
        assertTrue(body.toString().startsWith("Error 404"));
    }

    @Test
    void missingImageGetsPlainText() throws IOException, ServletException {
        when(request.getHeader("Accept")).thenReturn("image/webp,*/*");
        error(null, 404);
        filter(user()).doFilter(request, response, chain);
        verify(response).setContentType("text/plain;charset=UTF-8");
        verify(chain, never()).doFilter(any(), any());
        assertTrue(body.toString().startsWith("Error 404"));
        assertFalse(body.toString().contains("<"));
    }

    @Test
    void xhrOutsideJsfGetsPartialResponse() throws IOException, ServletException {
        when(request.getHeader("Faces-Request")).thenReturn("partial/ajax");
        error(new NullPointerException("x"), 500);
        filter(user()).doFilter(request, response, chain);
        assertTrue(body.toString().startsWith("<?xml"));
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void normalErrorPassesThroughWithStatus() throws IOException, ServletException {
        error(new NullPointerException("x"), 500);
        filter(user()).doFilter(request, response, chain);
        verify(response).setStatus(500);
        verify(chain, times(1)).doFilter(request, response);
        assertEquals("", body.toString());
    }

    @Test
    void sessionLookupNeverCreatesSession() throws IOException, ServletException {
        HttpSession none = null;
        when(request.getSession(false)).thenReturn(none);
        error(null, 403);
        new ErrorPageFilter().doFilter(request, response, chain);
        verify(request, never()).getSession(true);
        verify(request, never()).getSession();
        verify(response).sendRedirect("/goobi/uii/index.xhtml");
    }
}
