package de.sub.goobi.persistence.managers;

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
import org.goobi.beans.HistoryEvent;

import de.sub.goobi.helper.enums.HistoryEventType;

public class HistoryMysqlHelper {
    private static final Logger logger = Logger.getLogger(HistoryMysqlHelper.class);

    public static List<HistoryEvent> getHistoryEvents(int processId) throws SQLException {
        String sql = "SELECT * FROM history WHERE processID = " + processId;
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<HistoryEvent> list = new QueryRunner().query(connection, sql, resultSetToHistoryListHandler);
            return list;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void updateHistory(Integer id, Date date, double order, String value, int type, int processId) throws SQLException {
        Connection connection = null;
        Timestamp datetime = new Timestamp(date.getTime());

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            // String propNames = "numericValue, stringvalue, type, date, processId";
            Object[] param = { order, value == null ? null : value, type, datetime, processId };
            String sql = "Update history set numericValue = ?, stringvalue = ?,  type = ?, date = ?, processId =? WHERE historyid = " + id;
            logger.trace("added history event " + sql + ", " + Arrays.toString(param));
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void addHistory(Date date, double order, String value, int type, int processId) throws SQLException {
        Connection connection = null;
        Timestamp datetime = new Timestamp(date.getTime());

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            // String propNames = "numericValue, stringvalue, type, date, processId";
            Object[] param = { order, value, type, datetime, processId };
            String sql = "INSERT INTO " + "history" + " (numericValue, stringvalue, type, date, processId) VALUES ( ?, ?, ?, ? ,?)";
            logger.trace("added history event " + sql + ", " + Arrays.toString(param));
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void updateHistoryEvent(HistoryEvent he) throws SQLException {
        Connection connection = null;
        Timestamp datetime = new Timestamp(he.getDate().getTime());

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            // String propNames = "numericValue, stringvalue, type, date, processId";
            Object[] param = { he.getNumericValue(), he.getStringValue(), he.getHistoryType().getValue(), datetime, he.getProcess().getId() };
            String sql = "UPDATE history set numericValue = ?, stringvalue = ?, type = ?, date = ?, processId = ? WHERE historyid =" + he.getId();
            logger.trace("added history event " + sql + ", " + Arrays.toString(param));
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void deleteHistoryEvent(HistoryEvent he) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "DELETE from history  WHERE historyid =" + he.getId();
            run.update(connection, sql);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static ResultSetHandler<List<HistoryEvent>> resultSetToHistoryListHandler = new ResultSetHandler<List<HistoryEvent>>() {

        @Override
        public List<HistoryEvent> handle(ResultSet rs) throws SQLException {
            List<HistoryEvent> answer = new ArrayList<HistoryEvent>();
            try {
                while (rs.next()) {
                    int historyId = rs.getInt("historyid");
                    double numeric = rs.getDouble("numericvalue");
                    String stringvalue = rs.getString("stringvalue");
                    HistoryEventType type = HistoryEventType.getTypeFromValue(rs.getInt("type"));
                    Timestamp time = rs.getTimestamp("date");
                    HistoryEvent he = new HistoryEvent();
                    he.setDate(time == null ? null : new Date(time.getTime()));
                    he.setHistoryType(type);
                    he.setId(historyId);
                    he.setNumericValue(numeric);
                    he.setStringValue(stringvalue);

                    answer.add(he);

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
