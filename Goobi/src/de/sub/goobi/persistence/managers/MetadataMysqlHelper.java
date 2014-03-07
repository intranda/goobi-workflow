package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.production.cli.helper.StringPair;

class MetadataMysqlHelper implements Serializable {

    private static final long serialVersionUID = -8391750763010758774L;

    public static void removeMetadata(int processId) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "DELETE FROM metadata WHERE processid = " + processId;
            run.update(connection, sql);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void insertMetadata(int processid, List<StringPair> metadata) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO metadata (processid, name, value) VALUES ");

        for (StringPair pair : metadata) {
            sql.append("(" + processid + ", '" + pair.getOne() + "', '" + pair.getTwo() + "'),");
        }
        String sqlString = sql.toString().substring(0, sql.toString().length() - 1);

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sqlString);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getDistinctMetadataNames() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct name from metadata");

        sql.append(" ORDER BY name");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
