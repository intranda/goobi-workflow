package org.goobi.api.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.beans.PluginInfo;
import org.goobi.managedbeans.PluginsBean;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@javax.ws.rs.Path("/plugins")
public class Plugins {

    @GET
    @Operation(summary="Returns the list of plugins", description="Returns a list with all plugins, containing information about these plugins")
    @ApiResponse(responseCode="200", description="OK")
    @ApiResponse(responseCode="500", description="Internal error")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<PluginInfo>> getPlugins() {
        return PluginsBean.getPluginsFromFS();
    }

}
