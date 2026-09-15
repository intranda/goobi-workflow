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
package de.sub.goobi.helper;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.ConfigurableNavigationHandlerWrapper;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

/**
 * Turns every navigation that leaves the current view into a redirect (post/redirect/get). Without this, JSF forwards to the new view while the
 * browser keeps the previous URL, so the address bar always lags one page behind.
 *
 * Navigation that stays on the current view keeps the forward, which preserves view scoped state and request scoped messages.
 */
public class GoobiNavigationHandler extends ConfigurableNavigationHandlerWrapper {

    private static final String REDIRECT_PARAMETER = "faces-redirect=true";

    private final ConfigurableNavigationHandler wrapped;

    public GoobiNavigationHandler(NavigationHandler wrapped) {
        this.wrapped = (ConfigurableNavigationHandler) wrapped;
    }

    @Override
    public ConfigurableNavigationHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
        wrapped.handleNavigation(context, fromAction, redirectingOutcome(context, fromAction, outcome, null));
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        wrapped.handleNavigation(context, fromAction, redirectingOutcome(context, fromAction, outcome, toFlowDocumentId), toFlowDocumentId);
    }

    /**
     * Appends the redirect parameter if the outcome leads to another view, otherwise returns the outcome unchanged.
     */
    private String redirectingOutcome(FacesContext context, String fromAction, String outcome, String toFlowDocumentId) {
        if (outcome == null || outcome.isBlank() || outcome.contains(REDIRECT_PARAMETER)) {
            return outcome;
        }
        NavigationCase navigationCase = toFlowDocumentId == null ? wrapped.getNavigationCase(context, fromAction, outcome)
                : wrapped.getNavigationCase(context, fromAction, outcome, toFlowDocumentId);
        if (navigationCase == null || navigationCase.isRedirect()) {
            return outcome;
        }
        String targetViewId = navigationCase.getToViewId(context);
        UIViewRoot viewRoot = context.getViewRoot();
        if (targetViewId == null || (viewRoot != null && targetViewId.equals(viewRoot.getViewId()))) {
            return outcome;
        }
        // messages are request scoped and would be lost on the redirect
        context.getExternalContext().getFlash().setKeepMessages(true);
        return outcome + (outcome.contains("?") ? "&" : "?") + REDIRECT_PARAMETER;
    }
}
