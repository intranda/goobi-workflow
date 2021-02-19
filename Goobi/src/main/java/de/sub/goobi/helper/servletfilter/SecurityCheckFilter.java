package de.sub.goobi.helper.servletfilter;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.helper.FacesContextHelper;

public class SecurityCheckFilter implements Filter {

    @Inject
    private LoginBean userBean;

    public SecurityCheckFilter() { //called once. no method arguments allowed here!
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    /** Creates a new instance of SecurityCheckFilter */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request;
        String url = hreq.getRequestURI();
        String destination = "index.xhtml";
        if (((userBean == null || userBean.getMyBenutzer() == null)) && !url.contains("javax.faces.resource") && !url.contains("wi?")
                && !url.contains("currentUsers.xhtml") && !url.contains("logout.xhtml") && !url.contains("technicalBackground.xhtml")
                && !url.contains("mailNotificationDisabled.xhtml") && !url.contains(destination)) {
            ExternalContext ec = getFacesContext(request, response).getExternalContext();

            ec.redirect(destination);

        } else {
            chain.doFilter(request, response);
        }
    }

    // Create a new FacesContext if it doesn't exist on first request
    // idea taken from
    // https://balusc.omnifaces.org/2006/06/communication-in-jsf.html#AccessingTheFacesContextInsideHttpServletOrFilter

    public static FacesContext getFacesContext(ServletRequest request, ServletResponse response) {
        // Get current FacesContext.
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();

        // Check current FacesContext.
        if (facesContext == null) {

            // Create new Lifecycle.
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

            // Create new FacesContext.
            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            facesContext =
                    contextFactory.getFacesContext(((HttpServletRequest) request).getSession().getServletContext(), request, response, lifecycle);

            // Create new View.
            UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
            facesContext.setViewRoot(view);

            // Set current FacesContext.
            FacesContextWrapper.setCurrentInstance(facesContext);
        }

        return facesContext;
    }


    // Wrap the protected FacesContext.setCurrentInstance() in a inner class.
    private static abstract class FacesContextWrapper extends FacesContext {
        protected static void setCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }
}
