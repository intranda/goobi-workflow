package org.goobi.api.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import lombok.extern.log4j.Log4j;

@Provider
@Log4j
public class AddHeaderFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext resp) throws IOException {
        String path = req.getUriInfo().getPath();
        //TODO: get this from new config
        Map<String, List<String>> headers = new TreeMap<>();
        for (Entry<String, List<String>> headerEntry : headers.entrySet()) {
            resp.getHeaders().addAll(headerEntry.getKey(), headerEntry.getValue());
        }
        if (path.equals("processes/bla")) {
            resp.getHeaders().add("Allow-Origin", "*");
        }
    }

}
