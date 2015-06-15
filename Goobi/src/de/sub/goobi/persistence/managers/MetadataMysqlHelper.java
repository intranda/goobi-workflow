package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;
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

    public static void insertMetadata(int processid, Map<String, String> metadata) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO metadata (processid, name, value) VALUES ");
        List<Object> values = new ArrayList<Object>();
        for (Entry<String, String> pair : metadata.entrySet()) {
            sql.append("(" + processid + ", ? , ? ),");
            values.add(pair.getKey());
            values.add(StringEscapeUtils.escapeSql(pair.getValue()));

        }
        String sqlString = sql.toString().substring(0, sql.toString().length() - 1);
        Object[] param = values.toArray(new Object[values.size()]);
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sqlString, param);
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
    
    
    public static List<Integer> getAllProcessesWithMetadata(String name, String value) throws SQLException {
        String sql = "SELECT processid FROM metadata WHERE name = ? and value = ?";
        Object[] param = { name, value };
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

    public static ResultSetHandler<List<StringPair>> resultSetToMetadataHandler = new ResultSetHandler<List<StringPair>>() {
        @Override
        public List<StringPair> handle(ResultSet rs) throws SQLException {
            List<StringPair> answer = new ArrayList<StringPair>();
            try {
                while (rs.next()) {
                   String name = rs.getString("name");
                   String value = rs.getString("value");
                   StringPair sp = new StringPair(name, value);
                   answer.add(sp);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };
}
