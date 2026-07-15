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
package org.goobi.api.rest.command;

import java.io.File;
import java.util.List;

import org.goobi.api.rest.response.CloseStepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ShellScript;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.log4j.Log4j2;

@Path("/closestep")
@Log4j2
@Deprecated
public class CommandStepClose {

    @Deprecated
    @Context
    private UriInfo uriInfo;

    /**
     * Closes step by process title and step name. Uses the first step with the step name in the process
     *
     * @param processTitle
     * @param stepName
     * @return Response
     */
    @Deprecated
    @Path("/processtitles/{processtitle}/{stepname}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStepByName(@PathParam("processtitle") String processTitle, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessByExactTitle(processTitle);
        return closeStep(stepName, p);
    }

    @Deprecated
    @Path("/processid/{processid}/{stepname}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStepByProcessIdAndName(@PathParam("processid") Integer processid, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessById(processid);
        return closeStep(stepName, p);
    }

    private Response closeStep(String stepName, Process p) {
        List<Step> allSteps = StepManager.getStepsForProcess(p.getId());
        Step so = null;
        for (Step step : allSteps) {
            if (step.getTitel().equals(stepName)) {
                so = step;
                break;
            }
        }
        if (so == null) {
            CloseStepResponse cr = new CloseStepResponse();
            cr.setResult("error");
            String message = "Step not found";
            cr.setComment(message);
            Status status = Response.Status.NOT_FOUND;
            return Response.status(status).entity(cr).build();
        }
        return closeStepAndRemoveLink(null, so.getId());
    }

    @Deprecated
    @Path("/{stepid}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStep(@PathParam("stepid") int stepid) {
        return closeStepAndRemoveLink(null, stepid);
    }

    @Deprecated
    @Path("/{username}/{stepid}")
    @POST
    @Produces(MediaType.TEXT_XML)
    public Response closeStepAndRemoveLink(@PathParam("username") String username, @PathParam("stepid") int stepid) {
        CloseStepResponse cr = new CloseStepResponse();
        cr.setStepId(stepid);
        String message = "success";
        Status status = Response.Status.OK;

        log.debug("closing step with id " + stepid);

        Step so = StepManager.getStepById(stepid);
        if (so == null) {
            cr.setResult("error");
            message = "Step not found";
            status = Response.Status.NOT_FOUND;
        } else if (so.getValidationPlugin() != null && so.getValidationPlugin().length() > 0) {
            IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, so.getValidationPlugin());
            ivp.setStep(so);
            if (!ivp.validate()) {
                message = "Step not closed, validation failed";
                cr.setResult("error");
                status = Response.Status.NOT_ACCEPTABLE;
            }
        } else if (username != null) {
            if (!username.matches("[a-zA-Z0-9_.@-]+")) {
                cr.setResult("error");
                message = "Invalid username";
                status = Response.Status.BAD_REQUEST;
                cr.setComment(message);
                return Response.status(status).entity(cr).build();
            }
            StringBuilder sb = new StringBuilder();
            sb.append(ConfigurationHelper.getInstance().getUserFolder());
            Process po = so.getProzess();
            sb.append(username);
            sb.append("/");
            sb.append(

                    po.getTitel());

            sb.append(" [");
            sb.append(po.getId());
            sb.append("]");
            String nach = sb.toString().replace(" ", "__");
            File benutzerHome = new File(nach);
            String command = ConfigurationHelper.getInstance().getScriptDeleteSymLink() + " " + benutzerHome;

            try {
                ShellScript.legacyCallShell2(command, so.getProcessId());
            } catch (java.io.IOException | InterruptedException ioe) {
                log.error("IOException UploadFromHome", ioe);
                message = "Removing symlink from user home failed";
                status = Response.Status.NOT_ACCEPTABLE;
                cr.setResult("error");
            }
        }
        if (so != null && StepStatus.DONE.equals(so.getBearbeitungsstatusEnum())) {
            message = "Step was already closed.";
            status = Response.Status.BAD_REQUEST;
            cr.setResult("error");
        }

        if (Response.Status.OK.equals(status)) {
            HelperSchritte hs = new HelperSchritte();
            hs.CloseStepObjectAutomatic(so);
            log.debug("step closed");
        }
        cr.setComment(message);
        return Response.status(status).entity(cr).build();
    }
}
