package org.goobi.api.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.goobi.beans.User;
import org.goobi.managedbeans.LoginBean;

import com.auth0.jwt.interfaces.DecodedJWT;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.extern.log4j.Log4j;

@Log4j
@Path("/login")
public class Login {
    @Context
    private HttpServletRequest servletRequest;
    @Context
    private HttpServletResponse servletResponse;

    @Path("/openid")
    @POST
    public void openIdLogin(@FormParam("error") String error, @FormParam("id_token") String idToken) {
        String clientID = ConfigurationHelper.getInstance().getOIDCClientID();
        String nonce = (String) servletRequest.getSession().getAttribute("openIDNonce");
        if (error == null) {
            // no error - we should have a token. Verify it.
            DecodedJWT jwt = JwtHelper.verifyOpenIdToken(idToken);
            if (jwt != null) {
                // now check if the nonce is the same as in the old session
                if (nonce.equals(jwt.getClaim("nonce").asString()) && clientID.equals(jwt.getClaim("aud").asString())) {
                    //all OK, login the user
                    LoginBean userBean = (LoginBean) servletRequest.getSession().getAttribute("LoginForm");
                    //TODO: get the user by something from the JWT here.
                    String login = jwt.getClaim("email").asString();
                    User user = UserManager.getUserByLogin(login);
                    user.lazyLoad();
                    userBean.setMyBenutzer(user);
                    userBean.setRoles(user.getAllUserRoles());
                    userBean.setMyBenutzer(user);
                    //add the user to the sessionform that holds information about all logged in users
                    SessionForm temp = (SessionForm) servletRequest.getServletContext().getAttribute("SessionForm");
                    temp.sessionBenutzerAktualisieren(servletRequest.getSession(), user);
                }
            }
        } else {
            log.error(error);
        }
        try {
            servletResponse.sendRedirect("/goobi/index.xhtml");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }
}
