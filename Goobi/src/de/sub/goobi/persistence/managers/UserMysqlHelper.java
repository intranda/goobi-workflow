package de.sub.goobi.persistence.managers;

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
import org.apache.log4j.Logger;
import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.helper.exceptions.DAOException;

class UserMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6133952688951728602L;

    private static final Logger logger = Logger.getLogger(UserMysqlHelper.class);

    public static List<User> getUsers(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzer");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            List<User> ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getUserCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(BenutzerID) FROM benutzer");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
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
            logger.debug(sql.toString());
            User ret = new QueryRunner().query(connection, sql.toString(), UserManager.resultSetToUserHandler);
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
                        "Vorname, Nachname, login, passwort, IstAktiv, Standort, metadatensprache, css, mitMassendownload, Tabellengroesse, sessiontimeout, ldapgruppenID, isVisible, ldaplogin,"
                        + "displayAutomaticTasks, displayBatchColumn, displayDeactivatedProjects, displayFinishedProcesses, displayIdColumn, displayLocksColumn, "
                        + "displayModulesColumn, displayOnlyOpenTasks, displayOnlySelectedTasks, displayProcessDateColumn, displaySelectBoxes, displaySwappingColumn, hideCorrectionTasks";

                String prop = "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
                Object[] param =
                        {ro.getVorname() == null ? null : ro.getVorname(), 
                        ro.getNachname() == null ? null : ro.getNachname(), 
                        ro.getLogin() == null ? null : ro.getLogin(), 
                        ro.getPasswort() == null ? null : ro.getPasswort(), 
                        ro.isIstAktiv(), 
                        ro.getStandort() == null ? null : ro.getStandort(), 
                        ro.getMetadatenSprache() == null ? null : ro.getMetadatenSprache(), 
                        ro.getCss() == null ? null : ro.getCss(), 
                        ro.isMitMassendownload(), 
                        ro.getTabellengroesse() == null ? null : ro.getTabellengroesse(), 
                        ro.getSessiontimeout() == null ? null : ro.getSessiontimeout(), 
                        ro.getLdapGruppe() == null ?  null : ro.getLdapGruppe().getId(), 
                        visible, 
                        ro.getLdaplogin()  == null ? null : ro.getLdaplogin(),
                        ro.isDisplayAutomaticTasks(),
                        ro.isDisplayBatchColumn(),
                        ro.isDisplayDeactivatedProjects(),
                        ro.isDisplayFinishedProcesses(),
                        ro.isDisplayIdColumn(),
                        ro.isDisplayLocksColumn(),
                        ro.isDisplayModulesColumn(),
                        ro.isDisplayOnlyOpenTasks(),
                        ro.isDisplayOnlySelectedTasks(),
                        ro.isDisplayProcessDateColumn(),
                        ro.isDisplaySelectBoxes(),
                        ro.isDisplaySwappingColumn(),
                        ro.isHideCorrectionTasks()
                        };
                sql.append("INSERT INTO benutzer (");
                sql.append(propNames.toString());
                sql.append(") VALUES (");
                sql.append(prop);
                sql.append(")");

                logger.debug(sql.toString() + ", " + Arrays.toString(param));

                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
                if (id != null) {
                    ro.setId(id);
                }

            } else {
                sql.append("UPDATE benutzer SET ");
                sql.append("Vorname = ?, " );
                sql.append("Nachname = ?, ");
                sql.append("login = ?, ");
                sql.append("passwort = ?, ");
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
                sql.append("displaySelectBoxes =  ?, ");
                sql.append("displaySwappingColumn =  ?, ");
                sql.append("hideCorrectionTasks =  ? ");
                sql.append(" WHERE BenutzerID = " + ro.getId() + ";");
                Object[] param =
                    {ro.getVorname() == null ? null : ro.getVorname(), 
                    ro.getNachname() == null ? null : ro.getNachname(), 
                    ro.getLogin() == null ? null : ro.getLogin(), 
                    ro.getPasswort() == null ? null : ro.getPasswort(), 
                    ro.isIstAktiv(), 
                    ro.getStandort() == null ? null : ro.getStandort(), 
                    ro.getMetadatenSprache() == null ? null : ro.getMetadatenSprache(), 
                    ro.getCss() == null ? null : ro.getCss(), 
                    ro.isMitMassendownload(), 
                    ro.getTabellengroesse() == null ? null : ro.getTabellengroesse(), 
                    ro.getSessiontimeout() == null ? null : ro.getSessiontimeout(), 
                    ro.getLdapGruppe() == null ?  null : ro.getLdapGruppe().getId(), 
                    visible, 
                    ro.getLdaplogin()  == null ? null : ro.getLdaplogin(),
                    ro.isDisplayAutomaticTasks(),
                    ro.isDisplayBatchColumn(),
                    ro.isDisplayDeactivatedProjects(),
                    ro.isDisplayFinishedProcesses(),
                    ro.isDisplayIdColumn(),
                    ro.isDisplayLocksColumn(),
                    ro.isDisplayModulesColumn(),
                    ro.isDisplayOnlyOpenTasks(),
                    ro.isDisplayOnlySelectedTasks(),
                    ro.isDisplayProcessDateColumn(),
                    ro.isDisplaySelectBoxes(),
                    ro.isDisplaySwappingColumn(),
                    ro.isHideCorrectionTasks()};
                logger.debug(sql.toString() + ", " + Arrays.toString(param));
                run.update(connection, sql.toString(), param);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        return ro;
    }

    public static void hideUser(User ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "UPDATE benutzer SET isVisible = 'deleted' WHERE BenutzerID = " + ro.getId() + ";";
                logger.debug(sql);
                run.update(connection, sql);
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
                logger.debug(sql);
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
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
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
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
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
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<User> getUsersForUsergroup(Usergroup usergroup) throws SQLException {
        return getUsers("Nachname,Vorname", "BenutzerID IN (SELECT BenutzerID FROM benutzergruppenmitgliedschaft WHERE BenutzerGruppenID="
                + usergroup.getId() + ")", null, null);
    }

    public static List<User> getUserForStep(int stepId) throws SQLException {
        String sql =
                "select * from benutzer where BenutzerID in (select BenutzerID from schritteberechtigtebenutzer where  schritteberechtigtebenutzer.schritteID = ? )";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { stepId };
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
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
                    String insert =
                            " INSERT INTO benutzergruppenmitgliedschaft (BenutzerID , BenutzerGruppenID) VALUES (" + user.getId() + "," + gruppenID
                                    + ")";
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
            List<String> answer = new ArrayList<String>();
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

    public static void main(String[] args) {
        try {
            List<Ldap> ldapList = LdapManager.getLdaps(null, null, 0, 10);
            for (Ldap ldap : ldapList) {
                System.out.println(ldap.getTitel());
            }

            List<User> list = UserMysqlHelper.getUsers(null, "login = 'testadmin'", 0, 10);
            for (User u : list) {
                System.out.println(u.getLogin());
            }
        } catch (SQLException e) {
            logger.error(e);
        } catch (DAOException e) {
            logger.error(e);
        }
    }
}
