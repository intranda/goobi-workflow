package org.goobi.api.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.cli.WebInterfaceConfig;

@Provider
public class AuthorizationFilter implements ContainerRequestFilter {
    @Context
    private HttpServletRequest req;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String token = requestContext.getHeaderString("token");
        if (StringUtils.isBlank(token)) {
            token = req.getParameter("token");
        }

        String ip = req.getRemoteHost();
        if (ip.startsWith("127.0.0.1")) {
            ip = req.getHeader("x-forwarded-for");
            if (ip == null) {
                ip = "127.0.0.1";
            }
        }
        //  check against configured ip range
        if (!checkPermissions(ip, token)) {
//        	 requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.")
//                     .build());
        	ErrorResponse er = new ErrorResponse();
        	er.setErrorText("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.");
        	er.setResult("Error");
        	requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(er)
                    .build());
        }
    }

    private boolean checkPermissions(String ip, String token) {
        if (token == null) {
            return false;
        } else
            return !WebInterfaceConfig.getCredencials(ip, token).isEmpty();
    }

}
