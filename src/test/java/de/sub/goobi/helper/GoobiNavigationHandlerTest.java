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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;

/**
 * Tests that navigation which leaves the current view is turned into a redirect (post/redirect/get), so that the address bar matches the rendered
 * page.
 */
class GoobiNavigationHandlerTest {

    private static final String CURRENT_VIEW = "/uii/process_all.xhtml";

    private ConfigurableNavigationHandler wrapped;
    private FacesContext context;
    private Flash flash;
    private GoobiNavigationHandler handler;

    @BeforeEach
    void setUp() {
        wrapped = mock(ConfigurableNavigationHandler.class);
        context = mock(FacesContext.class);
        flash = mock(Flash.class);

        ExternalContext externalContext = mock(ExternalContext.class);
        when(externalContext.getFlash()).thenReturn(flash);
        when(context.getExternalContext()).thenReturn(externalContext);

        UIViewRoot viewRoot = mock(UIViewRoot.class);
        when(viewRoot.getViewId()).thenReturn(CURRENT_VIEW);
        when(context.getViewRoot()).thenReturn(viewRoot);

        handler = new GoobiNavigationHandler(wrapped);
    }

    private void expectNavigationCase(String outcome, String targetViewId) {
        NavigationCase navigationCase = mock(NavigationCase.class);
        when(navigationCase.getToViewId(context)).thenReturn(targetViewId);
        when(navigationCase.isRedirect()).thenReturn(false);
        when(wrapped.getNavigationCase(context, null, outcome)).thenReturn(navigationCase);
    }

    private String delegatedOutcome() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(wrapped).handleNavigation(eq(context), eq(null), captor.capture());
        return captor.getValue();
    }

    @Test
    void shouldAppendRedirectWhenTargetViewDiffers() {
        expectNavigationCase("task_all", "/uii/task_all.xhtml");

        handler.handleNavigation(context, null, "task_all");

        assertEquals("task_all?faces-redirect=true", delegatedOutcome());
    }

    @Test
    void shouldKeepOutcomeWhenTargetViewIsTheCurrentView() {
        expectNavigationCase("process_all", CURRENT_VIEW);

        handler.handleNavigation(context, null, "process_all");

        assertEquals("process_all", delegatedOutcome());
    }

    @Test
    void shouldKeepOutcomeWhenItAlreadyRequestsARedirect() {
        handler.handleNavigation(context, null, "task_all?faces-redirect=true");

        assertEquals("task_all?faces-redirect=true", delegatedOutcome());
    }

    @Test
    void shouldKeepOutcomeWhenThereIsNoNavigationCase() {
        when(wrapped.getNavigationCase(context, null, "unknown_page")).thenReturn(null);

        handler.handleNavigation(context, null, "unknown_page");

        assertEquals("unknown_page", delegatedOutcome());
    }

    @Test
    void shouldKeepBlankOutcomeUntouched() {
        handler.handleNavigation(context, null, "");

        assertEquals("", delegatedOutcome());
    }

    @Test
    void shouldSeparateWithAmpersandWhenOutcomeAlreadyHasParameters() {
        expectNavigationCase("task_all?id=5", "/uii/task_all.xhtml");

        handler.handleNavigation(context, null, "task_all?id=5");

        assertEquals("task_all?id=5&faces-redirect=true", delegatedOutcome());
    }

    @Test
    void shouldKeepFacesMessagesWhenRedirecting() {
        expectNavigationCase("task_all", "/uii/task_all.xhtml");

        handler.handleNavigation(context, null, "task_all");

        verify(flash).setKeepMessages(true);
    }

    @Test
    void shouldNotTouchFlashWhenStayingOnTheSameView() {
        expectNavigationCase("process_all", CURRENT_VIEW);

        handler.handleNavigation(context, null, "process_all");

        verify(flash, never()).setKeepMessages(any(Boolean.class));
    }

    @Test
    void shouldAppendRedirectForFlowAwareNavigationAsWell() {
        NavigationCase navigationCase = mock(NavigationCase.class);
        when(navigationCase.getToViewId(context)).thenReturn("/uii/task_all.xhtml");
        when(navigationCase.isRedirect()).thenReturn(false);
        when(wrapped.getNavigationCase(context, null, "task_all", "flow-id")).thenReturn(navigationCase);

        handler.handleNavigation(context, null, "task_all", "flow-id");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(wrapped).handleNavigation(eq(context), eq(null), captor.capture(), eq("flow-id"));
        assertEquals("task_all?faces-redirect=true", captor.getValue());
    }
}
