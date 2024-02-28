package de.sub.goobi.persistence.managers;

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
 */
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.api.rest.AuthenticationMethodDescription;
import org.goobi.api.rest.AuthenticationToken;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UserManager implements IManager, Serializable {

    private static final long serialVersionUID = -5265381235116154785L;

    public static User getUserById(int id) throws DAOException {
        User o = null;
        try {
            o = UserMysqlHelper.getUserById(id);
        } catch (SQLException e) {
            log.error("error while getting User with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static User getUserBySsoId(String id) {
        User o = null;
        try {
            o = UserMysqlHelper.getUserBySsoId(id);
        } catch (SQLException e) {
            log.error("error while getting User with id " + id, e);
        }
        return o;
    }

    public static User saveUser(User o) throws DAOException {
        try {
            return UserMysqlHelper.saveUser(o);
        } catch (SQLException e) {
            log.error("error while saving User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteUser(User o) throws DAOException {
        try {
            UserMysqlHelper.deleteUser(o);
        } catch (SQLException e) {
            log.error("error while deleting User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void hideUser(User o) throws DAOException {
        try {

            JournalManager.deleteAllJournalEntries(o.getId(), EntryType.USER);

            StepMysqlHelper.removeUserFromAllSteps(o);
            UserMysqlHelper.deleteAllUsergroupAssignments(o);
            UserMysqlHelper.deleteAllProjectAssignments(o);
            UserMysqlHelper.hideUser(o);
        } catch (SQLException e) {
            log.error("error while deleting User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<User> getUsers(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getUsers(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting Users", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<User> getUsersForUsergroup(Usergroup usergroup) throws DAOException {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getUsersForUsergroup(usergroup);
        } catch (SQLException e) {
            log.error("error while getting Users for user group", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getUsers(order, filter, start, count, institution);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = UserMysqlHelper.getUserCount(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting User hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    public static void addFilter(int userId, String filterstring) {
        if (getFilters(userId).contains(filterstring)) {
            return;
        }
        try {
            UserMysqlHelper.addFilterToUser(userId, filterstring);
        } catch (SQLException e) {
            log.error("Cannot add filter to user with id " + userId, e);
        }

    }

    public static void removeFilter(int userId, String filterstring) {
        if (!getFilters(userId).contains(filterstring)) {
            return;
        }
        try {
            UserMysqlHelper.removeFilterFromUser(userId, filterstring);
        } catch (SQLException e) {
            log.error("Cannot remove filter from user with id " + userId, e);
        }
    }

    public static List<String> getFilters(int userId) {
        List<String> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getFilterForUser(userId);
        } catch (SQLException e) {
            log.error("Cannot load filter for user with id " + userId, e);
        }

        return answer;
    }

    public static List<User> getUserForStep(int stepId) {
        List<User> userList = new ArrayList<>();
        try {
            userList = UserMysqlHelper.getUserForStep(stepId);
        } catch (SQLException e) {
            log.error("Cannot load user for step with id " + stepId, e);
        }
        return userList;
    }

    /* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

    public static User convert(ResultSet rs) throws SQLException {
        User r = new User();
        r.setId(rs.getInt("BenutzerID"));
        r.setVorname(rs.getString("Vorname"));
        r.setNachname(rs.getString("Nachname"));
        r.setLogin(rs.getString("login"));
        r.setPasswort(rs.getString("passwort"));
        r.setEncryptedPassword(rs.getString("encryptedPassword"));
        r.setPasswordSalt(rs.getString("salt"));
        r.setStandort(rs.getString("Standort"));
        r.setMetadatenSprache(rs.getString("metadatensprache"));
        r.setCss(rs.getString("css"));
        r.setMitMassendownload(rs.getBoolean("mitMassendownload"));
        r.setTabellengroesse(rs.getInt("Tabellengroesse"));
        r.setSessiontimeout(rs.getInt("sessiontimeout"));
        r.setDisplayAutomaticTasks(rs.getBoolean("displayAutomaticTasks"));
        r.setDisplayBatchColumn(rs.getBoolean("displayBatchColumn"));
        r.setDisplayDeactivatedProjects(rs.getBoolean("displayDeactivatedProjects"));
        r.setDisplayFinishedProcesses(rs.getBoolean("displayFinishedProcesses"));
        r.setDisplayIdColumn(rs.getBoolean("displayIdColumn"));
        r.setDisplayLocksColumn(rs.getBoolean("displayLocksColumn"));
        r.setDisplayModulesColumn(rs.getBoolean("displayModulesColumn"));
        r.setDisplayOnlyOpenTasks(rs.getBoolean("displayOnlyOpenTasks"));
        r.setDisplayOnlySelectedTasks(rs.getBoolean("displayOnlySelectedTasks"));
        r.setDisplayProcessDateColumn(rs.getBoolean("displayProcessDateColumn"));
        r.setDisplayRulesetColumn(rs.getBoolean("displayRulesetColumn"));
        r.setDisplaySelectBoxes(rs.getBoolean("displaySelectBoxes"));
        r.setDisplaySwappingColumn(rs.getBoolean("displaySwappingColumn"));
        r.setDisplayNumberOfImages(rs.getBoolean("displayNumberOfImagesColumn"));
        r.setHideCorrectionTasks(rs.getBoolean("hideCorrectionTasks"));
        r.setEmail(rs.getString("email"));
        r.setShortcutPrefix(rs.getString("shortcut"));
        r.setMetsEditorTime(rs.getInt("metseditortime"));
        r.setDisplayOtherTasks(rs.getBoolean("displayOtherTasks"));
        r.setMetsDisplayHierarchy(rs.getBoolean("metsDisplayHierarchy"));
        r.setMetsDisplayPageAssignments(rs.getBoolean("metsDisplayPageAssignments"));
        r.setMetsDisplayTitle(rs.getBoolean("metsDisplayTitle"));
        r.setMetsLinkImage(rs.getBoolean("metsLinkImage"));
        r.setMetsDisplayProcessID(rs.getBoolean("metsDisplayProcessID"));
        r.setDisplayGridView(rs.getBoolean("displayGridView"));
        r.setDisplayMetadataColumn(rs.getBoolean("displayMetadataColumn"));
        r.setDisplayThumbColumn(rs.getBoolean("displayThumbColumn"));
        r.setCustomColumns(rs.getString("customColumns"));
        r.setCustomCss(rs.getString("customCss"));
        r.setMailNotificationLanguage(rs.getString("mailNotificationLanguage"));
        r.setProcessListDefaultSortField(rs.getString("processses_sort_field"));
        r.setProcessListDefaultSortOrder(rs.getString("processes_sort_order"));
        r.setTaskListDefaultSortingField(rs.getString("tasks_sort_field"));
        r.setTaskListDefaultSortOrder(rs.getString("tasks_sort_order"));
        r.setDisplayLastEditionDate(rs.getBoolean("displayLastEditionDate"));
        r.setDisplayLastEditionUser(rs.getBoolean("displayLastEditionUser"));
        r.setDisplayLastEditionTask(rs.getBoolean("displayLastEditionTask"));
        try {
            r.setLdapGruppe(LdapManager.getLdapById(rs.getInt("ldapgruppenID")));
            if (rs.wasNull()) {
                r.setLdapGruppe(null);
            }
        } catch (DAOException e) {
            throw new SQLException(e);
        }
        r.setLdaplogin(rs.getString("ldaplogin"));
        r.setInstitutionId(rs.getInt("institution_id"));
        r.setSuperAdmin(rs.getBoolean("superadmin"));
        r.setDisplayInstitutionColumn(rs.getBoolean("displayInstitutionColumn"));
        r.setDashboardPlugin(rs.getString("dashboardPlugin"));
        r.setSsoId(rs.getString("ssoId"));
        r.setDashboardConfiguration(rs.getString("dashboard_configuration"));
        r.setUiMode(rs.getString("ui_mode"));
        r.setStatus(User.UserStatus.getStatusByName(rs.getString("userstatus")));
        r.setAdditionalData(MySQLHelper.convertStringToMap(rs.getString("additional_data")));
        r.setJournal(JournalManager.getLogEntriesForUser(r.getId()));
        r.setAdditionalSearchFields(rs.getString("additional_search_fields"));
        List<AuthenticationToken> apiToken = getAuthenticationTokenForUser(r);
        r.setApiToken(apiToken);
        return r;
    }

    public static final ResultSetHandler<User> resultSetToUserHandler = new ResultSetHandler<User>() {
        @Override
        public User handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null
                    return convert(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<User>> resultSetToUserListHandler = new ResultSetHandler<List<User>>() {
        @Override
        public List<User> handle(ResultSet rs) throws SQLException {
            List<User> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    User o = convert(rs); // implies that o != null
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static AuthenticationToken convertToken(ResultSet rs) throws SQLException {
        if (rs != null) {
            AuthenticationToken token = new AuthenticationToken();
            token.setTokenHash(rs.getString("token_name"));
            token.setUserId(rs.getInt("user_id"));
            token.setTokenId(rs.getInt("id"));
            token.setDescription(rs.getString("token_description"));
            List<AuthenticationMethodDescription> configuredMethods = getConfiguredMethods(token.getTokenId());
            token.setMethods(configuredMethods);
            return token;
        }
        return null;
    }

    public static final ResultSetHandler<AuthenticationToken> resultSetToAuthenticationTokenHandler = new ResultSetHandler<AuthenticationToken>() {
        @Override
        public AuthenticationToken handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null
                    return convertToken(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<AuthenticationToken>> resultSetToAuthenticationTokenListHandler =
            new ResultSetHandler<List<AuthenticationToken>>() {
        @Override
        public List<AuthenticationToken> handle(ResultSet rs) throws SQLException {
            List<AuthenticationToken> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    answer.add(convertToken(rs));
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static final ResultSetHandler<List<AuthenticationMethodDescription>> resultSetToAuthenticationTokenMethodListHandler =
            new ResultSetHandler<List<AuthenticationMethodDescription>>() {
        @Override
        public List<AuthenticationMethodDescription> handle(ResultSet rs) throws SQLException {
            List<AuthenticationMethodDescription> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    AuthenticationMethodDescription o = new AuthenticationMethodDescription(rs.getString("method_type"),
                            rs.getString("method_description"), rs.getString("method_url"));
                    o.setMethodID(rs.getInt("id"));
                    o.setApiTokenId(rs.getInt("token_id"));
                    o.setSelected(rs.getBoolean("selected"));
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static void addUsergroupAssignment(User user, Integer gruppenID) {
        try {
            UserMysqlHelper.addUsergroupAssignment(user, gruppenID);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void addProjectAssignment(User user, Integer projektID) {
        try {
            UserMysqlHelper.addProjectAssignment(user, projektID);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static void deleteUsergroupAssignment(User user, int gruppenID) {
        try {
            UserMysqlHelper.deleteUsergroupAssignment(user, gruppenID);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteProjectAssignment(User user, int projektID) {
        try {
            UserMysqlHelper.deleteProjectAssignment(user, projektID);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    public static User getUserByLogin(String loginName) {
        User o = null;
        try {
            o = UserMysqlHelper.getUserByLogin(loginName);
        } catch (SQLException e) {
            log.error("error while getting User with login name " + loginName, e);
        }
        return o;
    }

    public static List<User> getAllUsers() {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getAllUsers();
        } catch (SQLException e) {
            log.error("error while getting all Users", e);
        }
        return answer;
    }

    public static List<UserProjectConfiguration> getEmailConfigurationForUser(List<Project> projects, Integer id, boolean showAllItems) {
        List<UserProjectConfiguration> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getEmailConfigurationForUser(projects, id, showAllItems);
        } catch (SQLException e) {
            log.error("error while getting email configuration for user", e);
        }

        return answer;
    }

    /**
     * Find the users who should be informed by mail when the status of a step changes.
     * 
     * @param stepName
     * @param projectId
     * @param stepStatus
     * @return
     */

    public static List<User> getUsersToInformByMail(String stepName, Integer projectId, String stepStatus) {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getUsersToInformByMail(stepName, projectId, stepStatus);
        } catch (SQLException e) {
            log.error("error while getting Users for email", e);
        }
        return answer;
    }

    /**
     * Remove email configuration for a project
     * 
     * @param myClass
     * @param projektID
     */

    public static void deleteEmailAssignmentForProject(User user, int projektID) {
        try {
            UserMysqlHelper.deleteEmailAssignmentForProject(user, projektID);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    /**
     * Remove email configuration for a single step within a project
     * 
     * @param myClass
     * @param projektID
     */

    public static void deleteEmailAssignmentForStep(User user, int projectID, String stepName) {
        try {
            UserMysqlHelper.deleteEmailAssignmentForStep(user, projectID, stepName);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static AuthenticationToken getAuthenticationToken(String tokenName) {
        try {
            return UserMysqlHelper.getAuthenticationToken(tokenName);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static List<AuthenticationToken> getAuthenticationTokenForUser(User user) {
        List<AuthenticationToken> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getAuthenticationTokenForUser(user.getId());
        } catch (SQLException e) {
            log.error(e);
        }
        return answer;
    }

    public static List<AuthenticationMethodDescription> getConfiguredMethods(Integer tokenID) {
        List<AuthenticationMethodDescription> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getConfiguredMethods(tokenID);
        } catch (SQLException e) {
            log.error(e);
        }
        return answer;
    }

    public static void deleteApiToken(AuthenticationToken token) {
        try {
            UserMysqlHelper.deleteAuthenticationToken(token);
        } catch (SQLException e) {
            log.error(e);
        }
    }

}
