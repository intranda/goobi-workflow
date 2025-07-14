package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MySQLHelper implements Serializable {

    private static final long serialVersionUID = -1396485589047649760L;
    private static final int TIME_FOR_CONNECTION_VALID_CHECK = 5;

    private static MySQLHelper helper = new MySQLHelper();
    private ConnectionManager cm = null;

    protected static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private MySQLHelper() {
        this.cm = new ConnectionManager();
    }

    public enum SQLTYPE {
        H2,
        MYSQL,
        MARIADB;
    }

    private SQLTYPE type = null;

    public SQLTYPE getSqlType() {
        if (type == null) {
            try (Connection connection = MySQLHelper.getInstance().getConnection()) {
                DatabaseMetaData meta = connection.getMetaData();
                String dbType = meta.getDatabaseProductName();
                if ("H2".equals(dbType)) {
                    type = SQLTYPE.H2;
                } else if ("MySQL".equals(dbType)) {
                    type = SQLTYPE.MYSQL;
                } else {
                    type = SQLTYPE.MARIADB;
                }

            } catch (SQLException e) {
                log.error("Error while getting database provider information for SQL type request", e);
            }
        }
        return type;
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
            return ("H2".equals(dbType));
        } catch (SQLException e) {
            log.error("Error while getting database provider information for H2 usage request", e);
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
            log.error("Error while getting database provider information for JSON capability request", e);
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
            if (dbv.contains("~")) {
                version = dbv.substring(mariaIdx + 8, dbv.indexOf('+'));
            } else {
                Pattern p = Pattern.compile("5.*-(.*?)-maria"); //NOSONAR, regex is not vulnerable to backtracking
                Matcher m = p.matcher(dbv);
                if (m.find()) {
                    version = m.group(1);
                } else {
                    return false;
                }
            }
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
                log.error("error parsing database version: " + dbVersion);
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

        return nums[2] >= 3;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = null;

        int maxAttempts = ConfigurationHelper.getInstance().getMaxDatabaseConnectionRetries();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                connection = this.cm.getDataSource().getConnection(); //NOSONAR, Connection is closed by the methods that requested the connection
                if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
                    return connection;
                }
            } catch (SQLException e) {
                log.error("Connection failed: Trying to get new connection. Attempt: {} of {}", attempt, maxAttempts);
            }
        }

        try {
            log.error("Connection failed: Trying to get a connection from a new ConnectionManager");
            this.cm = new ConnectionManager();
            connection = this.cm.getDataSource().getConnection(); //NOSONAR, Connection is closed by the methods that requested the connection

            if (connection.isValid(TIME_FOR_CONNECTION_VALID_CHECK)) {
                return connection;
            }
        } catch (SQLException e) {
            log.error("Connection failed!");
        }

        return connection;
    }

    public static void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    public static MySQLHelper getInstance() {
        return helper;
    }

    public static ResultSetHandler<List<Integer>> resultSetToIntegerListHandler = new ResultSetHandler<>() {
        @Override
        public List<Integer> handle(ResultSet rs) throws SQLException {
            List<Integer> answer = new ArrayList<>();
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
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<List<String>> resultSetToStringListHandler = new ResultSetHandler<>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    answer.add(rs.getString(1));
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<String> resultSetToStringHandler = new ResultSetHandler<>() {
        @Override
        public String handle(ResultSet rs) throws SQLException {
            String answer = "";
            try {
                if (rs.next()) {
                    answer = rs.getString(1);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<Integer> resultSetToIntegerHandler = new ResultSetHandler<>() {
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
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<Boolean> resultSetToBooleanHandler = new ResultSetHandler<>() {
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
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<Long> resultSetToLongHandler = new ResultSetHandler<>() {
        @Override
        public Long handle(ResultSet rs) throws SQLException {
            Long answer = null;
            try {
                if (rs.next()) {
                    answer = Long.valueOf(rs.getLong(1));
                    if (rs.wasNull()) {
                        answer = 0l;
                    }
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static ResultSetHandler<Double> resultSetToDoubleHandler = new ResultSetHandler<>() {
        @Override
        public Double handle(ResultSet rs) throws SQLException {
            Double answer = null;
            try {
                if (rs.next()) {
                    answer = Double.valueOf(rs.getDouble(1));
                    if (rs.wasNull()) {
                        answer = 0.0;
                    }
                }
            } finally {
                rs.close();
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

    public static ResultSetHandler<List<Map<String, String>>> resultSetToMapListHandler = new ResultSetHandler<>() {

        @Override
        public List<Map<String, String>> handle(ResultSet rs) throws SQLException {
            List<Map<String, String>> answer = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null
                    answer.add(generateMapResult(rs));
                }
                return answer;
            } finally {
                rs.close();
            }
        }
    };

    public static ResultSetHandler<Map<String, String>> resultSetToMapHandler = new ResultSetHandler<>() {
        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return generateMapResult(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public final static ResultSetHandler<Map<String, Integer>> resultSetToIntMapHandler = new ResultSetHandler<>() {
        @Override
        public Map<String, Integer> handle(ResultSet rs) throws SQLException {
            Map<String, Integer> result = new HashMap<>();
            try {
                while (rs.next()) {
                    result.put(rs.getString(1), rs.getInt(2));

                }
            } finally {
                rs.close();
            }
            return result;
        }
    };

    public static ResultSetHandler<List<Object[]>> resultSetToObjectListHandler = new ResultSetHandler<>() {

        @Override
        public List<Object[]> handle(ResultSet rs) throws SQLException {
            try {
                List<Object[]> answer = new ArrayList<>();

                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getString(i);
                    }
                    answer.add(row);
                }
                return answer;
            } finally {
                rs.close();
            }
        }
    };

    private static Map<String, String> generateMapResult(ResultSet rs) throws SQLException {
        Map<String, String> answer = new HashMap<>();
        ResultSetMetaData meta = rs.getMetaData();
        int numberOfColumns = meta.getColumnCount();

        for (int i = 0; i < numberOfColumns; i++) {
            String columnName = meta.getColumnLabel(i + 1);
            String columnType = meta.getColumnTypeName(i + 1);
            if (columnType.contains("INT") || columnType.startsWith("DECIMAL")) {
                answer.put(columnName, rs.getInt(columnName) + "");
            } else if (columnType.startsWith("VARCHAR") || columnType.startsWith("TEXT")) {
                answer.put(columnName, rs.getString(columnName));
            } else if (columnType.startsWith("DATETIME") || columnType.startsWith("TIMESTAMP")) {
                Timestamp date = rs.getTimestamp(columnName);
                if (date != null) {
                    answer.put(columnName, formatter.print(date.getTime()));
                }
            } else if (columnType.startsWith("TINYINT")) {
                answer.put(columnName, rs.getString(columnName));
            }
        }
        return answer;
    }

    public static String convertDataToString(Map<String, String> additionalData) {
        if (additionalData != null && !additionalData.isEmpty()) {
            XStream xstream = new XStream();
            xstream.registerConverter(new MapToStringConverter());
            xstream.alias("root", Map.class);
            return xstream.toXML(additionalData);
        }
        return null;
    }

    public static Map<String, String> convertStringToMap(String additionalData) {

        if (StringUtils.isNotBlank(additionalData)) {
            XStream xstream = new XStream();
            xstream.registerConverter(new MapToStringConverter());
            xstream.alias("root", Map.class);
            xstream.allowTypes(new Class[] { Map.class });
            @SuppressWarnings("unchecked")

            Map<String, String> map = (HashMap<String, String>) xstream.fromXML(additionalData);
            return map;
        }

        return new HashMap<>();
    }

    public static class MapToStringConverter implements Converter {

        @Override
        public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
            return AbstractMap.class.isAssignableFrom(clazz);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {

            AbstractMap map = (AbstractMap) value;
            for (Object obj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                writer.startNode(entry.getKey().toString());
                Object val = entry.getValue();
                if (null != val) {
                    writer.setValue(val.toString());
                }
                writer.endNode();
            }

        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

            Map<String, String> map = new HashMap<>();

            while (reader.hasMoreChildren()) {
                reader.moveDown();

                String key = reader.getNodeName(); // nodeName aka element's name
                String value = reader.getValue();
                map.put(key, value);

                reader.moveUp();
            }

            return map;
        }

    }

    /**
     * Prepare the SQL query and generate order statement
     * 
     * If a regular column is used, nothing gets changed. But if a custom column is used, the query gets extended to include the data into the result
     * list, so ordering by this data is possible.
     * 
     * @param order column name to order
     * @param sql prepared sql statement, gets extended if needed
     * @return order statement
     */

    public static String prepareSortField(String order, StringBuilder sql) {
        if (StringUtils.isBlank(order) || !order.startsWith("{")) {
            return order;
        }

        String sortfield = "";
        boolean reverse = false;
        if (order.endsWith(" desc")) {
            reverse = true;
            order = order.replace(" desc", "");
        }
        if (order.endsWith(" asc")) {
            order = order.replace(" asc", "");
        }
        String fieldname = order.replace("{", "").replace("}", "").substring(order.indexOf("."));
        if (order.startsWith("{db_meta")) {
            sql.append("LEFT JOIN (SELECT processid, MAX(value) AS value FROM metadata WHERE metadata.name = '");
            sql.append(fieldname);
            sql.append("' GROUP BY processid) AS field ON field.processid = prozesse.prozesseID ");
        } else if (order.startsWith("{process.")) {
            sql.append(
                    "LEFT JOIN (SELECT object_id, MAX(property_value) AS value FROM properties WHERE properties.object_type='process' AND properties.property_name = '");
            sql.append(fieldname);
            sql.append("' GROUP BY object_id) AS field ON field.object_id = prozesse.prozesseID ");
        }
        if (reverse) {
            sortfield = " case when field.value = '' or field.value is null then 1 else 0 end, field.value desc ";
        } else {
            sortfield = " case when field.value = '' or field.value is null then 1 else 0 end, field.value ";
        }
        return sortfield;
    }

    public static String escapeSql(String value) {
        if (value == null) {
            return null;
        }

        return StringUtils.replace(value, "'", "''");

    }
}
