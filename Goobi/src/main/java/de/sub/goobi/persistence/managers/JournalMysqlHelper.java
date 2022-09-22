package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
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
}
