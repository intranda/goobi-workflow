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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
import org.apache.commons.lang.StringUtils;
import org.goobi.api.mq.MqStatusMessage;
import org.goobi.beans.DatabaseObject;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class MQResultMysqlHelper {

    public static void insertMessage(MqStatusMessage message) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO mq_results (ticket_id, time, status, message, original_message, objects, processid, stepid ");
        sql.append(", ticketType, ticketName) VALUES (?,?,?,?,?, ?,?,?,?,?) ");

        Object[] param = generateParameter(message);
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (log.isTraceEnabled()) {
                log.trace(sql + ", " + Arrays.toString(param));
            }
            run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
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
        int numberOfObjects = rs.getInt("objects");
        String ticketType = rs.getString("ticketType");
        int processid = rs.getInt("processid");
        int stepid = rs.getInt("stepid");
        String ticketName = rs.getString("ticketName");
        return new MqStatusMessage(ticketId, time, status, message, origMessage, numberOfObjects, ticketType, processid, stepid, ticketName);
    }

    private static Object[] generateParameter(MqStatusMessage message) {
        return new Object[] { message.getTicketId(), message.getTime(), message.getStatus().getName(), message.getStatusMessage(),
                message.getOriginalMessage(), message.getNumberOfObjects(), message.getProcessid(), message.getStepId(), message.getTicketType(),
                message.getTicketName() };
    }

    public static int getMessagesCount(String filter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(1) FROM mq_results ");

        if (StringUtils.isNotBlank(filter)) {
            String filterString = MySQLHelper.escapeSql(filter);
            sql.append(" WHERE message like '%");
            sql.append(filterString);
            sql.append("%' OR  original_message like '%");
            sql.append(filterString);
            sql.append("%' OR  ticketType like  '%");
            sql.append(filterString);
            sql.append("%' OR  ticketName like  '%");
            sql.append(filterString);
            sql.append("%' ");
        }

        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return new QueryRunner().query(conn, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        }
    }

    public static List<? extends DatabaseObject> getMessageList(String order, String filter, Integer start, Integer count) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM mq_results");
        if (StringUtils.isNotBlank(filter)) {
            String filterString = MySQLHelper.escapeSql(filter);
            sql.append(" WHERE message like '%");
            sql.append(filterString);
            sql.append("%' OR  original_message like '%");
            sql.append(filterString);
            sql.append("%' OR  ticketType like  '%");
            sql.append(filterString);
            sql.append("%' OR  ticketName like  '%");
            sql.append(filterString);
            sql.append("%' ");
        }
        if (StringUtils.isNotBlank(order)) {
            sql.append(" ORDER BY ").append(order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT ").append(start).append(", ").append(count);
        }
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            return new QueryRunner().query(conn, sql.toString(), rsToStatusMessageListHandler);
        }
    }

    public static List<String> getAllTicketNames() throws SQLException {
        List<String> values = new ArrayList<>();
        values.add(""); // to select all ticket types

        String sql = "select distinct ticketName from mq_results where ticketName is not null order by ticketName";
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            values.addAll(new QueryRunner().query(conn, sql, MySQLHelper.resultSetToStringListHandler));
        }

        return values;
    }

}
