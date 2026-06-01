/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.managedbeans;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.User;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;

import com.auth0.jwt.interfaces.DecodedJWT;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named
@ViewScoped
@Log4j2
public class PasswordResetBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String PURPOSE_CLAIM = "passwordReset";

    @Getter
    @Setter
    private String token;

    @Getter
    @Setter
    private String newPassword;

    @Getter
    @Setter
    private String confirmPassword;

    @Getter
    private boolean tokenValid;

    @Getter
    private boolean passwordSaved;

    User user; //NOSONAR: package-private for testing

    public void init() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx != null && ctx.isPostback()) {
            return;
        }
        tokenValid = false;
        if (StringUtils.isBlank(token)) {
            return;
        }
        try {
            DecodedJWT jwt = JwtHelper.verifyTokenAndReturnClaims(token);
            if (jwt == null || !PURPOSE_CLAIM.equals(jwt.getClaim("purpose").asString())) {
                return;
            }
            String userIdStr = jwt.getClaim("userId").asString();
            if (StringUtils.isBlank(userIdStr)) {
                return;
            }
            user = UserManager.getUserById(Integer.parseInt(userIdStr));
            tokenValid = (user != null);
        } catch (Exception e) { //NOSONAR: catch every exception, so its runtimes ends here
            log.error("Password reset token validation failed", e);
        }
    }

    public void saveNewPassword() {
        if (StringUtils.isBlank(token)) {
            Helper.setFehlerMeldung("pwReset_tokenInvalid");
            return;
        }
        try {
            DecodedJWT jwt = JwtHelper.verifyTokenAndReturnClaims(token);
            if (jwt == null || !PURPOSE_CLAIM.equals(jwt.getClaim("purpose").asString())) {
                Helper.setFehlerMeldung("pwReset_tokenInvalid");
                return;
            }
        } catch (Exception e) { //NOSONAR: catch every exception, so its runtimes ends here
            log.error("Password reset token re-validation failed", e);
            Helper.setFehlerMeldung("pwReset_tokenInvalid");
            return;
        }
        if (user == null) {
            Helper.setFehlerMeldung("pwReset_tokenInvalid");
            return;
        }

        int minLength = ConfigurationHelper.getInstance().getMinimumPasswordLength();
        if (StringUtils.isBlank(newPassword) || newPassword.length() < minLength) {
            Helper.setFehlerMeldung("neuesPasswortNichtLangGenug", String.valueOf(minLength));
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Helper.setFehlerMeldung("neuesPasswortNichtGleich");
            return;
        }

        if (user.getLdapGruppe() != null
                && AuthenticationType.LDAP.equals(user.getLdapGruppe().getAuthenticationTypeEnum())
                && !user.getLdapGruppe().isReadonly()) {
            LdapAuthentication ldap = new LdapAuthentication();
            try {
                ldap.changeUserPassword(user, null, newPassword);
            } catch (NoSuchAlgorithmException e) {
                log.error(e);
            }
        }

        UserBean.saltAndSaveUserPassword(user, newPassword);
        passwordSaved = true;
    }
}
