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
 */
package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.repository.Repository;

public class HarvesterRepositoryMysqlHelper implements Serializable {

    private static final long serialVersionUID = -8160933323894230856L;

    private static final DateTimeFormatter formatterISO8601DateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static Repository getRepository(Integer repositoryId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "SELECT * FROM repository WHERE id=?";
            List<Repository> retList = new QueryRunner().query(connection, sql, resultSetToRepositoriesList, repositoryId);
            if (!retList.isEmpty()) {
                return retList.get(0);
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * Converts {@link ResultSet} to {@link ArrayList} of {@link OAIDublinCoreRepository}
     */
    public static final ResultSetHandler<List<Repository>> resultSetToRepositoriesList = new ResultSetHandler<List<Repository>>() {
        @Override
        public List<Repository> handle(ResultSet rs) throws SQLException {
            List<Repository> retList = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                Repository r = new Repository(rs.getInt("id"), rs.getString("name"), rs.getString("base_url"), rs.getString("export_folder"),
                        rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"), rs.getBoolean("enabled"),
                        rs.getBoolean("goobi_import"), rs.getString("project_name"), rs.getString("template_name"), rs.getString("fileformat"));
                r.setRepositoryType(rs.getString("type"));
                r.setTestMode(rs.getBoolean("testmode"));
                r.setStartDate(rs.getString("start_date"));
                r.setEndDate(rs.getString("end_date"));

                String query = "SELECT name, value FROM repository_parameter WHERE repository_id = ?";
                Connection connection = null;
                try {
                    connection = MySQLHelper.getInstance().getConnection();
                    Map<String, String> parameter = new QueryRunner().query(connection, query, resultSetToMapHandler, r.getId());
                    r.setParameter(parameter);
                } finally {
                    if (connection != null) {
                        MySQLHelper.closeConnection(connection);
                    }
                }
                retList.add(r);
            }
            return retList;
        }
    };

    public static int getRepositoryCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder("SELECT count(1) FROM repository");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<Repository> getRepositories(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder("SELECT * FROM repository");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), resultSetToRepositoriesList);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveRepository(Repository repository) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (repository.getId() == null) {

                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO repository ");
                sql.append("(name, base_url, export_folder, script_path, last_harvest, freq, delay, enabled, type, ");
                sql.append("goobi_import, project_name, template_name, fileformat, testmode, start_date, end_date) ");
                sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                Integer id = runner.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, repository.getName(),
                        repository.getUrl(), repository.getExportFolderPath(), repository.getScriptPath(), repository.getLastHarvest(),
                        repository.getFrequency(), repository.getDelay(), repository.isEnabled(), repository.getRepositoryType(),
                        repository.isGoobiImport(), repository.getImportProjectName(), repository.getProcessTemplateName(),
                        repository.getFileformat(), repository.isTestMode(), repository.getStartDate(), repository.getEndDate());
                repository.setId(id);
            } else {
                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE repository SET name = ?, base_url = ?, export_folder = ?, script_path = ?, last_harvest = ?, ");
                sql.append("freq = ?, delay = ?, enabled = ?, type = ?, goobi_import = ?, project_name = ?, ");
                sql.append("template_name = ?, fileformat = ?, testmode = ?, start_date = ?, end_date = ? WHERE id = ? ");
                runner.update(connection, sql.toString(), repository.getName(), repository.getUrl(), repository.getExportFolderPath(),
                        repository.getScriptPath(), repository.getLastHarvest(), repository.getFrequency(), repository.getDelay(),
                        repository.isEnabled(), repository.getRepositoryType(), repository.isGoobiImport(), repository.getImportProjectName(),
                        repository.getProcessTemplateName(), repository.getFileformat(), repository.isTestMode(), repository.getStartDate(),
                        repository.getEndDate(), repository.getId());

            }
            if (!repository.getParameter().isEmpty()) {
                String deletion = "delete from repository_parameter where repository_id = ?";
                runner.update(connection, deletion, repository.getId());

                String insert = "INSERT INTO repository_parameter (repository_id, name, value) VALUES (?,?,?)";
                for (Entry<String, String> param : repository.getParameter().entrySet()) {

                    runner.update(connection, insert, repository.getId(), param.getKey(), param.getValue());

                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void deleteRepository(Repository repository) throws SQLException {
        if (repository.getId() != null) {
            String deletion = "DELETE FROM repository_parameter WHERE repository_id = ?";
            String sql = "DELETE FROM repository WHERE id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                new QueryRunner().update(connection, deletion, repository.getId());
                new QueryRunner().update(connection, sql, repository.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    private static ResultSetHandler<Map<String, String>> resultSetToMapHandler = new ResultSetHandler<Map<String, String>>() {
        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getString("value"));
            }
            return map;
        }
    };

    public static Timestamp getLastHarvest(Integer repositoryId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "SELECT last_harvest FROM repository WHERE id=?";
            return new QueryRunner().query(connection, sql, new ResultSetHandler<Timestamp>() {
                @Override
                public Timestamp handle(ResultSet rs) throws SQLException {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getTimestamp("last_harvest");
                }
            }, repositoryId);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int addRecords(List<Record> recordList, boolean allowUpdates) throws SQLException {
        int counterNew = 0;
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            // Check if same Record is already in DB. This check makes this method pretty slow, but secure..
            String sqlCheck = "SELECT count(1) FROM record WHERE identifier=?";
            QueryRunner run = new QueryRunner();
            for (Record rec : recordList) {
                String subquery = rec.getSubquery() == null ? "" : " AND subquery='" + rec.getSubquery() + "'";
                // Check whether the record has previously been harvested
                int i = run.query(connection, sqlCheck + subquery, MySQLHelper.resultSetToIntegerHandler, MySQLHelper.escapeSql(rec.getIdentifier()));
                if (i == 0) {
                    // Add record to DB
                    StringBuilder sb = new StringBuilder();
                    sb.append("INSERT INTO record ");
                    sb.append("(repository_id,title,creator,repository_datestamp,setSpec,identifier,job_id,source,subquery) ");
                    sb.append("VALUES ");
                    sb.append("(?,?,?,?,?,?,?,?,?) ");
                    run.update(connection, sb.toString(), rec.getRepositoryId(), MySQLHelper.escapeSql(rec.getTitle()),
                            MySQLHelper.escapeSql(rec.getCreator()), rec.getRepositoryTimestamp(), rec.getSetSpec(),
                            MySQLHelper.escapeSql(rec.getIdentifier()), rec.getJobId(), MySQLHelper.escapeSql(rec.getSource()),
                            MySQLHelper.escapeSql(rec.getSubquery()));
                    counterNew++;
                } else if (allowUpdates) {
                    // Update existing record and reset exported status
                    StringBuilder sb = new StringBuilder();
                    sb.append("UPDATE record SET ");
                    sb.append(" exported=NULL, title=?, creator=?, repository_datestamp=?, ");
                    sb.append("setSpec=?, job_id=?, source=?, subquery=? ");
                    sb.append("WHERE identifier = ? ");
                    run.update(connection, sb.toString(), MySQLHelper.escapeSql(rec.getTitle()), MySQLHelper.escapeSql(rec.getCreator()),
                            rec.getRepositoryTimestamp(), rec.getSetSpec(), rec.getJobId(), MySQLHelper.escapeSql(rec.getSource()),
                            MySQLHelper.escapeSql(rec.getSubquery()), rec.getIdentifier());
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        return counterNew;
    }

    public static List<String> getExistingIdentifier(List<String> identifiers) throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (String identifier : identifiers) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("'");
            sb.append(identifier);
            sb.append("'");
        }

        String sql = "SELECT identifier from record where identifier in (" + sb.toString() + ")";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            return run.query(connection, sql, new ColumnListHandler<>(1));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Job addNewJob(Job j) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            Object[] param = { j.getTimestamp().toString(), j.getStatus(), j.getRepositoryId(), j.getRepositoryName() };
            j.getTimestamp().setNanos(0);
            String propNames = "timestamp,status,repository_id,repository_name";
            String values = "?,?,?,?";
            String sql = "INSERT INTO job (" + propNames + ") VALUES (" + values + ")";
            int rows = run.update(connection, sql, param);
            if (rows != 0) {
                Object[] selectParam = { j.getTimestamp().toString(), j.getRepositoryId() };
                sql = "SELECT * FROM job WHERE timestamp=? AND repository_id=?";
                List<Job> retList = run.query(connection, sql, resultSetToJobList, selectParam);
                if (!retList.isEmpty()) {
                    j.setId(retList.get(0).getId());
                } else {
                    sql = "SELECT * FROM job WHERE repository_id=? ORDER BY timestamp DESC LIMIT 1";
                    Object[] altSelectParam = { j.getRepositoryId() };
                    retList = run.query(connection, sql, resultSetToJobList, altSelectParam);
                }
            } else {
                throw new SQLException("Job could not be added");
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

        return j;
    }

    public static final ResultSetHandler<List<Job>> resultSetToJobList = new ResultSetHandler<List<Job>>() {
        @Override
        public List<Job> handle(ResultSet rs) throws SQLException {
            List<Job> retList = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                Job j = new Job(rs.getInt("id"), rs.getString("status"), rs.getInt("repository_id"), rs.getString("repository_name"),
                        rs.getString("message"), rs.getTimestamp("timestamp"));
                retList.add(j);
            }
            return retList;
        }
    };

    public static final ResultSetHandler<List<Record>> resultSetToRecordList = new ResultSetHandler<List<Record>>() {
        @Override
        public List<Record> handle(ResultSet rs) throws SQLException {
            List<Record> retList = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                Record r = new Record(rs.getInt("id"), rs.getTimestamp("timestamp"), rs.getString("identifier"), rs.getDate("repository_datestamp"),
                        rs.getString("title"), rs.getString("creator"), rs.getInt("repository_id"), rs.getString("setSpec"), rs.getInt("job_id"),
                        rs.getString("source"), rs.getString("exported"), rs.getTimestamp("exported_datestamp"), rs.getString("subquery"));
                retList.add(r);
            }
            return retList;
        }
    };

    public static void updateJobStatus(Job job) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Object> paramList = new ArrayList<>();
            StringBuilder sql = new StringBuilder("UPDATE job SET status=?");
            paramList.add(job.getStatus());
            if (StringUtils.isNotEmpty(job.getMessage())) {
                sql.append(", message=?");
                paramList.add(job.getMessage());
            }
            sql.append(" WHERE id=?");
            paramList.add(job.getId());
            if (new QueryRunner().update(connection, sql.toString(), paramList.toArray()) == 0) {
                throw new SQLException("Can't update status of Job with id: " + job.getId());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Record> getRecords(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters, boolean exported)
            throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Record> retList = new ArrayList<>();
            String sql = generateGetRecordsQuery(first, pageSize, sortField, sortOrder, filters, exported);
            retList = new QueryRunner().query(connection, sql, resultSetToRecordList);
            return retList;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static String generateGetRecordsQuery(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters,
            boolean exported) {
        String notNull = "";
        if (exported) {
            notNull = "NOT ";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM record WHERE exported IS ").append(notNull).append("NULL");
        if (filters != null && filters.size() > 0) {
            List<String> sortedKeys = new ArrayList<>(filters.keySet());
            Collections.sort(sortedKeys);
            for (String key : sortedKeys) {
                String value = filters.get(key);
                if (StringUtils.isNotBlank(value)) {
                    sql.append(" AND ");
                    switch (key) {
                        case "repositoryTimestamp":
                            key = "repository_datestamp";
                            sql.append(key);
                            sql.append(" LIKE '%").append(MySQLHelper.escapeSql(value)).append("%'");
                            break;
                        case "exportedDatestamp":
                            key = "exported_datestamp";
                            sql.append(key);
                            sql.append(" LIKE '%").append(MySQLHelper.escapeSql(value)).append("%'");
                            break;
                        case "repositoryId":
                            key = "repository_id";
                            sql.append(key);
                            sql.append(" = '").append(MySQLHelper.escapeSql(value)).append("'");
                            break;
                        default:
                            sql.append(key);
                            sql.append(" LIKE '%").append(MySQLHelper.escapeSql(value)).append("%'");
                    }
                }
            }
        }
        if (StringUtils.isNotBlank(sortField)) {
            if ("repositoryTimestamp".equals(sortField)) {
                sortField = "repository_datestamp";
            } else if ("exportedDatestamp".equals(sortField)) {
                sortField = "exported_datestamp";
            }
            String so = "ASC";
            if (!sortOrder) {
                so = "DESC";
            }
            sql.append(" ORDER BY ").append(sortField).append(" ").append(so);
        }
        sql.append(" LIMIT ").append(pageSize).append(" OFFSET ").append(first);

        return sql.toString();
    }

    public static void addExportHistoryEntry(ExportHistoryEntry hist) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            String sql = "INSERT INTO export_history (record_id,record_identifier,record_title,repository_id,status,message) VALUES (?,?,?,?,?,?)";
            run.update(connection, sql, hist.getRecordId(), MySQLHelper.escapeSql(hist.getRecordIdentifier()),
                    MySQLHelper.escapeSql(hist.getRecordTitle()), hist.getRepositoryId(), hist.getStatus(), MySQLHelper.escapeSql(hist.getMessage()));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void updateLastHarvestingTime(Integer repositoryId, Timestamp timestamp) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { timestamp.toString(), repositoryId };
            String sql = "UPDATE repository SET last_harvest=?";
            sql += " WHERE id=?";
            new QueryRunner().update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void setRecordExported(Record rec) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "UPDATE record SET exported='" + rec.getExported() + "', exported_datestamp='"
                    + formatterISO8601DateTime.print(rec.getExportedDatestamp().getTime()) + "' WHERE id='" + rec.getId() + "'";
            new QueryRunner().update(connection, sql);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void changeStatusOfRepository(Repository repository) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            int i = 0;
            if (repository.isEnabled()) {
                i = 1;
            }
            Object[] param = { repository.getId() };
            new QueryRunner().update(connection, "UPDATE repository SET enabled=" + i + " WHERE id= ?", param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
