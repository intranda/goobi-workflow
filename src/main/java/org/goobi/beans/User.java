package org.goobi.beans;

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
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.api.rest.AuthenticationToken;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.encryption.DesEncrypter;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import jakarta.faces.model.SelectItem;
import jgravatar.Gravatar;
import jgravatar.GravatarRating;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class User extends AbstractJournal implements DatabaseObject, Serializable {

    private static final long serialVersionUID = -1540863402168133130L;

    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String vorname;
    @Getter
    @Setter
    private String nachname;
    @Getter
    @Setter
    private String login;
    @Getter
    @Setter
    private String ldaplogin;
    @Getter
    @Setter
    private String passwort;
    @Getter
    @Setter
    private String standort;
    @Setter
    private Integer tabellengroesse = Integer.valueOf(10);
    @Setter
    private Integer sessiontimeout = 14400;
    @Getter
    @Setter
    private String metadatenSprache;
    @Setter
    private List<Usergroup> benutzergruppen;
    @Getter
    @Setter
    private List<Step> schritte;
    @Getter
    @Setter
    private List<Step> bearbeitungsschritte;
    @Setter
    private List<Project> projekte;
    @Getter
    @Setter
    private List<UserProperty> eigenschaften;
    @Getter
    @Setter
    private boolean mitMassendownload = false;
    @Getter
    @Setter
    private Ldap ldapGruppe;
    @Setter
    private String css;
    @Getter
    @Setter
    private String email;
    @Getter
    @Setter
    private String shortcutPrefix = "ctrl+shift";

    @Getter
    @Setter
    private String encryptedPassword;
    @Getter
    @Setter
    private String passwordSalt;

    @Getter
    @Setter
    private boolean displayDeactivatedProjects = false;
    @Getter
    @Setter
    private boolean displayFinishedProcesses = false;
    @Getter
    @Setter
    private boolean displaySelectBoxes = false;
    @Getter
    @Setter
    private boolean displayIdColumn = false;
    @Getter
    @Setter
    private boolean displayBatchColumn = false;
    @Getter
    @Setter
    private boolean displayProcessDateColumn = false;
    @Getter
    @Setter
    private boolean displayLocksColumn = false;
    @Getter
    @Setter
    private boolean displaySwappingColumn = false;
    @Getter
    @Setter
    private boolean displayModulesColumn = false;
    @Getter
    @Setter
    private boolean displayMetadataColumn = false;
    @Getter
    @Setter
    private boolean displayThumbColumn = false;
    @Getter
    @Setter
    private boolean displayGridView = false;
    @Getter
    @Setter
    private boolean displayRulesetColumn = false;
    @Getter
    @Setter
    private boolean displayNumberOfImages = false;

    @Getter
    @Setter
    private boolean displayAutomaticTasks = false;
    @Getter
    @Setter
    private boolean hideCorrectionTasks = false;
    @Getter
    @Setter
    private boolean displayOnlySelectedTasks = false;
    @Getter
    @Setter
    private boolean displayOnlyOpenTasks = false;
    @Getter
    @Setter
    private boolean displayOtherTasks = false;

    @Getter
    @Setter
    private boolean metsDisplayTitle = false;
    @Getter
    @Setter
    private boolean metsLinkImage = false;
    @Getter
    @Setter
    private boolean metsDisplayPageAssignments = false;
    @Getter
    @Setter
    private boolean metsDisplayHierarchy = false;
    @Getter
    @Setter
    private boolean metsDisplayProcessID = false;

    @Getter
    @Setter
    private Integer metsEditorTime;

    @Getter
    private static final int IMAGE_SIZE = 27;

    @Getter
    @Setter
    private String customColumns;
    @Getter
    @Setter
    private String customCss;

    @Getter
    @Setter
    private String mailNotificationLanguage;

    @Setter
    private List<UserProjectConfiguration> emailConfiguration;

    @Setter
    private Institution institution;
    @Getter
    @Setter
    private Integer institutionId;

    @Getter
    @Setter
    private boolean superAdmin;

    @Getter
    @Setter
    private boolean displayInstitutionColumn = false;

    @Getter
    @Setter
    private String dashboardPlugin;
    @Getter
    @Setter
    private String ssoId;

    @Getter
    @Setter
    private String processListDefaultSortField = "prozesse.titel";
    @Getter
    @Setter
    private String processListDefaultSortOrder = " asc";

    @Getter
    @Setter
    private String taskListDefaultSortingField = "prioritaet";
    @Getter
    @Setter
    private String taskListDefaultSortOrder = " desc";

    @Getter
    @Setter
    private boolean displayLastEditionDate = false;

    @Getter
    @Setter
    private boolean displayLastEditionUser = false;

    @Getter
    @Setter
    private boolean displayLastEditionTask = false;

    @Getter
    @Setter
    private String dashboardConfiguration;

    @Getter
    @Setter
    private String uiMode;

    @Getter
    @Setter
    private String additionalSearchFields;

    @Getter
    @Setter
    private UserStatus status = UserStatus.ACTIVE;

    private List<SelectItem> availableUiModes = null;

    @Getter
    @Setter
    private transient List<AuthenticationToken> apiToken = new ArrayList<>();

    @Getter
    @Setter
    private AuthenticationToken token;

    @Getter
    @Setter
    // any additional data is hold in a map and gets stored in an xml column, it is searchable using xpath
    // individual values can be extracted: 'select ExtractValue(additional_data, '/root/key') from benutzer'
    private Map<String, String> additionalData = new HashMap<>();

    @Override
    public void lazyLoad() {
        try {
            this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
            this.projekte = ProjectManager.getProjectsForUser(this, false);
        } catch (DAOException e) {
            log.error("error during lazy loading of User", e);
        }
    }

    public String getPasswortCrypt() {
        DesEncrypter encrypter = new DesEncrypter();
        return encrypter.decrypt(this.passwort);
    }

    public void setPasswortCrypt(String inpasswort) {
        DesEncrypter encrypter = new DesEncrypter();
        String encrypted = encrypter.encrypt(inpasswort);
        this.passwort = encrypted;
    }

    public Integer getTabellengroesse() {
        if (this.tabellengroesse == null) {
            return Integer.valueOf(10);
        }
        return this.tabellengroesse;
    }

    public int getBenutzergruppenSize() {
        if (this.benutzergruppen == null) {
            return 0;
        } else {
            return this.benutzergruppen.size();
        }
    }

    public List<Usergroup> getBenutzergruppen() {
        if (benutzergruppen == null || benutzergruppen.isEmpty()) {
            try {
                this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
            } catch (DAOException e) {
                log.error(e);
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

    public List<Project> getProjekte() {
        if (projekte == null || projekte.isEmpty()) {
            try {
                this.projekte = ProjectManager.getProjectsForUser(this, false);
            } catch (DAOException e) {
                log.error(e);
            }
        }

        return this.projekte;
    }

    public Institution getInstitution() {
        if (institution == null && institutionId != null && institutionId.intValue() != 0) {
            institution = InstitutionManager.getInstitutionById(institutionId);
        }
        return institution;
    }

    public boolean istPasswortKorrekt(String inPasswort) {
        if (inPasswort == null || inPasswort.length() == 0) {
            return false;
        } else /* Verbindung zum LDAP-Server aufnehmen und Login prüfen, wenn LDAP genutzt wird */
        if (ldapGruppe.getAuthenticationTypeEnum() == AuthenticationType.LDAP) {
            LdapAuthentication myldap = new LdapAuthentication();
            return myldap.isUserPasswordCorrect(this, inPasswort);
        } else {
            String hashedPasswordBase64 = new Sha256Hash(inPasswort, passwordSalt, 10000).toBase64();
            return this.encryptedPassword.equals(hashedPasswordBase64);
        }
    }

    public String getPasswordHash(String plainTextPassword) {
        return new Sha256Hash(plainTextPassword, passwordSalt, 10000).toBase64();
    }

    public String getNachVorname() {
        return this.nachname + ", " + this.vorname;
    }

    public String getFirstProjectTitle() {
        if (this.projekte != null && !this.projekte.isEmpty()) {
            return this.projekte.get(0).getTitel();
        } else {
            return "";
        }
    }

    public String getFirstUserGroupTitle() {
        if (this.benutzergruppen != null && !this.benutzergruppen.isEmpty()) {
            return this.benutzergruppen.get(0).getTitel();
        } else {
            return "";
        }
    }

    public String getInstitutionName() {
        // The getter must be here because the getter initializes the institution when it is null
        return this.getInstitution().getShortName();
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

        if (ldapGruppe.getAuthenticationTypeEnum() == AuthenticationType.LDAP) {
            LdapAuthentication myldap = new LdapAuthentication();
            rueckgabe = myldap.getUserHomeDirectory(this);
        } else {
            rueckgabe = ConfigurationHelper.getInstance().getUserFolder() + this.login;
        }

        if ("".equals(rueckgabe)) {
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
            this.sessiontimeout = 14400;
        }
        return this.sessiontimeout;
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
        this.login = null;
        this.vorname = null;
        this.nachname = null;
        this.standort = null;
        this.benutzergruppen = null;
        this.projekte = null;
        this.status = UserStatus.DELETED;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
            String url = gravatar.getUrl(email).replace("http://", "https://");
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

    public List<UserProjectConfiguration> getEmailConfiguration() {
        if (emailConfiguration == null) {
            emailConfiguration = UserManager.getEmailConfigurationForUser(projekte, id, getAllUserRoles().contains("Admin_All_Mail_Notifications"));
        }
        return emailConfiguration;
    }

    public List<SelectItem> getAvailableUiModes() {
        if (availableUiModes == null) {
            availableUiModes = new ArrayList<>();
            availableUiModes.add(new SelectItem("regular", Helper.getTranslation("user_ui_mode_regular")));
            availableUiModes.add(new SelectItem("low_vision", Helper.getTranslation("user_ui_mode_low_vision")));
            availableUiModes.add(new SelectItem("accessibility", Helper.getTranslation("user_ui_mode_accessibility_compatible")));
        }
        return availableUiModes;
    }

    public List<SelectItem> getAvailableDashboards() {
        List<SelectItem> dashboards = new ArrayList<>();
        Institution currentInstitution = Helper.getCurrentUser().getInstitution();
        List<InstitutionConfigurationObject> configuredDashboards = currentInstitution.getAllowedDashboardPlugins();
        dashboards.add(new SelectItem("", Helper.getTranslation("user_noDashboad")));
        for (InstitutionConfigurationObject ico : configuredDashboards) {
            if (currentInstitution.isAllowAllPlugins() || ico.isSelected()) {
                dashboards.add(new SelectItem(ico.getObject_name(), Helper.getTranslation(ico.getObject_name())));
            }
        }
        return dashboards;
    }

    public List<SelectItem> getTaskListColumnNames() {
        List<SelectItem> taskList = new ArrayList<>();
        taskList.add(new SelectItem("prioritaet", Helper.getTranslation("prioritaet")));
        if (isDisplayIdColumn()) {
            taskList.add(new SelectItem("prioritaet desc, prozesse.ProzesseID", Helper.getTranslation("id")));
        }
        taskList.add(new SelectItem("prioritaet desc, schritte.titel", Helper.getTranslation("arbeitsschritt")));
        taskList.add(new SelectItem("prioritaet desc, prozesse.titel", Helper.getTranslation("prozessTitel")));
        if (isDisplayProcessDateColumn()) {
            taskList.add(new SelectItem("prioritaet desc, prozesse.erstellungsdatum", Helper.getTranslation("vorgangsdatum")));
        }
        taskList.add(new SelectItem("prioritaet desc, projekte.titel", Helper.getTranslation("projekt")));
        if (isDisplayInstitutionColumn()) {
            taskList.add(new SelectItem("prioritaet desc, institution.shortName", Helper.getTranslation("institution")));
        }
        if (isDisplayBatchColumn()) {
            taskList.add(new SelectItem("prioritaet desc, prozesse.batchID", Helper.getTranslation("batch")));
        }
        return taskList;
    }

    public List<SelectItem> getProcessListColumnNames() {
        List<SelectItem> taskList = new ArrayList<>();
        if (isDisplayIdColumn()) {
            taskList.add(new SelectItem("prozesse.ProzesseID", Helper.getTranslation("id")));
        }
        if (isDisplayBatchColumn()) {
            taskList.add(new SelectItem("prozesse.batchID", Helper.getTranslation("batch")));
        }
        taskList.add(new SelectItem("prozesse.titel", Helper.getTranslation("titel")));

        if (isDisplayProcessDateColumn()) {
            taskList.add(new SelectItem("prozesse.erstellungsdatum", Helper.getTranslation("vorgangsdatum")));
        }
        taskList.add(new SelectItem("prozesse.sortHelperStatus", Helper.getTranslation("status")));
        taskList.add(new SelectItem("projekte.titel", Helper.getTranslation("projekt")));

        if (isDisplayInstitutionColumn()) {
            taskList.add(new SelectItem("institution.shortName", Helper.getTranslation("institution")));
        }

        return taskList;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void setActive(boolean active) {
        if (active) {
            status = UserStatus.ACTIVE;
        } else if (status == UserStatus.ACTIVE) {
            status = UserStatus.INACTIVE;
        }
    }

    public enum UserStatus {
        REGISTERED("registered"),
        ACTIVE("active"),
        INACTIVE("inactive"),
        REJECTED("rejected"),
        DELETED("deleted");

        @Getter
        private String name;

        private UserStatus(String name) {
            this.name = name;
        }

        public static UserStatus getStatusByName(String name) {
            for (UserStatus status : UserStatus.values()) {
                if (status.getName().equals(name)) {
                    return status;
                }
            }
            return ACTIVE; // default status
        }
    }

    @Override
    public Path getDownloadFolder() {
        return Paths.get(ConfigurationHelper.getInstance().getGoobiFolder(), "uploads", "user", login);
    }

    @Override
    public String getUploadFolder() {
        return getDownloadFolder().toString();
    }

    @Override
    public void setUploadFolder(String uploadFolder) {
        // do nothing
    }

    @Override
    public EntryType getEntryType() {
        return EntryType.USER;
    }

    public List<StringPair> getAllAdditionalSearchFilter() {
        List<StringPair> answer = new ArrayList<>();
        if (StringUtils.isNotBlank(additionalSearchFields)) {
            // for each line
            String[] lines = additionalSearchFields.split("[\r\n]");
            for (String line : lines) {
                if (line.contains("=")) {
                    // split on first equals character
                    String label = line.substring(0, line.indexOf("="));
                    String value = line.substring(line.indexOf("=") + 1);
                    answer.add(new StringPair(label, value));
                }
            }
        }
        return answer;
    }

    public void createNewToken() {
        token = new AuthenticationToken(UUID.randomUUID().toString(), id);
        apiToken.add(token);
    }

    public void deleteToken() {
        apiToken.remove(token);
        UserManager.deleteApiToken(token);
        token = null;
    }

}
