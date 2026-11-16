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

import java.io.IOException;
import java.util.function.Function;

import org.goobi.beans.User;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

/**
 * Runs on the ERROR dispatch to the error page. Reports the error once, keeps non-page clients (REST API, images, plain XHR) free of HTML and
 * protects the visitor from a failing error page by falling back to the static page.
 */
@Log4j2
public class ErrorPageFilter implements Filter {

    private static final String START_PAGE = "/uii/index.xhtml";
    private static final String CONTENT_TYPE_HTML = "text/html;charset=UTF-8";
    private static final String CONTENT_TYPE_TEXT = "text/plain;charset=UTF-8";

    private Function<HttpServletRequest, User> userLookup = SessionUser::find;

    /** @param userLookup finds the logged in user of a request without creating a session */
    void setUserLookup(Function<HttpServletRequest, User> userLookup) {
        this.userLookup = userLookup;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        if (!(servletRequest instanceof HttpServletRequest request) || !(servletResponse instanceof HttpServletResponse response)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }
        String contextPath = request.getContextPath();
        User user = lookupUser(request);
        ErrorReport report = ErrorReporting.report(request, (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION),
                (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE), user);
        ErrorEntry entry = report.error().entry();

        if (isNonPageUri(request, contextPath)) {
            writePlainText(request, response, report);
            return;
        }
        if ("partial/ajax".equals(request.getHeader("Faces-Request"))) {
            // faces.js and PrimeFaces discard the body of a non-2xx response, so the partial response is sent as 200
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/xml;charset=UTF-8");
            response.getWriter().write(FallbackPage.renderPartial(report, request.getLocale()));
            return;
        }
        if (!acceptsHtml(request)) {
            writePlainText(request, response, report);
            return;
        }
        if (user == null && ErrorCatalogue.FORBIDDEN.key().equals(entry.key())) {
            response.sendRedirect(contextPath + START_PAGE);
            return;
        }
        if (user == null && ErrorCatalogue.VIEW_EXPIRED.key().equals(entry.key())) {
            response.sendRedirect(contextPath + START_PAGE + "?loginExpired=true");
            return;
        }
        if (entry.standalone()) {
            response.setStatus(entry.httpStatus());
            response.setContentType(CONTENT_TYPE_HTML);
            response.getWriter().write(FallbackPage.render(report, request.getLocale()));
            return;
        }
        response.setStatus(entry.httpStatus());
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) { //NOSONAR the visitor must get a page even if the error page fails
            IncidentLogger.logFollowUp(report, t);
            if (!response.isCommitted()) {
                // resetBuffer keeps the headers (security headers set by other filters), reset would drop them
                response.resetBuffer();
                response.setStatus(500);
                response.setContentType(CONTENT_TYPE_HTML);
                response.getWriter().write(FallbackPage.render(report, request.getLocale()));
            }
        }
    }

    private static boolean isNonPageUri(HttpServletRequest request, String contextPath) {
        Object original = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String uri = original != null ? original.toString() : request.getRequestURI();
        return uri != null && (uri.startsWith(contextPath + "/api/") || uri.startsWith(contextPath + "/plugins/")
                || uri.equals(contextPath + "/wi") || uri.equals(contextPath + "/ps"));
    }

    private static boolean acceptsHtml(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }

    private static void writePlainText(HttpServletRequest request, HttpServletResponse response, ErrorReport report) throws IOException {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String text = "Error " + (status != null ? status : report.error().entry().httpStatus());
        if (report.incidentId() != null) {
            text += " (incident " + report.incidentId() + ")";
        }
        response.setContentType(CONTENT_TYPE_TEXT);
        response.getWriter().write(text);
    }

    private User lookupUser(HttpServletRequest request) {
        try {
            return userLookup.apply(request);
        } catch (RuntimeException e) { //NOSONAR the error path must never fail
            return null;
        }
    }

}
