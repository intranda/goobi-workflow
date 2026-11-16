/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * Copyright 2012, intranda GmbH, Göttingen
 * 
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

package de.sub.goobi.helper.exceptions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import org.goobi.beans.User;
import org.omnifaces.context.OmniPartialViewContext;
import org.omnifaces.util.Hacks;

import de.sub.goobi.helper.FacesContextHelper;
import io.goobi.workflow.error.ErrorCatalogue;
import io.goobi.workflow.error.ErrorEntry;
import io.goobi.workflow.error.ErrorReport;
import io.goobi.workflow.error.ErrorReporting;
import io.goobi.workflow.error.FallbackPage;
import io.goobi.workflow.error.IncidentLogger;
import io.goobi.workflow.error.SessionUser;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PreRenderViewEvent;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

/**
 * Handles exceptions of ajax requests by rendering the error page into the partial response. Exceptions of all other requests stay in the queue, so
 * the wrapped handler throws them to the container, which dispatches to the error page declared in web.xml.
 */
@Log4j2
public class GoobiExceptionHandler extends ExceptionHandlerWrapper {

    /** What to do with an exception. */
    enum Decision {
        /** Throw it to the servlet container (web.xml error page). */
        CONTAINER,
        /** Send the visitor to the login page. */
        LOGIN_REDIRECT,
        /** Write the static fallback page into the partial response. */
        FALLBACK,
        /** Render the error page view into the partial response. */
        RENDER_ERROR_VIEW
    }

    private static final String ERROR_VIEW_ID = "/uii/error.xhtml";
    private static final String ATTRIBUTE_EXCEPTION = "jakarta.servlet.error.exception";
    private static final String ATTRIBUTE_STATUS = "jakarta.servlet.error.status_code";
    private static final String PARTIAL_CONTENT_TYPE = "text/xml";
    private static final String ENCODING = "UTF-8";

    private final ExceptionHandler exceptionHandler;

    @SuppressWarnings("deprecation")
    public GoobiExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.exceptionHandler;
    }

    static Decision decide(boolean ajax, ErrorEntry entry, boolean loggedIn) {
        if (!ajax) {
            return Decision.CONTAINER;
        }
        if (!loggedIn && ErrorCatalogue.VIEW_EXPIRED.key().equals(entry.key())) {
            return Decision.LOGIN_REDIRECT;
        }
        if (entry.standalone()) {
            return Decision.FALLBACK;
        }
        return Decision.RENDER_ERROR_VIEW;
    }

    @Override
    public void handle() throws FacesException {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        if (context != null && isAjax(context)) {
            for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
                ExceptionQueuedEvent event = i.next();
                Throwable throwable = ((ExceptionQueuedEventContext) event.getSource()).getException();
                i.remove();
                if (!context.getResponseComplete()) {
                    handleAjax(context, throwable);
                }
            }
        }
        // everything left (all non-ajax exceptions) is thrown to the container
        getWrapped().handle();
    }

    private static boolean isAjax(FacesContext context) {
        return context.getPartialViewContext() != null && context.getPartialViewContext().isAjaxRequest();
    }

    @SuppressWarnings("java:S1141")
    private void handleAjax(FacesContext context, Throwable throwable) {
        ExternalContext external = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) external.getRequest();
        User user = SessionUser.find(request);
        ErrorReport report = ErrorReporting.report(request, throwable, null, user);
        try {
            if (external.isResponseCommitted()) {
                // too late to replace the response: log once and close what Faces already started
                IncidentLogger.logFollowUp(report, new IllegalStateException("response already committed"));
                closePartialResponse(context);
                return;
            }
            switch (decide(true, report.error().entry(), user != null)) {
                case LOGIN_REDIRECT -> {
                    external.redirect(external.getRequestContextPath() + "/uii/index.xhtml?loginExpired=true");
                    context.responseComplete();
                }
                case FALLBACK -> writeFallback(context, report);
                default -> renderErrorView(context, request, throwable, report);
            }
        } catch (Exception | LinkageError e) { //NOSONAR the error path must never fail
            IncidentLogger.logFollowUp(report, e);
            try {
                writeFallback(context, report);
            } catch (Exception | LinkageError second) { //NOSONAR the error path must never fail
                IncidentLogger.logFollowUp(report, second);
            }
        }
    }

    private static void closePartialResponse(FacesContext context) {
        try {
            OmniPartialViewContext.getCurrentInstance(context).closePartialResponse();
        } catch (RuntimeException e) {
            log.debug("Could not close the partial response", e);
        }
        context.responseComplete();
    }

    private static void renderErrorView(FacesContext context, HttpServletRequest request, Throwable throwable, ErrorReport report)
            throws IOException {
        request.setAttribute(ATTRIBUTE_EXCEPTION, throwable);
        request.setAttribute(ATTRIBUTE_STATUS, report.error().entry().httpStatus());
        try {
            renderErrorPage(context);
        } finally {
            request.removeAttribute(ATTRIBUTE_EXCEPTION);
        }
    }

    private static void renderErrorPage(FacesContext context) throws IOException {
        resetResponse(context);
        ViewHandler viewHandler = context.getApplication().getViewHandler();
        UIViewRoot viewRoot = viewHandler.createView(context, ERROR_VIEW_ID);
        Hacks.removeResourceDependencyState(context);
        context.setViewRoot(viewRoot);
        context.getPartialViewContext().setRenderAll(true);
        ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(context, ERROR_VIEW_ID);
        vdl.buildView(context, viewRoot);
        context.getApplication().publishEvent(context, PreRenderViewEvent.class, viewRoot);
        vdl.renderView(context, viewRoot);
        context.responseComplete();
    }

    private static void writeFallback(FacesContext context, ErrorReport report) throws IOException {
        resetResponse(context);
        ExternalContext external = context.getExternalContext();
        external.setResponseContentType(PARTIAL_CONTENT_TYPE);
        external.setResponseCharacterEncoding(ENCODING);
        Locale locale = context.getViewRoot() != null ? context.getViewRoot().getLocale() : null;
        external.getResponseOutputWriter().write(FallbackPage.renderPartial(report, locale));
        context.responseComplete();
    }

    /** Throws away what was written so far and restores content type and encoding, which a reset drops. */
    private static void resetResponse(FacesContext context) {
        ExternalContext external = context.getExternalContext();
        String contentType = external.getResponseContentType();
        String encoding = external.getResponseCharacterEncoding();
        external.responseReset();
        try {
            OmniPartialViewContext.getCurrentInstance(context).resetPartialResponse();
        } catch (RuntimeException e) {
            log.debug("No OmniFaces partial response to reset", e);
        }
        external.setResponseContentType(contentType != null ? contentType : PARTIAL_CONTENT_TYPE);
        external.setResponseCharacterEncoding(encoding != null ? encoding : ENCODING);
    }
}
