package org.goobi.api.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.Provider;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.goobi.managedbeans.LoginBean;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.jgonian.ipmath.Ipv4;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6;
import com.github.jgonian.ipmath.Ipv6Range;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
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

        String jwt = requestContext.getHeaderString("jwt");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).entity("Not found\n").build());
            return;
        }
        String method = requestContext.getMethod();

        //Always open for image, 3d object, multimedia requests and messages requests
        if (pathInfo.startsWith("/view/object/")
                || pathInfo.startsWith("/view/media/")
                || pathInfo.startsWith("/process/image/")
                || pathInfo.startsWith("/messages/")
                || pathInfo.matches("/processes/\\d+?/images.*")
                || pathInfo.endsWith("/openapi.json")) {
            if (hasJsfContext(req)) {
                return;
            }
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
        if (!checkJwt(jwt, pathInfo, method) && !checkPermissions(ip, token, pathInfo, method)) {
            //            ErrorResponse er = new ErrorResponse();
            //            er.setErrorText("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.");
            //            er.setResult("Error");
            //            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity(er).build());
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.")
                    .build());
            return;
        }

    }

    public static boolean hasJsfContext(HttpServletRequest request) {
        if (request != null) {
            LoginBean userBean = Helper.getLoginBean();
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
                for (Entry<String, String> netmaskPwPair : rmc.getNetmaskPasswordPairs().entrySet()) {
                    if (Strings.isBlank(netmaskPwPair.getValue()) || netmaskPwPair.getValue().equals(token)) {
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

    /**
     * Verifies the JSON web token and checks if the "api_path" and "api_methods" claims match the actual request
     * 
     * @param jwt
     * @param path the endpoint path the request tries to use
     * @param method the HTTP method used in the request
     * @return true, if the JWT authorizes the usage of the API path and method. Else: false
     */
    public static boolean checkJwt(String jwt, String path, String method) {
        if (StringUtils.isBlank(jwt)) {
            return false;
        }
        try {
            DecodedJWT decodedJWT = JwtHelper.verifyTokenAndReturnClaims(jwt);
            Claim pathClaim = decodedJWT.getClaim("api_path");
            if (pathClaim == null || pathClaim.isNull()) {
                return false;
            }
            if (!Pattern.matches(pathClaim.asString(), path)) {
                return false;
            }
            Claim methodsClaim = decodedJWT.getClaim("api_methods");
            if (methodsClaim == null) {
                return false;
            }
            boolean methodMatch = Arrays.stream(methodsClaim.asArray(String.class))
                    .anyMatch(claimMethod -> method.equalsIgnoreCase(claimMethod));
            if (!methodMatch) {
                return false;
            }
            return true;
        } catch (javax.naming.ConfigurationException | JWTVerificationException e) {
            log.error(e);
            return false;
        }
    }

}
