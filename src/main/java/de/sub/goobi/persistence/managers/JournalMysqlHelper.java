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

package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;

class JournalMysqlHelper implements Serializable {

    private static final long serialVersionUID = 1125568423028562107L;

    public static JournalEntry saveLogEntry(JournalEntry logEntry) throws SQLException {

        if (logEntry.getId() == null) {
            return inserLogEntry(logEntry);
        } else {
            updateLogEntry(logEntry);
            return logEntry;
        }
    }

    private static void updateLogEntry(JournalEntry logEntry) throws SQLException {
        String sql =
                "UPDATE journal set objectID =?, creationDate = ?, userName = ?, type = ? , content = ?, filename = ?, entrytype = ? WHERE id = ?";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, logEntry.getObjectId(), new Timestamp(logEntry.getCreationDate().getTime()), logEntry.getUserName(),
                    logEntry.getType().getTitle(), logEntry.getContent(), logEntry.getFilename(), logEntry.getEntryType().getTitle(),
                    logEntry.getId());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static JournalEntry inserLogEntry(JournalEntry logEntry) throws SQLException {
        String sql = "INSERT INTO journal (objectID, creationDate, userName, type , content, filename, entrytype ) VALUES (?, ?,  ?, ?, ?, ?, ?);";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, logEntry.getObjectId(),
                    new Timestamp(logEntry.getCreationDate().getTime()), logEntry.getUserName(), logEntry.getType().getTitle(), logEntry.getContent(),
                    logEntry.getFilename(), logEntry.getEntryType().getTitle());
            logEntry.setId(id);
            return logEntry;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteLogEntry(JournalEntry logEntry) throws SQLException {
        if (logEntry.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM journal WHERE id = " + logEntry.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void deleteAllJournalEntries(Integer objectId, EntryType type) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "DELETE FROM journal WHERE objectId = ? and entrytype = ?";
            run.update(connection, sql, objectId, type.getTitle());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<JournalEntry> getLogEntries(int processId, EntryType entryType) throws SQLException {
        Connection connection = null;
        String sql = " SELECT * from journal WHERE objectID = ? AND entrytype = ? ORDER BY creationDate";
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, JournalManager.resultSetToLogEntryListHandler, processId, entryType.getTitle());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static JournalEntry getJournalEntryById(int id) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM journal WHERE id = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), new BeanHandler<>(JournalEntry.class));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
