package org.goobi.api.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.managedbeans.ExternalQueuePausedJobTypesBean;

@Path("/stepspaused")
public class PausedAutomaticStepsResource {
    @Inject
    private ExternalQueuePausedJobTypesBean pausedJobsBean;

    @GET
    @Path("/{stepName}")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean isStepPaused(@PathParam("stepName") String stepName) {
        return pausedJobsBean.isStepPaused(stepName);
    }
}
