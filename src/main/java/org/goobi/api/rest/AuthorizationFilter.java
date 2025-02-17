/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.api.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.util.Strings;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.goobi.managedbeans.LoginBean;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.jgonian.ipmath.Ipv4;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6;
import com.github.jgonian.ipmath.Ipv6Range;
import com.rometools.rome.io.impl.Base64;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Provider
@PreMatching
public class AuthorizationFilter implements ContainerRequestFilter {
    @Context
    private HttpServletRequest req;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // try to get basic authentication
        String authentication = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // get token, decode it, check if token exists in db
        if (StringUtils.isNotBlank(authentication)) {

            authentication = authentication.replace("Basic ", "");
            String keyName = Base64.decode(authentication);

            String tokenHash = new Sha256Hash(keyName, ConfigurationHelper.getInstance().getApiTokenSalt(), 10000).toBase64();

            AuthenticationToken token = UserManager.getAuthenticationToken(tokenHash);
            if (token != null) {

                //if token exists, check if token has the permission to access the current request
                String methodType = requestContext.getMethod();
                String requestUri = req.getPathInfo();
                for (AuthenticationMethodDescription method : token.getMethods()) {
                    if (method.isSelected() && methodType.equalsIgnoreCase(method.getMethodType()) && Pattern.matches(method.getUrl(), requestUri)) {
                        return;
                    }
                }
            }
        }

        // get token
        String token = requestContext.getHeaderString("token");
        if (StringUtils.isBlank(token)) {
            token = req.getParameter("token");
        }

        // get jwt token
        String jwt = requestContext.getHeaderString("jwt");
        if (StringUtils.isBlank(jwt)) {
            jwt = req.getParameter("jwt");
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo == null) {
            requestContext.abortWith(Response.status(Response.Status.NOT_FOUND).entity("Not found\n").build());
            return;
        }
        String method = requestContext.getMethod();

        // allow /developer/ prefix when devMode is on
        if (ConfigurationHelper.getInstance().isDeveloping() && pathInfo.startsWith("/developer/")) {
            return;
        }

        //Always open for image, 3d object, multimedia requests and messages requests
        boolean validPath = pathInfo.startsWith("/view/object");
        validPath = validPath || pathInfo.startsWith("/view/media/");
        validPath = validPath || pathInfo.startsWith("/process/image/");
        validPath = validPath || pathInfo.startsWith("/process/pdf/");
        validPath = validPath || pathInfo.startsWith("/process/thumbs/");
        validPath = validPath || pathInfo.startsWith("/tmp/image/");
        validPath = validPath || pathInfo.startsWith("/messages/");
        validPath = validPath || pathInfo.matches("/processes/\\d+?/images.*");
        validPath = validPath || pathInfo.endsWith("/openapi.json");
        if (validPath && hasJsfContext(req)) {
            return;
        }

        String ip = req.getHeader("x-forwarded-for");
        if (ip == null) {
            ip = req.getRemoteAddr();
        }
        //all is OK until now. Now check if this is an OPTIONS request (mostly issued by browsers as preflight request for CORS).
        if ("OPTIONS".equals(method)) {
            //check the CORS config if this is allowed.
            RestEndpointConfig conf = null;
            try {
                conf = RestConfig.getConfigForPath(pathInfo);
            } catch (ConfigurationException e) {
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
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("You are not allowed to access the Goobi REST API from IP " + ip + " or your password is wrong.")
                    .build());
        }

    }

    public static boolean hasJsfContext(HttpServletRequest request) {
        if (request != null) {
            LoginBean userBean = Helper.getLoginBean();
            return (userBean != null && userBean.getMyBenutzer() != null);

        } else {
            return false;
        }
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
            return Arrays.stream(methodsClaim.asArray(String.class)).anyMatch(method::equalsIgnoreCase);

        } catch (javax.naming.ConfigurationException | JWTVerificationException e) {
            log.error(e);
            return false;
        }
    }

}
