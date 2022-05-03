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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.goobi.api.mail.SendMail;
import org.goobi.api.mail.StepConfiguration;
import org.goobi.api.mail.UserProjectConfiguration;
import org.goobi.beans.Institution;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import lombok.extern.log4j.Log4j2;

@Log4j2
class UserMysqlHelper implements Serializable {
    private static final long serialVersionUID = 6133952688951728602L;

    public static List<User> getUsers(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer LEFT JOIN institution ON benutzer.institution_id = institution.id");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }

        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            List<User> ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getUserCount(String order, String filter, Institution institution) throws SQLException {
        boolean whereSet = false;
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(BenutzerID) FROM benutzer");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
            whereSet = true;
        }
        if (institution != null) {
            if (whereSet) {
                sql.append(" AND ");
            } else {
                sql.append(" WHERE ");
            }
            sql.append("institution_id = ");
            sql.append(institution.getId());
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static User getUserById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer WHERE BenutzerID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            User ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static User getUserBySsoId(String id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer WHERE ssoId = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            User ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserHandler, id);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static User saveUser(User ro) throws SQLException {
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            String visible = null;
            if (ro.getIsVisible() != null) {
                visible = "'" + ro.getIsVisible() + "'";
            }

            if (ro.getId() == null) {

                String propNames =
                        "Vorname, Nachname, login, IstAktiv, Standort, metadatensprache, css, mitMassendownload, Tabellengroesse, sessiontimeout, ldapgruppenID, isVisible, ldaplogin,"
                                + "displayAutomaticTasks, displayBatchColumn, displayDeactivatedProjects, displayFinishedProcesses, displayIdColumn, displayLocksColumn, "
                                + "displayModulesColumn, displayOnlyOpenTasks, displayOnlySelectedTasks, displayProcessDateColumn, displayRulesetColumn, displaySelectBoxes, displaySwappingColumn, hideCorrectionTasks, email, shortcut, metseditortime, "
                                + "metsDisplayHierarchy, metsDisplayPageAssignments, metsDisplayTitle, metsLinkImage, displayOtherTasks, encryptedPassword, salt, metsDisplayProcessID, displayGridView, displayMetadataColumn, "
                                + "displayThumbColumn, customColumns, customCss, mailNotificationLanguage, institution_id, superadmin, displayInstitutionColumn, dashboardPlugin, ssoId"
                                + ", processses_sort_field, processes_sort_order, tasks_sort_field, tasks_sort_order, displayLastEditionDate, displayLastEditionUser, displayLastEditionTask, dashboard_configuration, ui_mode ";
                String prop =
                        "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,  ?,?,?,?,?,?,?,?,?,? ";
                Object[] param = { ro.getVorname() == null ? null : ro.getVorname(), ro.getNachname() == null ? null : ro.getNachname(),
                        ro.getLogin() == null ? null : ro.getLogin(), ro.isIstAktiv(), ro.getStandort() == null ? null : ro.getStandort(),
                        ro.getMetadatenSprache() == null ? null : ro.getMetadatenSprache(), ro.getCss() == null ? null : ro.getCss(),
                        ro.isMitMassendownload(), ro.getTabellengroesse() == null ? null : ro.getTabellengroesse(),
                        ro.getSessiontimeout() == null ? null : ro.getSessiontimeout(),
                        ro.getLdapGruppe() == null ? null : ro.getLdapGruppe().getId(), visible, ro.getLdaplogin() == null ? null : ro.getLdaplogin(),
                        ro.isDisplayAutomaticTasks(), ro.isDisplayBatchColumn(), ro.isDisplayDeactivatedProjects(), ro.isDisplayFinishedProcesses(),
                        ro.isDisplayIdColumn(), ro.isDisplayLocksColumn(), ro.isDisplayModulesColumn(), ro.isDisplayOnlyOpenTasks(),
                        ro.isDisplayOnlySelectedTasks(), ro.isDisplayProcessDateColumn(), ro.isDisplayRulesetColumn(), ro.isDisplaySelectBoxes(),
                        ro.isDisplaySwappingColumn(), ro.isHideCorrectionTasks(), ro.getEmail() == null ? null : ro.getEmail(),
                        ro.getShortcutPrefix() == null ? "ctrl+shift" : ro.getShortcutPrefix(),
                        ro.getMetsEditorTime() == null ? null : ro.getMetsEditorTime(), ro.isMetsDisplayHierarchy(),
                        ro.isMetsDisplayPageAssignments(), ro.isMetsDisplayTitle(), ro.isMetsLinkImage(), ro.isDisplayOtherTasks(),
                        ro.getEncryptedPassword(), ro.getPasswordSalt(), ro.isMetsDisplayProcessID(), ro.isDisplayGridView(),
                        ro.isDisplayMetadataColumn(), ro.isDisplayThumbColumn(), ro.getCustomColumns(), ro.getCustomCss(),
                        ro.getMailNotificationLanguage(), ro.getInstitution() == null ? null : ro.getInstitution().getId(), ro.isSuperAdmin(),
                        ro.isDisplayInstitutionColumn(), ro.getDashboardPlugin(), ro.getSsoId(), ro.getProcessListDefaultSortField(),
                        ro.getProcessListDefaultSortOrder(), ro.getTaskListDefaultSortingField(), ro.getTaskListDefaultSortOrder(),
                        ro.isDisplayLastEditionDate(), ro.isDisplayLastEditionUser(), ro.isDisplayLastEditionDate(), ro.getDashboardConfiguration(),
                        ro.getUiMode() };

                sql.append("INSERT INTO benutzer (");
                sql.append(propNames.toString());
                sql.append(") VALUES (");
                sql.append(prop);
                sql.append(")");

                if (log.isTraceEnabled()) {
                    log.trace(sql.toString() + ", " + Arrays.toString(param));
                }

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }

            } else {
                sql.append("UPDATE benutzer SET ");
                sql.append("Vorname = ?, ");
                sql.append("Nachname = ?, ");
                sql.append("login = ?, ");
                sql.append("IstAktiv = ?, ");
                sql.append("Standort =  ?, ");
                sql.append("metadatensprache =  ?, ");
                sql.append("css =  ?, ");
                sql.append("mitMassendownload =  ?, ");
                sql.append("Tabellengroesse =  ?, ");
                sql.append("sessiontimeout =  ?, ");
                sql.append("ldapgruppenID =  ?, ");
                sql.append("isVisible =  ?, ");
                sql.append("ldaplogin =  ?, ");
                sql.append("displayAutomaticTasks =  ?, ");
                sql.append("displayBatchColumn =  ?, ");
                sql.append("displayDeactivatedProjects =  ?, ");
                sql.append("displayFinishedProcesses =  ?, ");
                sql.append("displayIdColumn =  ?, ");
                sql.append("displayLocksColumn =  ?, ");
                sql.append("displayModulesColumn =  ?, ");
                sql.append("displayOnlyOpenTasks =  ?, ");
                sql.append("displayOnlySelectedTasks =  ?, ");
                sql.append("displayProcessDateColumn =  ?, ");
                sql.append("displayRulesetColumn = ?, ");
                sql.append("displaySelectBoxes =  ?, ");
                sql.append("displaySwappingColumn =  ?, ");
                sql.append("hideCorrectionTasks =  ?, ");
                sql.append("email =  ?, ");
                sql.append("shortcut =  ?, ");
                sql.append("metseditortime =  ?, ");
                sql.append("metsDisplayHierarchy =  ?, ");
                sql.append("metsDisplayPageAssignments =  ?, ");
                sql.append("metsDisplayTitle =  ?, ");
                sql.append(" metsLinkImage =?, ");
                sql.append(" displayOtherTasks =?, ");
                sql.append(" encryptedPassword =?, ");
                sql.append(" salt =?, ");
                sql.append("metsDisplayProcessID =  ?, ");
                sql.append("displayGridView =  ?, ");
                sql.append("displayMetadataColumn =  ?, ");
                sql.append("displayThumbColumn =  ?, ");
                sql.append("customColumns =  ?, ");
                sql.append("customCss =  ?, ");
                sql.append("mailNotificationLanguage =  ?, ");
                sql.append("institution_id =  ?, ");
                sql.append("superadmin =  ?, ");
                sql.append("displayInstitutionColumn =  ?, ");
                sql.append("dashboardPlugin =  ?, ");
                sql.append("ssoId =  ?, ");
                sql.append("processses_sort_field =  ?, ");
                sql.append("processes_sort_order =  ?, ");
                sql.append("tasks_sort_field =  ?, ");
                sql.append("tasks_sort_order =  ?, ");
                sql.append(
                        "displayLastEditionDate  =  ?, displayLastEditionUser  =  ?, displayLastEditionTask = ?, dashboard_configuration = ?, ui_mode = ? ");
                sql.append(" WHERE BenutzerID = " + ro.getId() + ";");

                Object[] param = { ro.getVorname() == null ? null : ro.getVorname(), ro.getNachname() == null ? null : ro.getNachname(),
                        ro.getLogin() == null ? null : ro.getLogin(), ro.isIstAktiv(), ro.getStandort() == null ? null : ro.getStandort(),
                                ro.getMetadatenSprache() == null ? null : ro.getMetadatenSprache(), ro.getCss() == null ? null : ro.getCss(),
                                        ro.isMitMassendownload(), ro.getTabellengroesse() == null ? null : ro.getTabellengroesse(),
                                                ro.getSessiontimeout() == null ? null : ro.getSessiontimeout(),
                                                        ro.getLdapGruppe() == null ? null : ro.getLdapGruppe().getId(), visible, ro.getLdaplogin() == null ? null : ro.getLdaplogin(),
                                                                ro.isDisplayAutomaticTasks(), ro.isDisplayBatchColumn(), ro.isDisplayDeactivatedProjects(), ro.isDisplayFinishedProcesses(),
                                                                ro.isDisplayIdColumn(), ro.isDisplayLocksColumn(), ro.isDisplayModulesColumn(), ro.isDisplayOnlyOpenTasks(),
                                                                ro.isDisplayOnlySelectedTasks(), ro.isDisplayProcessDateColumn(), ro.isDisplayRulesetColumn(), ro.isDisplaySelectBoxes(),
                                                                ro.isDisplaySwappingColumn(), ro.isHideCorrectionTasks(), ro.getEmail() == null ? null : ro.getEmail(),
                                                                        ro.getShortcutPrefix() == null ? "ctrl+shift" : ro.getShortcutPrefix(),
                                                                                ro.getMetsEditorTime() == null ? null : ro.getMetsEditorTime(), ro.isMetsDisplayHierarchy(),
                                                                                        ro.isMetsDisplayPageAssignments(), ro.isMetsDisplayTitle(), ro.isMetsLinkImage(), ro.isDisplayOtherTasks(),
                                                                                        ro.getEncryptedPassword(), ro.getPasswordSalt(), ro.isMetsDisplayProcessID(), ro.isDisplayGridView(),
                                                                                        ro.isDisplayMetadataColumn(), ro.isDisplayThumbColumn(), ro.getCustomColumns(), ro.getCustomCss(),
                                                                                        ro.getMailNotificationLanguage(), ro.getInstitution() == null ? null : ro.getInstitution().getId(), ro.isSuperAdmin(),
                                                                                                ro.isDisplayInstitutionColumn(), ro.getDashboardPlugin(), ro.getSsoId(), ro.getProcessListDefaultSortField(),
                                                                                                ro.getProcessListDefaultSortOrder(), ro.getTaskListDefaultSortingField(), ro.getTaskListDefaultSortOrder(),
                                                                                                ro.isDisplayLastEditionDate(), ro.isDisplayLastEditionUser(), ro.isDisplayLastEditionTask(), ro.getDashboardConfiguration(),
                                                                                                ro.getUiMode() };

                if (log.isTraceEnabled()) {
                    log.trace(sql.toString() + ", " + Arrays.toString(param));
                }
                run.update(connection, sql.toString(), param);
            }

            if (SendMail.getInstance().getConfig().isEnableMail()) {

                String insert =
                        "INSERT INTO user_email_configuration (userid, projectid, stepname, open, inWork, done, error) VALUES (?, ?, ?, ?, ?, ?, ?)";
                String update = "UPDATE user_email_configuration set open = ?, inWork = ?, done = ?, error = ? where id = ?";
                if (ro.getEmailConfiguration() != null) {
                    for (UserProjectConfiguration upc : ro.getEmailConfiguration()) {
                        for (StepConfiguration sc : upc.getStepList()) {
                            if (sc.getId() == null) {

                                Integer id = run.insert(connection, insert, MySQLHelper.resultSetToIntegerHandler, ro.getId(), upc.getProjectId(),
                                        sc.getStepName(), sc.isOpen(), sc.isInWork(), sc.isDone(), sc.isError());
                                sc.setId(id);
                            } else {
                                if (!sc.isOpen() && !sc.isInWork() && !sc.isDone() && !sc.isError()) {
                                    run.update(connection, "DELETE FROM user_email_configuration WHERE id = ?", sc.getId());
                                } else {
                                    run.update(connection, update, sc.isOpen(), sc.isInWork(), sc.isDone(), sc.isError(), sc.getId());
                                }
                            }
                        }
                    }
                }
            }
        } finally

        {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        return ro;
    }

    public static void hideUser(User ro) throws SQLException {

        if (ro.getId() != null) {
            String currentUserName = ro.getNachVorname();
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                StringBuilder deactivateUserQuery = new StringBuilder();
                deactivateUserQuery.append("UPDATE benutzer SET ");
                deactivateUserQuery.append("isVisible = 'deleted', ");
                deactivateUserQuery.append("IstAktiv = false, ");
                deactivateUserQuery.append("login= 'deletedUser" + ro.getId() + "', ");
                deactivateUserQuery.append("email = '', ");
                deactivateUserQuery.append("Vorname ='deleted', ");
                deactivateUserQuery.append("Nachname = 'deleted', ");
                deactivateUserQuery.append("ssoId = '', ");
                deactivateUserQuery.append("ldaplogin = '' ");
                deactivateUserQuery.append("WHERE BenutzerID = " + ro.getId());

                if (log.isTraceEnabled()) {
                    log.trace(deactivateUserQuery.toString());
                }
                run.update(connection, deactivateUserQuery.toString());

                String processlogQuery = "UPDATE processlog SET userName = 'deleted user' WHERE userName = ?";
                run.update(connection, processlogQuery, currentUserName);

            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteUser(User ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM benutzer WHERE BenutzerID = " + ro.getId() + ";";
                if (log.isTraceEnabled()) {
                    log.trace(sql);
                }
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<String> getFilterForUser(int userId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzereigenschaften WHERE Titel = '_filter' AND BenutzerID = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { userId };
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            List<String> answer = new QueryRunner().query(connection, sql.toString(), resultSetToFilterListtHandler, param);
            return answer;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void addFilterToUser(int userId, String filter) throws SQLException {
        Connection connection = null;
        Timestamp datetime = new Timestamp(new Date().getTime());
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String propNames = "Titel, Wert, IstObligatorisch, DatentypenID, Auswahl, creationDate, BenutzerID";
            Object[] param = { "_filter", filter, false, 5, null, datetime, userId };
            String sql = "INSERT INTO " + "benutzereigenschaften" + " (" + propNames + ") VALUES ( ?, ?,? ,? ,? ,?,? )";
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void removeFilterFromUser(int userId, String filter) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { userId, filter };
            String sql = "DELETE FROM benutzereigenschaften WHERE Titel = '_filter' AND BenutzerID = ? AND Wert = ?";
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<User> getUsersForUsergroup(Usergroup usergroup) throws SQLException {
        return getUsers("Nachname,Vorname",
                "BenutzerID IN (SELECT BenutzerID FROM benutzergruppenmitgliedschaft WHERE BenutzerGruppenID=" + usergroup.getId() + ")", null, null,
                null);
    }

    public static List<User> getUserForStep(int stepId) throws SQLException {
        String sql =
                "select * from benutzer where BenutzerID in (select BenutzerID from schritteberechtigtebenutzer where  schritteberechtigtebenutzer.schritteID = ? )";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { stepId };
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            return run.query(connection, sql, UserManager.resultSetToUserListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void addUsergroupAssignment(User user, Integer gruppenID) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                // check if assignment exists
                String sql =
                        " SELECT * FROM benutzergruppenmitgliedschaft WHERE BenutzerID =" + user.getId() + " AND BenutzerGruppenID = " + gruppenID;
                boolean exists = new QueryRunner().query(connection, sql.toString(), checkForResultHandler);
                if (!exists) {
                    String insert = " INSERT INTO benutzergruppenmitgliedschaft (BenutzerID , BenutzerGruppenID) VALUES (" + user.getId() + ","
                            + gruppenID + ")";
                    new QueryRunner().update(connection, insert);
                }
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void addProjectAssignment(User user, Integer projektID) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                // check if assignment exists
                String sql = " SELECT * FROM projektbenutzer WHERE BenutzerID =" + user.getId() + " AND ProjekteID = " + projektID;
                boolean exists = new QueryRunner().query(connection, sql.toString(), checkForResultHandler);
                if (!exists) {
                    String insert = " INSERT INTO projektbenutzer (BenutzerID , ProjekteID) VALUES (" + user.getId() + "," + projektID + ")";
                    new QueryRunner().update(connection, insert);
                }
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteUsergroupAssignment(User user, int gruppenID) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = " DELETE FROM benutzergruppenmitgliedschaft WHERE BenutzerID =" + user.getId() + " AND BenutzerGruppenID = " + gruppenID;
                new QueryRunner().update(connection, sql);

            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteAllUsergroupAssignments(User user) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = " DELETE FROM benutzergruppenmitgliedschaft WHERE BenutzerID =" + user.getId();
                new QueryRunner().update(connection, sql);

            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteProjectAssignment(User user, int projektID) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM projektbenutzer WHERE BenutzerID =" + user.getId() + " AND ProjekteID = " + projektID;

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteAllProjectAssignments(User user) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM projektbenutzer WHERE BenutzerID =" + user.getId();

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static ResultSetHandler<Boolean> checkForResultHandler = new ResultSetHandler<Boolean>() {

        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return true;
                }
                return false;
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
    };

    public static ResultSetHandler<List<String>> resultSetToFilterListtHandler = new ResultSetHandler<List<String>>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    String filter = rs.getString("Wert");
                    answer.add(filter);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static User getUserByLogin(String loginName) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer WHERE login  = ?");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            User ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserHandler, loginName);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<User> getAllUsers() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer");

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            List<User> ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * Get all tasks for each project where the user has been assigned to. Check for each task, if the user has configured to get emails.
     * 
     * @param projects
     * @param id
     * @return
     * @throws SQLException
     */

    public static List<UserProjectConfiguration> getEmailConfigurationForUser(List<Project> projects, Integer id, boolean showAllItems)
            throws SQLException {
        List<UserProjectConfiguration> answer = new ArrayList<>();
        if (projects == null || projects.isEmpty() || !SendMail.getInstance().getConfig().isEnableMail()) {
            return answer;
        }

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            StringBuilder sql = new StringBuilder();

            if (MySQLHelper.isJsonCapable()) {
                // found mariadb
                sql.append("SELECT  ");
                sql.append("    id, sub.titel AS stepName, open, inWork, done, error ");
                sql.append("FROM ");
                sql.append("    (SELECT DISTINCT ");
                sql.append("        s.titel ");
                sql.append("    FROM ");
                sql.append("        schritte s ");
                sql.append("    WHERE ");
                sql.append("                s.ProzesseID IN (SELECT  ");
                sql.append("                ProzesseID ");
                sql.append("            FROM ");
                sql.append("                prozesse ");
                sql.append("            WHERE ");
                sql.append("                ProjekteID = ?) ");
                if (!showAllItems) {
                    sql.append("INTERSECT ");
                    sql.append("SELECT DISTINCT ");
                    sql.append("        s.titel ");
                    sql.append("    FROM ");
                    sql.append("        schritte s ");
                    sql.append("    WHERE ");
                    sql.append("            s.SchritteId IN (SELECT  ");
                    sql.append("                schritteID ");
                    sql.append("            FROM ");
                    sql.append("                schritteberechtigtegruppen ");
                    sql.append("            WHERE ");
                    sql.append("                BenutzerGruppenID IN (SELECT  ");
                    sql.append("                        b.BenutzerGruppenID ");
                    sql.append("                    FROM ");
                    sql.append("                        benutzergruppenmitgliedschaft bm ");
                    sql.append("                    LEFT JOIN benutzergruppen b ON bm.BenutzerGruppenID = b.BenutzerGruppenID ");
                    sql.append("                    WHERE ");
                    sql.append("                        bm.BenutzerID = ?)) ");
                }
                sql.append("    ORDER BY titel) sub ");
                sql.append("        LEFT JOIN ");
                sql.append("    user_email_configuration uec ON sub.titel = uec.stepname ");
                sql.append("        AND uec.projectid = ? ");
                sql.append("        AND uec.userid = ? ");
            } else {
                // older sql version without INTERSECT command
                if (showAllItems) {
                    sql.append("SELECT id, sub.titel AS stepName, open, inWork, done, error FROM (SELECT DISTINCT titel FROM schritte s1 WHERE ");
                    sql.append("EXISTS (SELECT NULL FROM prozesse WHERE ProjekteID = ? AND ProzesseID = s1.ProzesseID) ORDER BY titel) sub ");
                    sql.append("LEFT JOIN user_email_configuration uec ON sub.titel = uec.stepname AND uec.projectid = ? AND uec.userid = ? ");
                } else {
                    sql.append("SELECT id, sub.titel AS stepName, open, inWork, done, error FROM (SELECT DISTINCT titel FROM schritte s1 WHERE ");
                    sql.append("EXISTS (SELECT NULL FROM prozesse WHERE ProjekteID = ? AND ProzesseID = s1.ProzesseID) ");

                    sql.append("AND EXISTS (SELECT NULL FROM schritteberechtigtegruppen WHERE ");
                    sql.append("s1.SchritteID = schritteberechtigtegruppen.schritteID AND schritteberechtigtegruppen.BenutzerGruppenID in ");
                    sql.append("(SELECT benutzergruppenmitgliedschaft.BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE ");
                    sql.append("benutzergruppenmitgliedschaft.BenutzerID = ?)) ORDER BY titel) sub LEFT JOIN user_email_configuration uec ON ");
                    sql.append("sub.titel = uec.stepname AND uec.projectid = ? AND uec.userid = ?");
                }
            }
            for (Project project : projects) {
                UserProjectConfiguration upc = new UserProjectConfiguration();
                upc.setProjectName(project.getTitel());
                upc.setProjectId(project.getId());
                List<StepConfiguration> stepNames = null;
                if (showAllItems) {
                    stepNames = new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(StepConfiguration.class), project.getId(),
                            project.getId(), id);
                } else {
                    stepNames = new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(StepConfiguration.class), project.getId(),
                            id, project.getId(), id);
                }
                upc.setStepList(stepNames);
                if (stepNames != null && !stepNames.isEmpty()) {
                    answer.add(upc);
                }
            }
            return answer;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<User> getUsersToInformByMail(String stepName, Integer projectId, String stepStatus) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    benutzer.* ");
        sql.append("FROM ");
        sql.append("    user_email_configuration ");
        sql.append("        LEFT JOIN ");
        sql.append("    benutzer ON userid = BenutzerId ");
        sql.append("WHERE ");
        sql.append(stepStatus);
        sql.append(" = TRUE ");
        sql.append("    AND projectid = ? ");
        sql.append("    AND stepName = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            List<User> ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserListHandler, projectId, stepName);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteEmailAssignmentForProject(User user, int projektID) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM user_email_configuration WHERE userid =" + user.getId() + " AND projectid = " + projektID;

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }

    public static void deleteEmailAssignmentForStep(User user, int projectID, String stepName) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM user_email_configuration WHERE userid =" + user.getId() + " AND stepname = '" + stepName
                        + "' AND projectid = " + projectID;

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }

}
