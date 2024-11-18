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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.Institution;
import org.goobi.beans.Ldap;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.UserRole;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.LdapManager;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Named("BenutzerverwaltungForm")
@WindowScoped
@Log4j2
public class UserBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -3635859455444639614L;

    private static final String RETURN_PAGE_ALL = "user_all";
    private static final String RETURN_PAGE_EDIT = "user_edit";

    private static final String ID = "ID";
    private static final String TITLE = "titel";

    @Getter
    private User myClass = new User();
    @Getter
    @Setter
    private boolean hideInactiveUsers = true;
    @Getter
    @Setter
    private String displayMode = "";
    @Getter
    private DatabasePaginator usergroupPaginator;
    @Getter
    private DatabasePaginator projectPaginator;
    @Getter
    private boolean unsubscribedProjectsExist;

    @Getter
    private boolean unsubscribedGroupsExist;

    //changes since last save for each user:
    private Map<Integer, ArrayList<Integer>> addedToGroups = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> removedFromGroups = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> addedToProjects = new HashMap<>();
    private Map<Integer, ArrayList<Integer>> removedFromProjects = new HashMap<>();

    public String Neu() {
        this.myClass = new User();
        this.myClass.setVorname("");
        this.myClass.setNachname("");
        this.myClass.setLogin("");
        this.myClass.setLdaplogin("");
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        this.myClass.setPasswordSalt(salt.toString());
        updateUsergroupPaginator();
        updateProjectPaginator();

        resetChangeLists();

        return RETURN_PAGE_EDIT;
    }

    //set up the change recording lists
    private void resetChangeLists() {
        addedToGroups.put(myClass.getId(), new ArrayList<>());
        addedToProjects.put(myClass.getId(), new ArrayList<>());
        removedFromGroups.put(myClass.getId(), new ArrayList<>());
        removedFromProjects.put(myClass.getId(), new ArrayList<>());
    }

    private String getBasicFilter() {
        String hide;
        if (this.hideInactiveUsers) {
            hide = "userstatus='active'";
        } else {
            hide = "userstatus!='deleted'";
        }
        return hide;
    }

    public String FilterKein() {
        displayMode = "";
        this.filter = null;
        this.sortField = "nachname, vorname";
        UserManager m = new UserManager();
        paginator = new DatabasePaginator(sortField, getBasicFilter(), m, RETURN_PAGE_ALL);
        return RETURN_PAGE_ALL;
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public String FilterAlleStart() {
        UserManager m = new UserManager();
        String sqlQuery = getBasicFilter();
        StringBuilder sqlQueryBuilder = new StringBuilder(sqlQuery);
        if (this.filter != null && this.filter.length() != 0) {
            String[] searchParts = this.filter.trim().split("\\s+");
            sqlQueryBuilder.append(" AND (");
            for (int index = 0; index < searchParts.length; index++) {
                String like = MySQLHelper.escapeString(searchParts[index]);
                like = "\'%" + MySQLHelper.escapeSql(like) + "%\'";
                String inGroup =
                        "BenutzerID IN (SELECT DISTINCT BenutzerID FROM benutzergruppenmitgliedschaft, benutzergruppen WHERE benutzergruppenmitgliedschaft.BenutzerGruppenID = benutzergruppen.BenutzergruppenID AND benutzergruppen.titel LIKE "
                                + like + ")";
                String inProject =
                        "BenutzerID IN (SELECT DISTINCT BenutzerID FROM projektbenutzer, projekte WHERE projektbenutzer.ProjekteID = projekte.ProjekteID AND projekte.titel LIKE "
                                + like + ")";
                String inInstitution =
                        "BenutzerID IN (SELECT DISTINCT BenutzerID FROM benutzer, institution WHERE benutzer.institution_id = institution.id AND (institution.shortName LIKE "
                                + like + " OR institution.longName LIKE " + like + "))";
                String inName = "Vorname LIKE " + like + " OR Nachname LIKE " + like + " OR login LIKE " + like + " OR Standort LIKE " + like;
                sqlQueryBuilder.append(inName).append(" OR ").append(inGroup).append(" OR ").append(inProject).append(" OR ").append(inInstitution);
                if (index < searchParts.length - 1) {
                    sqlQueryBuilder.append(" OR ");
                }
            }
            sqlQueryBuilder.append(")");
            sqlQuery = sqlQueryBuilder.toString();
        }

        this.paginator = new DatabasePaginator(sortField, sqlQuery, m, RETURN_PAGE_ALL);

        return RETURN_PAGE_ALL;
    }

    public String Speichern() {

        String userName = myClass.getLogin();
        if (!isLoginNameValid(userName)) {
            return "";
        }

        try {

            // If the user is created now, the id will be generated later in the database and is null now.
            // Otherwise the user object has already a valid id.
            Integer userId = this.myClass.getId();
            boolean createNewUser = userId == null;

            if (createNewUser && existsUserName(userName)) {
                // new user should be created, but user name is already in use
                Helper.setFehlerMeldung("", "Error: User name is already in use: " + userName);
                return "";
            } else if (!createNewUser && !existsUserId(userId)) {
                // existing user should be edited, but does not exist
                Helper.setFehlerMeldung("", "Error: User can not be found in the database.");
                return "";
            }

            if (createNewUser && !isPasswordValid()) {
                // The 'FehlerMeldung' is already set in isPasswordValid() if the password is too short.
                return "";
            }

            updateInstitutionId();

            return saveUserAndLoadPaginator(false);

        } catch (DAOException daoException) {
            Helper.setFehlerMeldung("", "Error: Could not save user " + userName + "; exception: ", daoException.getMessage());
            return "";
        }
    }

    private boolean isPasswordValid() {
        switch (myClass.getLdapGruppe().getAuthenticationTypeEnum()) {
            case OPENID:
                break;
            case LDAP:
                if (myClass.getLdapGruppe().isReadonly()) {
                    break;
                }
                // fallthrough
            case DATABASE:
            default:
                // The new password must fulfill the minimum password length (read from default configuration file)
                int minimumLength = ConfigurationHelper.getInstance().getMinimumPasswordLength();

                if (myClass.getPasswort() == null || myClass.getPasswort().length() < minimumLength) {
                    this.displayMode = "";
                    Helper.setFehlerMeldung("neuesPasswortNichtLangGenug", "" + minimumLength);
                    return false;
                }
                myClass.setEncryptedPassword(myClass.getPasswordHash(myClass.getPasswort()));
        }
        return true;

    }

    private void updateInstitutionId() throws DAOException {
        // if there is only one institution, then it is not shown in ui and the value may be null:
        if (myClass.getInstitutionId() == null) {
            List<SelectItem> institutionList = this.getInstitutionsAsSelectList();
            if (!institutionList.isEmpty()) {
                Integer institution = (Integer) institutionList.get(0).getValue();
                myClass.setInstitutionId(institution);
            }
        }
    }

    private String saveUserAndLoadPaginator(boolean creatingUser) throws DAOException {

        UserManager.saveUser(this.myClass);
        paginator.load();

        this.resetChangeLists();

        if ("".equals(this.displayMode) && creatingUser) {
            this.displayMode = "tab2";
            return RETURN_PAGE_EDIT;
        } else {
            this.displayMode = "";
            return RETURN_PAGE_ALL;
        }
    }

    private boolean existsUserName(String userName) throws DAOException {
        String query = "login='" + userName + "'";
        int numberOfUsers = new UserManager().getHitSize(null, query, null);
        return numberOfUsers > 0;
    }

    private boolean existsUserId(Integer userId) throws DAOException {
        String query = "BenutzerID='" + userId + "'";
        int numberOfUsers = new UserManager().getHitSize(null, query, null);
        return numberOfUsers > 0;
    }

    private boolean isLoginNameValid(String inLogin) {
        boolean valide = true;
        String patternStr = "[A-Za-z0-9@_\\-.]*";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inLogin);
        valide = matcher.matches();
        if (!valide) {
            Helper.setFehlerMeldung("", Helper.getTranslation("loginNotValid"));
        }

        /* Pfad zur Datei ermitteln */
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        String filename = session.getServletContext().getRealPath("/WEB-INF") + FileSystems.getDefault().getSeparator() + "classes"
                + FileSystems.getDefault().getSeparator() + "goobi_loginBlacklist.txt";
        /* Datei zeilenweise durchlaufen und die auf ungültige Zeichen vergleichen */
        try {
            try (FileInputStream fis = new FileInputStream(filename); InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader in = new BufferedReader(isr);) {
                String str;
                while ((str = in.readLine()) != null) {
                    if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
                        valide = false;
                        Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
                    }
                }
            }
        } catch (IOException exception) {
            log.trace(exception);
        }
        return valide;
    }

    /**
     * The function Loeschen() deletes a user account. Please note that deleting a user in goobi will not delete the user from a connected LDAP
     * service.
     * 
     * @return a string indicating the screen showing up after the command has been performed.
     */
    public String Loeschen() {
        User currentUser = Helper.getCurrentUser();
        if (!currentUser.getId().equals(myClass.getId())) {
            try {
                Path folder = Paths.get(ConfigurationHelper.getInstance().getGoobiFolder(), "uploads", "user", myClass.getLogin());
                if (StorageProvider.getInstance().isFileExists(folder)) {
                    StorageProvider.getInstance().deleteDir(folder);
                }

                UserManager.hideUser(myClass);
                if (myClass.getLdapGruppe().getAuthenticationTypeEnum() == AuthenticationType.LDAP && !myClass.getLdapGruppe().isReadonly()) {
                    new LdapAuthentication().deleteUser(myClass);
                }
                paginator.load();
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Error_hideUser", e.getMessage());
                return "";
            }
            return FilterKein();
        }
        Helper.setFehlerMeldung("Error_selfDelete");
        return "";
    }

    public String AusGruppeLoeschen() {
        int gruppenID = Integer.parseInt(Helper.getRequestParameter(ID));
        String strResult = AusGruppeLoeschen(gruppenID);

        if (strResult != null) {
            removedFromGroups.get(myClass.getId()).add(gruppenID);
        }
        return strResult;
    }

    private String AusGruppeLoeschen(int gruppenID) {

        List<Usergroup> neu = new ArrayList<>();
        for (Usergroup u : this.myClass.getBenutzergruppen()) {
            if (u.getId().intValue() != gruppenID) {
                neu.add(u);
            }
        }
        List<UserProjectConfiguration> oldMailConfiguration = null;
        if (!myClass.getAllUserRoles().contains("Admin_All_Mail_Notifications")) {
            oldMailConfiguration = UserManager.getEmailConfigurationForUser(myClass.getProjekte(), myClass.getId(), false);
        }

        this.myClass.setBenutzergruppen(neu);
        UserManager.deleteUsergroupAssignment(myClass, gruppenID);
        if (oldMailConfiguration != null && !oldMailConfiguration.isEmpty()) {
            // check if mail configuration must be disabled for some tasks
            List<UserProjectConfiguration> newMailConfigurationWithoutGroup =
                    UserManager.getEmailConfigurationForUser(myClass.getProjekte(), myClass.getId(), false);

            for (UserProjectConfiguration oldProject : oldMailConfiguration) {
                for (UserProjectConfiguration newProject : newMailConfigurationWithoutGroup) {
                    if (oldProject.getProjectId().intValue() == newProject.getProjectId().intValue()) {
                        for (StepConfiguration oldStep : oldProject.getStepList()) {
                            boolean matched = false;
                            for (StepConfiguration newStep : newProject.getStepList()) {
                                if (oldStep.getStepName().equals(newStep.getStepName())) {
                                    matched = true;
                                    break;
                                }
                            }
                            if (!matched) {
                                // step belonged to the removed group
                                UserManager.deleteEmailAssignmentForStep(myClass, oldProject.getProjectId(), oldStep.getStepName());
                            }
                        }
                    }
                }
            }
        }

        updateUsergroupPaginator();
        return "";
    }

    public String ZuGruppeHinzufuegen() {
        Integer gruppenID = Integer.valueOf(Helper.getRequestParameter(ID));
        String strResult = ZuGruppeHinzufuegen(gruppenID);

        if (strResult != null) {
            addedToGroups.get(myClass.getId()).add(gruppenID);
        }
        return strResult;
    }

    private String ZuGruppeHinzufuegen(int gruppenID) {
        try {
            Usergroup usergroup = UsergroupManager.getUsergroupById(gruppenID);
            for (Usergroup b : this.myClass.getBenutzergruppen()) {
                if (b.equals(usergroup)) {
                    return "";
                }
            }
            this.myClass.getBenutzergruppen().add(usergroup);
            UserManager.addUsergroupAssignment(myClass, gruppenID);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error on reading database", e.getMessage());
            return null;
        }
        updateUsergroupPaginator();
        return "";
    }

    public String AusProjektLoeschen() {
        int projektID = Integer.parseInt(Helper.getRequestParameter(ID));
        String strResult = AusProjektLoeschen(projektID); // strResult == ""

        removedFromProjects.get(myClass.getId()).add(projektID);

        return strResult;
    }

    private String AusProjektLoeschen(int projektID) {
        List<Project> neu = new ArrayList<>();
        for (Project p : this.myClass.getProjekte()) {
            if (p.getId().intValue() != projektID) {
                neu.add(p);
            }
        }
        this.myClass.setProjekte(neu);
        if (!myClass.getAllUserRoles().contains("Admin_All_Mail_Notifications")) {
            UserManager.deleteEmailAssignmentForProject(myClass, projektID);
        }
        UserManager.deleteProjectAssignment(myClass, projektID);
        updateProjectPaginator();
        return "";
    }

    public String ZuProjektHinzufuegen() {
        Integer projektID = Integer.valueOf(Helper.getRequestParameter(ID));

        String strResult = ZuProjektHinzufuegen(projektID);

        if (strResult != null) {
            addedToProjects.get(myClass.getId()).add(projektID);
        }
        return strResult;
    }

    private String ZuProjektHinzufuegen(int projektID) {
        try {
            Project project = ProjectManager.getProjectById(projektID);
            for (Project p : this.myClass.getProjekte()) {
                if (p.equals(project)) {
                    return "";
                }
            }
            this.myClass.getProjekte().add(project);
            UserManager.addProjectAssignment(myClass, projektID);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error on reading database", e.getMessage());
            return null;
        }
        updateProjectPaginator();
        return "";
    }

    public void setMyClass(User inMyClass) {
        this.myClass = inMyClass;

        updateUsergroupPaginator();
        updateProjectPaginator();

        if (!addedToGroups.containsKey(myClass.getId())) {
            resetChangeLists();
        }
    }

    public Integer getLdapGruppeAuswahl() {
        if (this.myClass.getLdapGruppe() != null) {
            return this.myClass.getLdapGruppe().getId();
        } else {
            return null;
        }
    }

    public void setLdapGruppeAuswahl(Integer inAuswahl) {
        if (inAuswahl.intValue() != 0) {
            try {
                this.myClass.setLdapGruppe(LdapManager.getLdapById(inAuswahl));
            } catch (DAOException e) {
                Helper.setFehlerMeldung("Error on writing to database", e);
            }
        }
    }

    public void validateAuthenticationSelection(FacesContext context, UIComponent component, Object value) {
        boolean valid = true;
        if (value == null) {
            valid = false;
        }
        if (valid) {
            Integer intValue = (Integer) value;
            if (intValue.intValue() == 0) {
                valid = false;
            }
        }
        if (!valid) {
            String message = Helper.getTranslation("javax.faces.component.UIInput.REQUIRED");
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid value", message);
            throw new ValidatorException(facesMessage);
        }
    }

    public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
        List<SelectItem> myLdapGruppen = new ArrayList<>();
        myLdapGruppen.add(new SelectItem(0, Helper.getTranslation("bitteAuswaehlen")));
        List<Ldap> temp = LdapManager.getLdaps(TITLE, null, null, null,
                Helper.getCurrentUser().isSuperAdmin() ? null : Helper.getCurrentUser().getInstitution());
        for (Ldap gru : temp) {
            myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitel(), null));
        }
        return myLdapGruppen;
    }

    public String createUser() {
        // TODO
        // If result is empty -> error. Otherwise -> success
        String resultFromSaving = Speichern();
        boolean savedSuccessfully = !"".equals(resultFromSaving);

        if (savedSuccessfully && AuthenticationType.LDAP.equals(myClass.getLdapGruppe().getAuthenticationTypeEnum())) {
            LdapKonfigurationSchreiben();
        }

        // Tab was set in Speichern()
        return resultFromSaving;
    }

    public String LdapKonfigurationSchreiben() {
        LdapAuthentication myLdap = new LdapAuthentication();
        try {
            myLdap.createNewUser(this.myClass, this.myClass.getPasswort());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("Could not generate ldap entry: " + e.getMessage());
            Helper.setFehlerMeldung("Error on writing to database", e);
        }
        return "";
    }

    public String getCreateNewRandomPasswordForUser() {
        return createNewRandomPasswordForUser();
    }

    /**
     * This method generates a new (random) password for a certain user. It can be called by a button in the user list (visible only for
     * administrators). The administrator will be asked a last time before this method generates and resets the password. If the administrator
     * confirms, the password and salt are generated and reset. At last the new password is shown in the success message bar.
     * 
     * @return The user list page
     */
    public String createNewRandomPasswordForUser() {
        // This boolean flag is set to true if the user password can be reset. If at least one of the criteria is not fulfilled, it is set to false
        // and the password is not reset
        boolean passwordChangeable = true;

        // Check for administrator rules
        boolean userIsAdmin = Helper.getCurrentUser().getAllUserRoles().contains(UserRole.Admin_Users_Change_Passwords.toString());
        if (!userIsAdmin) {
            Helper.setFehlerMeldung("You are not allowed to change the user's password!");
            passwordChangeable = false;
        }

        // Get and create user
        User userToResetPassword = null;
        if (passwordChangeable) {
            try {
                Integer loginID = Integer.valueOf(Helper.getRequestParameter(ID));
                userToResetPassword = UserManager.getUserById(loginID);
            } catch (DAOException daoe) {
                Helper.setFehlerMeldung("Could not read database", daoe.getMessage());
                passwordChangeable = false;
            }
        }

        if (userToResetPassword == null) {
            Helper.setFehlerMeldung("The selected user account is invalid in the database.");
            passwordChangeable = false;
        }

        // Create the random password and save it
        if (passwordChangeable) {
            try {
                // The custom minimum password is >= 1. The random password should have a length of >= 11.
                int length = ConfigurationHelper.getInstance().getMinimumPasswordLength() + 10;
                String password = createRandomPassword(length);

                AuthenticationType authentication = userToResetPassword.getLdapGruppe().getAuthenticationTypeEnum();
                if (AuthenticationType.LDAP.equals(authentication) && !userToResetPassword.getLdapGruppe().isReadonly()) {

                    LdapAuthentication myLdap = new LdapAuthentication();
                    myLdap.changeUserPassword(userToResetPassword, null, password);
                }
                saltAndSaveUserPassword(userToResetPassword, password);
                // Show password in message box
                Helper.setMeldung("Password of user \"" + userToResetPassword.getNachVorname() + "\" was set to: " + password);
            } catch (NoSuchAlgorithmException e) {
                Helper.setFehlerMeldung("LDAP error", e.getMessage());
            }
        }
        return RETURN_PAGE_ALL;
    }

    /**
     * This method is to set the user's password with a random salt value.
     * 
     * @param user The user with the new password
     * @param password The new unencrypted password of user
     */
    public static void saltAndSaveUserPassword(User user, String password) {

        // Create a salt value
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
        user.setPasswordSalt(salt.toString());

        // Create and set new password
        String encryptedPassword = user.getPasswordHash(password);
        try {
            user.setEncryptedPassword(encryptedPassword);
            // Save salt and password
            UserManager.saveUser(user);
        } catch (DAOException e) {
            log.error(e);
            Helper.setFehlerMeldung("Couldn't set password of user \"" + user.getNachVorname() + "!");
        }
    }

    /**
     * This method generates a random String containing small letters. The length is determined by the parameter 'length'. Letters may occur more than
     * one time.<br />
     * Examples:<br />
     * createRandomPassword(10) -> "aherizbobr"<br />
     * createRandomPassword(4) -> "klww"<br />
     * 
     * @param length The length of required password
     * @return The generated password
     */
    public static String createRandomPassword(int length) {
        SecureRandom r = new SecureRandom();
        StringBuilder password = new StringBuilder();
        while (password.length() < length) {
            // ASCII interval: [97 + 0, 97 + 25] => [97, 122] => [a, z]
            password.append((char) (r.nextInt(26) + 'a'));
        }
        return password.toString();
    }

    public boolean getLdapUsage() {
        return ConfigurationHelper.getInstance().isUseLdap();
    }

    private void updateUsergroupPaginator() {
        String filter = "";
        if (myClass != null && myClass.getId() != null) {
            filter = " benutzergruppen.BenutzergruppenID not in (select benutzergruppenmitgliedschaft.BenutzerGruppenID from "
                    + "benutzergruppenmitgliedschaft where benutzergruppenmitgliedschaft.BenutzerID = " + myClass.getId() + ")";
        }
        UsergroupManager m = new UsergroupManager();
        usergroupPaginator = new DatabasePaginator(TITLE, filter, m, "");

        unsubscribedGroupsExist = usergroupPaginator.getTotalResults() > 0;
    }

    private void updateProjectPaginator() {
        String filter = "";
        if (myClass != null && myClass.getId() != null) {
            filter = " projekte.ProjekteID not in (select projektbenutzer.ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = "
                    + myClass.getId() + ")";
        }
        ProjectManager m = new ProjectManager();
        projectPaginator = new DatabasePaginator(TITLE, filter, m, "");

        unsubscribedProjectsExist = projectPaginator.getTotalResults() > 0;
    }

    public Integer getCurrentInstitutionID() {
        if (myClass.getInstitution() != null) {
            return myClass.getInstitution().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setCurrentInstitutionID(Integer id) {
        if (id != null && id.intValue() != 0) {
            Institution institution = InstitutionManager.getInstitutionById(id);
            myClass.setInstitution(institution);
        }
    }

    public Integer getNumberOfInstitutions() {
        return InstitutionManager.getNumberOfInstitutions();
    }

    public List<SelectItem> getInstitutionsAsSelectList() throws DAOException {
        List<SelectItem> institutions = new ArrayList<>();
        List<Institution> temp = null;
        if (Helper.getCurrentUser().isSuperAdmin()) {
            temp = InstitutionManager.getAllInstitutionsAsList();
        } else {
            temp = new ArrayList<>();
            temp.add(Helper.getCurrentUser().getInstitution());
        }
        if (temp != null && !temp.isEmpty()) {
            for (Institution proj : temp) {
                institutions.add(new SelectItem(proj.getId(), proj.getShortName(), null));
            }
        }
        return institutions;
    }

    /*
     * Undo the changes since last Save() was called
     */
    public String cancelEdit() {

        for (Integer iGroup : addedToGroups.get(myClass.getId())) {
            AusGruppeLoeschen(iGroup);
        }
        for (Integer iGroup : removedFromGroups.get(myClass.getId())) {
            ZuGruppeHinzufuegen(iGroup);
        }
        for (Integer iProj : addedToProjects.get(myClass.getId())) {
            AusProjektLoeschen(iProj);
        }
        for (Integer iProj : removedFromProjects.get(myClass.getId())) {
            ZuProjektHinzufuegen(iProj);
        }

        resetChangeLists();
        return RETURN_PAGE_ALL;
    }
}
