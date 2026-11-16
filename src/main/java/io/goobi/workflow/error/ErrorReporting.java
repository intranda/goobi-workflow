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

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.goobi.beans.User;

import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Creates, logs and stores the error report of a request, once per request. */
public final class ErrorReporting {

    public static final String REQUEST_ATTRIBUTE = "io.goobi.workflow.error.report";

    private static final String DASH = "-";

    private ErrorReporting() {
    }

    /**
     * @param request the request
     * @param throwable the failure, may be null
     * @param httpStatus the HTTP status, may be null
     * @param user the logged in user, may be null
     * @return the report of this request, created and logged on first call
     */
    public static ErrorReport report(HttpServletRequest request, Throwable throwable, Integer httpStatus, User user) {
        Object existing = attribute(request);
        if (existing instanceof ErrorReport report) {
            return report;
        }
        ErrorReport report;
        try {
            ResolvedError error = ErrorResolver.fromServiceLoader().resolve(throwable, httpStatus);
            if (error == null || error.entry() == null) {
                throw new IllegalStateException("no error resolved");
            }
            String incidentId = error.entry().incident() ? IncidentIds.next() : null;
            report = new ErrorReport(error, incidentId, Instant.now(), expiredViewId(throwable));
        } catch (RuntimeException e) { //NOSONAR the error path must never fail
            ResolvedError error = new ResolvedError(ErrorCatalogue.UNEXPECTED, List.of(), Map.of(), throwable);
            report = new ErrorReport(error, IncidentIds.next(), Instant.now(), null);
        }
        try {
            IncidentLogger.log(report, requestInfo(request, user));
        } catch (RuntimeException e) { //NOSONAR the error path must never fail
            // best effort
        }
        store(request, report);
        return report;
    }

    private static Object attribute(HttpServletRequest request) {
        try {
            return request == null ? null : request.getAttribute(REQUEST_ATTRIBUTE);
        } catch (RuntimeException e) { //NOSONAR the error path must never fail
            return null;
        }
    }

    private static void store(HttpServletRequest request, ErrorReport report) {
        try {
            if (request != null) {
                request.setAttribute(REQUEST_ATTRIBUTE, report);
            }
        } catch (RuntimeException e) { //NOSONAR the error path must never fail
            // best effort
        }
    }

    public static ErrorReport report(HttpServletRequest request, Throwable throwable, Integer httpStatus) {
        return report(request, throwable, httpStatus, null);
    }

    private static String expiredViewId(Throwable throwable) {
        for (Throwable t : ErrorResolver.chainOf(throwable)) {
            if (t instanceof ViewExpiredException vee) {
                return vee.getViewId();
            }
        }
        return null;
    }

    @SuppressWarnings("java:S1166")
    static IncidentLogger.RequestInfo requestInfo(HttpServletRequest request, User user) {
        String login = null;
        Integer id = null;
        if (user != null) {
            try {
                login = user.getLogin();
                id = user.getId();
            } catch (Exception e) { //NOSONAR the error path must never fail
                // leave as unknown
            }
        }
        String method = null;
        String uri = null;
        boolean ajax = false;
        String session = DASH;
        try {
            method = request.getMethod();
            Object errorUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            uri = errorUri != null ? errorUri.toString() : request.getRequestURI();
            ajax = "partial/ajax".equals(request.getHeader("Faces-Request"));
            HttpSession httpSession = request.getSession(false);
            if (httpSession != null && httpSession.getId() != null) {
                String sid = httpSession.getId();
                session = sid.substring(0, Math.min(4, sid.length())) + "…";
            }
        } catch (Exception e) { //NOSONAR the error path must never fail
            // leave as unknown
        }
        return new IncidentLogger.RequestInfo(login, id, method, uri, viewId(), ajax, session);
    }

    private static String viewId() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                UIViewRoot root = context.getViewRoot();
                if (root != null) {
                    return root.getViewId();
                }
            }
        } catch (Exception e) { //NOSONAR the error path must never fail
            // leave as unknown
        }
        return null;
    }
}
