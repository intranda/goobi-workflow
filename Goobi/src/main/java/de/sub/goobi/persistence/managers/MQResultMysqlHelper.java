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
import org.goobi.api.mq.MqStatusMessage;
import org.goobi.beans.DatabaseObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MQResultMysqlHelper {

    public static void insertMessage(MqStatusMessage message) throws SQLException {
        String sql = "INSERT INTO mq_results " + generateInsertQuery() + generateValueQuery();
        Object[] param = generateParameter(message, false, false);
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

    private static ResultSetHandler<List<MqStatusMessage>> rsToStatusMessageListHandler = new ResultSetHandler<List<MqStatusMessage>>() {
        @Override
        public List<MqStatusMessage> handle(ResultSet rs) throws SQLException {
            List<MqStatusMessage> messageList = new ArrayList<>();
            while (rs.next()) {
                messageList.add(convertRow(rs));
            }
            return messageList;
        }
    };

    private static MqStatusMessage convertRow(ResultSet rs) throws SQLException {
        String ticketId = rs.getString("ticket_id");
        Date time = rs.getDate("time");
        MqStatusMessage.MessageStatus status = MqStatusMessage.MessageStatus.valueOf(rs.getString("status"));
        String message = rs.getString("message");
        String origMessage = rs.getString("original_message");
        return new MqStatusMessage(ticketId, time, status, message, origMessage);
    }

    private static Object[] generateParameter(MqStatusMessage message, boolean b, boolean c) {
        return new Object[] { message.getTicketId(), message.getTime(), message.getStatus().getName(), message.getMessage(),
                message.getOriginalMessage() };
    }

    private static String generateInsertQuery() {
        return "(ticket_id, time, status, message, original_message) VALUES ";
    }

    private static String generateValueQuery() {
        return "(?,?,?,?,?)";
    }

    public static int getMessagesCount(String filter) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mq_results WHERE " + filter;
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return new QueryRunner().query(conn, sql, MySQLHelper.resultSetToIntegerHandler);
        }
    }

    public static List<? extends DatabaseObject> getMessageList(String order, String filter, Integer start, Integer count) throws SQLException {
        String sql = "SELECT * FROM mq_results";
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
