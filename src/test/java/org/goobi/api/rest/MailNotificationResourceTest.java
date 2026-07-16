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
 */
package org.goobi.api.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

public class MailNotificationResourceTest extends AbstractTest {

    @Test
    public void testInvalidTokenReturnsBadRequest() throws Exception {
        try (MockedStatic<JwtHelper> jwt = Mockito.mockStatic(JwtHelper.class)) {
            jwt.when(() -> JwtHelper.validateToken(Mockito.anyString(), Mockito.anyMap())).thenReturn(false);
            Response resp = new MailNotificationResource().deactivateEmailNotification("user", "bad",
                    Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
            assertEquals(400, resp.getStatus());
        }
    }

    @Test
    public void testStepInvalidTokenReturnsBadRequest() throws Exception {
        try (MockedStatic<JwtHelper> jwt = Mockito.mockStatic(JwtHelper.class)) {
            jwt.when(() -> JwtHelper.validateToken(Mockito.anyString(), Mockito.anyMap())).thenReturn(false);
            Response resp = new MailNotificationResource().deactivateEmailNotificationForStep("user", "step", "bad",
                    Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
            assertEquals(400, resp.getStatus());
        }
    }

    @Test
    public void testProjectInvalidTokenReturnsBadRequest() throws Exception {
        try (MockedStatic<JwtHelper> jwt = Mockito.mockStatic(JwtHelper.class)) {
            jwt.when(() -> JwtHelper.validateToken(Mockito.anyString(), Mockito.anyMap())).thenReturn(false);
            Response resp = new MailNotificationResource().deactivateEmailNotificationForProject("user", "project", "bad",
                    Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
            assertEquals(400, resp.getStatus());
        }
    }

    @Test
    public void testValidTokenDeactivatesAllNotificationsAndSavesUser() throws Exception {
        StepConfiguration step = new StepConfiguration();
        step.setDone(true);
        step.setError(true);
        step.setInWork(true);
        step.setOpen(true);

        UserProjectConfiguration projectConfiguration = new UserProjectConfiguration();
        projectConfiguration.setProjectName("project");
        projectConfiguration.getStepList().add(step);

        User user = Mockito.spy(new User());
        Mockito.doNothing().when(user).lazyLoad();
        user.setEmailConfiguration(new ArrayList<>(List.of(projectConfiguration)));

        try (MockedStatic<JwtHelper> jwt = Mockito.mockStatic(JwtHelper.class);
                MockedStatic<UserManager> userManager = Mockito.mockStatic(UserManager.class)) {
            jwt.when(() -> JwtHelper.validateToken(Mockito.anyString(), Mockito.anyMap())).thenReturn(true);
            userManager.when(() -> UserManager.getUserByLogin("user")).thenReturn(user);
            userManager.when(() -> UserManager.saveUser(user)).thenReturn(user);

            new MailNotificationResource().deactivateEmailNotification("user", "good",
                    Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));

            assertFalse(step.isDone());
            assertFalse(step.isError());
            assertFalse(step.isInWork());
            assertFalse(step.isOpen());
            userManager.verify(() -> UserManager.saveUser(user));
        }
    }
}
