package org.goobi.api.rest;

import java.io.IOException;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.goobi.managedbeans.LoginBean;

import com.github.jgonian.ipmath.Ipv4;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6;
import com.github.jgonian.ipmath.Ipv6Range;

import lombok.extern.log4j.Log4j;

@Log4j
@Provider
@PreMatching
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
        String method = requestContext.getMethod();

        //Always open for image and 3d obejct requests
        if (pathInfo.startsWith("/view/object/") || pathInfo.startsWith("/image/")) {
            if (!hasJsfContext(req)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not allowed to access the Goobi REST API")
                        .build());
                return;
            }
            return;
        }

        String ip = req.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = req.getRemoteAddr();
        }
        //all is OK until now. Now check if this is an OPTIONS request (mostly issued by browsers as preflight request for CORS). 
        if (method.equals("OPTIONS")) {
            //check the CORS config if this is allowed. 
            RestEndpointConfig conf = null;
            try {
                conf = RestConfig.getConfigForPath(pathInfo);
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                log.error(e);
            }
            if (conf != null && !conf.getCorsMethods().isEmpty()) {
                ResponseBuilder respB = Response.status(Response.Status.NO_CONTENT);
                respB.header("Access-Control-Allow-Methods", StringUtils.join(conf.getCorsMethods(), ", "));
                respB.header("Access-Control-Allow-Origin", StringUtils.join(conf.getCorsOrigins(), ", "));
                respB.header("Access-Control-Allow-Headers", "origin,content-type,accept,token");
                requestContext.abortWith(respB.build());
            } else {
                requestContext.abortWith(Response.status(Response.Status.NO_CONTENT).build());
            }
            return;
        }
        //  check against configured ip range
        if (!checkPermissions(ip, token, pathInfo, method)) {
            //            ErrorResponse er = new ErrorResponse();
            //            er.setErrorText("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.");
            //            er.setResult("Error");
            //            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(er).build());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("You are not allowed to access the Goobi REST API from IP "
                    + ip + " or your password is wrong.")
                    .build());
            return;
        }

    }

    public static boolean hasJsfContext(HttpServletRequest request) {
        if (request != null) {
            HttpSession session = request.getSession();
            LoginBean userBean = (LoginBean) session.getAttribute("LoginForm");
            return (userBean != null && userBean.getMyBenutzer() != null);

        } else {
            return false;
        }
        //        return FacesContext.getCurrentInstance() != null;
    }

    public static boolean checkPermissions(String ip, String token, String path, String method) {
        RestEndpointConfig conf = null;
        try {
            conf = RestConfig.getConfigForPath(path);
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
        if (conf == null || conf.getMethodConfigs() == null) {
            return false;
        }
        for (RestMethodConfig rmc : conf.getMethodConfigs()) {
            if (rmc.getMethod().equalsIgnoreCase(method)) {
                if (rmc.isAllAllowed()) {
                    return true;
                }
                if (token == null) {
                    continue;
                }
                for (Entry<String, String> netmaskPwPair : rmc.getNetmaskPasswordPairs().entrySet()) {
                    if (token.equals(netmaskPwPair.getValue())) {
                        String netMask = netmaskPwPair.getKey();
                        if (netMask.contains(":") && ip.contains(":") && Ipv6Range.parse(netMask).contains(Ipv6.parse(ip))) {
                            return true;
                        }
                        if (netMask.contains(".") && ip.contains(".") && Ipv4Range.parse(netMask).contains(Ipv4.parse(ip))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
