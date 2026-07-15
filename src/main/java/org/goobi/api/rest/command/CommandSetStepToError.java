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

import java.util.Date;
import java.util.List;

import org.goobi.api.rest.response.CloseStepResponse;
import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Deprecated
@Path("seterrorstep")
public class CommandSetStepToError {

    /**
     * Sets step to error by step-id.
     *
     * @param sourceid
     * @return Response
     */
    @Deprecated
    @Path("/{stepid}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response setStepToError(@PathParam("stepid") int sourceid) {

        try {
            Step source = StepManager.getStepById(sourceid);
            source.setBearbeitungsstatusEnum(StepStatus.ERROR);
            source.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
            source.setBearbeitungszeitpunkt(new Date());
            source.setBearbeitungsbeginn(null);
            source.setBearbeitungsende(new Date());
            StepManager.saveStep(source);

        } catch (DAOException e) {
            String message = "An error occured: " + e.getMessage();
            return Response.serverError().entity(message).build();
        }

        return Response.ok().build();
    }

    /**
     * Sets step to error by process title and step name. Uses the first step with the step name in the process
     *
     * @param processTitle
     * @param stepName
     * @return Response
     */
    @Deprecated
    @Path("/processtitles/{processtitle}/{stepname}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response setStepToErrorByName(@PathParam("processtitle") String processTitle, @PathParam("stepname") String stepName) {
        Process p = ProcessManager.getProcessByExactTitle(processTitle);
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
        return setStepToError(so.getId());
    }
}
