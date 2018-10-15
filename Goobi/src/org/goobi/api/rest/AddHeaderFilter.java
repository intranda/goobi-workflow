package org.goobi.api.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration.ConfigurationException;

import lombok.extern.log4j.Log4j;

@Provider
@Log4j
public class AddHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
        if (resp.getStatus() >= 400) {
            return;
        }
        String path = req.getUriInfo().getPath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        String method = req.getMethod();
        RestEndpointConfig rec = null;
        try {
            rec = RestConfig.getConfigForPath(path);
        } catch (ConfigurationException e) {
            log.error(e);
        }
        if (rec != null) {
            if (rec.getCorsMethods().contains(method)) {
                resp.getHeaders().addAll("Allow-Origin", rec.getCorsOrigins().toArray());
            }
            for (RestMethodConfig rmc : rec.getMethodConfigs()) {
                if (rmc.getMethod().equalsIgnoreCase(method)) {
                    for (Entry<String, List<String>> headerEntry : rmc.getCustomHeaders().entrySet()) {
                        resp.getHeaders().addAll(headerEntry.getKey(), headerEntry.getValue().toArray());
                    }
                }
            }
        }
    }

}
