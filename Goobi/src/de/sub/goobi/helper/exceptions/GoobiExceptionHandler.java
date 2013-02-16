/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

import com.sun.faces.context.flash.ELFlash;


/**
 * 
 * @author Robert Sehr
 *
 */
public class GoobiExceptionHandler extends ExceptionHandlerWrapper {

	private ExceptionHandler exceptionHandler;

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
			ExceptionQueuedEvent exceptionQueuedEvent = i.next();

			ExceptionQueuedEventContext exceptionQueuedEventContext = (ExceptionQueuedEventContext) exceptionQueuedEvent.getSource();

			Throwable throwable = exceptionQueuedEventContext.getException();

			if (throwable instanceof Throwable) {
				Throwable t = (Throwable) throwable;

				FacesContext facesContext = FacesContext.getCurrentInstance();

				@SuppressWarnings("unchecked")
				Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
				NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();

				try {
					requestMap.put("currentView", t.getMessage());
					ELFlash flash = ELFlash.getFlash(facesContext.getExternalContext(), true);
					flash.put("exceptionInfo",t.getCause());
					while (t.getCause() != null) {
						t = t.getCause();
					}
					List<StackTraceElement> trace = Arrays.asList(t.getStackTrace());
//					for (StackTraceElement ste : trace) {
//						System.out.println(ste.toString());
//					}
					flash.put("exceptionStacktrace", trace);
					navigationHandler.handleNavigation(facesContext, null, "/themes/default/error?faces-redirect=true");
					facesContext.renderResponse();
				} finally {
					i.remove();

				}
			}

		}
		getWrapped().handle();
	}

}
