package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.UserRole;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.NIOFileUtils;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.metadaten.MetadatenSperrung;
import de.sub.goobi.persistence.managers.UserManager;

@ManagedBean(name = "LoginForm")
@SessionScoped
public class LoginBean {
    private String login;
    private String passwort;
    private User myBenutzer;
    private User tempBenutzer;
    private boolean schonEingeloggt = false;
    private String passwortAendernAlt;
    private String passwortAendernNeu1;
    private String passwortAendernNeu2;
    private List<String> roles;
    
    public String Ausloggen() {
        if (this.myBenutzer != null) {
            new MetadatenSperrung().alleBenutzerSperrungenAufheben(this.myBenutzer.getId());
        }
        this.myBenutzer = null;
        this.schonEingeloggt = false;
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        temp.sessionBenutzerAktualisieren(mySession, this.myBenutzer);
        if (mySession != null) {
            mySession.invalidate();
        }
        return "index";
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
                treffer = UserManager.getUsers(null, "login='" + StringEscapeUtils.escapeSql(this.login) + "'", null, null);
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
                    if (!temp.BenutzerInAndererSessionAktiv(mySession, b)) {
                        /* in der Session den Login speichern */
                        temp.sessionBenutzerAktualisieren(mySession, b);
                        this.myBenutzer = b;
                        this.myBenutzer.lazyLoad();
                        roles = myBenutzer.getAllUserRoles();
                    } else {
                        this.schonEingeloggt = true;
                        this.tempBenutzer = b;
                    }
                } else {
                    Helper.setFehlerMeldung("passwort", "", Helper.getTranslation("wrongPassword"));
                }
            }
        }
        // checking if saved css stylesheet is available, if not replace it by something available
        //		if (this.myBenutzer != null) {
        //			String tempCss = this.myBenutzer.getCss();
        //			String newCss = new HelperForm().getCssLinkIfExists(tempCss);
        //			this.myBenutzer.setCss(newCss);
        //			return "";
        //		}
        return "";
    }

    public String NochmalEinloggen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        this.schonEingeloggt = false;
        roles = myBenutzer.getAllUserRoles();
        return "";
    }

    public String EigeneAlteSessionsAufraeumen() {
        SessionForm temp = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
        HttpSession mySession = (HttpSession) FacesContextHelper.getCurrentFacesContext().getExternalContext().getSession(false);
        temp.alteSessionsDesSelbenBenutzersAufraeumen(mySession, this.tempBenutzer);
        /* in der Session den Login speichern */
        temp.sessionBenutzerAktualisieren(mySession, this.tempBenutzer);
        this.myBenutzer = this.tempBenutzer;
        roles = myBenutzer.getAllUserRoles();
        this.schonEingeloggt = false;
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
        /* ist das aktuelle Passwort korrekt angegeben ? */
        /* ist das neue Passwort beide Male gleich angegeben? */
        if (!this.passwortAendernNeu1.equals(this.passwortAendernNeu2)) {
            Helper.setFehlerMeldung("neuesPasswortNichtGleich");
        } else {
            try {
                /* wenn alles korrekt, dann jetzt speichern */
                LdapAuthentication myLdap = new LdapAuthentication();
                myLdap.changeUserPassword(this.myBenutzer, this.passwortAendernAlt, this.passwortAendernNeu1);
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
            User temp = UserManager.getUserById(this.myBenutzer.getId());
            temp.setTabellengroesse(this.myBenutzer.getTabellengroesse());
            temp.setSessiontimeout(myBenutzer.getSessiontimeout());
            temp.setMetadatenSprache(this.myBenutzer.getMetadatenSprache());
            temp.setDisplayDeactivatedProjects(myBenutzer.isDisplayDeactivatedProjects());
            temp.setDisplayFinishedProcesses(myBenutzer.isDisplayFinishedProcesses());
            temp.setDisplaySelectBoxes(myBenutzer.isDisplaySelectBoxes());
            temp.setDisplayIdColumn(myBenutzer.isDisplayIdColumn());
            temp.setDisplayBatchColumn(myBenutzer.isDisplayBatchColumn());
            temp.setDisplayProcessDateColumn(myBenutzer.isDisplayProcessDateColumn());
            temp.setDisplayLocksColumn(myBenutzer.isDisplayLocksColumn());
            temp.setDisplaySwappingColumn(myBenutzer.isDisplaySwappingColumn());
            temp.setDisplayAutomaticTasks(myBenutzer.isDisplayAutomaticTasks());
            temp.setHideCorrectionTasks(myBenutzer.isHideCorrectionTasks());
            temp.setDisplayOnlySelectedTasks(myBenutzer.isDisplayOnlySelectedTasks());
            temp.setDisplayOnlyOpenTasks(myBenutzer.isDisplayOnlyOpenTasks());
            temp.setEmail(myBenutzer.getEmail());
            temp.setShortcutPrefix(myBenutzer.getShortcutPrefix());
            temp.setDisplayModulesColumn(myBenutzer.isDisplayModulesColumn());
            temp.setMetsEditorTime(myBenutzer.getMetsEditorTime());
            temp.setMetsDisplayHierarchy(myBenutzer.isMetsDisplayHierarchy());
            temp.setMetsDisplayPageAssignments(myBenutzer.isMetsDisplayPageAssignments());
            temp.setMetsDisplayTitle(myBenutzer.isMetsDisplayTitle());
            temp.setMetsLinkImage(myBenutzer.isMetsLinkImage());
            temp.setDisplayOtherTasks(myBenutzer.isDisplayOtherTasks());
            temp.setDisplayGridView(myBenutzer.isDisplayGridView());
            temp.setMetsDisplayProcessID(myBenutzer.isMetsDisplayProcessID());
            temp.setDisplayThumbColumn(myBenutzer.isDisplayThumbColumn());
            temp.setDisplayMetadataColumn(myBenutzer.isDisplayMetadataColumn());
            UserManager.saveUser(temp);
            this.myBenutzer = temp;
            Helper.setMeldung(null, "", Helper.getTranslation("configurationChanged"));
            Helper.setMeldung("changesAfterLogout");
        } catch (DAOException e) {
            Helper.setFehlerMeldung("could not save", e.getMessage());
        }
        return "";
    }

    private void AlteBilderAufraeumen() {
        /* Pages-Verzeichnis mit den temporären Images ermitteln */
        String myPfad = ConfigurationHelper.getTempImagesPathAsCompleteDirectory();

        List<String> dateien = NIOFileUtils.list(myPfad, pngfilter);

        /* alle Dateien durchlaufen und die alten löschen */
        if (!dateien.isEmpty()) {
            for (String filename : dateien) {
                Path file = Paths.get(myPfad + filename);
                try {
                    if ((System.currentTimeMillis() - Files.readAttributes(file, BasicFileAttributes.class).lastModifiedTime().toMillis()) > 7200000) {
                        Files.delete(file);
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
        if (this.login != null && !this.login.equals(login)) {
            this.schonEingeloggt = false;
        }
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

    public boolean isSchonEingeloggt() {
        return this.schonEingeloggt;
    }
    
    public boolean hasRole(String inRole){
    	return roles.contains(inRole);
    }
}
