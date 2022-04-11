package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.api.mq.ExternalCommandResult;
import org.goobi.beans.DatabaseObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ExternalMQMysqlHelper {
    public static void insertTicket(ExternalCommandResult message) throws SQLException {
        String sql = "INSERT INTO external_mq_results " + generateInsertQuery() + generateValueQuery();
        Object[] param = generateParameter(message);
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static ResultSetHandler<List<ExternalCommandResult>> rsToStatusMessageListHandler = new ResultSetHandler<List<ExternalCommandResult>>() {
        @Override
        public List<ExternalCommandResult> handle(ResultSet rs) throws SQLException {
            List<ExternalCommandResult> messageList = new ArrayList<>();
            while (rs.next()) {
                messageList.add(convertRow(rs));
            }
            return messageList;
        }
    };

    private static ExternalCommandResult convertRow(ResultSet rs) throws SQLException {
        int processId = rs.getInt("ProzesseID");
        int stepId = rs.getInt("SchritteID");
        String scriptName = rs.getString("scriptName");
        return new ExternalCommandResult(processId, stepId, scriptName);
    }

    private static Object[] generateParameter(ExternalCommandResult message) {
        return new Object[] { message.getProcessId(), message.getStepId(), new Date(), message.getScriptName() };
    }

    private static String generateInsertQuery() {
        return "(ProzesseID, SchritteID, time, scriptName) VALUES ";
    }

    private static String generateValueQuery() {
        return "(?,?,?,?)";
    }

    public static int getMessagesCount(String filter) throws SQLException {
        String sql = "SELECT COUNT(*) FROM external_mq_results WHERE " + filter;
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return new QueryRunner().query(conn, sql, MySQLHelper.resultSetToIntegerHandler);
        }
    }

    public static List<? extends DatabaseObject> getMessageList(String order, String filter, Integer start, Integer count) throws SQLException {
        String sql = "SELECT * FROM external_mq_results";
        if (filter != null && !filter.isEmpty()) {
            sql += " WHERE " + filter;
        }
        if (order != null && !order.isEmpty()) {
            sql += " ORDER BY " + order;
        }
        if (start != null && count != null) {
            sql += " LIMIT " + start + ", " + count;
        }
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return new QueryRunner().query(conn, sql, rsToStatusMessageListHandler);
        }
    }
}
