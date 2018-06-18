package org.goobi.api.rest;

import java.io.IOException;
import java.util.List;

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

        String pathInfo = req.getPathInfo();
        
        if(pathInfo.startsWith("/view/object/")) {
            return;
        }

        String ip = req.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = req.getRemoteAddr();
        }
        //  check against configured ip range
        if (!checkPermissions(ip, token, pathInfo)) {
//            ErrorResponse er = new ErrorResponse();
//            er.setErrorText("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.");
//            er.setResult("Error");
//            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(er).build());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.")
                                         .build());
        }
    }

    private boolean checkPermissions(String ip, String token, String pathInfo) {
        if (token == null) {
            return false;
        } else {
            List<String> commandList = WebInterfaceConfig.getCredencials(ip, token);
            if (commandList.isEmpty()) {
                return false;
            }

            for (String command : commandList) {
                if (pathInfo.startsWith(command)) {
                    return true;
                }
            }
            return false;
        }
    }

}
