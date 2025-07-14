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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.sub.goobi.helper.FacesContextHelper;
import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiExceptionHandler extends ExceptionHandlerWrapper {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private ExceptionHandler exceptionHandler;

    @SuppressWarnings("deprecation")
    public GoobiExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.exceptionHandler;
    }

    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();

            // Handle ViewExpiredExceptions here ... or even others :)
            if (!t.getClass().equals(ViewExpiredException.class)) {
                log.error("CLASS: " + t.getClass().getName(), t);
            }
            FacesContext fc = FacesContextHelper.getCurrentFacesContext();
            Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
            NavigationHandler nav = fc.getApplication().getNavigationHandler();

            if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                try {
                    // Push some useful stuff to the request scope for use in the page
                    requestMap.put("currentViewId", vee.getViewId());
                    nav.handleNavigation(fc, null, "index");
                    fc.renderResponse();
                } finally {
                    i.remove();
                }
            } else {
                log.error(t.getMessage(), t);
                try {
                    // Push some useful stuff to the request scope for use in the page
                    requestMap.put("errorDetails", sdf.format(new Date()) + ": " + t.getMessage());

                    // get root cause
                    while (t.getCause() != null) {
                        t = t.getCause();
                    }
                    List<StackTraceElement> trace = Arrays.asList(t.getStackTrace());

                    requestMap.put("stacktrace", trace);

                    nav.handleNavigation(fc, null, "error");
                    fc.renderResponse();
                } finally {
                    i.remove();
                }
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();

    }
}
