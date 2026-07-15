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

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.User;

import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.UserManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.log4j.Log4j2;

/**
 * This end point can be used to deactivate email notification for a single project, for all steps of a kind or at all.
 *
 */
@Path("/mails/disable")
@Log4j2
public class MailNotificationResource {

    @Path("/all/{user}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    @Operation(summary = "Disable all email notifications for a user",
            description = "Deactivates all email notifications for the given user. Invoked via a signed link contained in a notification email; "
                    + "the token in the path is validated by this endpoint.")
    @ApiResponse(responseCode = "200", description = "OK - redirects to a confirmation page")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "mails")
    public Response deactivateEmailNotification(@PathParam("user") String userName, @PathParam("token") String token,
            @Context HttpServletRequest request, @Context HttpServletResponse response) {

        userName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        Map<String, String> deactivateStepMap = new HashMap<>();
        deactivateStepMap.put("purpose", "disablemails");
        deactivateStepMap.put("type", "all");
        deactivateStepMap.put("user", userName);

        try {
            if (!JwtHelper.validateToken(token, deactivateStepMap)) {
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            log.error(e1);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            for (StepConfiguration step : configuration.getStepList()) {
                step.setDone(false);
                step.setError(false);
                step.setInWork(false);
                step.setOpen(false);
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;

    }

    @Path("/step/{user}/{step}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    @Operation(summary = "Disable email notifications for a step",
            description = "Deactivates email notifications for the given user and step. Invoked via a signed link contained in a notification email; "
                    + "the token in the path is validated by this endpoint.")
    @ApiResponse(responseCode = "200", description = "OK - redirects to a confirmation page")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "mails")
    public Response deactivateEmailNotificationForStep(@PathParam("user") String userName, @PathParam("step") String step,
            @PathParam("token") String token, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        userName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        step = URLDecoder.decode(step, StandardCharsets.UTF_8);

        Map<String, String> deactivateStepMap = new HashMap<>();
        deactivateStepMap.put("purpose", "disablemails");
        deactivateStepMap.put("type", "step");
        deactivateStepMap.put("user", userName);
        deactivateStepMap.put("step", step);

        try {
            if (!JwtHelper.validateToken(token, deactivateStepMap)) {
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            log.error(e1);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            for (StepConfiguration sc : configuration.getStepList()) {
                if (sc.getStepName().equals(step)) {
                    sc.setDone(false);
                    sc.setError(false);
                    sc.setInWork(false);
                    sc.setOpen(false);
                }
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    @Path("/project/{user}/{project}/{token}")
    @Produces(MediaType.TEXT_HTML)
    @GET // should be POST, but must be GET to have a clickable link within an email
    @Operation(summary = "Disable email notifications for a project",
            description = "Deactivates email notifications for the given user and project. Invoked via a signed link contained in a notification "
                    + "email; the token in the path is validated by this endpoint.")
    @ApiResponse(responseCode = "200", description = "OK - redirects to a confirmation page")
    @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Tag(name = "mails")
    public Response deactivateEmailNotificationForProject(@PathParam("user") String userName, @PathParam("project") String project,
            @PathParam("token") String token, @Context HttpServletRequest request, @Context HttpServletResponse response) {

        userName = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        project = URLDecoder.decode(project, StandardCharsets.UTF_8);

        Map<String, String> deactivateProjectMap = new HashMap<>();
        deactivateProjectMap.put("purpose", "disablemails");
        deactivateProjectMap.put("type", "project");
        deactivateProjectMap.put("user", userName);
        deactivateProjectMap.put("project", project);

        try {
            if (!JwtHelper.validateToken(token, deactivateProjectMap)) {
                return getErrorPage(Status.BAD_REQUEST, "invalid request, your url is worng or to old.");
            }
        } catch (ConfigurationException e1) {
            log.error(e1);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Configuration error, try again later.");
        }

        User user = UserManager.getUserByLogin(userName);
        user.lazyLoad();
        List<UserProjectConfiguration> configurationList = user.getEmailConfiguration();
        for (UserProjectConfiguration configuration : configurationList) {
            if (configuration.getProjectName().equals(project)) {
                for (StepConfiguration step : configuration.getStepList()) {
                    step.setDone(false);
                    step.setError(false);
                    step.setInWork(false);
                    step.setOpen(false);
                }
            }
        }
        try {
            UserManager.saveUser(user);
        } catch (DAOException e) {
            log.error(e);
            return getErrorPage(Status.INTERNAL_SERVER_ERROR, "Save error, try again later.");
        }

        String myJsfPage = "/uii/mailNotificationDisabled.xhtml";
        String contextPath = request.getContextPath();
        try {
            response.sendRedirect(contextPath + myJsfPage);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    private Response getErrorPage(Status status, String errorText) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<body>");
        sb.append("<p>");
        sb.append(errorText);
        sb.append("</p>");
        sb.append("</body>");
        sb.append("</html>");
        return Response.status(status).entity(sb.toString()).build();
    }
}
