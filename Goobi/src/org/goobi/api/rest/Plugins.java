package org.goobi.api.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.beans.PluginInfo;
import org.goobi.managedbeans.PluginsBean;

@javax.ws.rs.Path("/plugins")
public class Plugins {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, List<PluginInfo>> getPlugins() {
        return PluginsBean.getPluginsFromFS();
    }

}
