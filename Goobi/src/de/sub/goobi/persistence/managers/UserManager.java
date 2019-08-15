package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.helper.exceptions.DAOException;

public class UserManager implements IManager, Serializable {

    private static final long serialVersionUID = -5265381235116154785L;

    private static final Logger logger = Logger.getLogger(UserManager.class);

    public static User getUserById(int id) throws DAOException {
        User o = null;
        try {
            o = UserMysqlHelper.getUserById(id);
        } catch (SQLException e) {
            logger.error("error while getting User with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static User saveUser(User o) throws DAOException {
        try {
            return UserMysqlHelper.saveUser(o);
        } catch (SQLException e) {
            logger.error("error while saving User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteUser(User o) throws DAOException {
        try {
            UserMysqlHelper.deleteUser(o);
        } catch (SQLException e) {
            logger.error("error while deleting User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void hideUser(User o) throws DAOException {
        try {
            for (Usergroup ug : o.getBenutzergruppen()) {
                UserMysqlHelper.deleteUsergroupAssignment(o, ug.getId());
            }
            for (Project p : o.getProjekte()) {
                UserMysqlHelper.deleteProjectAssignment(o, p.getId());
            }
            UserMysqlHelper.hideUser(o);
        } catch (SQLException e) {
            logger.error("error while deleting User with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<User> getUsers(String order, String filter, Integer start, Integer count) throws DAOException {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getUsers(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting Users", e);
            throw new DAOException(e);
        }
        return answer;
    }

    public static List<User> getUsersForUsergroup(Usergroup usergroup) throws DAOException {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getUsersForUsergroup(usergroup);
        } catch (SQLException e) {
            logger.error("error while getting Users", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
        return getUsers(order, filter, start, count);
    }

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        int num = 0;
        try {
            num = UserMysqlHelper.getUserCount(order, filter);
        } catch (SQLException e) {
            logger.error("error while getting User hit size", e);
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
            logger.error("Cannot not add filter to user with id " + userId, e);
        }

    }

    public static void removeFilter(int userId, String filterstring) {
        if (!getFilters(userId).contains(filterstring)) {
            return;
        }
        try {
            UserMysqlHelper.removeFilterFromUser(userId, filterstring);
        } catch (SQLException e) {
            logger.error("Cannot not remove filter from user with id " + userId, e);
        }
    }

    public static List<String> getFilters(int userId) {
        List<String> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getFilterForUser(userId);
        } catch (SQLException e) {
            logger.error("Cannot not load filter for user with id " + userId, e);
        }

        return answer;
    }

    public static List<User> getUserForStep(int stepId) {
        List<User> userList = new ArrayList<>();
        try {
            userList = UserMysqlHelper.getUserForStep(stepId);
        } catch (SQLException e) {
            logger.error("Cannot not load user for step with id " + stepId, e);
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
        r.setIstAktiv(rs.getBoolean("IstAktiv"));
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
        r.setDisplaySelectBoxes(rs.getBoolean("displaySelectBoxes"));
        r.setDisplaySwappingColumn(rs.getBoolean("displaySwappingColumn"));
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
        try {
            r.setLdapGruppe(LdapManager.getLdapById(rs.getInt("ldapgruppenID")));
            if (rs.wasNull()) {
                r.setLdapGruppe(null);
            }
        } catch (DAOException e) {
            throw new SQLException(e);
        }
        r.setIsVisible(rs.getString("isVisible"));
        r.setLdaplogin(rs.getString("ldaplogin"));
        return r;
    }

    public static ResultSetHandler<User> resultSetToUserHandler = new ResultSetHandler<User>() {
        @Override
        public User handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return convert(rs);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static ResultSetHandler<List<User>> resultSetToUserListHandler = new ResultSetHandler<List<User>>() {
        @Override
        public List<User> handle(ResultSet rs) throws SQLException {
            List<User> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    User o = convert(rs);
                    if (o != null) {
                        answer.add(o);
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static void addUsergroupAssignment(User user, Integer gruppenID) {
        try {
            UserMysqlHelper.addUsergroupAssignment(user, gruppenID);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void addProjectAssignment(User user, Integer projektID) {
        try {
            UserMysqlHelper.addProjectAssignment(user, projektID);
        } catch (SQLException e) {
            logger.error(e);
        }

    }

    public static void deleteUsergroupAssignment(User user, int gruppenID) {
        try {
            UserMysqlHelper.deleteUsergroupAssignment(user, gruppenID);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void deleteProjectAssignment(User user, int projektID) {
        try {
            UserMysqlHelper.deleteProjectAssignment(user, projektID);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    @Override
    public List<Integer> getIdList(String order, String filter) {
        return null;
    }

    public static User getUserByLogin(String loginName) {
        User o = null;
        try {
            o = UserMysqlHelper.getUserByLogin(loginName);
        } catch (SQLException e) {
            logger.error("error while getting User with login name " + loginName, e);
        }
        return o;
    }

    public static List<User> getAllUsers() {
        List<User> answer = new ArrayList<>();
        try {
            answer = UserMysqlHelper.getAllUsers();
        } catch (SQLException e) {
            logger.error("error while getting Users", e);
        }
        return answer;
    }
}
