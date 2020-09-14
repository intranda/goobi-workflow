package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
 * 			- http://digiverso.com
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
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.utils.URIBuilder;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
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
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@ManagedBean(name = "LoginForm")
@SessionScoped
@Log4j2
public class LoginBean {
    private String login;
    private String passwort;
    private User myBenutzer;
    private String passwortAendernAlt;
    private String passwortAendernNeu1;
    private String passwortAendernNeu2;
    @Setter
    private List<String> roles;
    @Getter
    private boolean useOpenIDConnect;
    @Getter
    private boolean oidcAutoRedirect;
    @Getter
    @Setter
    private String ssoError;

    public LoginBean() {
        super();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        this.useOpenIDConnect = config.isUseOpenIDConnect();
        this.oidcAutoRedirect = this.useOpenIDConnect && config.isOIDCAutoRedirect();
    }

    public String Ausloggen() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }
        this.myBenutzer = null;
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        temp.sessionBenutzerAktualisieren(mySession, this.myBenutzer);
        if (mySession != null) {
            mySession.invalidate();
        }
        return "index";
    }

    public void logoutOpenId() {
        this.Ausloggen();
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        ExternalContext ec = FacesContextHelper.getCurrentFacesContext().getExternalContext();
        String applicationPath = ec.getApplicationContextPath();
        HttpServletRequest hreq = (HttpServletRequest) ec.getRequest();
        try {
            if (config.isUseOIDCSSOLogout()) {
                URIBuilder builder = new URIBuilder(config.getOIDCLogoutEndpoint());
                builder.addParameter("post_logout_redirect_uri",
                        hreq.getScheme() + "://" + hreq.getServerName() + ":" + hreq.getServerPort() + applicationPath + "/uii/logout.xhtml");
                ec.redirect(builder.build().toString());
            } else {
                ec.redirect(applicationPath + "/uii/logout.xhtml");
            }
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            log.error(e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

    public String Einloggen() {
        AlteBilderAufraeumen();
        this.myBenutzer = null;
        /* ohne Login gleich abbrechen */
        if (this.login == null) {
            Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
        } else {
            /* prüfen, ob schon ein Benutzer mit dem Login existiert */
            List<User> treffer;
            try {
                treffer = UserManager.getUsers(null, "login='" + StringEscapeUtils.escapeSql(this.login) + "'", null, null, null);
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not read database", e.getMessage());
                return "";
            }

            if (treffer == null || treffer.size() == 0) {
                Helper.setFehlerMeldung("login", "", Helper.getTranslation("wrongLogin"));
                return "";
            }

            if (treffer != null && treffer.size() > 0) {
                /* Login vorhanden, nun passwort prüfen */
                User b = treffer.get(0);
                if (b.getIsVisible() != null) {
                    Helper.setFehlerMeldung("login", "", Helper.getTranslation("loginDeleted"));
                    return "";
                }
                /* wenn der Benutzer auf inaktiv gesetzt (z.B. arbeitet er nicht mehr hier) wurde, jetzt Meldung anzeigen */
                if (!b.isIstAktiv()) {
                    Helper.setFehlerMeldung("login", "", Helper.getTranslation("loginInactive"));
                    return "";
                }

                /* wenn passwort auch richtig ist, den benutzer übernehmen */
                if (b.istPasswortKorrekt(this.passwort)) {
                    /* jetzt prüfen, ob dieser Benutzer schon in einer anderen Session eingeloggt ist */
                    SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
                    HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
                    /* in der Session den Login speichern */
                    temp.sessionBenutzerAktualisieren(mySession, b);
                    this.myBenutzer = b;
                    this.myBenutzer.lazyLoad();
                    roles = myBenutzer.getAllUserRoles();
                } else {
                    Helper.setFehlerMeldung("passwort", "", Helper.getTranslation("wrongPassword"));
                }
            }
        }
        return "";
    }

    public String EinloggenAls() {
        if (!hasRole(UserRole.Admin_Users_Allow_Switch.name())) {
            return "index";
        }
        this.myBenutzer = null;
        Integer LoginID = Integer.valueOf(Helper.getRequestParameter("ID"));
        try {
            this.myBenutzer = UserManager.getUserById(LoginID);
            /* in der Session den Login speichern */
            SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
            temp.sessionBenutzerAktualisieren((HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false),
                    this.myBenutzer);
            roles = myBenutzer.getAllUserRoles();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not read database", e.getMessage());
            return "";
        }
        return "index";
    }

    public String PasswortAendernAbbrechen() {
        return "index";
    }

    public String PasswortAendernSpeichern() {
    	/* Das alte Passwort muss noch einmal richtig angegeben werden */
    	if (!this.myBenutzer.istPasswortKorrekt(this.passwortAendernAlt)) {
    		//Fehler ausgeben?!
    		//was kommt da rein?
    		Helper.setFehlerMeldung("");
    		return "";
    	}
        /* ist das aktuelle Passwort korrekt angegeben ? */
        /* ist das neue Passwort beide Male gleich angegeben? */
        if (!this.passwortAendernNeu1.equals(this.passwortAendernNeu2)) {
            Helper.setFehlerMeldung("neuesPasswortNichtGleich");
        } else {
            try {
                /* wenn alles korrekt, dann jetzt speichern */

                if (AuthenticationType.LDAP.equals(myBenutzer.getLdapGruppe().getAuthenticationTypeEnum()) && !myBenutzer.getLdapGruppe().isReadonly()) {

                    LdapAuthentication myLdap = new LdapAuthentication();
                    myLdap.changeUserPassword(this.myBenutzer, this.passwortAendernAlt, this.passwortAendernNeu1);
                }
                User temp = UserManager.getUserById(this.myBenutzer.getId());
                // TODO
                //                temp.setPasswortCrypt(this.passwortAendernNeu1);

                RandomNumberGenerator rng = new SecureRandomNumberGenerator();
                Object salt = rng.nextBytes();
                temp.setPasswordSalt(salt.toString());
                temp.setEncryptedPassword(temp.getPasswordHash(this.passwortAendernNeu1));
                UserManager.saveUser(temp);
                this.myBenutzer = temp;

                Helper.setMeldung("passwortGeaendert");
            } catch (DAOException e) {
                Helper.setFehlerMeldung("could not save", e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                Helper.setFehlerMeldung("ldap errror", e.getMessage());
            }
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
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            log.error(e);
        } catch (IOException e) {
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
                } catch (IOException e) {
                }
            }
        }
    }

    private static final DirectoryStream.Filter<Path> pngfilter = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path path) {
            return (path.toString().endsWith(".png"));
        }
    };

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswort() {
        return this.passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    public User getMyBenutzer() {
        return this.myBenutzer;
    }

    public void setMyBenutzer(User myClass) {
        this.myBenutzer = myClass;
    }

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

    public String getPasswortAendernAlt() {
        return this.passwortAendernAlt;
    }

    public void setPasswortAendernAlt(String passwortAendernAlt) {
        this.passwortAendernAlt = passwortAendernAlt;
    }

    public String getPasswortAendernNeu1() {
        return this.passwortAendernNeu1;
    }

    public void setPasswortAendernNeu1(String passwortAendernNeu1) {
        this.passwortAendernNeu1 = passwortAendernNeu1;
    }

    public String getPasswortAendernNeu2() {
        return this.passwortAendernNeu2;
    }

    public void setPasswortAendernNeu2(String passwortAendernNeu2) {
        this.passwortAendernNeu2 = passwortAendernNeu2;
    }

    public boolean hasRole(String inRole) {
        return roles != null && roles.contains(inRole);
    }

    /**
     * receive list of custom columns configured by current user which is sent through the VariableReplacer later on
     * 
     * @return List of Strings for each column
     */
    public List<String> getListOfCustomColumns() {
        List<String> myColumns = new ArrayList<>();
        LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        String fields = login.getMyBenutzer().getCustomColumns();
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
}
