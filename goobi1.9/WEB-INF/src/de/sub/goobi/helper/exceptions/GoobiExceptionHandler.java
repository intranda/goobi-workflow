package de.sub.goobi.helper.exceptions;

import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

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

				Map<String, Object> requestMap = facesContext.getExternalContext().getRequestMap();
				NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();

				try {
					requestMap.put("currentView", t.getMessage());
					requestMap.put("exception", t.getStackTrace());
//					 facesContext.getExternalContext().getFlash().put("exceptioniNFO",t.getCause());
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
