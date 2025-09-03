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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.production.cli.helper.StringPair;

import com.google.gson.Gson;

final class MetadataMysqlHelper implements Serializable {

    private MetadataMysqlHelper() {
        // hide implicit public constructor
    }

    private static final long serialVersionUID = -8391750763010758774L;
    private static Gson gson = new Gson();

    /**
     * deletes metadata for processID from `metadata_json` table
     * 
     * @param processID
     * @throws SQLException
     */
    public static void removeJSONMetadata(int processID) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "DELETE FROM metadata_json WHERE processid = " + processID;
            run.update(connection, sql);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * deletes metadata values for processId from `metadata` table
     * 
     * @param processId
     * @throws SQLException
     */
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

    /**
     * inserts metadata into table `metadata_json` with one row per process and the values as json object
     * 
     * @param processid
     * @param metadata
     */
    public static void insertJSONMetadata(int processid, Map<String, List<String>> metadata) throws SQLException {

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sqlString = "INSERT INTO metadata_json (processid, value) VALUES (?,?)";
            Object[] param = new Object[] { processid, gson.toJson(metadata) };
            run.update(connection, sqlString, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * inserts metadata in two tables: `metadata` with one row per metadata value
     * 
     * @param processid the process id for the metadata
     * @param metadata the metadata
     * @throws SQLException
     */
    public static void insertMetadata(int processid, Map<String, List<String>> metadata) throws SQLException {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO metadata (processid, name, value, print) VALUES ");
        List<Object> values = new ArrayList<>();
        for (Entry<String, List<String>> pair : metadata.entrySet()) {
            String metadataName = pair.getKey();
            List<String> valueList = pair.getValue();
            StringBuilder sb = new StringBuilder();
            Iterator<String> iter = valueList.iterator();
            while (iter.hasNext()) {
                sb.append(MySQLHelper.escapeSql(iter.next()));
                if (iter.hasNext()) {
                    sb.append("; ");
                }
            }
            for (String item : valueList) {
                sql.append("(" + processid + ", ? , ?, ? ),");
                values.add(metadataName);
                values.add(item.trim());
                values.add(sb.toString());
            }

        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sqlString = sql.toString().substring(0, sql.toString().length() - 1);
            Object[] param = values.toArray(new Object[values.size()]);
            if (!values.isEmpty()) {
                run.update(connection, sqlString, param);
            }

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getDistinctMetadataNames() throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct name from metadata where name like 'index.%' ");
        sql.append("union select distinct name from metadata where name not like 'index.%'");
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

    public static List<StringPair> getMetadata(int processId) throws SQLException {
        String sql = "SELECT * FROM metadata WHERE processid = ? ORDER BY name";
        Object[] param = { processId };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToMetadataHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static String getMetadataValue(int processId, String metadataName) throws SQLException {
        String sql = "SELECT value FROM metadata WHERE processid = ? and name = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler, processId, metadataName);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static String getAllValuesForMetadata(int processId, String metadataName) throws SQLException {
        String sql = "SELECT print FROM metadata WHERE processid = ? and name = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler, processId, metadataName);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getAllMetadataValues(int processId, String metadataName) throws SQLException {
        String sql = "SELECT value FROM metadata WHERE processid = ? and name = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler, processId, metadataName);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Integer> getAllProcessesWithMetadata(String name, String value) throws SQLException {
        String sql = "SELECT processid FROM metadata WHERE name = ? and value LIKE '%" + MySQLHelper.escapeSql(value) + "%'";
        Object[] param = { name };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToIntegerListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static final ResultSetHandler<List<StringPair>> resultSetToMetadataHandler = new ResultSetHandler<>() {
        @Override
        public List<StringPair> handle(ResultSet rs) throws SQLException {
            List<StringPair> answer = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null
                    String name = rs.getString("name");
                    String value = rs.getString("value");
                    StringPair sp = new StringPair(name, value);
                    answer.add(sp);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };
}
