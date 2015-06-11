package org.goobi.api.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    @Context
    private HttpServletRequest req;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String ip = req.getRemoteHost();
        if (ip.startsWith("127.0.0.1")) {
            ip = req.getHeader("x-forwarded-for");
            if (ip == null) {
                ip = "127.0.0.1";
            }
        }
        // TODO check against configured ip range
        if (!ip.equals("127.0.0.1")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access the resource.").build());
        }
    }

}
