package org.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.goobi.api.mail.UserProjectConfiguration;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.encryption.DesEncrypter;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import jgravatar.Gravatar;
import jgravatar.GravatarRating;
import lombok.Getter;
import lombok.Setter;

public class User implements DatabaseObject {

    private static final Logger logger = Logger.getLogger(User.class);
    @Getter @Setter private Integer id;
    @Getter @Setter private String vorname;
    @Getter @Setter private String nachname;
    @Getter @Setter private String login;
    @Getter @Setter private String ldaplogin;
    @Getter @Setter private String passwort;
    @Getter @Setter private boolean istAktiv = true;
    @Getter @Setter private String isVisible;
    @Getter @Setter private String standort;
    private Integer tabellengroesse = Integer.valueOf(10);
    private Integer sessiontimeout = 7200;
    //	private boolean confVorgangsdatumAnzeigen = false;
    @Getter @Setter private String metadatenSprache;
    private List<Usergroup> benutzergruppen;
    @Getter @Setter private List<Step> schritte;
    @Getter @Setter private List<Step> bearbeitungsschritte;
    private List<Project> projekte;
    @Getter @Setter private List<UserProperty> eigenschaften;
    @Getter @Setter private boolean mitMassendownload = false;
    @Getter @Setter private Ldap ldapGruppe;
    private String css;
    @Getter @Setter private String email;
    @Getter @Setter private String shortcutPrefix = "ctrl+shift";

    private String encryptedPassword;
    private String passwordSalt;

    @Getter @Setter private boolean displayDeactivatedProjects = false;
    @Getter @Setter private boolean displayFinishedProcesses = false;
    @Getter @Setter private boolean displaySelectBoxes = false;
    @Getter @Setter private boolean displayIdColumn = false;
    @Getter @Setter private boolean displayBatchColumn = false;
    @Getter @Setter private boolean displayProcessDateColumn = false;
    @Getter @Setter private boolean displayLocksColumn = false;
    @Getter @Setter private boolean displaySwappingColumn = false;
    @Getter @Setter private boolean displayModulesColumn = false;
    @Getter @Setter private boolean displayMetadataColumn = false;
    @Getter @Setter private boolean displayThumbColumn = false;
    @Getter @Setter private boolean displayGridView = false;

    @Getter @Setter private boolean displayAutomaticTasks = false;
    @Getter @Setter private boolean hideCorrectionTasks = false;
    @Getter @Setter private boolean displayOnlySelectedTasks = false;
    @Getter @Setter private boolean displayOnlyOpenTasks = false;
    @Getter @Setter private boolean displayOtherTasks = false;

    @Getter @Setter private boolean metsDisplayTitle = false;
    @Getter @Setter private boolean metsLinkImage = false;
    @Getter @Setter private boolean metsDisplayPageAssignments = false;
    @Getter @Setter private boolean metsDisplayHierarchy = false;
    @Getter @Setter private boolean metsDisplayProcessID = false;

    @Getter @Setter private Integer metsEditorTime;

    @Getter private static final int IMAGE_SIZE = 27;


    @Getter @Setter private String customColumns;
    @Getter @Setter private String customCss;


    @Getter @Setter
    private List<UserProjectConfiguration> emailConfiguration;

    @Override
    public void lazyLoad() {
        try {
            this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
            this.projekte = ProjectManager.getProjectsForUser(this, false);

            emailConfiguration = UserManager.getEmailConfigurationForUser(projekte, id);

        } catch (DAOException e) {
            logger.error("error during lazy loading of User", e);
        }
    }

    public String getPasswortCrypt() {
        DesEncrypter encrypter = new DesEncrypter();
        String decrypted = encrypter.decrypt(this.passwort);
        return decrypted;
    }

    public void setPasswortCrypt(String inpasswort) {
        DesEncrypter encrypter = new DesEncrypter();
        String encrypted = encrypter.encrypt(inpasswort);
        this.passwort = encrypted;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Object getPasswordSalt() {
        return passwordSalt;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Integer getTabellengroesse() {
        if (this.tabellengroesse == null) {
            return Integer.valueOf(10);
        }
        return this.tabellengroesse;
    }

    public void setTabellengroesse(Integer tabellengroesse) {
        this.tabellengroesse = tabellengroesse;
    }

    public int getBenutzergruppenSize() {
        if (this.benutzergruppen == null) {
            return 0;
        } else {
            return this.benutzergruppen.size();
        }
    }

    public void setBenutzergruppen(List<Usergroup> benutzergruppen) {
        this.benutzergruppen = benutzergruppen;
    }

    public List<Usergroup> getBenutzergruppen() {
        if (benutzergruppen == null || benutzergruppen.size() == 0) {
            try {
                this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        return benutzergruppen;
    }

    public int getSchritteSize() {
        if (this.schritte == null) {
            return 0;
        } else {
            return this.schritte.size();
        }
    }

    public int getBearbeitungsschritteSize() {
        if (this.bearbeitungsschritte == null) {
            return 0;
        } else {
            return this.bearbeitungsschritte.size();
        }
    }

    public int getProjekteSize() {
        if (this.projekte == null) {
            return 0;
        } else {
            return this.projekte.size();
        }
    }

    public void setProjekte(List<Project> projekte) {
        this.projekte = projekte;
    }

    public List<Project> getProjekte() {
        if (projekte == null || projekte.size() == 0) {
            try {
                this.projekte = ProjectManager.getProjectsForUser(this, false);
            } catch (DAOException e) {
                logger.error(e);
            }
        }

        return this.projekte;
    }

    //	public boolean isConfVorgangsdatumAnzeigen() {
    //		return this.confVorgangsdatumAnzeigen;
    //	}
    //
    //	public void setConfVorgangsdatumAnzeigen(boolean confVorgangsdatumAnzeigen) {
    //		this.confVorgangsdatumAnzeigen = confVorgangsdatumAnzeigen;
    //	}

    public boolean istPasswortKorrekt(String inPasswort) {
        if (inPasswort == null || inPasswort.length() == 0) {
            return false;
        } else {

            /* Verbindung zum LDAP-Server aufnehmen und Login prüfen, wenn LDAP genutzt wird */
            if (ConfigurationHelper.getInstance().isUseLdap()) {
                LdapAuthentication myldap = new LdapAuthentication();
                return myldap.isUserPasswordCorrect(this, inPasswort);
            } else {
                String hashedPasswordBase64 = new Sha256Hash(inPasswort, passwordSalt, 10000).toBase64();
                return this.encryptedPassword.equals(hashedPasswordBase64);
            }
        }
    }

    public String getPasswordHash(String plainTextPassword) {
        String hashedPasswordBase64 = new Sha256Hash(plainTextPassword, passwordSalt, 10000).toBase64();
        return hashedPasswordBase64;

    }

    public String getNachVorname() {
        return this.nachname + ", " + this.vorname;
    }

    /**
     * BenutzerHome ermitteln und zurückgeben (entweder aus dem LDAP oder direkt aus der Konfiguration)
     * 
     * @return Path as String
     * @throws InterruptedException
     * @throws IOException
     */
    public String getHomeDir() throws IOException, InterruptedException {
        String rueckgabe = "";
        /* wenn LDAP genutzt wird, HomeDir aus LDAP ermitteln, ansonsten aus der Konfiguration */

        if (ConfigurationHelper.getInstance().isUseLdap()) {
            LdapAuthentication myldap = new LdapAuthentication();
            rueckgabe = myldap.getUserHomeDirectory(this);
        } else {
            rueckgabe = ConfigurationHelper.getInstance().getUserFolder() + this.login;
        }

        if (rueckgabe.equals("")) {
            return "";
        }

        if (!rueckgabe.endsWith(FileSystems.getDefault().getSeparator())) {
            rueckgabe += FileSystems.getDefault().getSeparator();
        }
        /* wenn das Verzeichnis nicht "" ist, aber noch nicht existiert, dann jetzt anlegen */
        FilesystemHelper.createDirectoryForUser(rueckgabe, login);
        return rueckgabe;
    }

    public Integer getSessiontimeout() {
        if (this.sessiontimeout == null) {
            this.sessiontimeout = 7200;
        }
        return this.sessiontimeout;
    }

    public void setSessiontimeout(Integer sessiontimeout) {
        this.sessiontimeout = sessiontimeout;
    }

    public Integer getSessiontimeoutInMinutes() {
        return getSessiontimeout() / 60;
    }

    public void setSessiontimeoutInMinutes(Integer sessiontimeout) {
        if (sessiontimeout.intValue() < 5) {
            this.sessiontimeout = 5 * 60;
        } else {
            this.sessiontimeout = sessiontimeout * 60;
        }
    }

    public String getCss() {
        if (this.css == null || this.css.length() == 0) {
            this.css = "/css/default.css";
        }
        return this.css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public int getEigenschaftenSize() {

        if (this.eigenschaften == null) {
            return 0;
        } else {
            return this.eigenschaften.size();
        }
    }

    /**
     * 
     * @return List of filters as strings
     */

    public List<String> getFilters() {
        return UserManager.getFilters(this.id);
    }

    /**
     * adds a new filter to list
     * 
     * @param inFilter the filter to add
     */

    public void addFilter(String inFilter) {
        UserManager.addFilter(this.id, inFilter);
    }

    /**
     * removes filter from list
     * 
     * @param inFilter the filter to remove
     */
    public void removeFilter(String inFilter) {
        UserManager.removeFilter(this.id, inFilter);
    }

    /**
     * The function selfDestruct() removes a user from the environment. Since the user ID may still be referenced somewhere, the user is not hard
     * deleted from the database, instead the account is set inactive and invisible.
     * 
     * To allow recreation of an account with the same login the login is cleaned - otherwise it would be blocked eternally by the login existence
     * test performed in the BenutzerverwaltungForm.Speichern() function. In addition, all personally identifiable information is removed from the
     * database as well.
     */

    public User selfDestruct() {
        this.isVisible = "deleted";
        this.login = null;
        this.istAktiv = false;
        this.vorname = null;
        this.nachname = null;
        this.standort = null;
        this.benutzergruppen = null;
        this.projekte = null;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((isVisible == null) ? 0 : isVisible.hashCode());
        result = prime * result + (istAktiv ? 1231 : 1237);
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((nachname == null) ? 0 : nachname.hashCode());
        result = prime * result + ((vorname == null) ? 0 : vorname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        User other = (User) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (isVisible == null) {
            if (other.isVisible != null) {
                return false;
            }
        } else if (!isVisible.equals(other.isVisible)) {
            return false;
        }
        if (istAktiv != other.istAktiv) {
            return false;
        }
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (nachname == null) {
            if (other.nachname != null) {
                return false;
            }
        } else if (!nachname.equals(other.nachname)) {
            return false;
        }
        if (vorname == null) {
            if (other.vorname != null) {
                return false;
            }
        } else if (!vorname.equals(other.vorname)) {
            return false;
        }
        return true;
    }


    public void setImageUrl(String url) {

    }

    public String getImageUrl() {
        if (StringUtils.isNotEmpty(email) && ConfigurationHelper.getInstance().isAllowGravatar()) {
            Gravatar gravatar = new Gravatar();
            gravatar.setSize(IMAGE_SIZE);
            gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
            String url = gravatar.getUrl(email).replaceAll("http://", "https://");
            url = url.replace("d=404", "d=https://www.gravatar.com/avatar/92bb3cacd091cbee44637e73f2ea1f7c.jpg?s=27");
            return url;
        }
        return null;
    }

    public List<String> getAllUserRoles() {
        Set<String> hs = new HashSet<>();
        for (Usergroup group : getBenutzergruppen()) {
            hs.addAll(group.getUserRoles());
        }
        List<String> roles = new ArrayList<>();
        roles.addAll(hs);
        Collections.sort(roles);
        return roles;
    }

}
