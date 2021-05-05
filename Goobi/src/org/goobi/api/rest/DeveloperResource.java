package org.goobi.api.rest;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.goobi.managedbeans.DeveloperModeBean;

@Path("developer")
public class DeveloperResource {

    @Inject
    DeveloperModeBean devModeBean;

    @POST
    @Path("/reload")
    public void reload() {
        devModeBean.reload();
    }
}
