package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

class UsergroupMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -6209215029673643876L;
    private static final Logger logger = Logger.getLogger(UsergroupMysqlHelper.class);

    public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen");
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
            List<Usergroup> ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Usergroup> getUsergroupsForUser(User user) throws SQLException {
        return getUsergroups("titel", "BenutzergruppenID IN (SELECT BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE BenutzerID="
                + user.getId() + ")", null, null);
    }

    public static int getUsergroupCount(String order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(BenutzergruppenID) FROM benutzergruppen");
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

    public static Usergroup getUsergroupById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM benutzergruppen WHERE BenutzergruppenID = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            Usergroup ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveUsergroup(Usergroup ro) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();

            if (ro.getId() == null) {
                String propNames = "titel, berechtigung";
                String propValues = "'" + StringEscapeUtils.escapeSql(ro.getTitel()) + "'," + ro.getBerechtigung() + "";
                sql.append("INSERT INTO benutzergruppen (");
                sql.append(propNames);
                sql.append(") VALUES (");
                sql.append(propValues);
                sql.append(")");

                logger.debug(sql.toString());
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
                if (id != null) {
                    ro.setId(id);
                }

            } else {
                sql.append("UPDATE benutzergruppen SET ");
                sql.append("titel = '" + StringEscapeUtils.escapeSql(ro.getTitel()) + "', ");
                sql.append("berechtigung = '" + ro.getBerechtigung() + "' ");
                sql.append(" WHERE BenutzergruppenID = " + ro.getId() + ";");
                logger.debug(sql.toString());
                run.update(connection, sql.toString());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteUsergroup(Usergroup ro) throws SQLException {
        if (ro.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM benutzergruppen WHERE BenutzergruppenID = " + ro.getId() + ";";
                logger.debug(sql);
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<Usergroup> getUserGroupsForStep(Integer stepId) throws SQLException {
        String sql =
                "select * from benutzergruppen where BenutzergruppenID in (select BenutzergruppenID from schritteberechtigtegruppen where  schritteberechtigtegruppen.schritteID = ? )";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { stepId };
            logger.debug(sql.toString() + ", " + param);
            return run.query(connection, sql, UsergroupManager.resultSetToUsergroupListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
