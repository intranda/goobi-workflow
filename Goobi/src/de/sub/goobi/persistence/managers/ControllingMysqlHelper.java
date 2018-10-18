package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;

class ControllingMysqlHelper implements Serializable {

    private static final long serialVersionUID = 6792892688043858306L;

    /**
     * search for a query and return the result values as a list of Object[] for each row.
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    static List<Object[]> getResultsAsObjectList(String sql) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToObjectListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * search for a query and return the first result as a map. The column names are used as keys
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    static Map<String, String> getSingleResultAsMap(String sql) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToMapHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * search for a query and return the result as a list of maps. Each result row is converted into a map, using the column names as keys and all
     * rows are returned as a list. The order of the entries in the list is the same as in the sql result
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    static List<Map<String, String>> getResultsAsMaps(String sql) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToMapListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

}
