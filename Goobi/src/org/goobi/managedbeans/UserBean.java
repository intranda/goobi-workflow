package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.LdapManager;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@ManagedBean(name = "BenutzerverwaltungForm")
@SessionScoped
public class UserBean extends BasicBean {
    private static final long serialVersionUID = -3635859455444639614L;
    private User myClass = new User();
    private boolean hideInactiveUsers = true;
    private static final Logger logger = LogManager.getLogger(UserBean.class);
    private String displayMode = "";
    private DatabasePaginator usergroupPaginator;
    private DatabasePaginator projectPaginator;
    //    @Getter
    //    private DatabasePaginator institutionPaginator;

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
        return "user_edit";
    }

    private String getBasicFilter() {
        String hide = "isVisible is null";
        if (this.hideInactiveUsers) {
            hide += " AND istAktiv=true";
        }
        return hide;
    }

    public String FilterKein() {
        displayMode = "";
        this.filter = null;
        this.sortierung = "nachname, vorname";
        UserManager m = new UserManager();
        paginator = new DatabasePaginator(sortierung, getBasicFilter(), m, "user_all");
        return "user_all";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public String FilterAlleStart() {
        this.sortierung = "nachname, vorname";
        UserManager m = new UserManager();
        String myfilter = getBasicFilter();
        if (this.filter != null && this.filter.length() != 0) {
            filter = MySQLHelper.escapeString(filter);
            myfilter += " AND (concat (vorname, \" \", nachname) like '%" + StringEscapeUtils.escapeSql(this.filter)
                    + "%' OR BenutzerID IN (select distinct BenutzerID from benutzergruppenmitgliedschaft, benutzergruppen where benutzergruppenmitgliedschaft.BenutzerGruppenID = benutzergruppen.BenutzergruppenID AND benutzergruppen.titel like '%"
                    + StringEscapeUtils.escapeSql(this.filter)
                    + "%') OR BenutzerID IN (SELECT distinct BenutzerID FROM projektbenutzer, projekte WHERE projektbenutzer.ProjekteID = projekte.ProjekteID AND projekte.titel LIKE '%"
                    + StringEscapeUtils.escapeSql(this.filter) + "%'))";
        }
        paginator = new DatabasePaginator(sortierung, myfilter, m, "user_all");
        return "user_all";
    }

    public String Speichern() {
        String bla = this.myClass.getLogin();

        if (!LoginValide(bla)) {
            return "";
        }

        Integer blub = this.myClass.getId();
        try {
            /* prüfen, ob schon ein anderer Benutzer mit gleichem Login existiert */
            String query = "login='" + bla + "'AND BenutzerID !=" + blub;
            if (blub == null) {
                query = "login='" + bla + "'AND BenutzerID is not null";
            }
            int num = new UserManager().getHitSize(null, query, null);
            if (num == 0) {
                if (myClass.getId() == null) {
                    myClass.setEncryptedPassword(myClass.getPasswordHash(myClass.getPasswort()));
                    //                myClass.setPasswort("");
                }
                UserManager.saveUser(this.myClass);
                paginator.load();
                return FilterKein();
            } else {
                Helper.setFehlerMeldung("", Helper.getTranslation("loginBereitsVergeben"));
                return "";
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            return "";
        }
    }

    private boolean LoginValide(String inLogin) {
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
            FileInputStream fis = new FileInputStream(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF8");
            BufferedReader in = new BufferedReader(isr);
            String str;
            while ((str = in.readLine()) != null) {
                if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
                    valide = false;
                    Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
                }
            }
            in.close();
        } catch (IOException e) {
        }
        return valide;
    }

    /**
     * The function Loeschen() deletes a user account. Please note that deleting a user in goobi.production will not delete the user from a connected
     * LDAP service.
     * 
     * @return a string indicating the screen showing up after the command has been performed.
     */
    public String Loeschen() {
        try {
            UserManager.hideUser(myClass);
            if (myClass.getLdapGruppe().getAuthenticationTypeEnum() == AuthenticationType.LDAP && !myClass.getLdapGruppe().isReadonly()) {
                new LdapAuthentication().deleteUser(myClass);
            }
            paginator.load();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not hide user", e.getMessage());
            return "";
        }
        return FilterKein();
    }

    public String AusGruppeLoeschen() {
        int gruppenID = Integer.parseInt(Helper.getRequestParameter("ID"));
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
        Integer gruppenID = Integer.valueOf(Helper.getRequestParameter("ID"));
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
        int projektID = Integer.parseInt(Helper.getRequestParameter("ID"));
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
        Integer projektID = Integer.valueOf(Helper.getRequestParameter("ID"));
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

    public User getMyClass() {
        return this.myClass;
    }

    public void setMyClass(User inMyClass) {
        this.myClass = inMyClass;

        updateUsergroupPaginator();
        updateProjectPaginator();
        //        updateInstitutionPaginator();

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
        FacesMessage message = null;
        if (value == null) {
            message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid value", Helper.getTranslation("javax.faces.component.UIInput.REQUIRED"));
        } else {
            Integer intValue = (Integer) value;
            if (intValue.intValue() == 0) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid value",
                        Helper.getTranslation("javax.faces.component.UIInput.REQUIRED"));
            }
        }
        if (message != null) {
            throw new ValidatorException(message);
        }
    }

    public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
        List<SelectItem> myLdapGruppen = new ArrayList<>();
        myLdapGruppen.add(new SelectItem(0, Helper.getTranslation("bitteAuswaehlen")));
        List<Ldap> temp = LdapManager.getLdaps("titel", null, null, null,
                Helper.getCurrentUser().isSuperAdmin() ? null : Helper.getCurrentUser().getInstitution());
        for (Ldap gru : temp) {
            myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitel(), null));
        }
        return myLdapGruppen;
    }

    public String createUser() {
        if (!Speichern().equals("") && AuthenticationType.LDAP.equals(myClass.getLdapGruppe().getAuthenticationTypeEnum())) {
            LdapKonfigurationSchreiben();
        }
        displayMode = "tab2";
        return "";
    }

    public String LdapKonfigurationSchreiben() {
        LdapAuthentication myLdap = new LdapAuthentication();
        try {
            myLdap.createNewUser(this.myClass, this.myClass.getPasswort());
        } catch (Exception e) {
            logger.warn("Could not generate ldap entry: " + e.getMessage());
            Helper.setFehlerMeldung("Error on writing to database", e);
        }
        return "";
    }

    public String getCreateNewRandomPasswordForUser() {
        return createNewRandomPasswordForUser();
    }

    /**
     * This method generates a new (random) password for a certain user. It can be called by a button in the user list (visible only for
     * administrators). The administrator will be asked a last time before this method generates and resets the password. When he/she/it confirms, the
     * password and salt are generated and reset. At last the new password is shown on screen.
     * 
     * @return The next page
     */
    public String createNewRandomPasswordForUser() {
        // Check for administrator rules
        if (!Helper.getCurrentUser().getAllUserRoles().contains(UserRole.Admin_Users_Change_Passwords.toString())) {
            Helper.setFehlerMeldung("You are not allowed to change the user's password!");
            return "user_all";
        }

        // Get and create user
        Integer LoginID = Integer.valueOf(Helper.getRequestParameter("ID"));
        User userToResetPassword;
        try {
            userToResetPassword = UserManager.getUserById(LoginID);
        } catch (DAOException daoe) {
            Helper.setFehlerMeldung("could not read database", daoe.getMessage());
            return "user_all";
        }

        /*
        // Ask a last time before resetting password
        if ("REALLY" == "CANCEL") {
            return "index";
        }
        */

        // Create the random password and save it
        if (userToResetPassword != null) {
            String password = createRandomPassword(LoginBean.DEFAULT_PASSWORD_LENGTH);
            saltAndSaveUserPassword(userToResetPassword, password);
            // Show password on screen
            Helper.setMeldung("Password of user \"" + userToResetPassword.getNachVorname() + "\" was set to: " + password);
        }
        return "user_all";
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
        } catch (DAOException daoe) {
            daoe.printStackTrace();
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
        Random r = new Random();
        StringBuilder password = new StringBuilder();
        while (password.length() < length) {
            // ASCII interval: [97 + 0, 97 + 25] => [97, 122] => [a, z]
            password.append((char) (r.nextInt(26) + 'a'));
        }
        return password.toString();
    }

    public boolean isHideInactiveUsers() {
        return this.hideInactiveUsers;
    }

    public void setHideInactiveUsers(boolean hideInactiveUsers) {
        this.hideInactiveUsers = hideInactiveUsers;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }

    public boolean getLdapUsage() {
        return ConfigurationHelper.getInstance().isUseLdap();
    }

    public DatabasePaginator getUsergroupPaginator() {
        return usergroupPaginator;
    }

    public DatabasePaginator getProjectPaginator() {
        return projectPaginator;
    }

    private void updateUsergroupPaginator() {
        String filter = "";
        if (myClass != null && myClass.getId() != null) {
            filter = " benutzergruppen.BenutzergruppenID not in (select benutzergruppenmitgliedschaft.BenutzerGruppenID from "
                    + "benutzergruppenmitgliedschaft where benutzergruppenmitgliedschaft.BenutzerID = " + myClass.getId() + ")";
        }
        UsergroupManager m = new UsergroupManager();
        usergroupPaginator = new DatabasePaginator("titel", filter, m, "");
    }

    private void updateProjectPaginator() {
        String filter = "";
        if (myClass != null && myClass.getId() != null) {
            filter = " projekte.ProjekteID not in (select projektbenutzer.ProjekteID from projektbenutzer where projektbenutzer.BenutzerID = "
                    + myClass.getId() + ")";
        }
        ProjectManager m = new ProjectManager();
        projectPaginator = new DatabasePaginator("titel", filter, m, "");
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
}
