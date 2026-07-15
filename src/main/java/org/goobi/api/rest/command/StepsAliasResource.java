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

import org.goobi.api.rest.ProcessStepResource;
import org.goobi.api.rest.model.RestReportProblem;
import org.goobi.api.rest.request.ReportProblem;
import org.goobi.beans.Step;

import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.log4j.Log4j2;

/**
 * Deprecated alias for the old plugin endpoints under "/steps/*". Delegates all logic to the canonical
 * {@link ProcessStepResource#reportProblem(String, String, RestReportProblem)} implementation to avoid duplicating the correction workflow logic.
 */
@Deprecated
@Log4j2
@Path("/steps")
public class StepsAliasResource {

    @Deprecated
    @Path("/{id}/reportproblem/{destinationTitle}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportProblemForTaskFromBody(@PathParam("id") int stepId, @PathParam("destinationTitle") String destinationTitle,
            String errorMessage) {
        return getReportProblemForTask(stepId, destinationTitle, errorMessage);
    }

    @Deprecated
    @Path("/{id}/reportproblem/{destinationTitle}/{errortext}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReportProblemForTask(@PathParam("id") int stepId, @PathParam("destinationTitle") String destinationTitle,
            @PathParam("errortext") String errorMessage) {
        return delegateReportProblem(stepId, destinationTitle, errorMessage);
    }

    @Deprecated
    @POST
    @Path("/reportproblem")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response getReportProblemForTask(ReportProblem problem) {
        int stepId;
        try {
            stepId = Integer.parseInt(problem.getStepId());
        } catch (NumberFormatException e) {
            log.debug("Invalid step id in deprecated /steps/reportproblem request: {}", problem.getStepId());
            return Response.status(Status.BAD_REQUEST).entity("Invalid step id").build();
        }
        return delegateReportProblem(stepId, problem.getDestinationStepName(), problem.getErrorText());
    }

    /**
     * Loads the step referenced by the deprecated request and delegates to the canonical process/step reportproblem implementation.
     */
    private Response delegateReportProblem(int stepId, String destinationTitle, String errorMessage) {
        Step step = StepManager.getStepById(stepId);
        if (step == null) {
            return Response.status(Status.NOT_FOUND).entity("Step not found").build();
        }

        RestReportProblem body = new RestReportProblem();
        body.setDestinationStepName(destinationTitle);
        body.setErrorText(errorMessage);

        return new ProcessStepResource().reportProblem(String.valueOf(step.getProcessId()), String.valueOf(stepId), body);
    }
}
