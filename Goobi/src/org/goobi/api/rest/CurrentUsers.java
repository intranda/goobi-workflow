package org.goobi.api.rest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.ValueBinding;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.goobi.api.rest.model.RestUserInfo;

import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;

// Access with http://localhost:8080/goobi/api/currentusers 
@Path("/currentusers")
public class CurrentUsers {

    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;

    private FacesContext facesContext;
    private SessionForm sessionForm;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public RestUserInfo[] getCurrentUsers() {
        // Try to get the sessionForm directly
        this.sessionForm = (SessionForm) (Helper.getManagedBeanValue("#{SessionForm}"));

        // Go the longer but saver way
        if (this.sessionForm == null) {
            // Prepare the facesContext depending on how this is possible
            this.initFacesContext();

            // Get the current application and valueBinding 
            Application application = this.facesContext.getApplication();
            ValueBinding valueBinding = application.createValueBinding("#{SessionForm}");
            //ExpressionFactory expressionFactory = application.getExpressionFactory();
            //ValueExpression valueExpression = expressionFactory.createValueExpression("#{SessionForm}", null);

            // Set the sessionForm on alternative way
            this.sessionForm = (SessionForm) (valueBinding.getValue(this.facesContext));

        }

        // Now return the expected list of users / sessions
        // Otherwise sessionForm stills null, this is a warning
        if (this.sessionForm != null) {
            return this.generateUserList();

        } else {
            return new RestUserInfo[0];
        }
    }
    // Reads the current sessions and generates a user list
    private RestUserInfo[] generateUserList() {
        // Read the sessions and create the list
        List sessions = this.sessionForm.getAlleSessions();
        int length = sessions.size();
        RestUserInfo[] rui = new RestUserInfo[length];

        // Handle all current users
        int x = 0;
        while (x < length) {

            // Create the current user's object
            rui[x] = new RestUserInfo();
            Map<String, String> userMap = (Map<String, String>) (sessions.get(x));

            // Set the user's values
            rui[x].setUser(userMap.get("user"));

            rui[x].setAddress(userMap.get("address"));

            String browser = userMap.get("browserIcon");
            // Cast the "browser.png" to "Browser" example: "firefox.png" -> "Firefox"
            browser = Character.toString((char) (browser.charAt(0) - 32)) + browser.substring(1, browser.length() - 4);
            rui[x].setBrowser(browser);

            rui[x].setCreated(userMap.get("created"));

            rui[x].setLast(userMap.get("last"));
            x++;
        }
        return rui;
    }

    private void initFacesContext() {
        // Try to get the current instance to initialize the facesContext
        this.facesContext = FacesContext.getCurrentInstance();

        // When this failed, the longer way with FacesContextFactory has to be done
        if (this.facesContext == null) {

            // Create the factory and set facesContext
            FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
            LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
            Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
            this.facesContext = contextFactory.getFacesContext(this.servletRequest.getSession().getServletContext(), this.servletRequest,
                    this.servletResponse, lifecycle);

            // Set this as current instance using our inner class (because it wasn't available above)
            InnerFacesContext.setFacesContextAsCurrentInstance(this.facesContext);

            // Set a new viewRoot, otherwise context.getViewRoot returns null
            UIViewRoot view = this.facesContext.getApplication().getViewHandler().createView(this.facesContext, "");
            this.facesContext.setViewRoot(view);
        }
    }

    // You need an inner class to be able to call FacesContext.setCurrentInstance
    // since it's a protected method
    private abstract static class InnerFacesContext extends FacesContext {
        protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
            FacesContext.setCurrentInstance(facesContext);
        }
    }
}