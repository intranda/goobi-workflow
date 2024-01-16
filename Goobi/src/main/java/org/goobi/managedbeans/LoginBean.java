package org.goobi.managedbeans;

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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.UserRole;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named("LoginForm")
@SessionScoped
@Log4j2
public class LoginBean implements Serializable {

    private static final long serialVersionUID = -6036632431688990910L;

    private static final String RETURN_PAGE = "index";

    /**
     * The login log prefix is used in each login log output line. This makes it possible to filter the whole log file for login log output lines.
     */
    public static final String LOGIN_LOG_PREFIX = "LOGIN PROCESS: ";

    private static final String HTML_LOGIN_FIELD_ID = "login";
    private static final String WRONG_LOGIN = "wrongLogin";

    @Getter
    @Setter
    private String login;
    @Getter
    @Setter
    private String passwort;
    @Getter
    @Setter
    private User myBenutzer;
    @Getter
    @Setter
    private String passwortAendernAlt;
    @Getter
    @Setter
    private String passwortAendernNeu1;
    @Getter
    @Setter
    private String passwortAendernNeu2;
    @Setter
    private List<String> roles;
    @Getter
    private boolean useOpenIDConnect;
    @Getter
    private boolean oidcAutoRedirect;
    @Getter
    private boolean showSSOLogoutPage;
    @Getter
    @Setter
    private String ssoError;

    public LoginBean() {
        super();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        this.useOpenIDConnect = config.isUseOpenIDConnect();
        this.showSSOLogoutPage = config.isShowSSOLogoutPage();
        this.oidcAutoRedirect = this.useOpenIDConnect && config.isOIDCAutoRedirect();
    }

    public String Ausloggen() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }
        this.myBenutzer = null;
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        if (mySession != null) {
            SessionForm sessionBean = Helper.getSessionBean();
            sessionBean.updateSessionUserName(mySession, null);
            mySession.invalidate();
        }
        return RETURN_PAGE;
    }

    public String logoutExternalUser() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }

        this.myBenutzer = null;
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        Helper.getSessionBean().updateSessionUserName(mySession, this.myBenutzer);
        if (mySession != null) {
            mySession.invalidate();
        }
        return "external_index";
    }

    public void logoutOpenId() {
        this.Ausloggen();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
        String applicationPath = ec.getApplicationContextPath();
        HttpServletRequest hreq = (HttpServletRequest) ec.getRequest();
        try {
            String logoutPath = applicationPath + "/uii/logout.xhtml";
            if (config.isUseOIDCSSOLogout()) {
                URIBuilder builder = new URIBuilder(config.getOIDCLogoutEndpoint());
                String protocolDomainPort = hreq.getScheme() + "://" + hreq.getServerName() + ":" + hreq.getServerPort();
                builder.addParameter("post_logout_redirect_uri", protocolDomainPort + logoutPath);
                ec.redirect(builder.build().toString());
            } else {
                ec.redirect(logoutPath);
            }
        } catch (URISyntaxException | IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

    public String Einloggen() {

        // Prepare login
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login button was pressed");
        AlteBilderAufraeumen();
        this.myBenutzer = null;

        // Check valid login information
        if (this.login == null || this.passwort == null) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. User could not log in because login name or password was null.");
            return "";
        }
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "The login data is available for further processing.");

        // Get user account from database

        // TODO check if email or account name was used
        User user = null;
        if (EmailValidator.getInstance().isValid(login)) {
            user = LoginBean.findUserByMail(login);
        } else {
            user = LoginBean.findUserByLoginName(this.login);
        }

        if (user == null) {
            // Log output is done in findUserByLoginName()
            return "";
        }
        // check if user is allwed to log in
        if (user.getStatus() == User.UserStatus.REGISTERED) {
            // registration not finished, login not allowed
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. User could not log in because account is not activated.");
            return "";
        } else if (user.getStatus() == User.UserStatus.DELETED || user.getStatus() == User.UserStatus.INACTIVE) {
            // disabled, login not allowed
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. User could not log in because account is not active.");
            return "";
        }

        log.trace(LoginBean.LOGIN_LOG_PREFIX + "The user is able to log in (user is visible and active).");

        // Check the password
        if (!user.istPasswortKorrekt(this.passwort)) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. Password of user with login " + this.login + " was not correct.");
            return "";
        }
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Password was correct.");

        // Get the user session if this user is already logged in in an other browser tab or create a new session for the user.
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "Getting available user session or creating a new session for user...");
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        // Update or add session in the sessions manager
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "Adding or replacing session in session manager...");
        Helper.getSessionBean().updateSessionUserName(mySession, user);

        this.myBenutzer = user;
        this.myBenutzer.lazyLoad();
        roles = myBenutzer.getAllUserRoles();

        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Following user was logged in successfully:");
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login name: " + user.getLogin());
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "First name: " + user.getVorname());
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Last name: " + user.getNachname());
        log.debug(LoginBean.LOGIN_LOG_PREFIX + "Login type: " + user.getLdapGruppe().getAuthenticationType());

        String dashboard = user.getDashboardPlugin();
        if (StringUtils.isBlank(dashboard) || "null".equals(dashboard)) {
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Loading start page...");
        } else {
            log.debug(LoginBean.LOGIN_LOG_PREFIX + "Trying to load dashboard plugin: " + dashboard);
        }

        return "";
    }

    private static User findUserByLoginName(String login) {
        try {
            String userString = "login='" + MySQLHelper.escapeSql(login) + "'";
            List<User> users = UserManager.getUsers(null, userString, null, null, null);
            if (users != null && users.size() == 1 && users.get(0) != null) {
                log.debug(LoginBean.LOGIN_LOG_PREFIX + "Found user with login name " + login + " in database.");
                return users.get(0);
            } else {
                Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
                log.error(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. User with login name " + login + " does not exist.");
                return null;
            }
        } catch (DAOException exception) {
            Helper.setFehlerMeldung("Could not read database ", exception.getMessage());
            log.error(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. Could not read database in login process.");
            log.error(exception);
            return null;
        }
    }

    private static User findUserByMail(String email) {
        try {
            String userString = "email='" + MySQLHelper.escapeSql(email) + "'";
            List<User> users = UserManager.getUsers(null, userString, null, null, null);
            if (users != null && users.size() == 1 && users.get(0) != null) {
                log.debug(LoginBean.LOGIN_LOG_PREFIX + "Found user with email " + email + " in database.");
                return users.get(0);
            } else {
                Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
                log.error(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. User with email " + email + " does not exist.");
                return null;
            }
        } catch (DAOException exception) {
            Helper.setFehlerMeldung("Could not read database ", exception.getMessage());
            log.error(LoginBean.LOGIN_LOG_PREFIX + "Login canceled. Could not read database in login process.");
            log.error(exception);
            return null;
        }
    }

    public String EinloggenAls() {
        if (!hasRole(UserRole.Admin_Users_Allow_Switch.name())) {
            return RETURN_PAGE;
        }

        User currentUser = this.myBenutzer;

        this.myBenutzer = null;
        Integer loginID = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            this.myBenutzer = UserManager.getUserById(loginID);

            // Creating journal entry
            currentUser.setContent("Log in as user: " + this.myBenutzer.getNachVorname());
            currentUser.addJournalEntry();
            this.myBenutzer.setContent("User \"" + currentUser.getNachVorname() + "\" logged into this account");
            this.myBenutzer.addJournalEntry();

            /* in der Session den Login speichern */
            HttpSession session = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
            Helper.getSessionBean().updateSessionUserName(session, this.myBenutzer);
            roles = this.myBenutzer.getAllUserRoles();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not read database", e.getMessage());
            return "";
        }
        return RETURN_PAGE;
    }

    public String PasswortAendernAbbrechen() {
        return RETURN_PAGE;
    }

    public String PasswortAendernSpeichern() {

        // The user may only change the password with further authentication
        if (!this.myBenutzer.istPasswortKorrekt(this.passwortAendernAlt)) {
            Helper.setFehlerMeldung("altesPasswortFalschEingegeben");
            return "";
        }

        // Both text fields for the new password must contain the same password
        if (!this.passwortAendernNeu1.equals(this.passwortAendernNeu2)) {
            Helper.setFehlerMeldung("neuesPasswortNichtGleich");
            return "";
        }

        // The new password must fulfill the minimum password length (read from default configuration file)
        int minimumLength = ConfigurationHelper.getInstance().getMinimumPasswordLength();
        if (this.passwortAendernNeu1.length() < minimumLength) {
            Helper.setFehlerMeldung("neuesPasswortNichtLangGenug", "" + minimumLength);
            return "";
        }

        try {
            // Passwords are correct, now the new password can be stored

            if (AuthenticationType.LDAP.equals(this.myBenutzer.getLdapGruppe().getAuthenticationTypeEnum())
                    && !myBenutzer.getLdapGruppe().isReadonly()) {

                LdapAuthentication myLdap = new LdapAuthentication();
                myLdap.changeUserPassword(this.myBenutzer, this.passwortAendernAlt, this.passwortAendernNeu1);
            }
            User currentUser = UserManager.getUserById(this.myBenutzer.getId());

            UserBean.saltAndSaveUserPassword(currentUser, this.passwortAendernNeu1);

            this.myBenutzer = currentUser;

            Helper.setMeldung("passwortGeaendert");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Helper.setFehlerMeldung("ldap errror", e.getMessage());
        }
        return "";
    }

    public String BenutzerkonfigurationSpeichern() {
        try {
            UserManager.saveUser(myBenutzer);
            Helper.setMeldung(null, "", Helper.getTranslation("configurationChanged"));
            Helper.setMeldung("changesAfterLogout");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
        }
        return "";
    }

    public boolean isUseHeaderLogin() {
        return ConfigurationHelper.getInstance().isEnableHeaderLogin();
    }

    public void headerSsoLogin() throws IOException {
        ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
        ec.redirect(Helper.getBaseUrl() + "/api/login/header");
    }

    public void openIDLogin() {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
        byte[] secureBytes = new byte[64];
        new SecureRandomNumberGenerator().getSecureRandom().nextBytes(secureBytes);
        String nonce = Base64.getUrlEncoder().encodeToString(secureBytes);
        HttpSession session = (HttpSession) ec.getSession(false);
        session.setAttribute("openIDNonce", nonce);
        String applicationPath = ec.getApplicationContextPath();
        HttpServletRequest hreq = (HttpServletRequest) ec.getRequest();
        try {
            URIBuilder builder = new URIBuilder(config.getOIDCAuthEndpoint());
            builder.addParameter("client_id", config.getOIDCClientID());
            builder.addParameter("response_type", "id_token");
            builder.addParameter("redirect_uri",
                    hreq.getScheme() + "://" + hreq.getServerName() + ":" + hreq.getServerPort() + applicationPath + "/api/login/openid");
            builder.addParameter("response_mode", "form_post");
            builder.addParameter("scope", "openid");
            builder.addParameter("nonce", nonce);

            ec.redirect(builder.build().toString());
        } catch (URISyntaxException | IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

    private void AlteBilderAufraeumen() {
        /* Pages-Verzeichnis mit den temporären Images ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        List<String> dateien = StorageProvider.getInstance().list(myPfad, pngfilter);

        /* alle Dateien durchlaufen und die alten löschen */
        if (!dateien.isEmpty()) {
            for (String filename : dateien) {
                Path file = Paths.get(myPfad + filename);
                try {
                    if (System.currentTimeMillis() - StorageProvider.getInstance().getLastModifiedDate(file) > 7200000) {
                        StorageProvider.getInstance().deleteDir(file);
                    }
                } catch (IOException exception) {
                    log.warn(exception);
                }
            }
        }
    }

    private static final DirectoryStream.Filter<Path> pngfilter = path -> {
        return (path.toString().endsWith(".png"));
    };

    public int getMaximaleBerechtigung() {
        int rueckgabe = 0;
        if (this.myBenutzer != null) {
            for (Usergroup u : this.myBenutzer.getBenutzergruppen()) {
                if (u.getBerechtigung().intValue() < rueckgabe || rueckgabe == 0) {
                    rueckgabe = u.getBerechtigung().intValue();
                }
            }
        }
        return rueckgabe;
    }

    public boolean hasRole(String inRole) {
        return myBenutzer.isSuperAdmin() || (roles != null && roles.contains(inRole));
    }

    /**
     * check if any of the assigned roles is related to GoobiScript
     * 
     * @return
     */
    public boolean isHasAnyGoobiScriptRole() {
        if (roles.contains("Workflow_Processes_Allow_GoobiScript")) {
            return true;
        }
        for (String role : roles) {
            if (role.startsWith("goobiscript_")) {
                return true;
            }
        }
        return false;
    }

    /**
     * receive list of custom columns configured by current user which is sent through the VariableReplacer later on
     * 
     * @return List of Strings for each column
     */
    public List<String> getListOfCustomColumns() {
        List<String> myColumns = new ArrayList<>();
        String fields = getMyBenutzer().getCustomColumns();
        // if nothing is configured return empty list
        if (fields == null || fields.trim().length() == 0) {
            return myColumns;
        }
        // otherwise add column to list
        String[] fieldArray = fields.trim().split(",");
        for (String string : fieldArray) {
            myColumns.add(string.trim());
        }
        return myColumns;
    }

    public boolean isDisplaySortfield(String field) {
        if (StringUtils.isBlank(field)) {
            return false;
        }
        return field.startsWith("{db_meta.") || field.startsWith("{process.") || field.startsWith("{product.") || field.startsWith("{template.");
    }

    public boolean isUserCreationEnabled() {
        return ConfigurationHelper.getInstance().isEnableExternalUserLogin();
    }

    public void resetPassword() {
        if (StringUtils.isBlank(login)) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation(WRONG_LOGIN));
            return;
        }

        if (!SendMail.getInstance().getConfig().isEnableMail()) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation("mailConfigurationDisabled"));
            return;
        }

        // load user by account name or email field
        User user = null;
        if (EmailValidator.getInstance().isValid(login)) {
            user = LoginBean.findUserByMail(login);
        } else {
            user = LoginBean.findUserByLoginName(this.login);
        }

        if (user == null) {
            // Log output is done in findUserByLoginName()
            return;
        }

        // check if mail address was filled
        if (StringUtils.isBlank(user.getEmail())) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation("loginNoEmail"));
            return;
        }

        // check if value is an email
        String email = user.getEmail();
        if (!EmailValidator.getInstance().isValid(email)) {
            Helper.setFehlerMeldung(HTML_LOGIN_FIELD_ID, "", Helper.getTranslation("loginInvalidEmail"));
            return;
        }

        // generate new password
        int passwordLength = ConfigurationHelper.getInstance().getMinimumPasswordLength();
        String password = UserBean.createRandomPassword(passwordLength);
        if (AuthenticationType.LDAP.equals(user.getLdapGruppe().getAuthenticationTypeEnum()) && !user.getLdapGruppe().isReadonly()) {

            LdapAuthentication myLdap = new LdapAuthentication();
            try {
                myLdap.changeUserPassword(user, null, password);
            } catch (NoSuchAlgorithmException e) {
                log.error(e);
            }
        }
        UserBean.saltAndSaveUserPassword(user, password);

        // send mail
        String messageSubject = SendMail.getInstance().getConfig().getPasswordResetSubject();
        String messageBody =
                SendMail.getInstance().getConfig().getPasswordResetBody().replace("{password}", password).replace("{login}", user.getLogin());
        SendMail.getInstance().sendMailToUser(messageSubject, messageBody, email);
    }

    public String openUserSettings() {
        // reload current user from database to reset all changed, but not submitted values
        try {
            myBenutzer = UserManager.getUserById(myBenutzer.getId());
        } catch (DAOException e) {
            log.error(e);
        }
        return "user_config";
    }

}
