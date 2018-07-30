package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.goobi.org
 *          - http://launchpad.net/goobi-production
 *          - http://gdz.sub.uni-goettingen.de
 *          - http://www.intranda.com
 *          - http://digiverso.com 
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
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

public class MySQLHelper implements Serializable {

    private static final long serialVersionUID = -1396485589047649760L;
    private static final int MAX_TRIES_NEW_CONNECTION = 5;
    private static final int TIME_FOR_CONNECTION_VALID_CHECK = 5;

    private static final Logger logger = Logger.getLogger(MySQLHelper.class);

    private static MySQLHelper helper = new MySQLHelper();
    private ConnectionManager cm = null;

    private MySQLHelper() {
        this.cm = new ConnectionManager();
    }

    /**
     * Check if current database connection is based on H2. Otherwise it is Mysql or Mariadb
     * 
     * @return
     */
    public static boolean isUsingH2() {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String dbType = meta.getDatabaseProductName();
            return (dbType.equals("H2"));
        } catch (SQLException e) {
            logger.error("Error getting database provider information", e);
        }
        return false;
    }

    /**
     * Checks connected database for MariaDB version >=10.2.3 (from which on mariadb is json capable)
     * 
     * @return true if MariaDB >= 10.2.3, else false
     */
    public static boolean isJsonCapable() {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String dbVersion = meta.getDatabaseProductVersion();
            return (checkMariadbVersion(dbVersion));
        } catch (SQLException e) {
            logger.error("Error getting database provider information", e);
        }
        return false;
    }

    /**
     * checks if dbVersion is a valid mariadb version string for mariadb >= 10.2.3
     * 
     * @param dbVersion
     * @return
     */
    public static boolean checkMariadbVersion(String dbVersion) {
        // example version string: "5.5.5-10.2.13-MariaDB-10.2.13+maria~stretch"
        // other example: "10.0.34-MariaDB-0ubuntu0.16.04.1"
        String dbv = dbVersion.toLowerCase();
        int mariaIdx = dbv.indexOf("mariadb");
        if (mariaIdx < 0) {
            return false;
        }
        String version = null;
        if (dbv.startsWith("5")) {
            version = dbv.substring(mariaIdx + 8, dbv.indexOf('+'));
        } else {
            version = dbv.substring(0, dbv.indexOf('-'));
        }
        String[] numStrs = version.split("\\.");
        if (numStrs[0].contains(":")) {
            numStrs[0] = numStrs[0].split(":")[1];
        }
        int[] nums = new int[numStrs.length];
        for (int i = 0; i < numStrs.length; i++) {
            try {
                nums[i] = Integer.parseInt(numStrs[i]);
            } catch (NumberFormatException e) {
                logger.error("error parsing database version: " + dbVersion);
                return false;
            }
        }
        if (nums[0] < 10) {
            return false;
        }
        if (nums[0] > 10) {
            return true;
        }
        if (nums[1] < 2) {
            return false;
        }
        if (nums[1] > 2) {
            return true;
        }
        if (nums[2] < 3) {
            return false;
        }
        return true;
    }

    public Connection getConnection() throws SQLException {

        Connection connection = this.cm.getDataSource().getConnection();
        if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
            return connection;
        }

        for (int i = 0; i < MAX_TRIES_NEW_CONNECTION; i++) {

            logger.warn("Connection failed: Trying to get new connection. Attempt:" + i);

            connection = this.cm.getDataSource().getConnection();

            if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
                return connection;
            }
        }

        logger.warn("Connection failed: Trying to get a connection from a new ConnectionManager");
        this.cm = new ConnectionManager();
        connection = this.cm.getDataSource().getConnection();

        if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
            return connection;
        }

        logger.error("Connection failed!");

        return connection;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    public static MySQLHelper getInstance() {
        return helper;
    }

    public static ResultSetHandler<List<Integer>> resultSetToIntegerListHandler = new ResultSetHandler<List<Integer>>() {
        @Override
        public List<Integer> handle(ResultSet rs) throws SQLException {
            List<Integer> answer = new ArrayList<Integer>();
            try {
                while (rs.next()) {
                    // returning object to get null object, rs.getInt returns '0' on null value
                    Object object = rs.getObject(1);
                    if (object != null) {
                        if (object instanceof Long) {
                            Long l = (Long) object;
                            answer.add(l.intValue());
                        } else if (object instanceof Integer) {
                            Integer in = (Integer) object;
                            answer.add(in);
                        }
                    } else {
                        answer.add(null);
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

    public static ResultSetHandler<List<String>> resultSetToStringListHandler = new ResultSetHandler<List<String>>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<String>();
            try {
                while (rs.next()) {
                    answer.add(rs.getString(1));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static ResultSetHandler<String> resultSetToStringHandler = new ResultSetHandler<String>() {
        @Override
        public String handle(ResultSet rs) throws SQLException {
            String answer = "";
            try {
                if (rs.next()) {
                    answer = rs.getString(1);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static ResultSetHandler<Integer> resultSetToIntegerHandler = new ResultSetHandler<Integer>() {
        @Override
        public Integer handle(ResultSet rs) throws SQLException {
            Integer answer = null;
            try {
                if (rs.next()) {
                    Integer id = rs.getInt(1);
                    if (rs.wasNull()) {
                        id = 0;
                    }
                    return id;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static ResultSetHandler<Boolean> resultSetToBooleanHandler = new ResultSetHandler<Boolean>() {
        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            Boolean answer = null;
            try {
                if (rs.next()) {
                    Boolean id = rs.getBoolean(1);
                    if (rs.wasNull()) {
                        id = false;
                    }
                    return id;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static ResultSetHandler<Long> resultSetToLongHandler = new ResultSetHandler<Long>() {
        @Override
        public Long handle(ResultSet rs) throws SQLException {
            Long answer = null;
            try {
                if (rs.next()) {
                    answer = new Long(rs.getLong(1));
                    if (rs.wasNull()) {
                        answer = 0l;
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

    public static ResultSetHandler<Double> resultSetToDoubleHandler = new ResultSetHandler<Double>() {
        @Override
        public Double handle(ResultSet rs) throws SQLException {
            Double answer = null;
            try {
                if (rs.next()) {
                    answer = new Double(rs.getDouble(1));
                    if (rs.wasNull()) {
                        answer = 0.0;
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

    public static String escapeString(String inputString) {
        if (inputString != null) {
            inputString = inputString.replace("\\", "\\\\");
            inputString = inputString.replace("%", "\\%");
            inputString = inputString.replace("_", "\\_");
            inputString = inputString.replace("?", "_");
            inputString = inputString.replace("*", "%");
        }
        return inputString;
    }

}
