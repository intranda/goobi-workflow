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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JournalManager implements IManager, Serializable {

    private static final long serialVersionUID = -7235727567932576532L;

    public static void saveJournalEntry(JournalEntry entry) {
        try {
            JournalMysqlHelper.saveLogEntry(entry);
        } catch (SQLException e) {
            log.error("Cannot not update journal for object with id " + entry.getObjectId(), e);
        }
    }

    /**
     * Delete a single entry from journal.
     * 
     * @param entry to delete
     */

    public static void deleteJournalEntry(JournalEntry entry) {
        try {
            JournalMysqlHelper.deleteLogEntry(entry);
        } catch (SQLException e) {
            log.error("Cannot not delete journal for object with id " + entry.getObjectId(), e);
        }
    }

    public static void deleteAllJournalEntries(Integer objectId, EntryType type) {
        try {
            JournalMysqlHelper.deleteAllJournalEntries(objectId, type);
        } catch (SQLException e) {
            log.error("Cannot not delete journal entries");
        }
    }

    public static List<JournalEntry> getLogEntriesForProcess(Integer id) {
        return getLogEntriesForObject(id, EntryType.PROCESS);
    }

    public static List<JournalEntry> getLogEntriesForInstitution(Integer id) {
        return getLogEntriesForObject(id, EntryType.INSTITUTION);
    }

    public static List<JournalEntry> getLogEntriesForUser(Integer id) {
        return getLogEntriesForObject(id, EntryType.USER);
    }

    public static List<JournalEntry> getLogEntriesForProject(Integer id) {
        return getLogEntriesForObject(id, EntryType.PROJECT);
    }

    public static List<JournalEntry> getLogEntriesForObject(Integer id, EntryType entryType) {
        try {
            return JournalMysqlHelper.getLogEntries(id, entryType);
        } catch (SQLException e) {
            log.error("Cannot not get journal for object with id " + id, e);
        }
        return Collections.emptyList();
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        throw new UnsupportedOperationException();
    }

    public static JournalEntry getJournalEntryById(int id) {
        try {
            return JournalMysqlHelper.getJournalEntryById(id);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static final ResultSetHandler<List<JournalEntry>> resultSetToLogEntryListHandler = new ResultSetHandler<>() {
        @Override
        public List<JournalEntry> handle(ResultSet rs) throws SQLException {
            List<JournalEntry> answer = new ArrayList<>();
            try {
                while (rs.next()) {

                    int id = rs.getInt("id");
                    int objectID = rs.getInt("objectID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    } else {
                        creationDate = new Date();
                    }
                    String userName = rs.getString("userName");
                    LogType type = LogType.getByTitle(rs.getString("type"));
                    String content = rs.getString("content");
                    String filename = rs.getString("filename");
                    String entryType = rs.getString("entrytype");

                    JournalEntry entry = new JournalEntry(objectID, creationDate, userName, type, content, EntryType.getByTitle(entryType));
                    entry.setId(id);
                    entry.setFilename(filename);
                    answer.add(entry);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

}
